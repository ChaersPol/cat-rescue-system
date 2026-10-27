<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="配置键" prop="configKey">
        <el-input v-model="queryParams.configKey" placeholder="请输入配置键" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="配置名称" prop="configName">
        <el-input v-model="queryParams.configName" placeholder="请输入配置名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:config:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:config:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:config:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置键" align="center" prop="configKey" width="240" />
      <el-table-column label="配置名称" align="center" prop="configName" width="220" />
      <el-table-column label="配置值" align="center" min-width="240">
        <template #default="scope">
          <el-tag v-if="scope.row.configKey === 'scan.permission.mode'" :type="scope.row.configValue === 'B' ? 'success' : 'warning'">
            {{ scope.row.configValue === 'B' ? 'B - 内部模式（扫码看全部病历）' : 'A - 公开模式（扫码仅看基础信息）' }}
          </el-tag>
          <el-tag v-else-if="isDayKey(scope.row.configKey)" type="primary" effect="plain">
            {{ scope.row.configValue || 0 }} 天
          </el-tag>
          <span v-else>{{ scope.row.configValue || '（未配置）' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="170" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:config:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:config:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如 scan.permission.mode" :disabled="form.configId != undefined" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-radio-group v-if="form.configKey === 'scan.permission.mode'" v-model="form.configValue">
            <el-radio label="A">A - 公开模式（扫码仅看基础信息，病历需登录）</el-radio>
            <el-radio label="B">B - 内部模式（扫码可直接查看全部病历）</el-radio>
          </el-radio-group>
          <el-input-number v-else-if="isDayKey(form.configKey)" v-model.number="form.configValue" :min="1" :max="365" controls-position="right" style="width: 100%" />
          <el-input v-else v-model="form.configValue" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item v-if="isDayKey(form.configKey)" label="说明">
          <span class="tip">提醒扫描时按此天数判定到期范围。例如设为 7，则「7 天内到期」和「过期 7 天内」的记录都会生成提醒。</span>
        </el-form-item>
        <el-form-item v-if="form.configKey === 'scan.base.url'" label="说明">
          <span class="tip">
            这就是「手机扫猫咪二维码后会打开的网址」。要填手机能访问到的地址，不能填 localhost：<br />
            · 局域网内测：填电脑的局域网地址，例如 http://192.168.1.113<br />
            · 正式使用：填域名，例如 https://cat.example.com<br />
            改完记得去「猫咪档案」点「批量更新二维码」，否则贴纸上的旧码还是指向老地址。
          </span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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

<script setup name="CatConfig">
import { listCatConfig, getCatConfig, delCatConfig, addCatConfig, updateCatConfig } from '@/api/cat/config'

const { proxy } = getCurrentInstance()

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configKey: undefined,
    configName: undefined
  },
  rules: {
    configKey: [{ required: true, message: '配置键不能为空', trigger: 'blur' }],
    configName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const DAY_KEYS = ['reminder.advance.days', 'reminder.overdue.days', 'referral.pending.days']

function isDayKey(key) {
  return DAY_KEYS.includes(key)
}

function getList() {
  loading.value = true
  listCatConfig(queryParams.value).then(response => {
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
    configId: undefined,
    configKey: undefined,
    configValue: undefined,
    configName: undefined,
    remark: undefined
  }
  proxy.resetForm('configRef')
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
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增系统配置'
}

function handleUpdate(row) {
  reset()
  const id = row.configId || ids.value
  getCatConfig(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改系统配置'
  })
}

function submitForm() {
  proxy.$refs['configRef'].validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) {
        updateCatConfig(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addCatConfig(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.configId || ids.value
  proxy.$modal.confirm('是否确认删除该配置？删除后相关功能将使用默认值。').then(function () {
    return delCatConfig(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}
</style>
