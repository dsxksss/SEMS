import apiClient from './axios';
import type { EventCategory } from '@/types/event';

export const categoryAPI = {
  /**
   * 获取公开类别列表
   */
  getPublicCategories: async () => {
    const response = await apiClient.get<EventCategory[]>('/categories/public');
    return response.data;
  },

  /**
   * 管理员: 获取所有类别
   */
  getAllCategories: async () => {
    const response = await apiClient.get<EventCategory[]>('/categories');
    return response.data;
  },

  /**
   * 获取类别详情
   */
  getCategoryById: async (id: number) => {
    const response = await apiClient.get<EventCategory>(`/categories/${id}`);
    return response.data;
  },

  /**
   * 管理员: 创建类别
   */
  createCategory: async (category: Omit<EventCategory, 'id'>) => {
    const response = await apiClient.post<EventCategory>('/categories', category);
    return response.data;
  },

  /**
   * 管理员: 更新类别
   */
  updateCategory: async (id: number, category: Partial<EventCategory>) => {
    const response = await apiClient.put<EventCategory>(`/categories/${id}`, category);
    return response.data;
  },

  /**
   * 管理员: 删除类别
   */
  deleteCategory: async (id: number) => {
    const response = await apiClient.delete(`/categories/${id}`);
    return response.data;
  }
};

export default categoryAPI;
