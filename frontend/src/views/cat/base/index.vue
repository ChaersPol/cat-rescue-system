<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="基地名称" prop="baseName">
        <el-input v-model="queryParams.baseName" placeholder="请输入基地名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="基地类型" prop="baseType">
        <el-select v-model="queryParams.baseType" placeholder="全部类型" clearable style="width: 160px">
          <el-option v-for="d in cat_base_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:base:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:base:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:base:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:base:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="基地名称" align="center" prop="baseName" :show-overflow-tooltip="true" />
      <el-table-column label="基地类型" align="center" prop="baseType" width="120">
        <template #default="scope">
          <dict-tag :options="cat_base_type" :value="scope.row.baseType" />
        </template>
      </el-table-column>
      <el-table-column label="负责人" align="center" prop="leader" width="110" />
      <el-table-column label="联系电话" align="center" prop="phone" width="140" />
      <el-table-column label="地址" align="center" prop="address" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:base:edit', 'cat:base:remove'])">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:base:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:base:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="baseRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="基地名称" prop="baseName">
          <el-input v-model="form.baseName" placeholder="请输入基地名称" />
        </el-form-item>
        <el-form-item label="基地类型" prop="baseType">
          <el-select v-model="form.baseType" placeholder="请选择基地类型" style="width: 100%">
            <el-option v-for="d in cat_base_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
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

<script setup name="CatBase">
import { listCatBase, getCatBase, delCatBase, addCatBase, updateCatBase } from '@/api/cat/base'

const { proxy } = getCurrentInstance()
const { cat_base_type, sys_normal_disable } = proxy.useDict('cat_base_type', 'sys_normal_disable')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    baseName: undefined,
    baseType: undefined,
    status: undefined
  },
  rules: {
    baseName: [{ required: true, message: '请输入基地名称', trigger: 'blur' }],
    baseType: [{ required: true, message: '请选择基地类型', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCatBase(queryParams.value).then(response => {
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
    baseId: undefined,
    baseName: undefined,
    baseType: '1',
    leader: undefined,
    phone: undefined,
    address: undefined,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('baseRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.baseId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加基地'
}

function handleUpdate(row) {
  reset()
  const id = row.baseId || ids.value
  getCatBase(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改基地'
  })
}

function submitForm() {
  proxy.$refs['baseRef'].validate(valid => {
    if (valid) {
      if (form.value.baseId != undefined) {
        updateCatBase(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addCatBase(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.baseId || ids.value
  proxy.$modal.confirm('是否确认删除选中的基地？').then(function () {
    return delCatBase(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('cat/base/export', { ...queryParams.value }, `基地_${new Date().getTime()}.xlsx`)
}

getList()
</script>
