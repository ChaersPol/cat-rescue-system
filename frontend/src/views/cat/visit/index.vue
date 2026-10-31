<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="猫咪" prop="catId">
        <el-select v-model="queryParams.catId" placeholder="全部猫咪" clearable filterable style="width: 200px">
          <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
        </el-select>
      </el-form-item>
      <el-form-item label="回访状态" prop="visitStatus">
        <el-select v-model="queryParams.visitStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待回访" value="0" />
          <el-option label="已回访" value="1" />
          <el-option label="回访异常" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="计划类型" prop="planType">
        <el-select v-model="queryParams.planType" placeholder="全部" clearable style="width: 120px">
          <el-option label="3天" value="3天" />
          <el-option label="1个月" value="1个月" />
          <el-option label="3个月" value="3个月" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:visit:add']">新增回访计划</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:visit:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:visit:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="猫咪" align="center" width="220">
        <template #default="scope">{{ catName(scope.row.catId) }}</template>
      </el-table-column>
      <el-table-column label="回访计划" align="center" prop="planType" width="110">
        <template #default="scope">
          <el-tag size="small" type="info">{{ scope.row.planType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计划日期" align="center" prop="planDate" width="120">
        <template #default="scope">
          <span :class="{ overdue: isOverdue(scope.row) }">{{ parseTime(scope.row.planDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="回访状态" align="center" prop="visitStatus" width="110">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.visitStatus)" size="small">{{ statusLabel(scope.row.visitStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="回访内容" align="center" prop="visitContent" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="回访照片" align="center" width="100">
        <template #default="scope">
          <el-button v-if="fileItems(scope.row.photos).length" link type="primary" @click="openPhotos(scope.row)">
            {{ fileItems(scope.row.photos).length }} 张
          </el-button>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="回访人" align="center" prop="visitBy" width="110" />
      <el-table-column label="实际回访时间" align="center" prop="visitTime" width="160" />
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width" v-if="checkPermi(['cat:visit:edit', 'cat:visit:remove'])">
        <template #default="scope">
          <el-button link type="success" icon="Edit" @click="handleVisit(scope.row)" v-hasPermi="['cat:visit:edit']">登记回访</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:visit:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 登记 / 修改回访 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="visitRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="猫咪" prop="catId">
          <el-select v-model="form.catId" placeholder="请选择猫咪" filterable style="width: 100%">
            <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
          </el-select>
        </el-form-item>
        <el-form-item label="回访计划" prop="planType">
          <el-radio-group v-model="form.planType">
            <el-radio label="3天">3天</el-radio>
            <el-radio label="1个月">1个月</el-radio>
            <el-radio label="3个月">3个月</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划日期" prop="planDate">
          <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择计划日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="回访状态" prop="visitStatus">
          <el-select v-model="form.visitStatus" style="width: 100%">
            <el-option label="待回访" value="0" />
            <el-option label="已回访" value="1" />
            <el-option label="回访异常" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="回访内容" prop="visitContent">
          <el-input v-model="form.visitContent" type="textarea" :rows="3" placeholder="记录猫咪适应情况、饮食、健康、与主人互动等" />
        </el-form-item>
        <el-form-item label="回访照片" prop="photos">
          <media-upload v-model="form.photos" :limit="9" :image-size="10" image-only />
        </el-form-item>
        <el-form-item label="回访人" prop="visitBy">
          <el-input v-model="form.visitBy" placeholder="请输入回访人" />
        </el-form-item>
        <el-form-item label="实际回访时间" prop="visitTime">
          <el-date-picker v-model="form.visitTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择回访时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 回访照片预览 -->
    <el-dialog v-model="photosVisible" :title="photosTitle" width="760px" append-to-body destroy-on-close>
      <el-empty v-if="!photos.length" description="暂无回访照片" :image-size="60" />
      <div v-else class="photo-grid">
        <el-image
          v-for="(u, i) in photos"
          :key="i"
          :src="resolveFileUrl(u)"
          :preview-src-list="photoUrls"
          :initial-index="i"
          fit="cover"
          preview-teleported
          class="photo-cell"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="CatVisit">
import { listCatVisit, getCatVisit, delCatVisit, addCatVisit, updateCatVisit } from '@/api/cat/visit'
import { listCatProfile } from '@/api/cat/profile'
import { catLabelOf, formatCatLabel } from '@/utils/catCache'
import { resolveFileUrl } from '@/utils/fileUrl'

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
const photosVisible = ref(false)
const photosTitle = ref('')
const photos = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    visitStatus: undefined,
    planType: undefined
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    planType: [{ required: true, message: '请选择回访计划', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 猫咪展示统一为「名字（档案编号）」，格式只在 utils/catCache.js 里定义
const catName = catId => catLabelOf(catOptions, catId)

function statusLabel(s) {
  return { '0': '待回访', '1': '已回访', '2': '回访异常' }[s] || '未知'
}

function statusTag(s) {
  return { '0': 'warning', '1': 'success', '2': 'danger' }[s] || 'info'
}

function isOverdue(row) {
  if (!row.planDate || row.visitStatus !== '0') return false
  return new Date(row.planDate).getTime() < Date.now() - 86400000
}

/** 逗号分隔的路径串 → 数组（空串过滤掉） */
function fileItems(str) {
  if (!str) return []
  return String(str)
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
}

const photoUrls = computed(() => photos.value.map(u => resolveFileUrl(u)))

function openPhotos(row) {
  photos.value = fileItems(row.photos)
  photosTitle.value = '回访照片 - ' + catName(row.catId)
  photosVisible.value = true
}

function loadCats() {
  listCatProfile({ pageNum: 1, pageSize: 999 }).then(res => {
    catOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listCatVisit(queryParams.value).then(response => {
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
    visitId: undefined,
    adoptionId: undefined,
    catId: undefined,
    planType: '3天',
    planDate: undefined,
    visitStatus: '0',
    visitContent: undefined,
    photos: undefined,
    visitBy: undefined,
    visitTime: undefined
  }
  proxy.clearFormValidate('visitRef')
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
  ids.value = selection.map(item => item.visitId)
  multiple.value = !selection.length
}

function handleAdd() {
  // 猫咪档案可能在别的页签刚新增，打开弹窗前重拉下拉
  loadCats()
  reset()
  open.value = true
  title.value = '新增回访计划'
}

function handleVisit(row) {
  reset()
  getCatVisit(row.visitId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '登记回访 - ' + catName(row.catId)
  })
}

function submitForm() {
  proxy.$refs['visitRef'].validate(valid => {
    if (valid) {
      if (form.value.visitId != undefined) {
        // 已回访时自动补记回访人为当前用户、回访时间为当前时间
        if (form.value.visitStatus !== '0' && !form.value.visitTime) {
          form.value.visitTime = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')
        }
        updateCatVisit(form.value).then(() => {
          proxy.$modal.msgSuccess('保存成功')
          open.value = false
          getList()
        })
      } else {
        addCatVisit(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.visitId || ids.value
  proxy.$modal.confirm('是否确认删除该回访记录？').then(function () {
    return delCatVisit(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('cat/visit/export', { ...queryParams.value }, `visit_${new Date().getTime()}.xlsx`)
}

// 从看板「待回访」等入口带过来的筛选条件，进页面即生效
const route = useRoute()
function applyRouteQuery() {
  const q = route.query || {}
  queryParams.value.visitStatus = q.visitStatus || undefined
  queryParams.value.catId = q.catId ? Number(q.catId) : undefined
  queryParams.value.pageNum = 1
}
applyRouteQuery()
watch(() => route.query, () => {
  applyRouteQuery()
  getList()
})

loadCats()
getList()
</script>

<style scoped>
.overdue {
  color: #f56c6c;
  font-weight: 600;
}

.photo-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.photo-cell {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-fill-color-darker);
}
</style>
