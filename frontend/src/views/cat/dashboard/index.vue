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

    <!-- 一、在册现状 -->
    <div class="sec-head">
      <span class="sec-title">在册现状</span>
      <span class="sec-hint">按猫咪当前状态统计，点击卡片查看对应档案</span>
    </div>
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

    <!-- 二、TNR 与待办（同一行展示：左 TNR 闭环，右今日待办） -->
    <div class="sec-head">
      <span class="sec-title">TNR 与待办提醒</span>
      <span class="sec-hint">
        TNR 抓捕 → 绝育闭环（放归数见上方「已放归」）·
        {{ todoTotal > 0 ? `共 ${todoTotal} 项待处理` : '暂无待处理事项' }}· 点击卡片直达对应列表
      </span>
    </div>
    <div class="flow-row">
      <div class="flow-group flow-group-tnr">
        <div
          v-for="card in tnrCards"
          :key="card.label"
          class="flow-card"
          :class="{ 'is-zero': !card.value }"
          :style="{ '--accent': card.color }"
          :title="card.tip || `点击查看「${card.label}」明细`"
          @click="goPage(card)"
        >
          <div class="flow-title">
            <span>{{ card.label }}</span>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-value" :style="{ color: card.value > 0 ? card.color : 'var(--el-text-color-placeholder)' }">
            {{ card.value }}<span class="stat-unit">只</span>
          </div>
          <div class="flow-sub">{{ card.sub }}</div>
        </div>
      </div>
      <span class="flow-sep"></span>
      <div class="flow-group flow-group-todo">
        <div
          v-for="item in todoList"
          :key="item.label"
          class="flow-card"
          :class="{ 'is-zero': !item.value }"
          :style="{ '--accent': item.color }"
          :title="item.tip || `点击进入「${item.label}」列表`"
          @click="goPage(item)"
        >
          <div class="flow-title">
            <span>{{ item.label }}</span>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-value" :style="{ color: item.value > 0 ? item.color : 'var(--el-text-color-placeholder)' }">
            {{ item.value }}<span class="stat-unit">项</span>
          </div>
          <div class="flow-sub">{{ item.desc }}</div>
        </div>
      </div>
    </div>

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

    <!-- 四、支出与死因 -->
    <el-row :gutter="16" class="dash-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>支出结构</span>
              <span class="tip">累计 ¥{{ formatMoney(stats.totalExpense) }} · 点击查看支出明细</span>
            </div>
          </template>
          <!-- 本期（本月）三项支出：口径统一按本月，避免和累计柱图混淆 -->
          <div class="expense-kpi">
            <div class="kpi" @click="goExpense()">
              <div class="kpi-label">本期总支出</div>
              <div class="kpi-value">¥{{ formatMoney(stats.monthExpense) }}</div>
              <div class="kpi-sub">本月合计</div>
            </div>
            <div class="kpi" @click="goExpense('2')">
              <div class="kpi-label">医疗支出</div>
              <div class="kpi-value">¥{{ formatMoney(stats.monthMedicalExpense) }}</div>
              <div class="kpi-sub">本月 · 外送诊疗</div>
            </div>
            <div class="kpi" @click="goExpense('1')">
              <div class="kpi-label">物资采购</div>
              <div class="kpi-value">¥{{ formatMoney(stats.monthMaterialExpense) }}</div>
              <div class="kpi-sub">本月 · 库房采购</div>
            </div>
          </div>
          <div ref="expenseChartRef" class="chart-box chart-box-sm"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>死因分布</span>
              <span class="tip">
                共 {{ Number(stats.deathCount || 0) }} 只 · 点击条形查看死亡台账
              </span>
            </div>
          </template>
          <div v-if="deathData.length" ref="deathChartRef" class="chart-box death-box"></div>
          <el-empty v-else description="暂无死亡登记" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 五、标签统计 -->
    <el-row :gutter="16" class="dash-row">
      <el-col :span="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>标签统计（慢性病等猫咪总数）</span>
              <span class="tip">点击标签直达档案列表</span>
            </div>
          </template>
          <el-empty v-if="!tagCount.length" description="暂无标签数据" :image-size="60" />
          <div v-else class="tag-wrap">
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
const deathChartRef = ref(null)
const cards = ref([])
const router = useRouter()

let statusChart = null
let baseChart = null
let expenseChart = null
let deathChart = null

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

/** 死因类型 → cat_death_type 字典值，顺序与后端 deathTypeCount 一致 */
const DEATH_TYPES = [
  { name: '自然寿终', value: '1', color: '#909399' },
  { name: '病重不治', value: '2', color: '#F56C6C' },
  { name: '安乐', value: '3', color: '#E6A23C' },
  { name: '意外', value: '4', color: '#722ED1' }
]

/** TNR 闭环 + 离世，放归数在「在册现状」里，这里不重复 */
const tnrCards = computed(() => {
  const captured = Number(stats.value.tnrCount || 0)
  const neutered = Number(stats.value.tnrNeutered || 0)
  const pending = Math.max(captured - neutered, 0)
  const rate = captured > 0 ? Math.round((neutered / captured) * 100) : 0
  return [
    {
      label: 'TNR 抓捕',
      value: captured,
      color: '#722ED1',
      sub: '累计抓捕登记',
      tip: '点击查看 TNR 抓捕记录',
      path: '/tnr/tnr'
    },
    {
      label: '已绝育',
      value: neutered,
      color: '#13C2C2',
      sub: captured > 0 ? `绝育率 ${rate}%` : '暂无抓捕记录',
      tip: captured > 0 ? `绝育率 ${rate}%，待绝育 ${pending} 只（已抓捕未登记手术）` : '暂无抓捕记录',
      path: '/tnr/tnr',
      query: { neuterStatus: '1' }
    },
    {
      label: '已离世',
      value: Number(stats.value.deathCount || 0),
      color: '#909399',
      sub: '死因分布见下',
      tip: '点击查看死亡台账，死因分布见下方图表',
      path: '/medical/death'
    }
  ]
})

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

/** 待绝育 = 已抓捕但还没登记手术日期 */
const pendingNeuter = computed(() =>
  Math.max(Number(stats.value.tnrCount || 0) - Number(stats.value.tnrNeutered || 0), 0)
)

/** 待办事项 → 对应业务列表页，带筛选条件直达 */
const todoList = computed(() => [
  { label: '待疫苗', value: stats.value.dueVaccine || 0, color: '#409EFF', desc: '7 天内到期', path: '/medical/vaccine', query: { dueDays: 7 } },
  { label: '待驱虫', value: stats.value.dueDeworm || 0, color: '#722ED1', desc: '7 天内到期', path: '/medical/deworm', query: { dueDays: 7 } },
  { label: '待绝育', value: pendingNeuter.value, color: '#13C2C2', desc: '已抓捕未手术', path: '/tnr/tnr', query: { neuterStatus: '0' } },
  { label: '待回访', value: stats.value.pendingVisit || 0, color: '#E6A23C', desc: '查看回访计划', path: '/adopt/visit', query: { visitStatus: '0' } },
  { label: '回访异常', value: stats.value.visitAbnormal || 0, color: '#F56C6C', desc: '需跟进处理', path: '/adopt/visit', query: { visitStatus: '2' } },
  { label: '外送治疗中', value: stats.value.referralCount || 0, color: '#D4380D', desc: '在院转诊记录', path: '/medical/referral', query: { status: '0' } }
])

const todoTotal = computed(() =>
  ['dueVaccine', 'dueDeworm', 'pendingVisit', 'visitAbnormal', 'referralCount'].reduce(
    (sum, k) => sum + Number(stats.value[k] || 0),
    0
  ) + pendingNeuter.value
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

/** 死因分布：过滤掉 0 只的类型，后端按名称返回数量 */
const deathData = computed(() => {
  const map = stats.value.deathTypeCount || {}
  return DEATH_TYPES
    .map(t => ({ ...t, count: Number(map[t.name] || 0) }))
    .filter(d => d.count > 0)
})

function goPage(item) {
  if (!item || !item.path) return
  router.push({ path: item.path, query: item.query || {} })
}

/** 支出指标点击：带类型筛选进支出列表 */
function goExpense(type) {
  router.push({ path: '/finance/expense', query: type ? { expenseType: type } : {} })
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
  // 手机上 7 个状态图例一行放不下：改自动换行 + 饼图缩小上移，桌面仍用横向滚动保持单行
  const mobile = window.innerWidth <= 768
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 只（{d}%）' },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      type: mobile ? 'plain' : 'scroll',
      itemGap: mobile ? 8 : 10,
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
      top: mobile ? '26%' : '32%',
      textAlign: 'center',
      textStyle: { fontSize: 22, fontWeight: 700 },
      subtextStyle: { fontSize: 11 }
    },
    series: [
      {
        name: '猫咪状态',
        type: 'pie',
        radius: mobile ? ['40%', '56%'] : ['48%', '66%'],
        center: ['50%', mobile ? '40%' : '44%'],
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
    grid: { left: 45, right: 20, top: 36, bottom: 56 },
    xAxis: {
      type: 'category',
      data: list.map(b => b.baseName),
      axisTick: { show: false },
      axisLabel: { interval: 0, fontSize: 11, rotate: list.length > 4 ? 22 : 0, formatter: v => (v.length > 8 ? v.slice(0, 7) + '…' : v) }
    },
    // name 默认画在轴顶端上方，grid.top 不够会被画布裁掉半个字
    yAxis: {
      type: 'value',
      minInterval: 1,
      name: '只',
      nameGap: 10,
      nameTextStyle: { fontSize: 11, color: '#909399', align: 'right', padding: [0, 5, 0, 0] }
    },
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
  // 轴顶按本组最大值留 15% 余量，否则柱顶标签会压柱、柱子也显得矮
  const peak = Math.max(medical, material, other, 0)
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
    grid: { left: 66, right: 28, top: 46, bottom: 40 },
    xAxis: { type: 'category', data: EXPENSE_TYPES.map(t => t.name), axisTick: { show: false } },
    yAxis: { type: 'value', name: '元', max: peak > 0 ? Math.ceil(peak * 1.25) : 100 },
    series: [
      {
        type: 'bar',
        barWidth: '45%',
        itemStyle: { borderRadius: [6, 6, 0, 0], color: '#409EFF' },
        label: { show: true, position: 'top', distance: 6, formatter: p => Number(p.value).toLocaleString('zh-CN') },
        data: EXPENSE_TYPES.map(t => values[t.value])
      }
    ]
  })
  expenseChart.on('click', params => {
    const type = EXPENSE_TYPES[params.dataIndex]
    if (type) router.push({ path: '/finance/expense', query: { expenseType: type.value } })
  })
}

/** 死因分布：横向条形，一眼看出哪类死因占比高 */
function renderDeathChart() {
  if (deathChart) {
    deathChart.dispose()
    deathChart = null
  }
  if (!deathChartRef.value || !deathData.value.length) return
  // 横向条形图的类目从下往上排，倒序后数量多的显示在上方
  const list = [...deathData.value].reverse()
  const total = deathData.value.reduce((s, d) => s + d.count, 0)
  deathChart = echarts.init(deathChartRef.value)
  deathChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: params => {
        const d = list[params[0].dataIndex]
        const pct = total > 0 ? Math.round((d.count / total) * 100) : 0
        return `${d.name}<br/>${d.count} 只（${pct}%）`
      }
    },
    grid: { left: 78, right: 64, top: 20, bottom: 20 },
    xAxis: {
      type: 'value',
      minInterval: 1,
      // 条形末端要写「N 只」，最大值留一点余量，否则标签会被裁掉
      max: value => Math.max(Math.ceil(value.max * 1.25), 2),
      axisLabel: { fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: list.map(d => d.name),
      axisTick: { show: false },
      axisLabel: { fontSize: 12 }
    },
    series: [
      {
        type: 'bar',
        // 死因种类少，限制条宽，否则两根条会变成两条大横杠
        barMaxWidth: 26,
        itemStyle: { borderRadius: [0, 6, 6, 0] },
        label: { show: true, position: 'right', fontSize: 11, formatter: '{c} 只' },
        data: list.map(d => ({ value: d.count, itemStyle: { color: d.color } }))
      }
    ]
  })
  deathChart.on('click', params => {
    const d = list[params.dataIndex]
    if (d) router.push({ path: '/medical/death', query: { deathType: d.value } })
  })
}

function getStats() {  loading.value = true
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
        renderDeathChart()
      })
    })
    .finally(() => {
      loading.value = false
    })
}

// 移动断点切换（横竖屏）时饼图图例形态要重算，单纯 resize 不会变
let mobileMode = window.innerWidth <= 768

function handleResize() {
  statusChart && statusChart.resize()
  baseChart && baseChart.resize()
  expenseChart && expenseChart.resize()
  deathChart && deathChart.resize()
  const nowMobile = window.innerWidth <= 768
  if (nowMobile !== mobileMode) {
    mobileMode = nowMobile
    renderStatusChart()
  }
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
  deathChart && deathChart.dispose()
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

/* ---------- TNR + 待办：并成一行九格（左 3 格 TNR，右 6 格待办） ---------- */
.dashboard .flow-row {
  display: flex;
  align-items: stretch;
  gap: 10px;
  margin-bottom: 12px;
}
.dashboard .flow-group {
  display: flex;
  gap: 10px;
  min-width: 0;
}
.dashboard .flow-group-tnr { flex: 3 1 0; }
.dashboard .flow-group-todo { flex: 6 1 0; }
/* 两组之间的细分隔线，保留「TNR / 待办」的语义分组 */
.dashboard .flow-sep {
  flex: 0 0 1px;
  margin: 6px 0;
  background: var(--el-border-color-darker);
}
.dashboard .flow-card {
  --accent: #409eff;
  flex: 1 1 0;
  min-width: 0;
  cursor: pointer;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-left: 3px solid var(--accent);
  border-radius: 6px;
  padding: 9px 10px;
  transition: all 0.2s;
}
.dashboard .flow-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 12px rgb(0 0 0 / 8%);
  transform: translateY(-2px);
}
/* 为 0 的项整体弱化，一眼看出哪些要处理 */
.dashboard .flow-card.is-zero {
  border-left-color: var(--el-border-color);
  background: var(--el-fill-color-lighter);
}
.dashboard .flow-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.dashboard .flow-title > span {
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.dashboard .flow-arrow {
  flex: none;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  transition: all 0.2s;
}
.dashboard .flow-card:hover .flow-arrow {
  color: var(--el-color-primary);
  transform: translateX(2px);
}
.dashboard .flow-value {
  margin: 5px 0 3px;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.15;
}
.dashboard .flow-sub {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 九格在窄屏（<1100px）挤不下时折成两组，每格仍有 110px 以上可读宽度 */
@media (max-width: 1100px) {
  .dashboard .flow-row { flex-wrap: wrap; }
  .dashboard .flow-sep { display: none; }
  .dashboard .flow-group { flex: 1 1 100%; }
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

/* ---------- 图表 ---------- */
.dashboard .dash-row { margin-top: 4px; }
/* 同一行两张卡片强制等高，避免一高一矮 */
.dashboard .chart-card {
  height: calc(100% - 16px);
  margin-bottom: 16px;
}
.dashboard .chart-box { height: 280px; }
/* 支出卡顶部有指标行，图表相应压低，保证与右侧死因卡等高 */
.dashboard .chart-box-sm { height: 208px; }
.dashboard .death-box { height: 280px; }

/* ---------- 支出指标 ---------- */
.dashboard .expense-kpi {
  display: flex;
  gap: 10px;
  margin-bottom: 2px;
}
.dashboard .kpi {
  flex: 1 1 0;
  min-width: 0;
  padding: 8px 10px;
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  cursor: pointer;
  transition: all 0.2s;
}
.dashboard .kpi:hover { background: var(--el-color-primary-light-9); }
.dashboard .kpi-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dashboard .kpi-value {
  margin-top: 2px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.25;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.dashboard .kpi-sub {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
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
/* 独占一行，标签流式铺开，多时卡片内滚动 */
.dashboard .tag-wrap {
  display: flex;
  align-items: flex-start;
  min-height: 64px;
  max-height: 200px;
  overflow: auto;
}
.dashboard .tag-stat {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
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
  /* 手机上收窄页边距，把宽度让给卡片 */
  .app-container.dashboard { padding: 12px; }
  .dashboard .chart-box { height: 240px; }
  .dashboard .death-box { height: 220px; }
  .dashboard .chart-box-sm { height: 190px; }
  .dashboard .stat-value { font-size: 22px; }

  /* 九格在手机上按 3 列折行：TNR 一行 3 格、待办两行各 3 格 */
  .dashboard .flow-group { flex-wrap: wrap; }
  /* 用 0 0 而不是 1 1：flex-shrink 会让 4 个卡片挤进一行，破坏 3 列等宽 */
  .dashboard .flow-card { flex: 0 0 calc((100% - 20px) / 3); }

  /* 标题与说明在窄屏换行排，避免挤压标题 */
  .dashboard .sec-head { flex-wrap: wrap; row-gap: 2px; }
  /* 图表卡右上角的操作提示让位给标题 */
  .dashboard .card-header .tip { display: none; }

  .dashboard .expense-kpi { gap: 8px; }
  .dashboard .kpi { padding: 6px 8px; }
  .dashboard .kpi-value { font-size: 16px; }

  /* 卡片内边距收窄，把宽度让给图表 */
  .dashboard :deep(.el-card__body) { padding: 14px 12px; }
  .dashboard :deep(.el-card__header) { padding: 12px; }
}

/* 超小屏（<420px）九格再降为 2 列，保证「外送治疗中」这类 5 字标签不被省略 */
@media (max-width: 420px) {
  .dashboard .flow-card { flex: 0 0 calc((100% - 10px) / 2); }
}
</style>
