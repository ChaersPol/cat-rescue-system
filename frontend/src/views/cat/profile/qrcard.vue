<template>
  <div class="app-container qrcard-page">
    <!-- 筛选区（打印时隐藏） -->
    <el-card shadow="never" class="no-print">
      <template #header>
        <div class="card-header">
          <span>二维码卡片打印</span>
          <span class="tip">勾选需要打印的猫咪，A4 纸每页 2 张（A5 横），剪下即可张贴到笼位</span>
        </div>
      </template>
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="query.catName" placeholder="名字 / 档案编号" clearable style="width: 180px" @keyup.enter="loadCats" />
        </el-form-item>
        <el-form-item label="基地">
          <el-select v-model="query.baseId" placeholder="全部基地" clearable style="width: 160px" @change="loadCats">
            <el-option v-for="b in baseList" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px" @change="loadCats">
            <el-option v-for="d in cat_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadCats">搜索</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-checkbox v-model="checkAll" :indeterminate="indeterminate" @change="handleCheckAll">全选当前列表</el-checkbox>
        <span class="selected-tip">已选 {{ selected.length }} 只</span>
        <el-button size="small" type="primary" plain icon="Printer" :disabled="!printList.length" @click="handlePrint">打印选中卡片</el-button>
        <el-button size="small" type="info" plain icon="Refresh" @click="loadCats">刷新</el-button>
      </div>

      <el-table ref="tableRef" :data="catList" size="small" border max-height="320" @selection-change="handleSelectionChange" row-key="catId">
        <el-table-column type="selection" width="45" reserve-selection />
        <el-table-column label="序号" type="index" :index="i => $seqIndex(i)" width="56" align="center" />
        <el-table-column label="档案编号" prop="archiveNo" width="150" />
        <el-table-column label="名字" prop="catName" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="scope"><dict-tag :options="cat_status" :value="scope.row.status" /></template>
        </el-table-column>
        <el-table-column label="所在基地" min-width="120">
          <template #default="scope">{{ baseName(scope.row.baseId) || '—' }}</template>
        </el-table-column>
        <el-table-column label="二维码" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.hasQrCode === 1" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">无</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 打印预览区 -->
    <div class="preview-wrap">
      <div class="preview-title no-print">打印预览（{{ printList.length }} 张）</div>
      <div class="card-sheet" id="printSheet">
        <el-empty v-if="!printList.length" description="在上方勾选猫咪，预览实时生成" :image-size="70" class="no-print" />
        <div class="cat-card" v-for="c in printList" :key="c.catId">
          <table class="card-tb">
            <colgroup>
              <col class="col-photo" />
              <col class="col-name" />
              <col class="col-sex" />
              <col class="col-neut" />
              <col class="col-weight" />
              <col class="col-qr" />
            </colgroup>
            <tbody>
              <!-- 标题行：左侧特征照片、右侧二维码，都纵向跨下面两行 -->
              <tr>
                <td rowspan="3" class="photo-cell">
                  <img v-if="c.photoUrl" :src="resolveUrl(c.photoUrl)" class="cat-photo" alt="特征照片" />
                  <div v-else class="photo-ph">无照片</div>
                  <div class="photo-cap">{{ c.photoFolder || '特征照片' }}</div>
                </td>
                <td colspan="4" class="card-title">
                  猫咪信息卡
                  <span class="card-meta">{{ c.archiveNo }}<template v-if="statusLabel(c.status)">｜{{ statusLabel(c.status) }}</template></span>
                </td>
                <td rowspan="3" class="qr-cell">
                  <img v-if="c.qrCode" :src="c.qrCode" class="qr" alt="二维码" />
                  <div v-else class="qr-ph">无二维码</div>
                  <div class="scan-tip">扫码查看档案</div>
                </td>
              </tr>
              <!-- 姓名 / 性别 / 绝育 / 体重（体重纵向跨两行） -->
              <tr>
                <td class="cell mid"><span class="lb">姓名：</span><span class="val">{{ c.catName }}</span></td>
                <td class="cell mid"><span class="lb">性别：</span><span class="val">{{ sexLabel(c.sex) }}</span></td>
                <td class="cell mid"><span class="lb">绝育：</span><span class="val">{{ neuteredLabel(c.neutered) }}</span></td>
                <td rowspan="2" class="cell mid"><span class="lb">体重（KG）：</span><span class="val block">{{ weightVal(c.weight) }}</span></td>
              </tr>
              <tr>
                <td colspan="3" class="cell mid"><span class="lb">疫苗：</span><span class="val">{{ c.vaccineText }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell"><span class="lb">入住医院及日期：</span><span class="val">{{ c.hospitalText }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell tall"><span class="lb">病情：</span><span class="val pre">{{ clamp(c.diagnosisText, 130) }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell tall"><span class="lb">用药：</span><span class="val pre">{{ medText(c.medicationText) }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell tall"><span class="lb">注意事项：</span><span class="val pre">{{ clamp(c.remark, 160) }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CatQrCard">
import { listCatProfile, listCatCards } from "@/api/cat/profile"
import { listCatBase } from "@/api/cat/base"
import { onMasterDataChanged, MASTER_KIND } from "@/utils/catCache"

const { proxy } = getCurrentInstance()
const { cat_status, cat_sex, cat_neutered } = useDict("cat_status", "cat_sex", "cat_neutered")
const route = useRoute()

const tableRef = ref(null)
const catList = ref([])
const baseList = ref([])
const selected = ref([])
const printList = ref([])
const checkAll = ref(false)
const indeterminate = ref(false)

const query = reactive({
  catName: undefined,
  baseId: undefined,
  status: undefined
})

const baseUrl = import.meta.env.VITE_APP_BASE_API

// 照片存的是 /profile/xx 相对路径，展示前补接口前缀
function resolveUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  return baseUrl + (url.startsWith('/') ? url : '/' + url)
}

function baseName(id) {
  if (!id) return ''
  const b = baseList.value.find(x => x.baseId === id)
  return b ? b.baseName : ''
}

function dictLabel(list, value) {
  const d = (list || []).find(x => String(x.value) === String(value))
  return d ? d.label : ''
}
function sexLabel(v) { return dictLabel(cat_sex.value, v) || '—' }
function neuteredLabel(v) { return dictLabel(cat_neutered.value, v) || '—' }
function statusLabel(v) { return dictLabel(cat_status.value, v) || '' }

// 体重库里存的是自由文本（如 4.5kg），表格已有 KG 单位，这里把单位去掉
function weightVal(w) {
  if (!w) return ''
  return String(w).replace(/\s*(kg|KG|Kg|公斤|千克)\s*/g, '').trim()
}

// 用药只留最近 4 行，超出用省略提示，避免撑破格子
function medText(text) {
  const lines = String(text || '').split('\n').map(s => s.trim()).filter(Boolean)
  if (!lines.length) return ''
  if (lines.length <= 4) return lines.join('\n')
  return lines.slice(0, 3).join('\n') + '\n…共 ' + lines.length + ' 条'
}

// 长文本截断，保证打印时格子高度可控（卡片是固定高度表单）
function clamp(text, n) {
  const s = String(text || '')
  return s.length > n ? s.slice(0, n) + '…' : s
}

function loadCats() {
  return listCatProfile({ pageNum: 1, pageSize: 500, ...query }).then(res => {
    catList.value = res.rows || []
  })
}

function getBaseList() {
  listCatBase({ pageNum: 1, pageSize: 500 }).then(res => { baseList.value = res.rows || [] })
}

function handleSelectionChange(rows) {
  selected.value = rows
  indeterminate.value = rows.length > 0 && rows.length < catList.value.length
  checkAll.value = rows.length > 0 && rows.length === catList.value.length
  // 勾选即实时刷新预览
  refreshPreview()
}

function handleCheckAll(val) {
  tableRef.value && tableRef.value.clearSelection()
  if (val) {
    catList.value.forEach(row => tableRef.value.toggleRowSelection(row, true))
  }
}

// 勾选变化时实时更新打印预览（不调用 window.print）
let previewSeq = 0
function refreshPreview() {
  const rows = selected.value
  if (!rows.length) {
    printList.value = []
    return
  }
  const ids = rows.map(c => c.catId)
  // 用序号防抖：全选会连续触发多次 selection-change，只认最后一次请求
  const seq = ++previewSeq
  listCatCards(ids).then(res => {
    if (seq !== previewSeq) return
    const order = new Map(ids.map((id, i) => [id, i]))
    const list = (res.data || []).slice().sort((a, b) => (order.get(a.catId) ?? 0) - (order.get(b.catId) ?? 0))
    printList.value = list
  })
}

// 真正打印：预览已经就绪，这里只调起打印对话框
function handlePrint() {
  if (!printList.value.length) {
    proxy.$modal.msgWarning('请先勾选需要打印的猫咪')
    return
  }
  window.print()
}

// 从档案详情页带过来的 catIds，进入即自动选中
function applyRouteCatIds() {
  const raw = route.query.catIds
  if (!raw) return
  const ids = String(raw).split(',').map(s => Number(s)).filter(n => !isNaN(n))
  if (!ids.length) return
  nextTick(() => {
    catList.value.forEach(row => {
      if (ids.includes(row.catId)) {
        tableRef.value && tableRef.value.toggleRowSelection(row, true)
      }
    })
  })
}

getBaseList()
loadCats().then(() => applyRouteCatIds())

// 基地改名/新增后，打印卡的基地筛选与卡片上的基地名立即更新
onMasterDataChanged(MASTER_KIND.BASE, () => {
  getBaseList()
})
</script>

<style scoped>
.card-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.tip { font-size: 12px; color: var(--el-text-color-placeholder); }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; flex-wrap: wrap; }
.selected-tip { font-size: 13px; color: var(--el-text-color-secondary); }
.preview-wrap { margin-top: 16px; }
.preview-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; }

/* 预览区：与打印尺寸一致（190mm × 130mm），所见即所得 */
.card-sheet {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8mm;
  background: #f5f7fa;
  padding: 8mm 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  min-height: 200px;
}
.cat-card {
  width: 190mm;
  background: #fff;
  page-break-inside: avoid;
  break-inside: avoid;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
}

/* 卡片本体：与纸质表单一致的黑框表格 */
.card-tb {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: #fff;
}
.col-photo { width: 30mm; }
.col-name { width: 50mm; }
.col-sex { width: 24mm; }
.col-neut { width: 28mm; }
.col-weight { width: 32mm; }
.col-qr { width: 26mm; }

.card-tb td {
  border: 1px solid #333;
  padding: 1.6mm 2mm;
  font-size: 10.5pt;
  line-height: 1.5;
  color: #000;
  vertical-align: top;
  word-break: break-word;
}
.card-title {
  position: relative;
  height: 12mm;
  text-align: center;
  vertical-align: middle !important;
  font-size: 18pt;
  font-weight: 700;
  letter-spacing: 4px;
}
.card-meta {
  position: absolute;
  right: 3mm;
  bottom: 1.2mm;
  font-size: 7.5pt;
  font-weight: 400;
  letter-spacing: 0;
  color: #666;
}
.card-tb tr:nth-child(2) td,
.card-tb tr:nth-child(3) td { height: 13mm; }
.card-tb tr:nth-child(4) td { height: 14mm; }
.cell.mid { vertical-align: middle; }
.cell.tall { height: 26mm; }
.lb { color: #000; font-size: 10pt; }
.val { color: #000; }
.val.block { display: block; margin-top: 1.5mm; font-size: 13pt; }
.val.pre { white-space: pre-wrap; }

.qr-cell {
  text-align: center;
  vertical-align: middle !important;
  padding: 1.5mm 1mm !important;
}
.qr { width: 23mm; height: 23mm; display: block; margin: 0 auto; }
.qr-ph {
  width: 23mm;
  height: 23mm;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8pt;
  color: #999;
  border: 1px dashed #bbb;
}
.scan-tip { font-size: 7.5pt; color: #666; margin-top: 0.8mm; }

/* 特征照片：贴左侧，纵向跨标题+姓名+疫苗三行 */
.photo-cell {
  text-align: center;
  vertical-align: middle !important;
  padding: 1mm !important;
}
.cat-photo {
  width: 27mm;
  height: 31mm;
  object-fit: cover;
  object-position: center;
  display: block;
  margin: 0 auto;
}
.photo-ph {
  width: 27mm;
  height: 31mm;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8pt;
  color: #999;
  border: 1px dashed #bbb;
}
.photo-cap {
  font-size: 6.5pt;
  color: #666;
  margin-top: 0.6mm;
  line-height: 1.2;
}
</style>

<!-- 打印样式：只留卡片，隐藏左侧菜单、顶栏、筛选区 -->
<style>
@media print {
  @page { size: A4 portrait; margin: 8mm; }
  /* 若依布局的外层容器带固定高度 + overflow，不解除会被压成一页 */
  html, body, #app, .app-wrapper, .main-container, .app-main {
    height: auto !important;
    min-height: 0 !important;
    max-height: none !important;
    overflow: visible !important;
  }
  body { background: #fff !important; }
  /* 侧栏收起后主内容仍带着 200px 的左外边距，打印时必须清掉，否则整卡右移出纸 */
  .main-container { margin-left: 0 !important; width: 100% !important; }
  .sidebar-container, .navbar, .tags-view-container, .no-print,
  .app-main > .app-container > .no-print { display: none !important; }
  .app-main { padding: 0 !important; margin: 0 !important; width: auto !important; }
  .qrcard-page { padding: 0 !important; }
  .preview-wrap { margin: 0 !important; }
  .card-sheet {
    display: block !important;
    background: #fff !important;
    border: none !important;
    padding: 0 !important;
    border-radius: 0 !important;
  }
  .cat-card {
    box-shadow: none !important;
    margin: 0 0 8mm 0 !important;
    page-break-inside: avoid;
    break-inside: avoid;
  }
  .cat-card:last-child { margin-bottom: 0 !important; }
  .card-tb td { border: 1px solid #000 !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
}
</style>
