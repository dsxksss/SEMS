<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 数据概览 -->
      <el-col :span="24">
        <el-card shadow="hover" class="data-overview">
          <div class="card-header">
            <h3>数据概览</h3>
          </div>
          <el-row :gutter="20" class="card-content">
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="data-item">
                <div class="icon blue">
                  <el-icon><el-icon-user /></el-icon>
                </div>
                <div class="info">
                  <p class="title">用户总数</p>
                  <p class="value">125</p>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="data-item">
                <div class="icon green">
                  <el-icon><el-icon-trophy /></el-icon>
                </div>
                <div class="info">
                  <p class="title">赛事总数</p>
                  <p class="value">32</p>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="data-item">
                <div class="icon orange">
                  <el-icon><el-icon-document /></el-icon>
                </div>
                <div class="info">
                  <p class="title">报名总数</p>
                  <p class="value">254</p>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
              <div class="data-item">
                <div class="icon purple">
                  <el-icon><el-icon-data-line /></el-icon>
                </div>
                <div class="info">
                  <p class="title">完赛数</p>
                  <p class="value">18</p>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <!-- 最近赛事 -->
      <el-col :span="12">
        <el-card shadow="hover" class="recent-events">
          <div class="card-header">
            <h3>最近赛事</h3>
            <el-button type="primary" size="small" plain @click="router.push('/admin/events/list')">
              查看更多
            </el-button>
          </div>
          <div class="card-content">
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
        </el-card>
      </el-col>

      <!-- 最近报名 -->
      <el-col :span="12">
        <el-card shadow="hover" class="recent-registrations">
          <div class="card-header">
            <h3>最近报名</h3>
            <el-button type="primary" size="small" plain @click="router.push('/admin/registrations')">
              查看更多
            </el-button>
          </div>
          <div class="card-content">
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
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <!-- 系统公告 -->
      <el-col :span="24">
        <el-card shadow="hover" class="announcements">
          <div class="card-header">
            <h3>系统公告</h3>
            <el-button type="primary" size="small" plain @click="router.push('/admin/announcements')">
              管理公告
            </el-button>
          </div>
          <div class="card-content">
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
        </el-card>
      </el-col>
    </el-row>
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

<style scoped>
.dashboard {
  width: 100%;
  max-width: 100%;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.data-overview .card-content {
  margin-top: 15px;
}

.data-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 4px;
  background-color: #f9f9f9;
  transition: all 0.3s;
}

.data-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.data-item .icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.icon.blue {
  background-color: #409eff;
}

.icon.green {
  background-color: #67c23a;
}

.icon.orange {
  background-color: #e6a23c;
}

.icon.purple {
  background-color: #8e44ad;
}

.data-item .info {
  flex: 1;
}

.data-item .info .title {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.data-item .info .value {
  margin: 5px 0 0;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.card-content {
  margin-top: 10px;
}
</style> 