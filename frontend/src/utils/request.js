import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // 从环境变量获取baseURL
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// loading实例
let loadingInstance = null

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 显示loading
    if (config.loadingMessage) {
      loadingInstance = ElLoading.service({
        text: config.loadingMessage,
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }

    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    return config
  },
  error => {
    // 关闭loading
    if (loadingInstance) {
      loadingInstance.close()
    }
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 关闭loading
    if (loadingInstance) {
      loadingInstance.close()
    }

    const res = response.data

    // 如果响应成功但业务状态码不为200，显示错误信息
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }

    return res.data
  },
  error => {
    // 关闭loading
    if (loadingInstance) {
      loadingInstance.close()
    }

    // 处理401未授权
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      return Promise.reject(error)
    }

    // 显示错误信息
    ElMessage.error(error.response?.data?.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default service 