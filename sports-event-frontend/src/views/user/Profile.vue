<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card class="profile-card">
          <div class="avatar-container">
            <el-avatar :size="100" :src="userInfo.avatar || ''" class="user-avatar">
              {{ userInfo.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="avatar-actions">
              <el-upload
                class="avatar-uploader"
                action="/api/users/avatar"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <el-button type="primary" size="small">更换头像</el-button>
              </el-upload>
            </div>
          </div>
          
          <div class="user-info">
            <h2 class="username">{{ userInfo.username }}</h2>
            <p class="email">{{ userInfo.email }}</p>
            <div class="user-roles">
              <el-tag v-for="role in userRoles" :key="role" size="small" effect="plain">
                {{ getRoleName(role) }}
              </el-tag>
            </div>
          </div>
          
          <el-divider />
          
          <div class="account-stats">
            <div class="stat-item">
              <div class="stat-value">{{ registrationCount }}</div>
              <div class="stat-label">报名次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ accountDays }}</div>
              <div class="stat-label">账号天数</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="security-card">
          <template #header>
            <div class="card-header">
              <h3>账号安全</h3>
            </div>
          </template>
          <div class="security-content">
            <div class="security-item">
              <span class="item-label">账号密码</span>
              <div class="item-action">
                <el-button type="primary" size="small" text @click="showChangePasswordDialog">
                  修改密码
                </el-button>
              </div>
            </div>
            
            <div class="security-item">
              <span class="item-label">邮箱绑定</span>
              <div class="item-action">
                <el-tag size="small" type="success">已绑定</el-tag>
              </div>
            </div>
            
            <div class="security-item">
              <span class="item-label">手机绑定</span>
              <div class="item-action">
                <template v-if="userInfo.phone">
                  <el-tag size="small" type="success">已绑定</el-tag>
                  <el-button type="primary" size="small" text @click="showChangePhoneDialog">
                    修改
                  </el-button>
                </template>
                <template v-else>
                  <el-button type="primary" size="small" text @click="showBindPhoneDialog">
                    绑定手机
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="16">
        <el-card class="profile-form-card">
          <template #header>
            <div class="card-header">
              <h3>个人资料</h3>
            </div>
          </template>
          
          <el-form 
            :model="profileForm" 
            :rules="profileRules" 
            ref="profileFormRef" 
            label-position="top"
            class="profile-form"
          >
            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :md="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-form-item label="电子邮箱" prop="email">
                  <el-input v-model="profileForm.email" disabled />
                </el-form-item>
              </el-col>
              
              <el-col :xs="24" :md="12">
                <el-form-item label="手机号码" prop="phone">
                  <el-input 
                    v-model="profileForm.phone" 
                    placeholder="请输入手机号码" 
                    :disabled="!!userInfo.phone"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item>
              <el-button type="primary" @click="submitForm" :loading="submitting">保存修改</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <h3>最近活动</h3>
            </div>
          </template>
          
          <div v-if="loadingActivities" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="activities.length === 0" class="empty-container">
            <el-empty description="暂无活动记录" />
          </div>
          <div v-else>
            <el-timeline>
              <el-timeline-item
                v-for="activity in activities"
                :key="activity.id"
                :timestamp="formatDateTime(activity.createdAt)"
                :type="getActivityType(activity.type)"
              >
                <div class="activity-content">
                  {{ activity.description }}
                </div>
                <div v-if="activity.event" class="activity-event">
                  <router-link :to="`/user/events/${activity.event.id}`">
                    {{ activity.event.name }}
                  </router-link>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="30%"
    >
      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input 
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="changePassword" :loading="changingPassword">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 绑定手机对话框 -->
    <el-dialog
      v-model="phoneDialogVisible"
      title="绑定手机"
      width="30%"
    >
      <el-form
        :model="phoneForm"
        :rules="phoneRules"
        ref="phoneFormRef"
        label-position="top"
      >
        <el-form-item label="手机号码" prop="phone">
          <el-input 
            v-model="phoneForm.phone"
            placeholder="请输入手机号码"
          />
        </el-form-item>
        
        <el-form-item label="验证码" prop="code">
          <div class="verification-code">
            <el-input 
              v-model="phoneForm.code"
              placeholder="请输入验证码"
              class="code-input"
            />
            <el-button 
              type="primary" 
              :disabled="codeCounting" 
              @click="getVerificationCode"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="phoneDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="bindPhone" :loading="bindingPhone">
            确认绑定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useAuthStore } from '../../stores/auth';
import { userAPI } from '../../api/userAPI';
import { registrationAPI } from '../../api/registrationAPI';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import { formatDateTime, formatDate } from '../../utils/formatter';

const authStore = useAuthStore();

// 用户基本信息
const userInfo = computed(() => authStore.user || {});
const userRoles = computed(() => userInfo.value?.roles || []);

// 数据加载状态
const loadingActivities = ref(true);
const submitting = ref(false);
const changingPassword = ref(false);
const bindingPhone = ref(false);

// 统计数据
const registrationCount = ref(0);
const accountDays = computed(() => {
  // 假设创建时间在用户数据中
  if (userInfo.value?.createdAt) {
    const createdDate = new Date(userInfo.value.createdAt);
    const today = new Date();
    const diffTime = Math.abs(today.getTime() - createdDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }
  return 0;
});

// 对话框状态
const passwordDialogVisible = ref(false);
const phoneDialogVisible = ref(false);

// 个人资料表单
const profileFormRef = ref<FormInstance>();
const profileForm = reactive({
  username: userInfo.value?.username || '',
  email: userInfo.value?.email || '',
  realName: userInfo.value?.realName || '',
  phone: userInfo.value?.phone || ''
});

// 表单验证规则
const profileRules = reactive<FormRules>({
  realName: [
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
});

// 密码表单
const passwordFormRef = ref<FormInstance>();
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码表单验证规则
const passwordRules = reactive<FormRules>({
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// 手机表单
const phoneFormRef = ref<FormInstance>();
const phoneForm = reactive({
  phone: '',
  code: ''
});

// 手机表单验证规则
const phoneRules = reactive<FormRules>({
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
});

// 验证码计时
const codeCounting = ref(false);
const countdown = ref(60);
const codeButtonText = computed(() => codeCounting.value ? `${countdown.value}秒后重试` : '获取验证码');

// 活动记录
const activities = ref<any[]>([]);

// 上传头像的请求头
const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  };
});

// 初始化数据
onMounted(async () => {
  await Promise.all([
    fetchRegistrationCount(),
    fetchActivities()
  ]);
  
  // 初始化表单数据
  profileForm.username = userInfo.value?.username || '';
  profileForm.email = userInfo.value?.email || '';
  profileForm.realName = userInfo.value?.realName || '';
  profileForm.phone = userInfo.value?.phone || '';
});

// 获取报名次数
const fetchRegistrationCount = async () => {
  try {
    const registrations = await registrationAPI.getMyRegistrations();
    registrationCount.value = registrations.length;
  } catch (error) {
    console.error('获取报名次数失败:', error);
  }
};

// 获取活动记录
const fetchActivities = async () => {
  loadingActivities.value = true;
  try {
    // 模拟活动记录，实际应该从API获取
    // 这里可以替换为真实的活动记录API
    setTimeout(() => {
      activities.value = [
        {
          id: 1,
          type: 'registration',
          description: '您报名了赛事',
          event: { id: 1, name: '2023校园马拉松' },
          createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString()
        },
        {
          id: 2,
          type: 'profile',
          description: '您更新了个人信息',
          createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString()
        },
        {
          id: 3,
          type: 'login',
          description: '您登录了账号',
          createdAt: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString()
        }
      ];
      loadingActivities.value = false;
    }, 1000);
  } catch (error) {
    console.error('获取活动记录失败:', error);
    loadingActivities.value = false;
  }
};

// 提交个人资料表单
const submitForm = async () => {
  if (!profileFormRef.value) return;
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 调用更新用户信息API
        await userAPI.updateCurrentUser({
          realName: profileForm.realName,
          phone: profileForm.phone
        });
        
        // 更新本地存储的用户信息
        if (authStore.user) {
          authStore.user = {
            ...authStore.user,
            realName: profileForm.realName,
            phone: profileForm.phone
          };
        }
        
        ElMessage.success('个人资料更新成功');
      } catch (error: any) {
        let errorMsg = '更新失败，请稍后再试';
        if (error.response && error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message;
        }
        ElMessage.error(errorMsg);
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  if (profileFormRef.value) {
    profileFormRef.value.resetFields();
    // 恢复初始值
    profileForm.username = userInfo.value?.username || '';
    profileForm.email = userInfo.value?.email || '';
    profileForm.realName = userInfo.value?.realName || '';
    profileForm.phone = userInfo.value?.phone || '';
  }
};

// 显示修改密码对话框
const showChangePasswordDialog = () => {
  passwordDialogVisible.value = true;
  // 清空表单
  passwordForm.currentPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
};

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true;
      try {
        // 调用修改密码API
        await userAPI.changePassword({
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        });
        
        ElMessage.success('密码修改成功');
        passwordDialogVisible.value = false;
      } catch (error: any) {
        let errorMsg = '修改失败，请稍后再试';
        if (error.response && error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message;
        }
        ElMessage.error(errorMsg);
      } finally {
        changingPassword.value = false;
      }
    }
  });
};

// 显示绑定手机对话框
const showBindPhoneDialog = () => {
  phoneDialogVisible.value = true;
  // 清空表单
  phoneForm.phone = '';
  phoneForm.code = '';
};

// 显示修改手机对话框
const showChangePhoneDialog = () => {
  phoneDialogVisible.value = true;
  // 预填当前手机号
  phoneForm.phone = userInfo.value?.phone || '';
  phoneForm.code = '';
};

// 获取验证码
const getVerificationCode = async () => {
  if (!phoneForm.phone) {
    ElMessage.warning('请先输入手机号码');
    return;
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号码');
    return;
  }
  
  try {
    // 调用发送验证码API
    await userAPI.sendVerificationCode(phoneForm.phone);
    
    ElMessage.success('验证码已发送');
    
    // 开始倒计时
    codeCounting.value = true;
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
        codeCounting.value = false;
      }
    }, 1000);
  } catch (error: any) {
    let errorMsg = '发送失败，请稍后再试';
    if (error.response && error.response.data && error.response.data.message) {
      errorMsg = error.response.data.message;
    }
    ElMessage.error(errorMsg);
  }
};

// 绑定手机
const bindPhone = async () => {
  if (!phoneFormRef.value) return;
  
  await phoneFormRef.value.validate(async (valid) => {
    if (valid) {
      bindingPhone.value = true;
      try {
        // 调用绑定手机API
        await userAPI.bindPhone({
          phone: phoneForm.phone,
          code: phoneForm.code
        });
        
        // 更新本地存储的用户信息
        if (authStore.user) {
          authStore.user = {
            ...authStore.user,
            phone: phoneForm.phone
          };
        }
        
        // 更新表单中的手机号
        profileForm.phone = phoneForm.phone;
        
        ElMessage.success('手机绑定成功');
        phoneDialogVisible.value = false;
      } catch (error: any) {
        let errorMsg = '绑定失败，请稍后再试';
        if (error.response && error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message;
        }
        ElMessage.error(errorMsg);
      } finally {
        bindingPhone.value = false;
      }
    }
  });
};

// 头像上传前的处理
const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('头像只能是JPG或PNG格式!');
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过2MB!');
  }
  return isJPG && isLt2M;
};

// 头像上传成功的处理
const handleAvatarSuccess = (response: any) => {
  const avatarUrl = response.data.url;
  // 更新头像
  authStore.updateUserAvatar(avatarUrl);
  ElMessage.success('头像更新成功');
};

// 获取角色名称
const getRoleName = (role: string) => {
  const roleMap: Record<string, string> = {
    'ROLE_ADMIN': '管理员',
    'ROLE_USER': '普通用户',
    'ROLE_MODERATOR': '审核员'
  };
  
  return roleMap[role] || role;
};

// 获取活动类型样式
const getActivityType = (type: string) => {
  switch (type) {
    case 'registration': return 'primary';
    case 'profile': return 'info';
    case 'login': return 'success';
    default: return '';
  }
};
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card, .security-card, .profile-form-card, .activity-card {
  margin-bottom: 20px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.user-avatar {
  margin-bottom: 16px;
  border: 2px solid #f0f2f5;
  background-color: #409EFF;
  color: #fff;
  font-weight: bold;
}

.avatar-actions {
  margin-top: 12px;
}

.user-info {
  text-align: center;
}

.username {
  font-size: 20px;
  margin: 0 0 8px 0;
}

.email {
  color: #909399;
  margin: 0 0 12px 0;
}

.user-roles {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.account-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 24px;
  color: #409EFF;
  font-weight: bold;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-top: 5px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
}

.security-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-label {
  color: #606266;
}

.item-action {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-form {
  margin-top: 10px;
}

.verification-code {
  display: flex;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.loading-container, .empty-container {
  padding: 20px 0;
}

.activity-content {
  margin-bottom: 5px;
}

.activity-event a {
  color: #409EFF;
  text-decoration: none;
}

.activity-event a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .security-card, .profile-form-card, .activity-card {
    margin-top: 20px;
  }
}
</style> 