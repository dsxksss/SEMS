<template>
  <div>
    <!-- 页面标题和搜索栏 -->
    <div class="mb-8 flex flex-col gap-4 sm:flex-row sm:justify-between sm:items-center">
      <h1 class="text-2xl font-bold text-gray-800">公告列表</h1>
      <div class="flex gap-2">
        <el-input
          v-model="searchQuery"
          placeholder="搜索公告"
          class="min-w-[200px]"
          clearable
          @clear="fetchAnnouncements"
          @keyup.enter="fetchAnnouncements"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <button 
          class="px-4 py-2 bg-indigo-600 text-white font-medium rounded-md shadow-sm hover:bg-indigo-700 transition-colors flex items-center gap-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-50"
          @click="fetchAnnouncements"
        >
          <el-icon><Search /></el-icon> 搜索
        </button>
      </div>
    </div>

    <!-- 公告列表卡片 -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden mb-8">
      <div class="flex justify-between items-center px-6 py-4 border-b border-gray-100">
        <div class="flex items-center gap-2 text-gray-800 font-medium">
          <el-icon class="text-indigo-500" :size="20"><Bell /></el-icon>
          <span>系统公告</span>
        </div>
        <el-radio-group 
          v-model="timeFilter" 
          @change="fetchAnnouncements" 
          size="small"
        >
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="py-10 px-6">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="announcements.length === 0" class="py-16 flex justify-center">
        <el-empty description="暂无公告" />
      </div>

      <!-- 公告列表 -->
      <div v-else>
        <div 
          v-for="announcement in announcements" 
          :key="announcement.id" 
          class="border-b border-gray-100 last:border-b-0 px-6 py-4 hover:bg-gray-50 cursor-pointer transition-colors duration-200"
          @click="openAnnouncement(announcement)"
        >
          <div class="flex justify-between mb-2">
            <div class="flex items-center gap-2">
              <h3 class="text-lg font-medium text-gray-800">{{ announcement.title }}</h3>
              <el-tag 
                v-if="isNew(announcement.createdAt)" 
                size="small" 
                type="danger"
                class="!rounded-full"
              >新</el-tag>
            </div>
            <span class="text-sm text-gray-500">{{ formatDateTime(announcement.createdAt) }}</span>
          </div>
          <div 
            class="text-gray-600 mb-3 text-sm line-clamp-3"
            v-html="processContentForPreview(announcement.content)"
          ></div>
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-500">发布者：{{ getPublisherName(announcement.createdBy) }}</span>
            <button 
              class="text-indigo-600 font-medium hover:text-indigo-800 transition-colors focus:outline-none text-sm" 
            >
              阅读全文
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
        class="pagination-with-bg"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentAnnouncement.title"
      width="60%"
      destroy-on-close
    >
      <div class="flex justify-between text-sm text-gray-500 pb-4 mb-6 border-b border-gray-200">
        <span>发布时间：{{ formatDateTime(currentAnnouncement.createdAt) }}</span>
        <span>发布者：{{ getPublisherName(currentAnnouncement.createdBy) }}</span>
      </div>
      <div class="prose max-w-none" v-html="currentAnnouncement.content"></div>
      <template #footer>
        <button 
          @click="dialogVisible = false"
          class="px-4 py-2 bg-white text-gray-700 font-medium border border-gray-300 rounded-md hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-300 focus:ring-opacity-50"
        >
          关闭
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Bell, Search } from '@element-plus/icons-vue';
import { announcementAPI, type Announcement } from '../../api/announcementAPI';
import { formatDateTime } from '../../utils/formatter';

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

interface Publisher {
  realName?: string;
  username?: string;
  [key: string]: any;
}

// 判断是否为新公告（3天内）
const isNew = (date: string) => {
  const announcementDate = new Date(date);
  const now = new Date();
  const diffTime = now.getTime() - announcementDate.getTime();
  const diffDays = diffTime / (1000 * 60 * 60 * 24);
  return diffDays < 3;
};

// 获取发布者姓名
const getPublisherName = (publisher: string | Publisher | undefined): string => {
  // 如果未定义
  if (!publisher) return '系统';
  
  // 如果是字符串，可能已经是姓名
  if (typeof publisher === 'string') {
    // 尝试解析JSON字符串
    try {
      const obj = JSON.parse(publisher) as Publisher;
      if (obj.realName) {
        return obj.realName;
      } else if (obj.username) {
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
  return publisher.realName || publisher.username || '系统';
};

// 截断内容
const truncateContent = (content: string, maxLength = 150) => {
  if (content.length <= maxLength) return content;
  return content.substring(0, maxLength) + '...';
};

// 处理内容预览，保留图片并截断文本
const processContentForPreview = (content: string) => {
  if (!content) return '';
  
  // 先处理图片URL，确保路径正确
  let processedContent = content;
  const imgRegex = /<img[^>]+src="([^"]+)"/g;
  processedContent = processedContent.replace(imgRegex, (match, url) => {
    // 移除URL中可能存在的token参数
    let cleanUrl = url.split('?')[0];
    
    // 确保图片URL包含/api前缀
    if (cleanUrl.includes('/files/download/') && !cleanUrl.startsWith('/api')) {
      cleanUrl = `/api${cleanUrl}`;
    }
    
    return match.replace(url, cleanUrl);
  });
  
  // 提取第一张图片（如果有）
  let firstImage = '';
  const imgMatch = processedContent.match(/<img[^>]+>/);
  if (imgMatch) {
    firstImage = imgMatch[0];
  }
  
  // 提取纯文本内容
  const textWithoutTags = processedContent.replace(/<[^>]+>/g, ' ').trim();
  let truncatedText = truncateContent(textWithoutTags, 150);
  
  // 返回图片和截断的文本
  if (firstImage) {
    return `${firstImage}<div class="mt-2">${truncatedText}</div>`;
  } else {
    return `<div>${truncatedText}</div>`;
  }
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
    
    // 验证并处理公告数据
    if (response && response.content && Array.isArray(response.content)) {
      announcements.value = response.content.map(announcement => {
        // 处理可能为null或undefined的字段
        return {
          ...announcement,
          title: announcement.title || '无标题',
          content: announcement.content || '无内容',
          createdAt: announcement.createdAt || '',
          createdBy: announcement.createdBy || '系统'
        };
      });
      total.value = response.totalElements || 0;
    } else {
      announcements.value = [];
      total.value = 0;
      console.warn('公告数据格式异常:', response);
    }
  } catch (error) {
    console.error('获取公告失败:', error);
    ElMessage.error('获取公告列表失败，请稍后重试');
    announcements.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 打开公告详情
const openAnnouncement = (announcement: Announcement) => {
  // 处理内容中的图片URL，确保使用正确的路径格式
  let processedContent = announcement.content || '';
  if (processedContent) {
    // 查找所有图片URL，确保路径正确
    const imgRegex = /<img[^>]+src="([^"]+)"/g;
    processedContent = processedContent.replace(imgRegex, (match, url) => {
      // 移除URL中可能存在的token参数
      let cleanUrl = url.split('?')[0];
      
      // 确保图片URL包含/api前缀
      if (cleanUrl.includes('/files/download/') && !cleanUrl.startsWith('/api')) {
        cleanUrl = `/api${cleanUrl}`;
      }
      
      return match.replace(url, cleanUrl);
    });
  }

  // 确保公告对象完整
  currentAnnouncement.value = {
    ...announcement,
    title: announcement.title || '无标题',
    content: processedContent || announcement.content,
    createdAt: announcement.createdAt || '',
    createdBy: announcement.createdBy || '系统'
  };
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
:deep(.pagination-with-bg .el-pager li.is-active) {
  background-color: #4f46e5;
  border-color: #4f46e5;
  color: white;
}

:deep(.pagination-with-bg .el-pager li:hover:not(.is-active)) {
  color: #4f46e5;
}

:deep(img) {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
  border-radius: 0.375rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #4f46e5 !important;
  border-color: #4f46e5 !important;
  box-shadow: -1px 0 0 0 #4f46e5 !important;
}

:deep(.el-radio-button__inner:hover) {
  color: #4f46e5;
}

:deep(.prose) {
  line-height: 1.7;
  color: #374151;
}

:deep(.prose h1, .prose h2, .prose h3, .prose h4, .prose h5, .prose h6) {
  margin-top: 1.25em;
  margin-bottom: 0.75em;
  font-weight: 600;
  color: #111827;
}

:deep(.prose p) {
  margin-bottom: 1.25em;
}

:deep(.line-clamp-3) {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 