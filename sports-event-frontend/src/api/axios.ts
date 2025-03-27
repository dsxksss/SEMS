import axios from 'axios';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';

// 创建axios实例
const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000
});

// 请求拦截器 - 为所有请求添加认证头
apiClient.interceptors.request.use(
  (config) => {
    // 从localStorage获取token，这样可以确保总是使用最新的token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`请求: ${config.method?.toUpperCase()} ${config.url}，已添加Token`);
    } else {
      console.log(`请求: ${config.method?.toUpperCase()} ${config.url}，无Token`);
    }
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理统一错误
apiClient.interceptors.response.use(
  (response) => {
    console.log(`响应成功: ${response.config.method?.toUpperCase()} ${response.config.url}`);
    return response;
  },
  async (error) => {
    if (error.response) {
      const { status } = error.response;
      const url = error.config?.url || '';
      
      // 处理未授权错误
      if (status === 401) {
        console.error('认证错误 (401):', error.response.data);
        console.error('当前token:', localStorage.getItem('token'));
        console.error('请求URL:', error.config.url);
        console.error('请求方法:', error.config.method);
        
        // 检查是否是登录请求，如果是登录请求返回401，不执行退出操作
        if (url.includes('/auth/signin')) {
          console.warn('登录请求返回401，可能是用户名或密码错误');
          return Promise.reject(error);
        }
        
        // 对于事件相关API，检查是否可以使用公共API
        if (url.includes('/events/')) {
          // 如果是获取单个事件的请求，尝试通过eventsAPI.getPublicEventById获取
          console.warn('事件API请求返回401，将尝试使用公共API');
          
          // 由于拦截器无法直接修改返回内容，让eventsAPI.getEventById内部处理
          return Promise.reject(error);
        }
        
        // 检查token是否已经过期
        const authStore = useAuthStore();
        if (authStore && authStore.checkTokenExpiration(false)) {
          console.warn('Token已过期，需要重新登录');
          // 清除认证信息
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          
          // 延迟执行登出操作，让用户看到提示信息
          setTimeout(() => {
            ElMessage.error('您的登录已过期，请重新登录');
            authStore.logout();
          }, 1000);
          
          return Promise.reject(error);
        }
      }
      
      // 处理其他错误
      console.error('API Error:', error.response.data);
    } else if (error.request) {
      console.error('No response received:', error.request);
    } else {
      console.error('Request error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 