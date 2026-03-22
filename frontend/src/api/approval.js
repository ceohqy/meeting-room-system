import request from './request'

export const approvalApi = {

  // 查询审批列表
  list: (params) => request.get('/approval/list', { params }),

  // 审批通过
  pass: (data) => request.post('/approval/pass', data),

  // 审批拒绝
  reject: (data) => request.post('/approval/reject', data)

}