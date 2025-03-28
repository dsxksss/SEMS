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
    // 检查用户是否有管理员角色
    isAdmin: (state) => {
      if (!state.user || !state.user.roles) return false;
      return state.user.roles.includes('ROLE_ADMIN');
    },
    isUser: (state) => {
      if (!state.user || !state.user.roles) return false;
      return state.user.roles.includes('ROLE_USER');
    }
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
          
          console.log('已恢复用户状态:', this.user);
          console.log('用户角色:', this.user.roles);
          console.log('是否管理员:', this.isAdmin);
        } catch (error) {
          // 解析失败，清除存储
          console.error('解析用户数据失败:', error);
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
        console.log('正在登录...');
        const response = await authAPI.login(credentials);
        console.log('登录响应:', response);
        
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
        // 保存刷新token（如果后端提供了）
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        localStorage.setItem('user', JSON.stringify(this.user));
        
        console.log('登录成功, 用户信息:', this.user);
        console.log('用户角色:', this.user.roles);
        console.log('是否管理员:', this.isAdmin);
        
        // 登录成功，根据角色重定向到不同页面
        if (this.isAdmin) {
          console.log('用户是管理员，准备跳转到管理后台...');
          // 确保异步完成后再跳转
          setTimeout(() => {
            router.push('/admin/dashboard');
          }, 100);
        } else {
          console.log('普通用户登录，准备跳转到用户界面...');
          setTimeout(() => {
            router.push('/user/dashboard');
          }, 100);
        }
        
        // 登录成功
        return true;
      } catch (error: any) {
        console.error('登录失败:', error);
        
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
      
      // 清除localStorage
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      
      // 跳转到登录页
      router.push('/login');
    },
    
    // 更新用户头像
    updateUserAvatar(avatarUrl: string) {
      if (this.user) {
        this.user = {
          ...this.user,
          avatar: avatarUrl
        };
        
        // 更新localStorage中的用户信息
        localStorage.setItem('user', JSON.stringify(this.user));
      }
    },
    
    // 刷新用户信息
    async refreshUserInfo() {
      try {
        // 导入用户API
        const { userAPI } = await import('../api/userAPI');
        // 获取最新的用户信息
        const userData = await userAPI.getCurrentUser();
        if (userData) {
          // 确保合并所有用户属性，并转换类型以匹配User接口
          if (this.user) {
            // 将userData转换为兼容的类型
            const updatedUser: User = {
              id: userData.id,
              username: userData.username,
              email: userData.email || '',
              roles: this.user.roles, // 保留原有的roles
              avatar: userData.avatar,
              phone: userData.phone,
              realName: userData.realName
            };
            
            // 更新用户状态
            this.user = {
              ...this.user,
              ...updatedUser
            };
          } else {
            // 如果没有现有用户，尝试转换userData为User类型
            // 注意：这种情况可能缺少roles字段，实际使用中不应发生
            this.user = userData as unknown as User;
          }
          
          // 更新localStorage中的用户信息
          localStorage.setItem('user', JSON.stringify(this.user));
          console.log('用户信息已刷新:', this.user);
        }
        return true;
      } catch (error) {
        console.error('刷新用户信息失败:', error);
        return false;
      }
    },
    
    // 检查token是否已过期
    checkTokenExpiration(showLogs = true) {
      const token = localStorage.getItem('token');
      
      // 如果没有token，则视为已过期
      if (!token) {
        if (showLogs) console.log('没有找到token');
        return true;
      }
      
      try {
        // JWT格式为：header.payload.signature
        const parts = token.split('.');
        if (parts.length !== 3) {
          console.error('Token格式不正确');
          return true;
        }
        
        // 解码payload
        let payload;
        try {
          const base64Url = parts[1];
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
          const jsonPayload = decodeURIComponent(
            atob(base64).split('').map(c => {
              return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join('')
          );
          payload = JSON.parse(jsonPayload);
        } catch (e) {
          console.error('无法解码Token payload:', e);
          return true;
        }
        
        if (showLogs) console.log('Token payload:', payload);
        
        // 检查exp(过期时间)字段
        if (payload.exp) {
          const expirationTime = payload.exp * 1000; // 转换为毫秒
          const currentTime = Date.now();
          
          const timeUntilExpiration = expirationTime - currentTime;
          if (showLogs) {
            console.log(`Token过期时间: ${new Date(expirationTime).toLocaleString()}`);
            console.log(`当前时间: ${new Date(currentTime).toLocaleString()}`);
            console.log(`剩余时间: ${Math.floor(timeUntilExpiration / 1000 / 60)} 分钟`);
          }
          
          // 如果token已过期
          if (currentTime > expirationTime) {
            if (showLogs) console.log('Token已过期');
            return true;
          }
          
          // 如果token快要过期（剩余10分钟内）
          if (timeUntilExpiration < 10 * 60 * 1000) {
            if (showLogs) console.warn('Token即将过期，剩余不到10分钟');
            // 这里可以触发刷新token的逻辑
          }
          
          // token没有过期
          return false;
        }
      } catch (error) {
        console.error('解析Token失败:', error);
        return true;
      }
      
      // 如果没有exp字段或解析失败，保守起见视为已过期
      return true;
    }
  }
}); 