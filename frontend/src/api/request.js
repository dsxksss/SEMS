import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { ElMessage, ElLoading } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  // 不设置baseURL，直接使用完整路径
  // baseURL: '/api', 
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
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
service.interceptors.response.use(
  response => {
    // 如果返回的是二进制数据，直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    
    // 如果返回的是空数据，按特定配置处理
    if (!response.data) {
      // 如果请求配置了自定义错误处理，通过reject传递错误
      if (response.config.catchError) {
        return Promise.reject(new Error('空响应'))
      }
      // 没有配置自定义错误处理，直接返回空对象
      return {}
    }
    
    // 处理返回的数据，统一返回data部分
    const res = response.data
    
    // 检查是否有自定义错误代码
    if (res.code && res.code !== 200) {
      // 如果请求配置了自定义错误处理，则不显示错误消息
      if (!response.config.catchError) {
        const appStore = useAppStore()
        appStore.showError(res.message || '请求失败')
      }
      
      // 如果是登录过期或未授权
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 正常返回数据
    return res.data !== undefined ? res.data : res
  },
  error => {
    // 如果请求配置了自定义错误处理，则不显示错误消息
    if (!error.config?.catchError) {
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
        message = '服务器无响应，请检查网络连接或服务器状态'
      }
      
      appStore.showError(message)
    }
    
    return Promise.reject(error)
  }
)

export default service 