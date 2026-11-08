<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="猫咪" prop="catId">
        <el-select v-model="queryParams.catId" placeholder="全部猫咪" clearable filterable style="width: 200px">
          <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
        </el-select>
      </el-form-item>
      <cat-location-filter
        v-model:base-id="queryParams.catBaseId"
        v-model:ward-id="queryParams.catWardId"
        @change="handleQuery" />
      <el-form-item label="回访状态" prop="visitStatus">
        <el-select v-model="queryParams.visitStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待回访" value="0" />
          <el-option label="已回访" value="1" />
          <el-option label="回访异常" value="2" />
          <el-option label="已作废" value="3" />
          <el-option label="已结束" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="计划类型" prop="planType">
        <el-select v-model="queryParams.planType" placeholder="全部" clearable style="width: 120px">
          <el-option label="3天" value="3天" />
          <el-option label="1个月" value="1个月" />
          <el-option label="3个月" value="3个月" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:visit:add']">新增回访计划</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:visit:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:visit:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="猫咪" align="center" width="220">
        <template #default="scope">{{ catName(scope.row.catId) }}</template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="所属基地" width="150" :show-overflow-tooltip="true">
        <template #default="scope">{{ catBaseName(scope.row.catId) }}</template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="所属房间" width="110" :show-overflow-tooltip="true">
        <template #default="scope">{{ catWardName(scope.row.catId) }}</template>
      </el-table-column>
      <el-table-column label="回访计划" align="center" prop="planType" width="110">
        <template #default="scope">
          <el-tag size="small" type="info">{{ scope.row.planType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划日期" align="center" prop="planDate" width="120">
        <template #default="scope">
          <span :class="{ overdue: isOverdue(scope.row) }">{{ parseTime(scope.row.planDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="回访状态" align="center" prop="visitStatus" width="110">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.visitStatus)" size="small">{{ statusLabel(scope.row.visitStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="回访内容" align="center" prop="visitContent" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="回访照片" align="center" width="100">
        <template #default="scope">
          <el-button v-if="fileItems(scope.row.photos).length" link type="primary" @click="openPhotos(scope.row)">
            {{ fileItems(scope.row.photos).length }} 张
          </el-button>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="回访人" align="center" prop="visitBy" width="110" />
      <el-table-column v-if="!isNarrow" label="实际回访时间" align="center" prop="visitTime" width="160" />
      <el-table-column label="操作" align="center" width="180" fixed="right" class-name="small-padding fixed-width" v-if="checkPermi(['cat:visit:edit', 'cat:visit:remove'])">
        <template #default="scope">
          <el-button link type="success" icon="Edit" @click="handleVisit(scope.row)" v-hasPermi="['cat:visit:edit']">登记回访</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:visit:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 登记 / 修改回访 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-alert
        v-if="adoptionClosed"
        type="warning"
        :closable="false"
        show-icon
        title="该领养的「回访已结束」已打开，保存会被拒绝；要继续回访请先到领养台账点「重新开启」"
        style="margin-bottom: 12px"
      />
      <el-form ref="visitRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="猫咪" prop="catId">
          <el-select v-model="form.catId" placeholder="请选择猫咪" filterable style="width: 100%">
            <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
          </el-select>
        </el-form-item>
        <el-form-item label="回访计划" prop="planType">
          <el-radio-group v-model="form.planType" @change="onPlanTypeChange">
            <el-radio label="3天">3天</el-radio>
            <el-radio label="1个月">1个月</el-radio>
            <el-radio label="3个月">3个月</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划日期" prop="planDate">
          <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择计划日期" style="width: 100%" />
          <div class="plan-tip" v-if="planHint">{{ planHint }}</div>
        </el-form-item>
        <el-form-item label="回访状态" prop="visitStatus">
          <el-select v-model="form.visitStatus" style="width: 100%">
            <el-option label="待回访" value="0" />
            <el-option label="已回访" value="1" />
            <el-option label="回访异常" value="2" />
            <el-option label="已作废" value="3" />
            <el-option label="已结束" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="回访内容" prop="visitContent">
          <el-input v-model="form.visitContent" type="textarea" :rows="3" placeholder="记录猫咪适应情况、饮食、健康、与主人互动等" />
        </el-form-item>
        <el-form-item label="回访照片" prop="photos">
          <media-upload v-model="form.photos" :limit="9" :image-size="10" image-only />
        </el-form-item>
        <el-form-item label="回访人" prop="visitBy">
          <staff-select v-model="form.visitBy" scope="assistant" placeholder="请选择回访人" />
        </el-form-item>
        <el-form-item label="实际回访时间" prop="visitTime">
          <el-date-picker v-model="form.visitTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择回访时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 回访照片预览 -->
    <el-dialog v-model="photosVisible" :title="photosTitle" width="760px" append-to-body destroy-on-close>
      <el-empty v-if="!photos.length" description="暂无回访照片" :image-size="60" />
      <div v-else class="photo-grid">
        <el-image
          v-for="(u, i) in photos"
          :key="i"
          :src="resolveFileUrl(u)"
          :preview-src-list="photoUrls"
          :initial-index="i"
          fit="cover"
          preview-teleported
          class="photo-cell"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="CatVisit">
import { listCatVisit, getCatVisit, delCatVisit, addCatVisit, updateCatVisit } from '@/api/cat/visit'
import { listAdoptionByCat } from '@/api/cat/adoption'
import { listCatProfile } from '@/api/cat/profile'
import { catLabelOf, formatCatLabel, useCatCache } from '@/utils/catCache'
import { resolveFileUrl } from '@/utils/fileUrl'
import { useNarrow } from '@/utils/useNarrow'

const { proxy } = getCurrentInstance()
const { catBaseName, catWardName } = useCatCache()
const { isNarrow } = useNarrow()

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const catOptions = ref([])
const photosVisible = ref(false)
const photosTitle = ref('')
const photos = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catBaseId: undefined,
    catWardId: undefined,
    catId: undefined,
    visitStatus: undefined,
    planType: undefined
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    planType: [{ required: true, message: '请选择回访计划', trigger: 'change' }],
    planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 猫咪展示统一为「名字（档案编号）」，格式只在 utils/catCache.js 里定义
const catName = catId => catLabelOf(catOptions, catId)

function statusLabel(s) {
  return { '0': '待回访', '1': '已回访', '2': '回访异常', '3': '已作废', '4': '已结束' }[s] || '未知'
}

function statusTag(s) {
  return { '0': 'warning', '1': 'success', '2': 'danger', '3': 'info', '4': 'info' }[s] || 'info'
}

function isOverdue(row) {
  if (!row.planDate || row.visitStatus !== '0') return false
  return new Date(row.planDate).getTime() < Date.now() - 86400000
}

/** 逗号分隔的路径串 → 数组（空串过滤掉） */
function fileItems(str) {
  if (!str) return []
  return String(str)
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
}

const photoUrls = computed(() => photos.value.map(u => resolveFileUrl(u)))

function openPhotos(row) {
  photos.value = fileItems(row.photos)
  photosTitle.value = '回访照片 - ' + catName(row.catId)
  photosVisible.value = true
}

function loadCats() {
  listCatProfile({ pageNum: 1, pageSize: 999 }).then(res => {
    catOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listCatVisit(queryParams.value).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    visitId: undefined,
    adoptionId: undefined,
    catId: undefined,
    planType: '3天',
    planDate: undefined,
    visitStatus: '0',
    visitContent: undefined,
    photos: undefined,
    visitBy: undefined,
    visitTime: undefined
  }
  adoptionCtx.value = { adoptionId: undefined, adoptionDate: undefined }
  adoptionClosed.value = false
  planHint.value = ''
  proxy.clearFormValidate('visitRef')
}

// ---------------- 计划日期自动算 ----------------
// 口径：选「3天 / 1个月 / 3个月」= 领养日往后推；该猫没有领养登记时才按今天算。
// 自动填好之后仍然可以手改（有些领养人出差，实际回访日会前后挪几天）。
const adoptionCtx = ref({ adoptionId: undefined, adoptionDate: undefined })
const adoptionClosed = ref(false)
const planHint = ref('')

function pad2(n) {
  return n < 10 ? '0' + n : '' + n
}

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

/** 按档位往后推日期：月份按月末对齐，1-31 加 1 个月得到 2-28/29 而不是溢出到 3 月 */
function shiftDate(baseStr, type) {
  const parts = String(baseStr || '').slice(0, 10).split('-').map(Number)
  if (parts.length !== 3 || parts.some(n => isNaN(n))) return baseStr
  const dt = new Date(parts[0], parts[1] - 1, parts[2])
  if (type === '3天') {
    dt.setDate(dt.getDate() + 3)
  } else if (type === '1个月' || type === '3个月') {
    const months = type === '1个月' ? 1 : 3
    const day = dt.getDate()
    dt.setDate(1)
    dt.setMonth(dt.getMonth() + months)
    const lastDay = new Date(dt.getFullYear(), dt.getMonth() + 1, 0).getDate()
    dt.setDate(Math.min(day, lastDay))
  }
  return `${dt.getFullYear()}-${pad2(dt.getMonth() + 1)}-${pad2(dt.getDate())}`
}

function applyPlanDate() {
  const type = form.value.planType
  if (!type) return
  const base = adoptionCtx.value.adoptionDate || todayStr()
  const next = shiftDate(base, type)
  form.value.planDate = next
  const baseLabel = adoptionCtx.value.adoptionDate
    ? `领养日 ${String(adoptionCtx.value.adoptionDate).slice(0, 10)}`
    : '今天（该猫还没有领养登记）'
  planHint.value = `按${baseLabel}自动算：「${type}」→ ${next}，可手动改`
}

function onPlanTypeChange() {
  applyPlanDate()
}

/** 选猫后取该猫的领养记录：拿领养日算计划日期，顺便看回访是否已结束 */
function loadAdoptionContext(catId, autoFill) {
  adoptionCtx.value = { adoptionId: undefined, adoptionDate: undefined }
  adoptionClosed.value = false
  planHint.value = ''
  if (!catId) return
  listAdoptionByCat(catId).then(res => {
    const row = (res.rows || [])[0]
    if (!row) {
      if (autoFill) applyPlanDate()
      return
    }
    adoptionCtx.value = { adoptionId: row.adoptionId, adoptionDate: row.adoptionDate }
    adoptionClosed.value = row.visitClosed === '1'
    if (!form.value.adoptionId) form.value.adoptionId = row.adoptionId
    if (autoFill) {
      applyPlanDate()
    } else if (form.value.planDate) {
      planHint.value = `领养日 ${String(row.adoptionDate).slice(0, 10)}，改了档位会按它重算`
    }
  }).catch(() => {})
}

watch(() => form.value.catId, catId => {
  if (!open.value) return
  // 新增：选完猫直接把计划日期算好；修改：只带出提示，不覆盖已存的日期
  loadAdoptionContext(catId, !form.value.visitId)
})

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  // 路由带过来的筛选项（看板「待回访 / 回访异常」）不算表单初始值，要显式清掉
  queryParams.value.visitStatus = undefined
  queryParams.value.pageNum = 1
  if (Object.keys(route.query || {}).length) {
    router.replace({ path: route.path, query: {} }) // 交给 watch 重新应用并查询
  } else {
    getList()
  }
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.visitId)
  multiple.value = !selection.length
}

function handleAdd() {
  // 猫咪档案可能在别的页签刚新增，打开弹窗前重拉下拉
  loadCats()
  reset()
  open.value = true
  title.value = '新增回访计划'
}

function handleVisit(row) {
  reset()
  getCatVisit(row.visitId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '登记回访 - ' + catName(row.catId)
  })
}

function submitForm() {
  if (adoptionClosed.value) {
    proxy.$modal.msgWarning('该领养回访已结束，请先到领养台账点「重新开启」')
    return
  }
  proxy.$refs['visitRef'].validate(valid => {
    if (valid) {
      if (form.value.visitId != undefined) {
        // 已回访时自动补记回访人为当前用户、回访时间为当前时间
        if (form.value.visitStatus !== '0' && !form.value.visitTime) {
          form.value.visitTime = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
        }
        updateCatVisit(form.value).then(() => {
          proxy.$modal.msgSuccess('保存成功')
          open.value = false
          getList()
        })
      } else {
        addCatVisit(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.visitId || ids.value
  proxy.$modal.confirm('是否确认删除该回访记录？').then(function () {
    return delCatVisit(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('cat/visit/export', { ...queryParams.value }, `visit_${new Date().getTime()}.xlsx`)
}

// 从看板「待回访」等入口带过来的筛选条件，进页面即生效
const route = useRoute()
const router = useRouter()
function applyRouteQuery() {
  const q = route.query || {}
  queryParams.value.visitStatus = q.visitStatus || undefined
  queryParams.value.catId = q.catId ? Number(q.catId) : undefined
  queryParams.value.pageNum = 1
}
applyRouteQuery()
watch(() => route.query, () => {
  applyRouteQuery()
  getList()
})

loadCats()
getList()
</script>

<style scoped>
.overdue {
  color: #f56c6c;
  font-weight: 600;
}

/* 计划日期下方的算日期口径提示 */
.plan-tip {
  margin-top: 2px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}

.photo-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.photo-cell {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-fill-color-darker);
}
</style>
