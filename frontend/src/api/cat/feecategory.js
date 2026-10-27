import request from '@/utils/request'

// 查询费用分类列表
export function listCatFeeCategory(query) {
  return request({
    url: '/cat/feecategory/list',
    method: 'get',
    params: query
  })
}

// 查询费用分类详细
export function getCatFeeCategory(id) {
  return request({
    url: '/cat/feecategory/' + id,
    method: 'get'
  })
}

// 新增费用分类
export function addCatFeeCategory(data) {
  return request({
    url: '/cat/feecategory',
    method: 'post',
    data: data
  })
}

// 修改费用分类
export function updateCatFeeCategory(data) {
  return request({
    url: '/cat/feecategory',
    method: 'put',
    data: data
  })
}

// 删除费用分类
export function delCatFeeCategory(ids) {
  return request({
    url: '/cat/feecategory/' + ids,
    method: 'delete'
  })
}
