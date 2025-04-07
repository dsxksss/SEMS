import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';
import authAPI from './authAPI';

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
    __isRefreshing?: boolean;
    __refreshSubscribers?: ((token: string) => void)[];
  }
}

// 初始化刷新令牌相关的全局变量
window.__isRefreshing = false;
window.__refreshSubscribers = [];

// 订阅令牌刷新
const subscribeTokenRefresh = (cb: (token: string) => void) => {
  window.__refreshSubscribers?.push(cb);
};

// 当令牌刷新完成，通知所有订阅者
const onRefreshed = (token: string) => {
  window.__refreshSubscribers?.forEach(cb => cb(token));
  window.__refreshSubscribers = [];
};

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

// 解析JWT令牌，获取过期时间
const getTokenExpirationDate = (token: string): Date | null => {
  try {
    const decoded = JSON.parse(atob(token.split('.')[1]));
    if (decoded.exp === undefined) {
      return null;
    }
    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  } catch (error) {
    return null;
  }
};

// 检查令牌是否即将过期（在5分钟内）
const isTokenExpiringSoon = (token: string): boolean => {
  const expDate = getTokenExpirationDate(token);
  if (!expDate) return false;
  
  // 如果过期时间在5分钟内，认为即将过期
  return (expDate.valueOf() - Date.now()) < 5 * 60 * 1000;
};

// 请求拦截器 - 为所有请求添加认证头，并处理即将过期的令牌
apiClient.interceptors.request.use(
  async (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      // 检查令牌是否即将过期
      if (isTokenExpiringSoon(token) && !config.url?.includes('/auth/')) {
        // 如果令牌即将过期且不是认证请求，尝试刷新令牌
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken && !window.__isRefreshing) {
          window.__isRefreshing = true;
          
          try {
            // 刷新令牌
            const response = await authAPI.refreshToken(refreshToken);
            window.__isRefreshing = false;
            
            // 使用新令牌
            config.headers.Authorization = `Bearer ${response.accessToken}`;
            onRefreshed(response.accessToken);
          } catch (error) {
            window.__isRefreshing = false;
            console.error('自动刷新令牌失败:', error);
          }
        } else if (window.__isRefreshing) {
          // 如果正在刷新令牌，等待刷新完成
          await new Promise<void>((resolve) => {
            subscribeTokenRefresh((newToken) => {
              config.headers.Authorization = `Bearer ${newToken}`;
              resolve();
            });
          });
        }
      } else {
        // 使用现有令牌
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    // 添加防止缓存的参数
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

// 响应拦截器 - 处理统一错误和令牌刷新
apiClient.interceptors.response.use(
  (response) => {
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
    
    // 检查是否为超时错误或网络错误
    const isTimeoutError = error.code === 'ECONNABORTED' && error.message && error.message.includes('timeout');
    const isNetworkError = !error.response && error.message === 'Network Error';
    
    // 处理超时或网络错误的重试
    if ((isTimeoutError || isNetworkError) && originalRequest && originalRequest._retryCount < MAX_RETRIES) {
      originalRequest._retryCount++;
      const delay = originalRequest._retryCount * 1000;
      await new Promise(resolve => setTimeout(resolve, delay));
      return apiClient(originalRequest);
    }
    
    if (error.response) {
      const { status } = error.response;
      
      // 处理 401 未授权错误
      if (status === 401) {
        // 对于认证相关请求，直接返回错误
        if (url.includes('/auth/signin') || url.includes('/auth/refresh')) {
          return Promise.reject(error);
        }
        
        // 如果是令牌过期，尝试刷新令牌
        if (!originalRequest._retry) {
          originalRequest._retry = true;
          
          if (!window.__isRefreshing) {
            window.__isRefreshing = true;
            
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
              try {
                const response = await authAPI.refreshToken(refreshToken);
                window.__isRefreshing = false;
                
                // 通知所有订阅者
                onRefreshed(response.accessToken);
                
                // 使用新令牌重新请求
                originalRequest.headers.Authorization = `Bearer ${response.accessToken}`;
                return apiClient(originalRequest);
              } catch (refreshError) {
                window.__isRefreshing = false;
                console.error('刷新令牌失败，将重定向到登录页:', refreshError);
                
                // 清除认证信息并重定向到登录页
                if (!window.__isShowingExpiredMessage) {
                  window.__isShowingExpiredMessage = true;
                  
                  ElMessage.error('会话已过期，请重新登录');
                  setTimeout(() => {
                    const authStore = useAuthStore();
                    authStore.logout();
                    
                    // 保存当前页面路径
                    const currentPath = window.location.pathname + window.location.search;
                    localStorage.setItem('redirectPath', currentPath);
                    
                    window.__isShowingExpiredMessage = false;
                    window.location.href = '/login';
                  }, 1500);
                }
                
                return Promise.reject(refreshError);
              }
            } else {
              window.__isRefreshing = false;
              // 没有刷新令牌，直接登出
              const authStore = useAuthStore();
              authStore.logout();
              window.location.href = '/login';
            }
          } else {
            // 等待令牌刷新
            return new Promise((resolve) => {
              subscribeTokenRefresh((newToken) => {
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                resolve(apiClient(originalRequest));
              });
            });
          }
        }
      } else if (status === 403) {
        // 处理权限不足错误
        ElMessage.error('您没有权限执行此操作');
      } else if (status === 404) {
        // 处理资源不存在错误
        if (!isReportOrStatUrl(url)) {
          ElMessage.error('请求的资源不存在');
        }
      } else if (status >= 500) {
        // 处理服务器错误
        if (!isReportOrStatUrl(url) || errorCount > 1) {
          ElMessage.error('服务器错误，请稍后重试');
        }
      }
    } else if (error.request) {
      // 处理请求发送但没有收到响应的情况
      if (!isReportOrStatUrl(url)) {
        ElMessage.error('网络连接异常，请检查网络连接');
      }
    } else {
      // 处理请求设置时出错的情况
      console.error('请求错误:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 