<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" />
         </el-form-item>
         <el-form-item label="用药日期">
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:medication:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:medication:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:medication:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:medication:export']">导出</el-button>
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
         <el-table-column label="用药日期" align="center" prop="useDate" :show-overflow-tooltip="true" />
         <el-table-column label="时间段" align="center" prop="timePeriod">
            <template #default="scope">
               <dict-tag :options="cat_time_period" :value="scope.row.timePeriod" />
            </template>
         </el-table-column>
         <el-table-column label="药品名称" align="center" prop="drugName" :show-overflow-tooltip="true" />
         <el-table-column label="剂量" align="center" prop="dosage" :show-overflow-tooltip="true" />
         <el-table-column label="给药方式" align="center" prop="administration" width="100">
            <template #default="scope">
               <dict-tag :options="cat_administration" :value="scope.row.administration" />
            </template>
         </el-table-column>
         <el-table-column label="执行人" align="center" prop="executor" width="100" />
         <el-table-column label="不良反应" align="center" prop="adverseReaction" :show-overflow-tooltip="true" min-width="140" />
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:medication:edit', 'cat:medication:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:medication:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:medication:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="用药日期" prop="useDate">
               <el-row :gutter="10" style="width: 100%">
                  <el-col :span="14">
                     <el-date-picker v-model="form.useDate" type="date" value-format="YYYY-MM-DD" placeholder="开始用药日期" style="width: 100%" />
                  </el-col>
                  <el-col :span="10">
                     <el-input-number v-model="batchDays" :min="1" :max="60" :disabled="form.medicationId != undefined" controls-position="right" style="width: 100%" />
                  </el-col>
               </el-row>
               <div class="form-tip">第二个数字是「连续天数」：填 7 表示从开始日期起连续录入 7 天，同一天同一种药不会重复生成。</div>
            </el-form-item>
            <el-form-item label="时间段" prop="timePeriod">
               <el-select v-model="form.timePeriod" placeholder="请选择时间段" style="width: 100%">
                  <el-option v-for="d in cat_time_period" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="药品名称" prop="drugName">
               <el-input v-model="form.drugName" placeholder="请输入药品名称" />
            </el-form-item>
            <el-form-item label="剂量" prop="dosage">
               <el-input v-model="form.dosage" placeholder="请输入剂量" />
            </el-form-item>
            <el-form-item label="给药方式" prop="administration">
               <el-select v-model="form.administration" placeholder="请选择给药方式" style="width: 100%">
                  <el-option v-for="d in cat_administration" :key="d.value" :label="d.label" :value="d.value" />
               </el-select>
            </el-form-item>
            <el-form-item label="执行人" prop="executor">
               <el-input v-model="form.executor" placeholder="请输入用药执行人" />
            </el-form-item>
            <el-form-item label="不良反应" prop="adverseReaction">
               <el-input v-model="form.adverseReaction" type="textarea" :rows="2" placeholder="记录不良反应，无则留空" />
            </el-form-item>
            <el-form-item label="用药照片" prop="photos">
               <image-upload v-model="form.photos" :limit="5" :fileSize="10" />
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

<script setup name="CatMedication">
import { useCatCache } from "@/utils/catCache"
import { listCatMedication, getCatMedication, delCatMedication, addCatMedication, updateCatMedication, addCatMedicationBatch } from "@/api/cat/medication"
import { useFormDraft } from "@/utils/formDraft"

const { proxy } = getCurrentInstance()
const { cat_time_period, cat_administration } = proxy.useDict('cat_time_period', 'cat_administration')
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
const dateRange = ref([])
// 连续录入天数：>1 时走批量接口
const batchDays = ref(1)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    useDate: undefined,
    timePeriod: undefined,
    drugName: undefined,
    dosage: undefined,
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    useDate: [{ required: true, message: '请选择用药日期', trigger: 'change' }],
    drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)
// 表单草稿：填到一半误关闭不丢内容（需求文档 13）
const draft = useFormDraft('cat-medication-form', form)

function getList() {
  loading.value = true
  listCatMedication(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
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
    useDate: undefined,
    timePeriod: undefined,
    drugName: undefined,
    dosage: undefined,
    administration: undefined,
    executor: undefined,
    adverseReaction: undefined,
    photos: undefined,
    remark: undefined
  }
  batchDays.value = 1
  proxy.clearFormValidate("formRef")
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
  ids.value = selection.map(item => item.medicationId)
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
  title.value = "添加用药记录"
}

function handleUpdate(row) {
  reset()
  const id = row.medicationId || ids.value
  getCatMedication(id).then(response => {
    form.value = response.data
    draft.begin('edit')
    open.value = true
    title.value = "修改用药记录"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.medicationId != undefined) {
        updateCatMedication(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          draft.pause()
          open.value = false
          getList()
        })
      } else if (batchDays.value > 1) {
        // 批量录入同一只猫连续多天用药
        addCatMedicationBatch(form.value, batchDays.value).then(res => {
          proxy.$modal.msgSuccess(res.msg || "批量录入成功")
          draft.clear()
          draft.pause()
          open.value = false
          getList()
        })
      } else {
        addCatMedication(form.value).then(response => {
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
  const delIds = row.medicationId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatMedication(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/medication/export", { ...queryParams.value }, `medication_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  line-height: 1.6;
  margin-top: 4px;
}
</style>
