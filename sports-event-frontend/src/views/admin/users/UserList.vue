<template>
  <div class="user-list">
    <div class="filter-container">
      <el-form :inline="true" :model="filterForm" class="form-inline">
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="输入用户名搜索" clearable class="filter-input" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filterForm.role" placeholder="选择角色" clearable class="filter-select">
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="普通用户" value="ROLE_USER" />
            <el-option label="运动员" value="ROLE_ATHLETE" />
            <el-option label="观众" value="ROLE_SPECTATOR" />
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
          <el-checkbox-group v-model="userForm.roles" id="roles">
            <el-checkbox label="ROLE_ADMIN">管理员</el-checkbox>
            <el-checkbox label="ROLE_USER">普通用户</el-checkbox>
            <el-checkbox label="ROLE_ATHLETE">运动员</el-checkbox>
            <el-checkbox label="ROLE_SPECTATOR">观众</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status" id="status">
            <el-radio label="active">激活</el-radio>
            <el-radio label="disabled">禁用</el-radio>
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
  roles: [{ type: 'array', required: true, message: '请至少选择一个角色', trigger: 'change' }]
};

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    // 调用API获取用户列表
    const response = await userAPI.getAllUsers();
    
    // 处理筛选
    let filteredUsers = response.filter(user => {
      let matches = true;
      // 用户名筛选，不区分大小写
      if (filterForm.username && !user.username.toLowerCase().includes(filterForm.username.toLowerCase())) {
        matches = false;
      }
      // 角色筛选
      if (filterForm.role && !user.roles.includes(filterForm.role)) {
        matches = false;
      }
      // 状态筛选
      if (filterForm.status && user.status !== filterForm.status) {
        matches = false;
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
    ElMessage.error('获取用户列表失败，请刷新重试');
  } finally {
    loading.value = false;
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
  userForm.roles = row.roles;
  userForm.status = row.status;
  dialogVisible.value = true;
};

// 启用/禁用用户
const handleToggleStatus = (row: ExtendedUser) => {
  const newStatus = row.status === 'active' ? 'disabled' : 'active';
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
        // 调用API更新用户状态
        await userAPI.updateUser(row.id, { 
          status: newStatus,
          username: row.username,
          email: row.email,
          roles: row.roles
        });
        
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
      type: 'danger'
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
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          // 添加用户 - 创建新用户
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
          // 编辑用户 - 确保传递所有必要字段
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
const getRoleTagType = (role: string) => {
  switch (role) {
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

// 格式化角色名称
const formatRoleName = (role: string) => {
  switch (role) {
    case 'ROLE_ADMIN':
      return '管理员';
    case 'ROLE_USER':
      return '普通用户';
    case 'ROLE_ATHLETE':
      return '运动员';
    case 'ROLE_SPECTATOR':
      return '观众';
    default:
      return role;
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
</style> 