import request from '@/utils/request'

// 查询流转记录列表
export function listCatFlow(query) {
  return request({
    url: '/cat/flow/list',
    method: 'get',
    params: query
  })
}

// 查询流转记录详细
export function getCatFlow(id) {
  return request({
    url: '/cat/flow/' + id,
    method: 'get'
  })
}

// 新增流转记录
export function addCatFlow(data) {
  return request({
    url: '/cat/flow',
    method: 'post',
    data: data
  })
}

// 修改流转记录
export function updateCatFlow(data) {
  return request({
    url: '/cat/flow',
    method: 'put',
    data: data
  })
}

// 删除流转记录
export function delCatFlow(ids) {
  return request({
    url: '/cat/flow/' + ids,
    method: 'delete'
  })
}
