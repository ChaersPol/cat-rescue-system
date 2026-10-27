import request from '@/utils/request'

// 查询驱虫记录列表
export function listCatDeworm(query) {
  return request({
    url: '/cat/deworm/list',
    method: 'get',
    params: query
  })
}

// 查询驱虫记录详细
export function getCatDeworm(id) {
  return request({
    url: '/cat/deworm/' + id,
    method: 'get'
  })
}

// 新增驱虫记录
export function addCatDeworm(data) {
  return request({
    url: '/cat/deworm',
    method: 'post',
    data: data
  })
}

// 修改驱虫记录
export function updateCatDeworm(data) {
  return request({
    url: '/cat/deworm',
    method: 'put',
    data: data
  })
}

// 删除驱虫记录
export function delCatDeworm(ids) {
  return request({
    url: '/cat/deworm/' + ids,
    method: 'delete'
  })
}
