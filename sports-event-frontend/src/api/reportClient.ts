import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

// 扩展Axios请求配置类型
declare module 'axios' {
  export interface InternalAxiosRequestConfig {
    _retryCount?: number;
    _retry?: boolean;
  }
}

/**
 * 报表客户端
 * 专门用于处理报表和统计相关的API请求
 * 特点：
 * 1. 更长的超时时间
 * 2. 更多的重试次数
 * 3. 静默处理部分错误
 * 4. 特殊的错误处理逻辑
 */
const reportClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000 // 更长的超时时间，报表生成可能需要更多时间
});

// 重试配置
const MAX_RETRIES = 3;  // 比普通请求更多的重试次数

// 请求拦截器 - 为所有请求添加认证头
reportClient.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      try {
        // 尝试解析token并验证格式
        const parts = token.split('.');
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]));
          const expTime = payload.exp * 1000; // 转换为毫秒
          const nowTime = Date.now();
          
          // 只有token未过期才添加到请求头
          if (expTime > nowTime) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log(`报表请求: ${config.method?.toUpperCase()} ${config.url}`);
          } else {
            console.warn('报表请求: token已过期，不添加认证头');
          }
        } else {
          console.warn('报表请求: token格式不正确，不添加认证头');
        }
      } catch (e) {
        console.error('报表请求: 解析token出错，不添加认证头', e);
      }
    } else {
      console.warn('报表请求: 未找到token');
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
    console.error('报表请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 特殊处理报表相关错误
reportClient.interceptors.response.use(
  (response) => {
    console.log(`报表响应成功: ${response.config.method?.toUpperCase()} ${response.config.url}`);
    return response;
  },
  async (error) => {
    // 存储原始请求的配置
    const originalRequest = error.config;
    const url = originalRequest?.url || '';
    
    // 检查是否为超时错误
    const isTimeoutError = error.code === 'ECONNABORTED' && error.message && error.message.includes('timeout');
    
    // 检查是否为网络错误
    const isNetworkError = !error.response && error.message === 'Network Error';
    
    // 检查是否要重试请求
    if ((isTimeoutError || isNetworkError) && originalRequest && originalRequest._retryCount < MAX_RETRIES) {
      originalRequest._retryCount++;
      console.log(`报表请求超时或网络错误，正在重试第 ${originalRequest._retryCount} 次: ${url}`);
      
      // 网络错误更频繁时增加延迟
      const delay = originalRequest._retryCount * 1500; // 每次多等1.5秒
      await new Promise(resolve => setTimeout(resolve, delay));
      
      return reportClient(originalRequest);
    }
    
    if (error.response) {
      const { status } = error.response;
      
      // 特殊处理各种HTTP状态码
      if (status === 401) {
        console.warn('报表API认证错误 (401)，返回空数据');
        // 对于报表API，返回空数据但不登出用户
        return Promise.reject({ 
          ...error, 
          isReportError: true,
          message: '认证失败，请重新登录后再试' 
        });
      } else if (status === 403) {
        console.warn('报表API权限错误 (403)，返回空数据');
        return Promise.reject({ 
          ...error, 
          isReportError: true,
          message: '您没有权限生成此报表' 
        });
      } else if (status === 404) {
        console.warn('报表API不存在 (404)，返回空数据');
        return Promise.reject({ 
          ...error, 
          isReportError: true,
          message: '请求的报表功能不存在' 
        });
      } else if (status >= 500) {
        console.error('报表API服务器错误:', error.response.data);
        return Promise.reject({ 
          ...error, 
          isReportError: true,
          message: '服务器处理报表时出错，请稍后重试' 
        });
      }
      
      console.error('报表API错误:', error.response.data);
      return Promise.reject({ 
        ...error, 
        isReportError: true,
        message: '生成报表时出错' 
      });
    } else if (error.request) {
      console.error('报表API无响应:', error.request);
      return Promise.reject({ 
        ...error, 
        isReportError: true,
        message: '网络连接异常，请检查网络连接后重试' 
      });
    } else {
      console.error('报表API请求错误:', error.message);
      return Promise.reject({ 
        ...error, 
        isReportError: true,
        message: '报表请求出错' 
      });
    }
  }
);

export default reportClient; 