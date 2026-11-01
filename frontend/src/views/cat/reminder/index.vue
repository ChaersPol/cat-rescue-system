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
        <el-button type="success" plain icon="Check" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleMarkReadBatch" v-hasPermi="['cat:reminder:edit']">标记已读</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:reminder:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:reminder:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange" :row-class-name="rowClassName">
      <el-table-column type="selection" width="45" align="center" v-if="$route.meta.canRemove" />
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="类型" align="center" prop="reminderType" width="90">
        <template #default="scope">
          <el-tag :type="typeTag(scope.row.reminderType)" size="small">{{ scope.row.reminderType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" width="150" :show-overflow-tooltip="true" />
      <!-- 内容列不放 show-overflow-tooltip：用药冲突提示需要完整换行显示，不能被省略号截掉 -->
      <el-table-column label="内容" align="left" prop="content" min-width="300">
        <template #default="scope">
          <div>{{ scope.row.content }}</div>
          <!-- 用药冲突：醒目的红色提示，告诉用户这一针现在打不了 -->
          <div v-if="scope.row.conflictTip" class="conflict-tip">
            <el-icon class="conflict-icon"><WarningFilled /></el-icon>
            <span>{{ scope.row.conflictTip }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="关联猫咪" align="center" prop="catId" width="220">
        <template #default="scope">
          <span v-if="scope.row.catId">{{ catName(scope.row.catId) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="提醒日期" align="center" prop="remindDate" width="110">
        <template #default="scope">
          <span :class="{ 'overdue': isOverdue(scope.row) }">{{ parseTime(scope.row.remindDate, '{y}-{m}-{d}') }}</span>
          <div v-if="scope.row.deferCount > 0" class="defer-hint">
            已推迟 {{ scope.row.deferCount }} 次
            <span v-if="scope.row.originDate">（原 {{ parseTime(scope.row.originDate, '{m}-{d}') }}）</span>
          </div>
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
      <el-table-column label="操作" align="center" width="270" class-name="small-padding fixed-width" v-if="checkPermi(['cat:reminder:edit', 'cat:reminder:remove'])">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" link type="success" icon="Check" @click="handleMarkRead(scope.row)" v-hasPermi="['cat:reminder:edit']">已读</el-button>
          <el-button link type="warning" icon="Clock" @click="handleDefer(scope.row)" v-hasPermi="['cat:reminder:edit']">推迟</el-button>
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
            <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
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

    <!-- 推迟提醒：疫苗到期但猫咪在用药期不能接种时，把提醒顺延几天 -->
    <el-dialog title="推迟提醒" v-model="deferOpen" width="460px" append-to-body>
      <div v-if="deferRow.conflictTip" class="defer-conflict">
        <el-icon><WarningFilled /></el-icon>
        <span>{{ deferRow.conflictTip }}</span>
      </div>
      <el-form label-width="90px">
        <el-form-item label="顺延天数">
          <el-radio-group v-model="deferDays">
            <el-radio-button :value="3">3 天</el-radio-button>
            <el-radio-button :value="7">7 天</el-radio-button>
            <el-radio-button :value="14">14 天</el-radio-button>
            <el-radio-button :value="30">30 天</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="自定义">
          <el-input-number v-model="deferDays" :min="1" :max="365" controls-position="right" style="width: 140px" />
          <span class="defer-unit">天</span>
        </el-form-item>
        <el-form-item label="原提醒日期">
          <span class="defer-origin">{{ parseTime(deferRow.remindDate, '{y}-{m}-{d}') || '—' }}</span>
        </el-form-item>
        <el-form-item label="推迟后">
          <span class="defer-new">{{ deferPreview }}</span>
        </el-form-item>
        <el-form-item v-if="canSyncPlan" label="同步台账">
          <el-checkbox v-model="deferSyncPlan">
            同时把{{ syncPlanLabel }}改为 <b>{{ deferPreview }}</b>
          </el-checkbox>
        </el-form-item>
      </el-form>
      <div class="defer-note">
        推迟后这条提醒会重新变成未读，到期当天会再次提醒相关人员。
        <template v-if="canSyncPlan">
          <br />
          <span v-if="deferSyncPlan">台账日期会一起顺延，两处保持一致。</span>
          <span v-else class="defer-warn">未勾选同步，台账仍显示原日期，两边会对不上。</span>
        </template>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="deferLoading" @click="submitDefer">确 定 推 迟</el-button>
          <el-button @click="deferOpen = false">取 消</el-button>
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
  markReadBatchCatReminder,
  deferCatReminder
} from '@/api/cat/reminder'
import { listCatProfile } from '@/api/cat/profile'
import { catLabelOf, formatCatLabel } from '@/utils/catCache'

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

// 推迟提醒
const deferOpen = ref(false)
const deferLoading = ref(false)
const deferDays = ref(7)
const deferRow = ref({})
// 推迟时是否同步顺延疫苗/驱虫台账的计划日期，默认同步，避免两边日期对不上
const deferSyncPlan = ref(true)

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

// 猫咪展示统一为「名字（档案编号）」，格式只在 utils/catCache.js 里定义
const catName = catId => catLabelOf(catOptions, catId)

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
  proxy.clearFormValidate('reminderRef')
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

/** 推迟后落到哪天：与后端口径一致，取「原提醒日期」和「今天」中较晚的往后顺延 */
const deferPreview = computed(() => {
  const days = Number(deferDays.value) || 0
  if (days <= 0) return '—'
  const now = new Date()
  let base = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const raw = deferRow.value.remindDate
  if (raw) {
    // 用 / 分隔，避免 Safari 解析 yyyy-MM-dd 时按 UTC 处理导致差一天
    const d = new Date(String(raw).slice(0, 10).replace(/-/g, '/'))
    if (!isNaN(d.getTime()) && d.getTime() > base.getTime()) base = d
  }
  const target = new Date(base.getTime() + days * 86400000)
  const pad = n => String(n).padStart(2, '0')
  return `${target.getFullYear()}-${pad(target.getMonth() + 1)}-${pad(target.getDate())}`
})

/** 只有疫苗 / 驱虫提醒才有「台账计划日期」可同步 */
const canSyncPlan = computed(() => ['疫苗', '驱虫'].includes(deferRow.value.reminderType))
const syncPlanLabel = computed(() =>
  deferRow.value.reminderType === '疫苗' ? '疫苗台账的「下次接种」' : '驱虫台账的「下次驱虫」'
)

function handleDefer(row) {
  deferRow.value = { ...row }
  deferDays.value = 7
  deferSyncPlan.value = true
  deferOpen.value = true
}

function submitDefer() {
  const days = Number(deferDays.value)
  if (!days || days <= 0 || days > 365) {
    return proxy.$modal.msgWarning('推迟天数需在 1~365 天之间')
  }
  deferLoading.value = true
  const sync = canSyncPlan.value ? deferSyncPlan.value : false
  deferCatReminder(deferRow.value.reminderId, days, sync).then(res => {
    proxy.$modal.msgSuccess(res.msg || `已推迟 ${days} 天`)
    deferOpen.value = false
    getList()
  }).finally(() => {
    deferLoading.value = false
  })
}

function handleAdd() {
  // 猫咪档案可能在别的页签刚新增，打开弹窗前重拉下拉
  loadCats()
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

/* 用药冲突提示：红字 + 淡红底，扫一眼就能看到「这一针现在打不了」 */
.conflict-tip {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  margin-top: 4px;
  padding: 3px 6px;
  border-radius: 4px;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  color: #d93026;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.5;
  text-align: left;
  white-space: normal;
}
.conflict-tip .conflict-icon {
  flex: none;
  margin-top: 2px;
  color: #f56c6c;
  font-size: 13px;
}

.defer-hint {
  margin-top: 2px;
  font-size: 12px;
  color: #e6a23c;
}

/* 推迟弹窗 */
.defer-conflict {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 14px;
  padding: 8px 10px;
  border-radius: 4px;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  color: #d93026;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.6;
}
.defer-conflict .el-icon {
  flex: none;
  margin-top: 3px;
  color: #f56c6c;
}
.defer-unit {
  margin-left: 8px;
  color: var(--el-text-color-regular);
}
.defer-origin {
  color: var(--el-text-color-secondary);
}
.defer-new {
  color: #d93026;
  font-weight: 700;
  font-size: 15px;
}
.defer-note {
  padding-left: 90px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}
/* 未勾选同步台账时的提醒文案，要能一眼看到 */
.defer-warn {
  color: var(--el-color-danger);
  font-weight: 600;
}
</style>
