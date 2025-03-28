<template>
  <div>
    <!-- Hero 部分 -->
    <div class="relative overflow-hidden rounded-xl bg-gradient-to-r from-indigo-500 to-purple-600 mb-8">
      <div class="absolute inset-0 bg-indigo-500 opacity-20 z-0">
        <svg class="absolute right-0 top-0 h-full w-1/2 transform opacity-20" viewBox="0 0 926 676" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M899.324 118.231C899.324 118.231 808.516 240.644 592.822 173.839C377.128 107.035 291.454 238.502 189.345 255.402C87.2353 272.302 16.4826 302.898 0.788086 319.798" stroke="white" stroke-width="36" stroke-linecap="round"/>
          <path d="M848.17 263.724C848.17 263.724 736.2 424.497 501.111 324.638C266.021 224.779 225.095 338.359 140.317 358.066C55.5393 377.774 12.1462 409.056 0.78125 403.22" stroke="white" stroke-width="36" stroke-linecap="round"/>
          <path d="M925.217 407.705C925.217 407.705 727.963 553.959 475.782 468.769C223.602 383.579 181.225 547.632 87.2343 578.233C-6.75661 608.835 -1.60657 665.235 -1.60657 665.235" stroke="white" stroke-width="36" stroke-linecap="round"/>
        </svg>
      </div>
      <div class="relative z-10 px-8 py-16 flex flex-col md:flex-row items-center justify-between">
        <div class="text-center md:text-left md:max-w-lg">
          <h1 class="text-3xl md:text-4xl font-bold text-white mb-4">欢迎使用体育赛事管理系统</h1>
          <p class="text-lg text-indigo-100 mb-8">探索精彩赛事，展现运动魅力</p>
          <div class="flex flex-wrap gap-4 justify-center md:justify-start">
            <button 
              class="px-6 py-2.5 bg-white text-indigo-600 font-medium rounded-lg shadow-sm hover:bg-indigo-50 transition-colors focus:outline-none focus:ring-2 focus:ring-white focus:ring-opacity-50"
              @click="router.push('/user/events')"
            >
              浏览赛事
            </button>
            <button 
              class="px-6 py-2.5 bg-transparent text-white font-medium rounded-lg border border-white hover:bg-white/10 transition-colors focus:outline-none focus:ring-2 focus:ring-white focus:ring-opacity-50"
              @click="router.push('/user/registrations')"
            >
              查看我的报名
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="bg-white rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow duration-300 transform hover:-translate-y-1">
        <div class="flex items-center">
          <div class="flex-shrink-0 mr-4 p-3 rounded-full bg-indigo-100">
            <el-icon :size="32" class="text-indigo-600"><Calendar /></el-icon>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-1">即将开始的赛事</p>
            <p class="text-2xl font-bold text-gray-800">{{ upcomingEventsCount }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow duration-300 transform hover:-translate-y-1">
        <div class="flex items-center">
          <div class="flex-shrink-0 mr-4 p-3 rounded-full bg-green-100">
            <el-icon :size="32" class="text-green-600"><DocumentChecked /></el-icon>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-1">我的报名</p>
            <p class="text-2xl font-bold text-gray-800">{{ registrationCount }}</p>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow duration-300 transform hover:-translate-y-1">
        <div class="flex items-center">
          <div class="flex-shrink-0 mr-4 p-3 rounded-full bg-orange-100">
            <el-icon :size="32" class="text-orange-500"><Bell /></el-icon>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-1">最新公告</p>
            <p class="text-2xl font-bold text-gray-800">{{ announcementsCount }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="col-span-1 md:col-span-2">
        <div class="bg-white rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 flex justify-between items-center border-b border-gray-100">
            <h3 class="text-lg font-medium text-gray-800">即将开始的赛事</h3>
            <button 
              class="text-indigo-600 font-medium hover:text-indigo-800 transition-colors focus:outline-none text-sm"
              @click="router.push('/user/events')"
            >
              查看全部
            </button>
          </div>
          
          <div v-if="loadingEvents" class="p-6">
            <el-skeleton :rows="3" animated />
          </div>
          
          <div v-else-if="upcomingEvents.length === 0" class="p-12 flex justify-center">
            <el-empty description="暂无即将开始的赛事" />
          </div>
          
          <div v-else class="divide-y divide-gray-100">
            <div 
              v-for="event in upcomingEvents" 
              :key="event.id"
              class="px-6 py-4 flex justify-between items-center hover:bg-gray-50 transition-colors duration-200"
            >
              <div>
                <h4 class="font-medium text-gray-800 mb-1">{{ event.name }}</h4>
                <div class="flex items-center text-sm text-gray-500 gap-4">
                  <span class="flex items-center">
                    <el-icon class="mr-1 text-indigo-500"><Timer /></el-icon>
                    {{ formatDateTime(event.startTime) }}
                  </span>
                  <span class="flex items-center">
                    <el-icon class="mr-1 text-indigo-500"><Location /></el-icon>
                    {{ event.location }}
                  </span>
                </div>
              </div>
              <router-link :to="`/user/events/${event.id}`">
                <button 
                  class="px-4 py-1.5 bg-indigo-600 text-white text-sm font-medium rounded-full shadow-sm hover:bg-indigo-700 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-50"
                >
                  查看详情
                </button>
              </router-link>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-span-1 space-y-6">
        <!-- 最新公告 -->
        <div class="bg-white rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 flex justify-between items-center border-b border-gray-100">
            <h3 class="text-lg font-medium text-gray-800">最新公告</h3>
            <button 
              class="text-indigo-600 font-medium hover:text-indigo-800 transition-colors focus:outline-none text-sm"
              @click="router.push('/user/announcements')"
            >
              查看全部
            </button>
          </div>
          
          <div v-if="loadingAnnouncements" class="p-6">
            <el-skeleton :rows="3" animated />
          </div>
          
          <div v-else-if="latestAnnouncements.length === 0" class="p-8 flex justify-center">
            <el-empty description="暂无公告" />
          </div>
          
          <div v-else class="divide-y divide-gray-100">
            <div
              v-for="announcement in latestAnnouncements"
              :key="announcement.id"
              class="px-6 py-4 hover:bg-gray-50 transition-colors duration-200"
            >
              <div class="font-medium text-gray-800 mb-1 truncate">{{ announcement.title }}</div>
              <div class="text-xs text-gray-500">{{ formatDateTime(announcement.createdAt) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 我的报名 -->
        <div class="bg-white rounded-xl shadow-sm overflow-hidden">
          <div class="px-6 py-4 flex justify-between items-center border-b border-gray-100">
            <h3 class="text-lg font-medium text-gray-800">我的报名</h3>
            <button 
              class="text-indigo-600 font-medium hover:text-indigo-800 transition-colors focus:outline-none text-sm"
              @click="router.push('/user/registrations')"
            >
              查看全部
            </button>
          </div>
          
          <div v-if="loadingRegistrations" class="p-6">
            <el-skeleton :rows="2" animated />
          </div>
          
          <div v-else-if="myRegistrations.length === 0" class="p-8 flex justify-center">
            <el-empty description="您还没有报名任何赛事" />
          </div>
          
          <div v-else class="divide-y divide-gray-100">
            <div
              v-for="registration in myRegistrations.slice(0, 3)"
              :key="registration.id"
              class="px-6 py-4 flex items-center justify-between hover:bg-gray-50 transition-colors duration-200"
            >
              <span class="text-gray-800 truncate max-w-[200px]">{{ registration.event.name }}</span>
              <el-tag 
                :type="getStatusType(registration.status)" 
                size="small"
                class="!rounded-full"
              >
                {{ getStatusText(registration.status) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 特色部分 -->
    <div class="mb-8">
      <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">系统特色</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white rounded-xl p-6 shadow-sm flex flex-col items-center text-center hover:shadow-md transition-shadow duration-300 hover:-translate-y-1 transform">
          <div class="w-16 h-16 rounded-full bg-indigo-100 flex items-center justify-center mb-4">
            <el-icon :size="32" class="text-indigo-600"><Calendar /></el-icon>
          </div>
          <h3 class="text-lg font-medium text-gray-800 mb-2">赛事管理</h3>
          <p class="text-gray-600">浏览各类体育赛事，获取赛事详情和最新动态，参与喜爱的赛事</p>
        </div>
        
        <div class="bg-white rounded-xl p-6 shadow-sm flex flex-col items-center text-center hover:shadow-md transition-shadow duration-300 hover:-translate-y-1 transform">
          <div class="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mb-4">
            <el-icon :size="32" class="text-green-600"><DocumentChecked /></el-icon>
          </div>
          <h3 class="text-lg font-medium text-gray-800 mb-2">在线报名</h3>
          <p class="text-gray-600">便捷的在线报名流程，轻松参与各类体育赛事，跟踪报名状态</p>
        </div>
        
        <div class="bg-white rounded-xl p-6 shadow-sm flex flex-col items-center text-center hover:shadow-md transition-shadow duration-300 hover:-translate-y-1 transform">
          <div class="w-16 h-16 rounded-full bg-orange-100 flex items-center justify-center mb-4">
            <el-icon :size="32" class="text-orange-500"><Bell /></el-icon>
          </div>
          <h3 class="text-lg font-medium text-gray-800 mb-2">赛事公告</h3>
          <p class="text-gray-600">查看赛事重要公告和通知，及时了解赛事动态和变更信息</p>
        </div>
        
        <div class="bg-white rounded-xl p-6 shadow-sm flex flex-col items-center text-center hover:shadow-md transition-shadow duration-300 hover:-translate-y-1 transform">
          <div class="w-16 h-16 rounded-full bg-rose-100 flex items-center justify-center mb-4">
            <el-icon :size="32" class="text-rose-500"><User /></el-icon>
          </div>
          <h3 class="text-lg font-medium text-gray-800 mb-2">个人中心</h3>
          <p class="text-gray-600">管理个人资料，查看报名历史，跟踪个人参与的赛事情况</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../../stores/auth';
import { eventsAPI } from '../../api/eventsAPI';
import type { Event } from '../../api/eventsAPI';
import { announcementAPI } from '../../api/announcementAPI';
import type { Announcement } from '../../api/announcementAPI';
import { registrationAPI } from '../../api/registrationAPI';
import type { Registration } from '../../api/registrationAPI';
import { Calendar, Timer, Bell, Location, DocumentChecked, User } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { formatDateTime } from '../../utils/formatter';

const authStore = useAuthStore();
const router = useRouter();

// 赛事数据
const upcomingEvents = ref<Event[]>([]);
const loadingEvents = ref(true);
const upcomingEventsCount = ref(0);

// 公告数据
const latestAnnouncements = ref<Announcement[]>([]);
const loadingAnnouncements = ref(true);
const announcementsCount = ref(0);

// 报名数据
const myRegistrations = ref<Registration[]>([]);
const loadingRegistrations = ref(true);
const registrationCount = ref(0);

// 获取数据
onMounted(async () => {
  fetchUpcomingEvents();
  fetchLatestAnnouncements();
  fetchMyRegistrations();
});

// 获取即将开始的赛事
const fetchUpcomingEvents = async () => {
  try {
    const events = await eventsAPI.getUpcomingEvents();
    upcomingEvents.value = events;
    upcomingEventsCount.value = events.length;
  } catch (error) {
    console.error('获取即将开始的赛事失败', error);
  } finally {
    loadingEvents.value = false;
  }
};

// 获取最新公告
const fetchLatestAnnouncements = async () => {
  try {
    const announcements = await announcementAPI.getLatestAnnouncements();
    latestAnnouncements.value = announcements;
    announcementsCount.value = announcements.length;
  } catch (error) {
    console.error('获取最新公告失败', error);
  } finally {
    loadingAnnouncements.value = false;
  }
};

// 获取我的报名
const fetchMyRegistrations = async () => {
  try {
    const registrations = await registrationAPI.getMyRegistrations();
    myRegistrations.value = registrations;
    registrationCount.value = registrations.length;
  } catch (error) {
    console.error('获取我的报名失败', error);
  } finally {
    loadingRegistrations.value = false;
  }
};

// 获取状态类型（用于标签颜色）
const getStatusType = (status: string) => {
  switch (status) {
    case 'APPROVED': return 'success';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'danger';
    case 'CANCELLED': return 'info';
    default: return '';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'APPROVED': return '已批准';
    case 'PENDING': return '待审核';
    case 'REJECTED': return '已拒绝';
    case 'CANCELLED': return '已取消';
    default: return status;
  }
};
</script> 