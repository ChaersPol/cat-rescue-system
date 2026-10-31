<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
         <el-form-item label="所属基地" prop="baseId">
            <el-select v-model="queryParams.baseId" placeholder="全部基地" clearable filterable style="width: 180px">
               <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
            </el-select>
         </el-form-item>
         <el-form-item label="病房名称" prop="wardName">
            <el-input v-model="queryParams.wardName" placeholder="请输入病房名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="病房类型" prop="wardTypeId">
            <el-select v-model="queryParams.wardTypeId" placeholder="全部类型" clearable style="width: 160px">
               <el-option v-for="t in wardTypeList" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:ward:add']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" v-if="$route.meta.canRemove" :disabled="single" @click="handleUpdate" v-hasPermi="['cat:ward:edit']">修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" v-if="$route.meta.canRemove" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:ward:remove']">删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['cat:ward:export']">导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
         <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
         <el-table-column label="所属基地" align="center" prop="baseId" width="160" :show-overflow-tooltip="true">
            <template #default="scope">
               {{ baseLabel(scope.row.baseId) }}
            </template>
         </el-table-column>
         <el-table-column label="病房名称" align="center" prop="wardName" :show-overflow-tooltip="true" />
         <el-table-column label="病房类型" align="center" prop="wardType" :show-overflow-tooltip="true">
            <template #default="scope">
               <el-tag v-if="wardTypeLabel(scope.row.wardTypeId, scope.row.wardType)"
                       :type="wardTypeTag(scope.row.wardTypeId)" effect="light">
                  {{ wardTypeLabel(scope.row.wardTypeId, scope.row.wardType) }}
               </el-tag>
               <span v-else>-</span>
            </template>
         </el-table-column>
         <el-table-column label="显示顺序" align="center" prop="orderNum" :show-overflow-tooltip="true" />
         <el-table-column label="状态" align="center" prop="status">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-if="checkPermi(['cat:ward:edit', 'cat:ward:remove'])">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:ward:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:ward:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="所属基地" prop="baseId">
               <el-select v-model="form.baseId" placeholder="请选择基地" clearable filterable style="width: 100%">
                  <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
               </el-select>
            </el-form-item>
            <el-form-item label="病房名称" prop="wardName">
               <el-input v-model="form.wardName" placeholder="请输入病房名称" />
            </el-form-item>
            <el-form-item label="病房类型" prop="wardTypeId">
               <el-select v-model="form.wardTypeId" placeholder="请选择病房类型" filterable style="width: 100%">
                  <el-option v-for="t in wardTypeList" :key="t.typeId" :label="t.typeName" :value="t.typeId">
                     <el-tag :type="t.tagType || 'primary'" effect="light" size="small">{{ t.typeName }}</el-tag>
                  </el-option>
               </el-select>
               <div class="form-tip" v-if="checkPermi(['cat:wardtype:list'])">
                  没有想要的类型？去
                  <el-link type="primary" :underline="false" @click="gotoWardType">基础信息 → 病房类型</el-link>
                  里维护
               </div>
            </el-form-item>
            <el-form-item label="显示顺序" prop="orderNum">
               <el-input-number v-model="form.orderNum" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
               <el-radio-group v-model="form.status">
                  <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
               </el-radio-group>
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

<script setup name="CatWard">
import { useBaseCache, useWardTypeCache, invalidateMasterData, MASTER_KIND } from "@/utils/catCache"
import { listCatWard, getCatWard, delCatWard, addCatWard, updateCatWard } from "@/api/cat/ward"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')
const { baseMap, baseLabel, reloadBaseCache } = useBaseCache()
const baseOptions = computed(() => Object.values(baseMap.value))
// 病房类型来自「基础信息 → 病房类型」字典，不再写死在下拉里
const { wardTypeList, wardTypeLabel, wardTypeTag, reloadWardTypeCache } = useWardTypeCache()

const list = ref([])
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
    baseId: undefined,
    wardName: undefined,
    wardTypeId: undefined,
    orderNum: undefined,
    status: undefined,
  },
  rules: {
    baseId: [{ required: true, message: '请选择所属基地', trigger: 'change' }],
    wardName: [{ required: true, message: '请输入病房名称', trigger: 'blur' }],
    wardTypeId: [{ required: true, message: '请选择病房类型', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCatWard(queryParams.value).then(response => {
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
    baseId: undefined,
    wardName: undefined,
    wardTypeId: undefined,
    orderNum: undefined,
    status: undefined,
  }
  proxy.clearFormValidate("formRef")
}

/** 跳到病房类型维护页（没有该权限时不显示入口） */
function gotoWardType() {
  proxy.$router.push({ path: '/base/wardtype' })
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
  ids.value = selection.map(item => item.wardId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  // 基地/病房类型可能在别的页签刚新增，打开弹窗前重拉
  reloadBaseCache()
  reloadWardTypeCache()
  reset()
  open.value = true
  title.value = "添加病房"
}

function handleUpdate(row) {
  reloadBaseCache()
  reloadWardTypeCache()
  reset()
  const id = row.wardId || ids.value
  getCatWard(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改病房"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.wardId != undefined) {
        updateCatWard(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          // 其它页面的病房下拉/名称翻译立即刷新
          invalidateMasterData(MASTER_KIND.WARD)
          getList()
        })
      } else {
        addCatWard(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          invalidateMasterData(MASTER_KIND.WARD)
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row.wardId || ids.value
  proxy.$modal.confirm('是否确认删除该数据？').then(function () {
    return delCatWard(delIds)
  }).then(() => {
    invalidateMasterData(MASTER_KIND.WARD)
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("cat/ward/export", { ...queryParams.value }, `ward_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}
</style>
