<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="猫咪" prop="catId">
        <cat-select v-model="queryParams.catId" placeholder="全部猫咪" style="width: 220px" />
      </el-form-item>
      <el-form-item label="相册" prop="folder">
        <el-select v-model="queryParams.folder" placeholder="全部相册" clearable style="width: 160px">
          <el-option v-for="d in cat_photo_folder" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="全部" clearable style="width: 120px">
          <el-option label="图片" value="1" />
          <el-option label="视频" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['cat:photo:add']">上传照片/视频</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="RefreshRight" :loading="rebuilding" @click="handleRebuildThumb" v-hasPermi="['cat:photo:edit']">重建缩略图</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="grid">相册视图</el-radio-button>
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

    <!-- 相册视图 -->
    <div v-if="viewMode === 'grid'" v-loading="loading" class="photo-grid">
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
        <div class="photo-ops">
          <el-button link type="primary" @click="handleUpdate(item)" v-hasPermi="['cat:photo:edit']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(item)" v-hasPermi="['cat:photo:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <el-table v-else v-loading="loading" :data="list">
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
      <el-table-column label="猫咪ID" align="center" prop="catId" width="90" />
      <el-table-column label="相册分类" align="center" prop="folder" width="110" />
      <el-table-column label="类型" align="center" prop="fileType" width="90">
        <template #default="scope">
          <el-tag :type="isVideo(scope.row) ? 'danger' : 'success'" size="small">
            {{ isVideo(scope.row) ? '视频' : '图片' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="缩略图" align="center" min-width="180" :show-overflow-tooltip="true">
        <template #default="scope">
          <span v-if="scope.row.thumbUrl && !scope.row.thumbFailed" class="text-success">已生成</span>
          <span v-else-if="scope.row.thumbUrl" class="text-muted">缩略图缺失，已回退原图</span>
          <span v-else class="text-muted">{{ isVideo(scope.row) ? '前端首帧兜底' : '无需生成/使用原图' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="文件地址" align="center" prop="fileUrl" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
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

    <!-- 视频播放弹窗 -->
    <el-dialog v-model="videoOpen" :title="videoTitle" width="760px" append-to-body destroy-on-close>
      <video :src="videoUrl" controls autoplay class="video-player"></video>
    </el-dialog>
  </div>
</template>

<script setup name="CatPhoto">
import { listCatPhoto, getCatPhoto, delCatPhoto, addCatPhoto, updateCatPhoto, rebuildCatPhotoThumb, getCatPhotoThumbStatus } from '@/api/cat/photo'
import { useCatCache } from '@/utils/catCache'

const { proxy } = getCurrentInstance()
const { cat_photo_folder } = proxy.useDict('cat_photo_folder')
const { catLabel } = useCatCache()

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
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
    fileType: undefined
  },
  rules: {
    catId: [{ required: true, message: '猫咪ID不能为空', trigger: 'blur' }],
    fileUrl: [{ required: true, message: '请上传照片或视频', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function isVideo(item) {
  return item && item.fileType === '2'
}

function getList() {
  loading.value = true
  listCatPhoto(queryParams.value).then(response => {
    list.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

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
  proxy.resetForm('photoRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
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
      const request = form.value.photoId != undefined ? updateCatPhoto(form.value) : addCatPhoto(form.value)
      request.then(() => {
        proxy.$modal.msgSuccess(form.value.photoId != undefined ? '修改成功' : '新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该照片？缩略图会随原文件一并保留在磁盘上。').then(function () {
    return delCatPhoto(row.photoId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

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

getList()
loadThumbStatus()
</script>

<style scoped>
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
