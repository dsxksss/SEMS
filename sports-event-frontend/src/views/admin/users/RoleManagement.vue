<template>
  <div class="role-management">
    <div class="main-container">
      <div class="header">
        <h3>角色管理</h3>
      </div>

      <el-table
        v-loading="loading"
        :data="roleList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色标识" width="180">
          <template #default="scope">
            {{ scope.row.name }}
            <el-tag v-if="isCustomRole(scope.row)" size="small" type="warning" effect="plain">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="displayName" label="角色名称" width="180">
          <template #default="scope">
            {{ scope.row.displayName || getDisplayNameForRole(scope.row.name) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="系统角色" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isSystem" type="info">是</el-tag>
            <el-tag v-else type="success">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
              >编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑角色对话框 -->
    <el-dialog
      title="编辑角色"
      v-model="dialogVisible"
      width="600px"
      @closed="handleDialogClose"
    >
      <el-form
        :model="roleForm"
        :rules="roleRules"
        ref="roleFormRef"
        label-width="100px"
      >
        <el-form-item label="角色标识" prop="name">
          <el-input 
            v-model="roleForm.name" 
            disabled
          />
        </el-form-item>
        <el-form-item label="角色名称" prop="displayName">
          <el-input v-model="roleForm.displayName" placeholder="请输入角色名称" :disabled="isSystemRole(roleForm)" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            placeholder="请输入角色描述"
            rows="3"
            :disabled="isSystemRole(roleForm)"
          />
        </el-form-item>
        
        <!-- 权限选择 -->
        <el-form-item label="权限" prop="permissions">
          <div v-if="permissionsLoading" class="permissions-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载权限中...</span>
          </div>
          <div v-else>
            <div v-if="permissionGroups.length === 0" class="no-permissions">
              暂无可用权限
            </div>
            <div v-else class="permissions-container">
              <div v-for="(group, index) in permissionGroups" :key="index" class="permission-group">
                <div class="group-title">{{ group.title }}</div>
                <div class="group-content">
                  <el-checkbox
                    v-for="perm in group.permissions"
                    :key="perm.key"
                    v-model="roleForm.permissions"
                    :label="perm.key"
                  >
                    {{ perm.label }}
                  </el-checkbox>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRole">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { roleAPI } from '../../../api';
import type { Role } from '../../../api/roleAPI';
import { ERole } from '../../../api/types';
import dayjs from 'dayjs';
import { Loading } from '@element-plus/icons-vue';
import { useAuthStore } from '../../../stores/auth';
import { getDisplayNameForRole } from '../../../api/roleAPI';

// 角色列表数据
const roleList = ref<Role[]>([]);
const loading = ref(false);

// 对话框相关
const dialogVisible = ref(false);
const isEdit = ref(true); // 始终处于编辑模式
const roleFormRef = ref(null);
const roleForm = reactive<Role>({
  id: 0,
  name: '',
  displayName: '',
  description: '',
  permissions: [],
  createdAt: ''
});

// 权限相关
const permissionsLoading = ref(false);
const allPermissions = ref<string[]>([]);
const permissionGroups = ref<{ title: string; permissions: { key: string; label: string; available: boolean }[] }[]>([]);

// 判断是否为自定义角色
const isCustomRole = (role: Role) => {
  return !Object.values(ERole).includes(role.name as ERole);
};

// 表单验证规则
const roleRules = {
  displayName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ]
};

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-';
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
};

// 获取权限组的中文标题
const getPermissionGroupTitle = (key: string) => {
  // 将英文权限组名转换为中文
  const groupTitleMap: Record<string, string> = {
    'user': '用户',
    'role': '角色',
    'event': '赛事',
    'category': '分类',
    'registration': '报名',
    'result': '成绩',
    'announcement': '公告',
    'statistics': '统计',
    'file': '文件'
  };
  
  return groupTitleMap[key.toLowerCase()] || key;
};

// 获取所有权限
const fetchAllPermissions = async () => {
  permissionsLoading.value = true;
  try {
    const permissions = await roleAPI.getAllPermissions();
    allPermissions.value = permissions || [];
    
    // 对权限进行分组
    const groups: Record<string, { key: string; label: string; available: boolean }[]> = {};
    
    allPermissions.value.forEach(perm => {
      const parts = perm.split(':');
      const group = parts[0] || '其他';
      const label = parts.length > 1 ? parts.slice(1).join(':') : perm;
      
      if (!groups[group]) {
        groups[group] = [];
      }
      
      groups[group].push({
        key: perm,
        label: label,
        available: true
      });
    });
    
    // 转换为组件需要的格式
    permissionGroups.value = Object.keys(groups).map(key => ({
      title: getPermissionGroupTitle(key),
      permissions: groups[key]
    }));
    
  } catch (error) {
    console.error('获取权限列表失败', error);
    ElMessage.error('获取权限列表失败，请稍后重试');
  } finally {
    permissionsLoading.value = false;
  }
};

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true;
  try {
    // 调用API获取角色列表
    const roles = await roleAPI.getAllRoles();
    roleList.value = roles;
  } catch (error) {
    console.error('获取角色列表失败', error);
    ElMessage.error('获取角色列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 编辑角色
const handleEdit = async (row: Role) => {
  try {
    // 获取角色详情，包括权限
    const roleDetail = await roleAPI.getRoleById(row.id);
    Object.assign(roleForm, roleDetail);
    
    // 如果没有权限数组，初始化为空数组
    if (!roleForm.permissions) {
      roleForm.permissions = [];
    }
    
    dialogVisible.value = true;
  } catch (error) {
    console.error('获取角色详情失败', error);
    ElMessage.error('获取角色详情失败，请稍后重试');
  }
};

// 判断是否为系统内置角色
const isSystemRole = (role: Role) => {
  // 管理员和预设核心角色无法修改角色信息，但可以修改权限
  const coreRoles = [ERole.ROLE_ADMIN, ERole.ROLE_ATHLETE, ERole.ROLE_SPECTATOR, ERole.ROLE_USER];
  return role && role.name && coreRoles.includes(role.name as ERole);
};

// 保存角色
const saveRole = async () => {
  const formEl = roleFormRef.value as any;
  if (!formEl) return;
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 显示加载中状态
        const loadingInstance = ElLoading.service({
          lock: true,
          text: '正在更新角色...',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        try {
          // 确保displayName有值
          if (!roleForm.displayName && !isSystemRole(roleForm)) {
            // 对于非系统角色，如果未提供，使用内置的显示名称
            roleForm.displayName = getDisplayNameForRole(roleForm.name as string);
          }
          
          console.log('准备保存角色:', roleForm);
          
          // 对于系统角色，只更新权限
          if (isSystemRole(roleForm)) {
            console.log('更新系统角色，只处理权限:', roleForm.permissions);
            
            // 更新角色基本信息，名称和描述对系统角色不可修改
            await roleAPI.updateRole(roleForm.id, {
              permissions: roleForm.permissions
            });
          } else {
            // 对于非系统角色，可以更新所有字段
            await roleAPI.updateRole(roleForm.id, {
              name: roleForm.name,
              displayName: roleForm.displayName,
              description: roleForm.description,
              permissions: roleForm.permissions
            });
          }
          
          ElMessage.success('更新角色成功');
          dialogVisible.value = false;
          fetchRoleList(); // 刷新角色列表
        } catch (error: any) {
          console.error('保存角色失败', error);
          
          // 错误处理逻辑
          let errorMessage = '保存角色失败，请重试';
          
          // 检查是否是401错误
          if (error.response && error.response.status === 401) {
            errorMessage = '您的登录会话已过期，请重新登录';
            
            // 检查token是否已经过期
            const authStore = useAuthStore();
            if (authStore && authStore.checkTokenExpiration()) {
              // 延迟执行登出操作，让用户看到提示信息
              setTimeout(() => {
                authStore.logout();
              }, 1500);
            }
          } 
          // 检查是否是400错误(请求错误)
          else if (error.response && error.response.status === 400) {
            if (error.response.data && error.response.data.message) {
              // 直接使用服务器返回的错误消息
              errorMessage = error.response.data.message;
            }
          }
          // 其他错误
          else if (error.response && error.response.data && error.response.data.message) {
            // 服务器返回的具体错误信息
            errorMessage = error.response.data.message;
          }
          
          ElMessage.error(errorMessage);
        } finally {
          // 关闭加载中状态
          loadingInstance.close();
        }
      } catch (e) {
        ElMessage.error('界面出错，请重试');
      }
    } else {
      return false;
    }
  });
};

// 监听对话框关闭事件，重置表单
const handleDialogClose = () => {
  if (roleFormRef.value) {
    (roleFormRef.value as any).resetFields();
  }
};

// 初始化加载
onMounted(() => {
  fetchRoleList();
  fetchAllPermissions();
});
</script>

<style scoped>
.role-management {
  width: 100%;
}

.main-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.permissions-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.permission-group {
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.group-title {
  background-color: #f5f7fa;
  padding: 8px 15px;
  font-weight: 500;
  color: #606266;
}

.group-content {
  padding: 10px 15px;
  display: flex;
  flex-wrap: wrap;
}

.group-content .el-checkbox {
  margin-right: 15px;
  margin-bottom: 5px;
}

.permissions-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
}

.no-permissions {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 