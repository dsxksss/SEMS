import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import { useAppStore } from './app'
import authApi from '@/api/modules/auth'

export const useUserStore = defineStore('user', () => {
  const appStore = useAppStore()
  
  // 用户状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  
  // 设置认证令牌
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
    
    // 设置 axios 默认头
    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
  }
  
  // 移除认证令牌
  function removeToken() {
    token.value = ''
    localStorage.removeItem('token')
    delete axios.defaults.headers.common['Authorization']
  }
  
  // 设置用户信息
  function setUser(userData) {
    user.value = userData
  }
  
  // 加载用户信息
  async function loadUser() {
    try {
      if (token.value) {
        const response = await axios.get('/user/profile')
        setUser(response.data)
      }
    } catch (error) {
      console.error('加载用户信息失败:', error)
      logout()
    }
  }
  
  // 用户登录
  async function login(credentials) {
    try {
      const response = await authApi.login(credentials)
      const { token: newToken, user: userData } = response
      
      setToken(newToken)
      setUser(userData)
      
      return true
    } catch (error) {
      const message = error.response?.data?.message || '登录失败，请检查用户名和密码'
      appStore.showError(message)
      return false
    }
  }
  
  // 用户注册
  async function register(userInfo) {
    try {
      const response = await authApi.register(userInfo)
      const { token: newToken, user: userData } = response
      
      setToken(newToken)
      setUser(userData)
      
      return true
    } catch (error) {
      const message = error.response?.data?.message || '注册失败，请稍后重试'
      appStore.showError(message)
      return false
    }
  }
  
  // 用户登出
  async function logout() {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('登出时发生错误:', error)
    } finally {
      removeToken()
      setUser(null)
    }
  }
  
  // 更新用户信息
  async function updateProfile(profileData) {
    try {
      const response = await axios.put('/user/profile', profileData)
      setUser(response.data)
      appStore.showError('个人资料更新成功', 'success')
      return true
    } catch (error) {
      const message = error.response?.data?.message || '更新个人资料失败'
      appStore.showError(message)
      return false
    }
  }
  
  // 更改密码
  async function changePassword(passwordData) {
    try {
      await axios.put('/user/change-password', passwordData)
      appStore.showError('密码修改成功', 'success')
      return true
    } catch (error) {
      const message = error.response?.data?.message || '密码修改失败'
      appStore.showError(message)
      return false
    }
  }
  
  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    loadUser,
    updateProfile,
    changePassword
  }
}) 