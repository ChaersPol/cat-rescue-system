import request from '@/utils/request'

// 查询病房类型列表
export function listCatWardType(query) {
  return request({
    url: '/cat/wardtype/list',
    method: 'get',
    params: query
  })
}

// 全部正常状态类型（下拉用，不分页）
export function optionselectWardType() {
  return request({
    url: '/cat/wardtype/optionselect',
    method: 'get'
  })
}

// 查询病房类型详细
export function getCatWardType(id) {
  return request({
    url: '/cat/wardtype/' + id,
    method: 'get'
  })
}

// 新增病房类型
export function addCatWardType(data) {
  return request({
    url: '/cat/wardtype',
    method: 'post',
    data: data
  })
}

// 修改病房类型
export function updateCatWardType(data) {
  return request({
    url: '/cat/wardtype',
    method: 'put',
    data: data
  })
}

// 删除病房类型
export function delCatWardType(ids) {
  return request({
    url: '/cat/wardtype/' + ids,
    method: 'delete'
  })
}
