import request from '@/utils/request'

// 查询基地列表
export function listCatBase(query) {
  return request({
    url: '/cat/base/list',
    method: 'get',
    params: query
  })
}

// 查询基地详细
export function getCatBase(id) {
  return request({
    url: '/cat/base/' + id,
    method: 'get'
  })
}

// 新增基地
export function addCatBase(data) {
  return request({
    url: '/cat/base',
    method: 'post',
    data: data
  })
}

// 修改基地
export function updateCatBase(data) {
  return request({
    url: '/cat/base',
    method: 'put',
    data: data
  })
}

// 删除基地
export function delCatBase(ids) {
  return request({
    url: '/cat/base/' + ids,
    method: 'delete'
  })
}
