<template>
  <div class="role-management">
    <div class="main-container">
      <div class="header">
        <h3>角色管理</h3>
        <el-button type="primary" @click="resetForm">添加角色</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="roleList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色标识" width="180" />
        <el-table-column prop="displayName" label="角色名称" width="180" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
              >编辑</el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="isSystemRole(scope.row)"
              @click="handleDelete(scope.row)"
              >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑角色对话框 -->
    <el-dialog
      :title="isEdit ? '编辑角色' : '添加角色'"
      v-model="dialogVisible"
      width="500px"
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
            placeholder="请输入角色标识，如ROLE_CUSTOM" 
            :disabled="isEdit && isSystemRole(roleForm)"
          />
        </el-form-item>
        <el-form-item label="角色名称" prop="displayName">
          <el-input v-model="roleForm.displayName" placeholder="请输入角色名称，如自定义角色" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            placeholder="请输入角色描述"
            rows="3"
          />
        </el-form-item>
        <el-form-item label="权限" prop="permissions">
          <el-checkbox-group v-model="roleForm.permissions">
            <div class="permission-group">
              <div class="group-title">用户管理</div>
              <div class="group-content">
                <el-checkbox label="user:view">查看用户</el-checkbox>
                <el-checkbox label="user:create">创建用户</el-checkbox>
                <el-checkbox label="user:edit">编辑用户</el-checkbox>
                <el-checkbox label="user:delete">删除用户</el-checkbox>
              </div>
            </div>
            <div class="permission-group">
              <div class="group-title">赛事管理</div>
              <div class="group-content">
                <el-checkbox label="event:view">查看赛事</el-checkbox>
                <el-checkbox label="event:create">创建赛事</el-checkbox>
                <el-checkbox label="event:edit">编辑赛事</el-checkbox>
                <el-checkbox label="event:delete">删除赛事</el-checkbox>
              </div>
            </div>
            <div class="permission-group">
              <div class="group-title">报名管理</div>
              <div class="group-content">
                <el-checkbox label="registration:view">查看报名</el-checkbox>
                <el-checkbox label="registration:approve">审核报名</el-checkbox>
                <el-checkbox label="registration:cancel">取消报名</el-checkbox>
              </div>
            </div>
            <div class="permission-group">
              <div class="group-title">公告管理</div>
              <div class="group-content">
                <el-checkbox label="announcement:view">查看公告</el-checkbox>
                <el-checkbox label="announcement:create">创建公告</el-checkbox>
                <el-checkbox label="announcement:edit">编辑公告</el-checkbox>
                <el-checkbox label="announcement:delete">删除公告</el-checkbox>
              </div>
            </div>
          </el-checkbox-group>
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { roleAPI } from '../../../api';
import type { Role } from '../../../api/roleAPI';

// 角色列表数据
const roleList = ref<Role[]>([]);
const loading = ref(false);

// 对话框相关
const dialogVisible = ref(false);
const isEdit = ref(false);
const roleFormRef = ref(null);
const roleForm = reactive<Role>({
  id: 0,
  name: '',
  displayName: '',
  description: '',
  permissions: [],
  createdAt: ''
});

// 表单验证规则
const roleRules = {
  name: [
    { required: true, message: '请输入角色标识', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z_]+$/, message: '角色标识必须以ROLE_开头，并且只能包含大写字母和下划线', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  permissions: [
    { type: 'array', required: true, message: '请至少选择一个权限', trigger: 'change' }
  ]
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
const handleEdit = (row: Role) => {
  isEdit.value = true;
  Object.assign(roleForm, row);
  dialogVisible.value = true;
};

// 判断是否为系统内置角色
const isSystemRole = (role: Role) => {
  return role.isSystem === true;
};

// 删除角色
const handleDelete = (row: Role) => {
  if (isSystemRole(row)) {
    ElMessage.warning('系统内置角色不能删除');
    return;
  }
  
  ElMessageBox.confirm(
    `确认要删除角色 "${row.displayName}" 吗？此操作不可恢复！`,
    '确认删除',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        // 调用API删除角色
        await roleAPI.deleteRole(row.id);
        
        ElMessage.success('删除角色成功');
        fetchRoleList(); // 重新获取角色列表
      } catch (error) {
        console.error('删除角色失败', error);
        ElMessage.error('删除角色失败，请重试');
      }
    })
    .catch(() => {
      // 用户取消删除操作
    });
};

// 保存角色
const saveRole = async () => {
  const formEl = roleFormRef.value as any;
  if (!formEl) return;
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑角色
          await roleAPI.updateRole(roleForm.id, {
            name: roleForm.name,
            displayName: roleForm.displayName,
            description: roleForm.description,
            permissions: roleForm.permissions
          });
          ElMessage.success('更新角色成功');
        } else {
          // 添加角色
          await roleAPI.createRole({
            name: roleForm.name,
            displayName: roleForm.displayName,
            description: roleForm.description,
            permissions: roleForm.permissions
          });
          ElMessage.success('添加角色成功');
        }
        dialogVisible.value = false;
        fetchRoleList(); // 刷新角色列表
      } catch (error) {
        console.error('保存角色失败', error);
        ElMessage.error('保存角色失败，请重试');
      }
    } else {
      return false;
    }
  });
};

// 重置表单
const resetForm = () => {
  isEdit.value = false;
  roleForm.id = 0;
  roleForm.name = '';
  roleForm.displayName = '';
  roleForm.description = '';
  roleForm.permissions = [];
  dialogVisible.value = true;
};

// 监听对话框关闭事件，重置表单
const handleDialogClose = () => {
  isEdit.value = false;
  if (roleFormRef.value) {
    (roleFormRef.value as any).resetFields();
  }
};

// 初始化加载
onMounted(() => {
  fetchRoleList();
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

.permission-group {
  margin-bottom: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.group-title {
  background-color: #f5f7fa;
  padding: 8px 15px;
  font-weight: 500;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 