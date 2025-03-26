<template>
  <div class="w-full max-w-full">
    <!-- 数据概览 -->
    <div class="grid grid-cols-1 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100">
          <h3 class="text-lg font-medium text-gray-800">数据概览</h3>
        </div>
        <div class="p-6">
          <div v-if="loading.stats" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="1" animated />
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <!-- 用户总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-blue-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-user /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">用户总数</p>
                <p class="text-xl font-bold text-gray-800">{{ stats.userCount }}</p>
              </div>
            </div>
            
            <!-- 赛事总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-green-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-trophy /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">赛事总数</p>
                <p class="text-xl font-bold text-gray-800">{{ stats.eventCount }}</p>
              </div>
            </div>
            
            <!-- 报名总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-amber-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-document /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">报名总数</p>
                <p class="text-xl font-bold text-gray-800">{{ stats.registrationCount }}</p>
              </div>
            </div>
            
            <!-- 完赛数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-purple-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-data-line /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">完赛数</p>
                <p class="text-xl font-bold text-gray-800">{{ stats.completedEventCount }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近赛事和报名 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 最近赛事 -->
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-lg font-medium text-gray-800">最近赛事</h3>
          <el-button type="primary" size="small" plain @click="router.push('/admin/events/list')">
            查看更多
          </el-button>
        </div>
        <div class="p-4">
          <div v-if="loading.events" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="4" animated />
          </div>
          <el-empty v-else-if="recentEvents.length === 0" description="暂无赛事数据" />
          <el-table v-else :data="recentEvents" stripe style="width: 100%">
            <el-table-column prop="name" label="赛事名称" />
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column prop="startDate" label="日期" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ formatStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 最近报名 -->
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-lg font-medium text-gray-800">最近报名</h3>
          <el-button type="primary" size="small" plain @click="router.push('/admin/registrations')">
            查看更多
          </el-button>
        </div>
        <div class="p-4">
          <div v-if="loading.registrations" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="4" animated />
          </div>
          <el-empty v-else-if="recentRegistrations.length === 0" description="暂无报名数据" />
          <el-table v-else :data="recentRegistrations" stripe style="width: 100%">
            <el-table-column prop="username" label="用户" />
            <el-table-column prop="eventName" label="赛事" />
            <el-table-column prop="registrationDate" label="报名日期" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getRegistrationStatusType(row.status)">
                  {{ formatRegistrationStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 系统公告 -->
    <div class="grid grid-cols-1 gap-6">
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-lg font-medium text-gray-800">系统公告</h3>
          <el-button type="primary" size="small" plain @click="router.push('/admin/announcements')">
            管理公告
          </el-button>
        </div>
        <div class="p-4">
          <div v-if="loading.announcements" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="3" animated />
          </div>
          <el-empty v-else-if="announcements.length === 0" description="暂无公告数据" />
          <el-table v-else :data="announcements" stripe style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容" :show-overflow-tooltip="true" />
            <el-table-column prop="createdDate" label="发布日期" width="120" />
            <el-table-column prop="authorName" label="发布者" width="100" />
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button type="primary" size="small" plain>详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  userAPI, 
  eventsAPI, 
  registrationAPI, 
  announcementAPI 
} from '../../api';
import dayjs from 'dayjs';

const router = useRouter();

// 加载状态
const loading = ref({
  stats: true,
  events: true,
  registrations: true,
  announcements: true
});

// 统计数据
const stats = ref({
  userCount: 0,
  eventCount: 0,
  registrationCount: 0,
  completedEventCount: 0
});

// 定义数据类型接口
interface RecentEvent {
  id: number;
  name: string;
  categoryName: string;
  startDate: string;
  status: 'ONGOING' | 'UPCOMING' | 'COMPLETED' | 'CANCELLED';
}

interface RecentRegistration {
  id: number;
  username: string;
  eventName: string;
  registrationDate: string;
  status: 'APPROVED' | 'PENDING' | 'REJECTED';
}

interface Announcement {
  id: number;
  title: string;
  content: string;
  createdDate: string;
  authorName: string;
}

// 最近赛事
const recentEvents = ref<RecentEvent[]>([]);

// 最近报名
const recentRegistrations = ref<RecentRegistration[]>([]);

// 系统公告
const announcements = ref<Announcement[]>([]);

// 获取统计数据
const fetchStats = async () => {
  loading.value.stats = true;
  try {
    // 用户总数
    const users = await userAPI.getAllUsers();
    stats.value.userCount = users.length;
    
    // 赛事相关数据
    const events = await eventsAPI.getAllEvents();
    stats.value.eventCount = events.length;
    stats.value.completedEventCount = events.filter(e => e.status === 'COMPLETED').length;
    
    // 报名总数
    const registrations = await registrationAPI.getAllRegistrations();
    stats.value.registrationCount = registrations.length;
  } catch (error) {
    console.error('获取统计数据失败:', error);
    ElMessage.error('获取统计数据失败');
  } finally {
    loading.value.stats = false;
  }
};

// 获取最近赛事
const fetchRecentEvents = async () => {
  loading.value.events = true;
  try {
    const events = await eventsAPI.getAllEvents();
    recentEvents.value = events.slice(0, 5).map(event => ({
      id: event.id,
      name: event.name,
      categoryName: event.category.name,
      startDate: dayjs(event.startTime).format('YYYY-MM-DD'),
      status: event.status
    }));
  } catch (error) {
    console.error('获取最近赛事失败:', error);
    ElMessage.error('获取最近赛事失败');
  } finally {
    loading.value.events = false;
  }
};

// 获取最近报名
const fetchRecentRegistrations = async () => {
  loading.value.registrations = true;
  try {
    const registrations = await registrationAPI.getAllRegistrations();
    recentRegistrations.value = registrations.slice(0, 5).map(reg => ({
      id: reg.id,
      username: reg.user.username,
      eventName: reg.event.name,
      registrationDate: dayjs(reg.createdAt).format('YYYY-MM-DD'),
      status: reg.status
    }));
  } catch (error) {
    console.error('获取最近报名失败:', error);
    ElMessage.error('获取最近报名失败');
  } finally {
    loading.value.registrations = false;
  }
};

// 获取系统公告
const fetchAnnouncements = async () => {
  loading.value.announcements = true;
  try {
    const latestAnnouncements = await announcementAPI.getAllAnnouncements();
    announcements.value = latestAnnouncements.slice(0, 5).map(ann => ({
      id: ann.id,
      title: ann.title,
      content: ann.content,
      createdDate: dayjs(ann.createdAt).format('YYYY-MM-DD'),
      authorName: ann.author && ann.author.username ? ann.author.username : '未知用户'
    }));
  } catch (error) {
    console.error('获取系统公告失败:', error);
    ElMessage.error('获取系统公告失败');
  } finally {
    loading.value.announcements = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  fetchStats();
  fetchRecentEvents();
  fetchRecentRegistrations();
  fetchAnnouncements();
});

// 获取赛事状态对应的标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'ONGOING': return 'success';
    case 'UPCOMING': return 'info';
    case 'COMPLETED': return 'danger';
    case 'CANCELLED': return 'warning';
    default: return 'info';
  }
};

// 格式化赛事状态
const formatStatus = (status: string) => {
  switch (status) {
    case 'ONGOING': return '进行中';
    case 'UPCOMING': return '未开始';
    case 'COMPLETED': return '已结束';
    case 'CANCELLED': return '已取消';
    default: return status;
  }
};

// 获取报名状态对应的标签类型
const getRegistrationStatusType = (status: string) => {
  switch (status) {
    case 'APPROVED': return 'success';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'danger';
    default: return 'info';
  }
};

// 格式化报名状态
const formatRegistrationStatus = (status: string) => {
  switch (status) {
    case 'APPROVED': return '已确认';
    case 'PENDING': return '待审核';
    case 'REJECTED': return '已拒绝';
    default: return status;
  }
};
</script> 