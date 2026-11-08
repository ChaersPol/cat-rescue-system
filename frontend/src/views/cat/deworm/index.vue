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
         <el-form-item label="驱虫类型" prop="dewormType">
            <el-select v-model="queryParams.dewormType" placeholder="全部驱虫类型" clearable style="width: 180px">
               <el-option v-for="d in cat_deworm_type" :key="d.value" :label="d.label" :value="d.value" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:deworm:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:deworm:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:deworm:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:deworm:export']">导出</el-button>
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
         <el-table-column label="驱虫类型" align="center" prop="dewormType">
            <template #default="scope">
               <dict-tag :options="cat_deworm_type" :value="scope.row.dewormType" />
            </template>
         </el-table-column>
         <el-table-column label="驱虫药" align="center" prop="dewormDrug" :show-overflow-tooltip="true" />
         <el-table-column label="驱虫日期" align="center" prop="dewormDate" :show-overflow-tooltip="true" />
         <el-table-column label="下次驱虫" align="center" prop="nextDate" width="110">
            <template #default="scope">
               <div>{{ scope.row.nextDate || '—' }}</div>
               <div v-if="scope.row.nextDate" class="due-tip" :class="dueInfo(scope.row.nextDate).cls">
                  {{ dueInfo(scope.row.nextDate).text }}
               </div>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="150" fixed="right" class-name="small-padding fixed-width" v-if="checkPermi(['cat:deworm:edit', 'cat:deworm:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:deworm:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:deworm:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="700px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <!-- 不预选、必须手选：以前默认停在「体内驱虫」，医生不改就保存，台账里体外永远是空的 -->
            <el-form-item label="驱虫类型" prop="dewormType">
               <el-select v-model="form.dewormType" placeholder="请选择：体内驱虫 / 体外驱虫（必选）" style="width: 100%" @change="onDewormTypeChange">
                  <el-option v-for="d in cat_deworm_type" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="驱虫药" prop="dewormDrug">
               <el-input v-model="form.dewormDrug" placeholder="请输入驱虫药" />
            </el-form-item>
            <el-form-item label="关联物资" prop="materialId">
               <material-select v-model="form.materialId" @change="onMaterialChange" />
               <div class="form-tip">挂了驱虫药物资，保存后自动生成<b>出库单</b>并扣库存；不选就不动库存。</div>
            </el-form-item>
            <el-form-item label="本次用量" prop="stockQty" v-if="form.materialId">
               <el-input-number v-model="form.stockQty" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
               <div class="form-tip" v-if="materialStock !== null">
                  当前库存 <b>{{ materialStock }}{{ materialUnit }}</b>
                  <span v-if="form.stockQty > materialStock" class="stock-warn"> · 本次用量超出库存，保存后库存会变成负数（允许，事后入库补齐即可）</span>
                  <span v-else> · 扣完剩 {{ roundStock(materialStock - (form.stockQty || 0)) }}{{ materialUnit }}</span>
               </div>
            </el-form-item>
            <el-form-item label="驱虫日期" prop="dewormDate">
               <el-date-picker v-model="form.dewormDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择驱虫日期" style="width: 100%" @change="onDewormDateChange" />
            </el-form-item>
            <el-form-item label="驱虫间隔">
               <el-radio-group v-model="cycleDays" @change="onCycleChange">
                  <el-radio-button v-for="d in cycleOptions" :key="d" :label="d">{{ d }} 天</el-radio-button>
               </el-radio-group>
               <div class="form-tip">体内、体外分开计算；常规 30 天，按医嘱可改 60 天</div>
            </el-form-item>
            <el-form-item label="下次驱虫" prop="nextDate">
               <el-date-picker v-model="form.nextDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择下次驱虫" style="width: 100%" />
               <div class="form-tip" v-if="nextDateTip">{{ nextDateTip }}</div>
               <!-- 反向同步：提醒页推迟会改这里，这里改了提醒也会跟着改，必须让操作的人知道 -->
               <div class="form-tip" v-if="form.dewormId">改动这个日期，会同步更新<b>已生成的驱虫提醒</b>日期</div>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
         </el-form>

         <!-- 上次驱虫：只给上一次体内、上一次体外，判断这次该补哪一种 -->
         <div class="hist-panel" v-if="form.catId">
            <div class="hist-head">
               <span class="hist-title">上次驱虫记录</span>
               <span class="hist-note">体内 / 体外分开计算，互不影响</span>
            </div>
            <div class="last-list">
               <div class="last-item" v-for="item in lastList" :key="item.key">
                  <el-tag size="small" :type="item.key === 'in' ? 'success' : 'primary'" effect="plain">{{ item.label }}</el-tag>
                  <template v-if="item.record">
                     <span class="last-date">{{ item.record.dewormDate }}</span>
                     <span class="last-drug">{{ item.record.dewormDrug || '—' }}</span>
                     <span class="last-next">下次 {{ item.record.nextDate || '—' }}</span>
                     <span class="due-tip" :class="dueInfo(item.record.nextDate).cls">{{ dueInfo(item.record.nextDate).text }}</span>
                  </template>
                  <span v-else class="last-none">该猫还没有{{ item.label }}记录</span>
               </div>
            </div>
            <div class="form-tip" v-if="gapTip">{{ gapTip }}</div>
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

<script setup name="CatDeworm">
import { useCatCache } from "@/utils/catCache"
import { useNarrow } from "@/utils/useNarrow"
import { calcDewormNextDate, dewormCycleOf, dueInfo, parseDate, formatDate, DEWORM_CYCLE_OPTIONS, DEWORM_DEFAULT_CYCLE } from "@/utils/immunization"
import { listCatDeworm, getCatDeworm, delCatDeworm, addCatDeworm, updateCatDeworm, getDewormHistory } from "@/api/cat/deworm"

const { proxy } = getCurrentInstance()
const { cat_deworm_type } = proxy.useDict('cat_deworm_type')
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
    dewormType: undefined,
    dewormDrug: undefined,
    dewormDate: undefined,
    nextDate: undefined,
    dueDays: undefined,
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    dewormType: [{ required: true, message: '请选择驱虫类型', trigger: 'change' }],
    dewormDate: [{ required: true, message: '请选择驱虫日期', trigger: 'change' }]
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

function getList() {
  loading.value = true
  listCatDeworm(queryParams.value).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// ---- 上次驱虫 / 驱虫间隔（弹窗下方） ----
const cycleOptions = DEWORM_CYCLE_OPTIONS
const cycleDays = ref(DEWORM_DEFAULT_CYCLE)
const lastRecords = ref({ lastIn: null, lastOut: null })

/** 体内、体外各一行，没有记录的那一行明确写「还没有」 */
const lastList = computed(() => [
  { key: 'in', label: '体内驱虫', record: lastRecords.value.lastIn },
  { key: 'out', label: '体外驱虫', record: lastRecords.value.lastOut },
])

/** 选中的类型上一次是什么时候，这次该不该补 */
const gapTip = computed(() => {
  const type = form.value.dewormType
  if (!type) return ''
  const isOut = type.includes('体外')
  const label = isOut ? '体外' : '体内'
  const record = isOut ? lastRecords.value.lastOut : lastRecords.value.lastIn
  if (!record || !record.dewormDate) return `该猫还没有${label}驱虫记录，这次是第一次`
  // 正在修改这条：它本身就是该猫最近一次同类驱虫，别提示成「距上次 0 天」
  if (form.value.dewormId && (!form.value.dewormDate || form.value.dewormDate === record.dewormDate)) {
    return `这条记录就是该猫最近一次${label}驱虫（${record.dewormDate}）`
  }
  const base = form.value.dewormDate || record.dewormDate
  const start = parseDate(record.dewormDate)
  const end = parseDate(base)
  const gap = start && end ? Math.round((end - start) / 86400000) : 0
  if (!gap) return ''
  return `上次${label}驱虫：${record.dewormDate}，距今 ${gap} 天${gap > 60 ? '，已超过最长 60 天周期' : ''}`
})

function loadHistory() {
  lastRecords.value = { lastIn: null, lastOut: null }
  const catId = form.value.catId
  if (!catId) return
  getDewormHistory(catId).then(res => {
    const d = res.data || {}
    lastRecords.value = { lastIn: d.lastIn || null, lastOut: d.lastOut || null }
  }).catch(() => {})
}

watch(() => form.value.catId, () => loadHistory())

/** 该类型上一次的驱虫日期（新增时还没填本次日期，用它当推算基准） */
function lastDateOf(type) {
  if (!type) return ''
  const record = type.includes('体外') ? lastRecords.value.lastOut : lastRecords.value.lastIn
  return record && record.dewormDate ? record.dewormDate : ''
}

/** 重算下次驱虫：基准日期优先用本次填的日期，其次用上次同类记录 */
function recalcNextDate() {
  const base = form.value.dewormDate || lastDateOf(form.value.dewormType)
  const auto = calcDewormNextDate(base, cycleDays.value)
  if (auto) form.value.nextDate = auto
}

function onDewormTypeChange() {
  // 现场录入基本都是当天做的，定了类型就把日期默认今天，省一步；要记往期的直接改日期
  if (!form.value.dewormDate) form.value.dewormDate = formatDate(new Date())
  recalcNextDate()
}

function onDewormDateChange() {
  recalcNextDate()
}

function onCycleChange() {
  recalcNextDate()
}

const nextDateTip = computed(() => {
  const base = form.value.dewormDate || lastDateOf(form.value.dewormType)
  const auto = calcDewormNextDate(base, cycleDays.value)
  if (!auto) return ''
  const from = form.value.dewormDate ? '本次驱虫日期' : '上次同类驱虫日期'
  if (form.value.nextDate === auto) {
    return `已按${from} ${base} + ${cycleDays.value} 天 → ${auto}，可手动改`
  }
  return `按 ${from} ${base} + ${cycleDays.value} 天应为 ${auto}，当前填的是 ${form.value.nextDate || '空'}`
})

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    catId: undefined,
    dewormType: undefined,
    dewormDrug: undefined,
    dewormDate: undefined,
    nextDate: undefined,
    remark: undefined,
    materialId: undefined,
    materialName: undefined,
    stockQty: undefined,
  }
  materialStock.value = null
  materialUnit.value = ''
  cycleDays.value = DEWORM_DEFAULT_CYCLE
  lastRecords.value = { lastIn: null, lastOut: null }
  proxy.clearFormValidate("formRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  // 路由带过来的筛选项（如看板点「待驱虫」的 dueDays=7）不算表单初始值，要显式清掉
  queryParams.value.dueDays = undefined
  queryParams.value.pageNum = 1
  if (Object.keys(route.query || {}).length) {
    router.replace({ path: route.path, query: {} }) // 交给 watch 重新应用并查询
  } else {
    getList()
  }
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dewormId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加驱虫记录"
}

function handleUpdate(row) {
  reset()
  const id = row.dewormId || ids.value
  getCatDeworm(id).then(response => {
    form.value = response.data
    // 回显间隔：这条记录下次日期 - 驱虫日期，正好是 30 / 60 天就选上
    cycleDays.value = dewormCycleOf(response.data.dewormDate, response.data.nextDate)
    open.value = true
    title.value = "修改驱虫记录"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 间隔天数只用于后端兜底推算，不入库
      const payload = { ...form.value, cycleDays: cycleDays.value }
      // 没选物资就传 0：后端据此清空关联并把之前那次出库冲销掉
      if (payload.materialId === undefined || payload.materialId === null || payload.materialId === '') {
        payload.materialId = 0
        payload.materialName = undefined
        payload.stockQty = undefined
      }
      if (form.value.dewormId != undefined) {
        updateCatDeworm(payload).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          // 后台把「下次驱虫」同步到了提醒上，右上角单独说明
          if (response.syncTip) {
            proxy.$modal.notifySuccess(response.syncTip)
          }
          open.value = false
          getList()
        })
      } else {
        addCatDeworm(payload).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.dewormId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatDeworm(delIds)
  }).then((response) => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
    if (response.syncTip) {
      proxy.$modal.notifySuccess(response.syncTip)
    }
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/deworm/export", { ...queryParams.value }, `deworm_${new Date().getTime()}.xlsx`)
}

// 从看板「待驱虫」等入口带过来的筛选条件，进页面即生效
const route = useRoute()
const router = useRouter()
function applyRouteQuery() {
  const q = route.query || {}
  queryParams.value.dueDays = q.dueDays ? Number(q.dueDays) : undefined
  queryParams.value.catId = q.catId ? Number(q.catId) : undefined
  queryParams.value.dewormType = q.dewormType || undefined
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
watch(() => route.query, () => {
  applyRouteQuery()
  openQuickEntry()
  getList()
})

getList()

// 在提醒页推迟驱虫提醒时，这条记录的「下次驱虫」会被后台一起顺延；
// 页签被 keep-alive 缓存，切回来补拉一次，避免看到旧日期。
let firstActivate = true
onActivated(() => {
  if (firstActivate) {
    firstActivate = false
    return
  }
  getList()
})
</script>
