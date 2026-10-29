<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="类型名称" prop="typeName">
            <el-input v-model="queryParams.typeName" placeholder="请输入类型名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
               <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:wardtype:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:wardtype:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:wardtype:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:wardtype:export']">导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-alert type="info" :closable="false" class="tip-alert"
                title="这里维护「病房类型」字典，病房管理里的类型下拉和列表颜色都取自这里；还有病房在用的类型不能删除，改名会自动同步到这些病房。" />

      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="类型名称" align="center" prop="typeName" :show-overflow-tooltip="true" min-width="120">
            <template #default="scope">
               <el-tag :type="scope.row.tagType || 'primary'" effect="light">{{ scope.row.typeName }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="显示顺序" align="center" prop="orderNum" width="100" />
         <el-table-column label="病房数量" align="center" prop="wardCount" width="100">
            <template #default="scope">
               <span :class="{ 'zero-count': !scope.row.wardCount }">{{ scope.row.wardCount || 0 }}</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="100">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" min-width="160" />
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:wardtype:edit', 'cat:wardtype:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:wardtype:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:wardtype:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="560px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="类型名称" prop="typeName">
               <el-input v-model="form.typeName" placeholder="如：隔离病房 / 术后病房" maxlength="50" show-word-limit />
            </el-form-item>
            <el-form-item label="标签颜色" prop="tagType">
               <el-select v-model="form.tagType" placeholder="请选择标签颜色" style="width: 100%">
                  <el-option v-for="c in colorOptions" :key="c.value" :label="c.label" :value="c.value">
                     <el-tag :type="c.value" effect="light" size="small">{{ c.label }}</el-tag>
                  </el-option>
               </el-select>
               <div class="color-preview">
                  预览：<el-tag :type="form.tagType || 'primary'" effect="light">{{ form.typeName || '类型名称' }}</el-tag>
               </div>
            </el-form-item>
            <el-form-item label="显示顺序" prop="orderNum">
               <el-input-number v-model="form.orderNum" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
               <el-radio-group v-model="form.status">
                  <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="这个类型的用途、注意事项" maxlength="500" show-word-limit />
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

<script setup name="CatWardType">
import { listCatWardType, getCatWardType, delCatWardType, addCatWardType, updateCatWardType } from "@/api/cat/wardtype"
import { useWardTypeCache } from "@/utils/catCache"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')
// 增删改后要让病房页/其它页的类型下拉缓存失效
const { reloadWardTypeCache } = useWardTypeCache()

const colorOptions = [
  { value: 'primary', label: '蓝色（默认）' },
  { value: 'success', label: '绿色' },
  { value: 'warning', label: '橙色' },
  { value: 'danger', label: '红色（隔离/危险）' },
  { value: 'info', label: '灰色' }
]

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
    typeName: undefined,
    status: undefined,
  },
  rules: {
    typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCatWardType(queryParams.value).then(response => {
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
    typeId: undefined,
    typeName: undefined,
    tagType: 'primary',
    orderNum: 0,
    status: '0',
    remark: undefined,
  }
  proxy.resetForm("formRef")
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
  ids.value = selection.map(item => item.typeId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  // 新增时默认排在最后
  const maxOrder = list.value.reduce((max, item) => Math.max(max, item.orderNum || 0), 0)
  form.value.orderNum = maxOrder + 1
  open.value = true
  title.value = "添加病房类型"
}

function handleUpdate(row) {
  reset()
  const id = row.typeId || ids.value
  getCatWardType(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改病房类型"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.typeId != undefined) {
        updateCatWardType(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功，相关病房的类型名称已同步")
          open.value = false
          getList()
          reloadWardTypeCache()
        })
      } else {
        addCatWardType(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
          reloadWardTypeCache()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.typeId || ids.value
  proxy.$modal.confirm('是否确认删除选中的病房类型？').then(function () {
    return delCatWardType(delIds)
  }).then(() => {
    getList()
    reloadWardTypeCache()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/wardtype/export", { ...queryParams.value }, `wardtype_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.tip-alert {
  margin-bottom: 12px;
}
.color-preview {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
.zero-count {
  color: #c0c4cc;
}
</style>
