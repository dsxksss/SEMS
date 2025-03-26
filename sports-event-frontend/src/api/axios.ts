import axios from 'axios';

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
  (error) => {
    if (error.response) {
      const { status } = error.response;
      const url = error.config.url || '';
      
      // 处理未授权错误
      if (status === 401) {
        console.error('认证错误 (401):', error.response.data);
        console.error('当前token:', localStorage.getItem('token'));
        console.error('请求URL:', error.config.url);
        console.error('请求方法:', error.config.method);
        
        // 特殊处理角色管理相关API - 不自动退出登录
        if (url.includes('/roles')) {
          console.warn('角色管理API返回401，不执行自动退出');
          return Promise.reject(error);
        }
        
        // 保存当前URL作为重定向目标
        const currentPath = window.location.pathname;
        console.log('当前路径:', currentPath);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
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