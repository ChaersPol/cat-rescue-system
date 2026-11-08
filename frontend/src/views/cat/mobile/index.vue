<template>
  <div class="mobile-profile">
    <!-- 顶部档案卡片 -->
    <div class="profile-header">
      <div class="profile-avatar">
        <img v-if="avatarSrc" :src="avatarSrc" alt="" @error="avatarBroken = true" />
        <span v-else>{{ profile.catName ? profile.catName.charAt(0) : '猫' }}</span>
      </div>
      <div class="profile-info">
        <div class="profile-name">{{ profile.catName || '未知猫咪' }}</div>
        <div class="profile-meta">
          <span class="meta-tag" v-if="profile.breed">{{ profile.breed }}</span>
          <span class="meta-tag" v-if="profile.sex !== undefined && profile.sex !== null">{{ sexLabel }}</span>
          <span class="meta-tag status-tag">{{ statusLabel }}</span>
        </div>
        <div class="profile-no">档案编号：{{ profile.archiveNo }}</div>
      </div>
    </div>

    <!-- 基础信息 -->
    <div class="section" v-if="!loading">
      <div class="section-title">基础信息</div>
      <div class="info-grid">
        <div class="info-item" v-if="profile.alias"><span class="label">别名</span><span class="value">{{ profile.alias }}</span></div>
        <div class="info-item" v-if="profile.ageText || profile.ageEstimate"><span class="label">年龄</span><span class="value">{{ profile.ageText || profile.ageEstimate }}</span></div>
        <div class="info-item" v-if="profile.weight"><span class="label">体重</span><span class="value">{{ profile.weight }}</span></div>
        <div class="info-item" v-if="profile.personality"><span class="label">性格</span><span class="value">{{ profile.personality }}</span></div>
        <div class="info-item" v-if="profile.capturePoint"><span class="label">抓捕点位</span><span class="value">{{ profile.capturePoint }}</span></div>
        <div class="info-item" v-if="profile.feature"><span class="label">特征</span><span class="value">{{ profile.feature }}</span></div>
        <div class="info-item" v-if="profile.remark"><span class="label">备注</span><span class="value">{{ profile.remark }}</span></div>
      </div>
    </div>

    <!-- 公开模式提示 -->
    <div class="section" v-if="!loading && needLogin">
      <div class="section-title">医疗记录</div>
      <div class="login-tip">
        <p>当前为公开扫码模式，仅展示猫咪基础信息。</p>
        <p>疫苗、驱虫、诊断、用药等医疗病历需登录救助站管理系统后查看。</p>
      </div>
    </div>

    <!-- 病历时间线 -->
    <div class="section" v-if="!loading && timeline.length > 0">
      <div class="section-head">
        <div class="section-title">病历时间线</div>
        <span class="filter-btn" v-if="flows.length" @click="filterOpen = true">
          {{ activeFlow ? '截至 ' + shortTime(activeFlow.operateTime) : '全部病历' }}
          <el-icon><ArrowDown /></el-icon>
        </span>
      </div>

      <!-- 选了某次流转：说清这一段病历是在哪个基地产生的 -->
      <div class="stage-tip" v-if="activeFlow">
        <div class="stage-main">
          正在看 <b>{{ shortTime(activeFlow.operateTime) }} {{ activeFlow.flowType || '流转' }}</b> 之前的
          {{ filteredTimeline.length }} 条病历
        </div>
        <div class="stage-sub">这一阶段产生于 {{ stageBaseName }}</div>
        <span class="stage-reset" @click="flowFilter = ''">显示全部</span>
      </div>

      <div class="timeline" v-if="filteredTimeline.length">
        <div class="timeline-item" v-for="(item, index) in filteredTimeline" :key="index">
          <div class="timeline-dot" :class="'type-' + item.type"></div>
          <div class="timeline-content">
            <div class="timeline-head">
              <span class="timeline-type">{{ item.type }}</span>
              <span class="timeline-place" v-if="item.baseName">{{ item.baseName }}</span>
              <span class="timeline-time">{{ formatTime(item.time) }}</span>
            </div>
            <div class="timeline-text">{{ item.content }}</div>
            <div class="timeline-sub" v-if="item.doctor">操作人：{{ item.doctor }}</div>
            <!-- 流转记录：一键切到「这次流转之前」，不用再翻开筛选抽屉 -->
            <div class="timeline-link" v-if="item.flowId" @click="pickFlow(item.flowId)">看此前病历 ›</div>
          </div>
        </div>
      </div>
      <div class="stage-empty" v-else>这一阶段没有病历记录</div>
    </div>

    <!-- 空状态 -->
    <div class="empty" v-if="!loading && timeline.length === 0">
      暂无病历记录
    </div>

    <!-- 加载中 -->
    <div class="loading" v-if="loading">加载中...</div>

    <!-- 未找到 -->
    <div class="loading" v-if="notFound">未找到该猫咪档案</div>

    <!-- 底部操作条：扫码现场直接用，不用再退出去找菜单 -->
    <div class="quick-bar">
      <el-button class="quick-edit" icon="Edit" @click="goEdit">修改档案</el-button>
      <el-button type="primary" class="quick-main" icon="Plus" @click="openQuick">快速记一笔</el-button>
    </div>

    <!-- 快速记一笔：按角色显示可选项 -->
    <el-drawer v-model="quickOpen" direction="btt" size="340px" title="快速记一笔">
      <div class="quick-account" v-if="loggedIn">当前账号：{{ staffName }}<span v-if="roleLabel">（{{ roleLabel }}）</span></div>
      <div class="quick-account muted" v-else>还没登录，点下面任意一项会先跳登录，登录后自动进入要记的页面</div>
      <div class="quick-list">
        <div class="quick-item" v-for="a in quickActions" :key="a.key" @click="goAction(a)">
          <el-icon class="quick-icon"><component :is="a.icon" /></el-icon>
          <span>{{ a.label }}</span>
        </div>
      </div>
      <div class="quick-foot">
        记录会自动关联到 <b>{{ profile.catName || '这只猫' }}</b>（{{ profile.archiveNo }}）
      </div>
    </el-drawer>

    <!-- 按流转轨迹筛选：手机上用底部抽屉选，比下拉框好点 -->
    <el-drawer v-model="filterOpen" direction="btt" size="68%" title="按流转轨迹筛选">
      <div class="flow-list">
        <div class="flow-item" :class="{ active: !flowFilter }" @click="pickFlow('')">
          <div class="flow-head">
            <span class="flow-title">全部病历</span>
            <span class="flow-count">{{ timeline.length }} 条</span>
          </div>
          <div class="flow-sub">不按阶段截断，从最新看到最早</div>
        </div>
        <div
          class="flow-item"
          v-for="f in flows"
          :key="f.flowId"
          :class="{ active: String(flowFilter) === String(f.flowId) }"
          @click="pickFlow(f.flowId)"
        >
          <div class="flow-head">
            <span class="flow-title">{{ shortTime(f.operateTime) }} · {{ f.flowType || '流转' }}</span>
            <span class="flow-count">{{ countBefore(f) }} 条</span>
          </div>
          <div class="flow-sub" v-if="f.flowReason">{{ f.flowReason }}</div>
          <div class="flow-sub" v-if="flowPathText(f)">{{ flowPathText(f) }}</div>
        </div>
      </div>
      <div class="flow-foot">选一条流转，看它之前的病历（这一阶段病例都产生于同一个基地）</div>
    </el-drawer>

    <!-- 手机相机：现场直接拍照上传，不做二次选择 -->
    <input ref="cameraInput" type="file" accept="image/*" capture="environment" class="hidden-input" @change="onCameraChange" />
  </div>
</template>

<script setup name="CatMobile">
import { getCatMobileProfile } from "@/api/cat/mobile"
import { addCatPhoto } from "@/api/cat/photo"
import request from "@/utils/request"
import { resolveFileUrl } from "@/utils/fileUrl"
import { ElMessage } from "element-plus"
import { FirstAidKit, Sugar, Stopwatch, Opportunity, Camera, ArrowDown } from "@element-plus/icons-vue"

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const profile = ref({})
const timeline = ref([])
const loading = ref(true)
const notFound = ref(false)
const needLogin = ref(false)
// 当前扫码人：是否已登录、什么岗位（后端按 token 解析，匿名扫码为 false）
const loggedIn = ref(false)
const staffName = ref('')
const roles = ref([])
const catId = ref(null)
const quickOpen = ref(false)
const cameraInput = ref(null)
const uploading = ref(false)
// 头像：优先相册照片，取不到或加载失败时回退成名字首字
const avatarUrl = ref('')
const avatarBroken = ref(false)

const avatarSrc = computed(() => {
  if (avatarBroken.value || !avatarUrl.value) return ''
  // 库里存相对路径，统一交给 resolveFileUrl 拼接口前缀 + 编码中文名
  return resolveFileUrl(avatarUrl.value)
})

const sexLabel = computed(() => {
  const map = { '0': '公', '1': '母', '2': '未知' }
  return map[profile.value.sex] || '未知'
})

const statusLabel = computed(() => {
  const map = { '1': '在养', '2': '基地内治疗中', '3': '外送医院治疗中', '4': '已放归', '5': '已领养', '6': '已离世', '7': 'TNR治疗中', '8': '姑息照料' }
  return map[profile.value.status] || '未知'
})

function formatTime(time) {
  if (!time) return ''
  return String(time).replace('T', ' ').substring(0, 16)
}

/** 筛选按钮/抽屉里的短时间：09-28 20:00（手机上一行放得下） */
function shortTime(time) {
  const s = formatTime(time)
  return s.length >= 16 ? s.substring(5) : s
}

// ---------- 按流转轨迹筛选病历时间线（和电脑端档案页同一套口径）----------
const flows = ref([])
// '' = 全部病历；否则是某次流转的 flowId
const flowFilter = ref('')
const filterOpen = ref(false)

const activeFlow = computed(() => {
  if (!flowFilter.value) return null
  return flows.value.find(f => String(f.flowId) === String(flowFilter.value)) || null
})

/** 选中的这次流转之前，猫咪在哪个基地（转出基地就是这一阶段病历产生的地方） */
const stageBaseName = computed(() => {
  const f = activeFlow.value
  if (!f) return ''
  return f.fromBaseName || f.toBaseName || '未记录基地'
})

/** 时间线截断：只看这次流转时间点之前的病历 */
const filteredTimeline = computed(() => {
  const f = activeFlow.value
  if (!f) return timeline.value
  const limit = timeOf(f.operateTime)
  if (limit === null) return timeline.value
  return timeline.value.filter(item => {
    const t = timeOf(item.time)
    // 没有时间的记录在阶段视图里不出现，避免归错阶段
    return t !== null && t <= limit
  })
})

function timeOf(value) {
  if (!value) return null
  if (typeof value === 'number') return value
  const t = new Date(String(value).replace(' ', 'T')).getTime()
  return isNaN(t) ? null : t
}

/** 某个流转节点之前的病历条数（抽屉里先给个预期，别点进去才发现是空的） */
function countBefore(flow) {
  const limit = timeOf(flow.operateTime)
  if (limit === null) return timeline.value.length
  return timeline.value.filter(item => {
    const t = timeOf(item.time)
    return t !== null && t <= limit
  }).length
}

/** 流转的「从哪到哪」 */
function flowPathText(flow) {
  if (flow.fromBaseName && flow.toBaseName && flow.fromBaseName !== flow.toBaseName) {
    return `${flow.fromBaseName} → ${flow.toBaseName}`
  }
  return flow.fromBaseName || flow.toBaseName || ''
}

function pickFlow(flowId) {
  filterOpen.value = false
  // 点的还是当前这一阶段：没有变化，不重复提示
  if (String(flowFilter.value) === String(flowId)) return
  flowFilter.value = flowId
  if (!flowId) return
  // 手机上时间线会整段重排，给个 Toast 说清切到了哪一段、有几条，避免以为没反应；
  // 切换阶段时先收掉上一条，避免连点堆一串
  ElMessage.closeAll()
  const flow = activeFlow.value
  const stage = flow ? `「${shortTime(flow.operateTime)} ${flow.flowType || '流转'}」之前` : '所选阶段之前'
  const count = filteredTimeline.value.length
  if (count > 0) {
    ElMessage.success(`已切换到${stage}的 ${count} 条病历`)
  } else {
    ElMessage.warning(`${stage}没有病历记录`)
  }
}

// ---------- 快速记一笔 ----------
const isManagerRole = computed(() => roles.value.includes('admin') || roles.value.includes('manager'))
const isDoctor = computed(() => isManagerRole.value || roles.value.includes('doctor'))
const isAssistant = computed(() => isManagerRole.value || roles.value.includes('assistant'))

const roleLabel = computed(() => {
  if (isManagerRole.value) return '管理员'
  if (roles.value.includes('doctor')) return '医生'
  if (roles.value.includes('assistant')) return '助理'
  return ''
})

// 医生：诊断 / 用药；助理：用药打卡 / 疫苗 / 驱虫；管理岗全部。拍照所有人都能拍
const quickActions = computed(() => {
  const map = new Map()
  if (isDoctor.value) {
    map.set('diagnosis', { key: 'diagnosis', label: '开诊断', icon: FirstAidKit, path: '/medical/diagnosis' })
    map.set('medication', { key: 'medication', label: '记用药', icon: Sugar, path: '/medical/medication' })
  }
  if (isAssistant.value) {
    if (!map.has('medication')) {
      map.set('medication', { key: 'medication', label: '用药打卡', icon: Sugar, path: '/medical/medication' })
    }
    map.set('vaccine', { key: 'vaccine', label: '打疫苗', icon: Stopwatch, path: '/medical/vaccine' })
    map.set('deworm', { key: 'deworm', label: '驱虫', icon: Opportunity, path: '/medical/deworm' })
  }
  map.set('photo', { key: 'photo', label: '拍照上传', icon: Camera })
  return Array.from(map.values())
})

function openQuick() {
  if (!loggedIn.value) {
    // 未登录：先去登录，登录后自动回到这只猫的页面，接着记
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  quickOpen.value = true
}

function goAction(a) {
  if (a.key === 'photo') {
    if (!loggedIn.value) {
      router.push({ path: '/login', query: { redirect: route.fullPath } })
      return
    }
    if (cameraInput.value) cameraInput.value.click()
    return
  }
  const target = { path: a.path, query: { add: 1, catId: catId.value } }
  if (!loggedIn.value) {
    // 带着目标页去登录（只传路径，查询参数登录回来再拼），登录完直接落到要记的页面
    router.push({ path: '/login', query: { redirect: route.fullPath, next: a.path } })
    return
  }
  quickOpen.value = false
  router.push(target)
}

function goEdit() {
  if (!catId.value) return
  // 直接落到「猫咪档案」页并打开这只猫的编辑表单；未登录时 permission 会自动跳登录，
  // 登录完成后带着 redirect 回到同一个地址，表单直接弹出来，不用再自己找
  router.push({ path: '/profile/profile', query: { editCatId: catId.value } })
}

// 手机相机拍照 → 直接上传并挂到这只猫的相册
function onCameraChange(e) {
  const file = e.target.files && e.target.files[0]
  e.target.value = ''
  if (!file) return
  if (!loggedIn.value) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  uploading.value = true
  const form = new FormData()
  form.append('file', file)
  request({
    url: '/common/upload',
    method: 'post',
    data: form,
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => {
    const url = res.url || res.fileName
    if (!url) throw new Error('上传失败')
    return addCatPhoto({ catId: catId.value, folder: '现场拍照', fileType: '1', fileUrl: url, remark: '手机扫码拍摄' })
  }).then(() => {
    proxy.$modal.msgSuccess('照片已上传到这只猫的相册')
    getData()
  }).catch(err => {
    proxy.$modal.msgError(err.message || '上传失败')
  }).finally(() => {
    uploading.value = false
  })
}

function getData() {
  const archiveNo = route.params.archiveNo
  if (!archiveNo) {
    notFound.value = true
    loading.value = false
    return
  }
  getCatMobileProfile(archiveNo).then(response => {
    if (response.code === 200) {
      profile.value = response.data.profile || {}
      timeline.value = response.data.timeline || []
      flows.value = response.data.flows || []
      // 重新拉数据后，之前筛选的那次流转可能已经不在列表里了（极少见），清掉避免时间线是空的
      if (flowFilter.value && !flows.value.some(f => String(f.flowId) === String(flowFilter.value))) {
        flowFilter.value = ''
      }
      needLogin.value = response.data.needLogin === true
      avatarUrl.value = response.data.avatar || ''
      loggedIn.value = response.data.loggedIn === true
      staffName.value = response.data.staffName || ''
      roles.value = response.data.roles || []
      catId.value = response.data.catId || null
      // 登录前点了某个记录入口：登录完直接进入那个页面并打开新增，不用再找
      const next = route.query.next
      if (next && loggedIn.value) {
        router.replace({ path: String(next), query: { add: 1, catId: catId.value } })
        return
      }
    } else {
      notFound.value = true
    }
    loading.value = false
  }).catch(() => {
    notFound.value = true
    loading.value = false
  })
}

getData()
</script>

<style scoped>
.mobile-profile {
  min-height: 100vh;
  background: #f5f6f8;
  /* 给底部固定操作条留位置，避免最后一条内容被挡住 */
  padding-bottom: 92px;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", sans-serif;
}

.profile-header {
  background: linear-gradient(135deg, #4a90d9, #6b5b95);
  padding: 24px 20px;
  display: flex;
  align-items: center;
  color: #fff;
}

.profile-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 16px;
  flex-shrink: 0;
  overflow: hidden;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.profile-name {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 6px;
}

.profile-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
}

.meta-tag {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.status-tag {
  background: rgba(255, 255, 255, 0.35);
  font-weight: 600;
}

.profile-no {
  font-size: 12px;
  opacity: 0.85;
}

.section {
  background: #fff;
  margin: 12px 12px 0;
  border-radius: 12px;
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #4a90d9;
}

/* 标题 + 右侧筛选按钮同一行 */
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}
.section-head .section-title {
  margin-bottom: 0;
}
.filter-btn {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex: 0 0 auto;
  max-width: 58%;
  padding: 4px 10px;
  border-radius: 12px;
  background: #eef4fc;
  color: #4a90d9;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.filter-btn:active {
  background: #e0ecfa;
}

/* 阶段说明：这一段病历是在哪个基地产生的 */
.stage-tip {
  position: relative;
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #eef4fc;
  border: 1px solid #d9e7f8;
  font-size: 12px;
  line-height: 1.6;
  color: #4a6b8a;
}
.stage-main b {
  color: #2f5d8a;
}
.stage-sub {
  color: #7d8b99;
}
.stage-reset {
  position: absolute;
  right: 10px;
  top: 10px;
  font-size: 12px;
  color: #4a90d9;
}
.stage-empty {
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 20px 0;
}

/* 流转筛选抽屉 */
.flow-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.flow-item {
  padding: 12px;
  border-radius: 10px;
  background: #f5f7fa;
  border: 1px solid #eef0f3;
}
.flow-item.active {
  background: #eaf1fb;
  border-color: #9cc0e8;
}
.flow-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}
.flow-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.flow-item.active .flow-title {
  color: #2f5d8a;
}
.flow-count {
  flex: 0 0 auto;
  font-size: 12px;
  color: #999;
}
.flow-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #8a94a1;
  line-height: 1.5;
  word-break: break-all;
}
.flow-foot {
  margin-top: 14px;
  font-size: 12px;
  color: #999;
  line-height: 1.6;
}

.login-tip {
  font-size: 13px;
  color: #888;
  line-height: 1.8;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
  font-size: 13px;
}

.info-item .label {
  color: #999;
  font-size: 12px;
  margin-bottom: 2px;
}

.info-item .value {
  color: #333;
  word-break: break-all;
}

.timeline {
  position: relative;
  padding-left: 20px;
}

.timeline-item {
  position: relative;
  padding-bottom: 16px;
}

.timeline-dot {
  position: absolute;
  left: -20px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #4a90d9;
}

.timeline-dot.type-诊断 { background: #e6a23c; }
.timeline-dot.type-用药 { background: #67c23a; }
.timeline-dot.type-疫苗 { background: #409eff; }
.timeline-dot.type-驱虫 { background: #909399; }
.timeline-dot.type-转诊 { background: #f56c6c; }
.timeline-dot.type-流转 { background: #6b5b95; }
.timeline-dot.type-死亡 { background: #333; }

.timeline-content {
  background: #f9fafb;
  border-radius: 8px;
  padding: 10px 12px;
}

.timeline-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
}

.timeline-type {
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.timeline-place {
  font-size: 11px;
  color: #7d8b99;
  background: #eef0f3;
  border-radius: 8px;
  padding: 1px 6px;
}

.timeline-time {
  margin-left: auto;
  color: #999;
  font-size: 12px;
}

.timeline-text {
  color: #555;
  font-size: 13px;
  line-height: 1.5;
}

.timeline-sub {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.timeline-link {
  margin-top: 6px;
  font-size: 12px;
  color: #4a90d9;
}

.loading, .empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}

/* 底部操作条 */
.quick-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  display: flex;
  gap: 10px;
  padding: 10px 14px calc(10px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid #ececec;
  backdrop-filter: blur(6px);
}
.quick-bar .el-button {
  margin-left: 0;
  height: 42px;
  border-radius: 21px;
}
.quick-edit {
  flex: 0 0 116px;
}
.quick-main {
  flex: 1 1 auto;
  font-weight: 600;
}

/* 快速记一笔抽屉 */
.quick-account {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}
.quick-account.muted {
  color: #999;
}
.quick-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.quick-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 12px;
  border-radius: 10px;
  background: #f5f7fa;
  border: 1px solid #eef0f3;
  font-size: 15px;
  color: #333;
  cursor: pointer;
}
.quick-item:active {
  background: #eaf1fb;
  border-color: #cfe0f5;
}
.quick-icon {
  font-size: 18px;
  color: #4a90d9;
}
.quick-foot {
  margin-top: 14px;
  font-size: 12px;
  color: #999;
}
.hidden-input {
  display: none;
}
</style>
