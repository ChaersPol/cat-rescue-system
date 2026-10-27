import request from '@/utils/request'

// 查询物资分类列表
export function listCatMaterialCategory(query) {
  return request({
    url: '/cat/materialcategory/list',
    method: 'get',
    params: query
  })
}

// 查询物资分类详细
export function getCatMaterialCategory(id) {
  return request({
    url: '/cat/materialcategory/' + id,
    method: 'get'
  })
}

// 新增物资分类
export function addCatMaterialCategory(data) {
  return request({
    url: '/cat/materialcategory',
    method: 'post',
    data: data
  })
}

// 修改物资分类
export function updateCatMaterialCategory(data) {
  return request({
    url: '/cat/materialcategory',
    method: 'put',
    data: data
  })
}

// 删除物资分类
export function delCatMaterialCategory(ids) {
  return request({
    url: '/cat/materialcategory/' + ids,
    method: 'delete'
  })
}
