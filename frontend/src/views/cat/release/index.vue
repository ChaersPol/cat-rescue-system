<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="猫咪" prop="catId">
            <cat-select v-model="queryParams.catId" placeholder="全部猫咪" />
         </el-form-item>
         <el-form-item label="标记状态" prop="marked">
            <el-select v-model="queryParams.marked" placeholder="全部" clearable style="width: 120px">
               <el-option label="已放归" value="1" />
            </el-select>
         </el-form-item>
         <el-form-item label="放归日期">
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:release:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:release:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:release:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:release:export']">导出</el-button>
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
         <el-table-column label="放归日期" align="center" prop="releaseDate" :show-overflow-tooltip="true" />
         <el-table-column label="放归点位" align="center" prop="releasePoint" :show-overflow-tooltip="true" />
         <el-table-column label="标记状态" align="center" prop="marked" width="100">
            <template #default="scope">
               <el-tag v-if="scope.row.marked === '1'" type="success">已放归</el-tag>
               <span v-else class="text-muted">未标记</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:release:edit', 'cat:release:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:release:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:release:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="猫咪" prop="catId">
               <cat-select v-model="form.catId" placeholder="请选择猫咪" />
            </el-form-item>
            <el-form-item label="放归日期" prop="releaseDate">
               <el-date-picker v-model="form.releaseDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择放归日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="放归点位" prop="releasePoint">
               <el-input v-model="form.releasePoint" placeholder="请输入放归点位" />
            </el-form-item>
            <el-form-item label="放归时状态" prop="releaseStatus">
               <el-input v-model="form.releaseStatus" placeholder="如：精神良好、体重 3.2kg" />
            </el-form-item>
            <el-form-item label="标记状态" prop="marked">
               <el-select v-model="form.marked" placeholder="请选择标记状态" style="width: 100%">
                  <el-option label="已放归" value="1" />
               </el-select>
               <div class="form-tip">放归登记后猫咪状态自动变更为<b>「已放归」</b>并归档</div>
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

<script setup name="CatRelease">
import { useCatCache } from "@/utils/catCache"
import { listCatRelease, getCatRelease, delCatRelease, addCatRelease, updateCatRelease } from "@/api/cat/release"
import { useFormDraft } from "@/utils/formDraft"

const { proxy } = getCurrentInstance()
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

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    marked: undefined,
    releaseDate: undefined,
    releasePoint: undefined,
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    releaseDate: [{ required: true, message: '请选择放归日期', trigger: 'change' }],
    marked: [{ required: true, message: '请选择标记状态', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)
// 表单草稿：填到一半误关闭不丢内容（需求文档 13）
const draft = useFormDraft('cat-release-form', form)

function getList() {
  loading.value = true
  listCatRelease(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
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
    releaseDate: undefined,
    releasePoint: undefined,
    releaseStatus: undefined,
    marked: '1',
    remark: undefined,
  }
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
  ids.value = selection.map(item => item.releaseId)
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
  title.value = "添加TNR放归"
}

function handleUpdate(row) {
  reset()
  const id = row.releaseId || ids.value
  getCatRelease(id).then(response => {
    form.value = response.data
    draft.begin('edit')
    open.value = true
    title.value = "修改TNR放归"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.releaseId != undefined) {
        updateCatRelease(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          draft.pause()
          open.value = false
          getList()
        })
      } else {
        addCatRelease(form.value).then(response => {
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
  const delIds = row.releaseId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatRelease(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/release/export", { ...queryParams.value }, `release_${new Date().getTime()}.xlsx`)
}

getList()
</script>
