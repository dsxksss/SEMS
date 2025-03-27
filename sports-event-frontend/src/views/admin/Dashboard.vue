<template>
  <div class="w-full max-w-full">
    <!-- 数据概览 -->
    <div class="grid grid-cols-1 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow-md overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center">
          <h3 class="text-lg font-medium text-gray-800">数据概览</h3>
          <div class="ml-2 text-xs text-gray-400">实时统计</div>
        </div>
        <div class="p-6">
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="1" animated />
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <!-- 用户总数 -->
            <div class="rounded-xl p-5 flex items-center transition-all duration-300 hover:scale-105 shadow-sm hover:shadow-md bg-gradient-to-br from-blue-500 to-blue-600 text-white relative overflow-hidden">
              <div class="absolute -right-6 -bottom-6 opacity-20">
                <el-icon class="text-7xl"><el-icon-user /></el-icon>
              </div>
              <div class="z-10">
                <p class="text-sm text-blue-100 font-medium">用户总数</p>
                <p class="text-3xl font-bold mt-1">{{ dashboardData.userCount }}</p>
                <div class="mt-2 text-xs text-blue-100">
                  <span class="inline-flex items-center">
                    <el-icon class="mr-1"><el-icon-arrow-up /></el-icon>
                    <span>30天内新增: 24</span>
                  </span>
                </div>
              </div>
            </div>
            
            <!-- 赛事总数 -->
            <div class="rounded-xl p-5 flex items-center transition-all duration-300 hover:scale-105 shadow-sm hover:shadow-md bg-gradient-to-br from-emerald-500 to-green-600 text-white relative overflow-hidden">
              <div class="absolute -right-6 -bottom-6 opacity-20">
                <el-icon class="text-7xl"><el-icon-trophy /></el-icon>
              </div>
              <div class="z-10">
                <p class="text-sm text-green-100 font-medium">赛事总数</p>
                <p class="text-3xl font-bold mt-1">{{ dashboardData.eventCount }}</p>
                <div class="mt-2 text-xs text-green-100">
                  <span class="inline-flex items-center">
                    <el-icon class="mr-1"><el-icon-calendar /></el-icon>
                    <span>进行中: {{ dashboardData.recentEvents.filter(e => e.status === 'ONGOING').length }}</span>
                  </span>
                </div>
              </div>
            </div>
            
            <!-- 报名总数 -->
            <div class="rounded-xl p-5 flex items-center transition-all duration-300 hover:scale-105 shadow-sm hover:shadow-md bg-gradient-to-br from-amber-500 to-orange-600 text-white relative overflow-hidden">
              <div class="absolute -right-6 -bottom-6 opacity-20">
                <el-icon class="text-7xl"><el-icon-document /></el-icon>
              </div>
              <div class="z-10">
                <p class="text-sm text-amber-100 font-medium">报名总数</p>
                <p class="text-3xl font-bold mt-1">{{ dashboardData.registrationCount }}</p>
                <div class="mt-2 text-xs text-amber-100">
                  <span class="inline-flex items-center">
                    <el-icon class="mr-1"><el-icon-tickets /></el-icon>
                    <span>待审核: {{ dashboardData.recentRegistrations.filter(r => r.status === 'PENDING').length }}</span>
                  </span>
                </div>
              </div>
            </div>
            
            <!-- 完赛数 -->
            <div class="rounded-xl p-5 flex items-center transition-all duration-300 hover:scale-105 shadow-sm hover:shadow-md bg-gradient-to-br from-purple-500 to-indigo-600 text-white relative overflow-hidden">
              <div class="absolute -right-6 -bottom-6 opacity-20">
                <el-icon class="text-7xl"><el-icon-data-line /></el-icon>
              </div>
              <div class="z-10">
                <p class="text-sm text-purple-100 font-medium">完赛数</p>
                <p class="text-3xl font-bold mt-1">{{ dashboardData.completedEventCount }}</p>
                <div class="mt-2 text-xs text-purple-100">
                  <span class="inline-flex items-center">
                    <el-icon class="mr-1"><el-icon-medal /></el-icon>
                    <span>占比: {{ Math.round((dashboardData.completedEventCount / dashboardData.eventCount) * 100) || 0 }}%</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近赛事和报名 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 最近赛事 -->
      <div class="bg-white rounded-lg shadow-md overflow-hidden border border-gray-100">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gradient-to-r from-blue-50 to-transparent">
          <div class="flex items-center">
            <el-icon class="text-blue-500 mr-2"><el-icon-trophy /></el-icon>
            <h3 class="text-lg font-medium text-gray-800">最近赛事</h3>
          </div>
          <el-button type="primary" size="small" @click="router.push('/admin/events/list')">
            查看更多
          </el-button>
        </div>
        <div class="p-4">
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="4" animated />
          </div>
          <el-empty v-else-if="dashboardData.recentEvents.length === 0" description="暂无赛事数据" />
          <el-table v-else :data="dashboardData.recentEvents" style="width: 100%" :header-cell-class-name="'bg-gray-50'" class="rounded-md">
            <el-table-column prop="name" label="赛事名称">
              <template #default="{ row }">
                <div class="font-medium text-gray-700">{{ row.name }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="100">
              <template #default="{ row }">
                <span class="text-gray-500 text-sm px-2 py-1 bg-gray-100 rounded-full">{{ row.categoryName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="日期" width="120">
              <template #default="{ row }">
                <div class="text-gray-600">{{ row.startDate }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" effect="light" class="rounded-full" size="small">
                  {{ formatStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 最近报名 -->
      <div class="bg-white rounded-lg shadow-md overflow-hidden border border-gray-100">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gradient-to-r from-green-50 to-transparent">
          <div class="flex items-center">
            <el-icon class="text-green-500 mr-2"><el-icon-document /></el-icon>
            <h3 class="text-lg font-medium text-gray-800">最近报名</h3>
          </div>
          <el-button type="success" size="small" @click="router.push('/admin/registrations')">
            查看更多
          </el-button>
        </div>
        <div class="p-4">
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="4" animated />
          </div>
          <el-empty v-else-if="dashboardData.recentRegistrations.length === 0" description="暂无报名数据" />
          <el-table v-else :data="dashboardData.recentRegistrations" style="width: 100%" :header-cell-class-name="'bg-gray-50'" class="rounded-md">
            <el-table-column prop="username" label="用户">
              <template #default="{ row }">
                <div class="font-medium text-gray-700">{{ row.username }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="eventName" label="赛事">
              <template #default="{ row }">
                <div class="text-gray-700">{{ row.eventName }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="registrationDate" label="报名日期" width="120">
              <template #default="{ row }">
                <div class="text-gray-600">{{ row.registrationDate }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getRegistrationStatusType(row.status)" effect="light" class="rounded-full" size="small">
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
      <div class="bg-white rounded-lg shadow-md overflow-hidden border border-gray-100">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gradient-to-r from-purple-50 to-transparent">
          <div class="flex items-center">
            <el-icon class="text-purple-500 mr-2"><el-icon-bell /></el-icon>
            <h3 class="text-lg font-medium text-gray-800">系统公告</h3>
          </div>
          <el-button type="primary" color="#8957e5" size="small" @click="router.push('/admin/announcements')">
            管理公告
          </el-button>
        </div>
        <div class="p-4">
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton style="width: 100%" :rows="3" animated />
          </div>
          <el-empty v-else-if="dashboardData.recentAnnouncements.length === 0" description="暂无公告数据" />
          <el-table v-else :data="dashboardData.recentAnnouncements" style="width: 100%" :header-cell-class-name="'bg-gray-50'" class="rounded-md">
            <el-table-column prop="title" label="标题">
              <template #default="{ row }">
                <div class="font-medium text-gray-700">{{ row.title }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" :show-overflow-tooltip="true">
              <template #default="{ row }">
                <div class="text-gray-600 line-clamp-1">{{ row.content }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="createdDate" label="发布日期" width="120">
              <template #default="{ row }">
                <div class="text-gray-500 text-sm">{{ row.createdDate }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="authorName" label="发布者" width="100">
              <template #default="{ row }">
                <div class="px-2 py-1 bg-purple-50 text-purple-700 rounded-full text-xs inline-block">{{ row.authorName }}</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default>
                <el-button type="primary" color="#8957e5" size="small" text>查看详情</el-button>
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