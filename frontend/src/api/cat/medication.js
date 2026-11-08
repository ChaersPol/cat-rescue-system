import request from '@/utils/request'

// 查询用药记录列表
export function listCatMedication(query) {
  return request({
    url: '/cat/medication/list',
    method: 'get',
    params: query
  })
}

// 查询用药记录详细
export function getCatMedication(id) {
  return request({
    url: '/cat/medication/' + id,
    method: 'get'
  })
}

// 新增用药记录
export function addCatMedication(data) {
  return request({
    url: '/cat/medication',
    method: 'post',
    data: data
  })
}

// 修改用药记录
export function updateCatMedication(data) {
  return request({
    url: '/cat/medication',
    method: 'put',
    data: data
  })
}

// 删除用药记录
export function delCatMedication(ids) {
  return request({
    url: '/cat/medication/' + ids,
    method: 'delete'
  })
}

// 批量录入连续多天用药（from useDate 起连续 days 天）
export function addCatMedicationBatch(data, days) {
  return request({
    url: '/cat/medication/batch',
    method: 'post',
    params: { days },
    data: data
  })
}

// ============ 合并视图（同一只猫 + 同一天 + 同一时间段 一行） ============

// 合并列表
export function listCatMedicationGroup(query) {
  return request({
    url: '/cat/medication/group/list',
    method: 'get',
    params: query
  })
}

// 某一组的药品明细（展开行时才查）
export function getCatMedicationGroupDetails(params) {
  return request({
    url: '/cat/medication/group/details',
    method: 'get',
    params
  })
}

// 医生改这一组的用药方案（可增删药品，已完成打卡的组不能改）
export function saveCatMedicationGroup(data) {
  return request({
    url: '/cat/medication/group',
    method: 'put',
    data: data
  })
}

// 执行打卡（整组）：完成 / 撤回 / 补照片
export function saveCatMedicationExec(data) {
  return request({
    url: '/cat/medication/exec',
    method: 'put',
    data: data
  })
}
