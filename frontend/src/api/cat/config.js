import request from '@/utils/request'

// 查询系统配置列表
export function listCatConfig(query) {
  return request({
    url: '/cat/config/list',
    method: 'get',
    params: query
  })
}

// 查询系统配置详细
export function getCatConfig(id) {
  return request({
    url: '/cat/config/' + id,
    method: 'get'
  })
}

// 新增系统配置
export function addCatConfig(data) {
  return request({
    url: '/cat/config',
    method: 'post',
    data: data
  })
}

// 修改系统配置
export function updateCatConfig(data) {
  return request({
    url: '/cat/config',
    method: 'put',
    data: data
  })
}

// 删除系统配置
export function delCatConfig(ids) {
  return request({
    url: '/cat/config/' + ids,
    method: 'delete'
  })
}
