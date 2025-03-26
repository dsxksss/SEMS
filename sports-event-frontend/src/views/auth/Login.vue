<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import AuthLayout from '../../components/AuthLayout.vue';

// 获取路由和认证存储
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
});

// 加载状态和表单引用
const loading = ref(false);
const formRef = ref();

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ]
};

// 处理登录
const handleLogin = async (formEl: any) => {
  if (!formEl) return;
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true;
      
      try {
        const success = await authStore.login({
          username: loginForm.username.trim(),
          password: loginForm.password
        });
        
        if (success) {
          // 成功登录，显示成功消息
          ElMessage.success('登录成功');
          
          // 如果有重定向地址，则跳转到该地址
          const redirectPath = route.query.redirect as string || '/';
          router.replace(redirectPath);
        } else {
          // 登录失败，显示错误消息
          ElMessage.error(authStore.error || '登录失败，请检查用户名和密码');
        }
      } catch (error: any) {
        // 处理异常情况
        console.error('登录失败:', error);
        ElMessage.error('登录请求失败，请检查网络连接或服务器状态');
      } finally {
        loading.value = false;
      }
    }
  });
};

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register');
};
</script>

<template>
  <AuthLayout title="欢迎回来">
    <div class="login-form-wrapper">
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin(formRef)"
        size="large"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username" class="form-item">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入您的用户名"
            :disabled="loading"
            :prefix-icon="User"
            class="form-input"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" class="form-item">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入您的密码"
            :disabled="loading"
            show-password
            :prefix-icon="Lock"
            class="form-input"
          />
        </el-form-item>
        
        <div class="remember-forgot">
          <el-checkbox>记住我</el-checkbox>
          <el-button link type="primary" class="forgot-link">忘记密码？</el-button>
        </div>
        
        <div class="form-actions">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin(formRef)"
            class="login-button"
            round
          >
            登录
          </el-button>
        </div>
        
        <div class="divider">
          <span>或</span>
        </div>
        
        <div class="form-footer">
          <span>还没有账号？</span>
          <el-button link type="primary" @click="goToRegister" class="register-link">立即注册</el-button>
        </div>
      </el-form>
    </div>
  </AuthLayout>
</template>

<style scoped>
.login-form-wrapper {
  animation: fadeIn 0.6s ease-out;
  width: 100%;
}

.login-form {
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

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.forgot-link {
  font-size: 14px;
  padding: 0;
}

.form-actions {
  margin-top: 28px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, var(--primary-color) 0%, #1989fa 100%);
  border: none;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  opacity: 0.9;
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

.register-link {
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