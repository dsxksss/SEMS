<template>
  <div class="admin-layout min-h-screen bg-gray-50">
    <!-- 顶部导航 -->
    <header class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link to="/" class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-blue-600">体育赛事管理系统</h1>
            </router-link>
          </div>
          
          <div class="flex items-center">
            <span class="text-gray-700 mr-4">管理员：{{ userStore.username }}</span>
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="el-dropdown-link cursor-pointer flex items-center">
                <el-avatar :size="32" class="mr-1">
                  {{ userStore.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>
    
    <div class="flex">
      <!-- 侧边导航 -->
      <aside class="w-64 bg-white shadow-md fixed h-full overflow-y-auto">
        <nav class="mt-5 px-2">
          <div class="space-y-1">
            <router-link 
              to="/admin" 
              class="group flex items-center px-4 py-3 text-base font-medium rounded-md"
              :class="[$route.name === 'AdminDashboard' ? 'bg-blue-50 text-blue-600' : 'text-gray-700 hover:bg-gray-50']"
              exact
            >
              <el-icon class="mr-3 text-lg" :class="[$route.name === 'AdminDashboard' ? 'text-blue-600' : 'text-gray-500']">
                <data-analysis />
              </el-icon>
              仪表盘
            </router-link>
            
            <router-link 
              to="/admin/users" 
              class="group flex items-center px-4 py-3 text-base font-medium rounded-md"
              :class="[$route.name === 'UserManagement' ? 'bg-blue-50 text-blue-600' : 'text-gray-700 hover:bg-gray-50']"
            >
              <el-icon class="mr-3 text-lg" :class="[$route.name === 'UserManagement' ? 'text-blue-600' : 'text-gray-500']">
                <user />
              </el-icon>
              用户管理
            </router-link>
            
            <router-link 
              to="/admin/events" 
              class="group flex items-center px-4 py-3 text-base font-medium rounded-md"
              :class="[$route.name === 'EventManagement' ? 'bg-blue-50 text-blue-600' : 'text-gray-700 hover:bg-gray-50']"
            >
              <el-icon class="mr-3 text-lg" :class="[$route.name === 'EventManagement' ? 'text-blue-600' : 'text-gray-500']">
                <calendar />
              </el-icon>
              赛事管理
            </router-link>
            
            <router-link 
              to="/admin/registrations" 
              class="group flex items-center px-4 py-3 text-base font-medium rounded-md"
              :class="[$route.name === 'RegistrationManagement' ? 'bg-blue-50 text-blue-600' : 'text-gray-700 hover:bg-gray-50']"
            >
              <el-icon class="mr-3 text-lg" :class="[$route.name === 'RegistrationManagement' ? 'text-blue-600' : 'text-gray-500']">
                <list />
              </el-icon>
              报名管理
            </router-link>
            
            <router-link 
              to="/admin/results" 
              class="group flex items-center px-4 py-3 text-base font-medium rounded-md"
              :class="[$route.name === 'ResultManagement' ? 'bg-blue-50 text-blue-600' : 'text-gray-700 hover:bg-gray-50']"
            >
              <el-icon class="mr-3 text-lg" :class="[$route.name === 'ResultManagement' ? 'text-blue-600' : 'text-gray-500']">
                <medal />
              </el-icon>
              成绩管理
            </router-link>
            
            <router-link 
              to="/admin/reports" 
              class="group flex items-center px-4 py-3 text-base font-medium rounded-md"
              :class="[$route.name === 'Reports' ? 'bg-blue-50 text-blue-600' : 'text-gray-700 hover:bg-gray-50']"
            >
              <el-icon class="mr-3 text-lg" :class="[$route.name === 'Reports' ? 'text-blue-600' : 'text-gray-500']">
                <document />
              </el-icon>
              报表统计
            </router-link>
          </div>
        </nav>
      </aside>
      
      <!-- 主内容区 -->
      <main class="flex-1 ml-64 p-8">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Document, List, Calendar, User, DataAnalysis, Medal } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.admin-layout {
  background-color: #f9fafb; /* 浅灰背景 */
}
</style> 