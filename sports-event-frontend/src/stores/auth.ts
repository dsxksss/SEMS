import { defineStore } from 'pinia';
import { authAPI, type User, type LoginRequest, type SignupRequest } from '../api/authAPI';
import router from '../router';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: null,
    isAuthenticated: false,
    loading: false,
    error: null
  }),
  
  getters: {
    isAdmin: (state) => state.user?.roles.includes('ROLE_ADMIN') || false,
    isUser: (state) => state.user?.roles.includes('ROLE_USER') || false
  },
  
  actions: {
    // 初始化用户状态（从本地存储恢复）
    async initializeUser() {
      const token = localStorage.getItem('token');
      const userStr = localStorage.getItem('user');
      
      if (token && userStr) {
        try {
          const user = JSON.parse(userStr) as User;
          this.user = user;
          this.token = token;
          this.isAuthenticated = true;
        } catch (error) {
          // 解析失败，清除存储
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      }
    },
    
    // 用户登录
    async login(credentials: LoginRequest) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await authAPI.login(credentials);
        
        // 保存认证信息
        this.user = {
          id: response.id,
          username: response.username,
          email: response.email,
          roles: response.roles
        };
        this.token = response.token;
        this.isAuthenticated = true;
        
        // 存储到localStorage
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(this.user));
        
        // 登录成功
        return true;
      } catch (error: any) {
        // 处理具体的错误类型
        if (error.response) {
          const status = error.response.status;
          const data = error.response.data;
          
          if (status === 401) {
            this.error = '用户名或密码错误';
          } else if (status === 400 && data.message) {
            this.error = data.message;
          } else {
            this.error = '登录失败，请稍后再试';
          }
        } else {
          this.error = '网络错误，请检查您的连接';
        }
        
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    // 用户注册
    async register(userData: SignupRequest) {
      this.loading = true;
      this.error = null;
      
      try {
        await authAPI.register(userData);
        // 注册成功
        return true;
      } catch (error: any) {
        // 处理具体的错误类型
        if (error.response) {
          const status = error.response.status;
          const data = error.response.data;
          
          if (status === 400 && data.message) {
            this.error = data.message;
          } else if (status === 409) {
            this.error = '用户名或邮箱已被使用';
          } else {
            this.error = '注册失败，请稍后再试';
          }
        } else {
          this.error = '网络错误，请检查您的连接';
        }
        
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    // 用户退出
    logout() {
      authAPI.logout();
      this.user = null;
      this.token = null;
      this.isAuthenticated = false;
      
      // 跳转到登录页
      router.push('/login');
    }
  }
}); 