<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="物资名称" prop="materialName">
            <el-input v-model="queryParams.materialName" placeholder="请输入物资名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="物资分类" prop="categoryId">
            <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 180px">
               <el-option v-for="c in categoryOptions" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
            </el-select>
         </el-form-item>
         <el-form-item label="库存状态" prop="stockStatus">
            <el-select v-model="queryParams.stockStatus" placeholder="全部" clearable style="width: 140px">
               <el-option label="库存不足" value="low" />
               <el-option label="库存正常" value="ok" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:material:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:material:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:material:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:material:export']">导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="pagedList" @selection-change="handleSelectionChange" :row-class-name="rowClassName">
         <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="物资名称" align="center" prop="materialName" :show-overflow-tooltip="true" />
         <el-table-column label="分类" align="center" prop="categoryId" width="120">
            <template #default="scope">
               <span>{{ categoryName(scope.row.categoryId) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="规格" align="center" prop="spec" :show-overflow-tooltip="true" />
         <el-table-column label="单位" align="center" prop="unit" width="80" />
         <el-table-column label="库存" align="center" prop="stock" width="100">
            <template #default="scope">
               <span :class="{ 'stock-low': isLow(scope.row) }">{{ scope.row.stock }}</span>
            </template>
         </el-table-column>
         <el-table-column label="预警阈值" align="center" prop="minStock" width="100" />
         <el-table-column label="库存状态" align="center" width="110">
            <template #default="scope">
               <el-tag v-if="isLow(scope.row)" type="danger" size="small" effect="dark">库存不足</el-tag>
               <el-tag v-else type="success" size="small">正常</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:material:edit', 'cat:material:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:material:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:material:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form :model="form" ref="formRef" :rules="rules" label-width="90px">
            <el-form-item label="物资分类" prop="categoryId">
               <el-select v-model="form.categoryId" placeholder="请选择分类" clearable style="width: 100%">
                  <el-option v-for="c in categoryOptions" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
               </el-select>
            </el-form-item>
            <el-form-item label="物资名称" prop="materialName">
               <el-input v-model="form.materialName" placeholder="请输入物资名称" />
            </el-form-item>
            <el-form-item label="规格" prop="spec">
               <el-input v-model="form.spec" placeholder="请输入规格" />
            </el-form-item>
            <el-form-item label="单位" prop="unit">
               <el-input v-model="form.unit" placeholder="请输入单位" />
            </el-form-item>
            <el-form-item label="库存" prop="stock">
               <el-input-number v-model="form.stock" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="预警阈值" prop="minStock">
               <el-input-number v-model="form.minStock" :min="0" :precision="2" style="width: 100%" />
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

<script setup name="CatMaterial">
import { listCatMaterial, getCatMaterial, delCatMaterial, addCatMaterial, updateCatMaterial } from "@/api/cat/material"
import { listCatMaterialCategory } from "@/api/cat/materialcategory"

const { proxy } = getCurrentInstance()

const list = ref([])
const categoryOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const pagedList = computed(() => list.value)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    materialName: undefined,
    categoryId: undefined,
    stockStatus: undefined
  },
  rules: {
    materialName: [{ required: true, message: '物资名称不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function isLow(row) {
  const stock = Number(row.stock || 0)
  const min = Number(row.minStock || 0)
  return min > 0 && stock <= min
}

function rowClassName({ row }) {
  return isLow(row) ? 'low-stock-row' : ''
}

function categoryName(id) {
  const c = categoryOptions.value.find(x => x.categoryId === id)
  return c ? c.categoryName : '-'
}

function loadCategories() {
  listCatMaterialCategory({ pageNum: 1, pageSize: 999 }).then(res => {
    categoryOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listCatMaterial(queryParams.value).then(response => {
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
    materialId: undefined,
    categoryId: undefined,
    materialName: undefined,
    spec: undefined,
    unit: undefined,
    stock: undefined,
    minStock: undefined,
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
  ids.value = selection.map(item => item.materialId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加物资"
}

function handleUpdate(row) {
  reset()
  const id = row.materialId || ids.value
  getCatMaterial(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改物资"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.materialId != undefined) {
        updateCatMaterial(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCatMaterial(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.materialId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatMaterial(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/material/export", { ...queryParams.value }, `material_${new Date().getTime()}.xlsx`)
}

loadCategories()
getList()
</script>

<style scoped>
.stock-low {
  color: #f56c6c;
  font-weight: 700;
}
:deep(.low-stock-row) {
  background-color: var(--el-color-danger-light-9);
}
</style>
