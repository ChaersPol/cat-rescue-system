import request from '@/utils/request'

// 扫码查询猫咪档案 + 病历时间线（免登录）
export function getCatMobileProfile(archiveNo) {
  return request({
    url: '/cat/mobile/profile/' + archiveNo,
    method: 'get'
  })
}

// 移动端首页：当前账号的聚合提醒列表
export function listMobileReminders() {
  return request({
    url: '/cat/mobile/reminders',
    method: 'get'
  })
}

// 移动端首页：标记单条提醒已读
export function readMobileReminder(reminderId) {
  return request({
    url: '/cat/mobile/reminders/read/' + reminderId,
    method: 'put'
  })
}

// 移动端首页：全部标记已读
export function readAllMobileReminders() {
  return request({
    url: '/cat/mobile/reminders/readAll',
    method: 'put'
  })
}
