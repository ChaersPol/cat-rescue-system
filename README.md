# 流浪动物救助管理系统

基于 Spring Boot 3 + Vue 3 开发的流浪动物救助站管理系统，覆盖猫咪全生命周期档案、医疗病历、TNR 绝育放归、领养回访、财务物资、提醒通知等 20+ 功能模块，并提供扫码 H5 档案页。

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | RuoYi-Vue springboot3 分支（Java 17 + SpringBoot 3.5 + MyBatis + MySQL 8 + Redis） |
| 前端后台 | RuoYi-Vue3（Vue 3 + Element Plus + Vite + ECharts） |
| 移动端 | 同一套 RuoYi-Vue3 内实现（扫码 H5 档案页 `/m/cat/:archiveNo`） |

## 目录结构

```
├── backend/          # 后端（RuoYi-Vue springboot3）
│   ├── sql/
│   │   ├── cat_rescue.sql               # 全项目唯一一份数据库文件（47 张表 + 初始/演示数据）
│   │   └── upgrade/                     # 生产环境增量脚本（只追加，永不改动历史）
│   └── ruoyi-system/src/main/java/com/ruoyi/cat/   # 业务代码（domain/mapper/service）
│   └── ruoyi-admin/src/main/java/com/ruoyi/web/controller/cat/  # 控制器
├── frontend/         # 前端（RuoYi-Vue3）
│   └── src/views/cat/      # 业务页面（含档案详情、二维码卡片打印、移动端首页）
│   └── src/api/cat/        # 业务 API
└── docs/             # 需求文档 + DATABASE.md（数据库规范）
```

## 快速开始

### 1. 数据库初始化

> 数据库名：`cat_rescue`（独立库，避免与其他项目混用）
>
> **本项目只有一份数据库文件**：`backend/sql/cat_rescue.sql`。不要再去找 `ry_xxx.sql`、
> `cat_menu_xxx.sql` 之类的分步脚本，那些已经全部合并进这一份了。

```bash
# 一条命令搞定：建库 + 47 张表 + 菜单/字典/角色/账号 + 演示数据
mysql -u root -p < backend/sql/cat_rescue.sql

# 导入后清一下缓存，否则菜单/字典/参数还是旧值
redis-cli --scan --pattern 'sys_config:*' | xargs -r redis-cli del
redis-cli --scan --pattern 'sys_dict:*'   | xargs -r redis-cli del
```

导入完直接可以登录，账号见下方「账号与角色」。
要重建就再跑一遍这条命令（会先删表再建，本地数据会丢，这是预期行为）。

> ⚠️ **生产环境禁止执行这个文件**（含 `DROP TABLE`）。
> 上线只执行 `backend/sql/upgrade/` 下的增量脚本，规则见 [docs/DATABASE.md](docs/DATABASE.md)。

### 2. 配置后端

编辑 `backend/ruoyi-admin/src/main/resources/application-druid.yml`：

```yaml
url: jdbc:mysql://localhost:3306/cat_rescue?useUnicode=true&characterEncoding=utf8&...
username: root
password: <你的 MySQL 密码>      # 本机开发用的值见 docs/CREDENTIALS.local.md（不入库）
```

> ⚠️ 这份 yml 是要提交的（否则别人 clone 下来连不上库），所以**上线前必须把库密码改成强密码**，
> 改完后同步到本机凭证文件，不要回写到这里。

编辑 `backend/ruoyi-admin/src/main/resources/application.yml`：
- `ruoyi.profile`：文件上传路径（默认 `./uploadPath`）
- `spring.data.redis`：Redis 地址（默认 `localhost:6379` 无密码）
- `server.port`：默认 8080

### 3. 启动后端（重要）

多模块工程需先安装依赖模块，再启动 admin 模块：

```bash
cd backend
mvn install -DskipTests          # 首次或改动 ruoyi-system 后必须执行

cd ruoyi-admin
mvn spring-boot:run
```

或打包运行：

```bash
cd backend
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

> 注意：`mvn spring-boot:run` 必须在 `ruoyi-admin` 目录执行，否则会因找不到依赖模块而失败。
> 若环境变量中存在 `SERVER__PORT`，会覆盖 `application.yml` 的端口，可用 `SERVER__PORT=8080 mvn spring-boot:run` 显式指定。

#### 上传目录（图片/视频不显示时先看这里）

`application.yml` 里 `ruoyi.profile: ./uploadPath` 是相对路径，相对的是「启动时的工作目录」：
在 `backend/` 下 `java -jar` 与在 `backend/ruoyi-admin/` 下 `mvn spring-boot:run`，会解析出两个不同的 uploadPath，
结果就是文件明明存在、`/profile/**` 却全部 404，表现为**照片相册、档案相册、二维码一片空白**。

现在启动时会由 `RuoYiConfig.initProfilePath()` 自动把它规范成绝对路径，按以下顺序探测已存在的目录：

1. `<工作目录>/uploadPath`
2. `<工作目录>/ruoyi-admin/uploadPath`（本项目的实际位置）
3. `<上级目录>/uploadPath`

命中即用，都不存在才在工作目录下新建。启动日志会打印 `上传目录：/绝对路径`；
若发生自动切换会打印一条 WARN，看到这条不算错误，说明它帮你纠正了目录。
如需指定固定目录，用环境变量覆盖即可：`java -jar ruoyi-admin.jar --ruoyi.profile=/data/cat-upload`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认端口 80（可在 `vite.config.js` 修改，如改用 `npx vite --port 5173`），代理 `/dev-api` 到后端 8080。

### 5. 登录

- 地址：`http://localhost`（或实际前端端口）
- 超级管理员：`admin`（内置账号，用户管理里看不到它）
- 业务管理员：`manager01`
- **初始密码不在仓库里**，记在本机 `docs/CREDENTIALS.local.md`（已 gitignore，不会上传）
  —— `git clone` 下来的仓库里没有这个文件，非本机部署请向项目维护者索取
- 登录验证码：**已开启**（`sys_config` 中 `sys.account.captchaEnabled=true`），类型为算术验证码（`ruoyi.captchaType=math`，需填算式结果），点击图片可刷新
- 如需临时关闭验证码，在「系统管理 → 参数设置」把 `sys.account.captchaEnabled` 改为 `false`，**改完刷新登录页**即可（或直接改库后清 Redis key `sys_config:sys.account.captchaEnabled`）

## 功能模块

| 分类 | 功能 |
|---|---|
| 统计看板 | 猫咪总数/各状态分布饼图、支出结构柱图、TNR/待回访/外送/离世待办 |
| 基础信息 | 基地管理、病房管理、**病房类型**（类型字典，病房下拉与列表配色都取自这里，改名自动同步、在用类型禁止删除）、标签管理、病房授权（助理按病房授权） |
| 猫咪档案 | 档案管理（自动编号、二维码、标签、照片、**按标签/归档状态筛选**、**归档与取消归档**）、**单猫完整档案页（基础信息 + 相册 + 流转轨迹 + 病历时间线）**、流转记录（不可删除） |
| 医疗病历 | 疫苗台账、驱虫台账、站内诊断（**检查照片上传、更正备注、防篡改**）、用药记录（**连续多天批量录入、用药照片**）、转诊记录、死亡台账 |
| TNR 绝育放归 | 抓捕登记、放归登记 |
| 领养回访 | 领养登记（自动生成回访计划）、回访登记 |
| 财务物资 | 费用分类、财务支出、物资分类、物资管理、出入库记录（冲销） |
| 提醒通知 | 疫苗/驱虫/回访/库存/转诊待接回到期提醒，按岗位定向派发（收件人为空则全员可见），已读状态按账号独立；右上角铃铛展示当前用户最近 5 条 + 未读数；Quartz 定时任务每天 08:00 自动生成 |
| 导出报表 | 各模块 Excel 导出；单猫档案 PDF 导出（完整病历/流转/用药/疫苗/标签/死亡记录 + 二维码）；**二维码卡片批量打印**（A4 每行 3 张，剪下贴笼子卡） |
| 其他 | 照片相册（图片/视频自动生成缩略图，相册与列表双视图）、系统配置（扫码权限 A/B、提醒阈值与收件岗位） |
| 移动端 | 扫码查看档案与病历时间线（免登录，支持 A/B 权限模式）；`/m` 移动端首页聚合待办提醒（可逐条/一键已读） |

## 核心业务规则

- **流转联动**：发起流转（入站/发病转入治疗/外送/医院返回/领养/放归/死亡/转入姑息）自动更新猫咪状态、所在基地与病房，同时写入不可删除的流转记录
- **死亡登记**：状态自动变为「已离世」并归档，清空所在基地/病房，生成流转记录
- **领养登记**：状态变为「已领养」并归档，自动生成 3 天 / 1 个月 / 3 个月回访计划
- **放归登记**：状态变为「已放归」并归档，清空病房
- **转诊出院**：按「出院去向」联动（返回医疗基地 / 返回原归属基地 / 转入指定基地姑息照料）；安乐或离世自动归档；出院后清空外送医院名称
- **物资出入库**：联动更新库存；记录不可修改/删除，录错时通过「冲销」生成反向记录并回滚库存
- **提醒生成**：扫描疫苗、驱虫、回访、库存、外送超期未接回，自动生成提醒；同一业务对象（猫咪 + 类型 + 内容）生成过就不再重复生成，与是否已读无关；提前/过期提醒天数与收件岗位均可在系统配置中调整
- **提醒收件人**：生成时按 `reminder.post.*` 配置的岗位查出对应账号写入 `target_user`（逗号分隔，为空表示全员）；列表与顶栏铃铛都按「目标用户为空 或 含当前登录名」过滤；**超级管理员与数据范围为「全部数据」的角色（管理员）不受限制**，能看到全站提醒
- **提醒已读**：记录在 `cat_reminder_read(reminder_id, user_id)`，每个账号各记各的，谁点已读不影响别人；删除提醒时同步清理已读记录
- **通知公告**：即框架自带的 `sys_notice`，供发布正式公告用，入口在「系统管理 → 通知公告」，**不再占用右上角铃铛**
- **物资预警**：库存 ≤ 预警阈值时列表标红并显示「库存不足」，可按库存状态筛选
- **扫码档案**：二维码内容为 `{scan.base.url}/m/cat/{档案编号}`，免登录访问
- **病历防篡改**：历史诊断只有内置超级管理员能改正文/删除，医生、管理员、助理一律只能追加「更正备注」，原始记录保持不变（`cat_diagnosis.correction_note/correction_by/correction_time`）
- **档案归档**：离世/放归/领养的猫可一键归档（`archived='1'`），列表默认全部可见并支持「未归档 / 已归档」筛选，误归档可随时取消，不做物理删除
- **标签筛选**：档案列表可按多个标签同时筛选（交集匹配），统计看板点标签可直接跳到对应猫群
- **连续用药**：用药记录支持「开始日期 + 连续天数」批量生成，同一天同一时间段同一种药自动跳过，重复提交不会产生重复台账

## 定时任务

基线里的 `sys_job` 已预置一条 Quartz 任务，可在「系统监控 → 定时任务」中查看/启停/手动执行一次：

| 任务名 | 调用目标 | Cron | 说明 |
|---|---|---|---|
| 猫咪到期提醒自动生成 | `catTask.generateReminder()` | `0 0 8 * * ?` | 每天 08:00 扫描并生成提醒（幂等，不会重复插入） |

对应实现：`backend/ruoyi-system/src/main/java/com/ruoyi/cat/task/CatReminderTask.java`

## 单猫档案 PDF 导出

入口：「猫咪档案」列表 → 每行「档案PDF」按钮，或直接请求 `GET /cat/profile/pdf/{catId}`。

导出内容：基本信息 + 二维码、标签、疫苗、驱虫、诊断、用药、外送转诊、流转轨迹、死亡记录。

PDF 中文依赖 TTF 字体，按以下顺序自动探测：

1. `cat_config` 中的 `pdf.font.path`（可选，用于替换成自有字体）
2. **jar 内置字体 `fonts/cjk.ttf`（Noto Sans SC Regular，OFL-1.1）**——默认命中，开箱即用
3. 常见系统字体：macOS `Arial Unicode.ttf`、Linux Noto CJK、Windows `simhei.ttf/msyh.ttf`

内置字体已覆盖简体中文与拉丁字母，部署到没有装中文字体的服务器也能直接导出，
**正常情况下无需任何配置**。只有在想换字体时才需要在「系统配置」新增 `pdf.font.path`，
指向一个包含中文和拉丁字形的 TTF 文件（不要用 `.ttc` 字体集合）。

> 换字体时注意：有些 CJK 字体（如 `DroidSansFallback.ttf`）**只带中文字形、不带数字和字母**，
> 换成这类字体后 PDF 里的编号、日期、数量会变成空白。

> 前端下载约定：RuoYi 的 `proxy.download()` 固定 `POST`，只适用于导出接口；本档案 PDF 是 `GET` 流式接口，必须用 `proxy.downloadGet(url, params, filename)`（`src/utils/request.js` 新增，内部以 `responseType: 'blob'` 走 GET），否则会返回 405。

## 单猫完整档案页

入口：「猫咪档案」列表 → 每行「档案」按钮，路由 `/profile/detail/{catId}`。

一次性展示：基础档案、标签、照片相册、流转轨迹、领养/死亡信息，以及按时间倒序合并的**病历时间线**（诊断 / 用药 / 疫苗 / 驱虫 / 转诊 / 流转 / 死亡 / TNR / 放归 / 领养），右上角带二维码（可下载/重新生成）。对应需求文档 6.6「病历时间线展示」与 4.2「档案内可查看完整流转轨迹」。

数据来自聚合接口 `GET /cat/profile/archive/{catId}`（需登录，`cat:profile:query`）。

## 二维码卡片打印

入口：「猫咪档案」→「二维码卡片打印」，或档案详情页「打印二维码卡」，路由 `/profile/qrcard`。

- 按关键词 / 基地 / 状态筛选并勾选猫咪，A4 纸每行 3 张、每页 12 张
- 卡片内容：名字、档案编号、品种/性别/年龄、所在基地、当前状态、二维码
- 点「打印选中卡片」调起浏览器打印，打印样式已隐藏左侧菜单与筛选区，只输出卡片（@media print）
- 未生成二维码的猫咪会在卡片上显示「无二维码」，可先回档案页生成

## 病历更正备注

需求文档 15 要求「历史病历只允许新增补充，禁止直接修改旧诊断」。

| 角色 | 修改诊断正文 | 删除诊断 | 追加更正备注 |
|---|:--:|:--:|:--:|
| 超级管理员 | ✓ | ✓ | ✓ |
| 管理员 / 医生 | ✗（提示改用更正备注） | ✗ | ✓ |

更正内容写在 `cat_diagnosis.correction_note`，同时记录 `correction_by` / `correction_time`，档案时间线里以黄色「更正」块展示，原始诊断字段不动。

## 移动端首页（待办提醒）

路由 `/m`，需登录（登录态缓存在浏览器，可退出）。

- 顶部显示当前账号、未读提醒数、可见提醒总数
- 聚合提醒列表按岗位派发规则只显示与自己相关的提醒，点条目即标记已读，支持「全部已读」
- 快捷入口：扫码查档案（输入档案编号）、猫咪档案、用药打卡、提醒中心
- 接口：`GET /cat/mobile/reminders`、`PUT /cat/mobile/reminders/read/{id}`、`PUT /cat/mobile/reminders/readAll`

## 表单草稿缓存

需求文档 13「未保存内容临时缓存，防止误关闭丢失」。

`src/utils/formDraft.js` 提供 `useFormDraft(key, form)`：表单打开即开始监听并防抖写入 `localStorage`，取消关闭不清草稿，下次打开自动恢复；保存成功后自动清除。已接入：猫咪档案、站内诊断、用药记录、转诊记录、死亡台账、领养登记、TNR 抓捕、放归登记。刷新页面时若草稿未保存会弹浏览器确认框。

## 前端表单与列表约定

| 场景 | 约定 |
|---|---|
| 枚举字段（状态、类型、结果、方式等） | 一律 `<el-select>` / `<el-radio-group>` 绑 `useDict()` 返回的字典，列表用 `<dict-tag>` 渲染中文标签，**不得**用 `<el-input>` 手输或直接输出数字码值 |
| 日期 / 时间字段 | 一律 `el-date-picker`（纯日期 `type="date"`、带时分秒 `type="datetime"`），`value-format` 与后端字段格式一致 |
| 日期区间查询 | 页面 `dateRange = ref([])` + `<el-date-picker type="daterange">`，查询时 `proxy.addDateRange(queryParams.value, dateRange.value)`；后端 Mapper 接收 `params.beginTime / params.endTime` 拼 `date_format(col,'%y.%m.%d')` 比较 |
| 外键（猫咪/基地/病房） | 列表用 `@/utils/catCache` 的 `catLabel() / baseLabel() / wardLabel()` 翻译成名称；表单一律用 `<cat-select>` 或对应下拉，不手输 ID |
| 菜单路由 | `sys_menu.path` 在各分支下可重复，但 **路由 name 必须全局唯一**。后端规则是 `route_name` 为空时取 `capitalize(path)`，所以不同分支里 path 相同（如 `config`）会生成同名路由，vue-router 后注册的会把先注册的整条删掉 → 表现为点菜单 404。冲突时在「菜单管理 → 修改 → 路由名称」填唯一值（如 `CatConfig`），URL 不受影响 |

> 加完菜单后建议自查（返回空即无冲突）：
> ```sql
> select ifnull(nullif(route_name,''), path) as route_key, count(*) cnt
> from sys_menu where menu_type = 'C' and status = '0'
> group by route_key having cnt > 1;
> ```

## 照片/视频缩略图

上传照片或视频并保存后，系统自动生成缩略图，相册/列表页加载缩略图而非原图。

| 类型 | 生成方式 | 输出 |
|---|---|---|
| 图片（jpg/png/gif/bmp） | JDK ImageIO 等比缩放到 400px 宽 | 原生图同目录 `thumb/xxx_thumb.jpg`，JPEG 质量 0.85 |
| 视频（mp4/mov/m4v/avi/webm/mkv/flv） | ① 系统 ffmpeg 抽第 1 秒画面 → ② 纯 Java jcodec 解码取帧（H.264） | 同上，400px 宽 |

降级规则：

- 图片宽度 ≤ 400px 且体积 < 120KB：不生成，直接用原图
- 视频两种方式都失败（如 HEVC 编码）：`thumb_url` 留空，前端用 `<video preload="metadata">` 首帧兜底展示
- 缩略图文件丢失/损坏：前端自动回退加载原图（`thumbSrc()` + `@error`），不会出现整块空白占位
- 外部链接图片（非本机 `/profile/` 路径）：跳过，由外部 CDN 自行缩放

配置项（`application.yml`）：

```yaml
thumbnail:
  enabled: true            # 总开关
  video:
    enabled: true          # 视频抽帧开关
  ffmpeg:
    path: ffmpeg           # 未安装可留空，会自动走 jcodec
    timeout: 20            # 单次抽帧超时（秒）
```

安装 ffmpeg 可提升视频格式兼容性（brew install ffmpeg / apt install ffmpeg）；**不装也能用**，jcodec 已覆盖手机拍摄的绝大多数 mp4/mov。

存量数据补齐：相册页点「重建缩略图」按钮，或 `POST /cat/photo/rebuildThumb`（可带 `catId`、`fileType` 过滤）。

上传大小限制：图片 10MB、视频 50MB（`spring.servlet.multipart.max-file-size: 50MB`）。视频扩展名白名单见 `MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION`。

## 演示素材

测试数据的照片相册使用**真实猫咪照片**，随项目一起提供，不依赖任何外部图床，断网也能正常显示：

```
backend/ruoyi-admin/uploadPath/upload/demo/cat/
├── photo-100.jpg ~ photo-121.jpg   # 22 张主图，1200×800，与 photo_id 一一对应
├── cat-video-01.mp4                # 1 段猫咪视频（9 秒 / 960×544 / H.264）
└── thumb/                          # 对应缩略图，400×267
```

- 数据库中以 `/profile/upload/demo/cat/...` 相对路径引用，部署换目录时只需整体拷贝 `uploadPath`
- 相册列表用缩略图（合计约 470KB），点开大图加载原图，兼顾速度与清晰度
- 图片文件不在 SQL 里，随仓库一起提交在 `backend/ruoyi-admin/uploadPath/`；清空了从 git 恢复该目录即可
- 视频封面由内置 jcodec 解码生成，用于验证「无 ffmpeg 环境下的视频抽帧」链路

## 扫码权限配置

在「救助站管理 → 系统配置」中维护（对应表 `cat_config`）：

| 配置键 | 说明 |
|---|---|
| `scan.permission.mode` | `A` 公开模式（扫码仅看基础信息，医疗记录需登录）/ `B` 内部模式（扫码看全部病历） |
| `scan.base.url` | 扫码前端地址前缀，如 `https://cat.example.com`（影响新生成二维码内容；留空或填 `localhost` 时自动取当前访问地址） |
| `scan.mobile.hint` | 扫码页顶部提示语 |
| `reminder.advance.days` | 疫苗/驱虫到期提前提醒天数（默认 7） |
| `reminder.overdue.days` | 疫苗/驱虫过期后仍提醒天数（默认 7） |
| `referral.pending.days` | 外送医院超期未接回提醒天数（默认 7） |
| `pdf.font.path` | 单猫档案 PDF 使用的中文 TTF 字体路径 |
| `reminder.auto.enabled` | 是否启用提醒自动生成（1 开启 / 0 关闭） |
| `reminder.post.vaccine` | 疫苗提醒收件岗位，逗号分隔（默认 主治医生,住院医师） |
| `reminder.post.deworm` | 驱虫提醒收件岗位（默认 主治医生,住院医师） |
| `reminder.post.visit` | 回访提醒收件岗位（默认 领养回访专员） |
| `reminder.post.stock` | 库存提醒收件岗位（默认 物资管理员） |
| `reminder.post.referral` | 转诊提醒收件岗位（默认 主治医生,住院医师） |

> 收件岗位留空表示全员可见；岗位下没有启用账号时同样回退为全员。改完配置后下一次生成提醒时生效，无需重启。

修改 `scan.base.url` 后，可在「猫咪档案」页面点击「批量补二维码」为全部猫咪重新生成。

> 二维码内容 = `scan.base.url` + `/m/cat/{档案编号}`。若该配置为空、或填的是 `localhost` / `127.0.0.1`
> 这类手机访问不到的地址，后端会自动改用**当前请求的访问地址**（`X-Forwarded-Proto` / `Host` 头，兼容 nginx 反代），
> 因此用 IP 直连部署时即使忘了改配置，扫出来的也是真实地址。配置里填了有效地址时仍以配置为准。

## 测试数据说明

`backend/sql/cat_rescue.sql` 自带全模块演示数据，覆盖情况如下（重灌基线即重置为这份数据）：

| 模块 | 数量 | 覆盖场景 |
|---|---|---|
| 基地 / 病房 / 标签 | 4 / 10 / 10 | 饲养基地 A/B、医疗基地、放归点位；普通/隔离/术后/治疗/幼猫/康复病房 |
| 猫咪档案 | 24 | 覆盖全部 8 种状态（在养 7、基地内治疗 4、外送 2、已放归 3、已领养 3、已离世 2、TNR 治疗 2、姑息照料 1） |
| 流转记录 | 34 | 入站、发病转入治疗、治愈返回、外送、医院返回、放归、领养、死亡 |
| 疫苗 / 驱虫 | 10 / 10 | 已过期、7 天内到期、未来到期 |
| 诊断 / 用药 | 7 / 12 | 含不良反应记录 |
| 转诊 | 4 | 治疗中（含超 7 天待接回）、已出院、已离世 |
| 死亡 | 2 | 自然寿终、安乐 |
| TNR / 放归 / 领养 / 回访 | 6 / 3 / 3 / 9 | 待定、放归、转基地；待回访、已回访、回访异常 |
| 支出 / 物资 / 出入库 | 12 / 10 / 17 | 物资采购、医疗外送、其他支出；库存充足/预警/临界；入库/出库/冲销 |
| 提醒 | 8 | 手工演示数据；点「生成到期提醒」后会再生成 15 条真实业务提醒，合计 23 条 |
| 病房授权 | 10 | 3 名助理分别管理 A 基地 / B 基地 / 医疗基地的不同病房，互不重叠 |
| 照片 | 23 | 救助时/治疗中/康复后/领养后；22 张真实猫咪照片 + 1 段猫咪视频，缩略图均已生成 |

导入后可在「提醒通知」点击「生成到期提醒」，会基于疫苗/驱虫/回访/库存/转诊数据生成 15 条提醒，并按 `reminder.post.*` 配置自动指派收件人（重复点击不会重复生成）。

## 账号与组织架构

对应需求文档 3.1「管理员 / 医生 / 助理」三类角色，另加一个系统内置的超级管理员。账号与授权已内置在基线文件（`sys_user` / `sys_role` / `sys_role_menu`）：

- 账号清单见下表；**密码不写入仓库**，明文记在本机 `docs/CREDENTIALS.local.md`（已 gitignore）
- 密码都是 BCrypt 哈希存库，基线里已经写好，导入后直接可登录，不用手工重置

| 账号 | 姓名 | 角色 | 部门 | 岗位 | 说明 |
|---|---|---|---|---|---|
| admin | 系统超管 | **超级管理员** | 站务管理部 | 站长 | 系统内置账号，拥有全部权限；**不在用户管理列表中显示** |
| manager01 | 李文 | 管理员 | 站务管理部 | 站长 | 站长，管理全站业务与基础数据 |
| doctor01 | 王建国 | 医生 | 医疗基地诊疗组 | 主治医生 | 诊断/转诊/死亡登记，物资财务只读，不可删档案 |
| doctor02 | 陈静 | 医生 | 医疗基地诊疗组 | 住院医师 | 同上 |
| assist01 | 张小敏 | 助理 | A基地区护理组 | 护理助理 | 只看到 A 基地 3 个病房的猫（4 只） |
| assist02 | 赵丽 | 助理 | B基地区护理组 | 护理助理 | 只看到 B 基地 3 个病房的猫（4 只） |
| assist03 | 孙倩 | 助理 | 医疗基地诊疗组 | 护理助理 | 只看到医疗基地 4 个病房的猫（8 只） |
| stock01 | 周涛 | 助理 | 后勤物资部 | 物资管理员 | 物资出入库登记 |
| adopt01 | 吴倩 | 助理 | 领养回访组 | 领养回访专员 | 领养登记与回访 |

### 超级管理员（硬编码保护）

`role_id = 1` 的角色与 `user_id = 1` 的账号是系统内置的超级管理员，后端按 ID 硬编码保护，不依赖任何配置：

| 保护点 | 实现位置 | 行为 |
|---|---|---|
| admin 不出现在用户列表 | `SysUserMapper.xml` → `selectUserList` 加 `AND u.user_id != 1` | 列表、导出、分页总数都不含 admin |
| admin 不参与角色分配 | `selectAllocatedList` / `selectUnallocatedList` 同样排除 | 任何角色的「分配用户」里都选不到 admin |
| admin 详情不可查看 | `SysUserController.getInfo` 校验 `userId == 1L` | 非超管请求 `/system/user/1` 返回「不允许查看超级管理员账户」 |
| 超管角色不显示 | `SysRoleServiceImpl.selectRoleList` 对非超管注入 `excludeSuperAdmin` | `manager01` 的角色列表只有 3 个，看不到「超级管理员」 |
| 超管角色不可查看/分配 | `SysRoleController.getInfo`、`authUser/*` 系列校验 `roleId == 1L` | 直接调接口返回空列表或错误提示 |
| 超管角色不可分配 | `SysUserServiceImpl.insertUserRole` 过滤 `roleId == 1L` | 新增/编辑用户时塞 `roleIds:[1]` 会被丢弃或拒绝 |
| admin 不可改不可删 | 框架原生 `checkUserAllowed` / `checkRoleAllowed` | 修改、删除、重置密码、改状态全部拒绝 |

> 因此 admin 的昵称、部门、密码只能在自己的「个人中心」里改，用户管理里看不到也动不了它。

### 业务管理员角色

| 角色 | role_id | 数据权限 | 说明 |
|---|---|---|---|
| 超级管理员 | 1 | 全部 | 内置，`isAdmin` 直接放行全部权限，不读菜单元 |
| 管理员 | 4 | 全部 | 234 个菜单（10 目录 + 43 页面 + 181 按钮），可管理用户、角色、参数 |
| 医生 | 2 | 自定义 | 84 个菜单 |
| 助理 | 3 | 自定义 | 64 个菜单 |

> ⚠️ 在「角色管理」里编辑「管理员」角色时，注意**系统管理那一组菜单别漏勾**，否则保存后管理员会立刻失去用户/角色等页面的权限（左侧菜单只剩业务部分）。恢复办法：重新把「系统管理」整组勾上保存；或者从 git 历史取回 `cat_admin_split_20261026.sql` 单独执行第 4 步。

组织架构（部门管理）：

```
流浪猫救助中心
├─ 站务管理部
├─ 医疗部 ── 医疗基地诊疗组
├─ 护理部 ── A基地区护理组 / B基地区护理组
├─ 领养回访组
└─ 后勤物资部
```

角色权限差异（与需求文档逐条对齐）：

| 能力 | 管理员 | 医生 | 助理 |
|---|:--:|:--:|:--:|
| 诊断新增 / 修改 | ✓ | ✓ | ✗（不可改历史诊断） |
| 用药录入 | ✓ | ✓ | ✓（每日打卡） |
| 档案新增 | ✓ | ✗ | ✓ |
| 档案删除 | ✓ | ✗ | ✗ |
| 标签库维护 | ✓ | ✗ | ✗ |
| 物资入库/出库登记 | ✓ | ✗（只读） | ✓ |
| 财务支出录入 | ✓ | ✗（只读） | ✗（只读） |
| 系统配置 / 账号权限 | ✓ | ✗ | ✗ |
| 病房授权划分 | ✓ | ✗ | ✗ |

### 助理按病房隔离数据

需求文档 15 明确「A 助理管犬房、犬猫房，B 助理管普通病房，管理员可划分」：

- 病房归属：`cat_ward`（基地 → 病房）
- 授权关系：`cat_ward_auth`（用户 → 病房），在「基础信息 → 病房授权」页面按基地分组勾选
- 猫咪归属：`cat_profile.ward_id`，档案列表可查看/编辑「所在病房」（窄屏下与基地合并为「所在位置」一列）

生效范围：助理登录后，**猫咪档案列表**和**统计看板**只统计被授权病房的猫（未授权任何病房则看不到档案）；管理员与医生不受限制。

> 注意：疫苗、用药、转诊等台账页目前仍按全量展示，只做了档案与看板两处的隔离。

## 已验证的端到端场景

- 新增档案：自动生成档案编号 `CAT{yyyyMMdd}{4位随机}`、二维码（Base64），标签关联生效
- 流转：发起「发病转入治疗」→ 状态 1→2、基地/病房更新、转出位置自动记录
- 领养：状态→已领养、归档、自动生成 3 条回访计划
- 死亡：状态→已离世、归档、基地/病房清空、流转记录写入
- 库存：入库 +10、出库 -4 联动库存；冲销生成反向记录并回滚
- 转诊出院：状态→基地内治疗中、清空病房与外送医院名、生成流转记录
- 提醒：首次生成 15 条，重复生成 0 条；单条/批量标记已读生效（重复标记已读返回 200，幂等）
- 提醒定向派发：库存→`stock01`、回访→`adopt01`、疫苗/驱虫/转诊→`doctor01,doctor02`；助理 `assist01` 只能看到全员提醒
- 提醒按人已读：`doctor01` 标记 1 条后未读 5→4，同时段 `admin`/`assist01`/`stock01`/`adopt01` 未读数不变；`stock01` 一键全部已读只清零本人
- 顶栏铃铛：`admin` 角标 20、`doctor01` 角标 13，面板仅显示各自可见的前 5 条；`manager01` 因数据范围为全部，角标 23（全站 23 条）
- 超级管理员隔离：`admin` 与 `manager01` 的用户列表均为 8 条、不含 admin；`admin` 角色列表 4 条、`manager01` 只有 3 条
- 超管越权拦截：`manager01` 请求 `/system/user/1` → 「不允许查看超级管理员账户」，`/system/role/1` → 「不允许查看超级管理员角色」，`roleId=1` 的分配用户接口返回 0 条
- 防提权：`manager01` 新增用户时塞 `roleIds:[1,4]` 被拒（500 没有权限访问角色数据），只传 `[4]` 正常创建
- 扫码：B 模式返回病历时间线；A 模式返回 `needLogin=true` 且不含病历
- 病房授权：按用户保存授权（先删后增）
- 库存预警：`stockStatus=low` 返回 4 条、`ok` 返回 6 条；名称模糊搜索生效
- 单猫档案 PDF：`GET /cat/profile/pdf/100` 返回 `application/pdf`，中文正常渲染
- 定时任务：Quartz 注册成功，手动执行一次日志输出「新增 0 条提醒」（幂等）
- 全模块 CRUD：23 个模块「列表 / 新增 / 详情 / 修改 / 删除」全部通过（自动回归探针，跑完自清理）
- 图片缩略图：上传 1200×900 / 238KB 图片 → 自动生成 400×300 / 63KB 缩略图，体积降 73%，HTTP 200 可访问
- 视频封面：上传 mp4（H.264）→ 无 ffmpeg 环境下由 jcodec 抽帧生成 400×225 / 29KB 封面，画面正确
- 缩略图重建：清空 `thumb_url` 后调用 `POST /cat/photo/rebuildThumb`，3 条记录全部恢复

## 需求文档对齐情况（本次核对结论）

按《流浪动物救助管理系统 功能需求文档-定档》逐条核对，本次补齐了以下原本缺失或不到位的能力：

| 需求条目 | 原状况 | 处理 |
|---|---|---|
| 6.6 病历时间线展示（单猫档案页合并时间线） | 仅移动端扫码页有时间线，PC 档案页只有一个简陋弹窗 | 新增 `GET /cat/profile/archive/{catId}` 聚合接口 + `/profile/detail/{catId}` 完整档案页，9 类记录按时间倒序合并 |
| 4.2 档案内查看完整流转轨迹 | 仅流转记录独立菜单可见 | 档案详情页内置流转轨迹表（时间/类型/转出→转入/原因/操作人） |
| 5.1 档案搜索支持按标签批量筛选 | 后端支持 tagIds，前端无入口；且 SQL 的 `#{tagIds.length}` 会抛 ReflectionException | 前端加标签多选筛选；SQL 改用 `<bind name="tagSize">` 修复数组长度取值 |
| 5.3 归档隐藏（替代物理删除） | `archived` 字段存在但无入口，归档后无法恢复 | 列表加归档/取消归档按钮与归档筛选、归档标记 |
| 5.1 编辑档案时标签回填 | `selectCatProfileById` 不返回 tagIds，编辑时标签显示为空 | Service 回填 `tagIds` |
| 5.2 / 11 二维码下载与卡片排版打印 | 只能看 Base64 图，无下载/打印 | 详情页支持下载二维码；新增 `/profile/qrcard` 批量打印页（A4 3 列、打印样式隔离） |
| 6.2 诊断上传检查照片/化验单 | `check_photos` 字段存在但无上传控件 | 诊断表单加图片上传 |
| 6.3 批量录入连续多天用药 + 用药照片 | 只能一天一天录 | 新增 `POST /cat/medication/batch?days=N`，同日同药自动去重；表单加照片上传 |
| 11 统计看板指标 | 只有状态分布、支出结构和 4 个待办 | 补各基地数量、待疫苗、待驱虫、TNR 绝育数、领养数、回访异常、死因分布、标签统计、本月支出 |
| 12 移动端首页聚合提醒 + 已读 | 移动端只有免登录扫码页 | 新增 `/m` 移动端首页与 `GET /cat/mobile/reminders` 等接口，支持逐条/一键已读 |
| 3.2 登录支持手机号 | 只支持用户名 | 登录时账号匹配不到则按手机号查（`selectUserByPhoneNumber`），登录页提示「账号 / 手机号」 |
| 13 表单未保存内容临时缓存 | 无 | 新增 `src/utils/formDraft.js`，接入 8 个主要表单，误关闭可恢复 |
| 15 历史病历禁止修改、只能加更正备注 | 诊断可随意改删 | 后端限制只有超管可改正文/删除，其余角色走 `PUT /cat/diagnosis/correction` 追加更正；前端列表标「已更正」 |
| 9.2 采购关联入库单、票据截图上传 | 字段存在但表单没有 | 财务支出表单加「关联入库单」下拉与票据/截图上传 |

仍存在差异（未做，属后续可裁剪范围）：

- **移动端现场快速新增记录**：需求 24 行提到「扫码后支持快速新增记录」，当前扫码页仍为只读，新增记录需登录 PC 后台或移动端首页跳转列表页
- **领养台账导出含回访状态**：当前领养导出为领养记录本身，回访状态在「回访记录」里单独导出
- **「就医中」状态**：需求 7.2 提到放归猫再次发病时状态临时改为「就医中」，现复用「基地内治疗中」，未新增独立状态字典值

## 待完善项

- 公网部署时注意上传目录（`uploadPath`）的备份，`upload/demo/**` 是随仓库分发的演示素材
- 移动端目前是「PC 页面 + 移动端专用首页/扫码页」的混合形态，未做完整的移动端路由与手势优化
- 疫苗、用药、转诊等台账页的数据范围仍按全量展示，只有猫咪档案与统计看板做了病房授权隔离（见上文）
