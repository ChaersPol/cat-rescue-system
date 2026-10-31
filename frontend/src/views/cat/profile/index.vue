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
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:profile:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:profile:remove']">删除</el-button>
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
         <el-table-column type="selection" width="45" align="center" v-if="$route.meta.canRemove" />
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="50" align="center" />
         <el-table-column label="档案编号" align="center" prop="archiveNo" min-width="152" :show-overflow-tooltip="true" />
         <el-table-column label="猫咪名字" align="center" prop="catName" min-width="82" :show-overflow-tooltip="true">
            <template #default="scope">
               {{ scope.row.catName }}
               <el-tag v-if="scope.row.archived === '1'" size="small" type="warning" effect="plain" style="margin-left: 4px">归档</el-tag>
            </template>
         </el-table-column>
         <el-table-column v-if="wideTable" label="品种" align="center" prop="breed" min-width="75" :show-overflow-tooltip="true" />
         <el-table-column v-if="wideTable" label="性别" align="center" prop="sex" min-width="55">
            <template #default="scope">
               <dict-tag :options="cat_sex" :value="scope.row.sex" />
            </template>
         </el-table-column>
         <el-table-column label="年龄" align="center" prop="ageEstimate" min-width="70" :show-overflow-tooltip="true" />
         <el-table-column label="状态" align="center" prop="status" min-width="150" :show-overflow-tooltip="true">
            <template #default="scope">
               <dict-tag :options="cat_status" :value="scope.row.status" />
               <span v-if="statusDetail(scope.row)" class="status-detail">{{ statusDetail(scope.row) }}</span>
            </template>
         </el-table-column>
         <!-- 基地名已在状态里体现，这里只留病房，避免重复占宽 -->
         <el-table-column label="所在病房" align="center" prop="wardId" :min-width="wideTable ? 118 : 98" :show-overflow-tooltip="true">
            <template #default="scope">{{ wardName(scope.row.wardId) || '—' }}</template>
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
                     <el-select v-model="form.status" placeholder="请选择状态" clearable style="width: 100%" @change="onStatusChange">
                        <el-option v-for="dict in cat_status" :key="dict.value" :label="dict.label" :value="dict.value" />
                     </el-select>
                     <div v-if="statusHint" class="status-hint">{{ statusHint }}</div>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="8">
                  <el-form-item label="所在基地" prop="baseId">
                     <el-select v-model="form.baseId" :placeholder="needBase ? '必须选择所在基地' : '请选择基地'" clearable style="width: 100%" @change="onBaseChange">
                        <el-option v-for="base in baseList" :key="base.baseId" :label="base.baseName" :value="base.baseId" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="8" v-if="needHospital">
                  <el-form-item label="外送医院" prop="hospitalName">
                     <el-select v-model="form.hospitalName" placeholder="选择或输入外送医院" filterable allow-create default-first-option clearable style="width: 100%">
                        <el-option v-for="name in hospitalOptions" :key="name" :label="name" :value="name" />
                     </el-select>
                  </el-form-item>
               </el-col>
               <el-col :span="8" v-else>
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
            <el-descriptions-item label="外送医院">{{ viewForm.hospitalName || '—' }}</el-descriptions-item>
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
import { listCatProfile, getCatProfile, delCatProfile, addCatProfile, updateCatProfile, regenerateCatQrCode, regenerateAllCatQrCode, archiveCatProfile, unarchiveCatProfile, listHospitalOptions } from "@/api/cat/profile"
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
const hospitalOptions = ref([])
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
// 状态列带上具体基地后变宽，宽表阈值相应提到 1100，否则各列最小宽加起来会撑破容器
const wideTable = computed(() => tableWidth.value >= 1100)
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
  }
})

const { queryParams, form, viewForm } = toRefs(data)
const router = useRouter()
// 表单草稿：新增档案填到一半误关也不丢
const draft = useFormDraft('cat-profile-form', form)

// 状态与所在位置的对应关系：
// 1在养、2基地内治疗中 → 必须选所在基地；3外送医院治疗中 → 必须填医院
const needBase = computed(() => ["1", "2"].includes(String(form.value.status || "")))
const needHospital = computed(() => String(form.value.status || "") === "3")
const statusHint = computed(() => {
  const s = String(form.value.status || "")
  if (s === "1") return "在养需要指定猫咪当前所在的基地"
  if (s === "2") return "基地内治疗中需要指定治疗所在的基地"
  if (s === "3") return "外送医院治疗中需要指定外送的医院"
  return ""
})

// 校验规则随状态变化：基地、医院按需必填
const rules = computed(() => {
  const result = {
    catName: [{ required: true, message: "猫咪名字不能为空", trigger: "blur" }]
  }
  if (needBase.value) {
    result.baseId = [{ required: true, message: "请选择猫咪当前所在基地", trigger: "change" }]
  }
  if (needHospital.value) {
    result.hospitalName = [{ required: true, message: "请选择或输入外送医院", trigger: "change" }]
  }
  return result
})

// 状态后面补上具体位置，比如「在养（北苑流浪猫基地）」「外送医院治疗中（瑞鹏宠物医院）」
function statusDetail(row) {
  const s = String((row && row.status) || "")
  if (s === "1" || s === "2") {
    const b = baseName(row.baseId)
    return b || ""
  }
  if (s === "3") {
    return row.hospitalName || ""
  }
  return ""
}

// 切换状态后清掉上一种状态的校验残留，避免红字一直挂着
function onStatusChange() {
  // 外送期间猫不在基地病房，清掉旧病房，避免「外送医院治疗中 + 幼猫房 B」这种矛盾组合
  if (String(form.value.status || "") === "3") {
    form.value.wardId = undefined
  }
  proxy.$nextTick(() => {
    const formRef = proxy.$refs["formRef"]
    if (formRef) formRef.clearValidate(["baseId", "hospitalName"])
  })
}

function baseName(baseId) {
  const base = baseList.value.find(b => b.baseId === baseId)
  return base ? base.baseName : ''
}

function wardName(wardId) {
  const ward = wardList.value.find(w => w.wardId === wardId)
  return ward ? ward.wardName : ''
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

// 外送医院候选：取历史用过的医院名，允许下拉选旧的、也能直接输入新医院
function getHospitalOptions() {
  listHospitalOptions().then(response => {
    hospitalOptions.value = response.data || []
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
    hospitalName: undefined,
    tagIds: [],
    remark: undefined
  }
  proxy.clearFormValidate("formRef")
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
    draft.begin('edit')
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
  // 注意：不能写成 const ids = ids.value...（会命中 TDZ，报 Cannot access 'ids' before initialization）
  const selectedIds = ids.value && ids.value.length ? ids.value.join(',') : ''
  router.push({ path: '/profile/qrcard', query: selectedIds ? { catIds: selectedIds } : {} })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.catId != undefined) {
        updateCatProfile(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
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
getHospitalOptions()
</script>

<style scoped>
/* 状态下面的具体位置（基地名 / 医院名），独立一行小字 */
.status-detail {
  display: block;
  margin-top: 1px;
  font-size: 11px;
  line-height: 1.3;
  color: var(--el-text-color-secondary);
}
/* 状态下方的联动提示，说明为什么必须再选基地 / 医院 */
.status-hint {
  width: 100%;
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--el-color-warning);
}
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
