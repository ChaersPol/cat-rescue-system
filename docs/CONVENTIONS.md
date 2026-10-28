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
