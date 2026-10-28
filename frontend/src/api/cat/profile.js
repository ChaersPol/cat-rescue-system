import request from '@/utils/request'

// 查询猫咪档案列表
export function listCatProfile(query) {
  return request({
    url: '/cat/profile/list',
    method: 'get',
    params: query
  })
}

// 查询猫咪档案详细
export function getCatProfile(id) {
  return request({
    url: '/cat/profile/' + id,
    method: 'get'
  })
}

// 新增猫咪档案
export function addCatProfile(data) {
  return request({
    url: '/cat/profile',
    method: 'post',
    data: data
  })
}

// 修改猫咪档案
export function updateCatProfile(data) {
  return request({
    url: '/cat/profile',
    method: 'put',
    data: data
  })
}

// 删除猫咪档案
export function delCatProfile(ids) {
  return request({
    url: '/cat/profile/' + ids,
    method: 'delete'
  })
}

// 重新生成单只猫咪二维码
export function regenerateCatQrCode(catId) {
  return request({
    url: '/cat/profile/qrcode/' + catId,
    method: 'post'
  })
}

// 批量为所有猫咪生成二维码
export function regenerateAllCatQrCode() {
  return request({
    url: '/cat/profile/qrcode/batch',
    method: 'post'
  })
}

// 单猫完整档案：基础信息 + 标签 + 相册 + 流转轨迹 + 病历时间线
export function getCatArchive(catId) {
  return request({
    url: '/cat/profile/archive/' + catId,
    method: 'get'
  })
}

// 归档（离世 / 放归 / 领养后不再出现在在养列表）
export function archiveCatProfile(catId) {
  return request({
    url: '/cat/profile/archive/' + catId,
    method: 'post'
  })
}

// 取消归档
export function unarchiveCatProfile(catId) {
  return request({
    url: '/cat/profile/unarchive/' + catId,
    method: 'post'
  })
}

// 批量取二维码（打印卡片用，列表接口不含 qr_code）
export function listCatQrCodes(catIds) {
  return request({
    url: '/cat/profile/qrcodes',
    method: 'get',
    params: { catIds: Array.isArray(catIds) ? catIds.join(',') : catIds }
  })
}

// 批量取「猫咪信息卡」打印数据（含二维码、最近疫苗/送医/诊断/用药）
export function listCatCards(catIds) {
  return request({
    url: '/cat/profile/cards',
    method: 'get',
    params: { catIds: Array.isArray(catIds) ? catIds.join(',') : catIds }
  })
}
