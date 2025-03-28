import apiClient from './axios';
import { useAuthStore } from '../stores/auth';

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

// 用于管理员页面的扩展用户接口
export interface ExtendedUser extends User {
  // 可能包含额外字段
  lastLogin?: string;
  bio?: string;
}

// 由于后端没有活动记录API，这里保留接口定义供模拟数据使用
export interface Activity {
  id: number;
  type: string;
  description: string;
  event?: {
    id: number;
    name: string;
  };
  createdAt: string;
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

export interface AvatarRequest {
  avatarUrl: string;
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
  changePassword: async (currentPassword: string, newPassword: string): Promise<any> => {
    const response = await apiClient.put('/users/me/password', {
      newPassword: newPassword
    });
    return response.data;
  },

  // 上传头像
  uploadAvatar: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    
    // 上传文件 - 修正路径，不要重复/api前缀
    const uploadResponse = await apiClient.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    // 获取文件名
    const filename = uploadResponse.data.filename;
    console.log("获取到的文件名:", filename);
    
    // 使用完整的URL路径，确保前端能够正确访问
    const fileUrl = `/api/files/download/${filename}`;
    console.log("生成的头像URL:", fileUrl);
    
    // 更新用户头像
    const avatarRequest: AvatarRequest = {
      avatarUrl: fileUrl
    };
    const response = await apiClient.put('/users/me/avatar', avatarRequest);
    console.log("头像更新响应:", response.data);
    
    // 获取最新的用户信息并更新本地存储
    try {
      const userData = await apiClient.get('/users/me');
      if (userData.data) {
        localStorage.setItem('user', JSON.stringify(userData.data));
      }
    } catch (error) {
      console.error("获取最新用户信息失败:", error);
    }
    
    return fileUrl;
  },

  // 更新头像 (单独的方法，针对已有URL)
  updateAvatar: async (avatarUrl: string): Promise<any> => {
    const response = await apiClient.put('/users/me/avatar', {
      avatarUrl: avatarUrl
    });
    return response.data;
  },

  // 发送验证码 (模拟)
  sendVerificationCode: async (phone: string): Promise<any> => {
    // 模拟API调用
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, message: '验证码已发送' });
      }, 500);
    });
  },

  // 绑定手机号 (模拟)
  bindPhone: async (data: BindPhoneRequest): Promise<any> => {
    // 模拟API调用
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, message: '手机绑定成功' });
      }, 500);
    });
  }
};

export default userAPI;