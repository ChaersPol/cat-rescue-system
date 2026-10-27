<template>
  <div class="app-container dashboard">
    <!-- 页头 -->
    <div class="dash-head">
      <div class="dash-head-left">
        <span class="dash-title">救助站总览</span>
        <span v-if="updatedAt" class="dash-time">数据更新于 {{ updatedAt }}</span>
      </div>
      <el-button size="small" icon="Refresh" :loading="loading" @click="getStats">刷新数据</el-button>
    </div>

    <!-- 一、核心指标 -->
    <el-row :gutter="12">
      <el-col :xs="12" :sm="8" :md="4" v-for="card in cards" :key="card.label">
        <div
          class="stat-card clickable"
          :style="{ '--accent': card.color }"
          :title="`点击查看「${card.label}」的猫咪档案`"
          @click="goPage(card)"
        >
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">
            {{ card.value }}<span class="stat-unit">只</span>
          </div>
          <div class="stat-sub">{{ card.sub }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 二、今日待办（放在最前面，进来就知道要做什么） -->
    <div class="sec-head">
      <span class="sec-title">待办与提醒</span>
      <span class="sec-hint">
        {{ todoTotal > 0 ? `共 ${todoTotal} 项待处理，点击卡片直达对应列表` : '暂无待处理事项，点击卡片可查看历史记录' }}
      </span>
    </div>
    <el-row :gutter="12">
      <el-col :xs="12" :sm="8" :md="4" v-for="item in todoList" :key="item.label">
        <div
          class="todo-item"
          :class="{ 'is-zero': !item.value }"
          :style="{ '--accent': item.color }"
          :title="`点击进入「${item.label}」列表`"
          @click="goPage(item)"
        >
          <div class="todo-title">
            <span>{{ item.label }}</span>
            <el-icon class="todo-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="todo-value" :style="{ color: item.value > 0 ? item.color : 'var(--el-text-color-placeholder)' }">
            {{ item.value }}<span class="stat-unit">项</span>
          </div>
          <div class="todo-desc">{{ item.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 三、猫咪分布 -->
    <el-row :gutter="16" class="dash-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>猫咪状态分布</span>
              <span class="tip">点击扇区查看该状态的档案</span>
            </div>
          </template>
          <div v-if="statusData.length" ref="statusChartRef" class="chart-box"></div>
          <el-empty v-else description="暂无档案数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>各基地猫咪数量</span>
              <span class="tip">点击柱子查看该基地的档案</span>
            </div>
          </template>
          <div v-if="baseList.length" ref="baseChartRef" class="chart-box"></div>
          <el-empty v-else description="暂无基地数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 四、支出与标签 -->
    <el-row :gutter="16" class="dash-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>支出结构</span>
              <span class="tip">
                本月 ¥{{ formatMoney(stats.monthExpense) }} · 累计 ¥{{ formatMoney(stats.totalExpense) }}
              </span>
            </div>
          </template>
          <div ref="expenseChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>标签统计（慢性病等猫咪总数）</span>
              <span class="tip">点击标签直达档案列表</span>
            </div>
          </template>
          <el-empty v-if="!tagCount.length" description="暂无标签数据" :image-size="60" />
          <div v-else class="chart-box tag-wrap">
            <div class="tag-stat">
              <div class="tag-item" v-for="t in tagCount" :key="t.tagId" @click="goTag(t)">
                <el-tag size="small" type="danger" effect="light">{{ t.tagName }}</el-tag>
                <span class="tag-count">{{ t.count }}</span>
                <span class="tag-group">{{ t.tagGroup || '—' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="CatDashboard">
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/cat/dashboard'

const stats = ref({})
const loading = ref(false)
const updatedAt = ref('')
const statusChartRef = ref(null)
const baseChartRef = ref(null)
const expenseChartRef = ref(null)
const cards = ref([])
const router = useRouter()

let statusChart = null
let baseChart = null
let expenseChart = null

/** 状态名 → cat_status 字典值，用于点击跳转档案列表 */
const STATUS_MAP = {
  在养: '1',
  基地内治疗中: '2',
  外送医院治疗中: '3',
  已放归: '4',
  已领养: '5',
  已离世: '6',
  TNR治疗中: '7',
  姑息照料: '8'
}

/** 状态配色固定，避免过滤 0 值后颜色错位 */
const STATUS_COLORS = {
  在养: '#67C23A',
  基地内治疗中: '#E6A23C',
  外送医院治疗中: '#F56C6C',
  已放归: '#00BCD4',
  已领养: '#8E44AD',
  已离世: '#909399',
  TNR治疗中: '#722ED1',
  姑息照料: '#B1A0C7'
}

/** 支出类型 → cat_expense_type 字典值 */
const EXPENSE_TYPES = [
  { name: '医疗外送', value: '2' },
  { name: '物资采购', value: '1' },
  { name: '其他支出', value: '3' }
]

function formatMoney(n) {
  const v = Number(n || 0)
  return v.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

/** 顶部统计卡 → 对应列表页 */
function buildCards() {
  const sc = stats.value.statusCount || {}
  const total = stats.value.totalCats || 0
  const pct = (n) => (total > 0 ? `占 ${Math.round((n / total) * 100)}%` : '暂无档案')
  cards.value = [
    { label: '猫咪总数', value: total, color: '#409EFF', sub: '全部在册档案', path: '/profile/profile', query: {} },
    { label: '在养', value: sc['在养'] || 0, color: '#67C23A', sub: pct(sc['在养'] || 0), path: '/profile/profile', query: { status: '1' } },
    { label: '治疗中(基地)', value: sc['基地内治疗中'] || 0, color: '#E6A23C', sub: pct(sc['基地内治疗中'] || 0), path: '/profile/profile', query: { status: '2' } },
    { label: '治疗中(外送)', value: sc['外送医院治疗中'] || 0, color: '#F56C6C', sub: pct(sc['外送医院治疗中'] || 0), path: '/profile/profile', query: { status: '3' } },
    { label: '已领养', value: sc['已领养'] || 0, color: '#8E44AD', sub: pct(sc['已领养'] || 0), path: '/profile/profile', query: { status: '5' } },
    { label: '已放归', value: sc['已放归'] || 0, color: '#00BCD4', sub: pct(sc['已放归'] || 0), path: '/profile/profile', query: { status: '4' } }
  ]
}

/** 待办事项 → 对应业务列表页，带筛选条件直达 */
const todoList = computed(() => [
  { label: '待疫苗', value: stats.value.dueVaccine || 0, color: '#409EFF', desc: '7 天内到期或已过期', path: '/medical/vaccine', query: {} },
  { label: '待驱虫', value: stats.value.dueDeworm || 0, color: '#722ED1', desc: '7 天内到期或已过期', path: '/medical/deworm', query: {} },
  { label: '待回访', value: stats.value.pendingVisit || 0, color: '#E6A23C', desc: '查看待回访计划', path: '/adopt/visit', query: { visitStatus: '0' } },
  { label: '回访异常', value: stats.value.visitAbnormal || 0, color: '#F56C6C', desc: '需要跟进处理', path: '/adopt/visit', query: { visitStatus: '2' } },
  { label: '外送治疗中', value: stats.value.referralCount || 0, color: '#D4380D', desc: '查看在院转诊记录', path: '/medical/referral', query: { status: '0' } },
  { label: '累计离世', value: stats.value.deathCount || 0, color: '#909399', desc: '查看死亡台账', path: '/medical/death', query: {} }
])

const todoTotal = computed(() =>
  ['dueVaccine', 'dueDeworm', 'pendingVisit', 'visitAbnormal', 'referralCount'].reduce(
    (sum, k) => sum + Number(stats.value[k] || 0),
    0
  )
)

/** 状态分布数据：过滤掉 0 只的状态，按数量倒序 */
const statusData = computed(() =>
  Object.keys(stats.value.statusCount || {})
    .map(name => ({ name, value: stats.value.statusCount[name] || 0, itemStyle: { color: STATUS_COLORS[name] } }))
    .filter(d => d.value > 0)
    .sort((a, b) => b.value - a.value)
)

const baseList = computed(() => (stats.value.baseCount || []).filter(b => b && b.baseName))

const tagCount = computed(() => (stats.value.tagCount || []).filter(t => t.count > 0).slice(0, 12))

function goPage(item) {
  if (!item || !item.path) return
  router.push({ path: item.path, query: item.query || {} })
}

function goTag(t) {
  router.push({ path: '/profile/profile', query: { tagIds: t.tagId } })
}

function renderStatusChart() {
  if (statusChart) {
    statusChart.dispose()
    statusChart = null
  }
  if (!statusChartRef.value || !statusData.value.length) return
  const total = statusData.value.reduce((s, d) => s + d.value, 0)
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 只（{d}%）' },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      type: 'scroll',
      textStyle: { fontSize: 11 },
      // 数量直接写进图例，扇区上不再堆标签
      formatter: name => {
        const item = statusData.value.find(d => d.name === name)
        return item ? `${name} ${item.value}` : name
      }
    },
    title: {
      text: String(total),
      subtext: '只在册',
      left: '50%',
      top: '32%',
      textAlign: 'center',
      textStyle: { fontSize: 22, fontWeight: 700 },
      subtextStyle: { fontSize: 11 }
    },
    series: [
      {
        name: '猫咪状态',
        type: 'pie',
        radius: ['48%', '66%'],
        center: ['50%', '44%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        labelLine: { show: false },
        data: statusData.value
      }
    ]
  })
  statusChart.on('click', params => {
    const status = STATUS_MAP[params.name]
    if (status) router.push({ path: '/profile/profile', query: { status } })
  })
}

function renderBaseChart() {
  if (baseChart) {
    baseChart.dispose()
    baseChart = null
  }
  if (!baseChartRef.value || !baseList.value.length) return
  const list = baseList.value
  baseChart = echarts.init(baseChartRef.value)
  baseChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>在册 {c} 只' },
    grid: { left: 45, right: 20, top: 24, bottom: 56 },
    xAxis: {
      type: 'category',
      data: list.map(b => b.baseName),
      axisTick: { show: false },
      axisLabel: { interval: 0, fontSize: 11, rotate: list.length > 4 ? 22 : 0, formatter: v => (v.length > 8 ? v.slice(0, 7) + '…' : v) }
    },
    yAxis: { type: 'value', name: '只', minInterval: 1 },
    series: [
      {
        type: 'bar',
        barWidth: '45%',
        itemStyle: { borderRadius: [6, 6, 0, 0], color: '#409EFF' },
        label: { show: true, position: 'top', fontSize: 11 },
        data: list.map(b => b.count)
      }
    ]
  })
  baseChart.on('click', params => {
    const base = list[params.dataIndex]
    if (base && base.baseId) router.push({ path: '/profile/profile', query: { baseId: base.baseId } })
  })
}

function renderExpenseChart() {
  if (expenseChart) {
    expenseChart.dispose()
    expenseChart = null
  }
  if (!expenseChartRef.value) return
  const total = Number(stats.value.totalExpense || 0)
  const medical = Number(stats.value.medicalExpense || 0)
  const material = Number(stats.value.materialExpense || 0)
  const other = Math.max(total - medical - material, 0)
  const values = { 2: medical, 1: material, 3: other }
  expenseChart = echarts.init(expenseChartRef.value)
  expenseChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: params => {
        const p = params[0]
        return `${p.name}<br/>¥${Number(p.value).toLocaleString('zh-CN')}`
      }
    },
    grid: { left: 66, right: 28, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: EXPENSE_TYPES.map(t => t.name), axisTick: { show: false } },
    yAxis: { type: 'value', name: '元' },
    series: [
      {
        type: 'bar',
        barWidth: '45%',
        itemStyle: { borderRadius: [6, 6, 0, 0], color: '#409EFF' },
        label: { show: true, position: 'top', formatter: p => Number(p.value).toLocaleString('zh-CN') },
        data: EXPENSE_TYPES.map(t => values[t.value])
      }
    ]
  })
  expenseChart.on('click', params => {
    const type = EXPENSE_TYPES[params.dataIndex]
    if (type) router.push({ path: '/finance/expense', query: { expenseType: type.value } })
  })
}

function getStats() {
  loading.value = true
  getDashboardStats()
    .then(res => {
      stats.value = res.data || {}
      buildCards()
      const d = new Date()
      const p = n => String(n).padStart(2, '0')
      updatedAt.value = `${p(d.getHours())}:${p(d.getMinutes())}`
      nextTick(() => {
        renderStatusChart()
        renderBaseChart()
        renderExpenseChart()
      })
    })
    .finally(() => {
      loading.value = false
    })
}

function handleResize() {
  statusChart && statusChart.resize()
  baseChart && baseChart.resize()
  expenseChart && expenseChart.resize()
}

onMounted(() => {
  getStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  statusChart && statusChart.dispose()
  baseChart && baseChart.dispose()
  expenseChart && expenseChart.dispose()
})
</script>

<style scoped>
.dashboard .dash-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.dashboard .dash-head-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.dashboard .dash-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.dashboard .dash-time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

/* ---------- 核心指标 ---------- */
.dashboard .stat-card {
  --accent: #409eff;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-left: 4px solid var(--accent);
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 12px;
  transition: all 0.2s;
}
.dashboard .stat-card.clickable { cursor: pointer; }
.dashboard .stat-card.clickable:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 12px rgb(0 0 0 / 8%);
  transform: translateY(-2px);
}
.dashboard .stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dashboard .stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
}
.dashboard .stat-unit {
  font-size: 12px;
  font-weight: 400;
  margin-left: 3px;
  color: var(--el-text-color-placeholder);
}
.dashboard .stat-sub {
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

/* ---------- 区块标题 ---------- */
.dashboard .sec-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin: 8px 0 10px;
}
.dashboard .sec-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.dashboard .sec-hint {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

/* ---------- 待办 ---------- */
.dashboard .todo-item {
  --accent: #409eff;
  cursor: pointer;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-left: 3px solid var(--accent);
  border-radius: 6px;
  padding: 12px 14px;
  margin-bottom: 12px;
  transition: all 0.2s;
}
.dashboard .todo-item:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 12px rgb(0 0 0 / 8%);
  transform: translateY(-2px);
}
/* 没有待办的项整体弱化，一眼看出哪些要处理 */
.dashboard .todo-item.is-zero {
  border-left-color: var(--el-border-color);
  background: var(--el-fill-color-lighter);
}
.dashboard .todo-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.dashboard .todo-arrow {
  color: var(--el-text-color-placeholder);
  transition: all 0.2s;
}
.dashboard .todo-item:hover .todo-arrow {
  color: var(--el-color-primary);
  transform: translateX(3px);
}
.dashboard .todo-value {
  margin: 6px 0 2px;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.1;
}
.dashboard .todo-desc {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

/* ---------- 图表 ---------- */
.dashboard .dash-row { margin-top: 4px; }
/* 同一行两张卡片强制等高，避免一高一矮 */
.dashboard .chart-card {
  height: calc(100% - 16px);
  margin-bottom: 16px;
}
.dashboard .chart-box { height: 280px; }
.dashboard .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.dashboard .card-header .tip {
  font-size: 12px;
  font-weight: 400;
  color: var(--el-text-color-placeholder);
}

/* ---------- 标签 ---------- */
/* 与图表同高，标签紧贴标题排列，多时可滚动 */
.dashboard .tag-wrap {
  display: flex;
  align-items: flex-start;
  overflow: auto;
}
.dashboard .tag-stat {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
  max-height: 100%;
}
.dashboard .tag-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  cursor: pointer;
  transition: all 0.2s;
}
.dashboard .tag-item:hover { background: var(--el-color-primary-light-9); }
.dashboard .tag-count {
  font-weight: 700;
  color: var(--el-text-color-primary);
  font-size: 14px;
}
.dashboard .tag-group {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

@media (max-width: 768px) {
  .dashboard .chart-box { height: 240px; }
  .dashboard .stat-value { font-size: 22px; }
}
</style>
