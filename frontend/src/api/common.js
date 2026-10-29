import request from '@/utils/request'

/**
 * 删除服务器上的上传文件
 *
 * 上传组件里点「删除」时调用：只把地址从表单里去掉是不够的，
 * 磁盘上的文件会一直留着，时间一长服务器就被无效文件塞满了。
 *
 * @param {string} fileName 文件访问地址，如 /profile/upload/2026/10/29/xxx.docx
 */
export function deleteUploadFile(fileName) {
  return request({
    url: '/common/delete',
    method: 'post',
    params: { fileName }
  })
}

/**
 * 扫描并清理没有被任何数据引用的上传文件（仅超管）
 * @param {boolean} dryRun 只统计不删除
 * @param {number} graceHours 宽限期（小时），默认 24
 */
export function sweepStorage(dryRun = true, graceHours = 24) {
  return request({
    url: '/common/storage/sweep',
    method: 'post',
    params: { dryRun, graceHours }
  })
}
