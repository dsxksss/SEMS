import apiClient from './axios';
import type { User } from './authAPI';
import { authAPI } from './authAPI';

// 定义Role接口
export interface Role {
  id?: number;
  name: string;
}

// 扩展User类型，添加status字段
export interface ExtendedUser extends Omit<User, 'roles'> {
  status?: string; // 前端展示用
  enabled?: boolean; // 与后端对应
  createdAt?: string;
  avatar?: string; // 用户头像URL
  roles: (string | Role)[];
}

export const userAPI = {
  /**
   * 获取当前用户信息
   */
  getCurrentUser: async () => {
    const response = await apiClient.get<User>('/users/me');
    return response.data;
  },

  /**
   * 更新当前用户信息
   */
  updateCurrentUser: async (userData: Partial<User>) => {
    const response = await apiClient.put<User>('/users/me', userData);
    return response.data;
  },

  /**
   * 更新当前用户头像
   */
  updateAvatar: async (avatarUrl: string) => {
    const response = await apiClient.put('/users/me/avatar', { avatarUrl });
    return response.data;
  },

  /**
   * 上传用户头像
   */
  uploadAvatar: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    // 获取上传后的文件名，然后更新用户头像
    if (response.data && response.data.filename) {
      const avatarUrl = `/api/files/download/${response.data.filename}`;
      await userAPI.updateCurrentUser({ avatar: avatarUrl });
      return avatarUrl;
    }
    
    return response.data;
  },

  /**
   * 更改密码
   */
  changePassword: async (currentPassword: string, newPassword: string) => {
    const response = await apiClient.put('/users/me/password', {
      currentPassword,
      newPassword
    });
    return response.data;
  },

  /**
   * 管理员: 获取所有用户
   */
  getAllUsers: async () => {
    const response = await apiClient.get<ExtendedUser[]>('/users');
    return response.data;
  },

  /**
   * 管理员: 获取用户详情
   */
  getUserById: async (id: number) => {
    const response = await apiClient.get<ExtendedUser>(`/users/${id}`);
    return response.data;
  },

  /**
   * 管理员: 创建用户
   */
  createUser: async (userData: {
    username: string;
    email: string;
    password: string;
    roles: string[];
    status: string;
  }) => {
    // 将前端的roles数组格式转换为后端需要的格式
    const roleNames = userData.roles.map(role => {
      // 从'ROLE_ADMIN'格式转换为'admin'格式
      return role.replace('ROLE_', '').toLowerCase();
    });
    
    // 使用authAPI的register方法创建用户
    const response = await authAPI.register({
      username: userData.username,
      email: userData.email,
      password: userData.password,
      roles: roleNames
    });
    
    // 如果成功创建用户并需要设置状态，进行额外的API调用
    if (response && response.id && userData.status) {
      try {
        await apiClient.put(`/users/${response.id}/status`, {
          enabled: userData.status === 'active'
        });
      } catch (error) {
        console.error('设置用户状态失败', error);
      }
    }
    
    return response;
  },

  /**
   * 管理员: 更新用户信息
   */
  updateUser: async (id: number, userData: Partial<ExtendedUser>) => {
    // 处理角色数据，如果存在则转换格式
    let updateData = {...userData};
    
    if (userData.roles && userData.roles.length > 0) {
      // 将字符串角色名称转换为Role对象格式
      updateData.roles = userData.roles.map(role => {
        // 如果已经是对象格式就保留
        if (typeof role === 'object' && role !== null && 'name' in role) {
          return role;
        }
        // 否则转换为对象格式
        return {
          name: role as string
        };
      });
      
      console.log('转换后的角色数据:', updateData.roles);
    } else if (userData.roles && userData.roles.length === 0) {
      // 如果是空数组，需要明确设置为空，便于后端清除所有角色
      updateData.roles = [];
      console.log('用户角色将被清空');
    }
    
    // 将前端的status转换为后端的enabled
    if (userData.status !== undefined) {
      updateData.enabled = userData.status === 'active';
      delete updateData.status;
    }
    
    console.log('发送给后端的用户数据:', updateData);
    
    const response = await apiClient.put<ExtendedUser>(`/users/${id}`, updateData);
    return response.data;
  },

  /**
   * 管理员: 切换用户状态（启用/禁用）
   */
  toggleUserStatus: async (id: number, enabled: boolean) => {
    const response = await apiClient.put(`/users/${id}/status`, { enabled });
    return response.data;
  },

  /**
   * 管理员: 删除用户
   */
  deleteUser: async (id: number) => {
    const response = await apiClient.delete(`/users/${id}`);
    return response.data;
  }
};

export default userAPI;