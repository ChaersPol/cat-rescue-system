<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="猫咪" prop="catId">
        <el-select v-model="queryParams.catId" placeholder="全部猫咪" clearable filterable style="width: 200px">
          <el-option v-for="c in catOptions" :key="c.catId" :label="c.catName + '（' + c.archiveNo + '）'" :value="c.catId" />
        </el-select>
      </el-form-item>
      <el-form-item label="流转类型" prop="flowType">
        <el-select v-model="queryParams.flowType" placeholder="全部类型" clearable style="width: 160px">
          <el-option v-for="t in flowTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:flow:add']">发起流转</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:flow:export']">导出</el-button>
      </el-col>
      <el-col :span="12">
        <span class="tip">流转记录为不可删除的业务凭证，发起流转会自动更新猫咪状态与所在基地/病房。</span>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="猫咪" align="center" prop="catId" width="170">
        <template #default="scope">
          <span>{{ catName(scope.row.catId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="流转类型" align="center" prop="flowType" width="130">
        <template #default="scope">
          <el-tag :type="typeTag(scope.row.flowType)" size="small">{{ scope.row.flowType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="转出 → 转入" align="center" min-width="260">
        <template #default="scope">
          <div class="flow-place">
            <div class="flow-line">
              <span class="flow-tag out">转出</span>
              <span class="flow-text" :title="placeLabel(scope.row.fromBaseId, scope.row.fromWardId)">
                {{ placeLabel(scope.row.fromBaseId, scope.row.fromWardId) || '—' }}
              </span>
            </div>
            <div class="flow-line">
              <span class="flow-tag in">转入</span>
              <span class="flow-text" :title="placeLabel(scope.row.toBaseId, scope.row.toWardId)">
                {{ placeLabel(scope.row.toBaseId, scope.row.toWardId) || '—' }}
              </span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="流转原因" align="center" prop="flowReason" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="医院/结论" align="center" width="180" :show-overflow-tooltip="true">
        <template #default="scope">
          <span v-if="scope.row.hospitalName">{{ scope.row.hospitalName }}</span>
          <span v-if="scope.row.hospitalResult"> / {{ scope.row.hospitalResult }}</span>
          <span v-if="!scope.row.hospitalName && !scope.row.hospitalResult">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operateBy" width="110" />
      <el-table-column label="操作时间" align="center" prop="operateTime" width="160" />
      <el-table-column label="操作" align="center" width="110" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:flow:edit']">补充备注</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="flowRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="猫咪" prop="catId">
          <el-select v-model="form.catId" placeholder="请选择猫咪" filterable style="width: 100%" @change="onCatChange">
            <el-option v-for="c in catOptions" :key="c.catId" :label="c.catName + '（' + c.archiveNo + '）'" :value="c.catId" />
          </el-select>
        </el-form-item>
        <el-form-item label="流转类型" prop="flowType">
          <el-select v-model="form.flowType" placeholder="请选择流转类型" style="width: 100%">
            <el-option v-for="t in flowTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入基地" prop="toBaseId">
          <el-select v-model="form.toBaseId" placeholder="请选择转入基地" clearable style="width: 100%">
            <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入病房" prop="toWardId">
          <el-select v-model="form.toWardId" placeholder="请选择转入病房" clearable style="width: 100%">
            <el-option
              v-for="w in wardOptions"
              :key="w.wardId"
              :label="w.wardName"
              :value="w.wardId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医院名称" prop="hospitalName" v-if="form.flowType === '外送医院' || form.flowType === '医院返回'">
          <el-input v-model="form.hospitalName" placeholder="请输入外送医院名称" />
        </el-form-item>
        <el-form-item label="医院处置结论" prop="hospitalResult" v-if="form.flowType === '医院返回'">
          <el-select v-model="form.hospitalResult" placeholder="请选择医院处置结论" clearable style="width: 100%">
            <el-option label="治愈出院" value="治愈出院" />
            <el-option label="预后不良" value="预后不良" />
            <el-option label="安乐" value="安乐" />
            <el-option label="离世" value="离世" />
          </el-select>
        </el-form-item>
        <el-form-item label="流转原因" prop="flowReason">
          <el-input v-model="form.flowReason" type="textarea" :rows="2" placeholder="请输入流转原因" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" :loading="submitting">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CatFlow">
import { listCatFlow, getCatFlow, addCatFlow, updateCatFlow } from '@/api/cat/flow'
import { listCatProfile } from '@/api/cat/profile'
import { listCatBase } from '@/api/cat/base'
import { listCatWard } from '@/api/cat/ward'

const { proxy } = getCurrentInstance()

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const submitting = ref(false)
const catOptions = ref([])
const baseOptions = ref([])
const wardOptions = ref([])
const allWards = ref([])

const flowTypes = ['入站', '发病转入治疗', '治愈返回', '外送医院', '医院返回', '转入姑息', '领养', '放归', '死亡']

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    flowType: undefined
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    flowType: [{ required: true, message: '请选择流转类型', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function catName(catId) {
  const c = catOptions.value.find(i => i.catId === catId)
  return c ? c.catName : (catId ? '#' + catId : '-')
}

function baseName(baseId) {
  const b = baseOptions.value.find(i => i.baseId === baseId)
  return b ? b.baseName : ''
}

function placeLabel(baseId, wardId) {
  const b = baseName(baseId)
  const w = allWards.value.find(i => i.wardId === wardId)
  if (!b && !w) return '—'
  if (b && w) return b + ' / ' + w.wardName
  return b || (w ? w.wardName : '—')
}

function typeTag(type) {
  if (!type) return 'info'
  if (type.includes('死亡')) return 'danger'
  if (type.includes('领养')) return 'success'
  if (type.includes('放归')) return 'primary'
  if (type.includes('外送')) return 'warning'
  return 'info'
}

function loadOptions() {
  listCatProfile({ pageNum: 1, pageSize: 999 }).then(res => {
    catOptions.value = res.rows || []
  })
  listCatBase({ pageNum: 1, pageSize: 999 }).then(res => {
    baseOptions.value = res.rows || []
  })
  listCatWard({ pageNum: 1, pageSize: 999 }).then(res => {
    allWards.value = res.rows || []
  })
}

function onCatChange(catId) {
  const c = catOptions.value.find(i => i.catId === catId)
  if (c && c.baseId) {
    form.value.toBaseId = c.baseId
    onBaseChange(c.baseId)
  }
}

function onBaseChange(baseId) {
  wardOptions.value = allWards.value.filter(w => w.baseId === baseId)
  if (form.value.toWardId && !wardOptions.value.some(w => w.wardId === form.value.toWardId)) {
    form.value.toWardId = undefined
  }
}

function getList() {
  loading.value = true
  listCatFlow(queryParams.value).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    flowId: undefined,
    catId: undefined,
    fromBaseId: undefined,
    fromWardId: undefined,
    toBaseId: undefined,
    toWardId: undefined,
    flowType: '发病转入治疗',
    flowReason: undefined,
    hospitalName: undefined,
    hospitalResult: undefined,
    remark: undefined
  }
  wardOptions.value = []
  proxy.resetForm('flowRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '发起流转'
}

function handleUpdate(row) {
  reset()
  getCatFlow(row.flowId).then(response => {
    form.value = response.data
    if (form.value.toBaseId) {
      onBaseChange(form.value.toBaseId)
    }
    open.value = true
    title.value = '补充流转备注'
  })
}

function submitForm() {
  proxy.$refs['flowRef'].validate(valid => {
    if (valid) {
      submitting.value = true
      const isEdit = form.value.flowId != undefined
      const action = isEdit ? updateCatFlow(form.value) : addCatFlow(form.value)
      action.then(() => {
        proxy.$modal.msgSuccess(isEdit ? '修改成功' : '流转成功，猫咪状态已同步更新')
        open.value = false
        getList()
      }).finally(() => {
        submitting.value = false
      })
    }
  })
}

function handleExport() {
  proxy.download('cat/flow/export', { ...queryParams.value }, `flow_${new Date().getTime()}.xlsx`)
}

watch(() => form.value.toBaseId, (val) => {
  if (open.value && val !== undefined) {
    onBaseChange(val)
  }
})

loadOptions()
getList()
</script>

<style scoped>
.flow-place {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}
.flow-line {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.flow-tag {
  flex: none;
  width: 32px;
  padding: 0 4px;
  border-radius: 3px;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
}
.flow-tag.out {
  color: var(--el-color-warning-dark-2);
  background: var(--el-color-warning-light-9);
}
.flow-tag.in {
  color: var(--el-color-success-dark-2);
  background: var(--el-color-success-light-9);
}
.flow-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.arrow {
  margin: 0 6px;
  color: var(--el-text-color-placeholder);
}
.tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 32px;
}
</style>
