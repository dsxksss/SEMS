<template>
  <div class="min-h-screen bg-gray-100">
    <div class="flex h-screen overflow-hidden">
      <!-- 侧边栏 -->
      <div class="w-64 bg-[#304156] flex-shrink-0">
        <div class="py-4 px-4 bg-[#263445] text-white">
          <h2 class="text-base font-medium">体育赛事管理系统</h2>
        </div>
        <el-menu
          router
          :default-active="$route.path"
          class="el-menu-vertical border-none h-[calc(100%-4rem)]"
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
      </div>

      <!-- 主要内容区 -->
      <div class="flex flex-col flex-1 overflow-hidden">
        <!-- 顶部导航栏 -->
        <header class="bg-white border-b border-gray-200 shadow-sm h-16 flex items-center px-6 justify-between">
          <div class="flex items-center">
            <el-icon class="text-xl cursor-pointer mr-4"><el-icon-fold /></el-icon>
          </div>
          <div class="flex items-center">
            <el-dropdown>
              <span class="flex items-center cursor-pointer text-sm">
                {{ authStore.user?.username }}
                <el-icon class="ml-1"><el-icon-arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/admin/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="authStore.logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </header>

        <!-- 内容区域 -->
        <main class="flex-1 overflow-y-auto p-6 bg-gray-100">
          <!-- 内容页头部 -->
          <div v-if="$route.meta.title" class="bg-white rounded-md p-4 mb-6 flex justify-between items-center shadow-sm">
            <h3 class="text-lg font-medium text-gray-800">{{ $route.meta.title }}</h3>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.parent">{{ $route.meta.parent }}</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <!-- 路由内容 -->
          <keep-alive>
            <router-view></router-view>
          </keep-alive>
        </main>

        <!-- 底部 -->
        <footer class="bg-white border-t border-gray-200 py-3 px-6 text-center text-xs text-gray-500">
          &copy; 2023 体育赛事管理系统 - 管理员后台
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();
</script>

<style>
/* 覆盖Element Plus菜单样式 */
.el-menu {
  border-right: none !important;
}

/* 确保页面占满整个视口 */
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* 侧边栏优化 */
.el-menu-vertical {
  background-color: #304156 !important;
  transition: width 0.3s;
}

.el-menu-item {
  height: 50px !important;
  line-height: 50px !important;
  border-left: 3px solid transparent;
  transition: all 0.3s;
}

.el-menu-item.is-active {
  background-color: #263445 !important;
  color: #409EFF !important;
  border-left: 3px solid #409EFF;
}

.el-menu-item:hover {
  background-color: #263445 !important;
}

.el-sub-menu__title {
  height: 50px !important;
  line-height: 50px !important;
  transition: all 0.3s;
}

.el-sub-menu__title:hover {
  background-color: #263445 !important;
}

/* 面包屑导航 */
.el-breadcrumb__inner {
  color: #909399 !important;
  font-weight: normal !important;
}

.el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #303133 !important;
  font-weight: 500 !important;
}

/* 卡片样式 */
.el-card {
  border-radius: 8px !important;
  overflow: hidden;
  transition: all 0.3s;
  border: none !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
}

.el-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
  transform: translateY(-2px);
}

/* 按钮样式 */
.el-button {
  font-weight: 500 !important;
  border-radius: 4px !important;
  transition: all 0.3s !important;
}

.el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3) !important;
}

/* 表格样式 */
.el-table {
  border-radius: 8px !important;
  overflow: hidden !important;
}

.el-table th {
  background-color: #f5f7fa !important;
  color: #606266 !important;
  font-weight: 600 !important;
  padding: 12px 0 !important;
}

/* 标签样式 */
.el-tag {
  border-radius: 4px !important;
  padding: 0 8px !important;
}

/* 下拉菜单 */
.el-dropdown-menu {
  border-radius: 8px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
}

.el-dropdown-menu__item {
  padding: 10px 20px !important;
}

.el-dropdown-menu__item:hover {
  background-color: #ecf5ff !important;
}
</style> 