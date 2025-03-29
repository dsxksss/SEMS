import axios from 'axios';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';

// 全局变量声明
declare global {
  interface Window {
    __isShowingExpiredMessage?: boolean;
  }
}

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
      
      // 尝试解析token并检查过期时间
      try {
        const parts = token.split('.');
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]));
          const expTime = payload.exp * 1000; // 转换为毫秒
          const nowTime = Date.now();
          const remainingTime = expTime - nowTime;
          
          console.log(`Token信息 - 过期时间: ${new Date(expTime).toLocaleString()}, 当前时间: ${new Date(nowTime).toLocaleString()}`);
          console.log(`Token剩余有效期: ${Math.floor(remainingTime / 1000 / 60)} 分钟 ${Math.floor(remainingTime / 1000 % 60)} 秒`);
          
          if (remainingTime <= 0) {
            console.warn('Token已过期，但仍在使用。将尝试请求但可能会失败。');
          }
        }
      } catch (e) {
        console.error('解析Token时出错:', e);
      }
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
    // 存储原始请求的配置
    const originalRequest = error.config;
    
    if (error.response) {
      const { status } = error.response;
      const url = originalRequest?.url || '';
      
      // 处理未授权错误
      if (status === 401) {
        console.error('认证错误 (401):', error.response.data);
        console.error('当前token:', localStorage.getItem('token'));
        console.error('请求URL:', originalRequest.url);
        console.error('请求方法:', originalRequest.method);
        
        // 检查是否是登录请求，如果是登录请求返回401，不执行退出操作
        if (url.includes('/auth/signin')) {
          console.warn('登录请求返回401，可能是用户名或密码错误');
          return Promise.reject(error);
        }
        
        // 检查该请求是否已经尝试过重新获取token
        if (!originalRequest._retry) {
          originalRequest._retry = true;
          
          // 尝试刷新token
          try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
              console.log('尝试使用refreshToken刷新认证...');
              
              // 不使用apiClient避免循环拦截
              const response = await axios.post('/api/auth/refresh', { refreshToken });
              
              if (response.data && response.data.token) {
                // 保存新token
                localStorage.setItem('token', response.data.token);
                if (response.data.refreshToken) {
                  localStorage.setItem('refreshToken', response.data.refreshToken);
                }
                
                // 更新原始请求的认证头
                originalRequest.headers.Authorization = `Bearer ${response.data.token}`;
                
                // 重新发送原始请求
                console.log('使用新token重新发送请求...');
                return axios(originalRequest);
              }
            }
          } catch (refreshError) {
            console.error('刷新token失败:', refreshError);
          }
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
          
          // 避免多次显示登录过期提示
          if (!window.__isShowingExpiredMessage) {
            window.__isShowingExpiredMessage = true;
            
            // 清除认证信息
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
            
            // 延迟执行登出操作，让用户看到提示信息
            setTimeout(() => {
              ElMessage.error('您的登录已过期，请重新登录');
              authStore.logout();
              window.__isShowingExpiredMessage = false;
            }, 1000);
          }
          
          return Promise.reject(error);
        }
      } else if (status === 403) {
        // 处理权限不足错误
        ElMessage.error('您没有权限执行此操作');
        console.error('权限错误 (403):', error.response.data);
      } else if (status === 404) {
        // 处理资源不存在错误
        console.error('资源不存在 (404):', error.response.data);
      } else if (status >= 500) {
        // 处理服务器错误
        ElMessage.error('服务器错误，请稍后重试');
        console.error('服务器错误:', error.response.data);
      }
      
      // 处理其他错误
      console.error('API Error:', error.response.data);
    } else if (error.request) {
      console.error('No response received:', error.request);
      ElMessage.error('网络连接异常，请检查网络连接');
    } else {
      console.error('Request error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 