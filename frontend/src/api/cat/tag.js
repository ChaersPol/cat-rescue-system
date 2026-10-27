import request from '@/utils/request'

// 查询标签列表
export function listCatTag(query) {
  return request({
    url: '/cat/tag/list',
    method: 'get',
    params: query
  })
}

// 查询标签详细
export function getCatTag(id) {
  return request({
    url: '/cat/tag/' + id,
    method: 'get'
  })
}

// 新增标签
export function addCatTag(data) {
  return request({
    url: '/cat/tag',
    method: 'post',
    data: data
  })
}

// 修改标签
export function updateCatTag(data) {
  return request({
    url: '/cat/tag',
    method: 'put',
    data: data
  })
}

// 删除标签
export function delCatTag(ids) {
  return request({
    url: '/cat/tag/' + ids,
    method: 'delete'
  })
}
