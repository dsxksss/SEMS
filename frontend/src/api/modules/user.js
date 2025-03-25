import request from '../request'

/**
 * 用户相关API
 */
const userApi = {
  /**
   * 获取用户列表
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回用户列表
   */
  getUserList(params) {
    return request({
      url: '/user/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取用户详情
   * @param {number} id - 用户ID
   * @returns {Promise} - 返回用户详情
   */
  getUserDetail(id) {
    return request({
      url: `/user/${id}`,
      method: 'get'
    })
  },

  /**
   * 添加用户
   * @param {Object} data - 用户信息
   * @returns {Promise} - 返回添加结果
   */
  addUser(data) {
    return request({
      url: '/user/add',
      method: 'post',
      data
    })
  },

  /**
   * 更新用户信息
   * @param {number} id - 用户ID
   * @param {Object} data - 用户更新信息
   * @returns {Promise} - 返回更新结果
   */
  updateUser(id, data) {
    return request({
      url: `/user/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除用户
   * @param {number} id - 用户ID
   * @returns {Promise} - 返回删除结果
   */
  deleteUser(id) {
    return request({
      url: `/user/${id}`,
      method: 'delete'
    })
  },

  /**
   * 更新用户状态
   * @param {number} id - 用户ID
   * @param {Object} data - 包含状态的对象
   * @returns {Promise} - 返回状态更新结果
   */
  updateUserStatus(id, data) {
    return request({
      url: `/user/${id}/status`,
      method: 'put',
      data
    })
  },

  /**
   * 更新个人资料
   * @param {Object} data - 个人资料信息
   * @returns {Promise} - 返回更新结果
   */
  updateProfile(data) {
    return request({
      url: '/user/profile',
      method: 'put',
      data
    })
  },

  /**
   * 更新密码
   * @param {Object} data - 密码信息
   * @returns {Promise} - 返回更新结果
   */
  updatePassword(data) {
    return request({
      url: '/user/password',
      method: 'put',
      data
    })
  },

  /**
   * 获取当前用户信息
   * @returns {Promise} - 返回当前用户信息
   */
  getCurrentUser() {
    return request({
      url: '/user/current',
      method: 'get'
    })
  }
}

export default userApi 