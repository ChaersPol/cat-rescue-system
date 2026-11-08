<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="itemName">
        <el-input v-model="queryParams.itemName" placeholder="请输入名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="itemType">
        <el-select v-model="queryParams.itemType" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['cat:checkitem:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
      <el-col :span="12">
        <span class="form-tip is-bar">
          <b>开单时能选的项目</b>。项目关联物资后，医生开具就会<b>自动出库并扣减库存</b>；
          类型选「用药/疫苗/驱虫」的，还会自动写进对应台账。
        </span>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="itemList"
      row-key="itemId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="itemName" label="名称" min-width="240" :show-overflow-tooltip="true" />
      <el-table-column prop="itemType" label="类型" width="110" align="center">
        <template #default="scope">
          <el-tag :type="typeTag(scope.row.itemType)" size="small" effect="plain">{{ typeLabel(scope.row.itemType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="关联物资" min-width="200" :show-overflow-tooltip="true">
        <template #default="scope">
          <span v-if="scope.row.materialId">
            {{ scope.row.materialName }}
            <span class="muted" v-if="scope.row.stock !== null && scope.row.stock !== undefined">（库存 {{ scope.row.stock }}）</span>
          </span>
          <span v-else class="muted">未关联（不影响库存）</span>
        </template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" prop="price" label="单价" width="90" align="center">
        <template #default="scope">{{ scope.row.price || 0 }}</template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" prop="unit" label="单位" width="80" align="center" />
      <el-table-column v-if="!isNarrow" prop="orderNum" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <!-- 无权限时整列隐藏（与全站 42 个列表页一致）：否则表头还在、按钮全被 v-hasPermi 藏掉，会留一条空白列 -->
      <el-table-column
        label="操作"
        align="center"
        :width="isNarrow ? 190 : 250"
        fixed="right"
        class-name="small-padding fixed-width op-col"
        v-if="checkPermi(['cat:checkitem:add', 'cat:checkitem:edit', 'cat:checkitem:remove'])"
      >
        <template #default="scope">
          <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['cat:checkitem:add']">新增下级</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:checkitem:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:checkitem:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="itemRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ value: 'itemId', label: 'itemName', children: 'children' }"
            value-key="itemId"
            check-strictly
            :render-after-expand="false"
            placeholder="不选就是顶级"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="如：血常规 / 阿莫西林克拉维酸钾" maxlength="100" />
        </el-form-item>
        <el-form-item label="类型" prop="itemType">
          <el-radio-group v-model="form.itemType">
            <el-radio-button v-for="t in typeOptions" :key="t.value" :value="t.value">{{ t.label }}</el-radio-button>
          </el-radio-group>
          <div class="form-tip">决定开单后自动写哪个台账：用药→用药记录，疫苗→疫苗台账，驱虫→驱虫台账</div>
        </el-form-item>
        <el-form-item label="关联物资" prop="materialId">
          <el-select v-model="form.materialId" placeholder="不关联就不扣库存" clearable filterable style="width: 100%">
            <el-option
              v-for="m in materialOptions"
              :key="m.materialId"
              :label="`${m.materialName}（库存 ${m.stock} ${m.unit || ''}）`"
              :value="m.materialId"
            />
          </el-select>
          <div class="form-tip">关联后医生开具这个项目，会自动生成一条出库记录并扣库存；不关联则只做诊疗记录</div>
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item label="单位" prop="unit" label-width="70px">
              <el-input v-model="form.unit" placeholder="片/支/次" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价" prop="price" label-width="70px">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="orderNum" label-width="70px">
              <el-input-number v-model="form.orderNum" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
          <div class="form-tip">停用后开单弹窗里就看不到这个项目了，历史记录不受影响</div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" maxlength="500" />
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

<script setup name="CatCheckItem">
import { listCatCheckItem, getCatCheckItem, addCatCheckItem, updateCatCheckItem, delCatCheckItem } from '@/api/cat/checkitem'
import { listCatMaterial } from '@/api/cat/material'
import { useNarrow } from '@/utils/useNarrow'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')
const { isNarrow } = useNarrow()

const typeOptions = [
  { value: '1', label: '检查项目' },
  { value: '2', label: '用药' },
  { value: '3', label: '疫苗' },
  { value: '4', label: '驱虫' }
]

const itemList = ref([])
// 原始扁平数据：上级下拉要用它重建树，不能拿已经转成树的 itemList 再转一次
const rawList = ref([])
const materialOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const isExpandAll = ref(true)
const refreshTable = ref(true)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    itemName: undefined,
    itemType: undefined,
    status: undefined
  },
  rules: {
    itemName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
    itemType: [{ required: true, message: '请选择类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/**
 * 由扁平数据构建一棵全新的树（每个节点都是浅拷贝，绝不改动 rawList 里的对象）
 * ⚠️ 不要用 proxy.handleTree：那个实现会往原对象的 children 里 push，
 * 对同一份数据再算一次（比如打开弹窗要算上级下拉）就会把已有下级复制一遍
 */
function buildTree(list) {
  const nodes = new Map()
  const roots = []
  for (const n of list || []) {
    nodes.set(n.itemId, { ...n, children: [] })
  }
  for (const n of nodes.values()) {
    const parent = nodes.get(n.parentId)
    if (parent) parent.children.push(n)
    else roots.push(n)
  }
  return roots
}

/** 上级下拉：顶级 + 目录树；编辑时把自己和子孙剔除，避免选成环 */
const parentOptions = computed(() => {
  const tree = buildTree(rawList.value)
  const selfId = form.value.itemId
  return [{ itemId: 0, itemName: '顶级（无上级）', children: selfId ? prune(tree, selfId) : tree }]
})

/** 递归剔除指定节点（连同它的整棵子树） */
function prune(nodes, id) {
  return (nodes || [])
    .filter(n => n.itemId !== id)
    .map(n => ({ ...n, children: prune(n.children, id) }))
}

function typeLabel(v) {
  const t = typeOptions.find(i => i.value === v)
  return t ? t.label : '—'
}

function typeTag(v) {
  return { 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[v] || 'info'
}

function getList() {
  loading.value = true
  listCatCheckItem(queryParams.value).then(res => {
    rawList.value = res.data || []
    itemList.value = buildTree(rawList.value)
    loading.value = false
    refreshTable.value = false
    nextTick(() => {
      refreshTable.value = true
    })
  })
}

function loadMaterials() {
  listCatMaterial({ pageNum: 1, pageSize: 999 }).then(res => {
    materialOptions.value = res.rows || []
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  getList()
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

function resetForm() {
  form.value = {
    itemId: undefined,
    parentId: 0,
    itemName: undefined,
    itemType: '1',
    materialId: undefined,
    unit: '',
    price: 0,
    orderNum: 0,
    status: '0',
    remark: undefined
  }
  proxy.clearFormValidate('itemRef')
}

function handleAdd(row) {
  resetForm()
  loadMaterials()
  if (row && row.itemId) {
    form.value.parentId = row.itemId
    // 在「用药」下面加项目，类型默认跟着上级走
    form.value.itemType = row.itemType || '1'
  }
  title.value = '新增检查项目及用药'
  open.value = true
}

function handleUpdate(row) {
  resetForm()
  loadMaterials()
  getCatCheckItem(row.itemId).then(res => {
    const d = res.data || {}
    form.value = {
      itemId: d.itemId,
      parentId: d.parentId || 0,
      itemName: d.itemName,
      itemType: d.itemType || '1',
      materialId: d.materialId || undefined,
      unit: d.unit,
      price: d.price !== null && d.price !== undefined ? Number(d.price) : 0,
      orderNum: d.orderNum || 0,
      status: d.status || '0',
      remark: d.remark
    }
    title.value = '修改检查项目及用药'
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['itemRef'].validate(valid => {
    if (!valid) return
    const payload = { ...form.value }
    // 0 代表顶级；后端按 0 处理，这里保持数字类型
    payload.parentId = payload.parentId || 0
    if (payload.materialId === undefined) payload.materialId = null
    const request = payload.itemId ? updateCatCheckItem(payload) : addCatCheckItem(payload)
    request.then(() => {
      proxy.$modal.msgSuccess(payload.itemId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm(`确认删除「${row.itemName}」吗？删除后开单里就选不到它了，历史诊断不受影响。`).then(() => {
    return delCatCheckItem(row.itemId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.muted {
  color: var(--el-text-color-secondary);
}
/* 操作列三个按钮排一行：默认 .cell 会按 word-break 折行，这里强制不换行 */
:deep(.op-col .cell) {
  white-space: nowrap;
}
</style>
