<template>
  <div class="app-container archive-detail" v-loading="loading">
    <!-- 顶部档案头 -->
    <div class="archive-header">
      <div class="header-main">
        <div class="avatar">{{ (profile.catName || '猫').charAt(0) }}</div>
        <div class="header-info">
          <div class="name-row">
            <span class="cat-name">{{ profile.catName || '未命名' }}</span>
            <el-tag v-if="profile.alias" size="small" type="info" effect="plain">别名 {{ profile.alias }}</el-tag>
            <dict-tag :options="cat_status" :value="profile.status" />
            <el-tag v-if="profile.archived === '1'" size="small" type="warning" effect="dark">已归档</el-tag>
          </div>
          <div class="meta-row">
            <span class="meta">档案编号：{{ profile.archiveNo || '—' }}</span>
            <span class="meta">所在位置：{{ locationText }}</span>
            <span class="meta" v-if="profile.hospitalName">外送医院：{{ profile.hospitalName }}</span>
          </div>
          <div class="tag-row" v-if="tagNames.length">
            <el-tag v-for="t in tagNames" :key="t" size="small" type="danger" effect="light" class="tag-item">{{ t }}</el-tag>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Back" @click="goBack">返回列表</el-button>
        <el-button type="primary" plain icon="Document" @click="handleExportPdf">档案PDF</el-button>
        <el-button type="success" plain icon="Printer" @click="goPrint">打印二维码卡</el-button>
        <el-button v-if="profile.archived !== '1'" type="warning" plain icon="FolderChecked" @click="handleArchive">归档</el-button>
        <el-button v-else type="info" plain icon="RefreshLeft" @click="handleUnarchive">取消归档</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 左：基础信息 + 流转轨迹 -->
      <el-col :xs="24" :md="15">
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>基础档案</span>
              <el-button link type="primary" icon="Edit" @click="goEdit" v-hasPermi="['cat:profile:edit']">编辑档案</el-button>
            </div>
          </template>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="品种">{{ profile.breed || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别"><dict-tag :options="cat_sex" :value="profile.sex" /></el-descriptions-item>
            <el-descriptions-item label="年龄">{{ profile.ageEstimate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ profile.weight || '—' }}</el-descriptions-item>
            <el-descriptions-item label="来源"><dict-tag :options="cat_source" :value="profile.source" /></el-descriptions-item>
            <el-descriptions-item label="抓捕日期">{{ profile.captureDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="是否绝育"><dict-tag :options="cat_neutered" :value="profile.neutered" /></el-descriptions-item>
            <el-descriptions-item label="是否驱虫"><dict-tag :options="cat_yes_no" :value="profile.dewormed" /></el-descriptions-item>
            <el-descriptions-item label="是否疫苗"><dict-tag :options="cat_yes_no" :value="profile.vaccinated" /></el-descriptions-item>
            <el-descriptions-item label="抓捕/投喂点位" :span="3">{{ profile.capturePoint || '—' }}</el-descriptions-item>
            <el-descriptions-item label="标志性特征" :span="3">{{ profile.feature || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性格/备注" :span="3">{{ [profile.personality, profile.remark].filter(Boolean).join('；') || '—' }}</el-descriptions-item>
            <el-descriptions-item label="登记人">{{ profile.createBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="建档时间" :span="2">{{ profile.createTime || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 相册 -->
        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>照片相册（{{ photos.length }}）</span>
              <el-button link type="primary" icon="Picture" @click="goPhoto">管理相册</el-button>
            </div>
          </template>
          <el-empty v-if="!photos.length" description="暂无照片" :image-size="60" />
          <div v-else class="photo-grid">
            <div class="photo-item" v-for="p in photos" :key="p.photoId">
              <el-image
                v-if="p.fileType !== '2'"
                :src="resolveUrl(p.thumbUrl || p.fileUrl)"
                :preview-src-list="imagePreviewList"
                :initial-index="imagePreviewList.indexOf(resolveUrl(p.fileUrl))"
                fit="cover"
                preview-teleported
                class="photo"
              />
              <div v-else class="photo video" @click="openVideo(p)">
                <el-image v-if="p.thumbUrl" :src="resolveUrl(p.thumbUrl)" fit="cover" class="photo" />
                <el-icon class="play-icon"><VideoPlay /></el-icon>
              </div>
              <div class="photo-folder">{{ p.folder || '未分类' }}</div>
            </div>
          </div>
        </el-card>

        <!-- 流转轨迹 -->
        <el-card shadow="never" class="block">
          <template #header><span>流转轨迹（{{ flows.length }}）</span></template>
          <el-empty v-if="!flows.length" description="暂无流转记录" :image-size="60" />
          <el-table v-else :data="flows" size="small" border>
            <el-table-column label="序号" type="index" :index="i => $seqIndex(i)" width="56" align="center" />
            <el-table-column label="时间" width="160" align="center">
              <template #default="scope">{{ scope.row.operateTime || '—' }}</template>
            </el-table-column>
            <el-table-column label="流转类型" width="110" align="center">
              <template #default="scope">
                <el-tag size="small" effect="plain">{{ scope.row.flowType || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="转出 → 转入" min-width="180">
              <template #default="scope">
                {{ baseName(scope.row.fromBaseId) || '—' }} → {{ baseName(scope.row.toBaseId) || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="原因 / 医院结论" min-width="180" :show-overflow-tooltip="true">
              <template #default="scope">
                {{ [scope.row.flowReason, scope.row.hospitalName, scope.row.hospitalResult].filter(Boolean).join(' / ') || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="操作人" width="100" align="center" prop="operateBy" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 右：二维码 + 时间线 -->
      <el-col :xs="24" :md="9">
        <el-card shadow="never" class="block">
          <template #header><span>专属二维码</span></template>
          <div class="qr-box">
            <img v-if="profile.qrCode" :key="qrTick" :src="profile.qrCode" class="qr-img" alt="档案二维码" />
            <el-empty v-else description="尚未生成二维码" :image-size="50" />
            <div v-if="profile.scanUrl" class="qr-url">手机扫这个码，打开这只猫的档案：{{ profile.scanUrl }}</div>
            <div class="qr-tip">档案里改了名字、状态、病历都不用重打这张码，扫码看到的永远是最新内容</div>
            <div class="qr-actions">
              <el-button size="small" icon="Download" @click="downloadQr" :disabled="!profile.qrCode">保存图片</el-button>
              <el-button size="small" icon="Refresh" @click="handleRegenQr" v-hasPermi="['cat:profile:edit']">更新二维码</el-button>
            </div>
            <div class="qr-help">只有换了系统访问网址，才需要点「更新二维码」</div>
          </div>
        </el-card>

        <el-card shadow="never" class="block">
          <template #header>
            <div class="card-header">
              <span>病历时间线（{{ timeline.length }}）</span>
              <el-tooltip content="诊断、用药、疫苗、驱虫、转诊、流转、死亡等记录按时间倒序合并展示" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-empty v-if="!timeline.length" description="暂无病历记录" :image-size="60" />
          <el-timeline v-else class="timeline">
            <el-timeline-item
              v-for="(item, idx) in timeline"
              :key="idx"
              :timestamp="formatTime(item.time)"
              placement="top"
              :color="typeColor(item.type)"
            >
              <div class="tl-card">
                <div class="tl-head">
                  <el-tag size="small" :type="typeTag(item.type)" effect="light">{{ item.type }}</el-tag>
                  <span class="tl-title">{{ item.title || '—' }}</span>
                </div>
                <div class="tl-content" v-if="item.content">{{ item.content }}</div>
                <div class="tl-detail" v-if="item.detail">{{ item.detail }}</div>
                <div class="tl-detail" v-if="item.checkItems">检查项目：{{ item.checkItems }}</div>
                <div class="tl-detail warn" v-if="item.adverseReaction">不良反应：{{ item.adverseReaction }}</div>
                <div class="tl-correction" v-if="item.correctionNote">
                  <el-icon><WarningFilled /></el-icon>
                  更正（{{ item.correctionBy }} {{ formatTime(item.correctionTime) }}）：{{ item.correctionNote }}
                </div>
                <div class="tl-photos" v-if="item.photos">
                  <el-image
                    v-for="(u, i) in splitPhotos(item.photos)"
                    :key="i"
                    :src="resolveUrl(u)"
                    :preview-src-list="splitPhotos(item.photos).map(resolveUrl)"
                    :initial-index="i"
                    fit="cover"
                    preview-teleported
                    class="tl-photo"
                  />
                </div>
                <div class="tl-foot" v-if="item.operator">操作人：{{ item.operator }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" class="block" v-if="adoption">
          <template #header><span>领养信息</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="领养人">{{ adoption.adopterName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ adoption.adopterPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="领养日期">{{ adoption.adoptionDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="住址">{{ adoption.adopterAddress || '—' }}</el-descriptions-item>
            <el-descriptions-item label="领养协议">
              <el-button v-if="adoption.agreementUrl" link type="primary" @click="openAgreement">查看协议</el-button>
              <span v-else>未上传</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="block" v-if="death">
          <template #header><span>死亡登记</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="死亡日期">{{ death.deathDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="死亡地点">{{ death.deathPlace || '—' }}</el-descriptions-item>
            <el-descriptions-item label="死亡类型"><dict-tag :options="cat_death_type" :value="death.deathType" /></el-descriptions-item>
            <el-descriptions-item label="死因">{{ death.deathReason || '—' }}</el-descriptions-item>
            <el-descriptions-item label="遗体处理">{{ death.bodyDisposal || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="videoOpen" title="视频预览" width="640px" append-to-body destroy-on-close>
      <video :src="videoUrl" controls autoplay style="width: 100%"></video>
    </el-dialog>
  </div>
</template>

<script setup name="CatProfileDetail">
import { getCatArchive, archiveCatProfile, unarchiveCatProfile, regenerateCatQrCode } from "@/api/cat/profile"
import { listCatBase } from "@/api/cat/base"
import { listCatWard } from "@/api/cat/ward"

const { proxy } = getCurrentInstance()
const { cat_status, cat_sex, cat_source, cat_neutered, cat_yes_no, cat_death_type } = useDict(
  "cat_status", "cat_sex", "cat_source", "cat_neutered", "cat_yes_no", "cat_death_type"
)

const route = useRoute()
const router = useRouter()
const baseUrl = import.meta.env.VITE_APP_BASE_API

const loading = ref(true)
const profile = ref({})
// 二维码刷新计数：作为 <img> 的 key，保证重新生成后一定重新渲染
const qrTick = ref(0)
const tagNames = ref([])
const photos = ref([])
const flows = ref([])
const timeline = ref([])
const adoption = ref(null)
const death = ref(null)
const baseList = ref([])
const wardList = ref([])
const videoOpen = ref(false)
const videoUrl = ref("")

const catId = computed(() => route.params.catId)

const imagePreviewList = computed(() =>
  photos.value.filter(p => p.fileType !== '2').map(p => resolveUrl(p.fileUrl))
)

const locationText = computed(() => {
  const b = baseName(profile.value.baseId)
  const w = wardName(profile.value.wardId)
  if (b && w) return b + ' / ' + w
  return b || w || '—'
})

function resolveUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  return baseUrl + (url.startsWith('/') ? url : '/' + url)
}

function baseName(id) {
  if (!id) return ''
  const b = baseList.value.find(x => x.baseId === id)
  return b ? b.baseName : '#' + id
}

function wardName(id) {
  if (!id) return ''
  const w = wardList.value.find(x => x.wardId === id)
  return w ? w.wardName : '#' + id
}

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').substring(0, 16)
}

function splitPhotos(str) {
  if (!str) return []
  return String(str).split(',').map(s => s.trim()).filter(Boolean)
}

const TYPE_COLORS = {
  '诊断': '#e6a23c', '用药': '#67c23a', '疫苗': '#409eff', '驱虫': '#909399',
  '转诊': '#f56c6c', '流转': '#6b5b95', '死亡': '#303133', 'TNR': '#13c2c2',
  '放归': '#00b96b', '领养': '#722ed1'
}
const TYPE_TAGS = {
  '诊断': 'warning', '用药': 'success', '疫苗': 'primary', '驱虫': 'info',
  '转诊': 'danger', '流转': '', '死亡': 'info', 'TNR': 'primary', '放归': 'success', '领养': 'danger'
}
function typeColor(type) { return TYPE_COLORS[type] || '#909399' }
function typeTag(type) { return TYPE_TAGS[type] || 'info' }

function getData() {
  loading.value = true
  getCatArchive(catId.value).then(res => {
    const d = res.data || {}
    profile.value = d.profile || {}
    tagNames.value = d.tagNames || []
    photos.value = d.photos || []
    flows.value = d.flows || []
    timeline.value = d.timeline || []
    adoption.value = d.adoption || null
    death.value = d.death || null
    loading.value = false
  }).catch(() => { loading.value = false })
}

function getBaseList() {
  listCatBase({ pageNum: 1, pageSize: 500 }).then(res => { baseList.value = res.rows || [] })
}
function getWardList() {
  listCatWard({ pageNum: 1, pageSize: 500 }).then(res => { wardList.value = res.rows || [] })
}

function goBack() {
  router.push('/profile/profile')
}
function goEdit() {
  router.push({ path: '/profile/profile', query: { editCatId: catId.value } })
}
function goPhoto() {
  router.push({ path: '/profile/photo', query: { catId: catId.value } })
}
function goPrint() {
  router.push({ path: '/profile/qrcard', query: { catIds: catId.value } })
}

function downloadQr() {
  const src = profile.value.qrCode
  if (!src) return
  const a = document.createElement('a')
  a.href = src
  a.download = `二维码_${profile.value.archiveNo || profile.value.catId}.png`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

function handleRegenQr() {
  // 接口直接返回新二维码，就地替换，页面立即生效，不用整页重新拉档案
  regenerateCatQrCode(catId.value).then(res => {
    const data = res.data || {}
    if (data.qrCode) {
      profile.value.qrCode = data.qrCode
      profile.value.scanUrl = data.scanUrl
      qrTick.value++
    }
    if (data.changed === false) {
      proxy.$modal.msg('这个二维码已经是最新的，不用更新（它指向的档案网址没有变）')
    } else {
      proxy.$modal.msgSuccess('二维码已更新，现在指向新的档案网址')
    }
  })
}

function handleExportPdf() {
  proxy.downloadGet('cat/profile/pdf/' + catId.value, {}, `猫咪档案_${profile.value.archiveNo || catId.value}.pdf`)
}

function handleArchive() {
  proxy.$modal.confirm('归档后该猫咪不再出现在在养列表，可随时取消归档。是否继续？').then(() => {
    return archiveCatProfile(catId.value)
  }).then(() => {
    proxy.$modal.msgSuccess('已归档')
    getData()
  }).catch(() => {})
}

function handleUnarchive() {
  unarchiveCatProfile(catId.value).then(() => {
    proxy.$modal.msgSuccess('已取消归档')
    getData()
  })
}

function openVideo(p) {
  videoUrl.value = resolveUrl(p.fileUrl)
  videoOpen.value = true
}

function openAgreement() {
  const url = adoption.value.agreementUrl
  window.open(resolveUrl(url), '_blank')
}

getBaseList()
getWardList()
getData()
</script>

<style scoped>
.archive-detail :deep(.el-card__header) { padding: 10px 16px; }
.archive-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  flex-wrap: wrap; gap: 12px;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px; padding: 16px 18px; margin-bottom: 14px;
}
.header-main { display: flex; gap: 14px; align-items: flex-start; }
.avatar {
  width: 54px; height: 54px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #4a90d9, #6b5b95);
  color: #fff; font-size: 24px; display: flex; align-items: center; justify-content: center;
}
.name-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 6px; }
.cat-name { font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); }
.meta-row { display: flex; gap: 16px; flex-wrap: wrap; font-size: 13px; color: var(--el-text-color-secondary); }
.tag-row { margin-top: 8px; display: flex; gap: 6px; flex-wrap: wrap; }
.header-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.block { margin-bottom: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.qr-box { text-align: center; }
.qr-img { width: 160px; height: 160px; }
.qr-url { font-size: 12px; color: var(--el-text-color-secondary); word-break: break-all; margin-top: 6px; }
.qr-tip { font-size: 12px; color: var(--el-text-color-placeholder); margin: 8px 0; }
.qr-help { font-size: 12px; color: var(--el-text-color-placeholder); margin-top: 6px; }
.qr-actions { display: flex; gap: 8px; justify-content: center; }
.photo-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(96px, 1fr)); gap: 10px; }
.photo-item { text-align: center; }
.photo { width: 100%; height: 86px; border-radius: 6px; display: block; }
.photo.video { position: relative; cursor: pointer; background: #000; }
.play-icon { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); font-size: 24px; color: #fff; }
.photo-folder { font-size: 12px; color: var(--el-text-color-placeholder); margin-top: 4px; }
.timeline { padding-left: 2px; max-height: 720px; overflow-y: auto; }
.tl-card { background: var(--el-fill-color-light); border-radius: 6px; padding: 8px 10px; }
.tl-head { display: flex; align-items: center; gap: 8px; }
.tl-title { font-size: 13px; font-weight: 600; color: var(--el-text-color-primary); }
.tl-content { font-size: 13px; color: var(--el-text-color-regular); margin-top: 4px; }
.tl-detail { font-size: 12px; color: var(--el-text-color-secondary); margin-top: 3px; }
.tl-detail.warn { color: var(--el-color-danger); }
.tl-correction {
  margin-top: 6px; font-size: 12px; color: var(--el-color-warning);
  background: var(--el-color-warning-light-9); border-radius: 4px; padding: 4px 6px;
  display: flex; gap: 4px; align-items: flex-start;
}
.tl-photos { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 6px; }
.tl-photo { width: 60px; height: 60px; border-radius: 4px; }
.tl-foot { font-size: 12px; color: var(--el-text-color-placeholder); margin-top: 4px; }
</style>
