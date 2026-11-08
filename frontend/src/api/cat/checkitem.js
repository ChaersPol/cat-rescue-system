import request from '@/utils/request'

// 检查项目/用药目录（树形，一次取全）
export function listCatCheckItem(query) {
  return request({
    url: '/cat/checkitem/list',
    method: 'get',
    params: query
  })
}

// 开单可选项目：启用的叶子节点，带关联物资库存
export function optionCatCheckItem(query) {
  return request({
    url: '/cat/checkitem/options',
    method: 'get',
    params: query
  })
}

export function getCatCheckItem(itemId) {
  return request({
    url: '/cat/checkitem/' + itemId,
    method: 'get'
  })
}

export function addCatCheckItem(data) {
  return request({
    url: '/cat/checkitem',
    method: 'post',
    data: data
  })
}

export function updateCatCheckItem(data) {
  return request({
    url: '/cat/checkitem',
    method: 'put',
    data: data
  })
}

export function delCatCheckItem(itemId) {
  return request({
    url: '/cat/checkitem/' + itemId,
    method: 'delete'
  })
}
