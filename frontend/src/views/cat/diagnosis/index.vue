<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" />
         </el-form-item>
         <el-form-item label="治疗基地" prop="baseId">
            <el-select v-model="queryParams.baseId" placeholder="全部医疗基地" clearable filterable style="width: 200px">
               <el-option v-for="b in baseOptions" :key="b.baseId" :label="baseOptionLabel(b)" :value="b.baseId" />
            </el-select>
         </el-form-item>
         <el-form-item label="就诊日期">
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:diagnosis:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:diagnosis:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:diagnosis:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:diagnosis:export']">导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="猫咪" align="center" prop="catId" width="190" :show-overflow-tooltip="true">
            <template #default="scope">
               {{ catLabel(scope.row.catId) }}
            </template>
         </el-table-column>
         <el-table-column label="就诊日期" align="center" prop="visitDate" :show-overflow-tooltip="true" />
         <el-table-column label="医生" align="center" prop="doctor" :show-overflow-tooltip="true" />
         <el-table-column label="治疗基地" align="center" prop="baseId" width="150" :show-overflow-tooltip="true">
            <template #default="scope">
               <span v-if="scope.row.baseId">{{ baseLabel(scope.row.baseId) }}</span>
               <span v-else style="color: var(--el-text-color-placeholder)">—</span>
            </template>
         </el-table-column>
         <el-table-column label="症状" align="center" prop="symptoms" :show-overflow-tooltip="true" />
         <el-table-column label="诊断结果" align="center" prop="diagnosisResult" :show-overflow-tooltip="true" />
         <el-table-column label="更正" align="center" width="76">
            <template #default="scope">
               <el-tooltip v-if="scope.row.correctionNote" :content="scope.row.correctionNote" placement="top">
                  <el-tag size="small" type="warning" effect="plain">已更正</el-tag>
               </el-tooltip>
               <span v-else style="color: var(--el-text-color-placeholder)">—</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="180" fixed="right" class-name="small-padding fixed-width" v-if="checkPermi(['cat:diagnosis:edit', 'cat:diagnosis:remove'])">
            <template #default="scope">
               <el-button link type="warning" icon="EditPen" @click="handleCorrect(scope.row)" v-hasPermi="['cat:diagnosis:edit']">更正备注</el-button>
               <el-button v-if="isAdmin" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:diagnosis:edit']">修改</el-button>
               <el-button v-if="isAdmin" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:diagnosis:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="就诊日期" prop="visitDate">
               <el-date-picker v-model="form.visitDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择就诊日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="医生" prop="doctor">
               <el-input v-model="form.doctor" placeholder="请输入医生" />
            </el-form-item>
            <el-form-item label="治疗基地" prop="baseId">
               <el-select v-model="form.baseId" placeholder="请选择当前治疗基地（仅医疗基地）" clearable filterable style="width: 100%">
                  <el-option v-for="b in baseOptions" :key="b.baseId" :label="baseOptionLabel(b)" :value="b.baseId" />
                  <el-option v-if="legacyBase" :key="'legacy'" :label="`${legacyBase.baseName}（非医疗基地·历史记录）`"
                     :value="legacyBase.baseId" disabled />
               </el-select>
            </el-form-item>
            <el-form-item label="症状" prop="symptoms">
               <el-input v-model="form.symptoms" type="textarea" :rows="2" placeholder="主诉/症状" />
            </el-form-item>
            <el-form-item label="诊断结果" prop="diagnosisResult">
               <el-input v-model="form.diagnosisResult" type="textarea" :rows="2" placeholder="初步诊断结果" />
            </el-form-item>
            <el-form-item label="检查项目" prop="checkItems">
               <el-input v-model="form.checkItems" type="textarea" :rows="2" placeholder="体温、血常规、皮肤镜检等" />
            </el-form-item>
            <el-form-item label="医嘱/预后" prop="medicalAdvice">
               <el-input v-model="form.medicalAdvice" type="textarea" :rows="2" placeholder="请输入医嘱/预后" />
            </el-form-item>
            <el-form-item label="检查照片" prop="checkPhotos">
               <image-upload v-model="form.checkPhotos" :limit="6" :fileSize="10" />
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
      <!-- 更正备注：原始诊断不可修改，只能追加说明 -->
      <el-dialog title="追加更正备注" v-model="correctOpen" width="560px" append-to-body>
         <el-alert type="warning" :closable="false" show-icon class="mb8"
            title="为保证病史真实，原始诊断内容不会被修改；更正内容会作为补充说明追加在记录上。" />
         <el-descriptions :column="1" border size="small" class="mb8">
            <el-descriptions-item label="就诊日期">{{ correctForm.visitDate }}</el-descriptions-item>
            <el-descriptions-item label="原诊断">{{ correctForm.diagnosisResult || '—' }}</el-descriptions-item>
            <el-descriptions-item label="既往更正">{{ correctForm.correctionNote || '—' }}</el-descriptions-item>
         </el-descriptions>
         <el-input v-model="correctForm.correctionNote" type="textarea" :rows="4" placeholder="请填写更正内容，例如：诊断结果应为「口炎」，原记录笔误" />
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitCorrect">提交更正</el-button>
               <el-button @click="correctOpen = false">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="CatDiagnosis">
import { useCatCache, useBaseCache } from "@/utils/catCache"
import { listCatDiagnosis, getCatDiagnosis, delCatDiagnosis, addCatDiagnosis, updateCatDiagnosis, correctCatDiagnosis } from "@/api/cat/diagnosis"
import { useFormDraft } from "@/utils/formDraft"
import useUserStore from "@/store/modules/user"

const { proxy } = getCurrentInstance()
const { catLabel } = useCatCache()
const { baseMap, baseLabel } = useBaseCache()

// 需求文档 6.2：站内诊断记录仅限于医疗基地，因此可选基地只取 base_type=2（医疗基地），
// 饲养基地不出现在可选项里。
const baseOptions = computed(() => {
  const list = Object.values(baseMap.value || {})
  return list
    .filter(item => String(item.baseType) === '2')
    .sort((a, b) => String(a.baseName || '').localeCompare(String(b.baseName || ''), 'zh'))
})

// 历史记录里存在早期录入在饲养基地的诊断（全部基地可选的年代）。
// 这类值不能悄悄丢掉，否则编辑时下拉显示成数字/空白；这里单独造一条禁用项用于回显原名。
const legacyBase = computed(() => {
  const id = form.value.baseId
  if (!id) {
    return null
  }
  if (baseOptions.value.some(item => item.baseId === id)) {
    return null
  }
  const base = (baseMap.value || {})[id]
  return base || { baseId: id, baseName: '#' + id }
})

function baseOptionLabel(base) {
  const typeLabel = String(base.baseType) === '2' ? '医疗基地' : '饲养基地'
  return `${base.baseName}（${typeLabel}）`
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
const correctOpen = ref(false)
const correctForm = ref({})
const userStore = useUserStore()
// 病历防篡改：只有内置超级管理员可以直接改/删历史诊断，其他角色只能追加更正备注
const isAdmin = computed(() => (userStore.roles || []).includes('admin'))

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    baseId: undefined,
    visitDate: undefined,
    doctor: undefined,
    symptoms: undefined,
    diagnosisResult: undefined,
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    visitDate: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
    baseId: [{ required: true, message: '请选择当前治疗基地', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)
// 表单草稿：填到一半误关闭不丢内容（需求文档 13）
const draft = useFormDraft('cat-diagnosis-form', form)

function getList() {
  loading.value = true
  listCatDiagnosis(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
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
    catId: undefined,
    baseId: undefined,
    visitDate: undefined,
    doctor: undefined,
    symptoms: undefined,
    diagnosisResult: undefined,
    checkItems: undefined,
    medicalAdvice: undefined,
    checkPhotos: undefined,
    remark: undefined
  }
  proxy.clearFormValidate("formRef")
}

// 打开更正弹窗
function handleCorrect(row) {
  correctForm.value = { ...row, correctionNote: '' }
  correctOpen.value = true
}

function submitCorrect() {
  if (!correctForm.value.correctionNote) {
    proxy.$modal.msgWarning('请填写更正内容')
    return
  }
  correctCatDiagnosis({
    diagnosisId: correctForm.value.diagnosisId,
    correctionNote: correctForm.value.correctionNote
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '已追加更正备注')
    correctOpen.value = false
    getList()
  })
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
  ids.value = selection.map(item => item.diagnosisId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  const savedDraft = draft.restore()
  if (savedDraft) {
    form.value = { ...savedDraft }
  }
  draft.begin()
  open.value = true
  title.value = "添加站内诊断"
}

function handleUpdate(row) {
  reset()
  const id = row.diagnosisId || ids.value
  getCatDiagnosis(id).then(response => {
    form.value = response.data
    draft.begin('edit')
    open.value = true
    title.value = "修改站内诊断"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.diagnosisId != undefined) {
        updateCatDiagnosis(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          draft.pause()
          open.value = false
          getList()
        })
      } else {
        addCatDiagnosis(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          draft.clear()
          draft.pause()
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.diagnosisId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatDiagnosis(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/diagnosis/export", { ...queryParams.value }, `diagnosis_${new Date().getTime()}.xlsx`)
}

getList()
</script>
