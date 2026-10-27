import request from '@/utils/request'

// 统计看板数据
export function getDashboardStats() {
  return request({
    url: '/cat/dashboard/stats',
    method: 'get'
  })
}
