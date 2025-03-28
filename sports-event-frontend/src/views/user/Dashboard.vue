<template>
  <div class="dashboard-container">
    <!-- Hero 部分 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>欢迎使用体育赛事管理系统</h1>
        <p class="subtitle">探索精彩赛事，展现运动魅力</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/user/events')">浏览赛事</el-button>
          <el-button size="large" @click="router.push('/user/registrations')">查看我的报名</el-button>
        </div>
      </div>
      <div class="hero-image">
        <img src="https://img.freepik.com/free-vector/gradient-national-sports-day-illustration_23-2149590146.jpg" alt="体育赛事插图">
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-icon">
              <el-icon :size="36" color="#409EFF"><Calendar /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-title">即将开始的赛事</div>
              <div class="stats-value">{{ upcomingEventsCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-icon">
              <el-icon :size="36" color="#67C23A"><DocumentChecked /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-title">我的报名</div>
              <div class="stats-value">{{ registrationCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-icon">
              <el-icon :size="36" color="#E6A23C"><Bell /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-title">最新公告</div>
              <div class="stats-value">{{ announcementsCount }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <el-row :gutter="20">
        <el-col :xs="24" :md="16">
          <el-card class="upcoming-events">
            <template #header>
              <div class="card-header">
                <h3>即将开始的赛事</h3>
                <el-button text type="primary" @click="router.push('/user/events')">查看全部</el-button>
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
                            {{ formatDateTime(scope.row.startTime) }}
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
        
        <el-col :xs="24" :md="8">
          <el-card class="announcements">
            <template #header>
              <div class="card-header">
                <h3>最新公告</h3>
                <el-button text type="primary" @click="router.push('/user/announcements')">查看全部</el-button>
              </div>
            </template>
            <div v-if="loadingAnnouncements" class="loading-placeholder">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="latestAnnouncements.length === 0" class="empty-data">
              <el-empty description="暂无公告" />
            </div>
            <div v-else class="announcement-list">
              <div
                v-for="(announcement, index) in latestAnnouncements"
                :key="announcement.id"
                class="announcement-item"
                :delay="index * 100"
              >
                <div class="announcement-title">{{ announcement.title }}</div>
                <div class="announcement-date">{{ formatDateTime(announcement.createdAt) }}</div>
              </div>
            </div>
          </el-card>
          
          <el-card class="my-registrations mt-4">
            <template #header>
              <div class="card-header">
                <h3>我的报名</h3>
                <el-button text type="primary" @click="router.push('/user/registrations')">查看全部</el-button>
              </div>
            </template>
            <div v-if="loadingRegistrations" class="loading-placeholder">
              <el-skeleton :rows="2" animated />
            </div>
            <div v-else-if="myRegistrations.length === 0" class="empty-data">
              <el-empty description="您还没有报名任何赛事" />
            </div>
            <div v-else class="registration-list">
              <div
                v-for="(registration, index) in myRegistrations.slice(0, 3)"
                :key="registration.id"
                class="registration-item"
                :delay="index * 100"
              >
                <span class="registration-event">{{ registration.event.name }}</span>
                <el-tag :type="getStatusType(registration.status)" size="small">{{ getStatusText(registration.status) }}</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 特色部分 -->
    <div class="features-section">
      <h2 class="section-title">系统特色</h2>
      <div class="features-grid">
        <el-card class="feature-card" shadow="hover">
          <el-icon :size="40" color="#409EFF"><Calendar /></el-icon>
          <h3>赛事管理</h3>
          <p>浏览各类体育赛事，获取赛事详情和最新动态，参与喜爱的赛事</p>
        </el-card>
        <el-card class="feature-card" shadow="hover">
          <el-icon :size="40" color="#67C23A"><DocumentChecked /></el-icon>
          <h3>在线报名</h3>
          <p>便捷的在线报名流程，轻松参与各类体育赛事，跟踪报名状态</p>
        </el-card>
        <el-card class="feature-card" shadow="hover">
          <el-icon :size="40" color="#E6A23C"><Bell /></el-icon>
          <h3>赛事公告</h3>
          <p>查看赛事重要公告和通知，及时了解赛事动态和变更信息</p>
        </el-card>
        <el-card class="feature-card" shadow="hover">
          <el-icon :size="40" color="#F56C6C"><User /></el-icon>
          <h3>个人中心</h3>
          <p>管理个人资料，查看报名历史，跟踪个人参与的赛事情况</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
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

<style scoped>
.dashboard-container {
  padding: 0 0 40px 0;
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero 部分 */
.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px 20px;
  margin-bottom: 40px;
  background: linear-gradient(135deg, #e0f7fa 0%, #b3e5fc 100%);
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.hero-content {
  flex: 1;
  padding-right: 20px;
  z-index: 1;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 16px;
  color: #333;
}

.hero-subtitle {
  font-size: 1.2rem;
  margin-bottom: 24px;
  color: #666;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.hero-image img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 40px;
  padding: 0 20px;
}

.stats-card {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 100%;
  transition: transform 0.3s;
}

.stats-card:hover {
  transform: translateY(-5px);
}

.stats-icon {
  margin-right: 20px;
  padding: 15px;
  border-radius: 10px;
  background-color: rgba(64, 158, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stats-content {
  flex: 1;
}

.stats-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

/* 主要内容 */
.main-content {
  margin-bottom: 40px;
  padding: 0 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
}

.event-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: transform 0.3s;
}

.event-item:hover {
  transform: translateX(5px);
}

.event-item:last-child {
  border-bottom: none;
}

.event-name {
  font-size: 16px;
  margin: 0 0 5px 0;
  font-weight: 500;
  color: #303133;
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
  color: #409EFF;
}

.announcement-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: transform 0.3s;
}

.announcement-item:hover {
  transform: translateX(5px);
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
  color: #303133;
}

.announcement-date {
  font-size: 12px;
  color: #999;
}

.registration-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: transform 0.3s;
}

.registration-item:hover {
  transform: translateX(5px);
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
  color: #303133;
}

/* 特色部分 */
.features-section {
  padding: 0 20px;
}

.section-title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 30px;
  color: #303133;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background-color: #409EFF;
  border-radius: 2px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.feature-card {
  padding: 30px 20px;
  text-align: center;
  height: 100%;
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.feature-card h3 {
  margin: 20px 0 10px;
  font-size: 18px;
  color: #303133;
}

.feature-card p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.loading-placeholder, .empty-data {
  padding: 20px 0;
}

.mt-4 {
  margin-top: 16px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding: 30px 20px;
  }

  .hero-content {
    padding-right: 0;
    margin-bottom: 24px;
  }

  .hero-actions {
    justify-content: center;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .stats-card {
    margin-bottom: 16px;
  }
  
  .upcoming-events {
    margin-bottom: 16px;
  }
}
</style> 