<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="领养人" prop="adopterName">
            <el-input v-model="queryParams.adopterName" placeholder="请输入领养人" clearable style="width: 180px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="联系电话" prop="adopterPhone">
            <el-input v-model="queryParams.adopterPhone" placeholder="请输入联系电话" clearable style="width: 180px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="猫咪" prop="catId">
            <el-select v-model="queryParams.catId" placeholder="全部猫咪" clearable filterable style="width: 200px">
               <el-option v-for="c in catOptions" :key="c.catId" :label="c.catName + '（' + c.archiveNo + '）'" :value="c.catId" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:adoption:add']">新增领养</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:adoption:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:adoption:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:adoption:export']">导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="猫咪" align="center" prop="catId" min-width="160">
            <template #default="scope">
               <span v-if="scope.row.catId">{{ catLabel(scope.row.catId) }}</span>
               <span v-else>-</span>
            </template>
         </el-table-column>
         <el-table-column label="领养人" align="center" prop="adopterName" width="110" />
         <el-table-column label="联系电话" align="center" prop="adopterPhone" width="130" />
         <el-table-column label="领养日期" align="center" prop="adoptionDate" width="120">
            <template #default="scope">{{ parseTime(scope.row.adoptionDate, '{y}-{m}-{d}') }}</template>
         </el-table-column>
         <el-table-column label="住址" align="center" prop="adopterAddress" :show-overflow-tooltip="true" min-width="180" />
         <el-table-column label="领养协议" align="center" width="150">
            <template #default="scope">
               <template v-if="scope.row.agreementUrl">
                  <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">预览</el-button>
                  <el-button link type="primary" icon="Download" @click="handleDownload(scope.row)">下载</el-button>
               </template>
               <el-tag v-else type="info" size="small">未上传</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:adoption:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:adoption:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="680px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
            <el-form-item label="领养猫咪" prop="catId">
               <el-select v-model="form.catId" placeholder="请选择猫咪" filterable style="width: 100%">
                  <el-option v-for="c in catOptions" :key="c.catId" :label="c.catName + '（' + c.archiveNo + '）'" :value="c.catId" />
               </el-select>
            </el-form-item>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="领养人" prop="adopterName">
                     <el-input v-model="form.adopterName" placeholder="请输入领养人姓名" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="联系电话" prop="adopterPhone">
                     <el-input v-model="form.adopterPhone" placeholder="请输入联系电话" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-form-item label="领养日期" prop="adoptionDate">
               <el-date-picker v-model="form.adoptionDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择领养日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="领养人住址" prop="adopterAddress">
               <el-input v-model="form.adopterAddress" placeholder="请输入领养人住址" />
            </el-form-item>
            <el-form-item label="领养协议" prop="agreementUrl">
               <file-upload
                  v-model="form.agreementUrl"
                  :limit="1"
                  :fileSize="20"
                  :fileType="['doc', 'docx', 'pdf', 'png', 'jpg', 'jpeg']"
               />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
            <el-alert
               v-if="form.adoptionId == undefined"
               title="保存后系统会自动将猫咪状态改为「已领养」并归档，同时生成 3 天 / 1 个月 / 3 个月三条回访计划"
               type="info"
               :closable="false"
               show-icon
            />
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

<script setup name="CatAdoption">
import { listCatAdoption, getCatAdoption, delCatAdoption, addCatAdoption, updateCatAdoption } from "@/api/cat/adoption"
import { useFormDraft } from "@/utils/formDraft"
import { listCatProfile } from "@/api/cat/profile"

const { proxy } = getCurrentInstance()

const list = ref([])
const catOptions = ref([])
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
    adopterName: undefined,
    adopterPhone: undefined
  },
  rules: {
    catId: [{ required: true, message: '请选择领养猫咪', trigger: 'change' }],
    adopterName: [{ required: true, message: '领养人不能为空', trigger: 'blur' }],
    adoptionDate: [{ required: true, message: '领养日期不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)
// 表单草稿：填到一半误关闭不丢内容（需求文档 13）
const draft = useFormDraft('cat-adoption-form', form)

function catLabel(catId) {
  const c = catOptions.value.find(x => x.catId === catId)
  return c ? c.catName + '（' + c.archiveNo + '）' : '#' + catId
}

function loadCats() {
  listCatProfile({ pageNum: 1, pageSize: 999 }).then(res => {
    catOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listCatAdoption(queryParams.value).then(response => {
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
    adoptionId: undefined,
    catId: undefined,
    adopterName: undefined,
    adopterPhone: undefined,
    adopterAddress: undefined,
    adoptionDate: undefined,
    agreementUrl: undefined,
    remark: undefined
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
  ids.value = selection.map(item => item.adoptionId)
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
  title.value = "新增领养登记"
}

function handleUpdate(row) {
  reset()
  const id = row.adoptionId || ids.value
  getCatAdoption(id).then(response => {
    form.value = response.data
    draft.begin()
    open.value = true
    title.value = "修改领养登记"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.adoptionId != undefined) {
        updateCatAdoption(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          draft.clear()
          draft.pause()
          open.value = false
          getList()
        })
      } else {
        addCatAdoption(form.value).then(res => {
          proxy.$modal.msgSuccess(res.msg || "新增成功，已自动生成回访计划")
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
  const delIds = row.adoptionId || ids.value
  proxy.$modal.confirm('是否确认删除该领养记录？').then(function () {
    return delCatAdoption(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/adoption/export", { ...queryParams.value }, `领养台账_${new Date().getTime()}.xlsx`)
}

// 协议预览：图片/PDF 由浏览器内置查看器打开
function handlePreview(row) {
  window.open(proxy.getFileUrl(row.agreementUrl), '_blank')
}

function handleDownload(row) {
  const name = (row.agreementUrl || '').split('/').pop()
  proxy.download('common/download/resource', { resource: '/profile/' + name.replace(/^profile\//, '') }, name)
}

loadCats()
getList()
</script>
