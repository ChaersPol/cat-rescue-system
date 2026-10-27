import request from '@/utils/request'

// 查询TNR放归列表
export function listCatRelease(query) {
  return request({
    url: '/cat/release/list',
    method: 'get',
    params: query
  })
}

// 查询TNR放归详细
export function getCatRelease(id) {
  return request({
    url: '/cat/release/' + id,
    method: 'get'
  })
}

// 新增TNR放归
export function addCatRelease(data) {
  return request({
    url: '/cat/release',
    method: 'post',
    data: data
  })
}

// 修改TNR放归
export function updateCatRelease(data) {
  return request({
    url: '/cat/release',
    method: 'put',
    data: data
  })
}

// 删除TNR放归
export function delCatRelease(ids) {
  return request({
    url: '/cat/release/' + ids,
    method: 'delete'
  })
}
