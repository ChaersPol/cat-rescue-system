<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="猫咪" prop="catId">
        <el-select v-model="queryParams.catId" placeholder="全部猫咪" clearable filterable style="width: 200px">
          <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
        </el-select>
      </el-form-item>
      <el-form-item label="死亡类型" prop="deathType">
        <el-select v-model="queryParams.deathType" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="d in cat_death_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="死亡登记提交后，猫咪状态将自动变更为「已离世」并归档，同时生成一条不可删除的流转记录。"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 12px"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:death:add']">死亡登记</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:death:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:death:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="猫咪" align="center" width="220">
        <template #default="scope">{{ catName(scope.row.catId) }}</template>
      </el-table-column>
      <el-table-column label="死亡时间" align="center" prop="deathDate" width="160" />
      <el-table-column label="死亡地点" align="center" prop="deathPlace" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="死亡类型" align="center" prop="deathType" width="110">
        <template #default="scope">
          <el-tag size="small" type="info">{{ deathTypeLabel(scope.row.deathType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="死因描述" align="center" prop="deathReason" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="医生" align="center" prop="doctor" width="100" />
      <el-table-column label="登记人" align="center" prop="registrar" width="100" />
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width" v-if="checkPermi(['cat:death:edit', 'cat:death:remove'])">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:death:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:death:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="deathRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="猫咪" prop="catId">
          <el-select v-model="form.catId" placeholder="请选择猫咪" filterable style="width: 100%" :disabled="form.deathId != undefined">
            <el-option v-for="c in catOptions" :key="c.catId" :label="formatCatLabel(c.catName, c.archiveNo)" :value="c.catId" />
          </el-select>
        </el-form-item>
        <el-form-item label="死亡时间" prop="deathDate">
          <el-date-picker v-model="form.deathDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择死亡时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="死亡地点" prop="deathPlace">
          <el-input v-model="form.deathPlace" placeholder="如：救助基地 / 合作宠物医院" />
        </el-form-item>
        <el-form-item label="死亡类型" prop="deathType">
          <el-select v-model="form.deathType" placeholder="请选择死亡类型" style="width: 100%">
            <el-option v-for="d in cat_death_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="死因描述" prop="deathReason">
          <el-input v-model="form.deathReason" type="textarea" :rows="3" placeholder="详细描述死因" />
        </el-form-item>
        <el-form-item label="既往病史" prop="history">
          <el-input v-model="form.history" type="textarea" :rows="2" placeholder="既往病史（可补充）" />
        </el-form-item>
        <el-form-item label="遗体处理" prop="bodyDisposal">
          <el-input v-model="form.bodyDisposal" type="textarea" :rows="2" placeholder="遗体处理方式备注" />
        </el-form-item>
        <el-form-item label="操作医生" prop="doctor">
          <el-input v-model="form.doctor" placeholder="操作医生" />
        </el-form-item>
        <el-form-item label="登记人" prop="registrar">
          <el-input v-model="form.registrar" placeholder="登记人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
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

<script setup name="CatDeath">
import { listCatDeath, getCatDeath, delCatDeath, addCatDeath, updateCatDeath } from '@/api/cat/death'
import { useFormDraft } from "@/utils/formDraft"
import { listCatProfile } from '@/api/cat/profile'
import { catLabelOf, formatCatLabel } from '@/utils/catCache'

const { proxy } = getCurrentInstance()
const { cat_death_type } = proxy.useDict('cat_death_type')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const catOptions = ref([])
const ids = ref([])
const multiple = ref(true)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    catId: undefined,
    deathType: undefined
  },
  rules: {
    catId: [{ required: true, message: '请选择猫咪', trigger: 'change' }],
    deathDate: [{ required: true, message: '请选择死亡时间', trigger: 'change' }],
    deathType: [{ required: true, message: '请选择死亡类型', trigger: 'change' }],
    deathReason: [{ required: true, message: '请填写死因描述', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)
// 表单草稿：填到一半误关闭不丢内容（需求文档 13）
const draft = useFormDraft('cat-death-form', form)

// 猫咪展示统一为「名字（档案编号）」，格式只在 utils/catCache.js 里定义
const catName = catId => catLabelOf(catOptions, catId)

function deathTypeLabel(v) {
  const d = (cat_death_type.value || []).find(i => i.value === v)
  return d ? d.label : v
}

function loadCats() {
  listCatProfile({ pageNum: 1, pageSize: 999 }).then(res => {
    catOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listCatDeath(queryParams.value).then(response => {
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
    deathId: undefined,
    catId: undefined,
    deathDate: proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
    deathPlace: undefined,
    deathType: '2',
    deathReason: undefined,
    history: undefined,
    bodyDisposal: undefined,
    doctor: undefined,
    registrar: undefined,
    remark: undefined
  }
  proxy.clearFormValidate('deathRef')
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
  // 猫咪档案可能在别的页签刚新增，打开弹窗前重拉下拉
  loadCats()
  reset()
  const savedDraft = draft.restore()
  if (savedDraft) {
    form.value = { ...savedDraft }
  }
  draft.begin()
  open.value = true
  title.value = '死亡登记'
}

function handleUpdate(row) {
  reset()
  getCatDeath(row.deathId).then(response => {
    form.value = response.data
    draft.begin('edit')
    open.value = true
    title.value = '修改死亡记录'
  })
}

function submitForm() {
  proxy.$refs['deathRef'].validate(valid => {
    if (valid) {
      if (form.value.deathId != undefined) {
        updateCatDeath(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          draft.pause()
          open.value = false
          getList()
        })
      } else {
        addCatDeath(form.value).then(() => {
          proxy.$modal.msgSuccess('登记成功，猫咪已变更为「已离世」并归档')
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
  ids.value = selection.map(item => item.deathId)
  multiple.value = !selection.length
}

function handleDelete(row) {
  const delIds = row.deathId || ids.value
  proxy.$modal.confirm('是否确认删除该死亡记录？').then(function () {
    return delCatDeath(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('cat/death/export', { ...queryParams.value }, `death_${new Date().getTime()}.xlsx`)
}

loadCats()
getList()
</script>
