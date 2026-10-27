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
