import request from '@/utils/request'

// 查询猫咪照片列表
export function listCatPhoto(query) {
  return request({
    url: '/cat/photo/list',
    method: 'get',
    params: query
  })
}

// 查询猫咪照片详细
export function getCatPhoto(id) {
  return request({
    url: '/cat/photo/' + id,
    method: 'get'
  })
}

// 新增猫咪照片
export function addCatPhoto(data) {
  return request({
    url: '/cat/photo',
    method: 'post',
    data: data
  })
}

// 修改猫咪照片
export function updateCatPhoto(data) {
  return request({
    url: '/cat/photo',
    method: 'put',
    data: data
  })
}

// 删除猫咪照片
export function delCatPhoto(ids) {
  return request({
    url: '/cat/photo/' + ids,
    method: 'delete'
  })
}

// 批量重建缩略图（历史数据补齐）
export function rebuildCatPhotoThumb(data) {
  return request({
    url: '/cat/photo/rebuildThumb',
    method: 'post',
    data: data || {}
  })
}

// 查询缩略图能力状态（ffmpeg 是否可用）
export function getCatPhotoThumbStatus() {
  return request({
    url: '/cat/photo/thumbStatus',
    method: 'get'
  })
}
