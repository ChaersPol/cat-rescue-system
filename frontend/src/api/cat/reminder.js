import request from '@/utils/request'

// 查询提醒通知列表
export function listCatReminder(query) {
  return request({
    url: '/cat/reminder/list',
    method: 'get',
    params: query
  })
}

// 查询提醒通知详细
export function getCatReminder(id) {
  return request({
    url: '/cat/reminder/' + id,
    method: 'get'
  })
}

// 新增提醒通知
export function addCatReminder(data) {
  return request({
    url: '/cat/reminder',
    method: 'post',
    data: data
  })
}

// 修改提醒通知
export function updateCatReminder(data) {
  return request({
    url: '/cat/reminder',
    method: 'put',
    data: data
  })
}

// 删除提醒通知
export function delCatReminder(ids) {
  return request({
    url: '/cat/reminder/' + ids,
    method: 'delete'
  })
}

// 手动生成到期提醒
export function generateCatReminder() {
  return request({
    url: '/cat/reminder/generate',
    method: 'post'
  })
}

// 顶栏铃铛：当前用户可见的提醒 + 未读数
export function headerCatReminder() {
  return request({
    url: '/cat/reminder/header',
    method: 'get'
  })
}

// 当前用户一键全部已读
export function markAllReadCatReminder() {
  return request({
    url: '/cat/reminder/readAll',
    method: 'put'
  })
}

// 标记单条已读
export function markReadCatReminder(reminderId) {
  return request({
    url: '/cat/reminder/read/' + reminderId,
    method: 'put'
  })
}

// 批量标记已读
export function markReadBatchCatReminder(reminderIds) {
  return request({
    url: '/cat/reminder/batchRead',
    method: 'put',
    data: reminderIds
  })
}
