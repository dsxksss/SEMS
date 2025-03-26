import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:25466/api',
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
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理统一错误
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const { status } = error.response;
      
      // 处理未授权错误
      if (status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
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