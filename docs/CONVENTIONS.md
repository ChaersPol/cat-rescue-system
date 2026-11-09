# 前端 / 导出 / 打印 统一机制约定

> 本文件是**强制规则**，新增页面、改表格、改导出、做打印页前先看对应章节。
> 项目根 `.workbuddy/memory/MEMORY.md` 只保留规则索引，细节都在这里。

---

## 一、Excel 导出四条铁律

1. **实体字段必须带 `@Excel`**，否则 `ExcelUtil.getFields()` 返回空，导出的 xlsx 只有空行、一个单元格都没有。
   2026-10-27 就因 24 个 cat 实体注解被整体剥掉，全站业务导出全废。新增/重构实体时别丢注解。
2. **导出 URL 必须与 `@RequestMapping` 完全同大小写**（本项目 cat 映射全小写）。写错时 POST 落到静态资源处理器，
   返回 `{"msg":"No static resource ..."}` 且 HTTP 仍是 200，前端表现为"下载失败"。
3. **验证 xlsx 不能用 `sharedStrings` 计数**：本项目 POI 输出 inlineStr（`<c t="inlineStr"><is><t>`），
   `sharedStrings.xml` 恒为 0。要判断有没有数据，直接解析 `sheet1.xml` 数 `<c r=` 或读 `<t>`。
4. **外键列导出显示名称**：走 `CatRefNameMapper`（格式集中在 SQL）+ `CatRefNameFiller`（反射回填 `xxxRefName`）。
   新增外键三步：实体加 `xxxRefName` 影子字段并挂 `@Excel`、
   `CatRefNameFiller.RULES` 加一条规则、Controller 的 `export` 里加 `refNameFiller.fill(list);`

---

## 二、表格勾选列 / 操作列的权限门控

判断依据由**后端下发到路由 meta**，页面模板里不出现权限字符串：

- 勾选列：`<el-table-column type="selection" ... v-if="$route.meta.canRemove" />`
- 工具栏批量按钮（`:disabled="single|multiple"` 那些）：同样 `v-if="$route.meta.canRemove"`
- 操作列：保留每页 `checkPermi([...])` 数组（列内放哪些按钮是模板静态信息，后端推不出来）

`canRemove` 的来源：`MetaVo.canRemove` + `SysMenuServiceImpl.resolveCanRemove(menu, userPerms)`。
菜单自身 perms 固定是 `模块前缀:list`，把末段换成 `:remove` 查用户权限集；`*:*:*` 直接 true。
新增模块只要在菜单里建了「删除」按钮就自动生效，页面模板不用改。

**三条必须记住的约束**

1. **不能用 CSS/DOM 隐藏表格整列**。Element Plus 列宽由 `colgroup/col` 决定，
   `display:none` 只藏单元格不释放 col，整行会串位（实测猫咪列 210px 被压成 55px）。只能 `v-if`。
2. **菜单树 SQL 不含 F 按钮**：`selectMenuTreeByUserId`/`selectMenuTreeAll` 限定 `menu_type in ('M','C')`，
   `menu.getChildren()` 拿不到按钮节点，别指望从子节点判断权限。
3. **静态详情页靠 `activeMenu` 继承**：字典数据、调度日志、分配用户这类 `router/index.js` 里的静态子路由
   没有后端 meta，由 `permission.js` 守卫从 `meta.activeMenu` 指向的主菜单 resolve 出 canRemove。

**有意不跟删除走的页面**：`tool/gen`（勾选用于批量生成代码）、`flow`/`stockrecord`（冲销回滚）、
`wardauth`（授权矩阵）、`system/menu`/`system/dept`（树表）。

---

## 三、表格「序号」列（跨页连续）

序号由全局方法算，**不要让页面自己写 `$index + 1`**（那样翻页会从 1 重新开始）：

```html
<el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
```

- 定义在 `frontend/src/main.js`：`(pageNum-1)*pageSize + index + 1`
- 位置固定在**勾选列之后**；无勾选列的页面即第一列
- 不分页的表（详情、打印页）传 `$seqIndex(i)`；前端 `slice` 分页的传 `$seqIndex(i, { pageNum, pageSize })`
- 生成器模板已同步（`vm/vue/{v3,v3ts,}/index.vue.vm`），新生成的页面自带
- **树表不加**（`system/menu`、`system/dept`）：层级展开后序号会跳，反而误导
- 已有手写跨页序号的页面别重复加：`monitor/online`、`system/user/authRole`、`tool/gen/index`、
  `system/notice/ReadUsers`、`monitor/cache/list`

---

## 四、猫咪在所有列表里的展示写法

**固定为「名字（档案编号）」，如 `毛豆（CAT202610180024）`。用户 2026-10-28 明确要求，全站一致。**

- 格式只在 `frontend/src/utils/catCache.js` 的 `formatCatLabel(catName, archiveNo)` 里定义，**页面不许自己拼字符串**
  （历史上 12 处下拉框写的是 `c.catName + '（' + c.archiveNo + '）'`，改格式要全站翻一遍）
- 列表单元格两种取数方式，都走同一格式：
  - 页面没有本地 options：`const { catLabel } = useCatCache()` → `{{ catLabel(row.catId) }}`
  - 页面已为下拉框加载了 `catOptions`：`import { catLabelOf }` → `const catName = id => catLabelOf(catOptions, id)`
- 下拉框统一 `:label="formatCatLabel(c.catName, c.archiveNo)"`；`<cat-select>` 组件内部已按此渲染（名字 + 灰色括号编号）
- 名字为空显示「未命名」；档案编号为空只显示名字，不出现空括号；catId 查不到时显示 `#id`
- 列宽：猫咪列给 `210~220px`，否则 `毛豆（CAT202610180024）` 会被截断
- Excel 导出侧对应 `CatRefNameMapper.selectCatLabels`（同样是「名字（档案号）」），两边要一起改
- **自查命令**：`grep -rn "catName\|catLabel" views/cat`，凡是单元格里只输出 `c.catName` 的都是漏的

---

## 五、打印页面（@media print）三条铁律

做任何「所见即所得 + 真的打印出来」的页面（现在只有 `cat/profile/qrcard.vue` 二维码信息卡）时必须先处理：

1. **`.main-container` 带 `margin-left: 200px`**（侧栏宽度）。侧栏 `display:none` 后外边距还在，
   整块内容右移出纸、右边被裁。→ `@media print { .main-container { margin-left: 0 !important } }`
2. **外层固定高度 + overflow** 会把整篇压成一页（4 张卡只出 1 页）。
   → `html, body, #app, .app-wrapper, .main-container, .app-main { height:auto; min-height:0; overflow:visible }`
3. 表格边框必须显式 `border: 1px solid #000 !important`，不然打印不出线。

尺寸一律用 `mm` 写，预览与打印才一致。验证分页：`page.pdf({format:'A4', preferCSSPageSize:true})`
后数 PDF 里的 `/Type /Page`（`sips -s format png x.pdf --out p.png` 可看首页，
`qlmanage` 在沙箱里会 `sandbox initialization failed`，用不了）。

---

## 六、二维码信息卡（打印）

- 卡片 = `190mm × 130mm`（A5 横），`@page A4 portrait / margin 8mm` → **每页 2 张**
- 列宽 `30(照片) / 50(姓名) / 24(性别) / 28(绝育) / 32(体重) / 26(二维码)`；
  行高 `12 / 13 / 13 / 14 / 26 / 26 / 26mm`；照片格与二维码格都 `rowspan=3`，体重格 `rowspan=2`
- 特征照片取法：`cat_photo` 没有主图字段，LATERAL 排序
  `order by (folder='正面照') desc, create_time desc, photo_id desc limit 1`，优先 `thumb_url`
- 数据来源统一走 `GET /cat/profile/cards?catIds=` → `CatProfileMapper.selectCardsByIds`
  （`CatCardVo` 放 `cat/domain` 下才有别名；一条 SQL 用相关子查询取疫苗/送医/诊断/用药最近一条，
  用药用 `group_concat(... order by use_date desc separator '\n')`）
- **加了 join 一定要记得把列写进 select 列表**：MyBatis resultMap 映射不到列不报错，静默给 null
- 该接口**不加 `@PreAuthorize`**（和 `/qrcodes` 一致），否则无档案编辑权的账号打印会 403
- 卡片上的行是给现场手写的，允许留空，别为了「填满」去编造数据

---

## 七、顺手修过的坑

- `cat/profile/index.vue` `handlePrintCards()` 曾写 `const ids = ids.value...` → TDZ 报错
  `Cannot access 'ids' before initialization`，勾选的猫全部丢失。局部变量改名为 `selectedIds`。
- `system/notice/index.vue` 曾把 `prop="noticeId"` 的列标成「序号」，已改名「公告ID」。

---

## 八、上传文件的「真实删除」与孤儿回收

RuoYi 原生的 `<file-upload>` / `<image-upload>` 在页面上点删除时**只把地址从表单里去掉**，
磁盘上的文件永远留着（假删除）。本项目已改为真删，新增功能时必须遵守：

- **页面删除 → 调 `deleteUploadFile(url)`**（`@/api/common`）→ `POST /common/delete`，
  服务端按 `/profile/...` 解析成磁盘路径（校验不得越出上传根目录）并物理删除，
  连同同目录 `thumb/` 下的缩略图一起删。
- **业务记录被删除**（如删相册、删领养记录）时，若页面能提前拿到 `fileUrl` / `thumbUrl`，
  在 `delXxx` 成功后再逐个 `deleteUploadFile`，让文件立刻消失，不必等定时任务。
- **兜底靠定时任务**：`FileCleanTask` 每天 03:30 调 `FileStorageService.sweep()`，
  把「全库文本字段里搜不到该文件相对路径」且超过宽限期（默认 24h）的文件删掉。
  这样「替换附件、编辑换图、记录被删」的旧文件最多留一天。
- **判定用子串而非正则**：把所有含 `upload/`、`avatar/` 的文本拼成语料，看路径是否出现在语料里。
  文件名带空格 / 中文 / 括号都不会漏判；宁可多留一个，也不误删在用的文件。
- **`upload/demo/**` 是随仓库分发的演示素材，永远跳过**（不然 `git status` 会一片 D）。
- 手动试跑 / 清理（仅超管）：
  `POST /common/storage/sweep?dryRun=true&graceHours=24`，确认清单没问题再把 `dryRun` 改 `false`。
  配置项在 `application.yml` 的 `cat.file-clean.{enabled,grace-hours,cron}`。

### ⚠️ update 语句里资源字段必须允许写空串（踩过大坑）

RuoYi 生成器给所有字段套的都是 `xxx != null and xxx != ''`。对**附件 / 图片地址**这类字段，
这个条件会让「清空」静默失效：

1. 用户点删除 → 磁盘文件被真删 ✅
2. 点保存 → `agreementUrl = ''`，被 `<if>` 挡掉 → 数据库仍保留旧地址 ❌
3. 再次打开记录 → 附件还显示 → 点预览打到已删除的文件 → 空白/500

**所有资源类字段（url / photos / image / avatar / file）在 `<update>` 里一律写 `xxx != null`**，
不加 `!= ''`；查询条件（`<where>` 里的 AND）保持原样不动，空串不该参与过滤。

已按此改过的：`cat_adoption.agreement_url`、`cat_expense.receipt_url`、`cat_diagnosis.check_photos`、
`cat_photo.file_url`、`cat_visit.photos`、`cat_medication.photos`、`cat_death.photos`。
新增带附件的业务表时照此办理。

### 已知取舍

删除是「点删除即删盘」，所以**删了附件却没点保存就关掉弹窗**，数据库会留下一个指向已删文件的地址。
出现这种情况时，重新打开该记录再删一次并保存即可清空；`sweep` 也能回收反向情况（磁盘有、库里没有）的孤儿文件。

### 图片/附件地址在页面上必须走 `resolveFileUrl`（踩过：用药照片「加载失败」）

库里存的是相对路径（`/profile/upload/demo/cat/用药照片_20261020081530A001.jpg`），**只有后端认这个路径**。
页面若直接 `:src="row.photos"`，浏览器会到**前端端口**要图（开发 81 / 生产 80），
被 SPA fallback 返回 `index.html`（HTTP 200 但 `Content-Type: text/html`），`<img>` 解码失败 →
一律显示「加载失败」。用 curl 看到 200 别急着以为没问题，必须看 `Content-Type`。

统一写法：`import { resolveFileUrl } from '@/utils/fileUrl'` → `:src="resolveFileUrl(url)"`。
它负责三件事：拼 `VITE_APP_BASE_API`（开发 `/dev-api`、生产 `/prod-api`）、中文文件名 `encodeURI`、
已带前缀 / `http` / `data:` / `blob:` 原样返回（幂等，可重复调用）。

- 逗号分隔的多图字段（`photos`、`checkPhotos`）先 `split(',')` + `trim` + `filter(Boolean)`，**逐个** map，
  别把整串丢给 `:src`；`preview-src-list` 用同一个数组，`initial-index` 才不会错位。
- 文件名里的上传时间戳（`uploadTimeLabel`）解析的是结尾 ASCII 段，URL 编码过也不影响，可放心先 resolve 再取时间。
- 2026-11-08 用药记录页（展开行 + 手机卡片）就是 `photoList()` 只 split 没拼前缀，弹窗里 ImageUpload 自己拼了前缀，
  所以「弹窗能看、列表加载失败」。新增列表/详情页展示照片、附件、协议、回执时照此办理。

---

## 九、提示文字的通用写法（`.form-tip`）

表单控件下方、上传控件下方、工具栏按钮右侧那些「说明 + 限制」的小字，全站只用一种写法。
`.form-tip` 定义在 `assets/styles/ruoyi.scss`，**页面里不要再自己写 `.tip` / `.form-tip` 样式**。

```html
<!-- 表单 / 上传控件下方 -->
<div class="form-tip">支持照片 <b>png/jpg/jpeg/webp</b>（单个不超过 <b>10MB</b>），最多 <b>9</b> 张</div>

<!-- 工具栏按钮右侧 -->
<span class="form-tip is-bar">出入库记录<b>不允许修改或删除</b>，如需更正请使用<b>「冲销」</b>生成反向记录。</span>
```

规则：

- **说明文字灰色**：`--el-text-color-secondary`、12px、行高 1.6、`margin-top: 4px`
- **关键限制标红加粗**：`.form-tip b` / `strong` 自动是 `--el-color-danger` + 600 字重。
  只给真正需要用户记住的点加 `<b>`——格式、大小、数量、不可逆操作、会自动变成什么状态；
  **不要把整句都包进 `<b>`，也不要整段涂红**
- 工具栏里加 `is-bar`：行高 32px 与按钮齐高、去掉上间距
- 上传组件（`MediaUpload` / `FileUpload` / `ImageUpload`）的提示元素写
  `class="el-upload__tip form-tip"`，自动套用同一套样式
- `MediaUpload` 加 `image-only` 时只收照片（拒绝视频），文案也只提照片——
  回访照片、诊断照片这类"只存图"的场景必须用它

### 表格里的第二行状态小字（`.due-tip`）

日期类字段（下次接种、下次驱虫、手术日期…）要显示"还剩几天 / 过期多久"时，
**不要为它单开一列**——多一列会把日期列挤到显示不全。写在原单元格里当第二行：

```html
<el-table-column label="下次接种" align="center" prop="nextDate" width="110">
   <template #default="scope">
      <div>{{ scope.row.nextDate || '—' }}</div>
      <div v-if="scope.row.nextDate" class="due-tip" :class="dueInfo(scope.row.nextDate).cls">
         {{ dueInfo(scope.row.nextDate).text }}
      </div>
   </template>
</el-table-column>
```

- 颜色只有四种：`is-danger` 已过期、`is-warning` 临期（7 天内 / 今天）、
  `is-success` 已完成、`is-muted` 正常
- 字号 11px，紧贴上一行（`margin-top: 1px`），与档案页 `.status-detail` 同一思路
- 天数按**自然日**算（`new Date(y, m-1, d)` 相减），不要用时间戳直接除，否则会被时区/时分秒差 1 天

### 看板卡片跳转必须带筛选条件

看板上的每张卡 / 每个图表点位跳到列表页时，`router.push` 的 `query` 必须能还原成
目标页搜索表单里的条件（`status` / `dueDays` / `neuterStatus` / `visitStatus` / `deathType` / `expenseType` …），
目标页在 `applyRouteQuery()` 里读 `route.query` 落到 `queryParams`，并 `watch(route.query)` 处理
同一路由只变 query 的情况。**卡片数字必须等于点进去后列表的「共 N 条」**，
口径不一致就改后端口径（例如待疫苗/待驱虫直接复用列表页的 `dueDays` 条件）。

筛选条件只在原有 RuoYi 搜索表单里体现（下拉选中态），**不要在页面上另加提示条/横幅**。
「重置」要连同路由带来的条件一起清掉，否则用户点了重置筛选依旧生效。

## 十、提醒 ↔ 台账 双向同步（改任何一边，另一边必须跟着变）

疫苗 / 驱虫的「下次接种 / 下次驱虫」和提醒列表里的日期是**同一个计划日的两处投影**，
两边必须一致，否则用户按其中一处安排就会踩空（曾经出现提醒 11-04、台账还写 11-01）。

### 两个方向

| 方向 | 触发点 | 实现 | 提示 |
|---|---|---|---|
| 提醒 → 台账 | 提醒页「推迟」且勾选同步 | `CatReminderServiceImpl.defer()` → `syncPlanDate()`，改 `cat_vaccine/cat_deworm.next_date` | 推迟弹窗目标日期 + 返回 `syncTip` |
| 台账 → 提醒 | 台账编辑改了下次日期 | `CatVaccineServiceImpl/CatDewormServiceImpl.update*()` → `CatReminderSyncService.syncFromPlan()`，改 `cat_reminder.remind_date` | 表单 `.form-tip` 说明 + 保存后 `notifySuccess` |

### 五条不能破的规则

1. **正向同步改台账必须走 mapper，不能走 service**：`CatReminderServiceImpl` 里用的是
   `catVaccineMapper.updateCatVaccine`。若改成调 `ICatVaccineService`，台账的 service 会再回调
   `CatReminderSyncService`，形成「推迟 → 改台账 → 又改提醒」的绕圈。
2. **只比「计划日期」是否真的变了**：`updateCatVaccine(疫苗 id + 负责人)` 这类不动日期的保存，
   日期相同就直接返回，不能每次保存都刷一遍提醒（否则每次都会清已读、重新变未读）。
   比较用 `yyyy-MM-dd` 字符串（`CatReminderSyncService.sameDay`），不要比毫秒。
3. **`origin_date` / `defer_count` 一律不动**：台账改期是「重新计划」，不能抹掉「推迟过几次」的历史；
   只改 `remind_date`，并清掉该提醒的 `cat_reminder_read`（计划变了，相关人应重新看到）。
4. **来源定位优先 `source_id`，兜底才按日期反查**：历史提醒 `source_id` 为空时，用
   「同猫 + 同类型 + 旧提醒日期」找一条并回填（`updateReminderSource` 只在 `source_id is null` 时写），
   之后就走精确路径。**不要**只按日期匹配 —— 连续推迟后日期早变了。
5. **台账删除要连带删提醒**：`cat_vaccine/cat_deworm` 是逻辑删除（`del_flag='2'`），
   提醒却是物理删除，先清 `cat_reminder_read` 再删 `cat_reminder`，别留悬空已读记录。

### 前端配套

- 台账页签与提醒页签都被 keep-alive 缓存 → 三处都要 `onActivated` 补拉（跳过首次），
  否则从别的页面改完切回来还是旧日期。
- 台账编辑弹窗里「下次接种 / 下次驱虫」下方固定一行 `.form-tip`，
  只在编辑态显示（`v-if="form.vaccineId"` / `form.dewormId`）。

## 十一、勾选类控件与入参宽容（2026-11-03 病房授权空指针事故）

线上「病房授权」保存报 `Cannot invoke "Object.toString()" because "o" is null`，
根因是**组标题的“全选”复选框被放进了 `el-checkbox-group`**。

### 事故链条

1. `<el-checkbox-group>` 内的每个 `el-checkbox` 都被视为组成员，点它会把自己的 `value` 写进绑定数组；
   而组标题那个“全选”框只有 `:model-value` / `@change`，**没有 `value`**，于是写进去的是 `undefined`
2. `JSON.stringify([105, 106, undefined, 103])` → `[105,106,null,103]`，`undefined` 变成 `null`
3. 后端 `.map(o -> Long.valueOf(o.toString()))` 对 `null` 求值 → NPE，整次保存失败
   （`sys_oper_log` 里留下 `wardIds:[105,106,null,103,...]` 与 status=1 三条失败记录）

### 两条硬规则

1. **“全选 / 半选”这类聚合复选框一定写在 `el-checkbox-group` 外面**。
   本项目已改为不用 `el-checkbox-group`，每个选项用受控写法：
   `:model-value="ids.includes(id)"` + `@change="val => toggle(id, val)"`，
   由 `toggle()` 自己做增删、去重、非法值过滤。
2. **后端解析前端传来的 id 数组必须宽容**，不允许直接 `o.toString()`：
   `toLong()` 对 `null / 空串 / 非数字` 返回 null，
   再 `filter(id -> id != null && id > 0).distinct()`。
   `0` 和负数也是非法 id（自增主键），测试时必须覆盖 `[null,"",0,-1,"abc"]` 这种组合。
   缺 `userId` 时直接返回 `error("缺少用户ID，无法保存病房授权")`，不要带着 null 往下走。

### 自检清单（新增勾选类功能时）

- 打开弹窗 → 点组标题全选 → 保存：`sys_oper_log.oper_param` 里**不能出现 null**
- 取消全选 → 保存：应是 `"wardIds":[]`，不是 `"[null]"`
- 回显：再次打开弹窗，已保存项必须全部勾上（`selectWardIdsByUserId` 已加 `ward_id is not null`）
- 接口级兜底：直接 `curl` 提交带 `null` 的数组，应返回 200 且只写入有效 id

## 十二、权限标识必须与 `sys_menu.perms` 逐字符一致（2026-11-03 财务物资按钮消失事故）

现象：给「管理员」角色勾选了费用分类 / 物资分类的全部按钮权限，用 manager 账号进页面，
新增、修改、删除、导出**一个都不显示**；换成超管账号又是好的。

根因：前端 `v-hasPermi` 写的是驼峰，菜单表里是全小写。

| 前端（错误） | `sys_menu.perms`（正确） |
|---|---|
| `cat:feeCategory:add` | `cat:feecategory:add` |
| `cat:materialCategory:remove` | `cat:materialcategory:remove` |

`v-hasPermi` 的实现是 `permissionFlag.includes(permission)`，**字符串全等**，大小写不同即判定无权限，
按钮被 `el.parentNode.removeChild(el)` 直接摘掉（不是隐藏，DOM 里根本没有）。

### 为什么超管测不出来

超管权限集是通配 `*:*:*`，`hasPermi` 里 `all_permission === permission` 直接放行，
**任何写错的权限串对超管都无效**。这个 bug 因此可以藏到第一次给普通角色授权才暴露。

→ **新增页面或改动按钮后，必须用一个非超管账号验证**，超管通过不代表权限串写对了。

### 三条硬规则

1. **权限串全部小写，模块名不转驼峰**：`cat:feecategory:*`、`cat:materialcategory:*`、
   `cat:stockrecord:*`，一律照抄 `sys_menu.perms`，不要按 Java 类名习惯写 `feeCategory`。
2. **以数据库为准，不以代码为准**：`sys_menu.perms` 是运行时真正比对的值。
   前端 `v-hasPermi`、后端 `@PreAuthorize("@ss.hasPermi('...')")`、`sys_menu.perms` 三处必须完全一致。
3. **`meta.canRemove` 是另一套推导**：后端 `SysMenuServiceImpl.resolveCanRemove()` 把菜单自身的
   `模块:list` 末段换成 `:remove` 推导，所以菜单 perms 写成 `cat:xxx:list` 才能推对。
   修改/删除按钮同时受 `v-hasPermi` 和 `v-if="$route.meta.canRemove"` 约束，
   两者任一不满足都不显示——排查时先看 `v-hasPermi`，再看角色有没有勾「删除」。

### 自检命令（改完权限相关代码就跑一遍）

```bash
# 1. 导出库里的 perms
mysql -uroot -p*** -h127.0.0.1 cat_rescue -N \
  -e "select perms from sys_menu where perms is not null and perms<>''" > /tmp/db_perms.txt
# 2. 比对前端所有 v-hasPermi / checkPermi
python3 - <<'PY'
import re, glob
db = set(x.strip() for x in open('/tmp/db_perms.txt') if x.strip())
miss = {}
for f in glob.glob('frontend/src/**/*.vue', recursive=True) + glob.glob('frontend/src/**/*.js', recursive=True):
    txt = open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r"v-hasPermi=\"\[([^\]]*)\]\"|checkPermi\(\[([^\]]*)\]\)", txt):
        for s in re.findall(r"['\"]([^'\"]+)['\"]", m.group(1) or m.group(2)):
            if s not in db:
                miss.setdefault(s, set()).add(f)
for k, v in sorted(miss.items()):
    print("✗", k, "->", sorted(v))
print("✓ 全部匹配" if not miss else f"共 {len(miss)} 处不匹配")
PY
```

### 授权后不生效的另一个原因

权限集合在**登录时**写入 Redis 的 `login_tokens:<uuid>`，之后不会自动刷新。
改完角色权限后，受影响的用户必须**退出重新登录**（刷新页面不够，`getInfo` 读的还是旧 token 里的集合）。
排查顺序：先查 `sys_role_menu` 有没有真勾上 → 再查权限串大小写 → 最后才怀疑缓存。

## 十三、病房逻辑删除必须先迁猫（2026-11-09 助理只看到 4/7 的事故）

**现象**：管理员按「基地 = 18号楼」筛选看到 7 只，助理 assist01 同一筛选只看到 4 只。
不是权限串问题、不是缓存问题，是**数据脏引用 + 按病房授权过滤**叠加的结果。

**根因链**

1. `cat_ward` 是逻辑删除（`del_flag='2'`），但 `cat_profile.ward_id` 不会跟着清空；
   系统当时也没有「病房里还有猫就禁止删除」的校验。
   18号楼 100 普通病房 A 区 / 101 隔离观察房 / 102 术后观察房 被删后，
   5 只猫（花卷、小满、糖糖、奶片、圆圆）仍然指着这三间已删病房。
2. 档案列表的「所在病房」列，前端 `wardName()` 只从**有效病房列表**里查名字，
   查不到就返回空 → 页面显示「—」。所以看起来像「没分房」，实际是分在已删病房里。
3. 助理的可见范围是 `AND ward_id IN (cat_ward_auth 里配给自己的病房)`。
   assist01 只配了 100 / 101 / 106，其中 100、101 已删，
   于是只剩「南间(106)」这一间能看到猫 —— 正好 4 只。**剩下的猫怎么勾都看不到。**

**已经落地的三道防线**

| 位置 | 做法 |
|---|---|
| `CatWardServiceImpl.deleteCatWardByIds` | 删除前 `selectWardCatCounts` 校验，有在册猫直接抛 `ServiceException` 并列出「病房（N只）」，提示先去迁猫 |
| 同上 | 校验通过后 `deleteWardAuthByWardIds` 连带清掉指向这些病房的授权行，不留死授权 |
| `CatWardMapper.selectCatWardList` | 新增 `includeDeleted` 查询标记；档案列表用 `includeDeleted=true` 取全量，已删病房显示成「南间（已删除）」而不是「—」 |

**两条不能改错的既有行为**

- `CatProfileMapper.xml` 里助理范围的写法是：`wardScope` 为真时，
  有授权 → `AND ward_id IN (...)`，**没授权 → `AND 1 = 0`**。
  这个 `1 = 0` 是故意的：助理一个病房都没配时必须零可见，不能退化成「不过滤 = 看全部」。
- `ward_id is null`（未分配病房）的猫**不在**助理可见范围内（`IN` 对 null 不成立）。
  这是安全默认：否则任何未分房的猫对所有助理都可见。要放开属于业务决策，必须显式改并写在这里。

**线上排查 SQL（只读，先跑这个再动数据）**

```sql
-- 1. 还有猫挂在已删病房上的（非空就是脏引用）
select p.base_id, p.ward_id, w.ward_name, count(*) cnt,
       group_concat(p.cat_name order by p.cat_id) cats
  from cat_profile p join cat_ward w on w.ward_id = p.ward_id
 where p.del_flag = '0' and w.del_flag = '2'
 group by p.base_id, p.ward_id, w.ward_name;

-- 2. 还有授权指向已删病房的（删完病房没清授权）
select a.user_id, a.ward_id, w.ward_name
  from cat_ward_auth a join cat_ward w on w.ward_id = a.ward_id
 where w.del_flag = '2';

-- 3. 某助理实际可见条数（把 101 换成目标 user_id）
select count(*) from cat_profile
 where base_id = ? and del_flag = '0'
   and ward_id in (select ward_id from cat_ward_auth where user_id = 101);
```

修复顺序永远是：**先迁猫 → 再清死授权 → 再按有效病房重新授权**，
顺序颠倒会把猫「迁进」一个马上要被删掉的病房。

---

## 十四、删除确认必须分「有依赖 / 无依赖」两态（2026-11-09 用户定的统一规则）

> 一句话：**删除被引用的数据 = 会产生脏数据 = 只提示、不给删；删除没人引用的数据 = 普通确认即可。**

### 14.1 判定标准

删除前先数一遍「谁在引用我」。只要下面任一成立，就是**有依赖（blocked）**：

| 类型 | 举例 |
|---|---|
| 被在册数据引用 | 病房里还有 `cat_profile`（`del_flag='0'`）、基地下还有猫/病房、病房类型还被病房用、物资分类下还有物资 |
| 被历史单据引用 | 费用分类被 `cat_expense` 用、检查项目被 `cat_diagnosis_item` / `cat_prescription_item` 用 |
| 有子记录 | 检查项目下面还有子项目、领养记录下面还有回访、处方下面还有明细/用药 |
| 删除会破坏台账 | 猫咪档案下挂病历/疫苗/费用/照片等 20 张子表；删猫不会级联，全是孤儿 |

叶子型流水（照片、疫苗、驱虫、流转、回访、TNR、放归、死亡、转诊）删掉不留任何悬空引用，
属于「无依赖」，用普通确认即可，**不要**也做成置灰。

### 14.2 交互形态（必须用 RuoYi 自带样式的 `$modal.confirm`，不要自绘 el-dialog）

- **blocked**：标题「无法删除」，按钮为「关 闭」+「确 定」，**确定按钮做成灰色不可点**
  （`customClass: 'msgbox-delete msgbox-delete--blocked'` + CSS 灰底灰字 `pointer-events:none`，
  并在 `beforeClose` 里吞掉 `confirm` 动作做兜底）。
- **可删除**：标题「删除确认」，「取 消」+「确 定」都可点。
- **级联删除**（单据类）：标题同样是「删除确认」，黄色警示条说明「以下关联记录会一并清除」，确定可点。
- 后端**必须同时保留校验**（抛 `ServiceException`）：前端只是体验层，两个人同时操作时后端才是最终防线。

**正文一律传结构化对象，不要拼纯文本**（纯文本只保留「主问句 + 说明」两段，用于普通确认）：

```js
message: {
  tone: 'error',                       // error=红 / warning=黄
  lead: '确认删除 布丁 · 2026-08-28 ？',  // 可选，加粗主问句
  summary: '以下基地还有数据在使用，不能删除：', // 顶部彩色提示条
  items: [{ name: '北苑流浪猫救助基地', detail: '4 只猫咪、3 间病房' }], // 明细行，detail 用「、」分隔会渲染成小标签
  tail: '请先处理这些引用后再删除基地。'   // 可选，底部灰字
}
```

视觉约定（2026-11-09 用户嫌原版丑后重做，别退回旧样式）：

- **不要用 Element 自带的状态大图标**（`type: 'error'` 会渲染一个图标列，把正文挤歪）→ `type: ''`，
  改为正文顶部彩色提示条（红/黄圆角条 + 小图标）
- 明细做成**卡片列表**：左边对象名加粗，右边「4 只猫咪」「3 间病房」小标签，比「· 名称：4 只猫咪、3 间病房」一坨文字清晰得多
- 弹窗宽度 460px，标题 16px/600，按钮 min-width 78px
- 置灰的确定按钮必须是**灰底灰字**（`--el-fill-color` / `--el-text-color-disabled`），
  不能是默认的「浅蓝半透明」——那样看起来像可点的次要按钮

### 14.3 复用件

- `frontend/src/utils/deleteConfirm.js`
  - `deleteWithImpact(proxy, {...})`：查引用 → 自动决定弹哪一种 → 执行删除（页面首选）
  - `deleteConfirm(proxy, { blocked, title, message, onConfirm })`：直接弹
  - `buildBlockedMessage(summary, items, tail, { tone, lead })`：拼结构化正文
- `frontend/src/plugins/modal.js` → `$modal.confirm(content, title, options)`（第三参可选，默认行为不变）
- 全局样式 `.msgbox-delete / --blocked / --normal` 与 `.del-msg*`（`assets/styles/index.scss`）

**明细里的名称来自数据库，拼进 HTML 前必须转义**（`deleteConfirm.js` 已内置 `esc()`），
因为正文是用 `dangerouslyUseHTMLString: true` 渲染的。

新增可删主数据时照抄这套，不要各写各的文案风格。

### 14.4 已知的两处「禁止删除」既有实现

- `CatWardServiceImpl`：病房里还有在册猫 → 抛异常并列出「病房（N只）」；删除时连带清 `cat_ward_auth`
- `CatWardTypeServiceImpl` / `CatCheckItemServiceImpl`：被引用 / 有子项目 → 抛异常
