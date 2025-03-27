<template>
  <div class="announcement-list-container">
    <div class="page-header">
      <h2>公告列表</h2>
      <div class="header-actions">
        <el-input
          v-model="searchQuery"
          placeholder="搜索公告"
          prefix-icon="Search"
          clearable
          @clear="fetchAnnouncements"
          @keyup.enter="fetchAnnouncements"
        />
        <el-button type="primary" @click="fetchAnnouncements">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>
    </div>

    <el-card class="announcement-list-card">
      <template #header>
        <div class="card-header">
          <div class="title-section">
            <el-icon><Bell /></el-icon>
            <span>系统公告</span>
          </div>
          <el-radio-group v-model="timeFilter" @change="fetchAnnouncements" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <el-empty v-else-if="announcements.length === 0" description="暂无公告" />

      <template v-else>
        <div class="announcement-item" v-for="announcement in announcements" :key="announcement.id" @click="openAnnouncement(announcement)">
          <div class="item-header">
            <div class="title-wrapper">
              <h3 class="announcement-title">{{ announcement.title }}</h3>
              <el-tag v-if="isNew(announcement.createdAt)" size="small" type="danger">新</el-tag>
            </div>
            <span class="announcement-date">{{ formatDateTime(announcement.createdAt) }}</span>
          </div>
          <div class="announcement-content-preview">
            {{ truncateContent(announcement.content) }}
          </div>
          <div class="announcement-footer">
            <span class="publisher">发布者：{{ getPublisherName(announcement.createdBy) }}</span>
            <el-button link type="primary" size="small">阅读全文</el-button>
          </div>
        </div>
      </template>
    </el-card>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="currentAnnouncement.title"
      width="60%"
      class="announcement-dialog"
    >
      <div class="announcement-meta">
        <span>发布时间：{{ formatDateTime(currentAnnouncement.createdAt) }}</span>
        <span>发布者：{{ getPublisherName(currentAnnouncement.createdBy) }}</span>
      </div>
      <div class="announcement-full-content">
        {{ currentAnnouncement.content }}
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Bell, Search } from '@element-plus/icons-vue';
import { announcementAPI, type Announcement } from '../../api/announcementAPI';
import { formatDateTime, getRelativeTime } from '../../utils/formatter';

// 数据
const announcements = ref<Announcement[]>([]);
const loading = ref(true);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchQuery = ref('');
const timeFilter = ref('all');
const dialogVisible = ref(false);
const currentAnnouncement = ref<Announcement>({} as Announcement);

// 判断是否为新公告（3天内）
const isNew = (date: string) => {
  const announcementDate = new Date(date);
  const now = new Date();
  const diffTime = now.getTime() - announcementDate.getTime();
  const diffDays = diffTime / (1000 * 60 * 60 * 24);
  return diffDays < 3;
};

// 获取发布者姓名
const getPublisherName = (publisher: any): string => {
  // 如果是字符串，可能已经是姓名
  if (typeof publisher === 'string') {
    // 尝试解析JSON字符串
    try {
      const obj = JSON.parse(publisher);
      if (obj && obj.realName) {
        return obj.realName;
      } else if (obj && obj.username) {
        return obj.username;
      }
      // 无法解析有效信息，返回原字符串
      return publisher;
    } catch (e) {
      // 不是有效JSON，直接返回原字符串
      return publisher;
    }
  }
  
  // 如果是对象，直接获取属性
  if (publisher && typeof publisher === 'object') {
    return publisher.realName || publisher.username || '系统';
  }
  
  // 默认返回
  return '系统';
};

// 截断内容
const truncateContent = (content: string, maxLength = 150) => {
  if (content.length <= maxLength) return content;
  return content.substring(0, maxLength) + '...';
};

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true;
  try {
    // 准备查询参数
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    };

    // 添加搜索关键词
    if (searchQuery.value) {
      params.keyword = searchQuery.value;
    }

    // 添加时间过滤
    if (timeFilter.value !== 'all') {
      const now = new Date();
      let startDate = new Date();
      
      if (timeFilter.value === 'week') {
        // 设置为本周开始
        startDate.setDate(now.getDate() - now.getDay());
      } else if (timeFilter.value === 'month') {
        // 设置为本月开始
        startDate.setDate(1);
      }
      
      params.startDate = startDate.toISOString().split('T')[0];
      params.endDate = now.toISOString().split('T')[0];
    }

    const response = await announcementAPI.getAnnouncements(params);
    announcements.value = response.content;
    total.value = response.totalElements;
  } catch (error) {
    console.error('获取公告失败:', error);
    ElMessage.error('获取公告列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 打开公告详情
const openAnnouncement = (announcement: Announcement) => {
  currentAnnouncement.value = announcement;
  dialogVisible.value = true;
};

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  fetchAnnouncements();
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  fetchAnnouncements();
};

// 初始化
onMounted(() => {
  fetchAnnouncements();
});
</script>

<style scoped>
.announcement-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.announcement-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.announcement-item {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item:hover {
  background-color: #f5f7fa;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.announcement-date {
  font-size: 13px;
  color: #909399;
}

.announcement-content-preview {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 10px;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.publisher {
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.announcement-meta {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 15px;
  color: #909399;
  font-size: 14px;
}

.announcement-full-content {
  padding: 10px 0;
  font-size: 15px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.loading-container {
  padding: 20px 0;
}
</style> 