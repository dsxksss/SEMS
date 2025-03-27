<template>
  <div class="user-layout">
    <el-container class="layout-container">
      <el-header class="layout-header">
        <div class="header-content">
          <div class="header-left">
            <router-link to="/user/dashboard" class="logo-link">
              <h1 class="site-title">体育赛事管理系统</h1>
            </router-link>
          </div>
          
          <div class="header-right">
            <el-dropdown trigger="click" class="user-dropdown">
              <div class="user-info">
                <el-avatar 
                  size="small" 
                  class="user-avatar" 
                  :src="authStore.user?.avatar || ''"
                >
                  {{ authStore.user?.username?.substring(0, 1).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ authStore.user?.username }}</span>
                <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
              </div>
              
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-container class="main-container">
        <el-aside width="220px" class="layout-aside">
          <el-menu
            :default-active="activeMenu"
            class="layout-menu custom-menu"
            router
          >
            <el-menu-item index="/user/dashboard">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <el-menu-item index="/user/events">
              <el-icon><Calendar /></el-icon>
              <span>赛事列表</span>
            </el-menu-item>
            
            <el-menu-item index="/user/registrations">
              <el-icon><DocumentChecked /></el-icon>
              <span>我的报名</span>
            </el-menu-item>
            
            <el-menu-item index="/user/announcements">
              <el-icon><Bell /></el-icon>
              <span>公告列表</span>
            </el-menu-item>
            
            <el-menu-item index="/user/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
      
      <el-footer class="layout-footer">
        <div class="footer-content">
          <p>&copy; 2023 体育赛事管理系统</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { 
  House, 
  Calendar, 
  Bell, 
  CaretBottom, 
  SwitchButton,
  DocumentChecked,
  User
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// 当前激活菜单
const activeMenu = computed(() => {
  // 获取当前路由路径作为激活菜单
  return route.path;
});

// 退出登录
const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-container {
  height: 100vh;
}

.layout-header {
  background-color: #409EFF;
  color: white;
  padding: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.logo-link {
  text-decoration: none;
  color: inherit;
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background-color: #ffffff;
  color: #409EFF;
}

.dropdown-icon {
  font-size: 12px;
  margin-left: 4px;
}

.main-container {
  flex: 1;
  height: calc(100vh - 60px - 50px); /* 减去header和footer的高度 */
}

.layout-aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  overflow-y: auto;
  height: 100%;
}

.layout-menu {
  border-right: none;
}

/* 自定义菜单样式 */
:deep(.custom-menu .el-menu-item.is-active) {
  background-color: #f5f7fa !important; /* 选中时的背景色为淡灰色 */
  color: #409EFF; /* 保持文字颜色 */
}

:deep(.custom-menu .el-menu-item:hover) {
  background-color: #f5f7fa !important; /* 悬停时的背景色为淡灰色 */
}

.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.layout-footer {
  background-color: #f5f7fa;
  color: #606266;
  height: 50px;
  padding: 0;
  border-top: 1px solid #e6e6e6;
}

.footer-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .layout-aside {
    width: 64px !important;
  }
  
  .site-title {
    display: none;
  }
  
  .username {
    display: none;
  }
}
</style> 