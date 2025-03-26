<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { ElMessage } from 'element-plus';
import { User, Lock, Message } from '@element-plus/icons-vue';
import AuthLayout from '../../components/AuthLayout.vue';

// 获取路由和认证存储
const router = useRouter();
const authStore = useAuthStore();

// 表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

// 加载状态和表单引用
const loading = ref(false);
const formRef = ref();

// 表单验证规则
const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else {
    if (registerForm.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
};

// 处理注册
const handleRegister = async (formEl: any) => {
  if (!formEl) return;
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true;
      
      try {
        // 提交注册（不包含确认密码字段，并添加默认角色）
        const { confirmPassword, ...userData } = registerForm;

        // 使用trim()移除用户名和邮箱的空白字符
        const registerData = {
          ...userData,
          username: userData.username.trim(),
          email: userData.email.trim(),
          roles: ['ROLE_USER'] // 设置默认用户角色
        };

        const success = await authStore.register(registerData);
        
        if (success) {
          ElMessage.success('注册成功，请登录');
          router.push('/login');
        } else {
          ElMessage.error(authStore.error || '注册失败，请检查输入信息');
        }
      } catch (error: any) {
        console.error('注册失败:', error);
        ElMessage.error('注册请求失败，请检查网络连接或服务器状态');
      } finally {
        loading.value = false;
      }
    }
  });
};

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login');
};

const agreeTerms = ref(false);
</script>

<template>
  <AuthLayout title="创建您的账号">
    <div class="register-form-wrapper">
      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="rules"
        label-position="top"
        size="large"
        class="register-form"
      >
        <el-form-item label="用户名" prop="username" class="form-item">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入您的用户名"
            :disabled="loading"
            :prefix-icon="User"
            class="form-input"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email" class="form-item">
          <el-input 
            v-model="registerForm.email" 
            placeholder="请输入您的邮箱"
            :disabled="loading"
            :prefix-icon="Message"
            class="form-input"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" class="form-item">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置您的密码"
            :disabled="loading"
            show-password
            :prefix-icon="Lock"
            class="form-input"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入您的密码"
            :disabled="loading"
            show-password
            :prefix-icon="Lock"
            class="form-input"
          />
        </el-form-item>
        
        <div class="terms-agreement">
          <el-checkbox v-model="agreeTerms">我已阅读并同意</el-checkbox>
          <el-button link type="primary">服务条款</el-button>和
          <el-button link type="primary">隐私政策</el-button>
        </div>
        
        <div class="form-actions">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister(formRef)"
            class="register-button"
            :disabled="!agreeTerms"
            round
          >
            创建账号
          </el-button>
        </div>
        
        <div class="divider">
          <span>或</span>
        </div>
        
        <div class="form-footer">
          <span>已有账号？</span>
          <el-button link type="primary" @click="goToLogin" class="login-link">立即登录</el-button>
        </div>
      </el-form>
    </div>
  </AuthLayout>
</template>

<style scoped>
.register-form-wrapper {
  animation: fadeIn 0.6s ease-out;
  width: 100%;
}

.register-form {
  width: 100%;
}

.form-item {
  margin-bottom: 20px;
}

.form-item :deep(.el-form-item__label) {
  padding-bottom: 8px;
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1;
}

.form-input {
  width: 100%;
}

.form-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--border-color);
  border-radius: 8px;
  padding: 0 15px;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

.form-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

.form-input :deep(.el-input__inner) {
  height: 42px;
  font-size: 14px;
}

.form-input :deep(.el-input__prefix) {
  font-size: 18px;
  color: var(--text-secondary);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.terms-agreement {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.terms-agreement .el-button {
  padding: 0 4px;
  font-size: 14px;
}

.form-actions {
  margin-top: 28px;
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, var(--primary-color) 0%, #1989fa 100%);
  border: none;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  opacity: 0.9;
}

.register-button:disabled {
  background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  opacity: 0.9;
  cursor: not-allowed;
}

.divider {
  position: relative;
  text-align: center;
  margin: 24px 0 20px;
}

.divider::before, 
.divider::after {
  content: "";
  position: absolute;
  top: 50%;
  width: calc(50% - 30px);
  height: 1px;
  background-color: var(--border-light);
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  display: inline-block;
  padding: 0 10px;
  background-color: #fff;
  color: var(--text-secondary);
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.form-footer {
  margin-top: 16px;
  text-align: center;
  color: var(--text-secondary);
}

.login-link {
  font-weight: 500;
  font-size: 15px;
}

:deep(.el-form-item.is-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--danger-color) !important;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
  font-size: 12px;
}
</style> 