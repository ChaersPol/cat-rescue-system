import request from '@/utils/request'

// 查询财务支出列表
export function listCatExpense(query) {
  return request({
    url: '/cat/expense/list',
    method: 'get',
    params: query
  })
}

// 查询财务支出详细
export function getCatExpense(id) {
  return request({
    url: '/cat/expense/' + id,
    method: 'get'
  })
}

// 新增财务支出
export function addCatExpense(data) {
  return request({
    url: '/cat/expense',
    method: 'post',
    data: data
  })
}

// 修改财务支出
export function updateCatExpense(data) {
  return request({
    url: '/cat/expense',
    method: 'put',
    data: data
  })
}

// 删除财务支出
export function delCatExpense(ids) {
  return request({
    url: '/cat/expense/' + ids,
    method: 'delete'
  })
}
