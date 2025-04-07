<template>
  <div>
    <!-- 页面标题和搜索筛选 -->
    <div class="mb-8 flex flex-col md:flex-row md:justify-between md:items-center gap-4">
      <h1 class="text-2xl font-bold text-gray-800">赛事列表</h1>
      
      <div class="flex flex-wrap gap-3">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索赛事名称"
          class="min-w-[240px]"
          clearable
          @clear="resetSearch"
          @keyup.enter="searchEvents"
        >
          <template #append>
            <el-btn 
               @click="searchEvents"
            >
              <el-icon><Search /></el-icon>
            </el-btn>
          </template>
        </el-input>
        
        <el-select 
          v-model="selectedCategory" 
          placeholder="赛事分类" 
          clearable 
          @change="handleCategoryChange"
          class="min-w-[140px]"
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
          class="min-w-[140px]"
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
    
    <!-- 加载状态 -->
    <div v-if="loading" class="py-10 text-center">
      <el-skeleton :rows="5" animated />
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="events.length === 0" class="py-16 flex justify-center">
      <el-empty description="未找到符合条件的赛事" />
    </div>
    
    <!-- 赛事卡片网格 -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
      <div 
        v-for="event in events" 
        :key="event.id" 
        class="bg-white rounded-xl shadow-sm overflow-hidden transition-all duration-300 hover:shadow-md hover:-translate-y-1 transform"
      >
        <div class="p-5 flex flex-col h-full relative">
          <!-- 状态标签 -->
          <div class="absolute top-5 right-5">
            <el-tag 
              :type="getStatusType(event.status)" 
              class="!rounded-full"
            >
              {{ getStatusText(event.status) }}
            </el-tag>
          </div>
          
          <!-- 赛事分类 -->
          <div class="mb-4">
            <el-tag 
              size="small" 
              type="info" 
              class="!bg-indigo-100 !text-indigo-800 !border-none !rounded-full"
            >
              {{ event.category.name }}
            </el-tag>
          </div>
          
          <!-- 赛事名称和描述 -->
          <h3 class="text-xl font-medium text-gray-800 mb-3 mt-2">{{ event.name }}</h3>
          <p class="text-gray-600 mb-5 flex-grow">{{ truncateText(event.description, 100) }}</p>
          
          <!-- 赛事详情 -->
          <div class="space-y-2 mb-6 border-t border-gray-100 pt-4">
            <div class="flex items-center text-gray-600">
              <el-icon class="mr-2 text-indigo-500"><Location /></el-icon>
              <span>{{ event.location }}</span>
            </div>
            <div class="flex items-center text-gray-600">
              <el-icon class="mr-2 text-indigo-500"><Timer /></el-icon>
              <span>{{ formatDate(event.startTime) }}</span>
            </div>
            <div class="flex items-center text-gray-600">
              <el-icon class="mr-2 text-indigo-500"><Calendar /></el-icon>
              <span>报名截止: {{ formatDate(event.registrationDeadline) }}</span>
            </div>
            <div class="flex items-center text-gray-600">
              <el-icon class="mr-2 text-indigo-500"><User /></el-icon>
              <span>人数上限: {{ event.maxParticipants }}人</span>
            </div>
          </div>
          
          <!-- 赛事操作 -->
          <div class="flex justify-end">
            <router-link :to="`/user/events/${event.id}`">
              <button 
                class="px-5 py-1.5 bg-indigo-600 text-white text-sm font-medium rounded-full shadow-sm hover:bg-indigo-700 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-50"
              >
                查看详情
              </button>
            </router-link>
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
        :total="totalElements"
        background
        class="pagination-with-bg"
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
import type { EventCategory } from '../../api/types';
import type { Event } from '../../api/eventsAPI';

// 状态数据
const events = ref<Event[]>([]);
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
    const response = await categoryAPI.getPublicCategories();
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
:deep(.pagination-with-bg .el-pager li.is-active) {
  background-color: #4f46e5;
  border-color: #4f46e5;
  color: white;
}

:deep(.pagination-with-bg .el-pager li:hover:not(.is-active)) {
  color: #4f46e5;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05) !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1) !important;
}

:deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.2) !important;
}
</style> 