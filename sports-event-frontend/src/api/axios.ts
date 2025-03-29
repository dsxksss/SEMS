import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';

// 扩展Axios请求配置类型
declare module 'axios' {
  export interface InternalAxiosRequestConfig {
    _retryCount?: number;
    _retry?: boolean;
  }
}

// 全局变量声明
declare global {
  interface Window {
    __isShowingExpiredMessage?: boolean;
    __errorCount?: Record<string, number>;
  }
}

// 创建axios实例
const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000 // 增加超时时间，提高可靠性
});

// 重试配置
const MAX_RETRIES = 2;  // 最大重试次数

// 初始化错误计数器
window.__errorCount = window.__errorCount || {};

// 检查URL是否为统计/报表相关
const isReportOrStatUrl = (url: string): boolean => {
  return url.includes('/stats/') || 
         url.includes('/reports/') || 
         url.includes('/certificates/');
};

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
    
    // 添加防止缓存的参数，确保请求不被浏览器缓存
    if (config.method === 'get') {
      config.params = config.params || {};
      config.params._t = Date.now();
    }
    
    // 添加用于重试的标记
    config._retryCount = config._retryCount || 0;
    
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
    
    // 重置该URL的错误计数
    if (response.config.url) {
      window.__errorCount![response.config.url] = 0;
    }
    
    return response;
  },
  async (error) => {
    // 存储原始请求的配置
    const originalRequest = error.config;
    const url = originalRequest?.url || '';
    
    // 增加错误计数
    window.__errorCount![url] = (window.__errorCount![url] || 0) + 1;
    const errorCount = window.__errorCount![url];
    
    // 检查是否为超时错误
    const isTimeoutError = error.code === 'ECONNABORTED' && error.message && error.message.includes('timeout');
    
    // 检查是否为网络错误
    const isNetworkError = !error.response && error.message === 'Network Error';
    
    // 检查是否要重试请求
    if ((isTimeoutError || isNetworkError) && originalRequest && originalRequest._retryCount < MAX_RETRIES) {
      originalRequest._retryCount++;
      console.log(`请求超时或网络错误，正在重试第 ${originalRequest._retryCount} 次: ${url}`);
      
      // 网络错误更频繁时增加延迟
      const delay = originalRequest._retryCount * 1000; // 每次多等1秒
      await new Promise(resolve => setTimeout(resolve, delay));
      
      return apiClient(originalRequest);
    }
    
    if (error.response) {
      const { status } = error.response;
      
      // 处理未授权错误
      if (status === 401) {
        console.error('认证错误 (401):', error.response.data);
        console.error('当前token:', localStorage.getItem('token'));
        console.error('请求URL:', url);
        console.error('请求方法:', originalRequest.method);
        
        // 检查是否是登录请求，如果是登录请求返回401，不执行退出操作
        if (url.includes('/auth/signin')) {
          console.warn('登录请求返回401，可能是用户名或密码错误');
          return Promise.reject(error);
        }
        
        // 对统计和报表相关API的特殊处理
        if (isReportOrStatUrl(url)) {
          console.warn('报表或统计API 401错误，返回空数据而不是登出用户');
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
          
          // 如果刷新token失败，尝试重新登录
          const authStore = useAuthStore();
          if (authStore) {
            // 避免多次显示登录过期提示
            if (!window.__isShowingExpiredMessage) {
              window.__isShowingExpiredMessage = true;
              
              // 清除认证信息并重新登录
              ElMessage.error('您的会话已过期，即将重新登录');
              setTimeout(() => {
                // 清除Token
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                
                // 保存当前页面路径
                const currentPath = window.location.pathname + window.location.search;
                localStorage.setItem('redirectPath', currentPath);
                
                // 执行登出操作
                authStore.logout();
                window.__isShowingExpiredMessage = false;
                
                // 重定向到登录页面
                window.location.href = '/login';
              }, 1500);
            }
          }
        }
        
        // 对于事件相关API，检查是否可以使用公共API
        if (url.includes('/events/')) {
          // 如果是获取单个事件的请求，尝试通过eventsAPI.getPublicEventById获取
          console.warn('事件API请求返回401，将尝试使用公共API');
        }
        
        return Promise.reject(error);
      } else if (status === 403) {
        // 处理权限不足错误
        ElMessage.error('您没有权限执行此操作');
        console.error('权限错误 (403):', error.response.data);
      } else if (status === 404) {
        // 处理资源不存在错误
        console.error('资源不存在 (404):', error.response.data);
        
        // 对统计和报表相关的API，当404时静默处理
        if (isReportOrStatUrl(url)) {
          console.warn('报表或统计API不存在 (404)，静默处理');
          return Promise.reject(error);
        }
      } else if (status >= 500) {
        // 处理服务器错误
        // 对于报表和统计API，第一次错误时不显示全局提示
        if (!isReportOrStatUrl(url) || errorCount > 1) {
          ElMessage.error('服务器错误，请稍后重试');
        }
        console.error('服务器错误:', error.response.data);
      }
      
      // 对于报表和统计API的错误，避免全局通知，让各组件自行处理
      if (isReportOrStatUrl(url)) {
        console.warn(`报表或统计API错误 (${status})，让组件自行处理`);
      }
      
      // 处理其他错误
      console.error('API Error:', error.response.data);
    } else if (error.request) {
      console.error('No response received:', error.request);
      
      // 对于报表和统计API，不显示全局错误
      if (!isReportOrStatUrl(url)) {
        ElMessage.error('网络连接异常，请检查网络连接');
      }
    } else {
      console.error('Request error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 