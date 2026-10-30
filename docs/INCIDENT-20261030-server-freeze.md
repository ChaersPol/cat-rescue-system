# 事故复盘：2026-10-30 线上整机卡死（白屏）

> 一句话结论：**`apt-daily` 自动更新任务在凌晨 07:45 与业务争抢内存和磁盘 IO，叠加 `swappiness=0` 与 Tomcat 800 线程/60 秒超时的配置，把 1.7G 内存的机器拖进了 direct reclaim，最终全系统冻结。**
> 不是应用代码 bug，也不是新加的孤儿文件清理定时任务（该任务 03:30 正常跑完）。

---

## 1. 现象

- 用户侧：打开系统后**整页白屏**，浏览器控制台报
  `AxiosError: timeout of 10000ms exceeded`（`code: ECONNABORTED`）
- 服务侧（我从外部探测）：

| 探测项 | 结果 | 说明 |
|---|---|---|
| `ping` | 30ms，0 丢包 | 网络链路正常，不是断网/封 IP |
| 22 端口 | TCP 能建连，但 `banner exchange` 超时 | sshd 进程在，但调度不上，连 greeting 都发不出 |
| 80 端口 | TCP 能建连，HTTP 请求 20s 无响应 | nginx 收下连接，worker 阻塞 |
| 8080 端口 | **已关闭** | 后端 Java 进程已死 |

「TCP 能握手、应用层全哑」是**整机资源耗尽**的典型特征，不是防火墙问题。

---

## 2. 时间线（北京时间，均有日志佐证）

| 时间 | 事件 | 证据来源 |
|---|---|---|
| 10-29 17:12 | 部署新 jar（含 `FileCleanTask` 每日清理），部署后验证正常 | 部署记录 |
| 10-30 **03:30:01** | `FileCleanTask` 执行**正常完成**：扫描 94、孤儿 0、耗时 1141ms | `stdout.log` |
| 10-30 06:06:31 | `apt-daily-upgrade.timer` 触发 | `systemctl list-timers` |
| 10-30 07:43:57 / 07:44:57 | 提醒轮询正常，SQL 毫秒级返回 | `stdout.log` |
| 10-30 **07:45:01** | systemd 启动 `Daily apt download activities`（`apt-daily.timer`） | `journalctl -b -1` |
| 10-30 **07:46:08 → 07:46:10** | `selectHeaderReminders` 发出后**隔了 2 秒**才打印参数（正常 2ms），之后查询再没返回 | `stdout.log` |
| 10-30 **07:50:58** | `TokenService` 连续报 `Redis command timed out`（exec-55/56/58/59 同时） | `stdout.log` |
| 10-30 07:55:59 | Druid `CommunicationsException`，MySQL 连不上 | `stdout.log` |
| 10-30 08:01:21 | `/cat/reminder/header` 抛未知异常 | `stdout.log` |
| 10-30 **08:39:48** | `systemd: snapd.service: Watchdog timeout (limit 5min)!` | `journalctl -b -1` |
| 10-30 **08:40:51** | `snapd: SIGABRT`；goroutine 44 已阻塞 **47 分钟**（倒推 ≈07:53 起） | `journalctl -b -1` |
| 10-30 08:40:51 之后 | **系统日志完全中断**（连 journal 都写不进去） | `journalctl` 断档 |
| 10-30 09:29 | 用户发现白屏 | 截图 |
| 10-30 10:02 | 用户从云控制台重启 | `uptime` |

关键判读：**07:45 触发 apt → 07:46 查询变慢 → 07:50 Redis 超时**，三点在 5 分钟内依次发生，因果链清晰。

---

## 3. 根因分析

### 3.1 触发源：`apt-daily` 自动更新

Ubuntu 默认启用 `apt-daily.timer`，每天随机时间跑一次 `apt-get update`：下载并解压软件包索引（几十 MB 压缩包，解压后数百 MB 文件）。在 **2 核 / 1.7G 内存**的机器上，这会瞬间吃掉大量 CPU、内存页缓存和磁盘 IO。

`/var/log/apt/history.log` 里 10-30 这次**连 `Start-Date` 都没写进去**——说明它在下载阶段就被卡住了，随后把整机带走。

同理，`snapd` 也在后台自动刷新（事故当天 17:35 本来还有一次），它是最先被 watchdog 判定超时并 abort 的进程。

### 3.2 放大器一：`vm.swappiness = 0`

这是最关键的隐性配置。`swappiness=0` 意味着内核**几乎不用 swap**，内存紧张时不做渐进换出，而是进入 **direct reclaim（直接回收）**：正在申请内存的进程被就地阻塞，等内核腾出页。

后果：
- 进程不是被 OOM killer 干净地杀掉（那样反而能自愈），而是**全员一起卡住**
- 表现就是 MySQL 查询卡死、Redis 命令超时、sshd 发不出 banner、nginx 不响应

> 这也解释了**为什么没有任何 `Out of memory: Killed process` 记录**：内核根本没走到 oom-killer 那一步，4G swap 是空的，大家都卡在"等内存"上。

### 3.3 放大器二：应用层超时与线程数配置过大（RuoYi 默认值直接上了小机器）

| 配置 | 原值 | 问题 |
|---|---|---|
| `server.tomcat.threads.max` | **800** | 依赖一慢，几百线程同时阻塞，每线程 1MB 栈 + 上下文切换，把 CPU 也吃掉 |
| `server.tomcat.threads.min-spare` | 100 | 常驻 100 线程 |
| `server.tomcat.accept-count` | 1000 | 排队 1000 个请求 |
| `druid.maxWait` | **60000** | 拿不到连接干等 60 秒，线程只进不出 |
| `druid.connectTimeout` | 30000 | 同上 |
| `druid.socketTimeout` | 60000 | 同上 |
| `spring.data.redis.timeout` | 10s | 本机 Redis 正常是毫秒级，10 秒过长 |
| `lettuce.pool.max-wait` | **-1ms** | 负值 = 无限等待，请求可以永久排队 |
| `logging.level.com.ruoyi` | **debug** | 每条 SQL 及参数都写盘，约 7MB/天，故障时还在拼命写日志 |

### 3.4 雪崩链条

```
apt-get update 抢内存/IO
      ↓
内存压力 → swappiness=0 → direct reclaim → 进程阻塞
      ↓
MySQL 查询卡死（buffer pool 要不到内存）
      ↓
Druid maxWait 60s，Tomcat 800 线程逐个堆积、只进不出
      ↓
Redis 也拿不到 CPU/内存 → 命令超时 → 每个请求再挂 10s
      ↓
整机 IO 饱和 → snapd watchdog 5min 超时 → SIGABRT
      ↓
journal 写不进 → 日志断档 → 系统冻结，只能硬重启
```

---

## 4. 已实施的修复

### 4.1 服务器系统层（`123.57.185.59`）

| 改动 | 文件 / 命令 | 说明 |
|---|---|---|
| 禁用 apt 自动检查与自动升级 | `/etc/apt/apt.conf.d/20auto-upgrades`（改为全 0，原文件备份为 `.bak.20261030`） | 生产机不做自动升级：升级 MySQL/内核本身就可能直接搞挂服务 |
| 停用并 mask 定时器 | `systemctl disable --now apt-daily.timer apt-daily-upgrade.timer`<br>`systemctl mask apt-daily.service apt-daily-upgrade.service` | 双保险，timer 和 service 都进不来 |
| 停用 unattended-upgrades | `systemctl disable --now unattended-upgrades.service` | 省下常驻的 21MB |
| 禁止 snap 自动刷新 | `snap refresh --hold=forever` | 事故当天 17:35 本来还有一次刷新 |
| 调整 swap 策略 | `/etc/sysctl.d/99-cat-rescue.conf`：`vm.swappiness=10`、`vm.vfs_cache_pressure=150` | 允许内核温和换出，避免 direct reclaim 全阻塞 |
| JVM 内存收紧 + GC 日志 | `/etc/systemd/system/cat-rescue.service.d/override.conf` | 见下 |

JVM 新参数：
```
-Xms256m -Xmx512m -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=96m
-XX:+UseSerialGC
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/cat-rescue/logs
-XX:+ExitOnOutOfMemoryError
-Xlog:gc*:file=/opt/cat-rescue/logs/gc.log:time,uptime:filecount=3,filesize=10M
```

cgroup v2 内存水位（关键保护）：
```
MemoryHigh=720M     # 超过后内核强制回收该 cgroup
MemoryMax=850M      # 硬上限，超了只 OOM kill 这个服务，systemd 5 秒后自动重启
```

> 设计意图：**让后端「自己死一次」而不是「拖着整机一起死」**。`ExitOnOutOfMemoryError` + `Restart=always` 保证 5 秒内自愈。

### 4.2 应用层（代码）

| 文件 | 改动 |
|---|---|
| `application.yml` | Tomcat `max 800→100`、`min-spare 100→20`、`accept-count 1000→200`；Redis `timeout 10s→3s`、`max-wait -1ms→2000ms`；日志 `com.ruoyi: debug→info` |
| `application-druid.yml` | `maxWait 60000→5000`、`connectTimeout 30000→5000`、`socketTimeout 60000→30000`、`minIdle 10→5` |
| `FileStorageService.java` | 语料上限 `80MB→8MB`；单表单列扫描上限 5 万行；**扫描不完整时直接放弃清理**（原实现会拿残缺语料继续删文件，有误删风险） |
| `HeaderNotice/index.vue` | 提醒轮询改为「页面隐藏时暂停」，切回时立即补一次 |

> 日志降级说明：`com.ruoyi` 从 debug 改 info 后，不再打印每条 SQL。需要排查时临时改回 debug 并重启即可。

---

## 5. 遗留风险与后续建议

1. **内存本身仍然紧张**：修好后常态占用约 940M / 1736M（MySQL 406M + Java 375M + 其他）。这台机器是「刚好够用」，任何额外负载都危险。**如果业务量再涨，建议升到 4G 内存**，这是最彻底的解法。
2. **MySQL 内存**：`innodb_buffer_pool_size` 目前 128M，MySQL 总 RSS 却到 406M（含连接缓冲、临时表）。若后续要压内存，这里还有空间可调。
3. **不要再启用 apt 自动升级**。需要打安全补丁时，手动在低峰期执行，并先看 `apt list --upgradable` 确认不涉及内核/MySQL。
4. **监控缺失**：这次是用户先发现的，系统没有任何告警。建议加一个轻量的内存/负载看门狗（例如每 5 分钟检查 `free` 和 `/proc/loadavg`，超过阈值才写一行日志或发通知），成本几乎为零。
5. **新增后台任务务必谨慎**：任何全表扫描、大批量处理的定时任务，都要先算清在 1.7G 机器上的内存峰值，并加行数上限与「失败即放弃」的保护。

---

## 6. 下次遇到同类问题怎么查（速查）

```bash
# 1. 确认是「应用挂了」还是「整机卡死」：能建 TCP 但应用无响应 = 整机
nc -z -G 4 <ip> 22; nc -z -G 4 <ip> 80
ping -c 3 <ip>

# 2. 看历史启动的日志（journald 已持久化，重启不丢）
journalctl --list-boots
journalctl -b -1 --since "YYYY-MM-DD 07:40:00" --until "YYYY-MM-DD 07:56:00"

# 3. 找 OOM / watchdog / 卡顿证据
journalctl -b -1 -k | grep -iE "out of memory|killed process|blocked for more than|watchdog"
journalctl -b -1 | grep -i "watchdog timeout"

# 4. 看自动维护任务是不是元凶
systemctl list-timers --all | grep -E "apt|snap|dpkg"
tail -30 /var/log/apt/history.log      # 有没有当天记录（写不进去说明卡在下载阶段）

# 5. 看应用侧第一个变慢的点
grep -n "Redis command timed out\|CommunicationsException" /opt/cat-rescue/logs/stdout.log | head

# 6. 当前内存水位
systemctl show cat-rescue -p MemoryHigh -p MemoryMax
cat /sys/fs/cgroup/system.slice/cat-rescue.service/memory.current
free -m; sysctl vm.swappiness
tail -20 /opt/cat-rescue/logs/gc.log
```

---

## 7. 修复后的验证结果（2026-10-30 12:40 线上实测）

| 检查项 | 结果 |
|---|---|
| 外网首页 | `200`，**70ms**（故障时为 20s 超时） |
| 后端 `captchaImage` | `200` |
| 登录 | 成功，token 正常签发 |
| `/cat/profile/list` | `200`，24 条（首次 1.7s，之后 0.13s 级） |
| `/cat/reminder/header` | `200`，162ms |
| `/cat/wardtype/optionselect` | `200`，132ms |
| `/getInfo` | `200`，admin，角色/权限正常 |
| GC 日志 | 已生成，`Young GC 119M->53M(247M) 45ms`，堆使用健康 |
| cgroup 内存 | 359MB / 软限 720M，余量充足 |
| 系统内存 | used 896M / 1736M，available 682M |
| `apt-daily.timer` | `disabled`，已不出现于 `list-timers` |

---

## 8. 变更清单（服务器）

> 服务器重装或迁移时，以下改动需要重做；代码侧改动已进仓库。

```
/etc/apt/apt.conf.d/20auto-upgrades                      # 全 0
/etc/apt/apt.conf.d/20auto-upgrades.bak.20261030         # 原文件备份
/etc/sysctl.d/99-cat-rescue.conf                         # swappiness=10
/etc/systemd/system/cat-rescue.service.d/override.conf   # JVM 参数 + MemoryHigh/Max
systemctl mask apt-daily.service apt-daily-upgrade.service
systemctl disable unattended-upgrades.service
snap refresh --hold=forever
```
