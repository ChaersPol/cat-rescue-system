<template>
  <div class="app-container dashboard">
    <!-- 核心指标 -->
    <el-row :gutter="16">
      <el-col :xs="12" :sm="8" :md="4" v-for="card in cards" :key="card.label">
        <div class="stat-card clickable" :style="{ borderLeftColor: card.color }" @click="goPage(card)">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="8">
        <el-card shadow="never" header="猫咪状态分布">
          <div ref="statusChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" header="各基地猫咪数量">
          <div ref="baseChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" header="死因分布">
          <div ref="deathChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" header="支出结构（元）">
          <div ref="expenseChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>标签统计（慢性病等猫咪总数）</span>
              <span class="tip">点击标签可直达档案列表</span>
            </div>
          </template>
          <el-empty v-if="!tagCount.length" description="暂无标签数据" :image-size="60" />
          <div v-else class="tag-stat">
            <div class="tag-item" v-for="t in tagCount" :key="t.tagId" @click="goTag(t)">
              <el-tag size="small" type="danger" effect="light">{{ t.tagName }}</el-tag>
              <span class="tag-count">{{ t.count }}</span>
              <span class="tag-group">{{ t.tagGroup || '—' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="never" header="待办事项">
          <el-row :gutter="12">
            <el-col :xs="12" :sm="8" :md="4" v-for="item in todoList" :key="item.label">
              <div class="todo-item" :style="{ borderLeftColor: item.color }" @click="goPage(item)">
                <div class="todo-title">
                  <span>{{ item.label }}</span>
                  <el-icon class="todo-arrow"><ArrowRight /></el-icon>
                </div>
                <div class="todo-value" :style="{ color: item.value > 0 ? item.color : 'var(--el-text-color-placeholder)' }">
                  {{ item.value }}
                </div>
                <div class="todo-desc">{{ item.desc }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="CatDashboard">
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/cat/dashboard'

const stats = ref({})
const statusChartRef = ref(null)
const baseChartRef = ref(null)
const deathChartRef = ref(null)
const expenseChartRef = ref(null)
const cards = ref([])
const router = useRouter()

let statusChart = null
let baseChart = null
let deathChart = null
let expenseChart = null

const STATUS_COLORS = ['#409EFF', '#E6A23C', '#F56C6C', '#909399', '#67C23A', '#8E44AD', '#00BCD4', '#FFB6C1']

/** 顶部统计卡 → 对应列表页（status 为 cat_status 字典值） */
function buildCards() {
  const sc = stats.value.statusCount || {}
  cards.value = [
    { label: '猫咪总数', value: stats.value.totalCats || 0, color: '#409EFF', path: '/profile/profile', query: {} },
    { label: '在养', value: sc['在养'] || 0, color: '#67C23A', path: '/profile/profile', query: { status: '1' } },
    { label: '治疗中(基地)', value: sc['基地内治疗中'] || 0, color: '#E6A23C', path: '/profile/profile', query: { status: '2' } },
    { label: '治疗中(外送)', value: sc['外送医院治疗中'] || 0, color: '#F56C6C', path: '/profile/profile', query: { status: '3' } },
    { label: '已领养', value: sc['已领养'] || 0, color: '#8E44AD', path: '/profile/profile', query: { status: '5' } },
    { label: '已放归', value: sc['已放归'] || 0, color: '#00BCD4', path: '/profile/profile', query: { status: '4' } }
  ]
}

/** 待办事项 → 对应业务列表页，带筛选条件直达 */
const todoList = computed(() => [
  {
    label: '待疫苗',
    value: stats.value.dueVaccine || 0,
    color: '#409EFF',
    desc: '7 天内到期或已过期',
    path: '/medical/vaccine',
    query: {}
  },
  {
    label: '待驱虫',
    value: stats.value.dueDeworm || 0,
    color: '#722ED1',
    desc: '7 天内到期或已过期',
    path: '/medical/deworm',
    query: {}
  },
  {
    label: '待回访',
    value: stats.value.pendingVisit || 0,
    color: '#E6A23C',
    desc: '查看待回访计划',
    path: '/adopt/visit',
    query: { visitStatus: '0' }
  },
  {
    label: '回访异常',
    value: stats.value.visitAbnormal || 0,
    color: '#F56C6C',
    desc: '需要跟进处理',
    path: '/adopt/visit',
    query: { visitStatus: '2' }
  },
  {
    label: '外送治疗中',
    value: stats.value.referralCount || 0,
    color: '#D4380D',
    desc: '查看在院转诊记录',
    path: '/medical/referral',
    query: { status: '0' }
  },
  {
    label: '累计离世',
    value: stats.value.deathCount || 0,
    color: '#909399',
    desc: '查看死亡台账',
    path: '/medical/death',
    query: {}
  }
])

function goPage(item) {
  if (!item || !item.path) return
  router.push({ path: item.path, query: item.query || {} })
}

function goTag(t) {
  router.push({ path: '/profile/profile', query: { tagIds: t.tagId } })
}

function renderStatusChart() {
  if (!statusChartRef.value) return
  const sc = stats.value.statusCount || {}
  const data = Object.keys(sc).map(k => ({ name: k, value: sc[k] }))
  if (!statusChart) statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 只 ({d}%)' },
    legend: { bottom: 0, icon: 'circle', textStyle: { fontSize: 11 } },
    color: STATUS_COLORS,
    series: [
      {
        name: '猫咪状态',
        type: 'pie',
        radius: ['40%', '66%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{c}' },
        data
      }
    ]
  })
}

function renderBaseChart() {
  if (!baseChartRef.value) return
  const list = stats.value.baseCount || []
  if (!baseChart) baseChart = echarts.init(baseChartRef.value)
  baseChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 50, right: 20, top: 20, bottom: 50 },
    xAxis: {
      type: 'category',
      data: list.map(b => b.baseName),
      axisTick: { show: false },
      axisLabel: { interval: 0, fontSize: 11, rotate: list.length > 4 ? 25 : 0 }
    },
    yAxis: { type: 'value', name: '只' },
    series: [
      {
        type: 'bar',
        barWidth: '45%',
        itemStyle: { borderRadius: [6, 6, 0, 0], color: '#409EFF' },
        label: { show: true, position: 'top' },
        data: list.map(b => b.count)
      }
    ]
  })
}

function renderDeathChart() {
  if (!deathChartRef.value) return
  const dc = stats.value.deathTypeCount || {}
  const data = Object.keys(dc).map(k => ({ name: k, value: dc[k] }))
  if (!deathChart) deathChart = echarts.init(deathChartRef.value)
  deathChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 只 ({d}%)' },
    legend: { bottom: 0, icon: 'circle', textStyle: { fontSize: 11 } },
    color: ['#909399', '#E6A23C', '#F56C6C', '#8E44AD'],
    series: [
      {
        name: '死因',
        type: 'pie',
        radius: ['40%', '66%'],
        center: ['50%', '44%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{c}' },
        data
      }
    ]
  })
}

function renderExpenseChart() {
  if (!expenseChartRef.value) return
  const total = Number(stats.value.totalExpense || 0)
  const medical = Number(stats.value.medicalExpense || 0)
  const material = Number(stats.value.materialExpense || 0)
  const other = Math.max(total - medical - material, 0)
  if (!expenseChart) expenseChart = echarts.init(expenseChartRef.value)
  expenseChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: ['医疗外送', '物资采购', '其他支出'],
      axisTick: { show: false }
    },
    yAxis: { type: 'value', name: '元' },
    series: [
      {
        type: 'bar',
        barWidth: '45%',
        itemStyle: { borderRadius: [6, 6, 0, 0] },
        color: '#409EFF',
        label: { show: true, position: 'top', formatter: '{c}' },
        data: [medical, material, other]
      }
    ]
  })
}

const tagCount = computed(() => (stats.value.tagCount || []).filter(t => t.count > 0).slice(0, 12))

function getStats() {
  getDashboardStats().then(res => {
    stats.value = res.data || {}
    buildCards()
    nextTick(() => {
      renderStatusChart()
      renderBaseChart()
      renderDeathChart()
      renderExpenseChart()
    })
  })
}

function handleResize() {
  statusChart && statusChart.resize()
  baseChart && baseChart.resize()
  deathChart && deathChart.resize()
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
  deathChart && deathChart.dispose()
  expenseChart && expenseChart.dispose()
})
</script>

<style scoped>
.dashboard .stat-card {
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-left: 4px solid #409eff;
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 16px;
  transition: all 0.2s;
}
.dashboard .stat-card.clickable { cursor: pointer; }
.dashboard .stat-card.clickable:hover {
  border-color: var(--el-color-primary-light-5);
  border-left-color: var(--el-color-primary);
  box-shadow: 0 2px 12px rgb(0 0 0 / 8%);
  transform: translateY(-2px);
}
.dashboard .stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}
.dashboard .stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
}
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .tip { font-size: 12px; color: var(--el-text-color-placeholder); }
.tag-stat { display: flex; flex-wrap: wrap; gap: 10px; }
.tag-item {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 10px; border-radius: 6px;
  background: var(--el-fill-color-light); cursor: pointer;
  transition: all 0.2s;
}
.tag-item:hover { background: var(--el-color-primary-light-9); }
.tag-count { font-weight: 700; color: var(--el-text-color-primary); font-size: 14px; }
.tag-group { font-size: 11px; color: var(--el-text-color-placeholder); }

.dashboard .todo-item {
  cursor: pointer;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-left: 3px solid #409eff;
  border-radius: 6px;
  padding: 12px 14px;
  transition: all 0.2s;
}
.dashboard .todo-item:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 12px rgb(0 0 0 / 8%);
  transform: translateY(-2px);
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
</style>
