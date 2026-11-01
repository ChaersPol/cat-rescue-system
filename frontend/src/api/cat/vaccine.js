import request from '@/utils/request'

// 查询疫苗记录列表
export function listCatVaccine(query) {
  return request({
    url: '/cat/vaccine/list',
    method: 'get',
    params: query
  })
}

// 查询疫苗记录详细
export function getCatVaccine(id) {
  return request({
    url: '/cat/vaccine/' + id,
    method: 'get'
  })
}

// 新增疫苗记录
export function addCatVaccine(data) {
  return request({
    url: '/cat/vaccine',
    method: 'post',
    data: data
  })
}

// 修改疫苗记录
export function updateCatVaccine(data) {
  return request({
    url: '/cat/vaccine',
    method: 'put',
    data: data
  })
}

// 删除疫苗记录
export function delCatVaccine(ids) {
  return request({
    url: '/cat/vaccine/' + ids,
    method: 'delete'
  })
}

// 查某只猫是否在用药期（在用药期不能接种，需要推迟）
export function getVaccineMedicationTip(catId) {
  return request({
    url: '/cat/vaccine/medicationTip/' + catId,
    method: 'get'
  })
}
