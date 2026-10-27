import request from '@/utils/request'

// 查询病房授权列表
export function listCatWardAuth(query) {
  return request({
    url: '/cat/wardauth/list',
    method: 'get',
    params: query
  })
}

// 查询某用户已授权病房id列表
export function getWardIdsByUser(userId) {
  return request({
    url: '/cat/wardauth/wards/' + userId,
    method: 'get'
  })
}

// 查询某病房已授权用户id列表
export function getUserIdsByWard(wardId) {
  return request({
    url: '/cat/wardauth/users/' + wardId,
    method: 'get'
  })
}

// 保存某用户的病房授权
export function saveWardAuth(data) {
  return request({
    url: '/cat/wardauth/save',
    method: 'post',
    data: data
  })
}

// 新增单条授权
export function addCatWardAuth(data) {
  return request({
    url: '/cat/wardauth',
    method: 'post',
    data: data
  })
}

// 删除某用户全部授权
export function delWardAuthByUser(userId) {
  return request({
    url: '/cat/wardauth/removeUser/' + userId,
    method: 'post'
  })
}
