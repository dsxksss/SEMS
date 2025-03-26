<template>
  <div class="bg-white shadow">
    <div class="container mx-auto px-4">
      <div class="flex justify-between h-16">
        <!-- 左侧Logo和导航 -->
        <div class="flex">
          <div class="flex-shrink-0 flex items-center">
            <router-link to="/" class="text-xl font-bold text-blue-600">SEMS</router-link>
          </div>
          <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
            <router-link to="/" class="nav-link" :class="{ 'active': isActive('/') }">首页</router-link>
            <router-link to="/events" class="nav-link" :class="{ 'active': isActive('/events') }">赛事</router-link>
            <router-link to="/announcements" class="nav-link" :class="{ 'active': isActive('/announcements') }">公告</router-link>
          </div>
        </div>
        
        <!-- 右侧用户区域 -->
        <div class="hidden sm:ml-6 sm:flex sm:items-center">
          <template v-if="authStore.isAuthenticated">
            <el-dropdown trigger="click">
              <span class="el-dropdown-link flex items-center cursor-pointer">
                <el-avatar :size="32" class="mr-2">{{ nameInitial }}</el-avatar>
                {{ authStore.user?.username || '用户' }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="authStore.isAdmin">
                    <router-link to="/admin" class="block w-full">管理控制台</router-link>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <router-link to="/profile" class="block w-full">个人中心</router-link>
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <router-link to="/my-registrations" class="block w-full">我的报名</router-link>
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="text-gray-500 hover:text-blue-600 px-3 py-2 rounded-md">登录</router-link>
            <router-link to="/register" class="ml-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">注册</router-link>
          </template>
        </div>
        
        <!-- 移动端菜单按钮 -->
        <div class="flex items-center sm:hidden">
          <button @click="toggleMobileMenu" class="text-gray-500 hover:text-blue-600 focus:outline-none">
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path v-if="mobileMenuOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 移动端菜单 -->
    <div v-show="mobileMenuOpen" class="sm:hidden">
      <div class="pt-2 pb-3 space-y-1">
        <router-link to="/" class="mobile-nav-link" :class="{ 'active': isActive('/') }" @click="mobileMenuOpen = false">首页</router-link>
        <router-link to="/events" class="mobile-nav-link" :class="{ 'active': isActive('/events') }" @click="mobileMenuOpen = false">赛事</router-link>
        <router-link to="/announcements" class="mobile-nav-link" :class="{ 'active': isActive('/announcements') }" @click="mobileMenuOpen = false">公告</router-link>
      </div>
      <div class="pt-4 pb-3 border-t border-gray-200">
        <div v-if="authStore.isAuthenticated" class="flex items-center px-4">
          <div class="flex-shrink-0">
            <el-avatar :size="40">{{ nameInitial }}</el-avatar>
          </div>
          <div class="ml-3">
            <div class="text-base font-medium text-gray-800">{{ authStore.user?.username || '用户' }}</div>
            <div class="text-sm font-medium text-gray-500">{{ authStore.user?.email }}</div>
          </div>
        </div>
        <div class="mt-3 space-y-1">
          <template v-if="authStore.isAuthenticated">
            <router-link v-if="authStore.isAdmin" to="/admin" class="mobile-nav-link" @click="mobileMenuOpen = false">管理控制台</router-link>
            <router-link to="/profile" class="mobile-nav-link" @click="mobileMenuOpen = false">个人中心</router-link>
            <router-link to="/my-registrations" class="mobile-nav-link" @click="mobileMenuOpen = false">我的报名</router-link>
            <a @click="handleLogout" class="mobile-nav-link cursor-pointer">退出登录</a>
          </template>
          <template v-else>
            <router-link to="/login" class="mobile-nav-link" @click="mobileMenuOpen = false">登录</router-link>
            <router-link to="/register" class="mobile-nav-link" @click="mobileMenuOpen = false">注册</router-link>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores';
import { ArrowDown } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 移动端菜单状态
const mobileMenuOpen = ref(false);

// 导航链接激活状态判断
const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(`${path}/`);
};

// 用户名首字母（用于头像显示）
const nameInitial = computed(() => {
  const username = authStore.user?.username || '';
  return username.charAt(0).toUpperCase();
});

// 切换移动端菜单
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value;
};

// 登出处理
const handleLogout = async () => {
  await authStore.logout();
  mobileMenuOpen.value = false;
  router.push('/');
  ElMessage.success('已成功退出登录');
};
</script>

<style scoped>
.nav-link {
  @apply inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-gray-500 hover:border-blue-300 hover:text-blue-600;
  height: 64px;
}

.nav-link.active {
  @apply border-blue-500 text-blue-600;
}

.mobile-nav-link {
  @apply block pl-3 pr-4 py-2 border-l-4 border-transparent text-base font-medium text-gray-500 hover:bg-gray-50 hover:border-blue-300 hover:text-blue-700;
}

.mobile-nav-link.active {
  @apply bg-blue-50 border-blue-500 text-blue-700;
}
</style>