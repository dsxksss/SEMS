import request from './request'
import authApi from './modules/auth'
import eventApi from './modules/event'
import announcementApi from './modules/announcement'

// 导出所有API
export {
  authApi,
  eventApi,
  announcementApi
}

// 导出请求函数
export { request }

// 默认导出所有API的集合
export default {
  auth: authApi,
  event: eventApi,
  announcement: announcementApi
} 