<template>
  <header class="sticky top-0 z-50 bg-white backdrop-blur-md bg-opacity-90 shadow-nav">
    <!-- 桌面导航 -->
    <div class="container mx-auto px-4 py-3">
      <div class="flex justify-between items-center">
        <!-- Logo -->
        <router-link to="/" class="flex items-center space-x-2">
          <div class="flex items-center justify-center w-10 h-10 rounded-lg bg-gradient-to-r from-primary-600 to-primary-500 text-white">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <span class="text-xl font-bold text-gray-900">体育赛事管理</span>
        </router-link>
        
        <!-- 导航菜单 -->
        <nav class="hidden md:flex items-center space-x-8">
          <router-link 
            v-for="item in navItems" 
            :key="item.path" 
            :to="item.path" 
            class="relative font-medium text-gray-700 hover:text-primary-600 transition-colors duration-200"
            active-class="text-primary-600 after:absolute after:bottom-[-8px] after:left-0 after:w-full after:h-0.5 after:bg-primary-600 after:rounded-full"
          >
            {{ item.name }}
          </router-link>
        </nav>
        
        <!-- 用户菜单 -->
        <div class="flex items-center space-x-6">
          <!-- 搜索按钮 -->
          <button class="hidden sm:flex items-center justify-center text-gray-500 hover:text-gray-700 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
          
          <!-- 未登录显示登录/注册按钮 -->
          <template v-if="!userStore.isLoggedIn">
            <router-link to="/login" class="hidden sm:block font-medium text-gray-700 hover:text-primary-600 transition-colors">
              登录
            </router-link>
            <router-link to="/register" class="btn-primary py-2">
              注册
            </router-link>
          </template>
          
          <!-- 已登录显示用户菜单 -->
          <el-dropdown v-else @command="handleCommand" trigger="click">
            <div class="flex items-center cursor-pointer">
              <div class="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center text-primary-600 font-semibold border-2 border-white shadow-sm">
                {{ userStore.username?.charAt(0).toUpperCase() || '用' }}
              </div>
              <div class="hidden sm:block ml-2">
                <p class="text-sm font-medium text-gray-900">{{ userStore.nickname || userStore.username }}</p>
                <p class="text-xs text-gray-500">{{ roleLabel }}</p>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" class="flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.role === 'ADMIN'" command="admin" class="flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="flex items-center text-red-500">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <!-- 移动端菜单按钮 -->
          <button @click="isMobileMenuOpen = !isMobileMenuOpen" class="md:hidden flex items-center justify-center w-10 h-10 rounded-lg text-gray-500 hover:bg-gray-100">
            <svg v-if="!isMobileMenuOpen" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 移动端下拉菜单 -->
    <transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 -translate-y-4"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-4"
    >
      <div v-if="isMobileMenuOpen" class="md:hidden bg-white border-t border-gray-200 shadow-lg">
        <div class="container mx-auto px-4 py-2 space-y-1">
          <router-link 
            v-for="item in navItems" 
            :key="item.path" 
            :to="item.path" 
            class="block py-3 px-4 rounded-lg hover:bg-gray-100 font-medium text-gray-800"
            @click="isMobileMenuOpen = false"
            active-class="bg-primary-50 text-primary-600"
          >
            {{ item.name }}
          </router-link>
          
          <div v-if="!userStore.isLoggedIn" class="pt-2 border-t border-gray-200 mt-2">
            <router-link to="/login" class="block py-3 px-4 rounded-lg hover:bg-gray-100 font-medium text-gray-800" @click="isMobileMenuOpen = false">
              登录
            </router-link>
            <router-link to="/register" class="block py-3 px-4 rounded-lg hover:bg-gray-100 font-medium text-gray-800" @click="isMobileMenuOpen = false">
              注册
            </router-link>
          </div>
        </div>
      </div>
    </transition>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const isMobileMenuOpen = ref(false)

// 导航项
const navItems = [
  { name: '首页', path: '/' },
  { name: '赛事信息', path: '/events' },
  { name: '公告信息', path: '/announcements' }
]

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已成功退出登录')
      router.push('/')
      break
  }
}

// 角色显示名称
const roleLabel = computed(() => {
  const roleMap = {
    'ADMIN': '管理员',
    'USER': '普通用户',
    'ATHLETE': '运动员'
  }
  return roleMap[userStore.role] || '用户'
})
</script>

<style scoped>
.nav-link {
  display: inline-block;
  padding: 0.5rem 0;
  color: #4b5563;
  font-weight: 500;
  transition: color 0.3s ease;
  position: relative;
}

.nav-link:hover {
  color: #3b82f6;
}

.nav-link-active {
  color: #3b82f6;
}

.nav-link-active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #3b82f6;
  border-radius: 2px;
}

.btn {
  display: inline-block;
  padding: 0.5rem 1rem;
  background-color: #3b82f6;
  color: white;
  border-radius: 0.375rem;
  font-weight: 500;
  transition: background-color 0.3s ease;
  text-decoration: none;
}

.btn:hover {
  background-color: #2563eb;
}

.btn-outline {
  display: inline-block;
  padding: 0.5rem 1rem;
  border: 1px solid #3b82f6;
  color: #3b82f6;
  border-radius: 0.375rem;
  font-weight: 500;
  transition: all 0.3s ease;
  text-decoration: none;
}

.btn-outline:hover {
  background-color: #3b82f6;
  color: white;
}

.mobile-nav-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.5rem 0;
  color: #4b5563;
  font-size: 0.75rem;
  transition: color 0.3s ease;
}

.mobile-nav-link:hover {
  color: #3b82f6;
}

.mobile-nav-link-active {
  color: #3b82f6;
}
</style> 