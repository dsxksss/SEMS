<template>
  <div class="event-list-container">
    <div class="page-header">
      <h1>赛事列表</h1>
      <div class="search-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索赛事名称"
          class="search-input"
          clearable
          @clear="resetSearch"
          @keyup.enter="searchEvents"
        >
          <template #append>
            <el-button @click="searchEvents">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        
        <el-select 
          v-model="selectedCategory" 
          placeholder="赛事分类" 
          clearable 
          @change="handleCategoryChange"
          class="category-filter"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
        
        <el-select 
          v-model="selectedStatus" 
          placeholder="赛事状态" 
          clearable 
          @change="handleStatusChange"
          class="status-filter"
        >
          <el-option
            v-for="status in statusOptions"
            :key="status.value"
            :label="status.label"
            :value="status.value"
          />
        </el-select>
      </div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="events.length === 0" class="empty-container">
      <el-empty description="未找到符合条件的赛事" />
    </div>
    
    <div v-else class="events-grid">
      <el-card 
        v-for="event in events" 
        :key="event.id" 
        class="event-card"
        shadow="hover"
      >
        <div class="event-card-content">
          <div class="event-status">
            <el-tag :type="getStatusType(event.status)">{{ getStatusText(event.status) }}</el-tag>
          </div>
          <h3 class="event-title">{{ event.name }}</h3>
          <p class="event-description">{{ truncateText(event.description, 100) }}</p>
          
          <div class="event-details">
            <div class="event-detail-item">
              <el-icon><Location /></el-icon>
              <span>{{ event.location }}</span>
            </div>
            <div class="event-detail-item">
              <el-icon><Timer /></el-icon>
              <span>{{ formatDate(event.startTime) }}</span>
            </div>
            <div class="event-detail-item">
              <el-icon><Calendar /></el-icon>
              <span>报名截止: {{ formatDate(event.registrationDeadline) }}</span>
            </div>
            <div class="event-detail-item">
              <el-icon><User /></el-icon>
              <span>人数上限: {{ event.maxParticipants }}人</span>
            </div>
          </div>
          
          <div class="event-category">
            <el-tag size="small" type="info">{{ event.category.name }}</el-tag>
          </div>
          
          <div class="event-actions">
            <router-link :to="`/user/events/${event.id}`">
              <el-button type="primary">查看详情</el-button>
            </router-link>
          </div>
        </div>
      </el-card>
    </div>
    
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
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { eventsAPI } from '../../api/eventsAPI';
import { categoryAPI } from '../../api/categoryAPI';
import { Search, Location, Timer, Calendar, User } from '@element-plus/icons-vue';
import type { EventCategory } from '../../api/eventsAPI';

// 状态数据
const events = ref<any[]>([]);
const categories = ref<EventCategory[]>([]);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const searchKeyword = ref('');
const selectedCategory = ref<number | null>(null);
const selectedStatus = ref<string | null>(null);

// 状态选项
const statusOptions = [
  { value: 'UPCOMING', label: '即将开始' },
  { value: 'ONGOING', label: '进行中' },
  { value: 'COMPLETED', label: '已结束' },
  { value: 'CANCELLED', label: '已取消' }
];

// 监听分页和筛选变化
watch([currentPage, pageSize], () => {
  fetchEvents();
});

// 初始化
onMounted(async () => {
  try {
    await Promise.all([
      fetchEvents(),
      fetchCategories()
    ]);
  } catch (error) {
    console.error('初始化数据失败:', error);
  }
});

// 获取赛事列表
const fetchEvents = async () => {
  loading.value = true;
  try {
    let response;
    
    // 根据不同的筛选条件调用不同的API
    if (searchKeyword.value) {
      // 搜索赛事
      response = await eventsAPI.searchPublicEvents(
        searchKeyword.value,
        currentPage.value - 1,
        pageSize.value
      );
    } else if (selectedCategory.value) {
      // 按分类筛选
      response = await eventsAPI.getEventsByCategory(
        selectedCategory.value,
        currentPage.value - 1,
        pageSize.value
      );
    } else if (selectedStatus.value) {
      // 按状态筛选
      response = await eventsAPI.getEventsByStatus(
        selectedStatus.value,
        currentPage.value - 1,
        pageSize.value
      );
    } else {
      // 获取所有公开赛事
      response = await eventsAPI.getPublicEvents(
        currentPage.value - 1,
        pageSize.value,
        'startTime',
        'asc'
      );
    }
    
    events.value = response.content;
    totalElements.value = response.totalElements;
  } catch (error) {
    console.error('获取赛事列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await categoryAPI.getAllActiveCategories();
    categories.value = response;
  } catch (error) {
    console.error('获取分类列表失败:', error);
  }
};

// 搜索赛事
const searchEvents = () => {
  currentPage.value = 1;
  selectedCategory.value = null;
  selectedStatus.value = null;
  fetchEvents();
};

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = '';
  fetchEvents();
};

// 分类变化处理
const handleCategoryChange = () => {
  currentPage.value = 1;
  searchKeyword.value = '';
  selectedStatus.value = null;
  fetchEvents();
};

// 状态变化处理
const handleStatusChange = () => {
  currentPage.value = 1;
  searchKeyword.value = '';
  selectedCategory.value = null;
  fetchEvents();
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

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'UPCOMING': return 'warning';
    case 'ONGOING': return 'success';
    case 'COMPLETED': return 'info';
    case 'CANCELLED': return 'danger';
    default: return '';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'UPCOMING': return '即将开始';
    case 'ONGOING': return '进行中';
    case 'COMPLETED': return '已结束';
    case 'CANCELLED': return '已取消';
    default: return status;
  }
};
</script>

<style scoped>
.event-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.search-filter {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  max-width: 700px;
}

.search-input {
  min-width: 250px;
}

.category-filter, .status-filter {
  min-width: 150px;
}

.events-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.event-card {
  height: 100%;
  transition: transform 0.2s;
}

.event-card:hover {
  transform: translateY(-5px);
}

.event-card-content {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.event-status {
  position: absolute;
  top: 0;
  right: 0;
}

.event-title {
  margin-top: 24px;
  margin-bottom: 10px;
  font-size: 18px;
}

.event-description {
  color: #666;
  margin-bottom: 16px;
  flex-grow: 1;
}

.event-details {
  margin-bottom: 16px;
}

.event-detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #666;
}

.event-detail-item .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.event-category {
  margin-bottom: 16px;
}

.event-actions {
  display: flex;
  justify-content: flex-end;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-filter {
    margin-top: 16px;
    width: 100%;
  }
  
  .events-grid {
    grid-template-columns: 1fr;
  }
}
</style> 