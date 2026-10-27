<template>
  <div class="mobile-home">
    <!-- 顶部：账号 + 未读概览 -->
    <div class="hero">
      <div class="hero-top">
        <div>
          <div class="hello">你好，{{ nickName || '同事' }}</div>
          <div class="hello-sub">{{ today }} · 救助站移动端</div>
        </div>
        <el-button link class="logout" @click="handleLogout">退出</el-button>
      </div>
      <div class="hero-stat">
        <div class="stat">
          <div class="num">{{ unreadCount }}</div>
          <div class="label">未读提醒</div>
        </div>
        <div class="stat">
          <div class="num">{{ reminders.length }}</div>
          <div class="label">可见提醒</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section">
      <div class="section-title">快捷操作</div>
      <div class="quick-grid">
        <div class="quick-item" @click="scanOpen = true">
          <el-icon><FullScreen /></el-icon>
          <span>扫码查档案</span>
        </div>
        <div class="quick-item" @click="goPc('/profile/profile')">
          <el-icon><Files /></el-icon>
          <span>猫咪档案</span>
        </div>
        <div class="quick-item" @click="goPc('/medical/medication')">
          <el-icon><FirstAidKit /></el-icon>
          <span>用药打卡</span>
        </div>
        <div class="quick-item" @click="goPc('/reminder/reminder')">
          <el-icon><Bell /></el-icon>
          <span>提醒中心</span>
        </div>
      </div>
    </div>

    <!-- 聚合提醒列表 -->
    <div class="section">
      <div class="section-title">
        <span>待办提醒</span>
        <span class="read-all" v-if="unreadCount > 0" @click="handleReadAll">全部已读</span>
      </div>
      <div v-if="loading" class="empty">加载中...</div>
      <el-empty v-else-if="!reminders.length" description="暂无待办提醒" :image-size="70" />
      <div v-else class="reminder-list">
        <div
          v-for="r in reminders"
          :key="r.reminderId"
          class="reminder-item"
          :class="{ read: isRead(r) }"
          @click="handleRead(r)"
        >
          <div class="r-left">
            <el-tag size="small" :type="typeTag(r.reminderType)" effect="light">{{ r.reminderType }}</el-tag>
          </div>
          <div class="r-main">
            <div class="r-title">
              <span class="dot" v-if="!isRead(r)"></span>{{ r.title }}
            </div>
            <div class="r-content">{{ r.content }}</div>
            <div class="r-time">{{ formatDate(r.remindDate) }}</div>
          </div>
          <div class="r-right">
            <span v-if="isRead(r)" class="read-tag">已读</span>
            <span v-else class="unread-tag">未读</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 扫码输入 -->
    <el-dialog v-model="scanOpen" title="扫码查看档案" width="90%" append-to-body>
      <el-input v-model="archiveNo" placeholder="请输入或粘贴档案编号，如 CAT202610250001" clearable />
      <div class="dialog-tip">现场用手机相机扫描笼子卡上的二维码可直接打开档案页</div>
      <template #footer>
        <el-button @click="scanOpen = false">取消</el-button>
        <el-button type="primary" @click="goArchive">查看档案</el-button>
      </template>
    </el-dialog>

    <!-- 底部导航 -->
    <div class="tabbar">
      <div class="tab active"><el-icon><HomeFilled /></el-icon><span>首页</span></div>
      <div class="tab" @click="goPc('/profile/profile')"><el-icon><Files /></el-icon><span>档案</span></div>
      <div class="tab" @click="goPc('/reminder/reminder')"><el-icon><Bell /></el-icon><span>提醒</span></div>
      <div class="tab" @click="goPc('/index')"><el-icon><DataBoard /></el-icon><span>看板</span></div>
    </div>
  </div>
</template>

<script setup name="CatMobileHome">
import { listMobileReminders, readMobileReminder, readAllMobileReminders } from "@/api/cat/mobile"
import useUserStore from '@/store/modules/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const reminders = ref([])
const unreadCount = ref(0)
const loading = ref(true)
const scanOpen = ref(false)
const archiveNo = ref('')

const nickName = computed(() => userStore.nickName || userStore.name)
const today = new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })

function isRead(r) {
  return r.status === '1' || !!r.readTime
}

const TYPE_TAGS = { 疫苗: 'primary', 驱虫: 'info', 转诊: 'danger', 库存: 'warning', 回访: 'success', 其他: '' }
function typeTag(t) { return TYPE_TAGS[t] || 'info' }

function formatDate(d) {
  return d ? String(d).replace('T', ' ').substring(0, 16) : ''
}

function loadReminders() {
  loading.value = true
  listMobileReminders().then(res => {
    reminders.value = res.data || []
    unreadCount.value = res.unreadCount || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleRead(r) {
  if (isRead(r)) return
  readMobileReminder(r.reminderId).then(() => {
    r.status = '1'
    r.readTime = new Date().toISOString()
    unreadCount.value = Math.max(unreadCount.value - 1, 0)
  })
}

function handleReadAll() {
  readAllMobileReminders().then(res => {
    reminders.value.forEach(r => { r.status = '1' })
    unreadCount.value = 0
    ElMessage.success(res.msg || '已全部标记为已读')
  })
}

function goArchive() {
  const no = (archiveNo.value || '').trim()
  if (!no) {
    ElMessage.warning('请输入档案编号')
    return
  }
  scanOpen.value = false
  router.push('/m/cat/' + encodeURIComponent(no))
}

// 跳转 PC 端页面（移动端自适应布局下同样可用）
function goPc(path) {
  router.push(path)
}

function handleLogout() {
  userStore.logOut().then(() => {
    router.push('/login')
  })
}

loadReminders()
</script>

<style scoped>
.mobile-home {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: 74px;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", sans-serif;
}
.hero {
  background: linear-gradient(135deg, #4a90d9, #6b5b95);
  color: #fff;
  padding: 22px 18px 26px;
  border-radius: 0 0 18px 18px;
}
.hero-top { display: flex; justify-content: space-between; align-items: flex-start; }
.hello { font-size: 19px; font-weight: 600; }
.hello-sub { font-size: 12px; opacity: 0.85; margin-top: 4px; }
.logout { color: #fff; opacity: 0.9; }
.hero-stat { display: flex; gap: 28px; margin-top: 18px; }
.hero-stat .num { font-size: 24px; font-weight: 700; line-height: 1.1; }
.hero-stat .label { font-size: 12px; opacity: 0.85; margin-top: 3px; }
.section {
  background: #fff;
  margin: 12px 12px 0;
  border-radius: 12px;
  padding: 14px 14px 16px;
}
.section-title {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 15px; font-weight: 600; color: #333;
  margin-bottom: 12px; padding-left: 8px; border-left: 3px solid #4a90d9;
}
.read-all { font-size: 12px; font-weight: 400; color: #4a90d9; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 12px 4px; border-radius: 10px; background: #f7f8fa;
  font-size: 12px; color: #555; cursor: pointer;
}
.quick-item:active { background: #eef2f8; }
.quick-item .el-icon { font-size: 22px; color: #4a90d9; }
.reminder-list { display: flex; flex-direction: column; gap: 8px; }
.reminder-item {
  display: flex; gap: 10px; padding: 10px 12px;
  background: #f7f8fa; border-radius: 10px; align-items: flex-start;
  border-left: 3px solid #f56c6c;
}
.reminder-item.read { border-left-color: #dcdfe6; opacity: 0.72; }
.r-main { flex: 1; min-width: 0; }
.r-title { font-size: 14px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 6px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: #f56c6c; flex-shrink: 0; }
.r-content { font-size: 12px; color: #606266; margin-top: 4px; line-height: 1.5; word-break: break-all; }
.r-time { font-size: 11px; color: #a8abb2; margin-top: 4px; }
.r-right { font-size: 11px; white-space: nowrap; }
.read-tag { color: #a8abb2; }
.unread-tag { color: #f56c6c; font-weight: 600; }
.empty { text-align: center; color: #999; padding: 24px 0; font-size: 13px; }
.dialog-tip { font-size: 12px; color: #909399; margin-top: 8px; }
.tabbar {
  position: fixed; left: 0; right: 0; bottom: 0;
  height: 56px; background: #fff; border-top: 1px solid #ebeef5;
  display: flex; align-items: center; justify-content: space-around;
}
.tab { display: flex; flex-direction: column; align-items: center; gap: 2px; font-size: 11px; color: #909399; }
.tab .el-icon { font-size: 20px; }
.tab.active { color: #4a90d9; }
</style>
