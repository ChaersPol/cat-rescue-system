import request from '@/utils/request'

// 查询回访记录列表
export function listCatVisit(query) {
  return request({
    url: '/cat/visit/list',
    method: 'get',
    params: query
  })
}

// 查询回访记录详细
export function getCatVisit(id) {
  return request({
    url: '/cat/visit/' + id,
    method: 'get'
  })
}

// 新增回访记录
export function addCatVisit(data) {
  return request({
    url: '/cat/visit',
    method: 'post',
    data: data
  })
}

// 修改回访记录
export function updateCatVisit(data) {
  return request({
    url: '/cat/visit',
    method: 'put',
    data: data
  })
}

// 删除回访记录
export function delCatVisit(ids) {
  return request({
    url: '/cat/visit/' + ids,
    method: 'delete'
  })
}
