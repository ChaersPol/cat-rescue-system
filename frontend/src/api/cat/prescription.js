import request from '@/utils/request'

// 处方列表（可按 catId / diagnosisId / status 过滤）
export function listCatPrescription(query) {
  return request({
    url: '/cat/prescription/list',
    method: 'get',
    params: query
  })
}

// 处方详情（含明细与变更留痕）
export function getCatPrescription(id) {
  return request({
    url: '/cat/prescription/' + id,
    method: 'get'
  })
}

// 按诊断单取处方（编辑诊断时回显，没有则返回空）
export function getCatPrescriptionByDiagnosis(diagnosisId) {
  return request({
    url: '/cat/prescription/byDiagnosis/' + diagnosisId,
    method: 'get'
  })
}

// 开处方（保存后按「天 × 时段 × 药」展开成用药记录）
export function addCatPrescription(data) {
  return request({
    url: '/cat/prescription',
    method: 'post',
    data: data
  })
}

// 改处方（只重建今天及以后、未完成打卡的明细，历史不动）
export function updateCatPrescription(data) {
  return request({
    url: '/cat/prescription',
    method: 'put',
    data: data
  })
}

// 停用处方
export function stopCatPrescription(id) {
  return request({
    url: '/cat/prescription/stop/' + id,
    method: 'put'
  })
}

// 删除处方
export function delCatPrescription(id) {
  return request({
    url: '/cat/prescription/' + id,
    method: 'delete'
  })
}
