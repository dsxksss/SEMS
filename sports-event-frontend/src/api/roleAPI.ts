import apiClient from './axios';
import { ERole } from './types';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';

export interface Role {
  id: number;
  name: ERole | string;
  displayName?: string;
  description?: string;
  permissions?: string[];
  createdAt?: string;
  updatedAt?: string;
  isSystem?: boolean;
}

export interface RoleRequest {
  name: ERole | string;
  displayName?: string;
  description?: string;
  permissions?: string[];
}

// 辅助函数：检查token并在需要时尝试刷新
const ensureAuthenticated = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    console.warn('角色API请求时无有效token');
    const authStore = useAuthStore();
    if (authStore) {
      console.log('用户未认证，将重定向到登录页面');
      authStore.logout();
      return false;
    }
  }
  
  // 检查token是否过期
  const authStore = useAuthStore();
  if (authStore) {
    // 使用正确的方式检查token
    const isExpired = authStore.checkTokenExpiration();
    if (isExpired) {
      console.warn('Token已过期，需要重新登录');
      ElMessage.error('您的登录已过期，请重新登录');
      
      // 延迟执行登出操作，让用户看到提示信息
      setTimeout(() => {
        authStore.logout();
      }, 1500);
      return false;
    }
    
    // 验证token是否是有效的JWT格式
    try {
      const parts = token.split('.');
      if (parts.length === 3) {
        // token格式正确，继续执行
        return true;
      } else {
        console.warn('Token格式不正确');
        ElMessage.error('您的登录信息无效，请重新登录');
        setTimeout(() => {
          authStore.logout();
        }, 1500);
        return false;
      }
    } catch (e) {
      console.error('验证token格式时出错', e);
      return false;
    }
  }
  
  // 没有authStore时默认通过
  return true;
};

export const roleAPI = {
  /**
   * 获取所有角色
   */
  getAllRoles: async () => {
    try {
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        return [];
      }
      
      const response = await apiClient.get<Role[]>('/roles');
      // 添加系统角色标识和显示名称
      const rolesWithDetails = response.data.map(role => {
        // 枚举角色认为是系统角色
        const isSystemRole = Object.values(ERole).includes(role.name as ERole);
        const displayName = getDisplayNameForRole(role.name as string);
        
        return {
          ...role,
          isSystem: isSystemRole,
          displayName: role.displayName || displayName
        };
      });
      console.log('获取角色列表成功:', rolesWithDetails);
      return rolesWithDetails;
    } catch (error) {
      console.error('获取角色列表失败:', error);
      // 返回空数组而不是抛出错误，避免UI组件崩溃
      return [];
    }
  },

  /**
   * 获取角色详情
   */
  getRoleById: async (id: number) => {
    try {
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        throw new Error('用户未认证');
      }
      
      console.log(`获取角色 ${id} 详情...`);
      const response = await apiClient.get<Role>(`/roles/${id}`);
      console.log(`获取角色 ${id} 详情成功:`, response.data);
      
      // 添加显示名称
      const role = response.data;
      const isSystemRole = Object.values(ERole).includes(role.name as ERole);
      const displayName = getDisplayNameForRole(role.name as string);
      
      return {
        ...role,
        isSystem: isSystemRole,
        displayName: role.displayName || displayName
      };
    } catch (error) {
      console.error(`获取角色 ${id} 详情失败:`, error);
      throw error;
    }
  },

  /**
   * 创建角色
   */
  createRole: async (role: RoleRequest) => {
    try {
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        throw new Error('用户未认证');
      }
      
      console.log('创建角色:', role);
      
      // 创建角色对象
      const roleToCreate: RoleRequest = {
        name: role.name,
        displayName: role.displayName || getDisplayNameForRole(role.name as string),
        description: role.description || `${role.displayName || getDisplayNameForRole(role.name as string)}角色`,
        permissions: role.permissions
      };
      
      console.log('发送到服务器的角色数据:', roleToCreate);
      const response = await apiClient.post<Role>('/roles', roleToCreate);
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
  updateRole: async (id: number, role: Partial<RoleRequest>) => {
    try {
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        throw new Error('用户未认证');
      }
      
      console.log(`更新角色 ${id}:`, role);
      
      // 构建更新数据
      const updateData: Partial<RoleRequest> = {};
      
      // 只包含有效的数据
      if (role.name !== undefined) {
        updateData.name = role.name;
      }
      
      if (role.displayName !== undefined) {
        updateData.displayName = role.displayName;
      }
      
      if (role.description !== undefined) {
        updateData.description = role.description;
      }
      
      // 特别处理权限字段，确保即使是空数组也会被发送
      if (role.permissions !== undefined) {
        updateData.permissions = role.permissions;
      }
      
      console.log(`将发送的更新数据:`, updateData);
      
      const response = await apiClient.put<Role>(`/roles/${id}`, updateData);
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
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        throw new Error('用户未认证');
      }
      
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
    try {
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        // 如果认证失败，返回一组基本权限而不是空数组，确保UI可以正常工作
        console.warn('用户未认证，返回基本权限列表');
        return getDefaultPermissions();
      }
      
      console.log('获取所有权限...');
      const response = await apiClient.get<string[]>('/roles/permissions');
      console.log('获取权限列表成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取权限列表失败:', error);
      // 发生错误时返回默认权限，确保UI可以正常工作
      ElMessage.warning('获取权限列表失败，已加载基本权限');
      return getDefaultPermissions();
    }
  },
  
  /**
   * 为角色添加权限
   */
  addPermissionToRole: async (roleId: number, permission: string) => {
    try {
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        throw new Error('用户未认证');
      }
      
      console.log(`为角色 ${roleId} 添加权限 ${permission}`);
      const response = await apiClient.post(`/roles/${roleId}/permissions`, { permission });
      console.log('添加权限成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('添加权限失败:', error);
      throw error;
    }
  },
  
  /**
   * 从角色移除权限
   */
  removePermissionFromRole: async (roleId: number, permission: string) => {
    try {
      // 确保认证状态
      if (!(await ensureAuthenticated())) {
        throw new Error('用户未认证');
      }
      
      console.log(`从角色 ${roleId} 移除权限 ${permission}`);
      const response = await apiClient.delete(`/roles/${roleId}/permissions/${permission}`);
      console.log('移除权限成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('移除权限失败:', error);
      throw error;
    }
  }
};

/**
 * 根据角色名获取显示名称
 */
export function getDisplayNameForRole(roleName: string): string {
  switch (roleName) {
    case ERole.ROLE_ADMIN:
      return '管理员';
    case ERole.ROLE_USER:
      return '普通用户';
    case ERole.ROLE_ATHLETE:
      return '运动员';
    case ERole.ROLE_SPECTATOR:
      return '观众';
    default:
      return roleName.replace('ROLE_', '');
  }
}

// 辅助函数：返回默认权限，当API调用失败时使用
const getDefaultPermissions = () => {
  // 返回基本权限集合
  return [
    "user:read", "role:read", "event:read", 
    "category:read", "registration:read", "result:read", 
    "announcement:read", "statistics:read", "file:download"
  ];
};

export default roleAPI; 