<template>
  <admin-layout>
    <div class="profile-container">
      <el-row :gutter="20">
        <!-- 左侧用户信息 -->
        <el-col :sm="24" :md="8" :lg="6">
          <el-card shadow="hover" class="user-card">
            <div class="user-header">
              <div class="avatar-container">
                <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar">
                  {{ userInfo.username?.substring(0, 1).toUpperCase() }}
                </el-avatar>
                <el-button class="change-avatar-btn" size="small" @click="handleChangeAvatar">
                  <el-icon><el-icon-camera /></el-icon>
                </el-button>
              </div>
              <h3 class="username">{{ userInfo.username }}</h3>
              <div class="user-role">
                <el-tag
                  v-for="role in userInfo.roles"
                  :key="role"
                  :type="getRoleTagType(role)"
                  class="role-tag"
                >
                  {{ formatRoleName(role) }}
                </el-tag>
              </div>
            </div>
            
            <div class="user-info">
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ userInfo.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">注册时间：</span>
                <span class="value">{{ userInfo.createdAt }}</span>
              </div>
              <div class="info-item">
                <span class="label">最后登录：</span>
                <span class="value">{{ userInfo.lastLogin }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 右侧信息编辑 -->
        <el-col :sm="24" :md="16" :lg="18">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <el-tabs v-model="activeTab">
                  <el-tab-pane label="基本信息" name="basic-info"></el-tab-pane>
                  <el-tab-pane label="修改密码" name="change-password"></el-tab-pane>
                  <el-tab-pane label="账户安全" name="security"></el-tab-pane>
                </el-tabs>
              </div>
            </template>
            
            <!-- 基本信息表单 -->
            <div v-if="activeTab === 'basic-info'">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-width="100px"
                label-position="left"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="basicForm.username" disabled />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="basicForm.email" />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="basicForm.realName" />
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="basicForm.phone" />
                </el-form-item>
                <el-form-item label="个人简介" prop="bio">
                  <el-input
                    type="textarea"
                    v-model="basicForm.bio"
                    rows="4"
                    placeholder="请输入个人简介"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateBasicInfo">保存修改</el-button>
                  <el-button @click="resetBasicForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 修改密码表单 -->
            <div v-if="activeTab === 'change-password'">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                label-position="left"
              >
                <el-form-item label="当前密码" prop="currentPassword">
                  <el-input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    show-password
                    placeholder="请输入当前密码"
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                    placeholder="请输入新密码"
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="请再次输入新密码"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updatePassword">更新密码</el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 账户安全信息 -->
            <div v-if="activeTab === 'security'">
              <el-timeline>
                <el-timeline-item
                  v-for="(activity, index) in securityActivities"
                  :key="index"
                  :timestamp="activity.timestamp"
                  :type="activity.type"
                  :color="activity.color"
                >
                  {{ activity.content }}
                </el-timeline-item>
              </el-timeline>
              
              <div class="security-settings">
                <h4>安全设置</h4>
                <el-divider />
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">两步验证</div>
                    <div class="setting-desc">启用两步验证提高账户安全性</div>
                  </div>
                  <el-switch v-model="securitySettings.twoFactorAuth" />
                </div>
                <el-divider />
                <div class="setting-item">
                  <div class="setting-info">
                    <div class="setting-title">登录通知</div>
                    <div class="setting-desc">异地登录时通过邮件通知</div>
                  </div>
                  <el-switch v-model="securitySettings.loginNotification" />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 头像上传对话框 -->
      <el-dialog v-model="avatarDialogVisible" title="更换头像" width="400px">
        <div class="avatar-upload">
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><el-icon-plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            请选择一张图片作为您的头像
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="avatarDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveAvatar">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../../stores/auth';
import AdminLayout from '../../components/AdminLayout.vue';

const authStore = useAuthStore();
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 用户基本信息
const userInfo = reactive({
  username: authStore.user?.username || '',
  email: authStore.user?.email || '',
  roles: authStore.user?.roles || [],
  avatar: '',
  createdAt: '2023-01-01 08:00:00',
  lastLogin: '2023-04-25 10:30:15',
  realName: '张三',
  phone: '13800138000',
  bio: '体育赛事管理系统管理员'
});

// 标签页
const activeTab = ref('basic-info');

// 基本信息表单
const basicFormRef = ref();
const basicForm = reactive({
  username: userInfo.username,
  email: userInfo.email,
  realName: userInfo.realName,
  phone: userInfo.phone,
  bio: userInfo.bio
});

// 表单验证规则
const basicRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
};

// 更新基本信息
const updateBasicInfo = async () => {
  if (!basicFormRef.value) return;
  
  try {
    await basicFormRef.value.validate();
    // 这里应该调用API更新用户信息
    // await userAPI.updateUserInfo(basicForm);
    
    // 更新本地用户信息
    Object.assign(userInfo, basicForm);
    ElMessage.success('个人信息更新成功');
  } catch (error) {
    console.error('表单验证失败', error);
  }
};

// 重置基本信息表单
const resetBasicForm = () => {
  if (basicFormRef.value) {
    basicFormRef.value.resetFields();
  }
};

// 密码修改表单
const passwordFormRef = ref();
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码表单验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 更新密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return;
  
  try {
    await passwordFormRef.value.validate();
    // 这里应该调用API更新密码
    // await userAPI.updatePassword(passwordForm);
    
    ElMessage.success('密码更新成功');
    resetPasswordForm();
  } catch (error) {
    console.error('表单验证失败', error);
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields();
  }
};

// 安全记录
const securityActivities = [
  {
    content: '成功登录系统',
    timestamp: '2023-04-25 10:30:15',
    type: 'success',
    color: '#67C23A'
  },
  {
    content: '修改了账户密码',
    timestamp: '2023-04-10 15:45:22',
    type: 'warning',
    color: '#E6A23C'
  },
  {
    content: '更新了邮箱地址',
    timestamp: '2023-03-28 09:15:30',
    type: 'info',
    color: '#909399'
  },
  {
    content: '首次登录系统',
    timestamp: '2023-01-01 08:00:00',
    type: 'primary',
    color: '#409EFF'
  }
];

// 安全设置
const securitySettings = reactive({
  twoFactorAuth: false,
  loginNotification: true
});

// 头像上传相关
const avatarDialogVisible = ref(false);
const avatarUrl = ref('');

// 打开头像上传对话框
const handleChangeAvatar = () => {
  avatarUrl.value = userInfo.avatar || '';
  avatarDialogVisible.value = true;
};

// 头像选择回调
const handleAvatarChange = (file: any) => {
  if (file.raw.type !== 'image/jpeg' && file.raw.type !== 'image/png') {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!');
    return;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => {
    avatarUrl.value = e.target?.result as string;
  };
  reader.readAsDataURL(file.raw);
};

// 保存头像
const saveAvatar = async () => {
  if (!avatarUrl.value) {
    ElMessage.warning('请先选择一张图片');
    return;
  }
  
  try {
    // 这里应该调用API上传头像
    // await userAPI.uploadAvatar(avatarFile);
    
    // 更新本地头像
    userInfo.avatar = avatarUrl.value;
    ElMessage.success('头像更新成功');
    avatarDialogVisible.value = false;
  } catch (error) {
    console.error('头像上传失败', error);
    ElMessage.error('头像上传失败，请重试');
  }
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

// 初始化
onMounted(() => {
  // 这里可以调用API获取更详细的用户信息
  // const fetchUserDetail = async () => {
  //   try {
  //     const response = await userAPI.getUserDetail(authStore.user?.id);
  //     Object.assign(userInfo, response.data);
  //     Object.assign(basicForm, {
  //       username: response.data.username,
  //       email: response.data.email,
  //       realName: response.data.realName,
  //       phone: response.data.phone,
  //       bio: response.data.bio
  //     });
  //   } catch (error) {
  //     console.error('获取用户详情失败', error);
  //   }
  // };
  // fetchUserDetail();
});
</script>

<style scoped>
.profile-container {
  width: 100%;
}

.user-card {
  height: 100%;
}

.user-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.avatar-container {
  position: relative;
  margin-bottom: 15px;
}

.change-avatar-btn {
  position: absolute;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  padding: 8px;
  background: rgba(255, 255, 255, 0.8);
}

.username {
  margin: 10px 0;
  font-size: 18px;
  font-weight: 500;
}

.user-role {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 5px;
}

.role-tag {
  margin: 0 2px;
}

.user-info {
  padding: 20px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
}

.info-item .label {
  width: 80px;
  color: #909399;
}

.info-item .value {
  flex: 1;
  color: #303133;
}

.card-header {
  padding: 0;
}

.security-settings {
  margin-top: 20px;
}

.security-settings h4 {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 500;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.setting-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.setting-desc {
  font-size: 13px;
  color: #909399;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  width: 150px;
  height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  color: #909399;
  font-size: 13px;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 