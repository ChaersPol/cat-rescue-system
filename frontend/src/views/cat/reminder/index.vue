<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="提醒类型" prop="reminderType">
        <el-select v-model="queryParams.reminderType" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="未读" value="0" />
          <el-option label="已读" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="关联猫咪" prop="catId">
        <cat-select v-model="queryParams.catId" placeholder="全部猫咪" style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Refresh" @click="handleGenerate" v-hasPermi="['cat:reminder:list']">生成到期提醒</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Check" :disabled="multiple" @click="handleMarkReadBatch" v-hasPermi="['cat:reminder:edit']">标记已读</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:reminder:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:reminder:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange" :row-class-name="rowClassName">
      <el-table-column type="selection" width="45" align="center" />
      <el-table-column label="类型" align="center" prop="reminderType" width="90">
        <template #default="scope">
          <el-tag :type="typeTag(scope.row.reminderType)" size="small">{{ scope.row.reminderType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="内容" align="center" prop="content" min-width="240" :show-overflow-tooltip="true" />
      <el-table-column label="关联猫咪" align="center" prop="catId" width="110">
        <template #default="scope">
          <span v-if="scope.row.catId">{{ catName(scope.row.catId) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="提醒日期" align="center" prop="remindDate" width="110">
        <template #default="scope">
          <span :class="{ 'overdue': isOverdue(scope.row) }">{{ parseTime(scope.row.remindDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="85">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="danger" size="small">未读</el-tag>
          <el-tag v-else type="success" size="small">已读</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="目标用户" align="center" prop="targetUser" width="130" :show-overflow-tooltip="true">
        <template #default="scope">
          <span v-if="scope.row.targetUser">{{ scope.row.targetUser }}</span>
          <el-tag v-else size="small" type="info" effect="plain">全员</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" link type="success" icon="Check" @click="handleMarkRead(scope.row)" v-hasPermi="['cat:reminder:edit']">已读</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:reminder:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:reminder:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="reminderRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="提醒类型" prop="reminderType">
          <el-select v-model="form.reminderType" placeholder="请选择提醒类型" style="width: 100%">
            <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联猫咪" prop="catId">
          <el-select v-model="form.catId" placeholder="可不选（库存类提醒）" clearable filterable style="width: 100%">
            <el-option v-for="c in catOptions" :key="c.catId" :label="c.catName + '（' + c.archiveNo + '）'" :value="c.catId" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入提醒内容" />
        </el-form-item>
        <el-form-item label="提醒日期" prop="remindDate">
          <el-date-picker v-model="form.remindDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="目标用户" prop="targetUser">
          <el-input v-model="form.targetUser" placeholder="留空表示全员可见；多个账号用英文逗号分隔" />
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

<script setup name="CatReminder">
import {
  listCatReminder,
  getCatReminder,
  delCatReminder,
  addCatReminder,
  updateCatReminder,
  generateCatReminder,
  markReadCatReminder,
  markReadBatchCatReminder
} from '@/api/cat/reminder'
import { listCatProfile } from '@/api/cat/profile'

const { proxy } = getCurrentInstance()

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const catOptions = ref([])
const catMap = ref({})

const typeOptions = ['疫苗', '驱虫', '回访', '库存', '转诊', '其他']

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    reminderType: undefined,
    status: undefined,
    catId: undefined
  },
  rules: {
    reminderType: [{ required: true, message: '提醒类型不能为空', trigger: 'change' }],
    title: [{ required: true, message: '标题不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function catName(catId) {
  return catMap.value[catId] || '#' + catId
}

function isOverdue(row) {
  if (!row.remindDate || row.status !== '0') return false
  return new Date(row.remindDate).getTime() < Date.now() - 86400000
}

function rowClassName({ row }) {
  return row.status === '0' ? 'unread-row' : ''
}

function typeTag(type) {
  const map = { 疫苗: 'success', 驱虫: 'warning', 回访: 'primary', 库存: 'danger', 转诊: 'info' }
  return map[type] || 'info'
}

function loadCats() {
  listCatProfile({ pageNum: 1, pageSize: 999 }).then(res => {
    catOptions.value = res.rows || []
    const map = {}
    catOptions.value.forEach(c => (map[c.catId] = c.catName))
    catMap.value = map
  })
}

function getList() {
  loading.value = true
  listCatReminder(queryParams.value).then(response => {
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
    reminderId: undefined,
    catId: undefined,
    reminderType: '疫苗',
    title: undefined,
    content: undefined,
    remindDate: undefined,
    targetUser: undefined
  }
  proxy.resetForm('reminderRef')
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
  ids.value = selection.map(item => item.reminderId)
  multiple.value = !selection.length
}

function handleGenerate() {
  generateCatReminder().then(res => {
    proxy.$modal.msgSuccess(res.msg || '生成完成')
    getList()
  })
}

function handleMarkRead(row) {
  markReadCatReminder(row.reminderId).then(() => {
    proxy.$modal.msgSuccess('已标记为已读')
    getList()
  })
}

function handleMarkReadBatch() {
  const unread = ids.value
  if (!unread.length) {
    proxy.$modal.msgWarning('请先选择提醒')
    return
  }
  markReadBatchCatReminder(unread).then(() => {
    proxy.$modal.msgSuccess('批量标记成功')
    getList()
  })
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加提醒通知'
}

function handleUpdate(row) {
  reset()
  const id = row.reminderId || ids.value
  getCatReminder(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改提醒通知'
  })
}

function submitForm() {
  proxy.$refs['reminderRef'].validate(valid => {
    if (valid) {
      if (form.value.reminderId != undefined) {
        updateCatReminder(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addCatReminder(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.reminderId || ids.value
  proxy.$modal.confirm('是否确认删除该提醒？').then(function () {
    return delCatReminder(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

loadCats()
getList()
</script>

<style scoped>
.overdue {
  color: #f56c6c;
  font-weight: 600;
}
:deep(.unread-row) {
  background-color: var(--el-color-danger-light-9);
}
</style>
