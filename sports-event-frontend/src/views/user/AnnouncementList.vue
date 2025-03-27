<template>
  <div class="announcement-list-container">
    <div class="page-header">
      <h1>公告列表</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="announcements.length === 0" class="empty-container">
      <el-empty description="暂无公告" />
    </div>
    
    <div v-else class="announcements-list">
      <el-timeline>
        <el-timeline-item
          v-for="announcement in announcements"
          :key="announcement.id"
          :timestamp="formatDate(announcement.createdAt)"
          placement="top"
          :type="getTimelineItemType(announcement)"
        >
          <el-card class="announcement-card">
            <template #header>
              <div class="announcement-header">
                <h3 class="announcement-title">{{ announcement.title }}</h3>
                <div class="announcement-meta">
                  <span v-if="announcement.event" class="event-badge">
                    <el-tag size="small" effect="plain">
                      {{ announcement.event.name }}
                    </el-tag>
                  </span>
                  <span class="view-count">
                    <el-icon><View /></el-icon>
                    {{ announcement.viewCount || 0 }}
                  </span>
                </div>
              </div>
            </template>
            
            <div class="announcement-content" v-html="formatContent(announcement.content)"></div>
            
            <div v-if="announcement.attachments && announcement.attachments.length > 0" class="announcement-attachments">
              <h4 class="attachments-title">附件列表</h4>
              <div class="attachments-list">
                <div 
                  v-for="(attachment, index) in announcement.attachments" 
                  :key="index"
                  class="attachment-item"
                >
                  <el-link 
                    :href="attachment.url" 
                    target="_blank"
                    :underline="false"
                    type="primary"
                  >
                    <el-icon><Document /></el-icon>
                    <span>{{ attachment.name }}</span>
                    <span v-if="attachment.size" class="attachment-size">
                      ({{ formatFileSize(attachment.size) }})
                    </span>
                  </el-link>
                </div>
              </div>
            </div>
            
            <div class="announcement-footer">
              <div class="announcement-author" v-if="announcement.author">
                发布者: {{ announcement.author.username }}
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalElements"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { announcementAPI } from '../../api/announcementAPI';
import { View, Document } from '@element-plus/icons-vue';

// 数据状态
const announcements = ref<any[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);

// 监听分页变化
watch([currentPage, pageSize], () => {
  fetchAnnouncements();
});

// 初始化
onMounted(async () => {
  await fetchAnnouncements();
});

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true;
  try {
    const response = await announcementAPI.getPublicAnnouncements(
      currentPage.value - 1,
      pageSize.value
    );
    
    announcements.value = response.content;
    totalElements.value = response.totalElements;
  } catch (error) {
    console.error('获取公告列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
};

// 当前页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val;
};

// 获取时间线项目类型
const getTimelineItemType = (announcement: any) => {
  if (announcement.event) return 'primary';
  
  // 根据公告类型设置不同颜色
  if (announcement.type === 'IMPORTANT') return 'danger';
  if (announcement.type === 'NOTICE') return 'warning';
  
  return 'info';
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

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) return size + ' B';
  
  const kb = size / 1024;
  if (kb < 1024) return kb.toFixed(1) + ' KB';
  
  const mb = kb / 1024;
  if (mb < 1024) return mb.toFixed(1) + ' MB';
  
  const gb = mb / 1024;
  return gb.toFixed(1) + ' GB';
};

// 格式化内容
const formatContent = (content: string) => {
  if (!content) return '';
  
  // 转换URL为链接
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  let formattedContent = content.replace(urlRegex, (url) => {
    return `<a href="${url}" target="_blank" class="content-link">${url}</a>`;
  });
  
  // 将换行符转换为<br>标签
  formattedContent = formattedContent.replace(/\n/g, '<br>');
  
  return formattedContent;
};
</script>

<style scoped>
.announcement-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
}

.announcements-list {
  margin-bottom: 40px;
}

.announcement-card {
  margin-bottom: 16px;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.announcement-title {
  margin: 0;
  font-size: 18px;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.announcement-content {
  margin-bottom: 16px;
  line-height: 1.6;
}

.announcement-attachments {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.attachments-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #606266;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
}

.attachment-size {
  margin-left: 4px;
  font-size: 12px;
  color: #909399;
}

.announcement-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}

.announcement-author {
  font-size: 14px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

:deep(.content-link) {
  color: #409EFF;
  text-decoration: none;
}

:deep(.content-link:hover) {
  text-decoration: underline;
}
</style> 