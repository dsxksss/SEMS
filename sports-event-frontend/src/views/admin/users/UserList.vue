<template>
  <div class="user-list">
    <div class="filter-container">
      <el-form :inline="true" :model="filterForm" class="form-inline">
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="输入用户名搜索" clearable class="filter-input" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filterForm.role" placeholder="选择角色" clearable class="filter-select">
            <el-option 
              v-for="role in roleList" 
              :key="role.id" 
              :label="role.displayName || formatRoleName(role.name)" 
              :value="role.name" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable class="filter-select">
            <el-option label="激活" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUserList">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3>用户列表</h3>
        <el-button type="primary" @click="handleAddUser">添加用户</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="userList"
        border
        style="width: 100%"
        fit
        class="user-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="roles" label="角色" min-width="200">
          <template #default="scope">
            <el-tag
              v-for="role in scope.row.roles"
              :key="role"
              :type="getRoleTagType(role)"
              class="role-tag"
            >
              {{ formatRoleName(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '激活' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="220">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
            >编辑</el-button>
            <el-button
              size="small"
              :type="scope.row.status === 'active' ? 'danger' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >{{ scope.row.status === 'active' ? '禁用' : '启用' }}</el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        :model="userForm"
        :rules="userRules"
        ref="userFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" id="username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" id="email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            id="password"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <div v-if="loadingRoles" class="loading-roles">
            <el-icon class="is-loading"><Loading /></el-icon> 加载角色中...
          </div>
          <el-checkbox-group v-else v-model="userForm.roles" id="roles">
            <el-checkbox v-for="role in roleList" :key="role.id" :label="role.name">
              {{ role.displayName || formatRoleName(role.name) }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status" id="status">
            <el-radio :label="'active'" value="active">激活</el-radio>
            <el-radio :label="'disabled'" value="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userAPI } from '../../../api/userAPI';
import type { User } from '../../../api/authAPI';
import { roleAPI, type Role as RoleType } from '../../../api/roleAPI';
import dayjs from 'dayjs';

// 扩展User类型，添加status和createdAt字段
interface ExtendedUser extends User {
  status: 'active' | 'disabled';
  createdAt?: string;
  createTime?: string;
}

// 定义列表过滤表单
const filterForm = reactive({
  username: '',
  role: '',
  status: ''
});

// 用户列表数据
const userList = ref<ExtendedUser[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// 角色列表数据
const roleList = ref<RoleType[]>([]);
const loadingRoles = ref(false);

// 对话框相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const userFormRef = ref(null);
const userForm = reactive({
  id: 0,
  username: '',
  email: '',
  password: '',
  roles: [] as string[],
  status: 'active' as 'active' | 'disabled'
});

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  roles: [{ type: 'array', required: dialogType.value === 'add', message: '请至少选择一个角色', trigger: 'change' }]
};

// 加载所有角色
const fetchAllRoles = async () => {
  loadingRoles.value = true;
  try {
    const roles = await roleAPI.getAllRoles();
    roleList.value = roles;
    console.log('获取到的角色列表:', roles);
  } catch (error) {
    console.error('获取角色列表失败', error);
    ElMessage.error('获取角色列表失败，可能影响用户编辑功能');
  } finally {
    loadingRoles.value = false;
  }
};

// 获取用户列表
const fetchUserList = async (retryCount = 0) => {
  loading.value = true;
  try {
    // 调用API获取用户列表
    const response = await userAPI.getAllUsers();
    
    if (!response || response.length === 0) {
      console.warn('获取用户列表成功，但数据为空');
      userList.value = [];
      total.value = 0;
      loading.value = false;
      return;
    }
    
    // 处理数据，将后端的enabled映射为前端的status
    const processedUsers = response.map(user => {
      try {
        // 统一角色格式为字符串数组
        const roles = Array.isArray(user.roles) 
          ? user.roles.map((role: any) => {
              if (typeof role === 'object' && role !== null && role.name) {
                return role.name;
              }
              return role as string;
            })
          : [user.role || 'ROLE_USER']; // 如果没有roles数组，使用单个role或默认角色
        
        return {
          ...user,
          roles,
          status: user.enabled !== undefined ? (user.enabled ? 'active' : 'disabled') : 'active'
        };
      } catch (err) {
        console.error('处理用户数据出错:', user, err);
        // 返回一个带有基本信息的对象，避免界面崩溃
        return {
          id: user.id || 0,
          username: user.username || '未知用户',
          email: user.email || '',
          roles: ['ROLE_USER'],
          status: 'active',
          createdAt: user.createdAt || new Date().toISOString()
        };
      }
    });
    
    // 处理筛选
    let filteredUsers = processedUsers.filter(user => {
      let matches = true;
      try {
        // 用户名筛选，不区分大小写
        if (filterForm.username && user.username && !user.username.toLowerCase().includes(filterForm.username.toLowerCase())) {
          matches = false;
        }
        
        // 角色筛选
        if (filterForm.role && Array.isArray(user.roles)) {
          const hasRole = user.roles.some((role: any) => {
            // 使用完全匹配，因为角色值可能是完整的ROLE_XX格式
            return role === filterForm.role;
          });
          
          if (!hasRole) {
            matches = false;
          }
        }
        
        // 状态筛选
        if (filterForm.status && user.status !== filterForm.status) {
          matches = false;
        }
      } catch (err) {
        console.error('过滤用户数据出错:', user, err);
        return true; // 出错时默认显示
      }
      return matches;
    });
    
    // 计算总数
    total.value = filteredUsers.length;
    
    // 分页并格式化数据
    const startIndex = (currentPage.value - 1) * pageSize.value;
    const endIndex = startIndex + pageSize.value;
    userList.value = filteredUsers.slice(startIndex, endIndex).map(user => ({
      ...user,
      createTime: user.createdAt ? dayjs(user.createdAt).format('YYYY-MM-DD HH:mm:ss') : '未知'
    }));
    
  } catch (error) {
    console.error('获取用户列表失败', error);
    
    // 重试逻辑
    if (retryCount < 2) {
      console.log(`正在重试获取用户列表(${retryCount + 1}/2)...`);
      setTimeout(() => {
        fetchUserList(retryCount + 1);
      }, 1000);
      return;
    }
    
    ElMessage.error('获取用户列表失败，请刷新重试');
    userList.value = [];
    total.value = 0;
  } finally {
    if (retryCount >= 2 || userList.value.length > 0) {
      loading.value = false;
    }
  }
};

// 重置过滤表单
const resetForm = () => {
  filterForm.username = '';
  filterForm.role = '';
  filterForm.status = '';
  fetchUserList();
};

// 添加用户
const handleAddUser = () => {
  dialogType.value = 'add';
  userForm.id = 0;
  userForm.username = '';
  userForm.email = '';
  userForm.password = '';
  userForm.roles = [];
  userForm.status = 'active';
  dialogVisible.value = true;
};

// 编辑用户
const handleEdit = (row: ExtendedUser) => {
  dialogType.value = 'edit';
  userForm.id = row.id;
  userForm.username = row.username;
  userForm.email = row.email;
  userForm.password = '';
  
  // 将角色转换为字符串数组
  userForm.roles = row.roles.map(role => {
    if (typeof role === 'object' && role !== null && role.name) {
      return role.name;
    }
    return role as string;
  });
  
  userForm.status = row.status;
  dialogVisible.value = true;
  
  // 打印角色信息用于调试
  console.log('编辑用户角色:', userForm.roles);
};

// 启用/禁用用户
const handleToggleStatus = (row: ExtendedUser) => {
  const newStatus = row.status === 'active' ? 'disabled' : 'active';
  const newEnabled = newStatus === 'active';
  const actionText = newStatus === 'active' ? '启用' : '禁用';
  
  ElMessageBox.confirm(
    `确认要${actionText}用户 "${row.username}" 吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        // 调用专门的状态切换API
        await userAPI.toggleUserStatus(row.id, newEnabled);
        ElMessage.success(`${actionText}用户成功`);
        fetchUserList(); // 重新获取列表
      } catch (error) {
        console.error(`${actionText}用户失败`, error);
        ElMessage.error(`${actionText}用户失败，请重试`);
      }
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 删除用户
const handleDelete = (row: ExtendedUser) => {
  ElMessageBox.confirm(
    `确认要删除用户 "${row.username}" 吗？此操作不可恢复！`,
    '确认删除',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'error'
    }
  )
    .then(async () => {
      try {
        // 调用API删除用户
        await userAPI.deleteUser(row.id);
        
        ElMessage.success('删除用户成功');
        fetchUserList(); // 重新获取列表
      } catch (error) {
        console.error('删除用户失败', error);
        ElMessage.error('删除用户失败，请重试');
      }
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 保存用户表单
const saveUser = async () => {
  // userFormRef.value需要类型断言或可选链调用
  const formEl = userFormRef.value as any;
  if (!formEl) return;
  
  // 在保存前打印当前表单数据，便于调试
  console.log('准备保存用户表单，当前数据:', {
    id: userForm.id,
    username: userForm.username,
    email: userForm.email,
    roles: userForm.roles,
    status: userForm.status
  });
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          // 添加用户 - 创建新用户
          console.log('创建用户，角色信息:', userForm.roles);
          
          const result = await userAPI.createUser({
            username: userForm.username,
            email: userForm.email,
            password: userForm.password,
            roles: userForm.roles,
            status: userForm.status
          });
          
          // 检查返回的消息，可能是成功或者错误信息
          if (result && result.message) {
            if (result.message.includes('successfully')) {
              ElMessage.success('添加用户成功');
              dialogVisible.value = false;
              fetchUserList(); // 刷新用户列表
            } else {
              // 处理后端返回的错误信息
              ElMessage.error(`添加用户失败: ${result.message}`);
            }
          } else {
            ElMessage.success('添加用户成功');
            dialogVisible.value = false;
            fetchUserList(); // 刷新用户列表
          }
        } else {
          // 编辑用户 - 使用映射后的数据结构
          console.log('更新用户信息:', userForm.id);
          console.log('更新的角色信息:', userForm.roles);
          
          await userAPI.updateUser(userForm.id, {
            username: userForm.username,
            email: userForm.email,
            roles: userForm.roles,
            status: userForm.status
          });
          ElMessage.success('更新用户成功');
          dialogVisible.value = false;
          fetchUserList(); // 刷新用户列表
        }
      } catch (error: any) {
        console.error('保存用户失败', error);
        // 显示详细的错误信息，如果有的话
        const errorMsg = error.response && error.response.data && error.response.data.message 
          ? error.response.data.message 
          : '保存用户失败，请重试';
        ElMessage.error(errorMsg);
      }
    } else {
      return false;
    }
  });
};

// 获取角色对应的标签类型
const getRoleTagType = (role: any) => {
  // 如果role是对象，则获取name属性
  const roleName = typeof role === 'object' && role !== null && role.name ? role.name : role;
  
  switch (roleName) {
    case 'ROLE_ADMIN':
      return 'danger';
    case 'ROLE_USER':
      return '';
    case 'ROLE_ATHLETE':
      return 'success';
    case 'ROLE_SPECTATOR':
      return 'warning';
    default:
      return 'info';
  }
};

// 格式化角色名称 - 修改为使用roleList的数据
const formatRoleName = (role: any) => {
  // 如果role是对象，则获取name属性
  const roleName = typeof role === 'object' && role !== null && role.name ? role.name : role;
  
  // 从roleList中查找对应的角色
  const foundRole = roleList.value.find(r => r.name === roleName);
  if (foundRole && foundRole.displayName) {
    return foundRole.displayName;
  }
  
  // 如果没找到，使用默认的显示逻辑
  switch (roleName) {
    case 'ROLE_ADMIN':
      return '管理员';
    case 'ROLE_USER':
      return '普通用户';
    case 'ROLE_ATHLETE':
      return '运动员';
    case 'ROLE_SPECTATOR':
      return '观众';
    default:
      // 移除ROLE_前缀用于显示
      return roleName.replace('ROLE_', '');
  }
};

// 分页相关
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchUserList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchUserList();
};

// 初始化加载
onMounted(() => {
  fetchUserList();
  fetchAllRoles();
});
</script>

<style scoped>
.user-list {
  width: 100%;
}

.filter-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.filter-input,
.filter-select {
  width: 200px;
}

.table-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  width: 100%;
  overflow-x: auto;
}

.user-table {
  width: 100%;
  table-layout: auto;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.role-tag {
  margin-right: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.loading-roles {
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}
</style> 