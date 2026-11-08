import request from '@/utils/request'

// 在职员工候选（scope: doctor / assistant / all）
export function listCatStaffOptions(scope) {
  return request({
    url: '/cat/staff/options',
    method: 'get',
    params: { scope }
  })
}
