import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authAPI, userAPI } from '../api';
import type { AuthResponse, LoginRequest, SignupRequest, User } from '../api/authAPI';

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'));
  const user = ref<User | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 计算属性
  const isAuthenticated = computed(() => !!token.value);
  const isAdmin = computed(() => user.value?.roles.includes('ROLE_ADMIN') ?? false);
  const isAthlete = computed(() => user.value?.roles.includes('ROLE_ATHLETE') ?? false);
  const isSpectator = computed(() => user.value?.roles.includes('ROLE_SPECTATOR') ?? false);
  
  // 初始化用户信息
  async function initializeUser() {
    if (token.value) {
      try {
        const userData = await userAPI.getCurrentUser();
        user.value = userData;
      } catch (err) {
        console.error('Failed to load user data:', err);
        await logout();
      }
    }
  }
  
  // 登录
  async function login(credentials: LoginRequest) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await authAPI.login(credentials);
      token.value = response.token;
      localStorage.setItem('token', response.token);
      
      user.value = {
        id: response.id,
        username: response.username,
        email: response.email,
        roles: response.roles
      };
      
      return true;
    } catch (err: any) {
      error.value = err.response?.data?.message || '登录失败，请检查您的用户名和密码';
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  // 注册
  async function register(userData: SignupRequest) {
    loading.value = true;
    error.value = null;
    
    try {
      await authAPI.register(userData);
      return true;
    } catch (err: any) {
      error.value = err.response?.data?.message || '注册失败，请稍后再试';
      return false;
    } finally {
      loading.value = false;
    }
  }
  
  // 登出
  async function logout() {
    token.value = null;
    user.value = null;
    localStorage.removeItem('token');
    authAPI.logout();
  }
  
  return {
    // 状态
    token,
    user,
    loading,
    error,
    
    // 计算属性
    isAuthenticated,
    isAdmin,
    isAthlete,
    isSpectator,
    
    // 方法
    initializeUser,
    login,
    register,
    logout
  };
}); 