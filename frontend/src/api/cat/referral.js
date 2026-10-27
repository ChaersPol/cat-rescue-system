import request from '@/utils/request'

// 查询转诊记录列表
export function listCatReferral(query) {
  return request({
    url: '/cat/referral/list',
    method: 'get',
    params: query
  })
}

// 查询转诊记录详细
export function getCatReferral(id) {
  return request({
    url: '/cat/referral/' + id,
    method: 'get'
  })
}

// 新增转诊记录
export function addCatReferral(data) {
  return request({
    url: '/cat/referral',
    method: 'post',
    data: data
  })
}

// 修改转诊记录
export function updateCatReferral(data) {
  return request({
    url: '/cat/referral',
    method: 'put',
    data: data
  })
}

// 删除转诊记录
export function delCatReferral(ids) {
  return request({
    url: '/cat/referral/' + ids,
    method: 'delete'
  })
}
