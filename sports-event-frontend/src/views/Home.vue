<template>
  <div>
    <!-- 英雄区域 -->
    <section class="bg-blue-600 text-white py-20 rounded-lg mb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-2xl">
          <h1 class="text-4xl md:text-5xl font-bold mb-6">欢迎使用体育赛事管理系统</h1>
          <p class="text-xl mb-8">便捷参与体育赛事，轻松查看比赛信息，实时跟踪比赛结果</p>
          <div class="flex flex-wrap gap-4">
            <router-link to="/events" class="bg-white text-blue-600 px-6 py-3 rounded-md font-medium hover:bg-blue-50 transition">
              浏览赛事
            </router-link>
            <router-link v-if="!authStore.isAuthenticated" to="/register" class="bg-blue-700 text-white px-6 py-3 rounded-md font-medium hover:bg-blue-800 transition">
              立即注册
            </router-link>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 即将到来的赛事 -->
    <section class="mb-12">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold">即将到来的赛事</h2>
        <router-link to="/events" class="text-blue-600 hover:text-blue-800">查看更多 &rarr;</router-link>
      </div>
      
      <el-skeleton :rows="3" animated v-if="loading" />
      
      <div v-else-if="upcomingEvents.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <el-card v-for="event in upcomingEvents" :key="event.id" class="event-card hover:shadow-lg transition">
          <div class="flex items-start">
            <el-tag :type="getStatusType(event.status)" class="mb-2">{{ getStatusText(event.status) }}</el-tag>
            <el-tag type="info" class="ml-2">{{ event.category.name }}</el-tag>
          </div>
          <h3 class="text-xl font-bold mb-2 line-clamp-1">{{ event.name }}</h3>
          <p class="text-gray-500 mb-3 line-clamp-2">{{ event.description }}</p>
          <div class="text-sm text-gray-600 mb-1">
            <i class="el-icon-location"></i> {{ event.location }}
          </div>
          <div class="text-sm text-gray-600 mb-3">
            <i class="el-icon-date"></i> {{ formatDate(event.startTime) }}
          </div>
          <div class="flex justify-between items-center mt-4">
            <span class="text-sm text-gray-500">报名截止: {{ formatDate(event.registrationDeadline) }}</span>
            <router-link :to="`/events/${event.id}`" class="text-blue-600 hover:text-blue-800">
              查看详情
            </router-link>
          </div>
        </el-card>
      </div>
      
      <el-empty v-else description="暂无即将到来的赛事" />
    </section>
    
    <!-- 最新公告 -->
    <section class="mb-12">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold">最新公告</h2>
        <router-link to="/announcements" class="text-blue-600 hover:text-blue-800">查看更多 &rarr;</router-link>
      </div>
      
      <el-skeleton :rows="3" animated v-if="loadingAnnouncements" />
      
      <div v-else-if="latestAnnouncements.length > 0" class="space-y-4">
        <el-card v-for="announcement in latestAnnouncements" :key="announcement.id" class="hover:shadow-md transition">
          <h3 class="text-lg font-bold mb-2">{{ announcement.title }}</h3>
          <p class="text-gray-500 mb-2 line-clamp-2">{{ announcement.content }}</p>
          <div class="flex justify-between items-center text-sm">
            <span class="text-gray-500">{{ formatDate(announcement.createdAt) }}</span>
            <router-link :to="`/announcements/${announcement.id}`" class="text-blue-600 hover:text-blue-800">
              阅读全文
            </router-link>
          </div>
        </el-card>
      </div>
      
      <el-empty v-else description="暂无公告" />
    </section>
    
    <!-- 系统特性 -->
    <section class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
      <div class="feature-card">
        <div class="feature-icon bg-blue-100 text-blue-600">
          <i class="el-icon-date text-2xl"></i>
        </div>
        <h3 class="text-xl font-bold mb-2">便捷报名</h3>
        <p class="text-gray-600">一键报名参加各类体育赛事，实时查看报名状态，避免繁琐的纸质流程。</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon bg-green-100 text-green-600">
          <i class="el-icon-trophy text-2xl"></i>
        </div>
        <h3 class="text-xl font-bold mb-2">成绩查询</h3>
        <p class="text-gray-600">实时更新比赛成绩，方便查询个人和团队的历史成绩记录。</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon bg-purple-100 text-purple-600">
          <i class="el-icon-bell text-2xl"></i>
        </div>
        <h3 class="text-xl font-bold mb-2">赛事通知</h3>
        <p class="text-gray-600">重要赛事信息及时通知，确保您不会错过任何重要的比赛或活动。</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useEventStore, useAuthStore } from '../stores';
import { format } from 'date-fns';
import { announcementAPI } from '../api';
import type { Announcement } from '../api/announcementAPI';
import type { Event } from '../api/eventsAPI';

// 状态管理
const eventStore = useEventStore();
const authStore = useAuthStore();

// 加载状态
const loading = ref(false);
const loadingAnnouncements = ref(false);

// 数据
const upcomingEvents = ref<Event[]>([]);
const latestAnnouncements = ref<Announcement[]>([]);

// 获取即将到来的赛事
const fetchUpcomingEvents = async () => {
  loading.value = true;
  try {
    const events = await eventStore.fetchUpcomingEvents();
    if (events) {
      upcomingEvents.value = events;
    }
  } catch (error) {
    console.error('获取即将到来的赛事失败:', error);
  } finally {
    loading.value = false;
  }
};

// 获取最新公告
const fetchLatestAnnouncements = async () => {
  loadingAnnouncements.value = true;
  try {
    const announcements = await announcementAPI.getLatestAnnouncements();
    latestAnnouncements.value = announcements;
  } catch (error) {
    console.error('获取最新公告失败:', error);
  } finally {
    loadingAnnouncements.value = false;
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm');
  } catch (error) {
    return dateString;
  }
};

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'UPCOMING':
      return 'primary';
    case 'ONGOING':
      return 'success';
    case 'COMPLETED':
      return 'info';
    case 'CANCELLED':
      return 'danger';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'UPCOMING':
      return '即将开始';
    case 'ONGOING':
      return '进行中';
    case 'COMPLETED':
      return '已结束';
    case 'CANCELLED':
      return '已取消';
    default:
      return '未知状态';
  }
};

// 组件挂载
onMounted(() => {
  fetchUpcomingEvents();
  fetchLatestAnnouncements();
});
</script>

<style scoped>
.feature-card {
  @apply p-6 rounded-lg border border-gray-200 hover:shadow-md transition;
}

.feature-icon {
  @apply flex items-center justify-center w-12 h-12 rounded-full mb-4;
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.event-card {
  height: 100%;
}
</style> 