<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="用户名称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="病房授权说明：助理账号只可查看被授权的病房及其中的猫咪档案。未授权任何病房的账号，仅可查看所属基地的公共信息。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 12px"
    />

    <el-table v-loading="loading" :data="userList">
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="用户ID" align="center" prop="userId" width="90" />
      <el-table-column label="登录账号" align="center" prop="userName" width="140" />
      <el-table-column label="用户昵称" align="center" prop="nickName" width="140" />
      <el-table-column label="手机号" align="center" prop="phonenumber" width="130" />
      <el-table-column label="已授权病房" align="center" min-width="260">
        <template #default="scope">
          <el-tag v-for="w in (authMap[scope.row.userId] || [])" :key="w.wardId" size="small" style="margin: 2px 4px 2px 0">
            {{ w.baseName ? w.baseName + ' / ' : '' }}{{ w.wardName }}
          </el-tag>
          <span v-if="!(authMap[scope.row.userId] || []).length" class="text-muted">未授权</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width" v-if="checkPermi(['cat:wardauth:edit', 'cat:wardauth:remove'])">
        <template #default="scope">
          <el-button link type="primary" icon="Setting" @click="handleAuth(scope.row)" v-hasPermi="['cat:wardauth:edit']">配置授权</el-button>
          <el-button link type="danger" icon="Delete" @click="handleClear(scope.row)" v-hasPermi="['cat:wardauth:remove']">清空</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getUserList" />

    <el-dialog :title="'配置病房授权 - ' + currentUser.nickName" v-model="open" width="640px" append-to-body>
      <div v-loading="authLoading">
        <el-checkbox-group v-model="checkedWardIds">
          <div v-for="group in wardGroups" :key="group.baseId" class="ward-group">
            <div class="ward-group-title">
              <el-checkbox :model-value="isGroupAllChecked(group)" :indeterminate="isGroupIndeterminate(group)" @change="val => toggleGroup(group, val)">
                {{ group.baseName }}
              </el-checkbox>
            </div>
            <div class="ward-group-body">
              <el-checkbox v-for="w in group.wards" :key="w.wardId" :label="w.wardId" :value="w.wardId">
                {{ w.wardName }}
              </el-checkbox>
            </div>
          </div>
        </el-checkbox-group>
        <el-empty v-if="wardGroups.length === 0" description="暂无病房数据，请先创建基地与病房" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAuth">保 存</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CatWardAuth">
import { listUser } from '@/api/system/user'
import { listCatWard } from '@/api/cat/ward'
import { listCatBase } from '@/api/cat/base'
import { getWardIdsByUser, saveWardAuth, delWardAuthByUser } from '@/api/cat/wardauth'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const authLoading = ref(false)
const showSearch = ref(true)
const userList = ref([])
const total = ref(0)
const open = ref(false)
const currentUser = ref({})
const checkedWardIds = ref([])
const authMap = ref({})
const wardGroups = ref([])
const wards = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined
  }
})

const { queryParams } = toRefs(data)

function getUserList() {
  loading.value = true
  const params = { ...queryParams.value, pageNum: queryParams.value.pageNum, pageSize: queryParams.value.pageSize }
  listUser(params).then(res => {
    userList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    userList.value.forEach(u => loadUserAuth(u))
  }).catch(() => {
    loading.value = false
  })
}

function loadUserAuth(user) {
  getWardIdsByUser(user.userId).then(res => {
    const ids = res.data || []
    authMap.value[user.userId] = wards.value.filter(w => ids.includes(w.wardId))
  })
}

function buildWardGroups() {
  const baseIdMap = {}
  wardGroups.value.forEach(g => (baseIdMap[g.baseId] = g.baseName))
  const map = {}
  wards.value.forEach(w => {
    const key = w.baseId || 0
    if (!map[key]) {
      map[key] = { baseId: key, baseName: baseIdMap[key] || w.baseName || '未分配基地', wards: [] }
    }
    map[key].wards.push(w)
  })
  wardGroups.value = Object.values(map)
}

function loadWards() {
  return Promise.all([
    listCatWard({ pageNum: 1, pageSize: 999 }),
    listCatBase({ pageNum: 1, pageSize: 999 })
  ]).then(([wardRes, baseRes]) => {
    const baseMap = {}
    ;(baseRes.rows || []).forEach(b => (baseMap[b.baseId] = b.baseName))
    wards.value = (wardRes.rows || []).map(w => ({ ...w, baseName: baseMap[w.baseId] || '' }))
    buildWardGroups()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getUserList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleAuth(row) {
  // 病房/基地可能在别的页签刚变化，打开授权弹窗前重拉，避免授权树缺新病房
  loadWards()
  currentUser.value = row
  open.value = true
  authLoading.value = true
  getWardIdsByUser(row.userId).then(res => {
    checkedWardIds.value = res.data || []
    authLoading.value = false
  }).catch(() => {
    authLoading.value = false
  })
}

function isGroupAllChecked(group) {
  return group.wards.length > 0 && group.wards.every(w => checkedWardIds.value.includes(w.wardId))
}

function isGroupIndeterminate(group) {
  const checked = group.wards.filter(w => checkedWardIds.value.includes(w.wardId)).length
  return checked > 0 && checked < group.wards.length
}

function toggleGroup(group, val) {
  const ids = group.wards.map(w => w.wardId)
  if (val) {
    checkedWardIds.value = Array.from(new Set([...checkedWardIds.value, ...ids]))
  } else {
    checkedWardIds.value = checkedWardIds.value.filter(id => !ids.includes(id))
  }
}

function submitAuth() {
  saveWardAuth({ userId: currentUser.value.userId, wardIds: checkedWardIds.value }).then(() => {
    proxy.$modal.msgSuccess('授权保存成功')
    open.value = false
    loadUserAuth(currentUser.value)
  })
}

function handleClear(row) {
  proxy.$modal.confirm('是否清空用户「' + row.nickName + '」的全部病房授权？').then(function () {
    return delWardAuthByUser(row.userId)
  }).then(() => {
    proxy.$modal.msgSuccess('已清空')
    authMap.value[row.userId] = []
    getUserList()
  }).catch(() => {})
}

loadWards().then(() => getUserList())
</script>

<style scoped>
.text-muted {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}
.ward-group {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  margin-bottom: 12px;
  overflow: hidden;
}
.ward-group-title {
  background: var(--el-fill-color-light);
  padding: 6px 12px;
  font-weight: 600;
}
.ward-group-body {
  padding: 10px 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px 18px;
}
</style>
