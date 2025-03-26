import apiClient from './axios';
import type { EventCategory } from '@/types/event';

export const categoryAPI = {
  /**
   * 获取公开类别列表
   */
  getPublicCategories: async () => {
    try {
      console.log('获取公开类别列表...');
      const response = await apiClient.get<EventCategory[]>('/api/categories/public');
      console.log('获取公开类别列表成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取公开类别列表失败:', error);
      throw error;
    }
  },

  /**
   * 管理员: 获取所有类别
   */
  getAllCategories: async () => {
    try {
      console.log('获取所有类别列表...');
      const response = await apiClient.get<EventCategory[]>('/api/categories');
      console.log('获取所有类别列表成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取所有类别列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取类别详情
   */
  getCategoryById: async (id: number) => {
    try {
      console.log(`获取类别 ${id} 详情...`);
      const response = await apiClient.get<EventCategory>(`/api/categories/${id}`);
      console.log(`获取类别 ${id} 详情成功:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`获取类别 ${id} 详情失败:`, error);
      throw error;
    }
  },

  /**
   * 管理员: 创建类别
   */
  createCategory: async (category: Omit<EventCategory, 'id'>) => {
    try {
      console.log('创建类别...');
      const response = await apiClient.post<EventCategory>('/api/categories', category);
      console.log('创建类别成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('创建类别失败:', error);
      throw error;
    }
  },

  /**
   * 管理员: 更新类别
   */
  updateCategory: async (id: number, category: Partial<EventCategory>) => {
    try {
      console.log(`更新类别 ${id}...`);
      const response = await apiClient.put<EventCategory>(`/api/categories/${id}`, category);
      console.log(`更新类别 ${id} 成功:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`更新类别 ${id} 失败:`, error);
      throw error;
    }
  },

  /**
   * 管理员: 删除类别
   */
  deleteCategory: async (id: number) => {
    try {
      console.log(`删除类别 ${id}...`);
      const response = await apiClient.delete(`/api/categories/${id}`);
      console.log(`删除类别 ${id} 成功:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`删除类别 ${id} 失败:`, error);
      throw error;
    }
  }
};

export default categoryAPI;
