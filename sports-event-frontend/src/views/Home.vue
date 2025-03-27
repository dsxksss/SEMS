<script setup lang="ts">
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
</script>

<template>
  <div class="home-container">
    <header class="header">
      <h1>体育赛事管理系统</h1>
      <div class="user-info">
        <template v-if="authStore.user">
          <div class="flex items-center user-avatar-container">
            <el-avatar 
              size="small" 
              class="mr-2 user-avatar" 
              :src="authStore.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            >
              {{ authStore.user.username?.substring(0, 1).toUpperCase() }}
            </el-avatar>
            <span>欢迎, {{ authStore.user.username }}</span>
          </div>
        </template>
        <el-button type="danger" size="small" @click="authStore.logout">退出登录</el-button>
      </div>
    </header>
    
    <main class="main-content">
      <div class="welcome-message">
        <h2>欢迎使用体育赛事管理系统</h2>
        <p>这里将展示赛事相关的信息和功能。</p>
      </div>
      
      <!-- 占位内容，后续会添加实际功能 -->
      <div class="placeholder-content">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>功能区域</span>
            </div>
          </template>
          <div class="card-body">
            <p>这里将显示体育赛事管理系统的主要功能。</p>
          </div>
        </el-card>
      </div>
    </main>
    
    <footer class="footer">
      <p>&copy; 2023 体育赛事管理系统</p>
    </footer>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #409eff;
  color: white;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-size: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.main-content {
  flex: 1;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.welcome-message {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-message h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.welcome-message p {
  color: #666;
}

.placeholder-content {
  margin-top: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-body {
  padding: 20px 0;
  color: #666;
}

.footer {
  background-color: #f5f7fa;
  color: #606266;
  text-align: center;
  padding: 16px;
  margin-top: auto;
}

/* 添加Tailwind相关类 */
.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.mr-2 {
  margin-right: 0.5rem;
}

/* 用户头像样式 */
.user-avatar-container:hover,
.user-avatar-container:focus {
  outline: none !important;
}

.user-avatar:hover,
.user-avatar:focus {
  outline: none !important;
  box-shadow: none !important;
  border-color: transparent !important;
  border: none !important;
}

.el-avatar:focus {
  outline: none !important;
  box-shadow: none !important;
  border: none !important;
}
</style> 