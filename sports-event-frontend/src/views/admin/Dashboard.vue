<template>
  <div class="w-full max-w-full">
    <!-- 数据概览 -->
    <div class="grid grid-cols-1 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100">
          <h3 class="text-lg font-medium text-gray-800">数据概览</h3>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <!-- 用户总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-blue-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-user /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">用户总数</p>
                <p class="text-xl font-bold text-gray-800">125</p>
              </div>
            </div>
            
            <!-- 赛事总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-green-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-trophy /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">赛事总数</p>
                <p class="text-xl font-bold text-gray-800">32</p>
              </div>
            </div>
            
            <!-- 报名总数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-amber-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-document /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">报名总数</p>
                <p class="text-xl font-bold text-gray-800">254</p>
              </div>
            </div>
            
            <!-- 完赛数 -->
            <div class="bg-gray-50 rounded-lg p-4 flex items-center transform transition-transform hover:translate-y-[-4px] hover:shadow-md">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-purple-500 text-white mr-4">
                <el-icon class="text-xl"><el-icon-data-line /></el-icon>
              </div>
              <div>
                <p class="text-sm text-gray-500">完赛数</p>
                <p class="text-xl font-bold text-gray-800">18</p>
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
          <el-table :data="recentEvents" stripe style="width: 100%">
            <el-table-column prop="name" label="赛事名称" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ scope.row.status }}
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
          <el-table :data="recentRegistrations" stripe style="width: 100%">
            <el-table-column prop="username" label="用户" />
            <el-table-column prop="event" label="赛事" />
            <el-table-column prop="date" label="报名日期" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getRegistrationStatusType(scope.row.status)">
                  {{ scope.row.status }}
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
          <el-table :data="announcements" stripe style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容" :show-overflow-tooltip="true" />
            <el-table-column prop="date" label="发布日期" width="120" />
            <el-table-column prop="author" label="发布者" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
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
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 示例数据 - 实际应从API获取
const recentEvents = ref([
  { name: '2023年校园马拉松', category: '田径', date: '2023-05-15', status: '进行中' },
  { name: '篮球联赛', category: '球类', date: '2023-06-01', status: '未开始' },
  { name: '游泳比赛', category: '水上', date: '2023-04-20', status: '已结束' },
  { name: '校园足球杯', category: '球类', date: '2023-05-10', status: '报名中' }
]);

const recentRegistrations = ref([
  { username: '张三', event: '2023年校园马拉松', date: '2023-04-15', status: '已确认' },
  { username: '李四', event: '篮球联赛', date: '2023-04-20', status: '待审核' },
  { username: '王五', event: '游泳比赛', date: '2023-04-18', status: '已确认' },
  { username: '赵六', event: '校园足球杯', date: '2023-04-22', status: '已拒绝' }
]);

const announcements = ref([
  { 
    title: '关于2023年校园体育节的通知', 
    content: '我校将于5月举办2023年校园体育节，欢迎各位同学积极参与。', 
    date: '2023-04-10', 
    author: '体育部' 
  },
  { 
    title: '特邀嘉宾讲座通知', 
    content: '奥运冠军将于下周来校进行体育精神讲座', 
    date: '2023-04-12', 
    author: '学工处' 
  },
  { 
    title: '体育场地维修通知', 
    content: '田径场将于4月25日至4月30日进行维修，期间暂停使用。', 
    date: '2023-04-15', 
    author: '后勤部' 
  }
]);

// 获取赛事状态对应的标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case '进行中': return 'success';
    case '未开始': return 'info';
    case '已结束': return 'danger';
    case '报名中': return 'warning';
    default: return 'info';
  }
};

// 获取报名状态对应的标签类型
const getRegistrationStatusType = (status: string) => {
  switch (status) {
    case '已确认': return 'success';
    case '待审核': return 'warning';
    case '已拒绝': return 'danger';
    default: return 'info';
  }
};
</script> 