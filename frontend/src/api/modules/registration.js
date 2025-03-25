import request from '../request'

/**
 * 报名相关API
 */
const registrationApi = {
  /**
   * 获取报名列表
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回报名列表
   */
  getRegistrationList(params) {
    return request({
      url: '/registration/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取报名详情
   * @param {number} id - 报名ID
   * @returns {Promise} - 返回报名详情
   */
  getRegistrationDetail(id) {
    return request({
      url: `/registration/${id}`,
      method: 'get'
    })
  },

  /**
   * 获取赛事报名列表
   * @param {number} eventId - 赛事ID
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回报名列表
   */
  getEventRegistrations(eventId, params) {
    return request({
      url: `/registration/event/${eventId}`,
      method: 'get',
      params
    })
  },

  /**
   * 获取用户报名列表
   * @param {number} userId - 用户ID，不传则获取当前用户
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回报名列表
   */
  getUserRegistrations(userId, params) {
    const url = userId ? `/registration/user/${userId}` : '/registration/my'
    return request({
      url,
      method: 'get',
      params
    })
  },

  /**
   * 创建报名
   * @param {Object} data - 报名信息
   * @returns {Promise} - 返回创建结果
   */
  createRegistration(data) {
    return request({
      url: '/registration',
      method: 'post',
      data
    })
  },

  /**
   * 更新报名
   * @param {number} id - 报名ID
   * @param {Object} data - 报名更新信息
   * @returns {Promise} - 返回更新结果
   */
  updateRegistration(id, data) {
    return request({
      url: `/registration/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 取消报名
   * @param {number} id - 报名ID
   * @returns {Promise} - 返回取消结果
   */
  cancelRegistration(id) {
    return request({
      url: `/registration/${id}/cancel`,
      method: 'put'
    })
  },

  /**
   * 审核报名
   * @param {number} id - 报名ID
   * @param {Object} data - 审核信息
   * @returns {Promise} - 返回审核结果
   */
  reviewRegistration(id, data) {
    return request({
      url: `/registration/${id}/review`,
      method: 'put',
      data
    })
  },

  /**
   * 删除报名
   * @param {number} id - 报名ID
   * @returns {Promise} - 返回删除结果
   */
  deleteRegistration(id) {
    return request({
      url: `/registration/${id}`,
      method: 'delete'
    })
  }
}

export default registrationApi