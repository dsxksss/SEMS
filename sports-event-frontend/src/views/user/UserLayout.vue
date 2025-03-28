<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶部导航栏 -->
    <header class="bg-white shadow-sm">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <router-link to="/user/dashboard" class="flex items-center gap-2 text-gray-800 no-underline">
          <el-icon size="24" class="text-indigo-600"><Trophy /></el-icon>
          <h1 class="text-xl font-bold m-0">体育赛事管理系统</h1>
        </router-link>
        
        <el-dropdown trigger="click" class="cursor-pointer">
          <div class="flex items-center gap-3 text-gray-700 hover:text-indigo-600 transition-colors duration-300">
            <el-avatar 
              size="small" 
              class="ring-2 ring-white shadow"
              :src="authStore.user?.avatar || ''"
            >
              {{ authStore.user?.username?.substring(0, 1).toUpperCase() }}
            </el-avatar>
            <span class="font-medium">{{ authStore.user?.username }}</span>
            <el-icon><CaretBottom /></el-icon>
          </div>
          
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/user/profile')">
                <div class="flex items-center gap-2">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided @click="logout">
                <div class="flex items-center gap-2 text-red-500">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <div class="container mx-auto px-4 py-6 flex gap-6">
      <!-- 侧边导航 -->
      <aside class="w-56 flex-shrink-0">
        <div class="bg-white rounded-lg shadow-sm overflow-hidden">
          <el-menu
            :default-active="activeMenu"
            class="border-0"
            router
          >
            <el-menu-item index="/user/dashboard" class="!h-14">
              <div class="flex items-center text-base">
                <el-icon size="20" class="mr-3"><House /></el-icon>
                <span>首页</span>
              </div>
            </el-menu-item>
            
            <el-menu-item index="/user/events" class="!h-14">
              <div class="flex items-center text-base">
                <el-icon size="20" class="mr-3"><Calendar /></el-icon>
                <span>赛事列表</span>
              </div>
            </el-menu-item>
            
            <el-menu-item index="/user/registrations" class="!h-14">
              <div class="flex items-center text-base">
                <el-icon size="20" class="mr-3"><DocumentChecked /></el-icon>
                <span>我的报名</span>
              </div>
            </el-menu-item>
            
            <el-menu-item index="/user/announcements" class="!h-14">
              <div class="flex items-center text-base">
                <el-icon size="20" class="mr-3"><Bell /></el-icon>
                <span>公告列表</span>
              </div>
            </el-menu-item>
            
            <el-menu-item index="/user/profile" class="!h-14">
              <div class="flex items-center text-base">
                <el-icon size="20" class="mr-3"><User /></el-icon>
                <span>个人中心</span>
              </div>
            </el-menu-item>
          </el-menu>
        </div>
      </aside>
      
      <!-- 主内容区 -->
      <main class="flex-1 bg-white rounded-lg shadow-sm p-6 min-h-[calc(100vh-12rem)]">
        <router-view />
      </main>
    </div>
    
    <!-- 页脚 -->
    <footer class="py-4 text-center text-gray-500 text-sm mt-6">
      <div class="container mx-auto">
        <p>&copy; {{ new Date().getFullYear() }} 体育赛事管理系统</p>
      </div>
    </footer>
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
  User,
  Trophy
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// 当前激活菜单
const activeMenu = computed(() => {
  return route.path;
});

// 退出登录
const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
:deep(.el-menu-item.is-active) {
  @apply bg-indigo-600 text-white font-medium rounded-md mx-2 my-1 !important;
}

:deep(.el-menu-item) {
  @apply rounded-md mx-2 my-1 hover:bg-gray-100;
}

:deep(.el-menu) {
  @apply p-2;
}
</style> 