import request from '../request'

export default {
  // 获取当前用户信息
  getProfile() {
    return request({
      url: '/users/me',
      method: 'get'
    })
  },

  // 获取指定用户信息（管理员）
  getById(id) {
    return request({
      url: `/users/${id}`,
      method: 'get'
    })
  },

  // 获取所有用户（管理员）
  getAll() {
    return request({
      url: '/users',
      method: 'get'
    })
  },

  // 创建用户（管理员）
  create(data) {
    return request({
      url: '/users',
      method: 'post',
      data
    })
  },

  // 更新当前用户信息
  updateProfile(data) {
    return request({
      url: '/users/me',
      method: 'put',
      data,
      loadingMessage: '更新信息中...'
    })
  },

  // 更新指定用户信息（管理员）
  update(id, data) {
    return request({
      url: `/users/${id}`,
      method: 'put',
      data
    })
  },

  // 删除用户（管理员）
  delete(id) {
    return request({
      url: `/users/${id}`,
      method: 'delete'
    })
  },

  // 修改当前用户密码
  changePassword(oldPassword, newPassword) {
    return request({
      url: '/users/me/password',
      method: 'put',
      data: {
        oldPassword,
        newPassword
      },
      loadingMessage: '修改密码中...'
    })
  },

  // 修改用户状态（管理员）
  updateStatus(id, status) {
    return request({
      url: `/users/${id}/status`,
      method: 'put',
      params: { status }
    })
  }
} 