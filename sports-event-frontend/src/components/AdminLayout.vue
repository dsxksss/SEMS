<template>
  <div class="min-h-screen bg-gray-100">
    <div class="flex h-screen overflow-hidden">
      <!-- 侧边栏 -->
      <div class="bg-[#304156] flex-shrink-0 transition-all duration-300" :class="isCollapse ? 'w-16' : 'w-64'">
        <div class="py-4 px-4 bg-[#263445] text-white overflow-hidden whitespace-nowrap transition-all">
          <h2 class="text-base font-medium">{{ isCollapse ? '系统' : '体育赛事管理系统' }}</h2>
        </div>
        <el-menu
          router
          :default-active="activeMenu"
          class="el-menu-vertical border-none h-[calc(100%-4rem)]"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :unique-opened="true"
          :collapse="isCollapse"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><el-icon-menu /></el-icon>
            <template #title>系统首页</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/profile">
            <el-icon><el-icon-user /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
          
          <el-sub-menu index="users">
            <template #title>
              <el-icon><el-icon-user-filled /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/users/list">用户列表</el-menu-item>
            <el-menu-item index="/admin/users/roles">角色管理</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="events">
            <template #title>
              <el-icon><el-icon-trophy /></el-icon>
              <span>赛事项目管理</span>
            </template>
            <el-menu-item index="/admin/events/categories">赛事分类</el-menu-item>
            <el-menu-item index="/admin/events/list">赛事列表</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/admin/registrations">
            <el-icon><el-icon-document /></el-icon>
            <template #title>运动员报名管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/results">
            <el-icon><el-icon-data-line /></el-icon>
            <template #title>赛事结果管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/reports">
            <el-icon><el-icon-document-copy /></el-icon>
            <template #title>报表汇总打印</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/announcements">
            <el-icon><el-icon-bell /></el-icon>
            <template #title>公告管理</template>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 主要内容区 -->
      <div class="flex flex-col flex-1 overflow-hidden">
        <!-- 顶部导航栏 -->
        <header class="bg-white border-b border-gray-200 shadow-sm h-16 flex items-center px-6 justify-between">
          <div class="flex items-center">
            <div 
              class="collapse-btn text-xl mr-4"
              @click="toggleCollapse"
            >
              <el-icon v-if="isCollapse"><el-icon-expand /></el-icon>
              <el-icon v-else><el-icon-fold /></el-icon>
            </div>
          </div>
          <div class="flex items-center">
            <el-dropdown>
              <span class="flex items-center cursor-pointer text-sm user-avatar-dropdown">
                <el-avatar 
                  size="small" 
                  class="mr-2" 
                  :src="authStore.user?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                >
                  {{ authStore.user?.username?.substring(0, 1).toUpperCase() }}
                </el-avatar>
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
import { useRouter, useRoute } from 'vue-router';
import { computed, ref, onMounted } from 'vue';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

// 菜单折叠状态
const isCollapse = ref(false);

// 切换折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
  // 可以考虑将状态保存到localStorage
  localStorage.setItem('menuCollapsed', String(isCollapse.value));
};

// 确保菜单正确高亮当前路由
const activeMenu = computed(() => {
  // 当前路由路径
  const { path } = route;
  
  // 优先使用路由元信息中的activeMenu属性
  if (route.meta.activeMenu) {
    return route.meta.activeMenu;
  }
  
  // 否则使用当前路径
  return path;
});

// 组件挂载时读取保存的折叠状态
onMounted(() => {
  const savedState = localStorage.getItem('menuCollapsed');
  if (savedState !== null) {
    isCollapse.value = savedState === 'true';
  }
});
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
  padding: 0 !important;
}

/* 菜单折叠状态 */
.el-menu--collapse {
  width: auto !important;
}

.el-menu--collapse .el-menu-item,
.el-menu--collapse .el-sub-menu__title {
  padding-left: 15px !important;
}

/* 菜单项基础样式 */
.el-menu-item {
  height: 56px !important;
  line-height: 56px !important;
  border-left: 4px solid transparent;
  transition: all 0.3s;
  padding-left: 16px !important; /* 统一左侧边距 */
  font-size: 14px !important;
}

/* 菜单项激活状态 */
.el-menu-item.is-active {
  background-color: #1f2d3d !important;
  border-left: 4px solid #409EFF;
  color: rgba(0, 0, 0, 0.8) !important;
  font-weight: 500;
}


/* 子菜单标题样式 */
.el-sub-menu__title {
  height: 56px !important;
  line-height: 56px !important;
  transition: all 0.3s;
  border-left: 4px solid transparent;
  padding-left: 16px !important; /* 统一左侧边距 */
  font-size: 14px !important;
}

/* 子菜单标题悬停状态 */
.el-sub-menu__title:hover {
  background-color: #263445 !important;
  color: #ffffff !important;
}

/* 子菜单激活状态 */
.el-sub-menu.is-active > .el-sub-menu__title {
  color: #f4f4f5 !important;
  border-left: 4px solid #409EFF;
}

.el-menu--inline .el-menu-item {
  background-color: #1f2d3d !important;
  height: 50px !important;
  line-height: 50px !important;
  padding-left: 60px !important; /* 子菜单缩进 */
  font-size: 13px !important;
  margin: 0 !important;
}



.el-menu--inline .el-menu-item:hover {
  background-color: #001528 !important;
}

/* 图标样式统一 */
.el-menu-item .el-icon,
.el-sub-menu__title .el-icon {
  color: inherit;
  font-size: 18px;
  margin-right: 10px;
  width: 24px;
  text-align: center;
  vertical-align: middle;
}

/* 折叠按钮样式 */
.collapse-btn {
  background-color: transparent;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
}

.collapse-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: #409EFF;
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
  background-color: #fff !important;
}

.el-button--primary {
  background-color: #409EFF !important;
  color: #fff !important;
  border-color: #409EFF !important;
}

.el-button--success {
  background-color: #67C23A !important;
  color: #fff !important;
  border-color: #67C23A !important;
}

.el-button--warning {
  background-color: #E6A23C !important;
  color: #fff !important;
  border-color: #E6A23C !important;
}

.el-button--danger {
  background-color: #F56C6C !important;
  color: #fff !important;
  border-color: #F56C6C !important;
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

.user-avatar-dropdown:hover,
.user-avatar-dropdown:focus {
  outline: none !important;
  box-shadow: none !important;
  border-color: transparent !important;
  border: none !important;
}

.user-avatar-dropdown .el-avatar:hover,
.user-avatar-dropdown .el-avatar:focus {
  outline: none !important;
  box-shadow: none !important;
  border-color: transparent !important;
  border: none !important;
}
</style> 