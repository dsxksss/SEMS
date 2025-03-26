<template>
  <div class="flex min-h-screen bg-gray-100">
    <div class="m-auto w-full max-w-md p-8 bg-white rounded-lg shadow-lg">
      <h1 class="text-3xl font-bold text-center mb-6 text-blue-600">
        登录系统
      </h1>
      
      <el-alert v-if="authStore.error" type="error" :title="authStore.error" show-icon class="mb-4" />
      
      <el-form 
        :model="loginForm" 
        :rules="rules" 
        ref="loginFormRef"
        label-position="top" 
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            prefix-icon="el-icon-user" 
            placeholder="请输入用户名"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            prefix-icon="el-icon-lock" 
            type="password" 
            placeholder="请输入密码" 
            show-password
          />
        </el-form-item>
        
        <div class="flex justify-between mb-4">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <router-link to="/register" class="text-blue-600 hover:text-blue-800">
            没有账号？点击注册
          </router-link>
        </div>
        
        <el-button 
          type="primary" 
          class="w-full" 
          :loading="authStore.loading" 
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
      
      <div class="mt-4 text-center text-gray-500 text-sm">
        <p>使用以下账号进行测试：</p>
        <div class="grid grid-cols-3 gap-2 mt-2">
          <div class="bg-gray-100 p-2 rounded">
            <div class="font-bold">管理员</div>
            <div>admin</div>
            <div class="text-xs">password</div>
          </div>
          <div class="bg-gray-100 p-2 rounded">
            <div class="font-bold">运动员</div>
            <div>athlete</div>
            <div class="text-xs">password</div>
          </div>
          <div class="bg-gray-100 p-2 rounded">
            <div class="font-bold">观众</div>
            <div>spectator</div>
            <div class="text-xs">password</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores';
import type { FormInstance, FormRules } from 'element-plus';

// 路由
const router = useRouter();
const route = useRoute();

// 认证状态
const authStore = useAuthStore();

// 表单引用
const loginFormRef = ref<FormInstance>();

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
});

// 记住我
const rememberMe = ref(false);

// 表单验证规则
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度应在3到20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度应在6到20个字符之间', trigger: 'blur' }
  ]
});

// 登录处理函数
const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      const success = await authStore.login(loginForm);
      
      if (success) {
        // 获取重定向地址，如果没有则跳转首页
        const redirectPath = route.query.redirect as string || '/';
        router.push(redirectPath);
        
        ElMessage.success('登录成功');
      }
    } else {
      return false;
    }
  });
};
</script> 