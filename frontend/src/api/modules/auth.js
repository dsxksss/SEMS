import request from '../request'

/**
 * 用户认证相关API
 */
const authApi = {
  /**
   * 用户登录
   * @param {Object} data - 登录信息
   * @param {string} data.username - 用户名
   * @param {string} data.password - 密码
   * @returns {Promise} - 返回登录结果
   */
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  /**
   * 用户注册
   * @param {Object} data - 注册信息
   * @param {string} data.username - 用户名
   * @param {string} data.password - 密码
   * @param {string} data.email - 邮箱
   * @returns {Promise} - 返回注册结果
   */
  register(data) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  /**
   * 用户登出
   * @returns {Promise} - 返回登出结果
   */
  logout() {
    return request({
      url: '/auth/logout',
      method: 'post'
    })
  },

  /**
   * 获取验证码
   * @param {string} email - 邮箱
   * @returns {Promise} - 返回验证码发送结果
   */
  getVerifyCode(email) {
    return request({
      url: '/auth/verify-code',
      method: 'post',
      data: { email }
    })
  },

  /**
   * 重置密码
   * @param {Object} data - 重置密码信息
   * @param {string} data.email - 邮箱
   * @param {string} data.code - 验证码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise} - 返回重置结果
   */
  resetPassword(data) {
    return request({
      url: '/auth/reset-password',
      method: 'post',
      data
    })
  }
}

export default authApi 