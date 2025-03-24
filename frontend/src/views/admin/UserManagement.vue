<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">用户管理</h1>
      <el-button type="primary" @click="handleAddUser">添加用户</el-button>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="flex flex-wrap gap-4">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名/邮箱/姓名"
          class="max-w-xs"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="roleFilter" placeholder="用户角色" clearable @change="handleFilterChange">
          <el-option label="全部角色" value="" />
          <el-option label="管理员" value="ADMIN" />
          <el-option label="运动员" value="ATHLETE" />
          <el-option label="观众" value="USER" />
        </el-select>
        
        <el-select v-model="statusFilter" placeholder="账号状态" clearable @change="handleFilterChange">
          <el-option label="全部状态" value="" />
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
      </div>
    </div>
    
    <!-- 用户列表 -->
    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <el-table
        v-loading="loading"
        :data="filteredUsers"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="username" label="用户名" min-width="120" />
        
        <el-table-column prop="email" label="邮箱" min-width="180" />
        
        <el-table-column prop="realName" label="姓名" min-width="120">
          <template #default="scope">
            {{ scope.row.realName || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="phone" label="联系电话" min-width="140">
          <template #default="scope">
            {{ scope.row.phone || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="role" label="角色" min-width="120">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="注册时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link :type="scope.row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)" :disabled="scope.row.role === 'ADMIN'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="flex justify-end p-4">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalUsers"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingUser.id ? '编辑用户' : '添加用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!!editingUser.id" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!editingUser.id">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="运动员" value="ATHLETE" />
            <el-option label="观众" value="USER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

// 用户列表
const users = ref([])
const loading = ref(true)
const error = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalUsers = ref(0)

// 搜索和筛选
const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')

// 用户表单
const dialogVisible = ref(false)
const userFormRef = ref(null)
const submitting = ref(false)
const editingUser = reactive({
  id: null
})

const userForm = reactive({
  username: '',
  email: '',
  password: '',
  realName: '',
  phone: '',
  role: 'USER',
  status: 1
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在3到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6到20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 筛选后的用户列表
const filteredUsers = computed(() => {
  return users.value
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get('/api/users', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        query: searchQuery.value || undefined,
        role: roleFilter.value || undefined,
        status: statusFilter.value || undefined
      }
    })
    
    users.value = response.data.data.list
    totalUsers.value = response.data.data.total
  } catch (err) {
    error.value = err.response?.data?.message || '获取用户列表失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 处理筛选
const handleFilterChange = () => {
  currentPage.value = 1
  fetchUsers()
}

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUsers()
}

// 添加用户
const handleAddUser = () => {
  // 重置表单
  Object.assign(userForm, {
    username: '',
    email: '',
    password: '',
    realName: '',
    phone: '',
    role: 'USER',
    status: 1
  })
  
  editingUser.id = null
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (user) => {
  // 填充表单数据
  Object.assign(userForm, {
    username: user.username,
    email: user.email,
    password: '', // 不回显密码
    realName: user.realName || '',
    phone: user.phone || '',
    role: user.role,
    status: user.status
  })
  
  editingUser.id = user.id
  dialogVisible.value = true
}

// 切换用户状态
const handleToggleStatus = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    const action = newStatus === 1 ? '启用' : '禁用'
    
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      `${action}用户`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await axios.put(`/api/users/${user.id}/status`, { status: newStatus })
    
    // 更新本地数据
    user.status = newStatus
    
    ElMessage.success(`${action}用户成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
    }
  }
}

// 删除用户
const handleDelete = async (user) => {
  if (user.role === 'ADMIN') {
    ElMessage.warning('不能删除管理员账号')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可撤销！`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    
    await axios.delete(`/api/users/${user.id}`)
    
    // 从列表中移除
    users.value = users.value.filter(u => u.id !== user.id)
    totalUsers.value--
    
    ElMessage.success('删除用户成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || '未知错误'))
    }
  }
}

// 提交用户表单
const submitUserForm = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      try {
        let response
        
        if (editingUser.id) {
          // 编辑用户
          response = await axios.put(`/api/users/${editingUser.id}`, userForm)
          
          // 更新本地数据
          const index = users.value.findIndex(u => u.id === editingUser.id)
          if (index !== -1) {
            users.value[index] = response.data.data
          }
          
          ElMessage.success('更新用户成功')
        } else {
          // 添加用户
          response = await axios.post('/api/users', userForm)
          
          // 添加到列表头部
          users.value.unshift(response.data.data)
          totalUsers.value++
          
          ElMessage.success('添加用户成功')
        }
        
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        submitting.value = false
      }
    }
  })
}

// 获取角色类型和文本
const getRoleType = (role) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'ATHLETE': return 'success'
    default: return 'info'
  }
}

const getRoleText = (role) => {
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'ATHLETE': return '运动员'
    default: return '观众'
  }
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化
onMounted(() => {
  fetchUsers()
  
  // 模拟数据
  users.value = [
    {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      realName: '系统管理员',
      phone: '13800138000',
      role: 'ADMIN',
      status: 1,
      createTime: '2023-01-01T08:00:00'
    },
    {
      id: 2,
      username: 'athlete1',
      email: 'athlete1@example.com',
      realName: '张三',
      phone: '13800138001',
      role: 'ATHLETE',
      status: 1,
      createTime: '2023-01-05T10:30:00'
    },
    {
      id: 3,
      username: 'user1',
      email: 'user1@example.com',
      realName: '李四',
      phone: '13800138002',
      role: 'USER',
      status: 1,
      createTime: '2023-01-10T14:20:00'
    }
  ]
  
  totalUsers.value = users.value.length
  loading.value = false
})
</script> 