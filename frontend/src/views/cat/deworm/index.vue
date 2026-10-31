<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" />
         </el-form-item>
         <el-form-item label="驱虫类型" prop="dewormType">
            <el-select v-model="queryParams.dewormType" placeholder="全部驱虫类型" clearable style="width: 180px">
               <el-option v-for="d in cat_deworm_type" :key="d.value" :label="d.label" :value="d.value" />
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
         <el-table-column label="驱虫类型" align="center" prop="dewormType">
            <template #default="scope">
               <dict-tag :options="cat_deworm_type" :value="scope.row.dewormType" />
            </template>
         </el-table-column>
         <el-table-column label="驱虫药" align="center" prop="dewormDrug" :show-overflow-tooltip="true" />
         <el-table-column label="驱虫日期" align="center" prop="dewormDate" :show-overflow-tooltip="true" />
         <el-table-column label="下次驱虫" align="center" prop="nextDate" :show-overflow-tooltip="true" />
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:deworm:edit', 'cat:deworm:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:deworm:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:deworm:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="驱虫类型" prop="dewormType">
               <el-select v-model="form.dewormType" placeholder="请选择驱虫类型" style="width: 100%">
                  <el-option v-for="d in cat_deworm_type" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="驱虫药" prop="dewormDrug">
               <el-input v-model="form.dewormDrug" placeholder="请输入驱虫药" />
            </el-form-item>
            <el-form-item label="驱虫日期" prop="dewormDate">
               <el-date-picker v-model="form.dewormDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择驱虫日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="下次驱虫" prop="nextDate">
               <el-date-picker v-model="form.nextDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择下次驱虫" style="width: 100%" />
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

<script setup name="CatDeworm">
import { useCatCache } from "@/utils/catCache"
import { listCatDeworm, getCatDeworm, delCatDeworm, addCatDeworm, updateCatDeworm } from "@/api/cat/deworm"

const { proxy } = getCurrentInstance()
const { cat_deworm_type } = proxy.useDict('cat_deworm_type')
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
    dewormType: undefined,
    dewormDrug: undefined,
    dewormDate: undefined,
    nextDate: undefined,
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    dewormType: [{ required: true, message: '请选择驱虫类型', trigger: 'change' }],
    dewormDate: [{ required: true, message: '请选择驱虫日期', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCatDeworm(queryParams.value).then(response => {
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
    dewormType: undefined,
    dewormDrug: undefined,
    dewormDate: undefined,
    nextDate: undefined,
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
  handleQuery()
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
    open.value = true
    title.value = "修改驱虫记录"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.dewormId != undefined) {
        updateCatDeworm(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCatDeworm(form.value).then(response => {
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
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/deworm/export", { ...queryParams.value }, `deworm_${new Date().getTime()}.xlsx`)
}

getList()
</script>
