<template>
  <div>
    <el-popover ref="noticePopover" placement="bottom-end" :width="360" trigger="manual" v-model:visible="noticeVisible" popper-class="notice-popover">
      <!-- 弹出内容 -->
      <div class="notice-header">
        <span class="notice-title">提醒通知</span>
        <span v-if="unreadCount > 0" class="notice-mark-all" @click="markAllRead">全部已读</span>
      </div>
      <div v-if="noticeLoading" class="notice-loading">
        <el-icon class="is-loading"><Loading /></el-icon> 加载中...
      </div>
      <div v-else-if="noticeList.length === 0" class="notice-empty">
        <el-icon style="font-size:24px;display:block;margin-bottom:6px;"><Postcard /></el-icon>
        暂无提醒
      </div>
      <div v-else>
        <div v-for="item in noticeList" :key="item.reminderId" class="notice-item" :class="{ 'is-read': item.status === '1' }" @click="openReminder(item)">
          <el-tag size="small" :type="typeTag(item.reminderType)" class="notice-tag">{{ item.reminderType }}</el-tag>
          <span class="notice-item-title" :title="item.title">{{ item.content || item.title }}</span>
          <span class="notice-item-date">{{ shortDate(item.remindDate) }}</span>
        </div>
      </div>
      <div class="notice-footer" @click="goReminderPage">
        查看全部提醒
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- 触发器 -->
      <template #reference>
        <div class="right-menu-item hover-effect notice-trigger" @mouseenter="onNoticeEnter" @mouseleave="onNoticeLeave">
          <svg-icon icon-class="bell" />
          <span v-if="unreadCount > 0" class="notice-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup>
import { headerCatReminder, markReadCatReminder, markAllReadCatReminder } from '@/api/cat/reminder'

const noticePopover = ref(null)
const noticeList = ref([])
const unreadCount = ref(0)
const noticeLoading = ref(false)
const noticeVisible = ref(false)
const noticeLeaveTimer = ref(null)
const refreshTimer = ref(null)
const { proxy } = getCurrentInstance()
const router = useRouter()

// 提醒类型对应的标签颜色，与提醒列表页保持一致
function typeTag(type) {
  const map = { 疫苗: 'success', 驱虫: 'warning', 回访: 'primary', 库存: 'danger', 转诊: 'info' }
  return map[type] || 'info'
}

// 只显示月-日，节省顶栏横向空间
function shortDate(date) {
  if (!date) return ''
  const text = String(date)
  return text.length >= 10 ? text.substring(5, 10) : text
}

// 加载当前用户的提醒列表
function loadHeader() {
  noticeLoading.value = true
  headerCatReminder().then(res => {
    noticeList.value = res.data || []
    unreadCount.value = res.unreadCount !== undefined ? res.unreadCount : 0
  }).catch(() => {}).finally(() => {
    noticeLoading.value = false
  })
}

// 启动/停止静默轮询。
// 只在页面可见时轮询：用户把标签页挂在后台一整夜时，每分钟一次请求毫无意义，
// 只会白白消耗服务器资源（2026-10-30 排查时发现有个标签页整夜轮询了 5 小时）
function startRefreshTimer() {
  stopRefreshTimer()
  refreshTimer.value = setInterval(loadHeader, 60000)
}

function stopRefreshTimer() {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

// 切回页面时立刻补一次，避免看到过期的角标
function onVisibilityChange() {
  if (document.hidden) {
    stopRefreshTimer()
  } else {
    loadHeader()
    startRefreshTimer()
  }
}

onMounted(() => {
  loadHeader()
  startRefreshTimer()
  document.addEventListener('visibilitychange', onVisibilityChange)
})

onBeforeUnmount(() => {
  stopRefreshTimer()
  document.removeEventListener('visibilitychange', onVisibilityChange)
  clearTimeout(noticeLeaveTimer.value)
})

// 鼠标移入铃铛区域
function onNoticeEnter() {
  clearTimeout(noticeLeaveTimer.value)
  noticeVisible.value = true
  nextTick(() => {
    const popper = noticePopover.value?.popperRef?.contentRef
    if (popper && !popper._noticeBound) {
      popper._noticeBound = true
      popper.addEventListener('mouseenter', () => clearTimeout(noticeLeaveTimer.value))
      popper.addEventListener('mouseleave', () => {
        noticeLeaveTimer.value = setTimeout(() => { noticeVisible.value = false }, 100)
      })
    }
  })
}

// 鼠标离开铃铛区域
function onNoticeLeave() {
  noticeLeaveTimer.value = setTimeout(() => { noticeVisible.value = false }, 150)
}

// 点开某条提醒：先标记本人已读，再跳到提醒列表处理
function openReminder(item) {
  if (item.status !== '1') {
    markReadCatReminder(item.reminderId).catch(() => {})
    item.status = '1'
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  noticeVisible.value = false
  router.push('/reminder')
}

function goReminderPage() {
  noticeVisible.value = false
  router.push('/reminder')
}

// 全部已读（只影响当前登录账号）
function markAllRead() {
  markAllReadCatReminder().then(() => {
    noticeList.value = noticeList.value.map(n => ({ ...n, status: '1' }))
    unreadCount.value = 0
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.notice-trigger {
  position: relative;
  transform: translateX(-6px);
  .svg-icon { width: 1.2em; height: 1.2em; vertical-align: -0.2em; }
  .notice-badge {
    position: absolute;
    top: 7px;
    right: -3px;
    background: #f56c6c;
    color: #fff;
    border-radius: 10px;
    font-size: 10px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
    min-width: 16px;
    text-align: center;
    white-space: nowrap;
    pointer-events: none;
  }
}
.notice-popover { padding: 0 !important; }
.notice-popover .notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.notice-popover .notice-mark-all {
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: normal;
  cursor: pointer;
}
.notice-popover .notice-mark-all:hover { opacity: 0.8; }
.notice-popover .notice-loading,
.notice-popover .notice-empty {
  padding: 24px;
  text-align: center;
  color: var(--el-text-color-placeholder);
  font-size: 12px;
  line-height: 1.8;
}
.notice-popover .notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: background 0.15s;
}
.notice-popover .notice-item:last-child { border-bottom: none; }
.notice-popover .notice-item:hover { background: var(--el-fill-color-light); }
.notice-popover .notice-item.is-read .notice-tag,
.notice-popover .notice-item.is-read .notice-item-title,
.notice-popover .notice-item.is-read .notice-item-date { opacity: 0.45; filter: grayscale(1); }
.notice-popover .notice-tag { flex-shrink: 0; }
.notice-popover .notice-item-title {
  flex: 1;
  font-size: 12px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.notice-popover .notice-item-date {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}
.notice-popover .notice-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 14px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-lighter);
  font-size: 12px;
  color: var(--el-color-primary);
  cursor: pointer;
}
.notice-popover .notice-footer:hover { background: var(--el-fill-color-light); }
</style>
