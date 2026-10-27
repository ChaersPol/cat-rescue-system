import request from '@/utils/request'

// 查询死亡台账列表
export function listCatDeath(query) {
  return request({
    url: '/cat/death/list',
    method: 'get',
    params: query
  })
}

// 查询死亡台账详细
export function getCatDeath(id) {
  return request({
    url: '/cat/death/' + id,
    method: 'get'
  })
}

// 新增死亡台账
export function addCatDeath(data) {
  return request({
    url: '/cat/death',
    method: 'post',
    data: data
  })
}

// 修改死亡台账
export function updateCatDeath(data) {
  return request({
    url: '/cat/death',
    method: 'put',
    data: data
  })
}

// 删除死亡台账
export function delCatDeath(ids) {
  return request({
    url: '/cat/death/' + ids,
    method: 'delete'
  })
}
