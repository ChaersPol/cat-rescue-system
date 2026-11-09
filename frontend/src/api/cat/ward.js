import request from '@/utils/request'

// 查询病房列表
export function listCatWard(query) {
  return request({
    url: '/cat/ward/list',
    method: 'get',
    params: query
  })
}

// 查询病房详细
export function getCatWard(id) {
  return request({
    url: '/cat/ward/' + id,
    method: 'get'
  })
}

// 新增病房
export function addCatWard(data) {
  return request({
    url: '/cat/ward',
    method: 'post',
    data: data
  })
}

// 修改病房
export function updateCatWard(data) {
  return request({
    url: '/cat/ward',
    method: 'put',
    data: data
  })
}

// 删除病房
export function delCatWard(ids) {
  return request({
    url: '/cat/ward/' + ids,
    method: 'delete'
  })
}

// 查询所选病房各自的在册猫数量（没有猫的病房也会返回，catCount = 0）
export function getWardCatCounts(wardIds) {
  return request({
    url: '/cat/ward/catCounts',
    method: 'get',
    params: { wardIds: Array.isArray(wardIds) ? wardIds.join(',') : wardIds }
  })
}
