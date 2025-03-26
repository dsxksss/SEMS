import apiClient from './axios';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  roles?: string[];
}

export interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export const authAPI = {
  /**
   * 用户登录
   */
  login: async (loginRequest: LoginRequest) => {
    const response = await apiClient.post<AuthResponse>('/api/auth/signin', loginRequest);
    return response.data;
  },

  /**
   * 用户注册
   */
  register: async (signupRequest: SignupRequest) => {
    const response = await apiClient.post('/api/auth/signup', signupRequest);
    return response.data;
  },

  /**
   * 检查登录状态
   */
  checkAuthStatus: (): boolean => {
    const token = localStorage.getItem('token');
    return !!token;
  },

  /**
   * 退出登录
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }
};

export default authAPI;
