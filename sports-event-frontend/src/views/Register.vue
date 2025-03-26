<template>
  <div class="flex min-h-screen bg-gray-100">
    <div class="m-auto w-full max-w-md p-8 bg-white rounded-lg shadow-lg">
      <h1 class="text-3xl font-bold text-center mb-6 text-blue-600">
        用户注册
      </h1>
      
      <el-alert v-if="authStore.error" type="error" :title="authStore.error" show-icon class="mb-4" />
      
      <el-form 
        :model="registerForm" 
        :rules="rules" 
        ref="registerFormRef"
        label-position="top" 
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username" 
            prefix-icon="el-icon-user" 
            placeholder="请输入用户名"
          />
        </el-form-item>
        
        <el-form-item label="电子邮箱" prop="email">
          <el-input 
            v-model="registerForm.email" 
            prefix-icon="el-icon-message" 
            placeholder="请输入电子邮箱"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            prefix-icon="el-icon-lock" 
            type="password" 
            placeholder="请输入密码" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            prefix-icon="el-icon-lock" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="用户角色" prop="roles">
          <el-radio-group v-model="registerForm.selectedRole">
            <el-radio label="athlete">运动员</el-radio>
            <el-radio label="spectator">观众</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <div class="flex justify-between mb-4">
          <el-checkbox v-model="agreeTerms" @change="validateAgree">我已阅读并同意用户协议</el-checkbox>
          <router-link to="/login" class="text-blue-600 hover:text-blue-800">
            已有账号？点击登录
          </router-link>
        </div>
        
        <el-button 
          type="primary" 
          class="w-full" 
          :loading="authStore.loading" 
          :disabled="!agreeTerms"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores';
import type { FormInstance, FormRules } from 'element-plus';

// 路由
const router = useRouter();

// 认证状态
const authStore = useAuthStore();

// 表单引用
const registerFormRef = ref<FormInstance>();

// 表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  selectedRole: 'spectator'
});

// 计算属性: 角色数组
const roles = computed(() => {
  return [registerForm.selectedRole];
});

// 同意条款
const agreeTerms = ref(false);

// 验证确认密码
const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

// 验证同意条款
const validateAgree = (value: boolean) => {
  if (!value) {
    ElMessage.warning('请阅读并同意用户协议');
  }
};

// 表单验证规则
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度应在3到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的电子邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度应在6到20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ]
});

// 注册处理函数
const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  if (!agreeTerms.value) {
    ElMessage.warning('请阅读并同意用户协议');
    return;
  }
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      const success = await authStore.register({
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password,
        roles: roles.value
      });
      
      if (success) {
        ElMessage.success('注册成功，请登录');
        router.push('/login');
      }
    } else {
      return false;
    }
  });
};
</script> 