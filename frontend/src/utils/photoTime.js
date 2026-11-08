/**
 * 上传图片的时间标注
 *
 * 后端 FileUploadUtils 保存文件时按「原文件名_yyyyMMddHHmmss + 机器码 + 3位序号.后缀」命名，
 * 例如 /profile/upload/2026/11/07/用药照片_20261107103012A001.jpg —— 地址里就带着上传那一刻的时间。
 * 所以同一组用药的多张照片即使共用一个 photo_time，也能各自标出真正的上传时间；
 * 早期没有时间戳的图（演示数据、外部链接）再退回后端记录的组级上传时间。
 */

/** 从图片地址里解析上传时间，解析不到返回空串 */
export function uploadTimeOf(url) {
  const name = String(url || '').split('?')[0].split('/').pop() || ''
  // 时间戳一定在扩展名前、末尾那一段（前面可能还有原文件名里的数字），所以只认结尾
  const m = name.match(/(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})[A-Za-z]?\d{0,3}\.[^.]+$/)
  if (!m) return ''
  return `${m[1]}-${m[2]}-${m[3]} ${m[4]}:${m[5]}`
}

/**
 * 图片上传时间文案，统一成「MM-DD HH:mm」这种短格式，够用又不占地方
 * @param {string} url      图片地址
 * @param {string} fallback 后端记录的组级上传时间（yyyy-MM-dd HH:mm:ss）
 */
export function uploadTimeLabel(url, fallback) {
  const text = uploadTimeOf(url) || String(fallback || '')
  if (!text) return ''
  // 2026-11-07 10:30:12 → 11-07 10:30
  const m = text.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})/)
  return m ? `${m[2]}-${m[3]} ${m[4]}:${m[5]}` : text
}
