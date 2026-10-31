<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="物资" prop="materialId">
        <el-select v-model="queryParams.materialId" placeholder="全部物资" clearable filterable style="width: 200px">
          <el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialName" :value="m.materialId" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="recordType">
        <el-select v-model="queryParams.recordType" placeholder="全部" clearable style="width: 120px">
          <el-option label="入库" value="1" />
          <el-option label="出库" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:stockrecord:add']">出入库登记</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:stockrecord:export']">导出</el-button>
      </el-col>
      <el-col :span="12">
        <span class="form-tip is-bar">出入库记录<b>不允许修改或删除</b>，如需更正请使用<b>「冲销」</b>生成反向记录。</span>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="物资" align="center" width="180">
        <template #default="scope">{{ materialName(scope.row.materialId) }}</template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="recordType" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.recordType === '1' ? 'success' : 'warning'" size="small">
            {{ scope.row.recordType === '1' ? '入库' : '出库' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数量" align="center" prop="quantity" width="100">
        <template #default="scope">
          <span :class="scope.row.recordType === '1' ? 'in' : 'out'">
            {{ scope.row.recordType === '1' ? '+' : '-' }}{{ scope.row.quantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="日期" align="center" prop="recordDate" width="160" />
      <el-table-column label="供应商/领用基地" align="center" min-width="180" :show-overflow-tooltip="true">
        <template #default="scope">{{ scope.row.supplier || scope.row.receiverBase || '-' }}</template>
      </el-table-column>
      <el-table-column label="领用人" align="center" prop="receiver" width="110" />
      <el-table-column label="用途" align="center" prop="purpose" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="备注" align="center" prop="remark" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="操作人" align="center" prop="operator" width="100" />
      <el-table-column label="操作" align="center" width="110" class-name="small-padding fixed-width" v-if="checkPermi(['cat:stockrecord:edit'])">
        <template #default="scope">
          <el-button link type="danger" icon="RefreshLeft" @click="handleReverse(scope.row)" v-hasPermi="['cat:stockrecord:edit']">冲销</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="出入库登记" v-model="open" width="620px" append-to-body>
      <el-form ref="stockRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="物资" prop="materialId">
          <el-select v-model="form.materialId" placeholder="请选择物资" filterable style="width: 100%">
            <el-option
              v-for="m in materialOptions"
              :key="m.materialId"
              :label="m.materialName + '（当前库存 ' + m.stock + ' ' + (m.unit || '') + '）'"
              :value="m.materialId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="recordType">
          <el-radio-group v-model="form.recordType">
            <el-radio label="1">入库</el-radio>
            <el-radio label="2">出库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier" v-if="form.recordType === '1'">
          <el-input v-model="form.supplier" placeholder="入库供应商" />
        </el-form-item>
        <el-form-item label="领用基地" prop="receiverBase" v-if="form.recordType === '2'">
          <el-select v-model="form.receiverBase" placeholder="请选择领用基地" filterable style="width: 100%">
            <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseName" />
          </el-select>
          <div class="form-tip">只能选择<b>基地管理</b>中已登记的基地，需新增请先到基地管理维护。</div>
        </el-form-item>
        <el-form-item label="领用人" prop="receiver" v-if="form.recordType === '2'">
          <el-input v-model="form.receiver" placeholder="领用人" />
        </el-form-item>
        <el-form-item label="用途" prop="purpose">
          <el-input v-model="form.purpose" placeholder="用途" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="操作人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CatStockRecord">
import { listCatStockRecord, addCatStockRecord, reverseCatStockRecord } from '@/api/cat/stockrecord'
import { listCatMaterial } from '@/api/cat/material'
import { listCatBase } from '@/api/cat/base'

const { proxy } = getCurrentInstance()

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const materialOptions = ref([])
const baseOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    materialId: undefined,
    recordType: undefined
  },
  rules: {
    materialId: [{ required: true, message: '请选择物资', trigger: 'change' }],
    recordType: [{ required: true, message: '请选择类型', trigger: 'change' }],
    quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function materialName(id) {
  const m = materialOptions.value.find(i => i.materialId === id)
  return m ? m.materialName : '#' + id
}

function loadMaterials() {
  listCatMaterial({ pageNum: 1, pageSize: 999 }).then(res => {
    materialOptions.value = res.rows || []
  })
}

// 领用基地只允许选系统里已登记的启用基地，避免录入手写名称
function loadBases() {
  listCatBase({ pageNum: 1, pageSize: 999, status: '0' }).then(res => {
    baseOptions.value = res.rows || []
  })
}

function getList() {
  loading.value = true
  listCatStockRecord(queryParams.value).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  })
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
  // 物资、基地可能在别的页签刚新增，打开弹窗前重拉下拉
  loadMaterials()
  loadBases()
  form.value = {
    recordId: undefined,
    materialId: undefined,
    recordType: '1',
    quantity: 1,
    recordDate: proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
    supplier: undefined,
    receiverBase: undefined,
    receiver: undefined,
    purpose: undefined,
    operator: undefined,
    remark: undefined
  }
  proxy.clearFormValidate('stockRef')
  open.value = true
}

function submitForm() {
  proxy.$refs['stockRef'].validate(valid => {
    if (valid) {
      addCatStockRecord(form.value).then(() => {
        proxy.$modal.msgSuccess('登记成功，库存已更新')
        open.value = false
        getList()
        loadMaterials()
      })
    }
  })
}

function handleReverse(row) {
  proxy.$modal.confirm('是否确认冲销该记录？系统将生成一条反向记录并回滚库存。').then(function () {
    return reverseCatStockRecord(row.recordId)
  }).then(() => {
    proxy.$modal.msgSuccess('冲销成功')
    getList()
    loadMaterials()
  }).catch(() => {})
}

function handleExport() {
  proxy.download('cat/stockrecord/export', { ...queryParams.value }, `stock_${new Date().getTime()}.xlsx`)
}

loadMaterials()
getList()
</script>

<style scoped>
.in {
  color: #67c23a;
  font-weight: 600;
}
.out {
  color: #e6a23c;
  font-weight: 600;
}
</style>
