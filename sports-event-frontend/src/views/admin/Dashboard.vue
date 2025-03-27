<template>
  <div class="w-full max-w-full">
    <!-- 数据概览 -->
    <div class="grid grid-cols-1 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100">
          <h3 class="text-lg font-medium text-gray-800">数据概览</h3>
        </div>
        <div class="p-6">
          <div v-if="loading" class="flex justify-center py-8">
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
                <p class="text-xl font-bold text-gray-800">{{ dashboardData.userCount }}</p>
              </div>
            </div>
            
            <!-- 赛事总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-green-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-trophy /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">赛事总数</p>
                <p class="text-xl font-bold text-gray-800">{{ dashboardData.eventCount }}</p>
              </div>
            </div>
            
            <!-- 报名总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-amber-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-document /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">报名总数</p>
                <p class="text-xl font-bold text-gray-800">{{ dashboardData.registrationCount }}</p>
              </div>
            </div>
            
            <!-- 完赛数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-purple-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-data-line /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">完赛数</p>
                <p class="text-xl font-bold text-gray-800">{{ dashboardData.completedEventCount }}</p>
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
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="4" animated />
          </div>
          <el-empty v-else-if="dashboardData.recentEvents.length === 0" description="暂无赛事数据" />
          <el-table v-else :data="dashboardData.recentEvents" stripe style="width: 100%">
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
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="4" animated />
          </div>
          <el-empty v-else-if="dashboardData.recentRegistrations.length === 0" description="暂无报名数据" />
          <el-table v-else :data="dashboardData.recentRegistrations" stripe style="width: 100%">
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
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="3" animated />
          </div>
          <el-empty v-else-if="dashboardData.recentAnnouncements.length === 0" description="暂无公告数据" />
          <el-table v-else :data="dashboardData.recentAnnouncements" stripe style="width: 100%">
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
import { statsAPI } from '../../api';
import type { DashboardStats } from '../../api/statsAPI';

const router = useRouter();

// 加载状态
const loading = ref(true);

// 仪表盘数据
const dashboardData = ref<DashboardStats>({
  userCount: 0,
  eventCount: 0,
  registrationCount: 0,
  completedEventCount: 0,
  recentEvents: [],
  recentRegistrations: [],
  recentAnnouncements: []
});

// 获取仪表盘数据
const fetchDashboardData = async () => {
  loading.value = true;
  try {
    dashboardData.value = await statsAPI.getDashboardStats();
  } catch (error) {
    console.error('获取仪表盘数据失败:', error);
    ElMessage.error('获取仪表盘数据失败');
  } finally {
    loading.value = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  fetchDashboardData();
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