/**
 * 文件地址与预览类型工具
 *
 * 背景：数据库里存的是相对路径（/profile/upload/2026/10/29/xxx.docx），
 * 页面里散落着各自拼 base 的写法，容易漏带环境前缀或忘编码中文名。
 * 统一走这里，行为一致：开发环境拼 /dev-api，生产环境拼 /prod-api。
 */

/** 是否外部地址 / 内联资源，直接返回不拼前缀 */
function isAbsolute(url) {
  return /^(https?:)?\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')
}

/**
 * 相对路径 → 可直接访问的 URL
 * 中文文件名会被编码（浏览器 window.open 不总是替我们做这件事）；
 * 已经编码过的路径不再二次编码，避免 %E9 变成 %25E9。
 */
export function resolveFileUrl(url) {
  if (!url) return ''
  if (isAbsolute(url)) return url
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const path = url.startsWith('/') ? url : '/' + url
  const encoded = /%[0-9A-Fa-f]{2}/.test(path) ? path : encodeURI(path)
  return base + encoded
}

/** 取扩展名（小写，不含点） */
export function fileExt(url) {
  if (!url) return ''
  const clean = url.split('?')[0].split('#')[0]
  const name = clean.substring(clean.lastIndexOf('/') + 1)
  const dot = name.lastIndexOf('.')
  return dot > -1 ? name.substring(dot + 1).toLowerCase() : ''
}

/** 取文件名（已解码，用于「另存为」的默认名） */
export function fileName(url) {
  if (!url) return ''
  const name = url.split('?')[0].split('#')[0].split('/').pop() || ''
  try {
    return decodeURIComponent(name)
  } catch (e) {
    return name
  }
}

/**
 * 浏览器能直接显示的预览类型
 * image  → 图片，可用 el-image / el-image-viewer
 * pdf    → PDF，浏览器内置阅读器（新窗口或 iframe）
 * video  → 视频，<video> 播放
 * office → Word / Excel / PPT，浏览器不能内嵌，只能下载
 * other  → 其它，交给系统默认程序或下载
 */
export function previewKind(url) {
  const ext = fileExt(url)
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico'].includes(ext)) return 'image'
  if (ext === 'pdf') return 'pdf'
  if (['mp4', 'webm', 'ogg', 'mov', 'm4v'].includes(ext)) return 'video'
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'wps', 'et', 'dps'].includes(ext)) return 'office'
  return 'other'
}

/** 扩展名对应的中文说明，用于提示文案 */
export function fileExtLabel(url) {
  const ext = fileExt(url)
  const map = {
    doc: 'Word', docx: 'Word', wps: 'WPS 文字',
    xls: 'Excel', xlsx: 'Excel', et: 'WPS 表格',
    ppt: 'PPT', pptx: 'PPT', dps: 'WPS 演示',
    pdf: 'PDF'
  }
  return map[ext] || (ext ? ext.toUpperCase() + ' 文件' : '该文件')
}
