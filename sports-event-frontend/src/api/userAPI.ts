import apiClient from './axios';
import { User } from './authAPI';

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
    const response = await apiClient.get<User[]>('/users');
    return response.data;
  },

  /**
   * 管理员: 获取用户详情
   */
  getUserById: async (id: number) => {
    const response = await apiClient.get<User>(`/users/${id}`);
    return response.data;
  },

  /**
   * 管理员: 更新用户信息
   */
  updateUser: async (id: number, userData: Partial<User>) => {
    const response = await apiClient.put<User>(`/users/${id}`, userData);
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