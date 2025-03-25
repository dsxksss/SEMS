<template>
  <div class="home-page">
    <!-- 英雄区域 -->
    <section class="bg-gradient-to-r from-primary-600 to-primary-800 text-white">
      <div class="container mx-auto px-4 py-16 md:py-24">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-10 items-center">
          <!-- 左侧内容 -->
          <div class="space-y-6 animate-fade-in">
            <h1 class="text-4xl md:text-5xl font-bold leading-tight">
              <span class="block">体育赛事管理</span>
              <span class="block">一站式解决方案</span>
            </h1>
            <p class="text-xl text-primary-100 leading-relaxed max-w-lg">
              轻松管理各类体育赛事，从报名到成绩公布，提供完整的赛事管理流程支持。
            </p>
            <div class="flex flex-wrap gap-4 pt-2">
              <router-link to="/events" class="btn bg-white text-primary-700 hover:bg-primary-50 shadow-lg">
                浏览赛事
              </router-link>
              <router-link to="/register" class="btn bg-primary-500 border border-primary-400 hover:bg-primary-400">
                立即注册
                  </router-link>
                </div>
              </div>
          
          <!-- 右侧图像 -->
          <div class="hidden md:block relative">
            <div class="absolute -top-10 -left-10 w-20 h-20 bg-primary-500 rounded-full opacity-20 animate-pulse-slow"></div>
            <div class="absolute bottom-10 right-10 w-16 h-16 bg-primary-400 rounded-full opacity-20 animate-pulse-slow"></div>
            
            <img src="@/assets/images/hero-image.svg" alt="体育赛事管理" class="w-full max-w-lg mx-auto drop-shadow-2xl animate-slide-up">
          </div>
        </div>
      </div>
    </section>
    
    <!-- 特色功能 -->
    <section class="py-16 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold text-gray-900">核心功能</h2>
          <p class="mt-4 text-xl text-gray-600 max-w-3xl mx-auto">
            我们提供全面的赛事管理功能，满足各类体育赛事的需求
          </p>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- 功能卡片 1 -->
          <div class="card hover:shadow-xl transition-shadow duration-300 transform hover:-translate-y-1">
            <div class="w-12 h-12 flex items-center justify-center rounded-lg bg-primary-100 text-primary-600 mb-6">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-3">赛事报名</h3>
            <p class="text-gray-600">
              简化赛事报名流程，支持在线报名和缴费，节省管理时间。
            </p>
          </div>
          
          <!-- 功能卡片 2 -->
          <div class="card hover:shadow-xl transition-shadow duration-300 transform hover:-translate-y-1">
            <div class="w-12 h-12 flex items-center justify-center rounded-lg bg-primary-100 text-primary-600 mb-6">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-3">成绩管理</h3>
            <p class="text-gray-600">
              实时记录和发布赛事成绩，支持多种赛制和排名规则。
            </p>
          </div>
          
          <!-- 功能卡片 3 -->
          <div class="card hover:shadow-xl transition-shadow duration-300 transform hover:-translate-y-1">
            <div class="w-12 h-12 flex items-center justify-center rounded-lg bg-primary-100 text-primary-600 mb-6">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-3">数据分析</h3>
            <p class="text-gray-600">
              全面的赛事数据分析，帮助组织者了解赛事情况和参与者表现。
            </p>
          </div>
        </div>
      </div>
      </section>
      
      <!-- 即将开始的赛事 -->
    <section class="py-16">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center mb-10">
          <h2 class="text-3xl font-bold text-gray-900">即将开始的赛事</h2>
          <router-link to="/events" class="text-primary-600 hover:text-primary-700 font-medium flex items-center group">
            查看全部
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1 transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </router-link>
        </div>
        
        <div v-if="eventsLoading" class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div v-for="i in 3" :key="i" class="card animate-pulse">
            <div class="h-4 bg-gray-200 rounded mb-4 w-3/4"></div>
            <div class="h-3 bg-gray-200 rounded mb-3 w-1/2"></div>
            <div class="h-3 bg-gray-200 rounded mb-3"></div>
            <div class="h-3 bg-gray-200 rounded mb-3"></div>
            <div class="h-3 bg-gray-200 rounded w-1/4"></div>
          </div>
        </div>
        
        <div v-else-if="upcomingEvents.length === 0" class="card text-center py-12">
          <div class="flex flex-col items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <h3 class="text-lg font-medium text-gray-900 mb-2">暂无即将开始的赛事</h3>
            <p class="text-gray-500">请稍后再来查看，或浏览所有赛事</p>
            <router-link to="/events" class="btn-primary mt-4">
              浏览所有赛事
            </router-link>
          </div>
        </div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div v-for="event in upcomingEvents.slice(0, 3)" :key="event.id" class="card rounded-xl overflow-hidden hover:shadow-xl transition-shadow duration-300 flex flex-col">
            <!-- 赛事状态标签 -->
            <div class="bg-primary-100 text-primary-700 text-xs font-bold uppercase px-3 py-1 rounded-full inline-block w-fit mb-4">
              即将开始
            </div>
            
            <h3 class="text-xl font-bold text-gray-900 mb-2">{{ event.name }}</h3>
            
            <div class="flex items-center mb-4 text-gray-500 text-sm">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{{ formatDate(event.startTime) }}</span>
        </div>
        
            <div class="flex items-center mb-4 text-gray-500 text-sm">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span>{{ event.location }}</span>
            </div>
            
            <p class="text-gray-600 mb-6 line-clamp-2 flex-grow">{{ event.description }}</p>
            
            <router-link :to="`/events/${event.id}`" class="text-primary-600 hover:text-primary-700 font-medium flex items-center group self-end">
              查看详情
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1 transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </router-link>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 最新公告 -->
    <section class="py-16 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center mb-10">
          <h2 class="text-3xl font-bold text-gray-900">最新公告</h2>
          <router-link to="/announcements" class="text-primary-600 hover:text-primary-700 font-medium flex items-center group">
            查看全部
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1 transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </router-link>
        </div>
        
        <div v-if="announcementsLoading" class="space-y-4">
          <div v-for="i in 3" :key="i" class="card animate-pulse">
            <div class="flex justify-between">
              <div class="h-4 bg-gray-200 rounded mb-4 w-1/3"></div>
              <div class="h-3 bg-gray-200 rounded w-1/4"></div>
            </div>
            <div class="h-3 bg-gray-200 rounded mb-2"></div>
            <div class="h-3 bg-gray-200 rounded mb-2"></div>
            <div class="h-3 bg-gray-200 rounded w-3/4"></div>
          </div>
        </div>
        
        <div v-else-if="announcements.length === 0" class="card text-center py-12">
          <div class="flex flex-col items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            <h3 class="text-lg font-medium text-gray-900 mb-2">暂无公告</h3>
            <p class="text-gray-500">请稍后再来查看最新公告</p>
        </div>
        </div>
        
        <div v-else class="space-y-4">
          <div v-for="announcement in announcements.slice(0, 4)" :key="announcement.id" 
            class="card hover:shadow-xl transition-shadow duration-300 cursor-pointer group"
              @click="viewAnnouncement(announcement)">
            <div class="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-2">
              <h3 class="text-lg font-bold text-gray-900 group-hover:text-primary-600 transition-colors duration-200">{{ announcement.title }}</h3>
              <span class="text-sm text-gray-500 whitespace-nowrap">{{ formatDate(announcement.publishTime) }}</span>
            </div>
            <p class="text-gray-600 mt-2 line-clamp-2">{{ announcement.content }}</p>
          </div>
        </div>
        </div>
      </section>
    
    <!-- 查看公告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentAnnouncement?.title"
      width="50%"
      class="announcement-dialog"
    >
      <div class="text-gray-500 mb-4 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        <span>{{ formatDate(currentAnnouncement?.publishTime) }}</span>
      </div>
      <div class="text-gray-700 whitespace-pre-wrap leading-relaxed">{{ currentAnnouncement?.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useEventStore } from '@/stores/event'
import { useAnnouncementStore } from '@/stores/announcement'
import { ElMessage } from 'element-plus'

// 获取router
const router = useRouter()

// 获取store
const eventStore = useEventStore()
const announcementStore = useAnnouncementStore()

// 数据加载状态
const eventsLoading = ref(true)
const announcementsLoading = ref(true)

// 本地数据
const upcomingEvents = ref([])
const announcements = ref([])

// 公告详情相关
const dialogVisible = ref(false)
const currentAnnouncement = ref(null)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未设置'
  
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric'
  })
}

// 查看公告详情
const viewAnnouncement = (announcement) => {
  currentAnnouncement.value = announcement
  dialogVisible.value = true
}

// 获取数据
const fetchData = async () => {
  // 获取即将开始的赛事
  try {
    eventsLoading.value = true
    const result = await eventStore.fetchUpcomingEvents(7)
    upcomingEvents.value = result || []
  } catch (error) {
    console.error('获取赛事数据失败:', error)
    ElMessage.error('获取赛事数据失败，请稍后重试')
    upcomingEvents.value = []
  } finally {
    eventsLoading.value = false
  }
  
  // 获取最新公告
  try {
    announcementsLoading.value = true
    const result = await announcementStore.fetchLatestAnnouncements(4)
    announcements.value = result || []
  } catch (error) {
    console.error('获取公告数据失败:', error)
    ElMessage.error('获取公告数据失败，请稍后重试')
    announcements.value = []
  } finally {
    announcementsLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(fetchData)
</script>

<style scoped>
.card {
  @apply bg-white p-6 rounded-lg shadow-sm;
}

/* 动画类定义 */
.animate-fade-in {
  animation: fadeIn 0.6s ease-in-out;
}

.animate-pulse-slow {
  animation: pulseSlow 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.animate-slide-up {
  animation: slideUp 0.8s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes pulseSlow {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 0.3;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.home-page {
  overflow-x: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.btn {
  @apply inline-flex items-center px-6 py-3 rounded-lg font-medium transition-colors duration-200;
}

.btn-primary {
  @apply btn bg-primary-600 text-white hover:bg-primary-700;
}

.announcement-dialog :deep(.el-dialog__title) {
  @apply text-xl font-bold text-gray-900;
}

.announcement-dialog :deep(.el-dialog__header) {
  @apply pb-0 border-b border-gray-200;
}
</style> 