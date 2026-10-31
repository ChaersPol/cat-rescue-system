<template>
  <div class="media-upload" :class="{ 'is-disabled': disabled }">
    <el-upload
      multiple
      :disabled="disabled"
      :action="uploadUrl"
      list-type="picture-card"
      :headers="headers"
      :file-list="fileList"
      :limit="limit"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-exceed="handleExceed"
      :before-upload="beforeUpload"
      :on-preview="handlePreview"
      :class="{ hide: fileList.length >= limit }"
    >
      <el-icon class="media-add"><Plus /></el-icon>
      <template #file="{ file }">
        <div class="media-item">
          <video
            v-if="isVideoUrl(file.url)"
            :src="file.url"
            class="media-thumb"
            preload="metadata"
            muted
            playsinline
          ></video>
          <img v-else :src="file.url" class="media-thumb" />
          <span v-if="isVideoUrl(file.url)" class="media-badge">
            <el-icon><VideoPlay /></el-icon>
          </span>
          <span class="media-actions" v-if="!disabled">
            <span class="media-action" title="预览" @click.stop="handlePreview(file)">
              <el-icon><ZoomIn /></el-icon>
            </span>
            <span class="media-action danger" title="删除" @click.stop="handleRemove(file)">
              <el-icon><Delete /></el-icon>
            </span>
          </span>
        </div>
      </template>
    </el-upload>
    <div class="el-upload__tip" v-if="!disabled">
      支持照片
      <b>png/jpg/jpeg/webp</b>（单个不超过
      <b>{{ imageSize }}MB</b>）与视频
      <b>mp4/mov/m4v/webm</b>（单个不超过
      <b>{{ videoSize }}MB</b>），最多 {{ limit }} 项
    </div>

    <!-- 图片大图预览 -->
    <el-dialog v-model="imageVisible" title="图片预览" width="800px" append-to-body destroy-on-close>
      <img :src="imageUrl" style="display: block; max-width: 100%; margin: 0 auto" />
    </el-dialog>

    <!-- 视频播放 -->
    <el-dialog v-model="videoVisible" title="视频预览" width="720px" append-to-body destroy-on-close>
      <video :src="videoUrl" controls autoplay style="width: 100%; max-height: 70vh; background: #000"></video>
    </el-dialog>
  </div>
</template>

<script setup name="MediaUpload">
import { getToken } from "@/utils/auth"
import { deleteUploadFile } from "@/api/common"
import { previewKind, fileName as pickFileName } from "@/utils/fileUrl"

const props = defineProps({
  modelValue: [String, Object, Array],
  // 上传接口地址
  action: {
    type: String,
    default: "/common/upload"
  },
  // 最多几项（照片 + 视频合计）
  limit: {
    type: Number,
    default: 9
  },
  // 单张照片大小限制(MB)
  imageSize: {
    type: Number,
    default: 10
  },
  // 单个视频大小限制(MB)
  videoSize: {
    type: Number,
    default: 50
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const { proxy } = getCurrentInstance()
const emit = defineEmits(["update:modelValue"])

const IMAGE_EXT = ["png", "jpg", "jpeg", "webp", "gif", "bmp"]
// 与后端 MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION 里放开的那批保持一致
const VIDEO_EXT = ["mp4", "mov", "m4v", "webm", "avi", "mkv", "rmvb", "flv"]

const baseUrl = import.meta.env.VITE_APP_BASE_API
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + props.action)
const headers = ref({ Authorization: "Bearer " + getToken() })
const fileList = ref([])
const imageVisible = ref(false)
const imageUrl = ref("")
const videoVisible = ref(false)
const videoUrl = ref("")

function extOf(url) {
  if (!url) return ""
  const clean = String(url).split("?")[0].split("#")[0]
  const name = clean.substring(clean.lastIndexOf("/") + 1)
  const dot = name.lastIndexOf(".")
  return dot > -1 ? name.substring(dot + 1).toLowerCase() : ""
}

function isVideoUrl(url) {
  return previewKind(url) === "video" || VIDEO_EXT.includes(extOf(url))
}

/** 数据库存的是相对路径；显示要拼上环境前缀，外链与 blob 直接用 */
function toDisplayUrl(raw) {
  if (!raw) return ""
  if (/^(https?:)?\/\//i.test(raw) || raw.startsWith("data:") || raw.startsWith("blob:")) return raw
  return baseUrl + (raw.startsWith("/") ? raw : "/" + raw)
}

function buildFile(raw, index) {
  const url = toDisplayUrl(raw)
  return {
    name: pickFileName(raw) || `资料${index + 1}`,
    raw,
    url,
    uid: Date.now() + index
  }
}

watch(
  () => props.modelValue,
  val => {
    if (!val) {
      fileList.value = []
      return
    }
    const list = (Array.isArray(val) ? val : String(val).split(","))
      .map(item => (typeof item === "string" ? item : item && item.url))
      .filter(Boolean)
    fileList.value = list.map((raw, i) => buildFile(raw, i))
  },
  { deep: true, immediate: true }
)

function handleBeforeTip(max, kind) {
  proxy.$modal.msgError(`${kind}大小不能超过 ${max}MB!`)
}

function beforeUpload(file) {
  const ext = extOf(file.name)
  const isVideo = VIDEO_EXT.includes(ext)
  if (!isVideo && !IMAGE_EXT.includes(ext)) {
    proxy.$modal.msgError(`只支持上传照片（${IMAGE_EXT.join("/")}）或视频（${VIDEO_EXT.join("/")}）!`)
    return false
  }
  if (file.name.includes(",")) {
    proxy.$modal.msgError("文件名不能包含英文逗号!")
    return false
  }
  const max = isVideo ? props.videoSize : props.imageSize
  if (file.size > max * 1024 * 1024) {
    handleBeforeTip(max, isVideo ? "视频" : "照片")
    return false
  }
  proxy.$modal.loading(isVideo ? "正在上传视频，请稍候..." : "正在上传照片，请稍候...")
  return true
}

function handleExceed() {
  proxy.$modal.msgError(`最多只能上传 ${props.limit} 项资料!`)
}

function handleSuccess(res) {
  proxy.$modal.closeLoading()
  if (res && res.code === 200) {
    // 必须整体替换数组，el-upload 才会同步内部列表（push 不触发）
    fileList.value = [...fileList.value, buildFile(res.fileName, fileList.value.length)]
    emitUpdate()
  } else {
    proxy.$modal.msgError((res && res.msg) || "上传失败")
  }
}

function handleError() {
  proxy.$modal.closeLoading()
  proxy.$modal.msgError("上传失败，请检查网络或文件大小")
}

function handlePreview(file) {
  if (isVideoUrl(file.url)) {
    videoUrl.value = file.url
    videoVisible.value = true
  } else {
    imageUrl.value = file.url
    imageVisible.value = true
  }
}

function handleRemove(file) {
  const target = file.url
  const item = fileList.value.find(f => f.url === target)
  proxy.$modal
    .confirm("删除后服务器上的文件也会被清除且无法恢复，确定删除吗？")
    .then(() => {
      fileList.value = fileList.value.filter(f => f.url !== target)
      emitUpdate()
      if (item && item.raw) deleteUploadFile(item.raw).catch(() => {})
    })
    .catch(() => {})
}

function emitUpdate() {
  const paths = fileList.value.map(f => f.raw).filter(Boolean)
  emit("update:modelValue", paths.join(","))
}
</script>

<style scoped lang="scss">
.media-upload {
  width: 100%;
  line-height: 1.4;
}

.media-add {
  font-size: 24px;
  color: var(--el-text-color-placeholder);
}

.media-item {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 6px;
  overflow: hidden;
  background: var(--el-fill-color-darker);
}

.media-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.media-badge {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  background: rgba(0, 0, 0, 0.35);
  pointer-events: none;
}

.media-actions {
  position: absolute;
  top: 2px;
  right: 2px;
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}

.media-item:hover .media-actions {
  opacity: 1;
}

.media-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  font-size: 13px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  cursor: pointer;

  &.danger:hover {
    background: var(--el-color-danger);
  }

  &:hover {
    background: var(--el-color-primary);
  }
}

// 卡片尺寸收小一点，弹窗里一行能多放两个
:deep(.el-upload--picture-card),
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 96px;
  height: 96px;
}

// 达到上限后隐藏「+」
:deep(.hide .el-upload--picture-card) {
  display: none;
}

.is-disabled :deep(.el-upload--picture-card) {
  display: none;
}
</style>
