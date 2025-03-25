<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-white py-12 px-4 sm:px-6 lg:px-8">
    <div class="w-full max-w-md">
      <div class="card shadow-xl animate-fade-in p-8">
        <!-- 标题 -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-900">欢迎回来</h2>
          <p class="mt-2 text-gray-600">登录您的账号以继续</p>
        </div>

        <!-- 表单 -->
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 用户名输入 -->
          <div>
            <label for="username" class="form-label">用户名</label>
            <input
              id="username"
              v-model="form.username"
              name="username"
              type="text"
              required
              class="form-input w-full"
              placeholder="请输入用户名"
              :disabled="isSubmitting"
            />
            <p v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</p>
          </div>

          <!-- 密码输入 -->
          <div>
            <div class="flex justify-between items-center">
              <label for="password" class="form-label">密码</label>
              <router-link to="/forgot-password" class="text-sm text-primary-600 hover:text-primary-500">忘记密码?</router-link>
            </div>
            <input
              id="password"
              v-model="form.password"
              name="password"
              type="password"
              required
              class="form-input w-full"
              placeholder="请输入密码"
              :disabled="isSubmitting"
            />
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
          </div>

          <!-- 记住我 -->
          <div class="flex items-center">
            <input
              id="remember-me"
              v-model="form.rememberMe"
              name="remember-me"
              type="checkbox"
              class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              :disabled="isSubmitting"
            />
            <label for="remember-me" class="ml-2 block text-sm text-gray-700">记住我</label>
          </div>

          <!-- 登录按钮 -->
          <div>
            <button
              type="submit"
              class="btn-primary w-full py-3"
              :disabled="isSubmitting"
            >
              <span v-if="isSubmitting" class="flex items-center justify-center">
                <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                登录中...
              </span>
              <span v-else>登录</span>
            </button>
          </div>
        </form>

        <!-- 注册链接 -->
        <div class="mt-6 text-center">
          <p class="text-sm text-gray-600">
            还没有账号?
            <router-link to="/register" class="text-primary-600 hover:text-primary-500 font-medium">
            立即注册
          </router-link>
        </p>
      </div>

        <!-- 社交登录 -->
        <div class="mt-8">
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-gray-500">其他登录方式</span>
            </div>
          </div>

          <div class="mt-6 grid grid-cols-3 gap-3">
            <button 
              type="button"
              class="flex justify-center items-center py-2 px-4 rounded-lg border border-gray-300 bg-white hover:bg-gray-50 transition-colors"
              :disabled="isSubmitting"
              @click="handleSocialLogin('facebook')"
            >
              <svg class="h-5 w-5 text-[#1877F2]" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path fill-rule="evenodd" d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z" clip-rule="evenodd"></path>
              </svg>
            </button>
            <button 
              type="button"
              class="flex justify-center items-center py-2 px-4 rounded-lg border border-gray-300 bg-white hover:bg-gray-50 transition-colors"
              :disabled="isSubmitting"
              @click="handleSocialLogin('twitter')"
            >
              <svg class="h-5 w-5 text-[#1DA1F2]" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M8.29 20.251c7.547 0 11.675-6.253 11.675-11.675 0-.178 0-.355-.012-.53A8.348 8.348 0 0022 5.92a8.19 8.19 0 01-2.357.646 4.118 4.118 0 001.804-2.27 8.224 8.224 0 01-2.605.996 4.107 4.107 0 00-6.993 3.743 11.65 11.65 0 01-8.457-4.287 4.106 4.106 0 001.27 5.477A4.072 4.072 0 012.8 9.713v.052a4.105 4.105 0 003.292 4.022 4.095 4.095 0 01-1.853.07 4.108 4.108 0 003.834 2.85A8.233 8.233 0 012 18.407a11.616 11.616 0 006.29 1.84"></path>
              </svg>
            </button>
            <button 
              type="button"
              class="flex justify-center items-center py-2 px-4 rounded-lg border border-gray-300 bg-white hover:bg-gray-50 transition-colors"
              :disabled="isSubmitting"
              @click="handleSocialLogin('google')"
            >
              <svg class="h-5 w-5 text-[#EA4335]" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path fill-rule="evenodd" d="M12 22a10 10 0 110-20 10 10 0 010 20zm0-7.143a2.857 2.857 0 100-5.714 2.857 2.857 0 000 5.714z" clip-rule="evenodd"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const isSubmitting = ref(false)
const errors = reactive({
  username: '',
  password: ''
})

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证
const validateForm = () => {
  let isValid = true
  errors.username = ''
  errors.password = ''
  
  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }
  
  if (!form.password.trim()) {
    errors.password = '请输入密码'
    isValid = false
  }
  
  return isValid
}

// 登录提交
const handleSubmit = async () => {
  if (isSubmitting.value) return
  
  if (!validateForm()) return
  
  try {
    isSubmitting.value = true
    appStore.setLoading(true, '登录中...')
    
    const result = await userStore.login({
      username: form.username,
      password: form.password,
      rememberMe: form.rememberMe
    })
    
    if (result) {
      appStore.showError('登录成功')
  router.push('/')
    }
  } catch (error) {
    console.error('登录失败', error)
    if (error.response?.status === 400) {
      errors.username = '用户名或密码错误'
    }
  } finally {
    isSubmitting.value = false
    appStore.setLoading(false)
  }
}

// 社交登录处理
const handleSocialLogin = async (platform) => {
  try {
    appStore.setLoading(true, `通过${platform}登录中...`)
    // TODO: 实现社交登录功能
    appStore.showError('社交登录功能尚未实现')
  } catch (error) {
    console.error('社交登录失败', error)
    appStore.showError('社交登录失败，请稍后重试')
  } finally {
    appStore.setLoading(false)
  }
}
</script> 