<template>
  <div class="admin-layout">
    <el-container class="container">
      <!-- 侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <h2>体育赛事管理系统</h2>
        </div>
        <el-menu
          router
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><el-icon-menu /></el-icon>
            <span>系统首页</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/profile">
            <el-icon><el-icon-user /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          
          <el-sub-menu index="/admin/users">
            <template #title>
              <el-icon><el-icon-user-filled /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/users/list">用户列表</el-menu-item>
            <el-menu-item index="/admin/users/roles">角色管理</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/admin/events">
            <template #title>
              <el-icon><el-icon-trophy /></el-icon>
              <span>赛事项目管理</span>
            </template>
            <el-menu-item index="/admin/events/categories">赛事分类</el-menu-item>
            <el-menu-item index="/admin/events/list">赛事列表</el-menu-item>
            <el-menu-item index="/admin/events/create">创建赛事</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/admin/registrations">
            <el-icon><el-icon-document /></el-icon>
            <span>运动员报名管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/results">
            <el-icon><el-icon-data-line /></el-icon>
            <span>赛事结果管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/reports">
            <el-icon><el-icon-document-copy /></el-icon>
            <span>报表汇总打印</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/announcements">
            <el-icon><el-icon-bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container class="main-container">
        <el-header class="header">
          <div class="header-left">
            <el-icon class="toggle-sidebar"><el-icon-fold /></el-icon>
          </div>
          <div class="header-right">
            <el-dropdown>
              <span class="user-dropdown">
                {{ authStore.user?.username }}
                <el-icon><el-icon-arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/admin/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="authStore.logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main-content">
          <!-- 内容页头部 -->
          <div class="page-header" v-if="$route.meta.title">
            <h3>{{ $route.meta.title }}</h3>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.parent">{{ $route.meta.parent }}</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <!-- 路由视图 -->
          <router-view></router-view>
        </el-main>
        <el-footer class="footer">
          &copy; 2023 体育赛事管理系统 - 管理员后台
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.container {
  height: 100%;
  width: 100%;
  display: flex;
}

.main-container {
  flex: 1;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar {
  background-color: #304156;
  color: #bfcbd9;
  height: 100%;
  overflow-y: auto;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #263445;
  color: #fff;
}

.logo h2 {
  font-size: 16px;
  margin: 0;
  font-weight: 500;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-sidebar {
  padding: 0 15px;
  cursor: pointer;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.footer {
  height: 40px;
  line-height: 40px;
  text-align: center;
  background-color: #fff;
  color: #606266;
  font-size: 12px;
  border-top: 1px solid #e6e6e6;
}
</style> 