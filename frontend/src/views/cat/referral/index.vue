<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="猫咪" prop="catId">
        <el-select v-model="queryParams.catId" placeholder="全部猫咪" clearable filterable style="width: 200px">
          <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="治疗中" value="0" />
          <el-option label="已出院" value="1" />
          <el-option label="已离世" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="医院" prop="hospitalName">
        <el-input v-model="queryParams.hospitalName" placeholder="请输入医院名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:referral:add']">送医登记</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:referral:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:referral:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="猫咪" align="center" width="220">
        <template #default="scope">{{ catName(scope.row.catId) }}</template>
      </el-table-column>
      <el-table-column label="送医时间" align="center" prop="sendDate" width="160" />
      <el-table-column label="医院" align="center" prop="hospitalName" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="送医原因" align="center" prop="sendReason" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="医院诊断" align="center" prop="hospitalDiagnosis" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处置结论/出院去向" align="center" width="200" :show-overflow-tooltip="true">
        <template #default="scope">
          <span v-if="scope.row.result">{{ scope.row.result }}</span>
          <span v-if="scope.row.dischargeTo"> → {{ scope.row.dischargeTo }}</span>
          <span v-if="!scope.row.result && !scope.row.dischargeTo">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width" v-if="checkPermi(['cat:referral:edit', 'cat:referral:remove'])">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" link type="success" icon="Finished" @click="handleDischarge(scope.row)" v-hasPermi="['cat:referral:edit']">出院登记</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:referral:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:referral:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="referralRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="猫咪" prop="catId">
          <el-select v-model="form.catId" placeholder="请选择猫咪" filterable style="width: 100%" :disabled="form.referralId != undefined">
            <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
          </el-select>
        </el-form-item>
        <el-form-item label="送医时间" prop="sendDate">
          <el-date-picker v-model="form.sendDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择送医时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="送医原因" prop="sendReason">
          <el-input v-model="form.sendReason" type="textarea" :rows="2" placeholder="站内无法处理的原因" />
        </el-form-item>
        <el-form-item label="医院名称" prop="hospitalName">
          <el-input v-model="form.hospitalName" placeholder="请输入外送医院名称" />
        </el-form-item>

        <el-divider content-position="left">医院处置（出院时填写）</el-divider>

        <el-form-item label="检查结果" prop="hospitalCheck">
          <el-input v-model="form.hospitalCheck" type="textarea" :rows="2" placeholder="医院检查结果" />
        </el-form-item>
        <el-form-item label="医院诊断" prop="hospitalDiagnosis">
          <el-input v-model="form.hospitalDiagnosis" type="textarea" :rows="2" placeholder="医院诊断结论" />
        </el-form-item>
        <el-form-item label="医院处方" prop="hospitalPrescription">
          <el-input v-model="form.hospitalPrescription" type="textarea" :rows="2" placeholder="医院处方/治疗方案" />
        </el-form-item>
        <el-form-item label="处置结论" prop="result">
          <el-select v-model="form.result" placeholder="请选择医院处置结论" clearable style="width: 100%">
            <el-option v-for="d in cat_hospital_result" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="出院去向" prop="dischargeTo">
          <el-select v-model="form.dischargeTo" placeholder="请选择出院去向" clearable style="width: 100%">
            <el-option v-for="d in cat_discharge_to" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="转入基地" prop="targetBaseId" v-if="form.dischargeTo === '转入指定基地'">
          <el-select v-model="form.targetBaseId" placeholder="请选择转入基地" clearable style="width: 100%">
            <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="出院医嘱" prop="dischargeAdvice">
          <el-input v-model="form.dischargeAdvice" type="textarea" :rows="2" placeholder="出院医嘱、康复评估" />
        </el-form-item>
        <el-form-item label="转诊状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="治疗中" value="0" />
            <el-option label="已出院" value="1" />
            <el-option label="已离世" value="2" />
          </el-select>
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

<script setup name="CatReferral">
import { listCatReferral, getCatReferral, delCatReferral, addCatReferral, updateCatReferral } from '@/api/cat/referral'
import { useFormDraft } from "@/utils/formDraft"
import { listCatProfile } from '@/api/cat/profile'
import { catLabelOf, formatCatLabel } from '@/utils/catCache'
import { listCatBase } from '@/api/cat/base'

const { proxy } = getCurrentInstance()
const { cat_discharge_to, cat_hospital_result } = proxy.useDict('cat_discharge_to', 'cat_hospital_result')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const title = ref('')
const catOptions = ref([])
const baseOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    status: undefined,
    hospitalName: undefined
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    hospitalName: [{ required: true, message: '请输入医院名称', trigger: 'blur' }],
    sendDate: [{ required: true, message: '请选择送医时间', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)
// 表单草稿：填到一半误关闭不丢内容（需求文档 13）
const draft = useFormDraft('cat-referral-form', form)

// 猫咪展示统一为「名字（档案编号）」，格式只在 utils/catCache.js 里定义
const catName = catId => catLabelOf(catOptions, catId)

function statusLabel(s) {
  return { '0': '治疗中', '1': '已出院', '2': '已离世' }[s] || '未知'
}

function statusTag(s) {
  return { '0': 'warning', '1': 'success', '2': 'danger' }[s] || 'info'
}

function loadOptions() {
  listCatProfile({ pageNum: 1, pageSize: 999 }).then(res => { catOptions.value = res.rows || [] })
  listCatBase({ pageNum: 1, pageSize: 999 }).then(res => { baseOptions.value = res.rows || [] })
}

function getList() {
  loading.value = true
  listCatReferral(queryParams.value).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  draft.pause()
  open.value = false
  reset()
}

function reset() {
  form.value = {
    referralId: undefined,
    catId: undefined,
    sendDate: proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
    sendReason: undefined,
    hospitalName: undefined,
    hospitalCheck: undefined,
    hospitalDiagnosis: undefined,
    hospitalPrescription: undefined,
    result: undefined,
    dischargeTo: undefined,
    targetBaseId: undefined,
    dischargeAdvice: undefined,
    status: '0'
  }
  proxy.resetForm('referralRef')
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
  const savedDraft = draft.restore()
  if (savedDraft) {
    form.value = { ...savedDraft }
  }
  draft.begin()
  open.value = true
  title.value = '送医登记'
}

function handleUpdate(row) {
  reset()
  getCatReferral(row.referralId).then(response => {
    form.value = response.data
    draft.begin()
    open.value = true
    title.value = '修改转诊记录'
  })
}

function handleDischarge(row) {
  reset()
  getCatReferral(row.referralId).then(response => {
    form.value = response.data
    form.value.status = '1'
    open.value = true
    title.value = '出院登记 - ' + catName(row.catId)
  })
}

function submitForm() {
  proxy.$refs['referralRef'].validate(valid => {
    if (valid) {
      if (form.value.referralId != undefined) {
        updateCatReferral(form.value).then(() => {
          proxy.$modal.msgSuccess('保存成功，猫咪状态已同步更新')
          draft.clear()
          draft.pause()
          open.value = false
          getList()
        })
      } else {
        addCatReferral(form.value).then(() => {
          proxy.$modal.msgSuccess('送医登记成功，猫咪状态已变更为外送医院治疗中')
          draft.clear()
          draft.pause()
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.referralId)
  multiple.value = !selection.length
}

function handleDelete(row) {
  const delIds = row.referralId || ids.value
  proxy.$modal.confirm('是否确认删除该转诊记录？').then(function () {
    return delCatReferral(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('cat/referral/export', { ...queryParams.value }, `referral_${new Date().getTime()}.xlsx`)
}

// 从看板「外送治疗中」等入口带过来的筛选条件，进页面即生效
const route = useRoute()
function applyRouteQuery() {
  const q = route.query || {}
  queryParams.value.status = q.status || undefined
  queryParams.value.catId = q.catId ? Number(q.catId) : undefined
  queryParams.value.pageNum = 1
}
applyRouteQuery()
watch(() => route.query, () => {
  applyRouteQuery()
  getList()
})

loadOptions()
getList()
</script>
