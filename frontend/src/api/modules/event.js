import request from '../request'

/**
 * 赛事相关API
 */
const eventApi = {
  /**
   * 获取赛事列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.category - 赛事类别
   * @param {string} params.status - 赛事状态
   * @returns {Promise} - 返回赛事列表
   */
  getEventList(params) {
    return request({
      url: '/events',
      method: 'get',
      params
    })
  },

  /**
   * 获取赛事详情
   * @param {string|number} id - 赛事ID
   * @returns {Promise} - 返回赛事详情
   */
  getEventDetail(id) {
    return request({
      url: `/events/${id}`,
      method: 'get'
    })
  },

  /**
   * 创建赛事
   * @param {Object} data - 赛事信息
   * @returns {Promise} - 返回创建结果
   */
  createEvent(data) {
    return request({
      url: '/events',
      method: 'post',
      data
    })
  },

  /**
   * 更新赛事
   * @param {string|number} id - 赛事ID
   * @param {Object} data - 赛事信息
   * @returns {Promise} - 返回更新结果
   */
  updateEvent(id, data) {
    return request({
      url: `/events/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除赛事
   * @param {string|number} id - 赛事ID
   * @returns {Promise} - 返回删除结果
   */
  deleteEvent(id) {
    return request({
      url: `/events/${id}`,
      method: 'delete'
    })
  },

  /**
   * 获取最新赛事
   * @param {number} limit - 获取数量
   * @returns {Promise} - 返回最新赛事列表
   */
  getLatestEvents(limit = 3) {
    return request({
      url: '/events/latest',
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 获取推荐赛事
   * @param {number} limit - 获取数量
   * @returns {Promise} - 返回推荐赛事列表
   */
  getRecommendedEvents(limit = 3) {
    return request({
      url: '/events/recommended',
      method: 'get',
      params: { limit }
    })
  },
  
  /**
   * 获取赛事统计数据
   * @returns {Promise} - 返回赛事统计数据
   */
  getEventStats() {
    return request({
      url: '/events/stats',
      method: 'get'
    })
  },
  
  /**
   * 获取用户参与的赛事
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回用户参与的赛事列表
   */
  getUserEvents(params) {
    return request({
      url: '/user/events',
      method: 'get',
      params
    })
  },
  
  /**
   * 获取即将到来的活动
   * @param {Object} params - 查询参数
   * @param {number} params.days - 未来几天内的活动
   * @returns {Promise} - 返回活动列表
   */
  getUpcomingEvents(params) {
    return request({
      url: '/api/events/upcoming',
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
   * 获取公开赛事（不需要登录）
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.query - 搜索关键词
   * @param {string} params.category - 赛事类别
   * @param {string} params.status - 赛事状态
   * @param {string} params.startDate - 开始日期
   * @param {string} params.endDate - 结束日期
   * @returns {Promise} - 返回赛事列表
   */
  getPublicEvents(params) {
    return request({
      url: '/events/public',
      method: 'get',
      params
    })
  }
}

export default eventApi