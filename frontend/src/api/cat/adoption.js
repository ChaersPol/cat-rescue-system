import request from '@/utils/request'

// 查询领养登记列表
export function listCatAdoption(query) {
  return request({
    url: '/cat/adoption/list',
    method: 'get',
    params: query
  })
}

// 查询领养登记详细
export function getCatAdoption(id) {
  return request({
    url: '/cat/adoption/' + id,
    method: 'get'
  })
}

// 新增领养登记
export function addCatAdoption(data) {
  return request({
    url: '/cat/adoption',
    method: 'post',
    data: data
  })
}

// 修改领养登记
export function updateCatAdoption(data) {
  return request({
    url: '/cat/adoption',
    method: 'put',
    data: data
  })
}

// 删除领养登记
export function delCatAdoption(ids) {
  return request({
    url: '/cat/adoption/' + ids,
    method: 'delete'
  })
}
