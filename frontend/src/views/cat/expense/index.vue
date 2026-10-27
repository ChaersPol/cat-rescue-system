<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="费用类型" prop="expenseType">
            <el-select v-model="queryParams.expenseType" placeholder="全部费用类型" clearable style="width: 180px">
               <el-option v-for="d in cat_expense_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
         </el-form-item>
         <el-form-item label="支出日期">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-"
               start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:expense:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:expense:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:expense:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:expense:export']">导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="费用类型" align="center" prop="expenseType">
            <template #default="scope">
               <dict-tag :options="cat_expense_type" :value="scope.row.expenseType" />
            </template>
         </el-table-column>
         <el-table-column label="支出日期" align="center" prop="expenseDate" :show-overflow-tooltip="true" />
         <el-table-column label="供应商" align="center" prop="supplier" :show-overflow-tooltip="true" />
         <el-table-column label="清单/说明" align="center" prop="items" :show-overflow-tooltip="true" />
         <el-table-column label="金额" align="center" prop="amount" :show-overflow-tooltip="true" />
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:expense:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:expense:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="费用类型" prop="expenseType">
               <el-select v-model="form.expenseType" placeholder="请选择费用类型" style="width: 100%">
                  <el-option v-for="d in cat_expense_type" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="支出日期" prop="expenseDate">
               <el-date-picker v-model="form.expenseDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择支出日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="供应商" prop="supplier">
               <el-input v-model="form.supplier" placeholder="请输入供应商" />
            </el-form-item>
            <el-form-item label="清单/说明" prop="items">
               <el-input v-model="form.items" type="textarea" :rows="2" placeholder="采购物资清单 / 费用说明" />
            </el-form-item>
            <el-form-item label="金额" prop="amount">
               <el-input-number v-model="form.amount" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
            <el-form-item label="付款方式" prop="payMethod">
               <el-input v-model="form.payMethod" placeholder="微信/支付宝/转账/现金" />
            </el-form-item>
            <el-form-item label="归属基地" prop="baseId">
               <el-select v-model="form.baseId" placeholder="请选择归属基地" clearable filterable style="width: 100%">
                  <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
               </el-select>
            </el-form-item>
            <el-form-item label="关联猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="关联入库单" prop="stockId">
               <el-select v-model="form.stockId" placeholder="物资采购可与一笔入库单关联" clearable filterable style="width: 100%">
                  <el-option v-for="r in stockOptions" :key="r.recordId"
                     :label="stockLabel(r)"
                     :value="r.recordId" />
               </el-select>
            </el-form-item>
            <el-form-item label="票据/截图" prop="receiptUrl">
               <image-upload v-model="form.receiptUrl" :limit="3" :fileSize="10" />
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

<script setup name="CatExpense">
import { useBaseCache } from "@/utils/catCache"
import { listCatExpense, getCatExpense, delCatExpense, addCatExpense, updateCatExpense } from "@/api/cat/expense"
import { listCatStockRecord } from "@/api/cat/stockrecord"
import { listCatMaterial } from "@/api/cat/material"

const { proxy } = getCurrentInstance()
const { cat_expense_type } = proxy.useDict('cat_expense_type')
const { baseMap } = useBaseCache()
const baseOptions = computed(() => Object.values(baseMap.value))
// 入库单下拉（物资采购与入库单一一对应）
const stockOptions = ref([])
const materialMap = ref({})

function stockLabel(r) {
  const m = materialMap.value[r.materialId]
  const name = m ? m.materialName : '物资#' + r.materialId
  return `${name} × ${r.quantity}（${String(r.recordDate || '').substring(0, 10)}）`
}

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    expenseType: undefined,
    expenseDate: undefined,
    supplier: undefined,
    items: undefined,
    amount: undefined,
  },
  rules: {
    expenseType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
    expenseDate: [{ required: true, message: '请选择支出日期', trigger: 'change' }],
    amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCatExpense(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function loadStockOptions() {
  listCatMaterial({ pageNum: 1, pageSize: 500 }).then(res => {
    const map = {}
    ;(res.rows || []).forEach(m => { map[m.materialId] = m })
    materialMap.value = map
  })
  listCatStockRecord({ pageNum: 1, pageSize: 200, recordType: '1' }).then(res => {
    stockOptions.value = res.rows || []
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    expenseType: undefined,
    expenseDate: undefined,
    supplier: undefined,
    items: undefined,
    amount: undefined,
  }
  proxy.resetForm("formRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.expenseId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加财务支出"
}

function handleUpdate(row) {
  reset()
  const id = row.expenseId || ids.value
  getCatExpense(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改财务支出"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.expenseId != undefined) {
        updateCatExpense(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCatExpense(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.expenseId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatExpense(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/expense/export", { ...queryParams.value }, `expense_${new Date().getTime()}.xlsx`)
}

// 从看板「支出结构」柱子点击进来时，按费用类型预筛选
const route = useRoute()
if (route.query.expenseType) {
   queryParams.value.expenseType = String(route.query.expenseType)
}

loadStockOptions()
getList()
</script>
