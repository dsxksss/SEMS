<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-white py-12 px-4 sm:px-6 lg:px-8">
    <div class="w-full max-w-md">
      <div class="card shadow-xl animate-fade-in p-8">
        <!-- 标题 -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-900">创建新账号</h2>
          <p class="mt-2 text-gray-600">填写信息以完成注册</p>
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
              placeholder="请设置用户名"
              :disabled="isSubmitting"
            />
            <p v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</p>
          </div>

          <!-- 邮箱输入 -->
          <div>
            <label for="email" class="form-label">电子邮箱</label>
            <input
              id="email"
              v-model="form.email"
            name="email"
            type="email"
            required
              class="form-input w-full"
            placeholder="请输入电子邮箱"
              :disabled="isSubmitting"
            />
            <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
          </div>

          <!-- 密码输入 -->
          <div>
            <label for="password" class="form-label">密码</label>
            <input
              id="password"
              v-model="form.password"
            name="password"
            type="password"
            required
              class="form-input w-full"
              placeholder="请设置密码"
              :disabled="isSubmitting"
            />
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
          </div>

          <!-- 确认密码 -->
          <div>
            <label for="confirmPassword" class="form-label">确认密码</label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
            name="confirmPassword"
            type="password"
            required
              class="form-input w-full"
            placeholder="请再次输入密码"
              :disabled="isSubmitting"
            />
            <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-600">{{ errors.confirmPassword }}</p>
          </div>

          <!-- 用户协议 -->
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input
                id="terms"
                v-model="form.acceptTerms"
                name="terms"
                type="checkbox"
            required
                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                :disabled="isSubmitting"
              />
            </div>
            <div class="ml-3 text-sm">
              <label for="terms" class="text-gray-700">
                我已阅读并同意
                <a href="#" class="text-primary-600 hover:text-primary-500">用户协议</a>
                和
                <a href="#" class="text-primary-600 hover:text-primary-500">隐私政策</a>
              </label>
              <p v-if="errors.acceptTerms" class="mt-1 text-sm text-red-600">{{ errors.acceptTerms }}</p>
            </div>
          </div>

          <!-- 注册按钮 -->
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
                注册中...
              </span>
              <span v-else>注册</span>
            </button>
          </div>
        </form>

        <!-- 登录链接 -->
        <div class="mt-6 text-center">
          <p class="text-sm text-gray-600">
            已有账号?
            <router-link to="/login" class="text-primary-600 hover:text-primary-500 font-medium">
              返回登录
            </router-link>
          </p>
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
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: ''
})

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false
})

const validateForm = () => {
  errors.username = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''
  errors.acceptTerms = ''
  
  let isValid = true
  
  if (!form.username || form.username.length < 3) {
    errors.username = '用户名至少需要3个字符'
    isValid = false
  }
  
  if (!form.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '请输入有效的电子邮箱地址'
    isValid = false
  }
  
  if (!form.password || form.password.length < 6) {
    errors.password = '密码至少需要6个字符'
    isValid = false
  }
  
  if (form.confirmPassword !== form.password) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }
  
  if (!form.acceptTerms) {
    errors.acceptTerms = '请阅读并同意用户协议和隐私政策'
    isValid = false
  }
  
  return isValid
}

const handleSubmit = async () => {
  if (isSubmitting.value) return
  
  if (!validateForm()) return
  
  try {
    isSubmitting.value = true
    appStore.setLoading(true, '注册中...')
    
    // 准备注册数据
    const userData = {
      username: form.username,
      email: form.email,
      password: form.password
    }
    
    const result = await userStore.register(userData)
    
    if (result) {
      appStore.showError('注册成功')
      router.push('/')
    }
  } catch (error) {
    console.error('注册失败', error)
    
    // 处理各种错误情况
    if (error.response) {
      const { status, data } = error.response
      
      if (status === 400) {
        if (data.message) {
          if (data.message.includes('用户名')) {
            errors.username = data.message
          } else if (data.message.includes('邮箱')) {
            errors.email = data.message
          } else if (data.message.includes('密码')) {
            errors.password = data.message
          } else {
            appStore.showError(data.message)
          }
        } else {
          appStore.showError('注册数据验证失败，请检查填写内容')
        }
      } else {
        appStore.showError('注册失败，请稍后重试')
      }
    } else {
      appStore.showError('注册失败，请检查网络连接')
    }
  } finally {
    isSubmitting.value = false
    appStore.setLoading(false)
  }
}
</script> 