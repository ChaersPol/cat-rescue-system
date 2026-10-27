<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="关键词" prop="catName">
            <el-input v-model="queryParams.catName" placeholder="名字/编号/特征" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
               <el-option v-for="dict in cat_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
         </el-form-item>
         <el-form-item label="基地" prop="baseId">
            <el-select v-model="queryParams.baseId" placeholder="请选择基地" clearable style="width: 160px">
               <el-option v-for="base in baseList" :key="base.baseId" :label="base.baseName" :value="base.baseId" />
            </el-select>
         </el-form-item>
         <el-form-item label="标签" prop="tagIds">
            <el-select v-model="queryParams.tagIds" multiple collapse-tags collapse-tags-tooltip placeholder="按标签筛选" clearable style="width: 200px">
               <el-option v-for="tag in tagList" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId" />
            </el-select>
         </el-form-item>
         <el-form-item label="归档" prop="archived">
            <el-select v-model="queryParams.archived" placeholder="全部" clearable style="width: 130px">
               <el-option label="未归档" value="0" />
               <el-option label="已归档" value="1" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:profile:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:profile:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:profile:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:profile:export']">导出</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="info" plain icon="Grid" @click="handleGenQrBatch" v-hasPermi="['cat:profile:edit']">批量更新二维码</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button plain icon="Printer" @click="handlePrintCards">二维码卡片打印</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div ref="tableWrap">
      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="45" align="center" />
         <el-table-column label="档案编号" align="center" prop="archiveNo" width="160" :show-overflow-tooltip="true" />
         <el-table-column label="猫咪名字" align="center" prop="catName" width="90" :show-overflow-tooltip="true">
            <template #default="scope">
               {{ scope.row.catName }}
               <el-tag v-if="scope.row.archived === '1'" size="small" type="warning" effect="plain" style="margin-left: 4px">归档</el-tag>
            </template>
         </el-table-column>
         <el-table-column v-if="wideTable" label="品种" align="center" prop="breed" width="85" :show-overflow-tooltip="true" />
         <el-table-column v-if="wideTable" label="性别" align="center" prop="sex" width="60">
            <template #default="scope">
               <dict-tag :options="cat_sex" :value="scope.row.sex" />
            </template>
         </el-table-column>
         <el-table-column label="年龄" align="center" prop="ageEstimate" width="90" :show-overflow-tooltip="true" />
         <el-table-column label="状态" align="center" prop="status" width="90" :show-overflow-tooltip="true">
            <template #default="scope">
               <dict-tag :options="cat_status" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column v-if="wideTable" label="所在基地" align="center" prop="baseId" min-width="150" :show-overflow-tooltip="true">
            <template #default="scope">{{ baseName(scope.row.baseId) }}</template>
         </el-table-column>
         <el-table-column v-if="wideTable" label="所在病房" align="center" prop="wardId" min-width="130" :show-overflow-tooltip="true">
            <template #default="scope">{{ wardName(scope.row.wardId) || '—' }}</template>
         </el-table-column>
         <el-table-column v-else label="所在位置" align="center" min-width="150" :show-overflow-tooltip="true">
            <template #default="scope">{{ locationText(scope.row) }}</template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="300" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" @click="handleView(scope.row)">档案</el-button>
               <el-button link type="success" @click="handleExportPdf(scope.row)" v-hasPermi="['cat:profile:query']">PDF</el-button>
               <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['cat:profile:edit']">修改</el-button>
               <el-button v-if="scope.row.archived !== '1'" link type="warning" @click="handleArchive(scope.row)" v-hasPermi="['cat:profile:edit']">归档</el-button>
               <el-button v-else link type="info" @click="handleUnarchive(scope.row)" v-hasPermi="['cat:profile:edit']">取消归档</el-button>
               <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['cat:profile:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <!-- 新增/修改对话框 -->
      <el-dialog :title="title" v-model="open" width="700px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-row>
               <el-col :span="12">
                  <el-form-item label="猫咪名字" prop="catName">
                     <el-input v-model="form.catName" placeholder="请输入猫咪名字" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="别名" prop="alias">
                     <el-input v-model="form.alias" placeholder="请输入别名" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="品种" prop="breed">
                     <el-input v-model="form.breed" placeholder="请输入品种" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="性别" prop="sex">
                     <el-select v-model="form.sex" placeholder="请选择性别" clearable style="width: 100%">
                        <el-option v-for="dict in cat_sex" :key="dict.value" :label="dict.label" :value="dict.value" />
                     </el-select>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="年龄" prop="ageEstimate">
                     <el-input v-model="form.ageEstimate" placeholder="如：2岁" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="体重" prop="weight">
                     <el-input v-model="form.weight" placeholder="如：3.5kg" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="来源" prop="source">
                     <el-select v-model="form.source" placeholder="请选择来源" clearable style="width: 100%">
                        <el-option v-for="dict in cat_source" :key="dict.value" :label="dict.label" :value="dict.value" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                     <el-select v-model="form.status" placeholder="请选择状态" clearable style="width: 100%">
                        <el-option v-for="dict in cat_status" :key="dict.value" :label="dict.label" :value="dict.value" />
                     </el-select>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="8">
                  <el-form-item label="所在基地" prop="baseId">
                     <el-select v-model="form.baseId" placeholder="请选择基地" clearable style="width: 100%" @change="onBaseChange">
                        <el-option v-for="base in baseList" :key="base.baseId" :label="base.baseName" :value="base.baseId" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="8">
                  <el-form-item label="所在病房" prop="wardId">
                     <el-select v-model="form.wardId" placeholder="请选择病房" clearable style="width: 100%">
                        <el-option v-for="ward in formWardList" :key="ward.wardId" :label="ward.wardName" :value="ward.wardId" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="8">
                  <el-form-item label="抓捕日期" prop="captureDate">
                     <el-date-picker v-model="form.captureDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="8">
                  <el-form-item label="是否绝育" prop="neutered">
                     <el-select v-model="form.neutered" placeholder="请选择" style="width: 100%">
                        <el-option v-for="dict in cat_neutered" :key="dict.value" :label="dict.label" :value="dict.value" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="8">
                  <el-form-item label="是否驱虫" prop="dewormed">
                     <el-select v-model="form.dewormed" placeholder="请选择" style="width: 100%">
                        <el-option v-for="dict in cat_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="8">
                  <el-form-item label="是否疫苗" prop="vaccinated">
                     <el-select v-model="form.vaccinated" placeholder="请选择" style="width: 100%">
                        <el-option v-for="dict in cat_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
                     </el-select>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-form-item label="抓捕点位" prop="capturePoint">
               <el-input v-model="form.capturePoint" placeholder="请输入抓捕点位/投喂点地址" />
            </el-form-item>
            <el-form-item label="标志特征" prop="feature">
               <el-input v-model="form.feature" type="textarea" placeholder="伤疤、缺耳、断尾、异色眼等" />
            </el-form-item>
            <el-form-item label="标签" prop="tagIds">
               <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%">
                  <el-option v-for="tag in tagList" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId" />
               </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="性格、应激程度、是否怕人、特殊习性" />
            </el-form-item>
            <!-- 二维码展示 -->
            <el-form-item label="二维码">
               <div class="qr-box">
                  <img v-if="form.qrCode" :key="qrTick" :src="form.qrCode" class="qr-img" />
                  <div v-else class="qr-empty">尚未生成二维码</div>
                  <div class="qr-meta">
                     <div v-if="form.scanUrl" class="qr-url">手机扫这个码，打开这只猫的档案：{{ form.scanUrl }}</div>
                     <div class="qr-help">换了系统访问网址才需要点「更新二维码」，平时不用点</div>
                     <el-button size="small" type="primary" plain icon="Refresh" @click="handleGenQr(form.catId)" v-hasPermi="['cat:profile:edit']">更新二维码</el-button>
                  </div>
               </div>
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 查看详情对话框 -->
      <el-dialog title="猫咪档案详情" v-model="viewOpen" width="600px" append-to-body>
         <el-descriptions :column="2" border>
            <el-descriptions-item label="档案编号">{{ viewForm.archiveNo }}</el-descriptions-item>
            <el-descriptions-item label="猫咪名字">{{ viewForm.catName }}</el-descriptions-item>
            <el-descriptions-item label="别名">{{ viewForm.alias }}</el-descriptions-item>
            <el-descriptions-item label="品种">{{ viewForm.breed }}</el-descriptions-item>
            <el-descriptions-item label="性别">
               <dict-tag :options="cat_sex" :value="viewForm.sex" />
            </el-descriptions-item>
            <el-descriptions-item label="年龄">{{ viewForm.ageEstimate }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ viewForm.weight }}</el-descriptions-item>
            <el-descriptions-item label="来源">
               <dict-tag :options="cat_source" :value="viewForm.source" />
            </el-descriptions-item>
            <el-descriptions-item label="状态">
               <dict-tag :options="cat_status" :value="viewForm.status" />
            </el-descriptions-item>
            <el-descriptions-item label="所在基地">{{ baseName(viewForm.baseId) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="所在病房">{{ wardName(viewForm.wardId) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="是否绝育">
               <dict-tag :options="cat_neutered" :value="viewForm.neutered" />
            </el-descriptions-item>
            <el-descriptions-item label="是否驱虫">
               <dict-tag :options="cat_yes_no" :value="viewForm.dewormed" />
            </el-descriptions-item>
            <el-descriptions-item label="是否疫苗">
               <dict-tag :options="cat_yes_no" :value="viewForm.vaccinated" />
            </el-descriptions-item>
            <el-descriptions-item label="抓捕日期">{{ viewForm.captureDate }}</el-descriptions-item>
            <el-descriptions-item label="抓捕点位" :span="2">{{ viewForm.capturePoint }}</el-descriptions-item>
            <el-descriptions-item label="标志特征" :span="2">{{ viewForm.feature }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ viewForm.remark }}</el-descriptions-item>
         </el-descriptions>
         <div style="text-align: center; margin-top: 15px;">
            <img v-if="viewForm.qrCode" :key="qrTick" :src="viewForm.qrCode" style="width: 150px; height: 150px;" />
            <div v-else style="color: #999; font-size: 12px; margin-bottom: 8px;">尚未生成二维码</div>
            <div v-if="viewForm.scanUrl" class="qr-url">手机扫这个码，打开这只猫的档案：{{ viewForm.scanUrl }}</div>
            <el-button size="small" type="primary" plain icon="Refresh" @click="handleGenQr(viewForm.catId)" v-hasPermi="['cat:profile:edit']">更新二维码</el-button>
            <div style="color: #999; font-size: 12px; margin-top: 5px;">换了系统访问网址才需要更新，平时不用点</div>
         </div>
      </el-dialog>
   </div>
</template>

<script setup name="CatProfile">
import { listCatProfile, getCatProfile, delCatProfile, addCatProfile, updateCatProfile, regenerateCatQrCode, regenerateAllCatQrCode, archiveCatProfile, unarchiveCatProfile } from "@/api/cat/profile"
import { listCatBase } from "@/api/cat/base"
import { listCatTag } from "@/api/cat/tag"
import { listCatWard } from "@/api/cat/ward"
import { useFormDraft } from "@/utils/formDraft"

const { proxy } = getCurrentInstance()
const { cat_status, cat_sex, cat_source, cat_neutered, cat_yes_no } = useDict("cat_status", "cat_sex", "cat_source", "cat_neutered", "cat_yes_no")

const list = ref([])
const baseList = ref([])
const wardList = ref([])
const tagList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
// 二维码刷新计数：作为 <img> 的 key，保证重新生成后一定重新渲染（内容相同的场景也能确认已刷新）
const qrTick = ref(0)

// 表格可用宽度：窄窗口下自动收起「品种 / 性别」两列，避免列被裁切或出现横向滚动
const tableWrap = ref(null)
const tableWidth = ref(1200)
const wideTable = computed(() => tableWidth.value >= 1050)
let tableObserver = null

onMounted(() => {
  if (!tableWrap.value) return
  tableWidth.value = tableWrap.value.clientWidth
  if (typeof ResizeObserver !== "undefined") {
    tableObserver = new ResizeObserver(entries => {
      const w = entries[0]?.contentRect?.width
      if (w) tableWidth.value = w
    })
    tableObserver.observe(tableWrap.value)
  }
})

onBeforeUnmount(() => {
  if (tableObserver) {
    tableObserver.disconnect()
    tableObserver = null
  }
})

const data = reactive({
  form: {},
  viewForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catName: undefined,
    status: undefined,
    baseId: undefined,
    tagIds: undefined,
    archived: undefined
  },
  rules: {
    catName: [{ required: true, message: "猫咪名字不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, viewForm, rules } = toRefs(data)
const router = useRouter()
// 表单草稿：新增档案填到一半误关也不丢
const draft = useFormDraft('cat-profile-form', form)

function baseName(baseId) {
  const base = baseList.value.find(b => b.baseId === baseId)
  return base ? base.baseName : ''
}

function wardName(wardId) {
  const ward = wardList.value.find(w => w.wardId === wardId)
  return ward ? ward.wardName : ''
}

// 窄屏下基地与病房合并成一列展示
function locationText(row) {
  const b = baseName(row.baseId)
  const w = wardName(row.wardId)
  if (b && w) return b + ' / ' + w
  return b || w || '—'
}

// 表单里的病房下拉跟随所选基地联动
const formWardList = computed(() => {
  if (!form.value.baseId) return wardList.value
  return wardList.value.filter(w => w.baseId === form.value.baseId)
})

// 切换基地后清空病房，避免出现"基地 A + 基地 B 的病房"这种非法组合
function onBaseChange() {
  form.value.wardId = undefined
}

function getBaseList() {
  listCatBase().then(response => {
    baseList.value = response.rows
  })
}

function getWardList() {
  listCatWard({ pageNum: 1, pageSize: 500 }).then(response => {
    wardList.value = response.rows || []
  })
}

function getTagList() {
  listCatTag().then(response => {
    tagList.value = response.rows
  })
}

function getList() {
  loading.value = true
  // 标签是多选，接口按逗号分隔的 tagIds 传，Spring 才能绑定到 Long[]
  const params = { ...queryParams.value }
  if (Array.isArray(params.tagIds) && params.tagIds.length) {
    params.tagIds = params.tagIds.join(',')
  } else {
    delete params.tagIds
  }
  listCatProfile(params).then(response => {
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
    archiveNo: undefined,
    catName: undefined,
    alias: undefined,
    breed: undefined,
    sex: undefined,
    ageEstimate: undefined,
    weight: undefined,
    personality: undefined,
    source: undefined,
    captureDate: undefined,
    capturePoint: undefined,
    neutered: '0',
    dewormed: '0',
    vaccinated: '0',
    feature: undefined,
    status: '1',
    baseId: undefined,
    wardId: undefined,
    tagIds: [],
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
  ids.value = selection.map(item => item.catId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  const saved = draft.restore()
  if (saved) {
    form.value = { ...saved }
    proxy.$modal.msgSuccess("已恢复上次未保存的档案内容")
  }
  draft.begin()
  open.value = true
  title.value = "添加猫咪档案"
}

function handleUpdate(row) {
  reset()
  const id = row.catId || ids.value
  getCatProfile(id).then(response => {
    form.value = response.data
    draft.begin()
    open.value = true
    title.value = "修改猫咪档案"
  })
}

// 查看完整档案：跳转档案详情页（含病历时间线、相册、流转轨迹）
function handleView(row) {
  router.push({ path: '/profile/detail/' + row.catId })
}

// 归档 / 取消归档
function handleArchive(row) {
  proxy.$modal.confirm('归档后该猫咪不再出现在在养列表，可随时取消归档。是否继续？').then(() => {
    return archiveCatProfile(row.catId)
  }).then(() => {
    proxy.$modal.msgSuccess('已归档')
    getList()
  }).catch(() => {})
}

function handleUnarchive(row) {
  unarchiveCatProfile(row.catId).then(() => {
    proxy.$modal.msgSuccess('已取消归档')
    getList()
  })
}

// 二维码卡片打印：带上当前勾选的猫咪
function handlePrintCards() {
  const ids = ids.value && ids.value.length ? ids.value.join(',') : ''
  router.push({ path: '/profile/qrcard', query: ids ? { catIds: ids } : {} })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.catId != undefined) {
        updateCatProfile(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          draft.clear()
          draft.pause()
          open.value = false
          getList()
        })
      } else {
        addCatProfile(form.value).then(response => {
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
  const delIds = row.catId || ids.value
  proxy.$modal.confirm('是否确认删除该猫咪档案？删除后不可恢复。').then(function () {
    return delCatProfile(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/profile/export", { ...queryParams.value }, `猫咪档案_${new Date().getTime()}.xlsx`)
}

// 导出单猫档案 PDF（完整病历、流转、用药、疫苗、标签、死亡记录）
function handleExportPdf(row) {
  proxy.downloadGet("cat/profile/pdf/" + row.catId, {}, `猫咪档案_${row.archiveNo || row.catId}.pdf`)
}

function handleGenQr(catId) {
  if (!catId) {
    proxy.$modal.msgWarning('请先保存档案后再生成二维码')
    return
  }
  // 接口直接返回新二维码，就地更新当前弹窗，避免再发一次详情查询（旧值可能顶掉新值）
  regenerateCatQrCode(catId).then(res => {
    const data = res.data || {}
    if (open.value && data.qrCode) {
      form.value.qrCode = data.qrCode
      form.value.scanUrl = data.scanUrl
      qrTick.value++
    }
    if (viewOpen.value && data.qrCode) {
      viewForm.value.qrCode = data.qrCode
      viewForm.value.scanUrl = data.scanUrl
      qrTick.value++
    }
    // 二维码里存的是「手机扫码要打开的网址」，网址没变，图案自然一样，这里说人话告诉用户
    if (data.changed === false) {
      proxy.$modal.msg('这个二维码已经是最新的，不用更新（它指向的档案网址没有变）')
    } else {
      proxy.$modal.msgSuccess('二维码已更新，现在指向新的档案网址')
    }
    // 列表里没有二维码列，但仍刷新一次，保证修改/更新时间等信息同步
    getList()
  })
}

function handleGenQrBatch() {
  proxy.$modal.confirm('将把所有猫咪的二维码都改成当前的系统访问网址。已经打印、贴在笼位上的旧码会失效，需要重新打印。是否继续？').then(() => {
    return regenerateAllCatQrCode()
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '全部二维码已更新')
    getList()
  }).catch(() => {})
}

// 从看板等入口带过来的筛选条件（?status=3 之类），进页面即生效
const route = useRoute()
function applyRouteQuery() {
   const q = route.query || {}
   queryParams.value.status = q.status || undefined
   queryParams.value.baseId = q.baseId ? Number(q.baseId) : undefined
   queryParams.value.catName = q.catName || undefined
   queryParams.value.archived = q.archived || undefined
   queryParams.value.tagIds = q.tagIds ? String(q.tagIds).split(',').map(Number) : undefined
   queryParams.value.pageNum = 1
   // 从档案详情页点「编辑档案」跳回时，直接打开该猫咪的编辑弹窗
   if (q.editCatId) {
      handleUpdate({ catId: Number(q.editCatId) })
   }
}
applyRouteQuery()
// 同路由仅 query 变化时（如返回看板再点另一张卡），组件不会重建，需手动刷新
watch(() => route.query, () => {
   applyRouteQuery()
   getList()
})

getList()
getBaseList()
getTagList()
getWardList()
</script>

<style scoped>
.qr-box {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.qr-img {
  width: 120px;
  height: 120px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  background: #fff;
}
.qr-empty {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  border: 1px dashed var(--el-border-color);
  border-radius: 4px;
}
.qr-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.qr-url {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
  line-height: 1.5;
}
.qr-help {
  font-size: 12px;
  color: #b1b3b8;
  line-height: 1.5;
}
</style>
