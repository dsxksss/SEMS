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

// 定义Role接口
export interface Role {
  id?: number;
  name: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  roles: (string | Role)[];
  enabled?: boolean;
  phone?: string;
  realName?: string;
  avatar?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
  refreshToken?: string;
}

export const authAPI = {
  /**
   * 用户登录
   */
  login: async (loginRequest: LoginRequest) => {
    const response = await apiClient.post<AuthResponse>('/auth/signin', loginRequest);
    return response.data;
  },

  /**
   * 用户注册
   */
  register: async (signupRequest: SignupRequest) => {
    const response = await apiClient.post('/auth/signup', signupRequest);
    return response.data;
  },

  /**
   * 刷新认证令牌
   */
  refreshToken: async (refreshToken: string) => {
    const response = await apiClient.post<{ token: string, refreshToken?: string }>('/auth/refresh', { refreshToken });
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
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }
};

export default authAPI;
