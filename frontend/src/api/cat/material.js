import request from '@/utils/request'

// 查询物资列表
export function listCatMaterial(query) {
  return request({
    url: '/cat/material/list',
    method: 'get',
    params: query
  })
}

// 查询物资详细
export function getCatMaterial(id) {
  return request({
    url: '/cat/material/' + id,
    method: 'get'
  })
}

// 新增物资
export function addCatMaterial(data) {
  return request({
    url: '/cat/material',
    method: 'post',
    data: data
  })
}

// 修改物资
export function updateCatMaterial(data) {
  return request({
    url: '/cat/material',
    method: 'put',
    data: data
  })
}

// 删除物资
export function delCatMaterial(ids) {
  return request({
    url: '/cat/material/' + ids,
    method: 'delete'
  })
}
