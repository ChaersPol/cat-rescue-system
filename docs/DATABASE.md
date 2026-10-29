# 数据库规范

> 本文件是**强制规则**，所有涉及数据库的改动都必须遵守。

---

## 一、铁律

1. **全项目只保留一份数据库文件**：`backend/sql/cat_rescue.sql`
2. **禁止**再新增任何第二个 `.sql` 文件充当基线、补丁或升级包（`upgrade/` 目录除外，见第五节）
3. **本地**：随时可以用这一份文件把库整个推倒重建
4. **上线**：只做追加，**绝不**在生产库上执行基线文件

违反第 2 条最典型的错误，就是本项目以前的样子——17 个脚本按顺序 `source`，
谁都不知道少跑一步会缺什么、重跑一次会不会翻倍。

---

## 二、两种环境，两套动作

| | 本地 / 新装环境 | 生产 / 线上环境 |
|---|---|---|
| 用什么 | `backend/sql/cat_rescue.sql`（全量） | `backend/sql/upgrade/*.sql`（增量） |
| 怎么做 | 整个库删掉重建 | 按日期序号依次执行，只加不改 |
| 会不会丢数据 | 会，这是预期行为 | 不允许丢任何业务数据 |
| 执行频率 | 想跑就跑 | 每次发版前跑一次，跑过的不再跑 |

---

## 三、本地：一条命令重建

```bash
# 会先 DROP 同名表再重建，47 张表 + 初始数据 + 演示数据，导入完直接能登录
mysql -u root -p < backend/sql/cat_rescue.sql
```

- 文件里自带 `CREATE DATABASE IF NOT EXISTS cat_rescue` 和 `USE cat_rescue`，
  **这两行会覆盖命令行指定的库名**——`mysql 别的库名 < cat_rescue.sql` 依然灌进 `cat_rescue`
- 要换库名：改这两行，或用 `sed` 替换（见第八节第 6 条）
- 导入后**必须清一次 Redis 缓存**，否则字典、参数开关还是旧值：

```bash
# 只清本系统的缓存键（推荐，Redis 里还有别的项目时别用 flushdb）
redis-cli --scan --pattern 'sys_config:*' | xargs -r redis-cli del
redis-cli --scan --pattern 'sys_dict:*'   | xargs -r redis-cli del
redis-cli --scan --pattern 'captcha_codes:*' | xargs -r redis-cli del

# 确定 Redis 只给本项目用，才可以整个清空
# redis-cli flushdb
```

---

## 四、同步：本地库 → 基线文件

> **定位：`cat_rescue.sql` 是本地库的完整快照。** 真身只有本地 MySQL 里的
> `cat_rescue` 库，文件只是它在某一刻的照片——拍完就静止，库再变它不会跟着变。

**这条是日常常规操作，不是偶尔才做一次。** 只要本地库有改动就必须重新导出覆盖：

| 改动类型 | 举例 | 要不要重新导出 |
|---|---|---|
| 业务数据 | 新增一只猫、录一条病历、改支出、删演示数据 | **要** |
| 系统数据 | 新增菜单、加字典、改角色权限、调系统配置 | **要** |
| 表结构 | `ALTER TABLE` 加字段、加索引 | **要** |
| 只读操作 | 查询、导出报表 | 不用 |

不重新导出的后果：`git clone` 下来的新环境、或换台电脑部署，导入的还是旧数据，
和你本地看到的完全对不上。

**不要手工编辑 `cat_rescue.sql`**，用导出覆盖它：

```bash
cd /path/to/project

mysqldump -h 127.0.0.1 -P 3306 -u root -p \
  --default-character-set=utf8mb4 \
  --single-transaction \
  --add-drop-table \
  --no-tablespaces \
  --set-gtid-purged=OFF \
  cat_rescue > /tmp/body.sql

# 保留原文件开头的说明块，中间的建库/选库语句重建，正文换成新导出的
python3 - <<'EOF'
import io, re
p = 'backend/sql/cat_rescue.sql'
old = io.open(p, encoding='utf-8').read()
head = old.split('CREATE DATABASE IF NOT EXISTS')[0]        # 文件头说明，原样保留
body = io.open('/tmp/body.sql', encoding='utf-8').read()
body = body[body.find('/*!40101 SET @OLD_CHARACTER_SET_CLIENT'):]   # 丢掉 dump 自带版本头

# 操作日志只保留表结构，剔除数据：日志会无限增长，且含用户操作痕迹。
# 注意这里只动导出的副本，本地库一行都不删。
body = re.sub(r'^INSERT INTO `(sys_oper_log|sys_logininfor)` VALUES [^\n]*\n', '', body, flags=re.M)

prelude = ('CREATE DATABASE IF NOT EXISTS `cat_rescue` '
           'DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;\n'
           'USE `cat_rescue`;\n\n')
io.open(p, 'w', encoding='utf-8').write(head + prelude + body)
print('基线已更新')
EOF
```

导出后一定要确认这两行还在（少了就导入不到正确的库）：

```bash
grep -n "CREATE DATABASE\|^USE " backend/sql/cat_rescue.sql
```

导完务必验证一次能还原（这一步能挡掉 90% 的"上线才发现导入失败"）：

```bash
mysql -u root -p -e "drop database if exists cat_verify; create database cat_verify default character set utf8mb4"
sed -e 's/`cat_rescue`/`cat_verify`/g' backend/sql/cat_rescue.sql | mysql -u root -p
mysql -u root -p -e "select count(*) from information_schema.tables where table_schema='cat_verify'"   # 应为 47
mysql -u root -p -e "drop database cat_verify"
```

> **日志表为什么只留结构**：`sys_oper_log` / `sys_logininfor` 是运行痕迹不是业务数据，
> 会无限增长。上面的脚本在**导出副本**里剔除它们的 `INSERT`，本地库原样保留——
> 不要反过来去 `truncate` 本地库，那是真删。

---

## 五、上线：追加增量脚本

目录：`backend/sql/upgrade/`

### 命名

```
YYYYMMDD_序号_简短英文描述.sql
```

例：`20261101_01_add_chip_no_to_cat_profile.sql`

- 日期 = 编写当天，序号 = 同一天的第几个（从 01 起）
- 按**文件名字典序**执行，所以日期和序号必须严格递增

### 硬性要求

| 要求 | 说明 |
|---|---|
| 只增不改 | `CREATE TABLE IF NOT EXISTS`、`ALTER TABLE ... ADD`、`INSERT`、带条件的 `UPDATE` / `DELETE` |
| 绝对禁止 | `DROP TABLE`、`DROP DATABASE`、`TRUNCATE`、不带 `WHERE` 的 `UPDATE` / `DELETE` |
| 必须幂等 | 同一条脚本重复执行不能报错、不能产生重复数据 |
| 写清头部 | 变更目的、影响表、能否回滚、回滚语句 |
| 永不修改 | 已经执行过的脚本**冻结**。写错了就再追加一个新脚本去修正 |

### 模板

```sql
-- ============================================================
-- 变更：为 cat_profile 增加「芯片编号」
-- 影响表：cat_profile
-- 可回滚：是 → ALTER TABLE `cat_profile` DROP COLUMN `chip_no`;
-- 作者/日期：xxx / 2026-11-01
-- ============================================================

-- MySQL 8 没有 ADD COLUMN IF NOT EXISTS，用 information_schema 判断保证幂等
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'cat_profile'
                 AND COLUMN_NAME = 'chip_no');
SET @sql := IF(@exist = 0,
  'ALTER TABLE `cat_profile` ADD COLUMN `chip_no` varchar(50) DEFAULT NULL COMMENT ''芯片编号'' AFTER `archive_no`',
  'SELECT ''chip_no 已存在，跳过'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 数据类变更示例（幂等插入）
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('芯片状态', 'cat_chip_status', '0', 'admin', now(), '新增字典');
```

### 执行

```bash
# 按文件名顺序逐个跑，必须指定库名，脚本里的 DATABASE() 依赖它
for f in backend/sql/upgrade/*.sql; do
  echo ">> $f"
  mysql -u root -p cat_rescue < "$f" || { echo "失败，中断"; break; }
done

# 清缓存，让新菜单/新字典立刻生效
redis-cli --scan --pattern 'sys_config:*' | xargs -r redis-cli del
redis-cli --scan --pattern 'sys_dict:*'   | xargs -r redis-cli del
```

### 上线后

- 增量脚本在本地库也执行一遍，让本地和线上结构一致
- 然后按第四节重新导出基线，基线自动包含这次增量
- 增量脚本**保留**在 `upgrade/` 里不删，它是线上环境的执行记录

---

## 六、基线里有什么

47 张表，InnoDB / `utf8mb4` / `utf8mb4_general_ci`，无视图、无触发器、无存储过程。

| 分类 | 数量 | 表 |
|---|---|---|
| 系统框架 | 18 | `sys_user` `sys_user_role` `sys_user_post` `sys_role` `sys_role_menu` `sys_role_dept` `sys_menu` `sys_dept` `sys_post` `sys_dict_type` `sys_dict_data` `sys_config` `sys_notice` `sys_notice_read` `sys_job` `sys_job_log` `sys_oper_log` `sys_logininfor` |
| 猫咪主档 | 9 | `cat_profile` `cat_profile_tag` `cat_photo` `cat_base` `cat_ward` `cat_ward_type` `cat_ward_auth` `cat_flow` `cat_tag` |
| 医疗 | 6 | `cat_diagnosis` `cat_medication` `cat_deworm` `cat_vaccine` `cat_referral` `cat_death` |
| 领养 / TNR | 4 | `cat_adoption` `cat_visit` `cat_tnr` `cat_release` |
| 物资 / 财务 | 5 | `cat_material` `cat_material_category` `cat_stock_record` `cat_expense` `cat_fee_category` |
| 提醒 / 配置 | 3 | `cat_reminder` `cat_reminder_read` `cat_config` |
| 代码生成 | 2 | `gen_table` `gen_table_column` |

内置账号（9 个，密码以 BCrypt 哈希存在 `sys_user.password`，基线里已写好，导入即可登录）：

| 账号 | 角色 |
|---|---|
| `admin` | 超级管理员（硬编码，不出现在用户列表） |
| `manager01` | 管理员 |
| `doctor01` `doctor02` | 医生 |
| `assist01` ~ `assist03` | 助理 |
| `stock01` | 库管 |
| `adopt01` | 领养专员 |

> **密码明文不写进仓库**（仓库是 public）。明文统一记在本机
> `docs/CREDENTIALS.local.md`，该文件被 `.gitignore` 的 `*.local.md` 规则排除。
> 也就是说：**`git clone` 下来的仓库里没有这个文件**，非本机部署时密码需向项目维护者索取。
>
> 改密码的标准做法：**在系统里「个人中心 → 修改密码」改**，系统自己会算 BCrypt 哈希。
> 不要手写哈希填库——每次 `encode()` 出来的串都不一样，手抄容易写错。
>
> 要把某个账号恢复成基线里的初始密码：让管理员在「用户管理 → 重置密码」重设，
> 或者重新导入基线（会一并恢复全部 9 个账号的初始密码）。
>
> ⚠️ 首次部署后**立刻改掉 `admin` 的密码**，改完把新值记在本地凭证文件，不要提交回来。

基线含**演示数据**（24 只猫、23 张照片记录、12 条支出、8 条提醒等），
目的是新装完立刻能点开每个页面看到内容。上线时如果不需要，导入后清空业务表即可
（不要为此另存一份"空库"文件）。

---

## 七、已删除的旧脚本（对照）

2026-10-27 合并前的 17 个脚本全部并入基线后删除，历史版本见 git：

| 原文件 | 作用 | 现在 |
|---|---|---|
| `ry_20260417.sql` | 框架基础表 + 初始数据 | 已在基线 |
| `cat_rescue_20261025.sql` | 25 张业务表 | 已在基线 |
| `cat_menu_20261025.sql` | 菜单与权限 | 已在基线 |
| `cat_menu_extra_20261025.sql` | 补充菜单 + 按钮权限 | 已在基线 |
| `cat_menu_flatten_20261026.sql` | 菜单扁平化 | 已在基线（结构就是扁平后的结果） |
| `cat_dict_20261025.sql` / `cat_dict_extra_20261025.sql` | 字典 118 条 | 已在基线 |
| `cat_job_20261025.sql` | 定时任务 + 阈值配置 | 已在基线 |
| `cat_test_data_20261025.sql` | 全模块演示数据 | 已在基线 |
| `cat_org_users_20261026.sql` | 组织架构 + 角色 + 账号 | 已在基线 |
| `cat_base_ward_20261026.sql` | 基地 / 病房 / 授权 | 已在基线 |
| `cat_reminder_header_20261026.sql` | 提醒已读表 + 收件岗位 | 已在基线 |
| `cat_admin_split_20261026.sql` | 超管拆分 + manager01 | 已在基线 |
| `cat_diagnosis_correction_20261027.sql` | 诊断更正备注字段 | 已在基线 |
| `cat_remove_region_20261027.sql` | 演示数据去地区限定 | 已在基线 |
| `cat_photo_thumb_20261025.sql` | 照片缩略图字段 | 已在基线 |
| `quartz.sql` | Quartz 11 张表 | **已删**：项目用内存 JobStore，这 11 张表从未建过。将来若改 JDBC JobStore 再从 git 历史取回 |

`gen_crud_probe.py`（全模块 CRUD 回归探针）不是数据库文件，已移到本地 `.workbuddy/qa/`（不进仓库）。

---

## 八、几个容易踩的坑

1. **改了 `sys_config` 界面没变化** —— 配置有 Redis 缓存，改完执行
   `redis-cli del sys_config:<配置key>`（或按第三节批量清）
2. **`application.yml` 里的 `ruoyi.profile` 是相对路径** —— 上传目录跟着启动工作目录跑，
   照片 404 先查这个（启动日志会打印「上传目录：...」）
3. **MySQL 8 的 `utf8mb4_general_ci` vs `utf8mb4_0900_ai_ci`** —— 基线统一用 `general_ci`，
   兼容 5.7；用 `0900_ai_ci` 导到低版本库会直接失败
4. **时区** —— 全部时间列都是 `datetime` / `date`，没有 `timestamp`，
   导出导入不受时区影响，`--skip-tz-utc` 不需要加
5. **不要用 Navicat 的"运行 SQL 文件"导基线** —— 部分版本会把 `/*!40101 ... */`
   条件注释当普通注释跳过，导致字符集错误。用命令行 `mysql < file`
6. **命令行指定库名对基线无效** —— 基线里写死了 `USE cat_rescue`，它的优先级高于
   `mysql 某个库名 < 文件` 的库名参数。想把基线导进临时库做验证，必须先替换库名：

   ```bash
   # 注意 sed 表达式必须用单引号包住，反引号在双引号里会被 shell 当命令执行
   sed -e 's/`cat_rescue`/`cat_verify`/g' backend/sql/cat_rescue.sql | mysql -u root -p
   ```

   用双引号写法 `sed -e "s/`cat_rescue`/cat_verify/g"` 会让反引号被 shell 展开成空，
   替换表达式失效，导入的还是 `cat_rescue`——**这条坑会直接清掉现库数据**
