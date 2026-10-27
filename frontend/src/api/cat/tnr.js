import request from '@/utils/request'

// 查询TNR抓捕列表
export function listCatTnr(query) {
  return request({
    url: '/cat/tnr/list',
    method: 'get',
    params: query
  })
}

// 查询TNR抓捕详细
export function getCatTnr(id) {
  return request({
    url: '/cat/tnr/' + id,
    method: 'get'
  })
}

// 新增TNR抓捕
export function addCatTnr(data) {
  return request({
    url: '/cat/tnr',
    method: 'post',
    data: data
  })
}

// 修改TNR抓捕
export function updateCatTnr(data) {
  return request({
    url: '/cat/tnr',
    method: 'put',
    data: data
  })
}

// 删除TNR抓捕
export function delCatTnr(ids) {
  return request({
    url: '/cat/tnr/' + ids,
    method: 'delete'
  })
}
