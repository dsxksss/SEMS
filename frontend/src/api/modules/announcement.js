import request from '../request'

/**
 * 公告相关API
 */
const announcementApi = {
  /**
   * 获取公告列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.keyword - 搜索关键词
   * @returns {Promise} - 返回公告列表
   */
  getAnnouncementList(params) {
    return request({
      url: '/announcements',
      method: 'get',
      params
    })
  },

  /**
   * 获取公告详情
   * @param {string|number} id - 公告ID
   * @returns {Promise} - 返回公告详情
   */
  getAnnouncementDetail(id) {
    return request({
      url: `/announcements/${id}`,
      method: 'get'
    })
  },

  /**
   * 创建公告
   * @param {Object} data - 公告信息
   * @returns {Promise} - 返回创建结果
   */
  createAnnouncement(data) {
    return request({
      url: '/announcements',
      method: 'post',
      data
    })
  },

  /**
   * 更新公告
   * @param {string|number} id - 公告ID
   * @param {Object} data - 公告信息
   * @returns {Promise} - 返回更新结果
   */
  updateAnnouncement(id, data) {
    return request({
      url: `/announcements/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除公告
   * @param {string|number} id - 公告ID
   * @returns {Promise} - 返回删除结果
   */
  deleteAnnouncement(id) {
    return request({
      url: `/announcements/${id}`,
      method: 'delete'
    })
  },

  /**
   * 获取最新公告
   * @param {number} limit - 获取数量
   * @returns {Promise} - 返回最新公告列表
   */
  getLatestAnnouncements(limit = 5) {
    return request({
      url: '/announcements/latest',
      method: 'get',
      params: { limit }
    })
  },
  
  /**
   * 获取指定类型的公告
   * @param {string} type - 公告类型
   * @returns {Promise} - 返回指定类型的公告列表
   */
  getAnnouncementsByType(type) {
    return request({
      url: `/announcements/type/${type}`,
      method: 'get'
    })
  },
  
  /**
   * 获取活跃公告
   * @returns {Promise} - 返回活跃公告列表
   */
  getActiveAnnouncements() {
    return request({
      url: '/announcements/active',
      method: 'get'
    })
  }
}

export default announcementApi 