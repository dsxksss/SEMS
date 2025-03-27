<template>
  <div class="dashboard-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <h2>{{ greeting }}，{{ authStore.user?.username }}</h2>
        </div>
      </template>
      <div class="dashboard-summary">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="我的报名" :value="registrationCount" value-style="font-size: 24px; color: #409EFF">
              <template #suffix>
                <el-icon><Calendar /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="即将开始的赛事" :value="upcomingEventsCount" value-style="font-size: 24px; color: #67C23A">
              <template #suffix>
                <el-icon><Timer /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="最新公告" :value="announcementsCount" value-style="font-size: 24px; color: #E6A23C">
              <template #suffix>
                <el-icon><Bell /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <div class="dashboard-sections">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="upcoming-events">
            <template #header>
              <div class="card-header">
                <h3>即将开始的赛事</h3>
                <router-link to="/user/events">
                  <el-button type="primary" text>查看全部</el-button>
                </router-link>
              </div>
            </template>
            <div v-if="loadingEvents" class="loading-placeholder">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="upcomingEvents.length === 0" class="empty-data">
              <el-empty description="暂无即将开始的赛事" />
            </div>
            <div v-else class="event-list">
              <el-table :data="upcomingEvents" style="width: 100%" :show-header="false" size="large">
                <el-table-column>
                  <template #default="scope">
                    <div class="event-item">
                      <div class="event-meta">
                        <h4 class="event-name">{{ scope.row.name }}</h4>
                        <div class="event-details">
                          <span class="event-time">
                            <el-icon><Timer /></el-icon>
                            {{ formatDate(scope.row.startTime) }}
                          </span>
                          <span class="event-location">
                            <el-icon><Location /></el-icon>
                            {{ scope.row.location }}
                          </span>
                        </div>
                      </div>
                      <div class="event-action">
                        <router-link :to="`/user/events/${scope.row.id}`">
                          <el-button type="primary" size="small">查看详情</el-button>
                        </router-link>
                      </div>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="announcements">
            <template #header>
              <div class="card-header">
                <h3>最新公告</h3>
                <router-link to="/user/announcements">
                  <el-button type="primary" text>查看全部</el-button>
                </router-link>
              </div>
            </template>
            <div v-if="loadingAnnouncements" class="loading-placeholder">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="latestAnnouncements.length === 0" class="empty-data">
              <el-empty description="暂无公告" />
            </div>
            <div v-else class="announcement-list">
              <div v-for="announcement in latestAnnouncements" :key="announcement.id" class="announcement-item">
                <div class="announcement-title">{{ announcement.title }}</div>
                <div class="announcement-date">{{ formatDate(announcement.createdAt) }}</div>
              </div>
            </div>
          </el-card>
          
          <el-card class="my-registrations mt-4">
            <template #header>
              <div class="card-header">
                <h3>我的报名</h3>
                <router-link to="/user/registrations">
                  <el-button type="primary" text>查看全部</el-button>
                </router-link>
              </div>
            </template>
            <div v-if="loadingRegistrations" class="loading-placeholder">
              <el-skeleton :rows="2" animated />
            </div>
            <div v-else-if="myRegistrations.length === 0" class="empty-data">
              <el-empty description="您还没有报名任何赛事" />
            </div>
            <div v-else class="registration-list">
              <div v-for="registration in myRegistrations.slice(0, 3)" :key="registration.id" class="registration-item">
                <span class="registration-event">{{ registration.event.name }}</span>
                <el-tag :type="getStatusType(registration.status)" size="small">{{ getStatusText(registration.status) }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '../../stores/auth';
import { eventsAPI } from '../../api/eventsAPI';
import { announcementAPI } from '../../api/announcementAPI';
import { registrationAPI } from '../../api/registrationAPI';
import { Calendar, Timer, Bell, Location } from '@element-plus/icons-vue';

const authStore = useAuthStore();

// 时间问候语
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 9) return '早上好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 17) return '下午好';
  if (hour < 19) return '傍晚好';
  return '晚上好';
});

// 赛事数据
const upcomingEvents = ref<any[]>([]);
const loadingEvents = ref(true);
const upcomingEventsCount = ref(0);

// 公告数据
const latestAnnouncements = ref<any[]>([]);
const loadingAnnouncements = ref(true);
const announcementsCount = ref(0);

// 报名数据
const myRegistrations = ref<any[]>([]);
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

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
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

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
}

.dashboard-summary {
  padding: 10px 0;
}

.dashboard-sections {
  margin-top: 20px;
}

.event-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.event-item:last-child {
  border-bottom: none;
}

.event-name {
  font-size: 16px;
  margin: 0 0 5px 0;
}

.event-details {
  display: flex;
  color: #999;
  font-size: 14px;
}

.event-time, .event-location {
  display: flex;
  align-items: center;
  margin-right: 15px;
}

.event-time .el-icon, .event-location .el-icon {
  margin-right: 4px;
}

.announcement-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-date {
  font-size: 12px;
  color: #999;
}

.registration-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.registration-item:last-child {
  border-bottom: none;
}

.registration-event {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;
}

.loading-placeholder, .empty-data {
  padding: 20px 0;
}

.mt-4 {
  margin-top: 16px;
}
</style> 