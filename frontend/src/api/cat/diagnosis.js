import request from '@/utils/request'

// 查询站内诊断列表
export function listCatDiagnosis(query) {
  return request({
    url: '/cat/diagnosis/list',
    method: 'get',
    params: query
  })
}

// 查询站内诊断详细
export function getCatDiagnosis(id) {
  return request({
    url: '/cat/diagnosis/' + id,
    method: 'get'
  })
}

// 新增站内诊断
export function addCatDiagnosis(data) {
  return request({
    url: '/cat/diagnosis',
    method: 'post',
    data: data
  })
}

// 修改站内诊断
export function updateCatDiagnosis(data) {
  return request({
    url: '/cat/diagnosis',
    method: 'put',
    data: data
  })
}

// 删除站内诊断
export function delCatDiagnosis(ids) {
  return request({
    url: '/cat/diagnosis/' + ids,
    method: 'delete'
  })
}

// 追加更正备注（原始诊断不可修改）
export function correctCatDiagnosis(data) {
  return request({
    url: '/cat/diagnosis/correction',
    method: 'put',
    data: data
  })
}
