import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  
  // getter
  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')
  const userId = computed(() => user.value?.id || null)
  const username = computed(() => user.value?.username || '')
  
  // actions
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
    // 设置axios全局请求头
    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
  }
  
  function setUser(userData) {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }
  
  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    delete axios.defaults.headers.common['Authorization']
  }
  
  async function login(credentials) {
    try {
      const response = await axios.post('/api/auth/login', credentials)
      const { data } = response.data
      
      setToken(data.token)
      setUser({
        id: data.id,
        username: data.username,
        email: data.email,
        role: data.role
      })
      
      return Promise.resolve(data)
    } catch (error) {
      return Promise.reject(error.response?.data?.message || '登录失败')
    }
  }
  
  async function register(userData) {
    try {
      const response = await axios.post('/api/auth/register', userData)
      const { data } = response.data
      
      setToken(data.token)
      setUser({
        id: data.id,
        username: data.username,
        email: data.email,
        role: data.role
      })
      
      return Promise.resolve(data)
    } catch (error) {
      return Promise.reject(error.response?.data?.message || '注册失败')
    }
  }
  
  function logout() {
    clearAuth()
  }
  
  // 初始化时设置axios全局请求头
  if (token.value) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
  }
  
  return {
    token,
    user,
    isLoggedIn,
    role,
    userId,
    username,
    login,
    register,
    logout,
    setUser
  }
}) 