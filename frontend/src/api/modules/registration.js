import request from '../request'

export default {
  // 提交报名
  register(data) {
    return request({
      url: '/registrations',
      method: 'post',
      data,
      loadingMessage: '提交报名中...'
    })
  },

  // 获取报名列表
  getList(params) {
    return request({
      url: '/registrations',
      method: 'get',
      params
    })
  },

  // 获取报名详情
  getById(id) {
    return request({
      url: `/registrations/${id}`,
      method: 'get',
      loadingMessage: '加载报名详情...'
    })
  },

  // 更新报名状态
  updateStatus(id, status) {
    return request({
      url: `/registrations/${id}/status`,
      method: 'put',
      data: { status }
    })
  },

  // 获取用户的报名历史
  getUserRegistrations() {
    return request({
      url: '/registrations/user',
      method: 'get'
    })
  },

  // 取消报名
  cancel(id) {
    return request({
      url: `/registrations/${id}`,
      method: 'delete',
      loadingMessage: '取消报名中...'
    })
  },

  // 管理员审核报名
  review(id, status, remark) {
    return request({
      url: `/registrations/${id}/review`,
      method: 'put',
      params: { status, remark },
      loadingMessage: '审核报名中...'
    })
  },

  // 获取当前用户的报名记录
  getMyRegistrations() {
    return request({
      url: '/registrations/my',
      method: 'get',
      loadingMessage: '加载报名历史...'
    })
  },

  // 获取指定赛事的所有报名记录（管理员）
  getEventRegistrations(eventId) {
    return request({
      url: `/registrations/event/${eventId}`,
      method: 'get'
    })
  },

  // 根据状态筛选报名记录（管理员）
  getByStatus(status) {
    return request({
      url: `/registrations/status/${status}`,
      method: 'get'
    })
  },

  // 导出报名数据
  exportRegistrations(eventId) {
    return request({
      url: `/registrations/export/${eventId}`,
      method: 'get',
      responseType: 'blob'
    })
  }
}