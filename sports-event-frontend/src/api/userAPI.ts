import apiClient from './axios';

export interface User {
  id: number;
  username: string;
  realName?: string;
  email?: string;
  phone?: string;
  avatar?: string;
  role: string;
  registrationCount?: number;
  createdAt: string;
  lastLoginAt?: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface UpdateUserRequest {
  realName?: string;
  email?: string;
  phone?: string;
}

export interface BindPhoneRequest {
  phone: string;
  verificationCode: string;
}

export const userAPI = {
  // 获取当前登录用户
  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get('/users/me');
    return response.data;
  },

  // 更新当前用户信息
  updateCurrentUser: async (userData: UpdateUserRequest): Promise<User> => {
    const response = await apiClient.put('/users/me', userData);
    return response.data;
  },

  // 修改密码
  changePassword: async (data: ChangePasswordRequest): Promise<any> => {
    const response = await apiClient.post('/users/change-password', data);
    return response.data;
  },

  // 发送验证码
  sendVerificationCode: async (phone: string): Promise<any> => {
    const response = await apiClient.post('/users/send-verification-code', { phone });
    return response.data;
  },

  // 绑定手机号
  bindPhone: async (data: BindPhoneRequest): Promise<any> => {
    const response = await apiClient.post('/users/bind-phone', data);
    return response.data;
  },

  // 上传头像
  uploadAvatar: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await apiClient.post('/users/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    return response.data.url;
  },

  // 获取用户活动日志
  getUserActivities: async (limit: number = 5): Promise<any[]> => {
    const response = await apiClient.get(`/users/me/activities?limit=${limit}`);
    return response.data;
  },

  // 获取用户数据统计
  getUserStats: async (): Promise<any> => {
    const response = await apiClient.get('/users/me/stats');
    return response.data;
  }
};

export default userAPI;