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
  refreshToken: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export const authAPI = {
  /**
   * 用户登录
   */
  login: async (loginRequest: LoginRequest) => {
    const response = await apiClient.post<AuthResponse>('/auth/signin', loginRequest);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      localStorage.setItem('user', JSON.stringify({
        id: response.data.id,
        username: response.data.username,
        email: response.data.email,
        roles: response.data.roles
      }));
    }
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
    try {
      const response = await apiClient.post<RefreshTokenResponse>('/auth/refresh', { refreshToken });
      if (response.data.accessToken) {
        localStorage.setItem('token', response.data.accessToken);
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken);
        }
      }
      return response.data;
    } catch (error) {
      console.error('刷新令牌失败:', error);
      authAPI.logout();
      throw error;
    }
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
  logout: async () => {
    try {
      // 如果用户已登录，调用退出接口
      const token = localStorage.getItem('token');
      if (token) {
        await apiClient.post('/auth/logout');
      }
    } catch (error) {
      console.error('退出时发生错误:', error);
    } finally {
      // 无论接口调用成功与否，都清除本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  }
};

export default authAPI;
