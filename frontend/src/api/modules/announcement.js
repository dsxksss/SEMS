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
   * @param {Object} params - 查询参数
   * @param {number} params.limit - 获取条数
   * @returns {Promise} - 返回公告列表
   */
  getLatestAnnouncements(params) {
    return request({
      url: '/api/announcements/latest',
      method: 'get',
      params,
      catchError: true,
      transformResponse: [
        data => {
          try {
            if (!data || data.trim() === '') {
              console.warn('服务器返回了空响应')
              return []
            }
            
            const parsed = JSON.parse(data)
            
            if (parsed.code !== undefined) {
              if (parsed.code === 200) {
                return parsed.data || []
              } else {
                console.warn('服务器返回了错误代码:', parsed.code, parsed.message)
                return []
              }
            }
            
            if (Array.isArray(parsed)) {
              return parsed
            }
            
            if (parsed && parsed.content && Array.isArray(parsed.content)) {
              return parsed.content
            }
            
            console.warn('未识别的响应格式:', parsed)
            return parsed || []
          } catch (error) {
            console.error('解析响应数据失败:', error)
            return []
          }
        }
      ]
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