<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" />
         </el-form-item>
         <cat-location-filter
           v-model:base-id="queryParams.catBaseId"
           v-model:ward-id="queryParams.catWardId"
           @change="handleQuery" />
         <el-form-item label="疫苗类型" prop="vaccineType">
            <el-select v-model="queryParams.vaccineType" placeholder="全部疫苗类型" clearable style="width: 180px">
               <el-option v-for="d in cat_vaccine_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
         </el-form-item>
         <el-form-item label="到期情况" prop="dueDays">
            <el-select v-model="queryParams.dueDays" placeholder="全部" clearable style="width: 180px">
               <el-option label="7 天内到期及已过期" :value="7" />
               <el-option label="30 天内到期及已过期" :value="30" />
            </el-select>
         </el-form-item>
         <el-form-item label="来源" prop="sourceType">
            <el-select v-model="queryParams.sourceType" placeholder="全部来源" clearable style="width: 140px">
               <el-option label="手动录入" value="1" />
               <el-option label="站内诊断" value="2" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:vaccine:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:vaccine:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:vaccine:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:vaccine:export']">导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="猫咪" align="center" prop="catId" width="210" :show-overflow-tooltip="true">
            <template #default="scope">
               {{ catLabel(scope.row.catId) }}
            </template>
         </el-table-column>
         <el-table-column v-if="!isNarrow" label="所属基地" width="150" :show-overflow-tooltip="true">
           <template #default="scope">{{ catBaseName(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column v-if="!isNarrow" label="所属房间" width="110" :show-overflow-tooltip="true">
           <template #default="scope">{{ catWardName(scope.row.catId) }}</template>
         </el-table-column>
         <el-table-column v-if="!isNarrow" label="来源" align="center" prop="sourceType" width="100">
            <template #default="scope">
               <el-tag :type="scope.row.sourceType === '2' ? 'primary' : 'info'" size="small" effect="plain">
                  {{ scope.row.sourceType === '2' ? '站内诊断' : '手动录入' }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="疫苗类型" align="center" prop="vaccineType">
            <template #default="scope">
               <dict-tag :options="cat_vaccine_type" :value="scope.row.vaccineType" />
            </template>
         </el-table-column>
         <el-table-column label="接种日期" align="center" prop="vaccineDate" :show-overflow-tooltip="true" />
         <el-table-column label="下次接种" align="center" prop="nextDate" width="130">
            <template #default="scope">
               <div>{{ scope.row.nextDate || '—' }}</div>
               <div v-if="scope.row.nextDate" class="due-tip" :class="dueInfo(scope.row.nextDate).cls">
                  {{ dueInfo(scope.row.nextDate).text }}
               </div>
               <!-- 用药期间不能接种：红字提醒，避免看着「该打了」就直接安排 -->
               <div v-if="scope.row.medicationTip" class="med-tip">
                  <el-icon><WarningFilled /></el-icon>
                  <span>用药中 · 暂缓接种</span>
               </div>
            </template>
         </el-table-column>
         <el-table-column v-if="!isNarrow" label="负责人" align="center" prop="responsible" :show-overflow-tooltip="true" />
         <el-table-column label="操作" align="center" width="150" fixed="right" class-name="small-padding fixed-width" v-if="checkPermi(['cat:vaccine:edit', 'cat:vaccine:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:vaccine:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:vaccine:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="760px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <!-- 选中的猫正在用药：红字警示，提示不能接种、需要推迟 -->
            <el-form-item v-if="dialogMedTip">
               <div class="med-warning">
                  <el-icon><WarningFilled /></el-icon>
                  <span>{{ dialogMedTip }}</span>
               </div>
            </el-form-item>
            <el-form-item label="疫苗类型" prop="vaccineType">
               <el-select v-model="form.vaccineType" placeholder="请选择疫苗类型" style="width: 100%" @change="onVaccineTypeChange">
                  <el-option v-for="d in cat_vaccine_type" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
               <div class="form-tip">基础免疫（第一/二/三针）间隔 21 天；狂犬、加强针按 11 个月周期，到期自动进提醒</div>
            </el-form-item>
            <el-form-item label="接种日期" prop="vaccineDate">
               <el-date-picker v-model="form.vaccineDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择接种日期" style="width: 100%" @change="onVaccineDateChange" />
            </el-form-item>
            <el-form-item label="下次接种" prop="nextDate">
               <el-date-picker v-model="form.nextDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择下次接种" style="width: 100%" />
               <!-- 自动算出来的日期允许手动改（个别猫要按医嘱调整） -->
               <div class="form-tip" v-if="nextDateTip">{{ nextDateTip }}</div>
               <!-- 反向同步：提醒页推迟会改这里，这里改了提醒也会跟着改，必须让操作的人知道 -->
               <div class="form-tip" v-if="form.vaccineId">改动这个日期，会同步更新<b>已生成的疫苗提醒</b>日期</div>
            </el-form-item>
            <el-form-item label="关联物资" prop="materialId">
               <material-select v-model="form.materialId" @change="onMaterialChange" />
               <div class="form-tip">挂了疫苗物资，保存后自动生成<b>出库单</b>并扣库存；不选就不动库存。</div>
            </el-form-item>
            <el-form-item label="本次用量" prop="stockQty" v-if="form.materialId">
               <el-input-number v-model="form.stockQty" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
               <div class="form-tip" v-if="materialStock !== null">
                  当前库存 <b>{{ materialStock }}{{ materialUnit }}</b>
                  <span v-if="form.stockQty > materialStock" class="stock-warn"> · 本次用量超出库存，保存后库存会变成负数（允许，事后入库补齐即可）</span>
                  <span v-else> · 扣完剩 {{ roundStock(materialStock - (form.stockQty || 0)) }}{{ materialUnit }}</span>
               </div>
            </el-form-item>
            <el-form-item label="负责人" prop="responsible">
               <staff-select v-model="form.responsible" scope="assistant" placeholder="请选择负责人" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
         </el-form>

         <!-- 该猫接种记录：改一条记录时也能看到第一针、第二针什么时候打的，不用回列表翻 -->
         <div class="hist-panel" v-if="form.catId">
            <div class="hist-head">
               <span class="hist-title">该猫接种记录（{{ historyRows.length }}）</span>
               <el-checkbox v-model="historyDueOnly" size="small">只看已到期的</el-checkbox>
            </div>
            <div class="hist-chips" v-if="historyOverview">
               <span class="imm-chip">
                  基础免疫 <b>{{ historyOverview.baseDoseDone }}/{{ historyOverview.baseDoseTotal }}</b> 针
                  <span class="sub" v-if="historyOverview.baseDoseNextDate">· 下一针 {{ historyOverview.baseDoseNextDate }}</span>
                  <span class="sub" v-else>· 已打完</span>
               </span>
               <span class="imm-chip" :class="chipClass(historyOverview.rabiesDaysLeft)">
                  狂犬
                  <span class="sub" v-if="historyOverview.rabiesNextDate">下次 {{ historyOverview.rabiesNextDate }}（{{ chipText(historyOverview.rabiesDaysLeft) }}）</span>
                  <span class="sub" v-else>未接种</span>
               </span>
               <span class="imm-chip" :class="chipClass(historyOverview.boosterDaysLeft)">
                  加强针
                  <span class="sub" v-if="historyOverview.boosterNextDate">下次 {{ historyOverview.boosterNextDate }}（{{ chipText(historyOverview.boosterDaysLeft) }}）</span>
                  <span class="sub" v-else>未接种</span>
               </span>
            </div>
            <el-table :data="historyRows" size="small" border max-height="230" :row-class-name="historyRowClass"
                      :empty-text="historyDueOnly ? '没有已到期的针次' : '该猫还没有疫苗记录'">
               <el-table-column label="针次" width="86" align="center">
                  <template #default="scope">
                     <el-tag size="small" :type="scope.row.doseNo ? 'primary' : 'info'" effect="plain">
                        {{ scope.row.doseNo ? `第${scope.row.doseNo}针` : '年度' }}
                     </el-tag>
                  </template>
               </el-table-column>
               <el-table-column label="疫苗类型" prop="vaccineType" width="96" align="center" />
               <el-table-column label="接种日期" prop="vaccineDate" width="106" align="center" />
               <el-table-column label="下次接种" min-width="150">
                  <template #default="scope">
                     <div>{{ scope.row.nextDate || '—' }}</div>
                     <div v-if="scope.row.nextDate" class="due-tip" :class="dueInfo(scope.row.nextDate).cls">
                        {{ dueInfo(scope.row.nextDate).text }}
                     </div>
                  </template>
               </el-table-column>
               <el-table-column label="负责人" prop="responsible" width="96" align="center" />
            </el-table>
            <div class="form-tip">已到期的记录标红；点「只看已到期的」可以快速排这一批该补哪几针</div>
         </div>

         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="CatVaccine">
import { useCatCache } from "@/utils/catCache"
import { useNarrow } from "@/utils/useNarrow"
import { calcVaccineNextDate, dueInfo, daysUntil } from "@/utils/immunization"
import { listCatVaccine, getCatVaccine, delCatVaccine, addCatVaccine, updateCatVaccine, getVaccineMedicationTip, getVaccineHistory } from "@/api/cat/vaccine"

const { proxy } = getCurrentInstance()
const { cat_vaccine_type } = proxy.useDict('cat_vaccine_type')
const { catLabel, catBaseName, catWardName } = useCatCache()
const { isNarrow } = useNarrow()

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catBaseId: undefined,
    catWardId: undefined,
    sourceType: undefined,
    catId: undefined,
    vaccineType: undefined,
    vaccineDate: undefined,
    nextDate: undefined,
    responsible: undefined,
    dueDays: undefined,
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    vaccineType: [{ required: true, message: '请选择疫苗类型', trigger: 'change' }],
    vaccineDate: [{ required: true, message: '请选择接种日期', trigger: 'change' }],
    responsible: [{ required: true, message: '请选择负责人', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 关联物资：选中后即时算「扣完剩多少」，库存不足只红字提示，不阻断保存
const materialStock = ref(null)
const materialUnit = ref('')

function onMaterialChange(material) {
  materialStock.value = material ? Number(material.stock) : null
  materialUnit.value = material ? (material.unit || '') : ''
  form.value.materialName = material ? material.materialName : undefined
  if (!material) {
    // 传 0 表示「主动取消关联」，否则后端动态 update 写不进 null
    form.value.materialId = 0
    form.value.stockQty = undefined
  } else if (!form.value.stockQty) {
    form.value.stockQty = 1
  }
}

function roundStock(value) {
  const num = Number(value)
  if (!Number.isFinite(num)) return 0
  return Number.isInteger(num) ? num : Number(num.toFixed(2))
}

// 弹窗里选中猫咪后，如果它正在用药期，红字提示「不能接种、建议推迟」
const dialogMedTip = ref('')
watch(() => form.value.catId, catId => {
  dialogMedTip.value = ''
  loadHistory()
  if (!catId || !open.value) return
  getVaccineMedicationTip(catId).then(res => {
    if (res && res.inMedication) {
      dialogMedTip.value = res.tip || '该猫近期在用药，暂缓接种。'
    }
  }).catch(() => {})
})

// ---- 该猫接种记录（弹窗下方） ----
const vaccineHistory = ref([])
const historyOverview = ref(null)
const historyDueOnly = ref(false)

/** 已到期（或 3 天内到期）的记录，勾选「只看已到期的」时用 */
const historyRows = computed(() => {
  if (!historyDueOnly.value) return vaccineHistory.value
  return vaccineHistory.value.filter(row => {
    const d = daysUntil(row.nextDate)
    return d !== null && d <= 3
  })
})

function loadHistory() {
  vaccineHistory.value = []
  historyOverview.value = null
  const catId = form.value.catId
  if (!catId) return
  getVaccineHistory(catId).then(res => {
    vaccineHistory.value = (res.data && res.data.rows) || []
    historyOverview.value = (res.data && res.data.overview) || null
  }).catch(() => {})
}

/** 已过期的行标红，翻记录时一眼看到哪几针超期了 */
function historyRowClass({ row }) {
  const d = daysUntil(row.nextDate)
  return d !== null && d < 0 ? 'row-due' : ''
}

/** 概览小标签的配色 */
function chipClass(daysLeft) {
  if (daysLeft === null || daysLeft === undefined) return 'is-muted'
  if (daysLeft < 0) return 'is-danger'
  if (daysLeft <= 3) return 'is-warning'
  return ''
}

function chipText(daysLeft) {
  if (daysLeft === null || daysLeft === undefined) return '—'
  if (daysLeft < 0) return `已过期 ${-daysLeft} 天`
  if (daysLeft === 0) return '今天到期'
  return `还有 ${daysLeft} 天`
}

// ---- 下次接种自动计算 ----
// 只在用户改「疫苗类型 / 接种日期」时重算，打开修改弹窗时保留库里已有的日期，不被规则覆盖
function onVaccineTypeChange(type) {
  const auto = calcVaccineNextDate(type, form.value.vaccineDate)
  if (auto) form.value.nextDate = auto
}

function onVaccineDateChange(date) {
  const auto = calcVaccineNextDate(form.value.vaccineType, date)
  if (auto) form.value.nextDate = auto
}

const nextDateTip = computed(() => {
  const type = form.value.vaccineType
  const date = form.value.vaccineDate
  const auto = calcVaccineNextDate(type, date)
  if (!auto) return ''
  const rule = type && (type.includes('狂犬') || type.includes('加强')) ? '11 个月' : '21 天'
  if (form.value.nextDate === auto) {
    return `已按「${type}」自动推算：${date} + ${rule} → ${auto}，可手动改`
  }
  return `按规则「${type}」应为 ${auto}（${date} + ${rule}），当前填的是 ${form.value.nextDate || '空'}`
})

function getList() {
  loading.value = true
  listCatVaccine(queryParams.value).then(response => {
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
    catId: undefined,
    vaccineType: undefined,
    vaccineDate: undefined,
    nextDate: undefined,
    responsible: undefined,
    remark: undefined,
    materialId: undefined,
    materialName: undefined,
    stockQty: undefined,
  }
  materialStock.value = null
  materialUnit.value = ''
  dialogMedTip.value = ''
  vaccineHistory.value = []
  historyOverview.value = null
  historyDueOnly.value = false
  proxy.clearFormValidate("formRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  // 路由带过来的筛选项（如看板点「待疫苗」的 dueDays=7）不算表单初始值，要显式清掉
  queryParams.value.dueDays = undefined
  queryParams.value.pageNum = 1
  if (Object.keys(route.query || {}).length) {
    router.replace({ path: route.path, query: {} }) // 交给 watch 重新应用并查询
  } else {
    getList()
  }
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.vaccineId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加疫苗记录"
}

function handleUpdate(row) {
  reset()
  const id = row.vaccineId || ids.value
  getCatVaccine(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改疫苗记录"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      const payload = { ...form.value }
      // 没选物资就传 0：后端据此清空关联并把之前那次出库冲销掉
      if (payload.materialId === undefined || payload.materialId === null || payload.materialId === '') {
        payload.materialId = 0
        payload.materialName = undefined
        payload.stockQty = undefined
      }
      if (form.value.vaccineId != undefined) {
        updateCatVaccine(payload).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          // 后台把「下次接种」同步到了提醒上，右上角单独说明，别让用户以为只是改了台账
          if (response.syncTip) {
            proxy.$modal.notifySuccess(response.syncTip)
          }
          open.value = false
          getList()
        })
      } else {
        addCatVaccine(payload).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.vaccineId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatVaccine(delIds)
  }).then((response) => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
    if (response.syncTip) {
      proxy.$modal.notifySuccess(response.syncTip)
    }
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/vaccine/export", { ...queryParams.value }, `vaccine_${new Date().getTime()}.xlsx`)
}

// 从看板「待疫苗」等入口带过来的筛选条件，进页面即生效
const route = useRoute()
const router = useRouter()
function applyRouteQuery() {
  const q = route.query || {}
  queryParams.value.dueDays = q.dueDays ? Number(q.dueDays) : undefined
  queryParams.value.catId = q.catId ? Number(q.catId) : undefined
  queryParams.value.vaccineType = q.vaccineType || undefined
  queryParams.value.pageNum = 1
}
applyRouteQuery()
// 扫码页「+ 快速记一笔」带 add=1&catId=xx 进来：直接打开新增，猫咪已填好
function openQuickEntry() {
  const q = route.query || {}
  if (String(q.add) !== '1' || !q.catId) return
  handleAdd()
  form.value.catId = Number(q.catId)
  router.replace({ path: route.path, query: {} })
}
openQuickEntry()
// 同路由仅 query 变化时（返回看板再点另一张卡）组件不会重建，需手动刷新
watch(() => route.query, () => {
  applyRouteQuery()
  openQuickEntry()
  getList()
})

getList()

// 在提醒页推迟疫苗提醒时，这条记录的「下次接种」会被后台一起顺延。
// 页签是 keep-alive 缓存的，切回来不会重建组件，这里激活时补拉一次，避免看到旧日期。
let firstActivate = true
onActivated(() => {
  if (firstActivate) {
    firstActivate = false
    return
  }
  getList()
})
</script>
