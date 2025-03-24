<template>
  <div class="home-page">
    <Header />
    
    <!-- 页面内容 -->
    <main class="container mx-auto px-4 py-8">
      <!-- 轮播图 -->
      <section class="mb-10">
        <el-carousel height="400px" indicator-position="outside">
          <el-carousel-item v-for="(banner, index) in banners" :key="index">
            <div class="w-full h-full bg-cover bg-center rounded-lg overflow-hidden" :style="{ backgroundImage: `url(${banner.image})` }">
              <div class="w-full h-full flex items-center bg-black bg-opacity-30 px-10">
                <div class="text-white max-w-xl">
                  <h2 class="text-3xl font-bold mb-4">{{ banner.title }}</h2>
                  <p class="text-lg mb-6">{{ banner.description }}</p>
                  <router-link v-if="banner.link" :to="banner.link" class="btn">
                    了解更多
                  </router-link>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>
      
      <!-- 即将开始的赛事 -->
      <section class="mb-10">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-2xl font-bold">即将开始的赛事</h2>
          <router-link to="/events" class="text-blue-600 hover:underline">
            查看全部 <el-icon><arrow-right /></el-icon>
          </router-link>
        </div>
        
        <div v-if="eventStore.isLoading" class="text-center py-10">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="eventStore.error" class="text-center py-10 text-red-500">
          {{ eventStore.error }}
        </div>
        
        <div v-else-if="eventStore.upcomingEvents.length === 0" class="text-center py-10 text-gray-500">
          暂无即将开始的赛事
        </div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="event in eventStore.upcomingEvents.slice(0, 3)" :key="event.id" class="card hover:shadow-lg transition-shadow">
            <h3 class="text-xl font-bold mb-2">{{ event.name }}</h3>
            <p class="text-gray-600 mb-4">{{ formatDate(event.startTime) }} - {{ formatDate(event.endTime) }}</p>
            <p class="text-gray-700 mb-4 line-clamp-2">{{ event.description }}</p>
            <div class="flex justify-between items-center">
              <span class="text-blue-600 font-semibold">{{ event.location }}</span>
              <router-link :to="`/events/${event.id}`" class="text-blue-600 hover:underline">
                详情 <el-icon><arrow-right /></el-icon>
              </router-link>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 最新公告 -->
      <section class="mb-10">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-2xl font-bold">最新公告</h2>
          <router-link to="/announcements" class="text-blue-600 hover:underline">
            查看全部 <el-icon><arrow-right /></el-icon>
          </router-link>
        </div>
        
        <div v-if="announcementStore.isLoading" class="text-center py-10">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="announcementStore.error" class="text-center py-10 text-red-500">
          {{ announcementStore.error }}
        </div>
        
        <div v-else-if="announcementStore.announcements.length === 0" class="text-center py-10 text-gray-500">
          暂无公告
        </div>
        
        <div v-else class="space-y-4">
          <div v-for="announcement in announcementStore.announcements.slice(0, 5)" :key="announcement.id" 
              class="card hover:shadow-lg transition-shadow cursor-pointer"
              @click="viewAnnouncement(announcement)">
            <div class="flex justify-between items-start">
              <h3 class="text-lg font-bold">{{ announcement.title }}</h3>
              <span class="text-sm text-gray-500">{{ formatDate(announcement.publishTime) }}</span>
            </div>
            <p class="text-gray-600 mt-2 line-clamp-2">{{ announcement.content }}</p>
          </div>
        </div>
      </section>
    </main>
    
    <!-- 查看公告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentAnnouncement?.title"
      width="50%">
      <div class="text-gray-500 mb-4">
        发布时间: {{ formatDate(currentAnnouncement?.publishTime) }}
      </div>
      <div class="whitespace-pre-wrap">{{ currentAnnouncement?.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useEventStore } from '@/stores/event'
import { useAnnouncementStore } from '@/stores/announcement'
import { ArrowRight } from '@element-plus/icons-vue'
import Header from '@/components/Header.vue'

const eventStore = useEventStore()
const announcementStore = useAnnouncementStore()

// 轮播图数据
const banners = ref([
  {
    title: '2023年全国大学生运动会',
    description: '展示青春活力，挑战自我极限！',
    image: 'https://picsum.photos/1200/400?random=1',
    link: '/events/1'
  },
  {
    title: '篮球联赛火热报名中',
    description: '组建你的梦之队，争夺最高荣誉！',
    image: 'https://picsum.photos/1200/400?random=2',
    link: '/events/2'
  },
  {
    title: '校园马拉松即将开跑',
    description: '突破自我，跑出你的精彩人生！',
    image: 'https://picsum.photos/1200/400?random=3',
    link: '/events/3'
  }
])

// 公告详情相关
const dialogVisible = ref(false)
const currentAnnouncement = ref(null)

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 查看公告详情
const viewAnnouncement = (announcement) => {
  currentAnnouncement.value = announcement
  dialogVisible.value = true
}

// 组件挂载时获取数据
onMounted(async () => {
  eventStore.fetchAllEvents()
  announcementStore.fetchAllAnnouncements()
})
</script> 