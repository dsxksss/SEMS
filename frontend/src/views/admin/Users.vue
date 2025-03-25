<template>
  <div class="container mx-auto py-8 px-4">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">用户管理</h1>
      <el-button type="primary" @click="showAddDialog">添加用户</el-button>
    </div>

    <!-- 筛选区域 -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <el-form :inline="true" :model="searchForm" class="flex flex-wrap items-center">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="searchForm.email" placeholder="邮箱地址" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="用户角色" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
            <el-option label="运动员" value="ATHLETE" />
            <el-option label="观众" value="AUDIENCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="账号状态" clearable>
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <el-table :data="users" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag :type="getRoleType(scope.row.role)">
            {{ translateRole(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ scope.row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editUser(scope.row)">编辑</el-button>
          <el-button 
            :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'"
            size="small" 
            @click="toggleUserStatus(scope.row)"
          >
            {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" size="small" @click="confirmDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="flex justify-end mt-4">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
            <el-option label="运动员" value="ATHLETE" />
            <el-option label="观众" value="AUDIENCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据列表
const users = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  username: '',
  email: '',
  role: '',
  status: ''
})

// 表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  role: 'USER',
  status: 'ACTIVE'
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度为4-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await axios.get('/user/list', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        username: searchForm.username,
        email: searchForm.email,
        role: searchForm.role,
        status: searchForm.status
      }
    })
    users.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.email = ''
  form.password = ''
  form.role = 'USER'
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

// 编辑用户
const editUser = (row) => {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.email = row.email
  form.password = '' // 编辑时不修改密码
  form.role = row.role
  form.status = row.status
  dialogVisible.value = true
}

// 切换用户状态
const toggleUserStatus = async (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = newStatus === 'ACTIVE' ? '启用' : '禁用'
  
  try {
    await axios.put(`/user/${row.id}/status`, { status: newStatus })
    ElMessage.success(`${actionText}成功`)
    row.status = newStatus // 直接更新本地数据
  } catch (error) {
    ElMessage.error(`${actionText}失败`)
    console.error(error)
  }
}

// 确认删除
const confirmDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteUser(row.id)
  }).catch(() => {})
}

// 删除用户
const deleteUser = async (id) => {
  try {
    await axios.delete(`/user/${id}`)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (isEdit.value) {
        // 编辑用户
        const updateData = {
          email: form.email,
          role: form.role,
          status: form.status
        }
        await axios.put(`/user/${form.id}`, updateData)
        ElMessage.success('更新成功')
      } else {
        // 添加用户
        await axios.post('/user/add', form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadUsers()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
      console.error(error)
    }
  })
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

// 重置搜索
const resetSearch = () => {
  searchForm.username = ''
  searchForm.email = ''
  searchForm.role = ''
  searchForm.status = ''
  currentPage.value = 1
  loadUsers()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  loadUsers()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadUsers()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 获取角色对应的Tag类型
const getRoleType = (role) => {
  const roleMap = {
    'ADMIN': 'danger',
    'USER': '',
    'ATHLETE': 'success',
    'AUDIENCE': 'info'
  }
  return roleMap[role] || ''
}

// 翻译角色枚举
const translateRole = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'USER': '用户',
    'ATHLETE': '运动员',
    'AUDIENCE': '观众'
  }
  return roleMap[role] || role
}

// 初始化
onMounted(() => {
  loadUsers()
})
</script> 