<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="猫咪" prop="catId">
        <cat-select v-model="queryParams.catId" placeholder="全部猫咪" style="width: 200px" />
      </el-form-item>
      <el-form-item label="品种" prop="breed">
        <el-select v-model="queryParams.breed" placeholder="全部品种" clearable filterable style="width: 140px">
          <el-option v-for="b in breedOptions" :key="b" :label="b" :value="b" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源基地" prop="baseId">
        <el-select v-model="queryParams.baseId" placeholder="全部基地" clearable style="width: 170px" @change="onFilterBaseChange">
          <el-option v-for="b in baseOptions" :key="b.baseId" :label="b.baseName" :value="b.baseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属房间" prop="wardId">
        <el-select v-model="queryParams.wardId" placeholder="全部房间" clearable style="width: 160px">
          <el-option v-for="w in wardFilterOptions" :key="w.wardId" :label="w.wardName" :value="w.wardId" />
        </el-select>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-select v-model="queryParams.sex" placeholder="全部" clearable style="width: 110px">
          <el-option v-for="d in cat_sex" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="相册" prop="folder">
        <el-select v-model="queryParams.folder" placeholder="全部相册" clearable style="width: 140px" :disabled="viewMode === 'cats'">
          <el-option v-for="d in cat_photo_folder" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="全部" clearable style="width: 110px">
          <el-option label="图片" value="1" />
          <el-option label="视频" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 从某只猫的档案点「管理相册」跳进来时，明确告诉用户当前只看这一只，别以为照片丢了 -->
    <div class="cat-scope" v-if="queryParams.catId">
      <span class="cat-scope-label">当前只看</span>
      <el-tag type="primary" effect="plain" closable @close="clearCatScope">{{ catLabel(queryParams.catId) }}</el-tag>
      <span class="cat-scope-label">的相册，共 {{ total }}{{ viewMode === 'grid' ? ' 组' : ' 条' }}</span>
      <el-button link type="primary" @click="clearCatScope">看全部猫咪</el-button>
    </div>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:photo:add']">上传照片/视频</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="RefreshRight" :loading="rebuilding" @click="handleRebuildThumb" v-hasPermi="['cat:photo:edit']">重建缩略图</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-if="viewMode === 'table'" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['cat:photo:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="grid">照片相册</el-radio-button>
          <el-radio-button label="cats">信息卡</el-radio-button>
          <el-radio-button label="table">列表视图</el-radio-button>
        </el-radio-group>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-alert
      v-if="ffmpegAvailable === false"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
      title="未检测到 ffmpeg，视频封面将由内置解码器生成；若解码失败会自动用视频首帧展示。"
    />

    <!-- 相册视图：按猫分组，一只猫一张封面卡，点进分组再左右翻看它全部照片 -->
    <div v-if="viewMode === 'grid'" v-loading="loading" class="album-grid">
      <el-empty v-if="list.length === 0" description="暂无照片" />
      <div class="album-item" v-for="c in list" :key="c.catId" @click="openAlbum(c)">
        <div class="album-cover">
          <el-image v-if="c.coverUrl" :src="resolveUrl(c.coverUrl)" fit="cover" class="album-img" lazy>
            <template #error>
              <div class="photo-error"><el-icon><Picture /></el-icon></div>
            </template>
          </el-image>
          <div v-else class="album-img photo-error"><el-icon><Picture /></el-icon></div>
          <span class="album-count">{{ c.photoCount || 0 }} 张</span>
        </div>
        <div class="album-title" :title="formatCatLabel(c.catName, c.archiveNo)">
          {{ formatCatLabel(c.catName, c.archiveNo) }}
        </div>
        <div class="album-attrs">{{ albumAttrs(c) }}</div>
        <div class="album-foot">
          <span class="album-time">{{ (c.latestTime || '').slice(0, 10) }}</span>
          <el-button link type="primary" @click.stop="goDetail(c.catId)">档案</el-button>
        </div>
      </div>
    </div>

    <!-- 信息卡视图（只看相册分类为「信息卡」的照片） -->
    <div v-else-if="viewMode === 'cats'" v-loading="loading" class="photo-grid">
      <el-empty v-if="list.length === 0" description="暂无照片" />
      <div class="photo-item" v-for="item in list" :key="item.photoId">
        <!-- 视频：显示封面 + 播放按钮 -->
        <div v-if="isVideo(item)" class="photo-thumb" @click="openVideo(item)">
          <el-image v-if="item.thumbUrl" :src="thumbSrc(item)" fit="cover" class="photo-img" @error="onThumbError(item)">
            <template #error>
              <video :src="resolveUrl(item.fileUrl)" preload="metadata" muted playsinline class="photo-img video-fallback"></video>
            </template>
          </el-image>
          <video v-else :src="resolveUrl(item.fileUrl)" preload="metadata" muted playsinline class="photo-img video-fallback"></video>
          <span class="play-btn"><el-icon><VideoPlay /></el-icon></span>
          <el-tag size="small" type="danger" effect="dark" class="type-badge">视频</el-tag>
        </div>
        <!-- 图片：缩略图 + 大图预览 -->
        <el-image
          v-else
          :src="thumbSrc(item)"
          :preview-src-list="imagePreviewList"
          :initial-index="previewIndex(item)"
          fit="cover"
          class="photo-img"
          preview-teleported
          @error="onThumbError(item)"
        >
          <template #error>
            <div class="photo-error"><el-icon><Picture /></el-icon></div>
          </template>
        </el-image>
        <div class="photo-meta">
          <el-tag size="small" type="info">{{ item.folder || '未分类' }}</el-tag>
          <span class="photo-cat" :title="catLabel(item.catId)">{{ catLabel(item.catId) }}</span>
        </div>
        <!-- 卡片下方直接带出档案属性：品种 / 性别 / 来源基地 / 所属房间 -->
        <div class="photo-attrs">
          <span>{{ item.breed || '品种未知' }}</span>
          <span class="dot">·</span>
          <span>{{ sexText(item.sex) }}</span>
          <span class="dot">·</span>
          <span>{{ item.baseName || '未分配基地' }}</span>
          <span class="dot">·</span>
          <span>{{ item.wardName || '未分房间' }}</span>
        </div>
        <div class="photo-ops">
          <el-button link type="primary" icon="Document" @click="goDetail(item.catId)">档案</el-button>
          <el-button link type="primary" @click="handleUpdate(item)" v-hasPermi="['cat:photo:edit']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(item)" v-hasPermi="['cat:photo:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <el-table v-else v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="$route.meta.canRemove" />
      <el-table-column label="序号" type="index" :index="i => $seqIndex(i, queryParams)" width="56" align="center" />
      <el-table-column label="缩略图" align="center" width="120">
        <template #default="scope">
          <div v-if="isVideo(scope.row)" class="cell-thumb" @click="openVideo(scope.row)">
            <el-image v-if="scope.row.thumbUrl" :src="thumbSrc(scope.row)" fit="cover" class="cell-img" @error="onThumbError(scope.row)" />
            <video v-else :src="resolveUrl(scope.row.fileUrl)" preload="metadata" muted playsinline class="cell-img"></video>
            <span class="play-btn small"><el-icon><VideoPlay /></el-icon></span>
          </div>
          <el-image
            v-else
            :src="thumbSrc(scope.row)"
            :preview-src-list="[resolveUrl(scope.row.fileUrl)]"
            fit="cover"
            class="cell-img"
            preview-teleported
            @error="onThumbError(scope.row)"
          >
            <template #error>
              <div class="photo-error small"><el-icon><Picture /></el-icon></div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="猫咪" align="center" prop="catId" width="200" :show-overflow-tooltip="true">
        <template #default="scope">{{ catLabel(scope.row.catId) }}</template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="品种" align="center" prop="breed" width="110" :show-overflow-tooltip="true">
        <template #default="scope">{{ scope.row.breed || '—' }}</template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="性别" align="center" prop="sex" width="80">
        <template #default="scope">{{ sexText(scope.row.sex) }}</template>
      </el-table-column>
      <el-table-column label="来源基地 / 房间" align="center" min-width="180" :show-overflow-tooltip="true">
        <template #default="scope">
          {{ [scope.row.baseName, scope.row.wardName].filter(Boolean).join(' / ') || '—' }}
        </template>
      </el-table-column>
      <el-table-column label="相册分类" align="center" prop="folder" width="110" />
      <el-table-column label="类型" align="center" prop="fileType" width="90">
        <template #default="scope">
          <el-tag :type="isVideo(scope.row) ? 'danger' : 'success'" size="small">
            {{ isVideo(scope.row) ? '视频' : '图片' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="缩略图" align="center" min-width="180" :show-overflow-tooltip="true">
        <template #default="scope">
          <span v-if="scope.row.thumbUrl && !scope.row.thumbFailed" class="text-success">已生成</span>
          <span v-else-if="scope.row.thumbUrl" class="text-muted">缩略图缺失，已回退原图</span>
          <span v-else class="text-muted">{{ isVideo(scope.row) ? '前端首帧兜底' : '无需生成/使用原图' }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="!isNarrow" label="文件地址" align="center" prop="fileUrl" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="150" fixed="right" class-name="small-padding fixed-width" v-if="checkPermi(['cat:photo:edit', 'cat:photo:remove'])">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['cat:photo:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['cat:photo:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 上传/编辑弹窗 -->
    <el-dialog :title="title" v-model="open" width="580px" append-to-body>
      <el-form ref="photoRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="猫咪" prop="catId">
          <cat-select v-model="form.catId" />
        </el-form-item>
        <el-form-item label="相册分类" prop="folder">
          <el-select v-model="form.folder" placeholder="请选择相册分类" filterable allow-create default-first-option style="width: 100%">
            <el-option v-for="d in cat_photo_folder" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型" prop="fileType">
          <el-radio-group v-model="form.fileType" @change="handleTypeChange">
            <el-radio label="1">图片</el-radio>
            <el-radio label="2">视频</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.fileType === '2' ? '视频' : '照片'" prop="fileUrl">
          <image-upload v-if="form.fileType !== '2'" v-model="form.fileUrl" :limit="1" :file-size="10" />
          <file-upload
            v-else
            v-model="form.fileUrl"
            :limit="1"
            :file-size="50"
            :file-type="videoTypes"
            @update:modelValue="handleVideoUploaded"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填，如拍摄部位、状态说明" />
        </el-form-item>
        <el-alert
          v-if="!form.photoId"
          type="success"
          :closable="false"
          show-icon
          title="保存后系统会自动为图片生成 400px 宽的缩略图、为视频抽取封面帧，列表加载更快。"
        />
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分组翻看：点开一只猫，左右滑动/方向键看它全部照片 -->
    <el-dialog v-model="viewer.open" :title="viewerTitle" width="min(880px, 96vw)" append-to-body destroy-on-close
      class="album-viewer" @opened="bindViewerKeys" @closed="unbindViewerKeys">
      <div v-loading="viewer.loading" class="viewer-stage" @touchstart.passive="onTouchStart" @touchend.passive="onTouchEnd">
        <el-button v-if="viewer.items.length > 1" class="viewer-nav prev" circle @click="stepViewer(-1)">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <template v-if="currentPhoto">
          <video v-if="isVideo(currentPhoto)" :src="resolveUrl(currentPhoto.fileUrl)" controls autoplay playsinline
            class="viewer-media"></video>
          <img v-else :src="resolveUrl(currentPhoto.fileUrl)" class="viewer-media zoomable" alt="" title="点击放大"
            @click="openZoom" />
        </template>
        <el-empty v-else-if="!viewer.loading" description="暂无照片" :image-size="60" />
        <el-button v-if="viewer.items.length > 1" class="viewer-nav next" circle @click="stepViewer(1)">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <div class="viewer-counter" v-if="viewer.items.length">{{ viewer.index + 1 }} / {{ viewer.items.length }}</div>
        <!-- 放大：右下角按钮，和直接点图片等效，进全屏可缩放/旋转/看原图 -->
        <el-button v-if="canZoom" class="viewer-zoom" circle title="放大查看（可缩放）" @click.stop="openZoom">
          <el-icon><ZoomIn /></el-icon>
        </el-button>
      </div>
      <div class="viewer-meta" v-if="currentPhoto">
        <el-tag size="small" type="info">{{ currentPhoto.folder || '未分类' }}</el-tag>
        <span class="viewer-remark">{{ currentPhoto.remark || '' }}</span>
        <span class="viewer-time">{{ (currentPhoto.createTime || '').slice(0, 16) }}</span>
      </div>
      <div class="viewer-strip" v-if="viewer.items.length > 1">
        <div v-for="(it, i) in viewer.items" :key="it.photoId" class="strip-item" :class="{ active: i === viewer.index }"
          @click="viewer.index = i">
          <img v-if="!isVideo(it)" :src="thumbSrc(it)" alt="" @error="onThumbError(it)" />
          <div v-else class="strip-video"><el-icon><VideoPlay /></el-icon></div>
        </div>
      </div>
      <template #footer>
        <div class="viewer-footer">
          <!-- 分组视图里照片不再是一行一条，列表行上的按钮也就没了；修改/删除必须在这里给出来，
               作用对象是当前这一张，改完/删完留在原处继续翻，不用退回相册再点进来 -->
          <div class="viewer-footer-ops">
            <el-button v-if="currentPhoto && !viewer.loading" icon="Edit" @click="handleUpdate(currentPhoto)"
              v-hasPermi="['cat:photo:edit']">修改本张</el-button>
            <el-button v-if="currentPhoto && !viewer.loading" type="danger" plain icon="Delete"
              @click="handleDeleteInViewer" v-hasPermi="['cat:photo:remove']">删除本张</el-button>
          </div>
          <div class="viewer-footer-actions">
            <el-button @click="goDetail(viewer.catId)">查看档案</el-button>
            <el-button type="primary" @click="viewer.open = false">关 闭</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 全屏查看器：滚轮/按钮缩放、旋转、全屏，看细节用（只在图片上出现，视频走原生播放器） -->
    <el-image-viewer v-if="zoomOpen" :url-list="zoomUrls" :initial-index="zoomIndex" :z-index="3000" teleported
      hide-on-click-modal :min-scale="0.2" :max-scale="8" @switch="onZoomSwitch" @close="zoomOpen = false" />

    <!-- 视频播放弹窗 -->
    <el-dialog v-model="videoOpen" :title="videoTitle" width="760px" append-to-body destroy-on-close>
      <video :src="videoUrl" controls autoplay class="video-player"></video>
    </el-dialog>
  </div>
</template>

<script setup name="CatPhoto">
import { listCatPhoto, listCatPhotoCards, getCatPhoto, delCatPhoto, addCatPhoto, updateCatPhoto, rebuildCatPhotoThumb, getCatPhotoThumbStatus, listCatPhotoBreeds } from '@/api/cat/photo'
import { listCatBase } from '@/api/cat/base'
import { listCatWard } from '@/api/cat/ward'
import { deleteUploadFile } from '@/api/common'
import { useCatCache, formatCatLabel } from '@/utils/catCache'
import { useNarrow } from '@/utils/useNarrow'

const { proxy } = getCurrentInstance()
const { isNarrow } = useNarrow()
const { cat_photo_folder, cat_sex } = proxy.useDict('cat_photo_folder', 'cat_sex')
const { catLabel } = useCatCache()
const router = useRouter()
const route = useRoute()

const list = ref([])
const breedOptions = ref([])
const baseOptions = ref([])
const wards = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const title = ref('')
const viewMode = ref('grid')
const rebuilding = ref(false)
const ffmpegAvailable = ref(null)

const videoOpen = ref(false)
const videoUrl = ref('')
const videoTitle = ref('')

const videoTypes = ['mp4', 'mov', 'm4v', 'avi', 'webm', 'mkv']

const baseUrl = import.meta.env.VITE_APP_BASE_API

// 上传组件返回的是 /profile/xx 相对路径，展示前需补全接口前缀
function resolveUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  return baseUrl + (url.startsWith('/') ? url : '/' + url)
}

// 缩略图地址：缩略图加载失败（文件被清理/重建失败）时自动回退到原图，避免整块空白
function thumbSrc(item) {
  const url = !item.thumbFailed && item.thumbUrl ? item.thumbUrl : item.fileUrl
  return resolveUrl(url)
}

function onThumbError(item) {
  if (item.thumbUrl && !item.thumbFailed) {
    item.thumbFailed = true
  }
}

// 相册预览只包含图片，视频单独播放；列表用缩略图，点开大图用原图保证清晰度
const imagePreviewList = computed(() =>
  list.value.filter(i => !isVideo(i)).map(i => resolveUrl(i.fileUrl)).filter(Boolean)
)

// 大图预览的起始位置（按原图列表计算）
function previewIndex(item) {
  const index = imagePreviewList.value.indexOf(resolveUrl(item.fileUrl))
  return index < 0 ? 0 : index
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 24,
    catId: undefined,
    folder: undefined,
    fileType: undefined,
    // 相册筛选：品种 / 来源基地 / 所属房间 / 性别
    breed: undefined,
    sex: undefined,
    baseId: undefined,
    wardId: undefined
  },
  rules: {
    catId: [{ required: true, message: '猫咪ID不能为空', trigger: 'blur' }],
    fileUrl: [{ required: true, message: '请上传照片或视频', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 所属房间下拉：选了基地就只列该基地的房间
const wardFilterOptions = computed(() => {
  const baseId = queryParams.value.baseId
  if (!baseId) return wards.value
  return wards.value.filter(w => w.baseId === baseId)
})

function onFilterBaseChange() {
  const baseId = queryParams.value.baseId
  if (baseId && queryParams.value.wardId) {
    const ward = wards.value.find(w => w.wardId === queryParams.value.wardId)
    if (ward && ward.baseId !== baseId) {
      queryParams.value.wardId = undefined
    }
  }
}

function sexText(sex) {
  const hit = (cat_sex.value || []).find(d => String(d.value) === String(sex))
  return hit ? hit.label : '未知'
}

function goDetail(catId) {
  if (!catId) return
  router.push('/profile/detail/' + catId)
}

function isVideo(item) {
  return item && item.fileType === '2'
}

function getList() {
  loading.value = true
  // 相册视图按猫分组（一只猫一张卡），信息卡/列表视图按照片逐条
  const request = viewMode.value === 'grid'
    ? listCatPhotoCards(queryParams.value)
    : listCatPhoto(queryParams.value)
  request.then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 相册筛选下拉的数据源：品种取档案里真实存在的值，基地/房间取主数据
function loadFilterOptions() {
  listCatPhotoBreeds().then(res => { breedOptions.value = res.data || [] }).catch(() => {})
  listCatBase({ pageNum: 1, pageSize: 500 }).then(res => { baseOptions.value = res.rows || [] }).catch(() => {})
  listCatWard({ pageNum: 1, pageSize: 500 }).then(res => { wards.value = res.rows || [] }).catch(() => {})
}

// 切换视图重新拉数据；信息卡视图 = 只看相册分类为「信息卡」的照片，其余筛选照常叠加
watch(viewMode, (mode, oldMode) => {
  queryParams.value.pageNum = 1
  if (mode === 'cats') {
    queryParams.value.folder = '信息卡'
  } else if (oldMode === 'cats' && queryParams.value.folder === '信息卡') {
    queryParams.value.folder = undefined
  }
  getList()
})

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    photoId: undefined,
    catId: undefined,
    folder: '救助时',
    fileType: '1',
    fileUrl: undefined,
    remark: undefined
  }
  selectedByType['1'] = undefined
  selectedByType['2'] = undefined
  lastFileType.value = '1'
  proxy.clearFormValidate('photoRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  // 信息卡视图的「相册 = 信息卡」是视图自带的，重置后要补回来
  if (viewMode.value === 'cats') {
    queryParams.value.folder = '信息卡'
  }
  handleQuery()
}

/**
 * 档案页「管理相册」跳过来会带 ?catId=100 → 只显示这只猫的相册。
 * 路由里的值转成数字（下拉 option 的 value 是数字，字符串会导致选中项显示空白）
 */
function toCatId(raw) {
  if (raw === undefined || raw === null || raw === '') return undefined
  const id = Number(raw)
  return Number.isNaN(id) ? undefined : id
}

/** 应用路由上的 catId 筛选；其余筛选清空，避免叠加上一份条件后出现空列表 */
function applyRouteCat(raw) {
  const catId = toCatId(raw)
  queryParams.value.catId = catId
  if (catId !== undefined) {
    queryParams.value.breed = undefined
    queryParams.value.sex = undefined
    queryParams.value.baseId = undefined
    queryParams.value.wardId = undefined
    queryParams.value.folder = undefined
    queryParams.value.fileType = undefined
  }
  // 信息卡视图只看「信息卡」分类，清空别的条件时别把它一起清掉
  if (viewMode.value === 'cats') {
    queryParams.value.folder = '信息卡'
  }
  queryParams.value.pageNum = 1
}

/** 取消「只看某只猫」：把 catId 从地址栏抹掉，刷新后也不再被套住 */
function clearCatScope() {
  queryParams.value.catId = undefined
  queryParams.value.pageNum = 1
  if (route.query.catId !== undefined) {
    const query = { ...route.query }
    delete query.catId
    router.replace({ path: route.path, query })
  }
  getList()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '上传照片/视频'
}

function handleUpdate(row) {
  reset()
  getCatPhoto(row.photoId).then(response => {
    form.value = response.data
    if (!form.value.fileType) {
      form.value.fileType = '1'
    }
    // 记录自带的文件放进对应类型，切走再切回来能还原
    selectedByType['1'] = undefined
    selectedByType['2'] = undefined
    selectedByType[form.value.fileType] = form.value.fileUrl
    lastFileType.value = form.value.fileType
    open.value = true
    title.value = '修改照片'
  })
}

// 图片 / 视频各自记住已选文件：来回切换类型时不再把已上传的文件清空
const selectedByType = { '1': undefined, '2': undefined }
const lastFileType = ref('1')

function handleTypeChange(val) {
  // 先把「刚离开的那个类型」当前选中的文件存起来
  selectedByType[lastFileType.value] = form.value.fileUrl
  lastFileType.value = val
  // 切回来就恢复该类型此前选中的文件；没选过才是空的
  form.value.fileUrl = selectedByType[val]
  // 缩略图跟文件地址走：地址没变时后端不会重复生成，这里不保留旧类型的缩略图
  form.value.thumbUrl = undefined
}

// 视频上传完成后按扩展名补一次类型，防止用户漏选
function handleVideoUploaded(val) {
  if (val) {
    form.value.fileType = '2'
  }
}

function submitForm() {
  proxy.$refs['photoRef'].validate(valid => {
    if (valid) {
      const photoId = form.value.photoId
      const request = photoId != undefined ? updateCatPhoto(form.value) : addCatPhoto(form.value)
      request.then(() => {
        proxy.$modal.msgSuccess(photoId != undefined ? '修改成功' : '新增成功')
        open.value = false
        getList()
        // 从分组弹窗里点「修改本张」进来的，改完要把弹窗里的那张也刷新（分类/备注可能变了）
        if (viewer.open && photoId != undefined) {
          loadAlbumItems(photoId)
        }
      })
    }
  })
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.photoId)
  multiple.value = !selection.length
}

// 要删的记录对应哪些磁盘文件：删除是两步，先删库记录，再把原图/缩略图清掉
function collectFiles(rows, idList) {
  const files = []
  rows.forEach(item => {
    if (idList.includes(item.photoId)) {
      if (item.fileUrl) files.push(item.fileUrl)
      if (item.thumbUrl) files.push(item.thumbUrl)
    }
  })
  return files
}

function handleDelete(row) {
  const delIds = row.photoId || ids.value
  const idList = Array.isArray(delIds) ? delIds : [delIds]
  // 先记下要删的记录对应的文件，删完数据库后再把磁盘文件一并清掉
  const files = collectFiles(list.value, idList)
  proxy.$modal.confirm('删除后服务器上的照片文件也会一并清除，且无法恢复，是否继续？').then(function () {
    return delCatPhoto(delIds)
  }).then(() => {
    files.forEach(f => deleteUploadFile(f).catch(() => {}))
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/**
 * 分组弹窗里删「当前这张」。
 * 删完不关弹窗，游标停在原位置（删的是最后一张就往前收一张），可以接着翻。
 */
function handleDeleteInViewer() {
  const cur = currentPhoto.value
  if (!cur) return
  const id = cur.photoId
  const files = collectFiles(viewer.items, [id])
  proxy.$modal.confirm('删除后服务器上的照片文件也会一并清除，且无法恢复，是否继续？').then(function () {
    return delCatPhoto([id])
  }).then(() => {
    files.forEach(f => deleteUploadFile(f).catch(() => {}))
    const idx = viewer.items.findIndex(it => it.photoId === id)
    if (idx >= 0) viewer.items.splice(idx, 1)
    if (viewer.index > viewer.items.length - 1) viewer.index = Math.max(0, viewer.items.length - 1)
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

// ---------- 按猫分组：一张封面卡 + 点开左右翻看 ----------
const viewer = reactive({
  open: false,
  loading: false,
  catId: undefined,
  catTitle: '',
  items: [],
  index: 0
})

const currentPhoto = computed(() => viewer.items[viewer.index])

// 标题里的张数跟着实际列表走：删掉一张、改完后照片被筛掉，标题立刻跟着变
const viewerTitle = computed(() => {
  if (!viewer.catTitle) return ''
  return viewer.loading ? viewer.catTitle : `${viewer.catTitle} · ${viewer.items.length} 张`
})

function albumAttrs(c) {
  const loc = [c.baseName, c.wardName].filter(Boolean).join(' / ')
  return [c.breed, sexText(c.sex), loc].filter(Boolean).join(' · ') || '—'
}

// 打开某只猫的分组：按当前筛选条件拉它全部照片（不受列表分页影响）
function openAlbum(c) {
  viewer.catId = c.catId
  viewer.catTitle = formatCatLabel(c.catName, c.archiveNo)
  viewer.items = []
  viewer.index = 0
  viewer.loading = true
  viewer.open = true
  loadAlbumItems()
}

// 按当前筛选条件重新拉这一组的照片；keepPhotoId 用来改完/删完后把游标停在原来那张（或它的下一张）
function loadAlbumItems(keepPhotoId) {
  if (!viewer.catId) return Promise.resolve()
  const query = { ...queryParams.value, catId: viewer.catId, pageNum: 1, pageSize: 500 }
  return listCatPhoto(query).then(res => {
    viewer.items = res.rows || []
    viewer.loading = false
    if (keepPhotoId) {
      const i = viewer.items.findIndex(it => it.photoId === keepPhotoId)
      if (i >= 0) viewer.index = i
    }
    // 删掉最后一张、或改完分类后照片不再属于当前筛选时，游标会越界，收回到有效的最后一张
    if (viewer.index > viewer.items.length - 1) {
      viewer.index = Math.max(0, viewer.items.length - 1)
    }
  }).catch(() => {
    viewer.loading = false
  })
}

// 左右翻页：到头绕回另一头，避免在多张图里卡死
function stepViewer(delta) {
  const total = viewer.items.length
  if (total < 2) return
  viewer.index = (viewer.index + delta + total) % total
}

// ---------- 放大查看 ----------
// 全屏查看器只收图片，索引必须按「过滤掉视频后」的数组算，否则会错位到别的图
const zoomOpen = ref(false)
const zoomItems = computed(() => viewer.items.filter(it => !isVideo(it)))
const zoomUrls = computed(() => zoomItems.value.map(it => resolveUrl(it.fileUrl)))
const canZoom = computed(() => !!currentPhoto.value && !isVideo(currentPhoto.value))
const zoomIndex = computed(() => {
  const cur = currentPhoto.value
  if (!cur) return 0
  const i = zoomItems.value.findIndex(it => it.photoId === cur.photoId)
  return i < 0 ? 0 : i
})

function openZoom() {
  if (!canZoom.value) return
  zoomOpen.value = true
}

// 全屏里翻过页再关掉时，把弹窗同步到同一张，避免「放大看的是 A、关掉回到 B」
function onZoomSwitch(i) {
  const target = zoomItems.value[i]
  if (!target) return
  const idx = viewer.items.findIndex(it => it.photoId === target.photoId)
  if (idx >= 0) viewer.index = idx
}

// 手机端左右滑动翻页
let touchStartX = 0
let touchStartY = 0

function onTouchStart(e) {
  const t = e.touches && e.touches[0]
  if (!t) return
  touchStartX = t.clientX
  touchStartY = t.clientY
}

function onTouchEnd(e) {
  const t = e.changedTouches && e.changedTouches[0]
  if (!t) return
  const dx = t.clientX - touchStartX
  const dy = t.clientY - touchStartY
  // 横向位移要明显大于纵向，才当成翻页，否则是误触/滚动
  if (Math.abs(dx) < 40 || Math.abs(dx) <= Math.abs(dy)) return
  stepViewer(dx < 0 ? 1 : -1)
}

// 键盘方向键翻页（弹窗打开时才生效）
function onViewerKey(e) {
  // 全屏查看器开着时方向键归它管，别再在背后偷偷翻弹窗里的图
  if (zoomOpen.value) return
  if (e.key === 'ArrowLeft') stepViewer(-1)
  else if (e.key === 'ArrowRight') stepViewer(1)
}

function bindViewerKeys() {
  window.addEventListener('keydown', onViewerKey)
}

function unbindViewerKeys() {
  window.removeEventListener('keydown', onViewerKey)
  // 弹窗关掉时顺手收掉全屏查看器，否则它可能残留在页面上（destroy-on-close 只管弹窗自己）
  zoomOpen.value = false
  viewer.items = []
}

onBeforeUnmount(() => window.removeEventListener('keydown', onViewerKey))

function openVideo(item) {
  videoUrl.value = resolveUrl(item.fileUrl)
  videoTitle.value = `${catLabel(item.catId)} · ${item.folder || '未分类'}`
  videoOpen.value = true
}

function handleRebuildThumb() {
  proxy.$modal.confirm('将对当前筛选条件下的照片/视频重新生成缩略图，是否继续？').then(() => {
    rebuilding.value = true
    return rebuildCatPhotoThumb({ catId: queryParams.value.catId, fileType: queryParams.value.fileType })
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '重建完成')
    getList()
  }).catch(() => {}).finally(() => {
    rebuilding.value = false
  })
}

function loadThumbStatus() {
  getCatPhotoThumbStatus().then(res => {
    ffmpegAvailable.value = res.ffmpegAvailable
  }).catch(() => {
    ffmpegAvailable.value = null
  })
}

// 首屏：地址栏带 ?catId=xx（档案页「管理相册」入口）就只看这只猫
applyRouteCat(route.query.catId)
getList()
loadThumbStatus()
loadFilterOptions()

// 页面被 keep-alive 缓存，从另一只猫的档案再点「管理相册」组件不会重建，只能监听路由
watch(() => route.query.catId, raw => {
  const next = toCatId(raw)
  // 值没变（比如 clearCatScope 自己改了地址栏）就不用重复请求
  if (next === queryParams.value.catId) return
  applyRouteCat(raw)
  getList()
})
</script>

<style scoped>
/* 单只猫的筛选提示条 */
.cat-scope {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  padding: 6px 12px;
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 6px;
  background: var(--el-color-primary-light-9);
  font-size: 13px;
}
.cat-scope-label {
  color: var(--el-text-color-secondary);
}
/* 按猫分组：一只猫一张封面卡 */
.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  min-height: 200px;
}
.album-grid > .el-empty {
  grid-column: 1 / -1;
}
.album-item {
  min-width: 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  overflow: hidden;
  background: var(--el-bg-color-overlay);
  cursor: pointer;
  transition: border-color .2s, transform .2s;
}
.album-item:hover {
  border-color: var(--el-color-primary-light-5);
  transform: translateY(-1px);
}
.album-cover {
  position: relative;
}
.album-img {
  width: 100%;
  aspect-ratio: 4 / 3;
  display: block;
  object-fit: cover;
}
/* 封面右上角的「共 N 张」，一眼知道这一组有多少内容 */
.album-count {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 1px 8px;
  border-radius: 10px;
  background: rgba(0, 0, 0, .55);
  color: #fff;
  font-size: 12px;
  line-height: 18px;
}
.album-title {
  padding: 8px 10px 0;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.album-attrs {
  padding: 4px 10px 0;
  font-size: 12px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.album-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2px 10px 8px;
}
.album-time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
/* 分组翻看弹窗 */
.viewer-stage {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  max-height: 62vh;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  touch-action: pan-y;
}
.viewer-media {
  max-width: 100%;
  max-height: 62vh;
  display: block;
  object-fit: contain;
}
.viewer-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  opacity: .85;
}
.viewer-nav.prev { left: 10px; }
.viewer-nav.next { right: 10px; }
/* 放大按钮停在图片右下角，跟「1 / 7」计数一左一右；半透明黑底保证压在亮图上也能看清 */
.viewer-zoom {
  position: absolute;
  right: 10px;
  bottom: 10px;
  z-index: 2;
  background: rgba(0, 0, 0, .55);
  border-color: rgba(255, 255, 255, .35);
  color: #fff;
}
.viewer-zoom:hover,
.viewer-zoom:focus {
  background: rgba(0, 0, 0, .75);
  border-color: #fff;
  color: #fff;
}
/* 图片本身也是可点放大的，给个放大镜光标提示 */
.zoomable {
  cursor: zoom-in;
}

.viewer-counter {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  padding: 2px 10px;
  border-radius: 10px;
  background: rgba(0, 0, 0, .55);
  color: #fff;
  font-size: 12px;
}
.viewer-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 2px 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.viewer-remark {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.viewer-time {
  color: var(--el-text-color-placeholder);
}
/* 弹窗底部：左边改/删当前这张，右边关闭，中间留白避免手滑删掉正在看的图 */
.viewer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}
.viewer-footer-ops,
.viewer-footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.viewer-footer .el-button + .el-button {
  margin-left: 0;
}
.viewer-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 2px 2px;
}
.strip-item {
  flex: 0 0 auto;
  width: 72px;
  height: 54px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  background: var(--el-fill-color-light);
}
.strip-item.active {
  border-color: var(--el-color-primary);
}
.strip-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.strip-video {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  background: #000;
}

/* 自适应网格：卡片按容器宽度均分，避免 flex 固定 200px 导致右侧留空列 */
.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
  min-height: 200px;
}
/* 空状态占满整行，不被挤在一个格子里 */
.photo-grid > .el-empty {
  grid-column: 1 / -1;
}
.photo-item {
  width: auto;
  min-width: 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-bg-color-overlay);
}
.photo-thumb {
  position: relative;
  cursor: pointer;
}
.photo-img {
  width: 100%;
  height: auto;
  aspect-ratio: 4 / 3;
  display: block;
  object-fit: cover;
  cursor: zoom-in;
}
.video-fallback {
  background: #000;
  cursor: pointer;
  object-fit: cover;
}
.play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  pointer-events: none;
}
.play-btn.small {
  width: 26px;
  height: 26px;
  font-size: 14px;
}
.type-badge {
  position: absolute;
  top: 6px;
  right: 6px;
}
.cell-thumb {
  position: relative;
  width: 64px;
  height: 64px;
  margin: 0 auto;
  cursor: pointer;
}
.cell-img {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  display: block;
}
.photo-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 32px;
  color: var(--el-text-color-placeholder);
  background: var(--el-fill-color-light);
}
.photo-error.small {
  height: 64px;
  width: 64px;
  font-size: 20px;
}
.photo-meta {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 10px;
  padding: 8px 10px 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
/* 状态标签固定宽度不参与压缩，保证与右边档案信息之间始终留有空隙 */
.photo-meta .el-tag {
  flex: 0 0 auto;
}
/* 档案号 + 名字：单行完整显示，不省略；卡片列宽已按最长标题（19 字符）留足空间 */
.photo-cat {
  flex: 1 1 auto;
  min-width: 0;
  white-space: nowrap;
  text-align: right;
  font-size: 12px;
  line-height: 1.4;
}
.photo-ops {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 4px 10px 8px;
}
/* 卡片下方的档案属性：品种 · 性别 · 来源基地 · 所属房间 */
.photo-attrs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  padding: 4px 10px 0;
  font-size: 12px;
  color: var(--el-text-color-regular);
}
.photo-attrs .dot {
  color: var(--el-text-color-placeholder);
}
.photo-ops .el-button + .el-button {
  margin-left: 0;
}
.video-player {
  width: 100%;
  max-height: 70vh;
  background: #000;
  border-radius: 4px;
}
.text-muted {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}
.text-success {
  color: var(--el-color-success);
  font-size: 12px;
}
</style>
