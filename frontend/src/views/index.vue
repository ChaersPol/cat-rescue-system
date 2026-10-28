<template>
  <div class="app-container home">
    <el-card class="hero" shadow="never">
      <div class="hero-main">
        <img :src="logo" class="hero-logo" alt="宠物救助系统" />
        <div class="hero-text">
          <h2>宠物救助系统</h2>
          <p>
            面向流浪猫救助站的日常管理系统，覆盖从救助捕捉、医疗诊治、库房领用，到领养回访、TNR 放归的完整流程，
            让每一只猫的来历和去向都有据可查。
          </p>
          <div class="hero-tags">
            <el-tag type="success" effect="plain">当前版本 v{{ version }}</el-tag>
            <el-tag effect="plain">在册猫咪 {{ stats.total }}</el-tag>
            <el-tag effect="plain">已领养 {{ stats.adopted }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <el-divider content-position="left">功能模块</el-divider>

    <el-row :gutter="16">
      <el-col v-for="item in modules" :key="item.title" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="module" shadow="hover" @click="go(item.path)">
          <div class="module-title">
            <el-icon class="module-icon"><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </div>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-divider content-position="left">使用提示</el-divider>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="update-log" shadow="never">
          <template #header><span>日常怎么用</span></template>
          <ol class="tips">
            <li>新救助一只猫，先进「猫咪档案」建档并生成二维码，贴在笼位上，手机扫码就能看完整病历。</li>
            <li>看病、打疫苗、驱虫都在「医疗管理」里记录，用药可以一次填连续多天。</li>
            <li>到期该打的疫苗、该做的回访，会在「提醒通知」和各人工作台自动提示。</li>
            <li>领养走完流程后到「领养管理」登记，后续按约定周期做回访。</li>
            <li>首页「统计看板」支持点图表直接跳到明细列表，不用再自己筛条件。</li>
          </ol>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="update-log" shadow="never">
          <template #header><span>技术信息</span></template>
          <div class="tech">
            <div class="tech-group">
              <h4>后端</h4>
              <ul>
                <li>Spring Boot 3</li>
                <li>Spring Security + JWT</li>
                <li>MyBatis</li>
                <li>MySQL 8</li>
                <li>Redis</li>
              </ul>
            </div>
            <div class="tech-group">
              <h4>前端</h4>
              <ul>
                <li>Vue 3</li>
                <li>Element Plus</li>
                <li>Vite</li>
                <li>ECharts</li>
                <li>Pinia</li>
              </ul>
            </div>
          </div>
          <p class="tech-note">
            系统支持手机端访问：右上角头像菜单里的「移动端工作台」，或在手机浏览器直接打开 <code>/m</code>。
          </p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import logo from '@/assets/logo/logo.svg'
import { getDashboardStats } from '@/api/cat/dashboard'

const router = useRouter()
const version = ref('1.0.0')
const stats = reactive({ total: 0, adopted: 0 })

const modules = [
  { title: '猫咪档案', icon: 'Files', path: '/profile/profile', desc: '基础信息、照片相册、二维码笼位卡，扫码即看完整病历。' },
  { title: '医疗管理', icon: 'FirstAidKit', path: '/medical/medication', desc: '诊断、用药、疫苗驱虫、转诊外送，检查照片一并归档。' },
  { title: '领养管理', icon: 'House', path: '/adopt/adoption', desc: '领养人登记、审核、定期回访，异常回访自动提醒。' },
  { title: '物资库房', icon: 'Box', path: '/finance/material', desc: '药品物资出入库、库存预警、批次与有效期跟踪。' },
  { title: '财务支出', icon: 'Money', path: '/finance/expense', desc: '收支记账、票据上传，按类型统计支出结构。' },
  { title: 'TNR 与放归', icon: 'Scissor', path: '/tnr/tnr', desc: '捕捉、绝育、放归全流程记录，绝育率一屏可见。' },
  { title: '提醒通知', icon: 'Bell', path: '/reminder', desc: '待疫苗、待驱虫、回访到期，自动汇总到工作台。' },
  { title: '统计看板', icon: 'DataAnalysis', path: '/index', desc: '在册、领养、死亡趋势，点图表直达明细列表。' },
]

function go(path) {
  router.push(path)
}

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    const d = res.data || {}
    stats.total = d.totalCats || 0
    stats.adopted = (d.statusCount || {})['已领养'] || 0
  } catch (e) {
    // 看板数据取不到不影响本页展示
  }
})
</script>

<style scoped lang="scss">
.home {
  font-size: 13px;
  color: var(--el-text-color-regular);

  .hero {
    border: none;
    background: linear-gradient(135deg, var(--el-color-primary-light-9), transparent 70%);
  }

  .hero-main {
    display: flex;
    align-items: center;
    gap: 20px;
    flex-wrap: wrap;
  }

  .hero-logo {
    width: 76px;
    height: 76px;
    flex: 0 0 auto;
  }

  .hero-text {
    flex: 1 1 320px;
    min-width: 0;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    p {
      margin: 0 0 12px;
      line-height: 1.8;
      max-width: 900px;
    }
  }

  .hero-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .module {
    margin-bottom: 16px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    .module-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin-bottom: 8px;
    }

    .module-icon {
      color: var(--el-color-primary);
      font-size: 16px;
    }

    p {
      margin: 0;
      line-height: 1.7;
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }
  }

  .update-log {
    margin-bottom: 16px;
    height: calc(100% - 16px);
  }

  .tips {
    margin: 0;
    padding-left: 18px;
    line-height: 2;

    li {
      list-style: decimal;
    }
  }

  .tech {
    display: flex;
    gap: 40px;
    flex-wrap: wrap;

    h4 {
      margin: 0 0 8px;
      font-size: 13px;
      color: var(--el-text-color-primary);
    }

    ul {
      margin: 0;
      padding: 0;
      list-style: none;
      line-height: 1.9;
    }
  }

  .tech-note {
    margin: 12px 0 0;
    line-height: 1.8;
    color: var(--el-text-color-secondary);

    code {
      padding: 1px 5px;
      border-radius: 3px;
      background: var(--el-fill-color-light);
    }
  }
}
</style>
