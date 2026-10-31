<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" />
         </el-form-item>
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
         <el-table-column label="疫苗类型" align="center" prop="vaccineType">
            <template #default="scope">
               <dict-tag :options="cat_vaccine_type" :value="scope.row.vaccineType" />
            </template>
         </el-table-column>
         <el-table-column label="接种日期" align="center" prop="vaccineDate" :show-overflow-tooltip="true" />
         <el-table-column label="下次接种" align="center" prop="nextDate" width="110">
            <template #default="scope">
               <div>{{ scope.row.nextDate || '—' }}</div>
               <div v-if="scope.row.nextDate" class="due-tip" :class="dueInfo(scope.row.nextDate).cls">
                  {{ dueInfo(scope.row.nextDate).text }}
               </div>
            </template>
         </el-table-column>
         <el-table-column label="负责人" align="center" prop="responsible" :show-overflow-tooltip="true" />
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:vaccine:edit', 'cat:vaccine:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:vaccine:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:vaccine:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="疫苗类型" prop="vaccineType">
               <el-select v-model="form.vaccineType" placeholder="请选择疫苗类型" style="width: 100%">
                  <el-option v-for="d in cat_vaccine_type" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="接种日期" prop="vaccineDate">
               <el-date-picker v-model="form.vaccineDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择接种日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="下次接种" prop="nextDate">
               <el-date-picker v-model="form.nextDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择下次接种" style="width: 100%" />
            </el-form-item>
            <el-form-item label="负责人" prop="responsible">
               <el-input v-model="form.responsible" placeholder="请输入负责人" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
         </el-form>
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
import { listCatVaccine, getCatVaccine, delCatVaccine, addCatVaccine, updateCatVaccine } from "@/api/cat/vaccine"

const { proxy } = getCurrentInstance()
const { cat_vaccine_type } = proxy.useDict('cat_vaccine_type')
const { catLabel } = useCatCache()

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
    vaccineDate: [{ required: true, message: '请选择接种日期', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCatVaccine(queryParams.value).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 距今天差几天（按自然日算，避免时区/时分秒干扰） */
function daysUntil(dateStr) {
  if (!dateStr) return null
  const parts = String(dateStr).slice(0, 10).split('-').map(Number)
  if (parts.length !== 3 || parts.some(n => isNaN(n))) return null
  const target = new Date(parts[0], parts[1] - 1, parts[2])
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  return Math.round((target - today) / 86400000)
}

/** 「下次接种」下方的到期提示：过期红、今天/临期橙、正常灰 */
function dueInfo(nextDate) {
  const d = daysUntil(nextDate)
  if (d === null) return { text: '', cls: '' }
  if (d < 0) return { text: `已过期 ${-d} 天`, cls: 'is-danger' }
  if (d === 0) return { text: '今天到期', cls: 'is-warning' }
  if (d <= 7) return { text: `${d} 天后`, cls: 'is-warning' }
  return { text: `${d} 天后`, cls: 'is-muted' }
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
  }
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
      if (form.value.vaccineId != undefined) {
        updateCatVaccine(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCatVaccine(form.value).then(response => {
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
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
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
// 同路由仅 query 变化时（返回看板再点另一张卡）组件不会重建，需手动刷新
watch(() => route.query, () => {
  applyRouteQuery()
  getList()
})

getList()
</script>
