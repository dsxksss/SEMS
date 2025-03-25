import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

// 创建axios实例
const request = axios.create({
  baseURL: '', // 这里不设置baseURL，因为已经在main.js中设置了全局baseURL
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
    'Accept': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    // 如果有token则添加到请求头
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果返回的是二进制数据，直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    
    // 处理返回的数据，统一返回data部分
    const res = response.data
    
    // 检查是否有自定义错误代码
    if (res.code && res.code !== 200) {
      const appStore = useAppStore()
      appStore.showError(res.message || '请求失败')
      
      // 如果是登录过期或未授权
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res.data // 直接返回data部分
  },
  error => {
    const appStore = useAppStore()
    
    // 获取错误信息
    let message = error.message || '请求失败，请稍后再试'
    
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = error.response.data?.message || '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          // 登录过期处理
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = error.response.data?.message || '服务器内部错误'
          break
        default:
          message = error.response.data?.message || `请求失败(${error.response.status})`
      }
    } else if (error.request) {
      message = '服务器无响应，请检查网络连接'
    }
    
    appStore.showError(message)
    return Promise.reject(error)
  }
)

export default request 