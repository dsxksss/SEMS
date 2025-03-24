<template>
  <header class="bg-white shadow-md">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <!-- Logo区域 -->
        <div class="flex items-center">
          <router-link to="/" class="text-xl font-bold text-blue-600">
            体育赛事管理系统
          </router-link>
        </div>

        <!-- 导航菜单 -->
        <nav class="flex items-center space-x-6">
          <router-link to="/" class="text-gray-700 hover:text-blue-600">首页</router-link>
          <router-link to="/events" class="text-gray-700 hover:text-blue-600">赛事信息</router-link>
          <router-link to="/announcements" class="text-gray-700 hover:text-blue-600">公告信息</router-link>
          
          <!-- 根据用户角色显示不同菜单项 -->
          <template v-if="userStore.isLoggedIn">
            <template v-if="userStore.role === 'ADMIN'">
              <router-link to="/admin" class="text-gray-700 hover:text-blue-600">
                后台管理
              </router-link>
            </template>
            
            <template v-if="userStore.role === 'ATHLETE'">
              <router-link to="/athlete-registration" class="text-gray-700 hover:text-blue-600">
                运动员报名
              </router-link>
            </template>
            
            <template v-if="userStore.role === 'USER'">
              <router-link to="/audience-registration" class="text-gray-700 hover:text-blue-600">
                观众报名
              </router-link>
            </template>
            
            <router-link to="/profile" class="text-gray-700 hover:text-blue-600">
              个人中心
            </router-link>
            
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="text-gray-700 hover:text-blue-600 cursor-pointer">
                {{ userStore.username }}
                <el-icon><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <!-- 未登录状态显示登录注册按钮 -->
          <template v-else>
            <router-link to="/login" class="btn-sm text-white bg-blue-600 hover:bg-blue-700 px-3 py-1 rounded">
              登录
            </router-link>
            <router-link to="/register" class="btn-sm text-blue-600 border border-blue-600 hover:bg-blue-50 px-3 py-1 rounded">
              注册
            </router-link>
          </template>
        </nav>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  }
}
</script> 