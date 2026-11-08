<template>
  <div class="app-container qrcard-page">
    <!-- 筛选区（打印时隐藏） -->
    <el-card shadow="never" class="no-print">
      <template #header>
        <div class="card-header">
          <span>猫咪信息卡打印</span>
          <span class="tip">勾选需要打印的猫咪，A4 纸每页 2 张（A5 横），剪下即可张贴到笼位</span>
        </div>
      </template>
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="名字 / 档案编号" clearable style="width: 180px" @keyup.enter="loadCats" />
        </el-form-item>
        <el-form-item label="基地">
          <el-select v-model="query.baseId" placeholder="全部基地" clearable style="width: 160px" @change="loadCats">
            <el-option v-for="b in baseList" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px" @change="loadCats">
            <el-option v-for="d in cat_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadCats">搜索</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-checkbox v-model="checkAll" :indeterminate="indeterminate" @change="handleCheckAll">全选当前列表</el-checkbox>
        <span class="selected-tip">已选 {{ selected.length }} 只</span>
        <el-button size="small" type="primary" plain icon="Printer" :disabled="!printList.length" @click="handlePrint">打印选中卡片</el-button>
        <el-button size="small" :type="uploading ? 'warning' : 'info'" plain icon="Refresh" :loading="uploading" @click="handleRefresh">刷新</el-button>
      </div>

      <el-table ref="tableRef" :data="catList" size="small" border max-height="320" @selection-change="handleSelectionChange" row-key="catId">
        <el-table-column type="selection" width="45" reserve-selection />
        <el-table-column label="序号" type="index" :index="i => $seqIndex(i)" width="56" align="center" />
        <el-table-column label="档案编号" prop="archiveNo" width="150" />
        <el-table-column label="名字" prop="catName" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="scope"><dict-tag :options="cat_status" :value="scope.row.status" /></template>
        </el-table-column>
        <el-table-column label="所在基地" min-width="120">
          <template #default="scope">{{ baseName(scope.row.baseId) || '—' }}</template>
        </el-table-column>
        <el-table-column label="二维码" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.hasQrCode === 1" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">无</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 打印预览区 -->
    <div class="preview-wrap">
      <div class="preview-title no-print">打印预览（{{ printList.length }} 张）</div>
      <div class="card-sheet" id="printSheet" ref="sheetRef">
        <el-empty v-if="!printList.length" description="在上方勾选猫咪，预览实时生成" :image-size="70" class="no-print" />
        <div class="card-box" v-for="c in printList" :key="c.catId">
          <!-- 卡片工具条：只在屏幕上显示，打印时隐藏 -->
          <div class="card-tools no-print">
            <span class="cover-tip">
              <el-icon><PictureFilled /></el-icon>
              封面：{{ coverLabel(c) }}
            </span>
            <div class="cover-ops">
              <el-button size="small" plain icon="Picture" @click="openPicker(c)">选照片</el-button>
              <el-button size="small" type="primary" plain icon="Camera" @click="quickShot(c)" v-hasPermi="['cat:photo:add']">拍照</el-button>
              <el-button v-if="pickedMap[c.catId]" size="small" text icon="RefreshLeft" @click="resetCover(c)">恢复默认</el-button>
            </div>
          </div>
          <div class="cat-card" :style="zoomStyle">
          <table class="card-tb">
            <colgroup>
              <col class="col-photo" />
              <col class="col-name" />
              <col class="col-sex" />
              <col class="col-neut" />
              <col class="col-weight" />
              <col class="col-qr" />
            </colgroup>
            <tbody>
              <!-- 标题行：左侧特征照片、右侧二维码，都纵向跨下面三行 -->
              <tr>
                <td rowspan="4" class="photo-cell">
                  <img v-if="coverSrc(c)" :src="coverSrc(c)" :data-orig="coverOrig(c)" class="cat-photo" alt="特征照片" @error="onImgError" />
                  <div v-else class="photo-ph">无照片</div>
                  <div class="photo-cap">{{ coverFold(c) || '特征照片' }}</div>
                </td>
                <td colspan="4" class="card-title">
                  猫咪信息卡
                  <span class="card-meta">{{ c.archiveNo }}<template v-if="statusLabel(c.status)">｜{{ statusLabel(c.status) }}</template></span>
                </td>
                <td rowspan="4" class="qr-cell">
                  <img v-if="c.qrCode" :src="c.qrCode" class="qr" alt="二维码" />
                  <div v-else class="qr-ph">无二维码</div>
                  <div class="scan-tip">扫码查看档案</div>
                </td>
              </tr>
              <!-- 位置行：卡片挂在笼子上，走过来第一眼要看的就是"哪只猫、在哪间屋" -->
              <tr>
                <td colspan="4" class="card-loc">{{ locText(c) }}</td>
              </tr>
              <!-- 姓名 / 性别 / 绝育 / 体重（体重纵向跨两行） -->
              <tr>
                <td class="cell mid"><span class="lb">姓名：</span><span class="val">{{ c.catName }}</span></td>
                <td class="cell mid"><span class="lb">性别：</span><span class="val">{{ sexLabel(c.sex) }}</span></td>
                <td class="cell mid"><span class="lb">绝育：</span><span class="val">{{ neuteredLabel(c.neutered) }}</span></td>
                <td rowspan="2" class="cell mid"><span class="lb">体重（KG）：</span><span class="val block">{{ weightVal(c.weight) }}</span></td>
              </tr>
              <tr>
                <td colspan="3" class="cell mid"><span class="lb">疫苗：</span><span class="val">{{ c.vaccineText }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell"><span class="lb">入住医院及日期：</span><span class="val">{{ c.hospitalText }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell tall"><span class="lb">病情：</span><span class="val pre">{{ clamp(c.diagnosisText, 130) }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell tall"><span class="lb">用药：</span><span class="val pre">{{ medText(c.medicationText) }}</span></td>
              </tr>
              <tr>
                <td colspan="6" class="cell tall"><span class="lb">注意事项：</span><span class="val pre">{{ clamp(c.remark, 160) }}</span></td>
              </tr>
            </tbody>
          </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 选照片：同一只猫的照片按上传时间倒序，最新一张排第一并标「默认」 -->
    <el-dialog
      v-model="picker.open"
      :title="`选择卡片照片 · ${picker.catName || ''}`"
      width="min(760px, 94vw)"
      append-to-body
      destroy-on-close
      class="photo-picker"
    >
      <div class="pk-head">
        <div class="pk-actions">
          <div class="pk-upload" v-hasPermi="['cat:photo:add']">
            <el-button icon="Camera" type="primary" plain @click="triggerCamera">拍照上传</el-button>
            <el-button icon="Picture" plain @click="triggerAlbum">从相册选择</el-button>
          </div>
          <span v-if="picker.loading" class="pk-loading">加载中…</span>
          <span v-else class="pk-count">共 {{ pickerPhotos.length }} 张</span>
        </div>
        <div class="pk-tip">
          默认用最新上传的一张（排第一）。挑一张只影响这张卡，不选就按默认打印。
        </div>
      </div>

      <div class="pk-grid" v-loading="picker.loading">
        <div
          v-for="(p, i) in pickerPhotos"
          :key="p.photoId"
          class="pk-item"
          :class="{ active: isActive(p) }"
          @click="pickPhoto(p)"
        >
          <div class="pk-img">
            <img :src="resolveUrl(p.thumbUrl || p.fileUrl)" :data-orig="resolveUrl(p.fileUrl)" alt="" @error="onImgError" />
            <span v-if="i === 0" class="pk-badge">最新</span>
            <span v-if="isActive(p)" class="pk-check"><el-icon><Select /></el-icon></span>
          </div>
          <div class="pk-meta">
            <span class="pk-date">{{ dateOnly(p.createTime) }}</span>
            <span class="pk-folder">{{ p.folder || '未分类' }}</span>
          </div>
        </div>
        <el-empty
          v-if="!picker.loading && !pickerPhotos.length"
          description="这只猫还没有照片，点上面「拍照上传」"
          :image-size="60"
        />
      </div>

      <template #footer>
        <el-button @click="useDefault">设为默认（最新一张）</el-button>
        <el-button type="primary" @click="picker.open = false">完成</el-button>
      </template>
    </el-dialog>

    <!-- 手机拍照 / 相册选择：capture 直接调起后置相机 -->
    <input ref="cameraInput" type="file" accept="image/*" capture="environment" class="hidden-input" @change="onFilePicked" />
    <input ref="albumInput" type="file" accept="image/*" class="hidden-input" @change="onFilePicked" />
  </div>
</template>

<script setup name="CatQrCard">
import { listCatProfile, listCatCards } from "@/api/cat/profile"
import { listCatPhoto, addCatPhoto } from "@/api/cat/photo"
import { listCatBase } from "@/api/cat/base"
import { getToken } from "@/utils/auth"
import { onMasterDataChanged, MASTER_KIND } from "@/utils/catCache"

const { proxy } = getCurrentInstance()
const { cat_status, cat_sex, cat_neutered } = useDict("cat_status", "cat_sex", "cat_neutered")
const route = useRoute()

const tableRef = ref(null)
const catList = ref([])
const baseList = ref([])
const selected = ref([])
const printList = ref([])
const checkAll = ref(false)
const indeterminate = ref(false)

const query = reactive({
  // 用统一关键词搜：名字、档案编号都能命中（后端 keyword 还兼搜品种/性别/年龄）
  keyword: undefined,
  baseId: undefined,
  status: undefined
})

const baseUrl = import.meta.env.VITE_APP_BASE_API

// 照片存的是 /profile/xx 相对路径，展示前补接口前缀
function resolveUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  return baseUrl + (url.startsWith('/') ? url : '/' + url)
}

function baseName(id) {
  if (!id) return ''
  const b = baseList.value.find(x => x.baseId === id)
  return b ? b.baseName : ''
}

/**
 * 上传一张图片到服务器，返回文件地址（/profile/...）
 * 走和相册同一个 /common/upload，拿到的 url 直接写进 cat_photo
 * 这里用 fetch 而不是 axios：axios 默认 JSON 头会把 FormData 转成 JSON，上传会失败
 */
async function uploadImage(file) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await fetch(baseUrl + '/common/upload', {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + getToken() },
    body: fd
  })
  if (!res.ok) throw new Error('HTTP ' + res.status)
  const data = await res.json()
  if (data.code !== 200) throw new Error(data.msg || '上传失败')
  return data
}

// 接口返回的是带域名+端口的绝对地址，库里统一存相对路径，换域名/端口也不会坏
function toRelative(url) {
  return String(url || '').replace(/^https?:\/\/[^/]+/i, '')
}

function dictLabel(list, value) {
  const d = (list || []).find(x => String(x.value) === String(value))
  return d ? d.label : ''
}
function sexLabel(v) { return dictLabel(cat_sex.value, v) || '—' }
function neuteredLabel(v) { return dictLabel(cat_neutered.value, v) || '—' }
function statusLabel(v) { return dictLabel(cat_status.value, v) || '' }

/**
 * 卡片位置行：基地 · 病房
 * 没分病房（待安置 / 外送中）时只显示基地，并标一句提示，避免让人以为漏打。
 */
function locText(c) {
  const base = c.baseName || ''
  const ward = c.wardName || ''
  if (!base && !ward) return '位置：未分配'
  if (!ward) return `${base} · 未分配病房`
  return `${base} · ${ward}`
}

// 体重库里存的是自由文本（如 4.5kg），表格已有 KG 单位，这里把单位去掉
function weightVal(w) {
  if (!w) return ''
  return String(w).replace(/\s*(kg|KG|Kg|公斤|千克)\s*/g, '').trim()
}

// 用药只留最近 4 行，超出用省略提示，避免撑破格子
function medText(text) {
  const lines = String(text || '').split('\n').map(s => s.trim()).filter(Boolean)
  if (!lines.length) return ''
  if (lines.length <= 4) return lines.join('\n')
  return lines.slice(0, 3).join('\n') + '\n…共 ' + lines.length + ' 条'
}

// 长文本截断，保证打印时格子高度可控（卡片是固定高度表单）
function clamp(text, n) {
  const s = String(text || '')
  return s.length > n ? s.slice(0, n) + '…' : s
}

function loadCats() {
  return listCatProfile({ pageNum: 1, pageSize: 500, ...query }).then(res => {
    catList.value = res.rows || []
  })
}

// ---- 卡片封面：默认取最新一张，也可以现场拍照或从相册里挑一张 ----
// 照片列表按上传时间倒序（接口已排序），最新一张排第一，就是「默认」
const photoMap = ref({})      // catId -> 该猫的照片列表
const pickedMap = ref({})     // catId -> 手动选中的照片；没有键就是走默认
const loadedCats = ref({})    // catId -> 是否已拉过照片，避免全选时反复请求
const uploading = ref(false)
const cameraInput = ref(null)
const albumInput = ref(null)
// 现场拍的这张只为打印用，统一归到「正面照」，不再让用户选分类
const UPLOAD_FOLDER = '正面照'

const picker = reactive({ open: false, catId: null, catName: '', loading: false })
const pickerPhotos = computed(() => (picker.catId ? photosOf(picker.catId) : []))

function photosOf(catId) {
  return photoMap.value[catId] || []
}

/** 默认封面：最新一张；「信息卡」是拍下来的纸质卡，排到最后兜底，与后端口径一致 */
function defaultPhoto(catId) {
  const list = photosOf(catId)
  return list.find(p => p.folder !== '信息卡') || list[0] || null
}

/** 当前这张卡实际用的照片（手选了就用手选的，否则默认） */
function currentPhoto(catId) {
  return pickedMap.value[catId] || defaultPhoto(catId) || null
}

function coverSrc(c) {
  const p = currentPhoto(c.catId)
  if (p) return resolveUrl(p.thumbUrl || p.fileUrl)
  // 照片列表还没回来时先用手接口给的默认值，避免预览闪空
  return resolveUrl(c.photoUrl)
}

function coverFold(c) {
  const p = currentPhoto(c.catId)
  return p ? (p.folder || '') : (c.photoFolder || '')
}

/** 缩略图缺失时回退原图（历史数据的 thumb 可能没生成） */
function coverOrig(c) {
  const p = currentPhoto(c.catId)
  return resolveUrl((p && p.fileUrl) || c.photoUrl)
}

function onImgError(e) {
  const img = e.target
  const orig = img.dataset.orig
  if (orig && img.src !== orig) img.src = orig
}

function coverLabel(c) {
  const p = currentPhoto(c.catId)
  if (!p) return '无照片'
  const day = dateOnly(p.createTime) || '—'
  const tag = p.folder || '未分类'
  const mode = pickedMap.value[c.catId] ? '已选' : '默认最新'
  return `${mode} · ${day} · ${tag}`
}

function dateOnly(t) {
  return t ? String(t).replace('T', ' ').substring(0, 10) : ''
}

/**
 * 拉这几只猫的照片；force=true 时强制重取（刚上传、或点了刷新）
 * 一次只拉需要的猫，全选 20 只也只是 20 个并行请求，比接口里 N+1 更可控
 */
async function loadPhotosFor(catIds, force = false) {
  const ids = (catIds || []).filter(id => id && (force || !loadedCats.value[id]))
  if (!ids.length) return
  await Promise.all(ids.map(async id => {
    try {
      const res = await listCatPhoto({ catId: id, fileType: '1', pageNum: 1, pageSize: 200 })
      photoMap.value[id] = res.rows || []
      loadedCats.value[id] = true
    } catch (e) {
      photoMap.value[id] = []
    }
  }))
}

function openPicker(c) {
  picker.catId = c.catId
  picker.catName = c.catName
  picker.open = true
  picker.loading = true
  loadPhotosFor([c.catId], true).finally(() => { picker.loading = false })
}

function isActive(p) {
  const cur = picker.catId ? currentPhoto(picker.catId) : null
  return !!cur && cur.photoId === p.photoId
}

function pickPhoto(p) {
  if (!picker.catId) return
  pickedMap.value[picker.catId] = p
}

function useDefault() {
  if (!picker.catId) return
  delete pickedMap.value[picker.catId]
}

function resetCover(c) {
  delete pickedMap.value[c.catId]
}

function triggerCamera() {
  cameraInput.value && cameraInput.value.click()
}

function triggerAlbum() {
  albumInput.value && albumInput.value.click()
}

/** 手机端点卡片上的「拍照」：直接调起相机，拍完上传并设为这张卡的封面 */
function quickShot(c) {
  picker.catId = c.catId
  picker.catName = c.catName
  triggerCamera()
}

async function onFilePicked(e) {
  const input = e.target
  const file = input.files && input.files[0]
  input.value = '' // 清空，同一张图再选一次也能触发 change
  if (!file) return
  const catId = picker.catId
  if (!catId) return
  if (!/^image\//i.test(file.type || '')) {
    proxy.$modal.msgWarning('请选择图片文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    proxy.$modal.msgWarning('图片不能超过 10MB')
    return
  }
  uploading.value = true
  picker.loading = picker.open
  try {
    const up = await uploadImage(file)
    const url = toRelative(up && (up.url || (up.data && up.data.url)))
    if (!url) throw new Error('未返回文件地址')
    await addCatPhoto({
      catId,
      folder: UPLOAD_FOLDER,
      fileType: '1',
      fileUrl: url,
      remark: `信息卡打印时现场上传（${new Date().toLocaleDateString('zh-CN')}）`
    })
    await loadPhotosFor([catId], true)
    const list = photosOf(catId)
    if (list.length) pickedMap.value[catId] = list[0]
    proxy.$modal.msgSuccess('照片已上传，并设为这张卡的封面')
  } catch (err) {
    proxy.$modal.msgError('照片上传失败，请重试')
  } finally {
    uploading.value = false
    picker.loading = false
  }
}

/** 窄屏时整张卡等比缩小，手机上也能一屏看到完整卡片（打印时强制还原） */
const sheetRef = ref(null)
const cardZoom = ref(1)
const zoomStyle = computed(() => (cardZoom.value < 1 ? { zoom: String(cardZoom.value) } : undefined))

// 190mm 在 96dpi 下约 718px
const CARD_BASE_WIDTH = 718

function syncZoom() {
  if (typeof window === 'undefined') return
  // 用预览容器的实际宽度来算：窗口宽度里还含侧栏和页面内边距，直接拿 innerWidth 会偏大，卡片还是会溢出
  const sheetWidth = sheetRef.value ? sheetRef.value.clientWidth : 0
  const avail = (sheetWidth || Math.min(window.innerWidth || 0, document.documentElement.clientWidth || 0) - 32) - 4
  cardZoom.value = avail >= CARD_BASE_WIDTH ? 1 : Math.max(0.3, Number((avail / CARD_BASE_WIDTH).toFixed(3)))
}

// 勾选后卡片才渲染出来，此时再按容器宽度同步一次
watch(printList, () => nextTick(syncZoom))

onMounted(() => {
  nextTick(syncZoom)
  window.addEventListener('resize', syncZoom)
  window.addEventListener('orientationchange', syncZoom)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', syncZoom)
  window.removeEventListener('orientationchange', syncZoom)
})

function getBaseList() {
  listCatBase({ pageNum: 1, pageSize: 500 }).then(res => { baseList.value = res.rows || [] })
}

function handleSelectionChange(rows) {
  selected.value = rows
  indeterminate.value = rows.length > 0 && rows.length < catList.value.length
  checkAll.value = rows.length > 0 && rows.length === catList.value.length
  // 勾选即实时刷新预览
  refreshPreview()
}

function handleCheckAll(val) {
  tableRef.value && tableRef.value.clearSelection()
  if (val) {
    catList.value.forEach(row => tableRef.value.toggleRowSelection(row, true))
  }
}

// 勾选变化时实时更新打印预览（不调用 window.print）
let previewSeq = 0
function refreshPreview() {
  const rows = selected.value
  if (!rows.length) {
    printList.value = []
    return
  }
  const ids = rows.map(c => c.catId)
  // 用序号防抖：全选会连续触发多次 selection-change，只认最后一次请求
  const seq = ++previewSeq
  listCatCards(ids).then(res => {
    if (seq !== previewSeq) return
    const order = new Map(ids.map((id, i) => [id, i]))
    const list = (res.data || []).slice().sort((a, b) => (order.get(a.catId) ?? 0) - (order.get(b.catId) ?? 0))
    printList.value = list
    // 照片列表决定每张卡用哪张图，先拉回来，预览里的封面才是对的
    loadPhotosFor(ids)
  })
}

// 全站统一口径：各入口跳过来只做定位，绝不自动调起打印，一律由用户点「打印选中卡片」

/** 等卡片里的图片画完再打印；超时也照打，不能因为一张图卡住整张卡 */
function printWhenImagesReady(timeout) {
  nextTick(() => {
    const imgs = Array.from(document.querySelectorAll('#printSheet img'))
    const pending = imgs.filter(img => !img.complete)
    if (!pending.length) {
      window.print()
      return
    }
    let left = pending.length
    let fired = false
    const fire = () => {
      if (fired) return
      fired = true
      window.print()
    }
    const timer = setTimeout(fire, timeout)
    const tick = () => {
      if (--left <= 0) {
        clearTimeout(timer)
        fire()
      }
    }
    pending.forEach(img => {
      img.addEventListener('load', tick, { once: true })
      img.addEventListener('error', tick, { once: true })
    })
  })
}

// 真正打印：预览已经就绪，这里只调起打印对话框
function handlePrint() {
  if (!printList.value.length) {
    proxy.$modal.msgWarning('请先勾选需要打印的猫咪')
    return
  }
  printWhenImagesReady(2000)
}

function handleRefresh() {
  // 别处可能刚传了新照片，清掉缓存重新拉
  loadedCats.value = {}
  loadCats().then(() => {
    if (selected.value.length) refreshPreview()
  })
}

// 从档案详情页 / 用药记录页带过来的 catIds，进入即自动选中
function applyRouteCatIds() {
  const raw = route.query.catIds
  if (!raw) return
  const ids = String(raw).split(',').map(s => Number(s)).filter(n => !isNaN(n))
  if (!ids.length) return
  nextTick(() => {
    if (!tableRef.value) return
    // 页面被 keep-alive 缓存，再次跳进来时先清掉上一次的勾选，只留这一只
    tableRef.value.clearSelection()
    catList.value.forEach(row => {
      if (ids.includes(row.catId)) {
        tableRef.value.toggleRowSelection(row, true)
      }
    })
  })
}

getBaseList()
loadCats().then(() => applyRouteCatIds())

// key 只到 path，同一路径换 catIds 组件不会重建，必须监听路由重新勾选
watch(() => route.query.catIds, () => applyRouteCatIds())

// 基地改名/新增后，打印卡的基地筛选与卡片上的基地名立即更新
onMasterDataChanged(MASTER_KIND.BASE, () => {
  getBaseList()
})
</script>

<style scoped>
.card-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.tip { font-size: 12px; color: var(--el-text-color-placeholder); }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; flex-wrap: wrap; }
.selected-tip { font-size: 13px; color: var(--el-text-color-secondary); }
.preview-wrap { margin-top: 16px; }
.preview-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; }

/* 预览区：与打印尺寸一致（190mm × 130mm），所见即所得 */
.card-sheet {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8mm;
  background: #f5f7fa;
  padding: 8mm 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  min-height: 200px;
}
.cat-card {
  width: 190mm;
  background: #fff;
  page-break-inside: avoid;
  break-inside: avoid;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
}

/* 卡片外壳：工具条 + 卡片本体 */
.card-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.card-tools {
  width: min(190mm, 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  padding: 6px 8px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}
.cover-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.cover-ops { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }

/* 手机拍照 / 相册选择用的隐藏 input */
.hidden-input { display: none; }

/* ---- 选照片弹窗 ---- */
.pk-head { margin-bottom: 10px; }
.pk-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.pk-upload { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.pk-count, .pk-loading { font-size: 12px; color: var(--el-text-color-secondary); margin-left: auto; }
.pk-tip {
  margin-top: 8px;
  padding: 6px 10px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.pk-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(118px, 1fr));
  gap: 10px;
  max-height: 52vh;
  overflow-y: auto;
  padding: 2px;
}
.pk-item {
  border: 2px solid transparent;
  border-radius: 6px;
  padding: 4px;
  cursor: pointer;
  background: var(--el-fill-color-lighter);
  transition: border-color 0.15s;
}
.pk-item:hover { border-color: var(--el-color-primary-light-5); }
.pk-item.active { border-color: var(--el-color-primary); background: var(--el-color-primary-light-9); }
.pk-img {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 4px;
  overflow: hidden;
  background: #f2f3f5;
}
.pk-img img { width: 100%; height: 100%; object-fit: cover; display: block; }
.pk-badge {
  position: absolute;
  left: 4px;
  top: 4px;
  padding: 0 5px;
  font-size: 10px;
  line-height: 16px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 8px;
}
.pk-check {
  position: absolute;
  right: 4px;
  bottom: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
.pk-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
.pk-folder {
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 卡片本体：与纸质表单一致的黑框表格 */
.card-tb {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: #fff;
}
.col-photo { width: 30mm; }
.col-name { width: 50mm; }
.col-sex { width: 24mm; }
.col-neut { width: 28mm; }
.col-weight { width: 32mm; }
.col-qr { width: 26mm; }

.card-tb td {
  border: 1px solid #333;
  padding: 1.6mm 2mm;
  font-size: 10.5pt;
  line-height: 1.5;
  color: #000;
  vertical-align: top;
  word-break: break-word;
}
.card-title {
  position: relative;
  height: 12mm;
  text-align: center;
  vertical-align: middle !important;
  font-size: 18pt;
  font-weight: 700;
  letter-spacing: 4px;
}
.card-meta {
  position: absolute;
  right: 3mm;
  bottom: 1.2mm;
  font-size: 7.5pt;
  font-weight: 400;
  letter-spacing: 0;
  color: #666;
}
/* 位置行：基地 · 病房，加粗居中，隔着笼子也能一眼看清 */
.card-loc {
  text-align: center;
  vertical-align: middle !important;
  font-size: 12pt;
  font-weight: 700;
  letter-spacing: 1px;
}
.card-tb tr:nth-child(2) td { height: 9mm; }
.card-tb tr:nth-child(3) td,
.card-tb tr:nth-child(4) td { height: 13mm; }
.card-tb tr:nth-child(5) td { height: 14mm; }
.cell.mid { vertical-align: middle; }
.cell.tall { height: 26mm; }
.lb { color: #000; font-size: 10pt; }
.val { color: #000; }
.val.block { display: block; margin-top: 1.5mm; font-size: 13pt; }
.val.pre { white-space: pre-wrap; }

.qr-cell {
  text-align: center;
  vertical-align: middle !important;
  padding: 1.5mm 1mm !important;
}
.qr { width: 23mm; height: 23mm; display: block; margin: 0 auto; }
.qr-ph {
  width: 23mm;
  height: 23mm;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8pt;
  color: #999;
  border: 1px dashed #bbb;
}
.scan-tip { font-size: 7.5pt; color: #666; margin-top: 0.8mm; }

/* 特征照片：贴左侧，纵向跨标题+姓名+疫苗三行 */
.photo-cell {
  text-align: center;
  vertical-align: middle !important;
  padding: 1mm !important;
}
.cat-photo {
  width: 27mm;
  height: 31mm;
  object-fit: cover;
  object-position: center;
  display: block;
  margin: 0 auto;
}
.photo-ph {
  width: 27mm;
  height: 31mm;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8pt;
  color: #999;
  border: 1px dashed #bbb;
}
.photo-cap {
  font-size: 6.5pt;
  color: #666;
  margin-top: 0.6mm;
  line-height: 1.2;
}

/* ---- 手机端：卡片按容器宽度等比缩小，工具条不再撑破屏幕 ---- */
@media screen and (max-width: 768px) {
  .card-sheet {
    padding: 12px 0;
    gap: 12px;
    /* 缩放万一没生效（部分内核不支持 zoom），至少还能横向滚动看全整张卡 */
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  .card-box { width: 100%; }
  .card-tools {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
    padding: 8px;
  }
  .cover-tip { justify-content: center; white-space: normal; word-break: break-all; }
  .cover-ops { width: 100%; }
  .cover-ops .el-button {
    flex: 1 1 0;
    margin-left: 0 !important;
  }
}
</style>

<!-- 打印样式：只留卡片，隐藏左侧菜单、顶栏、筛选区 -->
<style>
@media print {
  @page { size: A4 portrait; margin: 8mm; }
  /* 若依布局的外层容器带固定高度 + overflow，不解除会被压成一页 */
  html, body, #app, .app-wrapper, .main-container, .app-main {
    height: auto !important;
    min-height: 0 !important;
    max-height: none !important;
    overflow: visible !important;
  }
  body { background: #fff !important; }
  /* 侧栏收起后主内容仍带着 200px 的左外边距，打印时必须清掉，否则整卡右移出纸 */
  .main-container { margin-left: 0 !important; width: 100% !important; }
  .sidebar-container, .navbar, .tags-view-container, .no-print,
  .app-main > .app-container > .no-print { display: none !important; }
  .app-main { padding: 0 !important; margin: 0 !important; width: auto !important; }
  .qrcard-page { padding: 0 !important; }
  .preview-wrap { margin: 0 !important; }
  .card-sheet {
    display: block !important;
    background: #fff !important;
    border: none !important;
    padding: 0 !important;
    border-radius: 0 !important;
  }
  .cat-card {
    box-shadow: none !important;
    margin: 0 0 8mm 0 !important;
    page-break-inside: avoid;
    break-inside: avoid;
    /* 手机上为了看全整卡做过缩放，打印必须还原成真实尺寸 */
    zoom: 1 !important;
  }
  .card-box { display: block !important; }
  .cat-card:last-child { margin-bottom: 0 !important; }
  .card-tb td { border: 1px solid #000 !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
}
</style>
