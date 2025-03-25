// 导入请求工具
import request from './request'

// 导入各个API模块
import authApi from './modules/auth'
import eventApi from './modules/event'
import announcementApi from './modules/announcement'
import userApi from './modules/user'
import registrationApi from './modules/registration'

// 导出所有API
export {
  request,
  authApi,
  eventApi,
  announcementApi,
  userApi,
  registrationApi
}

// 默认导出所有API的集合
export default {
  auth: authApi,
  event: eventApi,
  announcement: announcementApi,
  user: userApi,
  registration: registrationApi
} 