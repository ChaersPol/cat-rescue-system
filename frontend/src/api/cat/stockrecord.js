import request from '@/utils/request'

// 查询出入库记录列表
export function listCatStockRecord(query) {
  return request({
    url: '/cat/stockrecord/list',
    method: 'get',
    params: query
  })
}

// 查询出入库记录详细
export function getCatStockRecord(id) {
  return request({
    url: '/cat/stockrecord/' + id,
    method: 'get'
  })
}

// 新增出入库记录
export function addCatStockRecord(data) {
  return request({
    url: '/cat/stockrecord',
    method: 'post',
    data: data
  })
}

// 修改出入库记录
export function updateCatStockRecord(data) {
  return request({
    url: '/cat/stockrecord',
    method: 'put',
    data: data
  })
}

// 删除出入库记录
export function delCatStockRecord(ids) {
  return request({
    url: '/cat/stockrecord/' + ids,
    method: 'delete'
  })
}

// 冲销出入库记录（生成反向记录并回滚库存）
export function reverseCatStockRecord(recordId) {
  return request({
    url: '/cat/stockrecord/reverse/' + recordId,
    method: 'post'
  })
}
