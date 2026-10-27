<template>
  <div class="app-container qrcard-page">
    <!-- 筛选区（打印时隐藏） -->
    <el-card shadow="never" class="no-print">
      <template #header>
        <div class="card-header">
          <span>二维码卡片打印</span>
          <span class="tip">勾选需要打印的猫咪，A4 纸每行 3 张、每页 12 张，剪下即可贴笼子卡</span>
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
        <el-button size="small" type="primary" plain icon="Printer" :disabled="!selected.length" @click="handlePrint">打印选中卡片</el-button>
        <el-button size="small" type="info" plain icon="Refresh" @click="loadCats">刷新</el-button>
      </div>

      <el-table ref="tableRef" :data="catList" size="small" border max-height="320" @selection-change="handleSelectionChange" row-key="catId">
        <el-table-column type="selection" width="45" reserve-selection />
        <el-table-column label="档案编号" prop="archiveNo" width="150" />
        <el-table-column label="名字" prop="catName" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="scope"><dict-tag :options="cat_status" :value="scope.row.status" /></template>
        </el-table-column>
        <el-table-column label="所在基地" min-width="120">
          <template #default="scope">{{ baseName(scope.row.baseId) || '—' }}</template>
        </el-table-column>
        <el-table-column label="二维码" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.qrCode" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">无</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 打印预览区 -->
    <div class="preview-wrap">
      <div class="preview-title no-print">打印预览（{{ printList.length }} 张）</div>
      <div class="card-sheet" id="printSheet">
        <el-empty v-if="!printList.length" description="请在上方勾选猫咪后点击「打印选中卡片」" :image-size="70" class="no-print" />
        <div class="cat-card" v-for="c in printList" :key="c.catId">
          <div class="card-left">
            <div class="card-name">{{ c.catName || '未命名' }}</div>
            <div class="card-no">{{ c.archiveNo }}</div>
            <div class="card-line">{{ c.breed || '—' }} / {{ sexLabel(c.sex) }} / {{ c.ageEstimate || '—' }}</div>
            <div class="card-line">{{ baseName(c.baseId) || '—' }}</div>
            <div class="card-line status">{{ statusLabel(c.status) }}</div>
          </div>
          <div class="card-right">
            <img v-if="c.qrCode" :src="c.qrCode" class="qr" alt="二维码" />
            <div v-else class="qr placeholder">无二维码</div>
            <div class="scan-tip">扫码查看档案</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CatQrCard">
import { listCatProfile, listCatQrCodes } from "@/api/cat/profile"
import { listCatBase } from "@/api/cat/base"

const { proxy } = getCurrentInstance()
const { cat_status, cat_sex } = useDict("cat_status", "cat_sex")
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

function baseName(id) {
  if (!id) return ''
  const b = baseList.value.find(x => x.baseId === id)
  return b ? b.baseName : ''
}

function dictLabel(list, value) {
  const d = (list || []).find(x => x.value === value)
  return d ? d.label : ''
}
function sexLabel(v) { return dictLabel(cat_sex.value, v) || '—' }
function statusLabel(v) { return dictLabel(cat_status.value, v) || '—' }

function loadCats() {
  listCatProfile({ pageNum: 1, pageSize: 500, ...query }).then(res => {
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
}

function handleCheckAll(val) {
  tableRef.value && tableRef.value.clearSelection()
  if (val) {
    catList.value.forEach(row => tableRef.value.toggleRowSelection(row, true))
  }
}

function handlePrint() {
  if (!selected.value.length) {
    proxy.$modal.msgWarning('请先勾选需要打印的猫咪')
    return
  }
  const ids = selected.value.map(c => c.catId)
  // 列表接口不带 qr_code，打印前按 id 批量取一次二维码
  listCatQrCodes(ids).then(res => {
    const map = {}
    ;(res.data || []).forEach(q => { map[q.catId] = q.qrCode })
    printList.value = selected.value.map(c => ({ ...c, qrCode: map[c.catId] || c.qrCode }))
    const noQr = printList.value.filter(c => !c.qrCode)
    nextTick(() => {
      if (noQr.length) {
        proxy.$modal.msgWarning(`有 ${noQr.length} 只猫咪尚未生成二维码，卡片将显示「无二维码」，可先回档案页生成`)
      }
      window.print()
    })
  })
}

// 从档案详情页带过来的 catIds，进入即自动选中并打印
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
loadCats()
applyRouteCatIds()
</script>

<style scoped>
.card-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.tip { font-size: 12px; color: var(--el-text-color-placeholder); }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; flex-wrap: wrap; }
.selected-tip { font-size: 13px; color: var(--el-text-color-secondary); }
.preview-wrap { margin-top: 16px; }
.preview-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; }
.card-sheet {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  background: #fff;
  padding: 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  min-height: 200px;
}
.cat-card {
  border: 1px dashed #bbb;
  border-radius: 6px;
  padding: 8px 10px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  height: 118px;
  box-sizing: border-box;
  page-break-inside: avoid;
  break-inside: avoid;
  color: #303133;
  background: #fff;
}
.card-left { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }
.card-name { font-size: 16px; font-weight: 700; line-height: 1.2; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-no { font-size: 11px; color: #606266; margin: 2px 0 4px; letter-spacing: 0.3px; }
.card-line { font-size: 11px; color: #606266; line-height: 1.5; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-line.status { color: #c45656; font-weight: 600; }
.card-right { display: flex; flex-direction: column; align-items: center; justify-content: center; }
.qr { width: 84px; height: 84px; }
.qr.placeholder { display: flex; align-items: center; justify-content: center; font-size: 10px; color: #c0c4cc; border: 1px dashed #ddd; }
.scan-tip { font-size: 10px; color: #909399; margin-top: 1px; }
</style>

<!-- 打印样式：只留卡片，隐藏左侧菜单、顶栏、筛选区 -->
<style>
@media print {
  @page { size: A4 portrait; margin: 10mm; }
  body { background: #fff !important; }
  .sidebar-container, .navbar, .tags-view-container, .no-print,
  .app-main > .app-container > .no-print { display: none !important; }
  .app-main { padding: 0 !important; margin: 0 !important; }
  .qrcard-page { padding: 0 !important; }
  .preview-wrap { margin: 0 !important; }
  .card-sheet { border: none !important; padding: 0 !important; gap: 6px !important; }
  .cat-card { border: 1px dashed #999 !important; }
}
</style>
