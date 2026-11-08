import request from '@/utils/request'

// 查询站内诊断列表
export function listCatDiagnosis(query) {
  return request({
    url: '/cat/diagnosis/list',
    method: 'get',
    params: query
  })
}

// 按猫合并视图：一只猫一行（就诊次数、首次/最近就诊、最近诊断）
export function listCatDiagnosisSummary(query) {
  return request({
    url: '/cat/diagnosis/summary',
    method: 'get',
    params: query
  })
}

// 当前筛选范围的统计：多少只猫 + 多少条诊断记录
export function getDiagnosisStat(query) {
  return request({
    url: '/cat/diagnosis/stat',
    method: 'get',
    params: query
  })
}

// 病房下拉：每个病房当前在册猫数（已放归/已领养/已离世不计入）
export function listDiagnosisWardStats(query) {
  return request({
    url: '/cat/diagnosis/wardStats',
    method: 'get',
    params: query
  })
}

// 选中猫咪后带出来源基地、当前病房、默认就诊类型
export function getDiagnosisCatInfo(catId) {
  return request({
    url: '/cat/diagnosis/catInfo/' + catId,
    method: 'get'
  })
}

// 接诊医生候选（「医生」角色的账号）
export function listDiagnosisDoctorOptions() {
  return request({
    url: '/cat/diagnosis/doctorOptions',
    method: 'get'
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
