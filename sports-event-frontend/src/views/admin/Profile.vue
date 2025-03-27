<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧用户信息 -->
      <el-col :sm="4" :md="8" :lg="6">
        <el-card shadow="hover" class="user-card">
          <div class="user-header">
            <div class="avatar-container">
              <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar">
                {{ userInfo.username?.substring(0, 1).toUpperCase() }}
              </el-avatar>
              <el-button class="change-avatar-btn" size="small" @click="handleChangeAvatar">
                <el-icon><Camera /></el-icon>
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
          action="/api/files/upload"
          :headers="{ Authorization: `Bearer ${authStore.token}` }"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
          name="file"
        >
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-preview" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">
          请选择一张图片作为您的头像
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="avatarDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../../stores/auth';
import { userAPI, type ExtendedUser } from '../../api/userAPI';
import dayjs from 'dayjs';
import { Camera, Plus } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 用户基本信息
const userInfo = reactive({
  username: authStore.user?.username || '',
  email: authStore.user?.email || '',
  roles: authStore.user?.roles || [],
  avatar: '',
  createdAt: '',
  lastLogin: '',
  realName: '',
  phone: '',
  bio: ''
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
    // 调用API更新用户信息
    await userAPI.updateCurrentUser({
      email: basicForm.email,
      realName: basicForm.realName,
      phone: basicForm.phone
      // bio 字段不在 User 类型中，不传递给 API
    });
    
    // 更新本地用户信息
    Object.assign(userInfo, basicForm);
    ElMessage.success('个人信息更新成功');
  } catch (error) {
    console.error('更新个人信息失败', error);
    ElMessage.error('更新个人信息失败，请重试');
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
      validator: (_: any, value: string, callback: Function) => {
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
    // 调用API更新密码
    await userAPI.changePassword(
      passwordForm.currentPassword,
      passwordForm.newPassword
    );
    
    ElMessage.success('密码更新成功');
    resetPasswordForm();
  } catch (error) {
    console.error('更新密码失败', error);
    ElMessage.error('密码更新失败，请检查当前密码是否正确');
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields();
  }
};

// 安全记录 - 从API获取或设置为空
const securityActivities = ref([
  {
    content: '成功登录系统',
    timestamp: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    type: 'success',
    color: '#67C23A'
  }
]);

// 安全设置
const securitySettings = reactive({
  twoFactorAuth: false,
  loginNotification: true
});

// 头像上传相关
const avatarDialogVisible = ref(false);
const avatarUrl = ref('');
const uploadingAvatar = ref(false);

// 打开头像上传对话框
const handleChangeAvatar = () => {
  avatarUrl.value = userInfo.avatar || '';
  avatarDialogVisible.value = true;
};

// 上传前验证
const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg';
  const isPNG = file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!');
    return false;
  }
  
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!');
    return false;
  }
  
  uploadingAvatar.value = true;
  return true;
};

// 上传成功回调
const handleAvatarSuccess = async (response: any) => {
  uploadingAvatar.value = false;
  console.log('上传响应:', response);
  
  // 处理不同的响应格式
  let avatarPath = '';
  if (response && response.filename) {
    avatarPath = `/api/files/download/${response.filename}`;
  } else if (response && response.id) {
    avatarPath = `/api/files/download/${response.id}`;
  } else if (response && response.fileId) {
    avatarPath = `/api/files/download/${response.fileId}`;
  } else if (response && response.data) {
    avatarPath = response.data;
  } else if (typeof response === 'string') {
    avatarPath = response;
  }
  
  // 确保URL不包含token参数
  if (avatarPath) {
    try {
      // 移除URL中的token参数
      const urlWithoutToken = avatarPath.split('?')[0];
      
      // 更新本地头像
      userInfo.avatar = urlWithoutToken;
      authStore.updateUserAvatar(urlWithoutToken);
      avatarUrl.value = urlWithoutToken;
      
      // 关键修复：调用API更新用户头像到后端数据库
      await userAPI.updateCurrentUser({ avatar: urlWithoutToken });
      
      ElMessage.success('头像更新成功');
      avatarDialogVisible.value = false;
    } catch (error) {
      console.error('保存头像到后端失败:', error);
      ElMessage.error('头像更新失败，请重试');
    }
  } else {
    ElMessage.error('头像上传失败，请重试');
    console.error('无效的响应格式:', response);
  }
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

// 格式化角色名称
const formatRoleName = (role: any) => {
  // 如果role是对象，则获取name属性
  const roleName = typeof role === 'object' && role !== null && role.name ? role.name : role;
  
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
      return roleName;
  }
};

// 初始化
onMounted(async () => {
  try {
    // 获取当前用户详细信息
    const userData = await userAPI.getCurrentUser();
    console.log('获取到的用户信息:', userData);
    
    // 更新用户信息，处理 createdAt 和 lastLogin（在 ExtendedUser 中可能存在）
    Object.assign(userInfo, {
      ...userData,
      createdAt: (userData as ExtendedUser).createdAt ? dayjs((userData as ExtendedUser).createdAt).format('YYYY-MM-DD HH:mm:ss') : '未知',
      lastLogin: '未知', // 后端API暂不提供 lastLogin
      avatar: userData.avatar || '' // 确保头像URL被正确赋值
    });
    
    // 如果头像存在，同时更新到authStore
    if (userData.avatar) {
      authStore.updateUserAvatar(userData.avatar);
    }
    
    // 更新表单数据
    Object.assign(basicForm, {
      username: userData.username || '',
      email: userData.email || '',
      realName: (userData as any).realName || '',
      phone: userData.phone || '',
      bio: (userData as any).bio || ''
    });
    
    // 如果有登录历史记录API，获取安全记录
    // const securityLogs = await userAPI.getSecurityLogs();
    // if (securityLogs && securityLogs.length > 0) {
    //   securityActivities.value = securityLogs.map(log => ({
    //     content: log.action,
    //     timestamp: dayjs(log.timestamp).format('YYYY-MM-DD HH:mm:ss'),
    //     type: log.type,
    //     color: getLogColor(log.type)
    //   }));
    // }
  } catch (error) {
    console.error('获取用户信息失败', error);
    ElMessage.error('获取用户信息失败，请刷新重试');
  }
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

.username {
  font-size: 18px;
  font-weight: 500;
  margin: 10px 0 5px;
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
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
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
  justify-content: center;
}

.avatar-uploader {
  width: 200px;
  height: 200px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
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
  color: #606266;
  font-size: 14px;
  text-align: center;
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 