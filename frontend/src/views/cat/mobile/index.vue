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
        <div class="info-item" v-if="profile.ageEstimate"><span class="label">年龄</span><span class="value">{{ profile.ageEstimate }}</span></div>
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
      <div class="section-title">病历时间线</div>
      <div class="timeline">
        <div class="timeline-item" v-for="(item, index) in timeline" :key="index">
          <div class="timeline-dot" :class="'type-' + item.type"></div>
          <div class="timeline-content">
            <div class="timeline-head">
              <span class="timeline-type">{{ item.type }}</span>
              <span class="timeline-time">{{ formatTime(item.time) }}</span>
            </div>
            <div class="timeline-text">{{ item.content }}</div>
            <div class="timeline-sub" v-if="item.doctor">操作人：{{ item.doctor }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty" v-if="!loading && timeline.length === 0">
      暂无病历记录
    </div>

    <!-- 加载中 -->
    <div class="loading" v-if="loading">加载中...</div>

    <!-- 未找到 -->
    <div class="loading" v-if="notFound">未找到该猫咪档案</div>
  </div>
</template>

<script setup name="CatMobile">
import { getCatMobileProfile } from "@/api/cat/mobile"

const route = useRoute()
const profile = ref({})
const timeline = ref([])
const loading = ref(true)
const notFound = ref(false)
const needLogin = ref(false)
// 头像：优先相册照片，取不到或加载失败时回退成名字首字
const avatarUrl = ref('')
const avatarBroken = ref(false)

const avatarSrc = computed(() => {
  if (avatarBroken.value || !avatarUrl.value) return ''
  const url = avatarUrl.value
  if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  const base = import.meta.env.VITE_APP_BASE_API
  return base + (url.startsWith('/') ? url : '/' + url)
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
      needLogin.value = response.data.needLogin === true
      avatarUrl.value = response.data.avatar || ''
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
  padding-bottom: 30px;
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
  justify-content: space-between;
  margin-bottom: 6px;
}

.timeline-type {
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.timeline-time {
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

.loading, .empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}
</style>
