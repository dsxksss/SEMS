import apiClient from './axios';

export interface Role {
  id: number;
  name: string;
  displayName: string;
  description: string;
  permissions: string[];
  createdAt: string;
  updatedAt?: string;
  isSystem?: boolean;
}

export const roleAPI = {
  /**
   * 获取所有角色
   */
  getAllRoles: async () => {
    const response = await apiClient.get<Role[]>('/roles');
    return response.data;
  },

  /**
   * 获取角色详情
   */
  getRoleById: async (id: number) => {
    try {
      console.log(`获取角色 ${id} 详情...`);
      const response = await apiClient.get<Role>(`/roles/${id}`);
      console.log(`获取角色 ${id} 详情成功:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`获取角色 ${id} 详情失败:`, error);
      throw error;
    }
  },

  /**
   * 创建角色
   */
  createRole: async (role: Omit<Role, 'id' | 'createdAt' | 'updatedAt'>) => {
    try {
      console.log('创建角色:', role);
      const response = await apiClient.post<Role>('/roles', role);
      console.log('创建角色成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('创建角色失败:', error);
      throw error;
    }
  },

  /**
   * 更新角色
   */
  updateRole: async (id: number, role: Partial<Role>) => {
    try {
      console.log(`更新角色 ${id}:`, role);
      const response = await apiClient.put<Role>(`/roles/${id}`, role);
      console.log(`更新角色 ${id} 成功:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`更新角色 ${id} 失败:`, error);
      throw error;
    }
  },

  /**
   * 删除角色
   */
  deleteRole: async (id: number) => {
    try {
      console.log(`删除角色 ${id}...`);
      const response = await apiClient.delete(`/roles/${id}`);
      console.log(`删除角色 ${id} 成功:`, response.data);
      return response.data;
    } catch (error) {
      console.error(`删除角色 ${id} 失败:`, error);
      throw error;
    }
  },

  /**
   * 获取所有权限
   */
  getAllPermissions: async () => {
    const response = await apiClient.get<string[]>('/roles/permissions');
    console.log('获取权限列表成功:', response.data);
    return response.data;
  }
};

export default roleAPI; 