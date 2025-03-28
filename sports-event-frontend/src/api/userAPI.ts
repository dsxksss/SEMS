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

  // 获取所有用户（管理员接口）
  getAllUsers: async (): Promise<any[]> => {
    try {
      const response = await apiClient.get('/users');
      return response.data;
    } catch (error) {
      console.error('获取所有用户失败', error);
      // 添加重试逻辑
      try {
        console.log('尝试重新获取用户列表...');
        const retryResponse = await apiClient.get('/users');
        return retryResponse.data;
      } catch (retryError) {
        console.error('重试获取用户列表失败', retryError);
        return [];
      }
    }
  },

  // 创建用户（管理员接口）
  createUser: async (userData: any): Promise<any> => {
    const response = await apiClient.post('/users', userData);
    return response.data;
  },

  // 更新用户（管理员接口）
  updateUser: async (userId: number, userData: any): Promise<any> => {
    const response = await apiClient.put(`/users/${userId}`, userData);
    return response.data;
  },

  // 删除用户（管理员接口）
  deleteUser: async (userId: number): Promise<any> => {
    const response = await apiClient.delete(`/users/${userId}`);
    return response.data;
  },

  // 切换用户状态（管理员接口）
  toggleUserStatus: async (userId: number, enabled: boolean): Promise<any> => {
    const response = await apiClient.put(`/users/${userId}/status`, { enabled });
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