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
          
          // 验证token是否已过期
          if (this.checkTokenExpiration(false)) {
            console.log('Token已过期，尝试刷新...');
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
              try {
                await authAPI.refreshToken(refreshToken);
                console.log('Token刷新成功');
              } catch (error) {
                console.error('Token刷新失败，将退出登录');
                this.logout();
                return;
              }
            } else {
              console.log('无刷新Token，将退出登录');
              this.logout();
              return;
            }
          }
        } catch (error) {
          // 解析失败，清除存储
          console.error('解析用户数据失败:', error);
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
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
        
        // 由于已在authAPI中保存了token和用户信息，只需更新状态
        this.user = {
          id: response.id,
          username: response.username,
          email: response.email,
          roles: response.roles
        };
        this.token = response.token;
        this.isAuthenticated = true;
        
        // 登录成功，根据角色重定向
        if (this.isAdmin) {
          router.push('/admin/dashboard');
        } else {
          router.push('/user/dashboard');
        }
        
        return true;
      } catch (error: any) {
        // 处理登录错误
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
        return true;
      } catch (error: any) {
        // 处理注册错误
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
    async logout() {
      try {
        await authAPI.logout();
      } catch (error) {
        console.error('退出登录时发生错误:', error);
      } finally {
        this.user = null;
        this.token = null;
        this.isAuthenticated = false;
        router.push('/login');
      }
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
        const { userAPI } = await import('../api/userAPI');
        const userData = await userAPI.getCurrentUser();
        if (userData) {
          // 确保合并所有用户属性
          if (this.user) {
            const updatedUser: User = {
              id: userData.id,
              username: userData.username,
              email: userData.email || '',
              roles: this.user.roles, // 保留原有的roles
              avatar: userData.avatar,
              phone: userData.phone,
              realName: userData.realName
            };
            
            this.user = {
              ...this.user,
              ...updatedUser
            };
            
            // 更新localStorage中的用户信息
            localStorage.setItem('user', JSON.stringify(this.user));
          }
        }
        return true;
      } catch (error) {
        console.error('刷新用户信息失败:', error);
        return false;
      }
    },
    
    // 检查token是否已过期
    checkTokenExpiration(showLogs = false) {
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
        const base64Url = parts[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
          atob(base64).split('').map(c => {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          }).join('')
        );
        const payload = JSON.parse(jsonPayload);
        
        // 检查exp(过期时间)字段
        if (payload.exp) {
          const expirationTime = payload.exp * 1000; // 转换为毫秒
          const currentTime = Date.now();
          
          // token已过期
          if (currentTime > expirationTime) {
            return true;
          }
          
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