<template>
  <div class="container mx-auto px-4 py-8">
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-800 mb-4">赛事活动</h1>
      <p class="text-gray-600">探索各类体育赛事活动，立即报名参与</p>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
      <div class="flex flex-wrap gap-4 mb-4 filter-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索赛事名称/地点"
          class="max-w-xs md:max-w-none md:w-auto flex-grow"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="categoryFilter" placeholder="赛事类别" clearable @change="handleFilterChange" class="flex-grow md:flex-grow-0">
          <el-option label="全部类别" value="" />
          <el-option
            v-for="category in categories"
            :key="category"
            :label="category"
            :value="category"
          />
        </el-select>
        
        <el-select v-model="statusFilter" placeholder="赛事状态" clearable @change="handleFilterChange" class="flex-grow md:flex-grow-0">
          <el-option label="全部状态" value="" />
          <el-option label="报名中" value="UPCOMING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="COMPLETED" />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleFilterChange"
          class="flex-grow"
        />
      </div>
    </div>
    
    <!-- 赛事列表 -->
    <div v-if="loading" class="flex justify-center items-center h-64">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="events.length === 0" class="flex flex-col items-center justify-center h-64">
      <el-empty description="暂无符合条件的赛事" />
    </div>
    
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="event in events" :key="event.id" class="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow duration-300 event-card">
        <div class="relative h-48 bg-gray-200">
          <img 
            v-if="event.coverImage" 
            :src="event.coverImage" 
            :alt="event.name" 
            class="w-full h-full object-cover"
          >
          <div v-else class="w-full h-full flex items-center justify-center image-placeholder">
            <span class="text-xl font-bold text-white">{{ event.name }}</span>
          </div>
          
          <div class="absolute top-3 right-3">
            <el-tag 
              :type="getStatusType(event.status)" 
              effect="dark"
              size="large"
              class="px-3 py-1"
            >
              {{ getStatusText(event.status) }}
            </el-tag>
          </div>
          
          <div v-if="isRegistrationDeadlineSoon(event.registrationDeadline)" class="absolute top-3 left-3">
            <el-tag type="danger" effect="dark" size="large" class="px-3 py-1">
              报名即将截止
            </el-tag>
          </div>
        </div>
        
        <div class="p-4">
          <div class="flex justify-between items-start mb-2">
            <h2 class="text-xl font-bold text-gray-800 hover:text-blue-600 transition-colors duration-300 cursor-pointer event-title" @click="goToEventDetail(event.id)">
              {{ event.name }}
            </h2>
            <el-button v-if="event.featured" type="warning" size="small" plain>推荐</el-button>
          </div>
          
          <div class="mb-3 text-sm text-gray-500">
            <div class="flex items-center mb-1">
              <el-icon><location /></el-icon>
              <span class="ml-1 event-location">{{ event.location }}</span>
            </div>
            <div class="flex items-center mb-1">
              <el-icon><calendar /></el-icon>
              <span class="ml-1">{{ formatDate(event.startTime || event.eventDate) }}</span>
            </div>
            <div class="flex items-center">
              <el-icon><timer /></el-icon>
              <span class="ml-1">报名截止: {{ formatDate(event.registrationDeadline) }}</span>
            </div>
          </div>
          
          <div class="mb-3">
            <el-progress 
              :percentage="calculateRegistrationPercentage(event)" 
              :color="getRegistrationProgressColor(event)"
              :format="() => `${event.currentParticipants || event.registeredCount || 0}/${event.maxParticipants || '不限'}`"
              :stroke-width="10"
              :show-text="true"
            />
          </div>
          
          <div class="flex flex-wrap gap-2 mb-3">
            <el-tag v-for="tag in (event.categories || [])" :key="tag" size="small" effect="plain">
              {{ tag }}
            </el-tag>
          </div>
          
          <div class="flex justify-between items-center">
            <span class="text-sm text-gray-500">已有 {{ event.currentParticipants || event.registeredCount || 0 }} 人报名</span>
            <el-button 
              type="primary" 
              :disabled="event.status === 'COMPLETED' || isRegistrationClosed(event)" 
              @click="goToRegistration(event.id)"
            >
              {{ getActionButtonText(event) }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[9, 18, 36, 72]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalEvents"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Location, Calendar, Timer } from '@element-plus/icons-vue'
import { useEventStore } from '@/stores/event'

const router = useRouter()
const eventStore = useEventStore()

// 赛事列表
const events = computed(() => eventStore.events)
const loading = computed(() => eventStore.loading)
const error = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(9)
const totalEvents = computed(() => eventStore.pagination.total)

// 搜索和筛选
const searchQuery = ref('')
const categoryFilter = ref('')
const statusFilter = ref('')
const dateRange = ref(null)

// 可用类别列表
const categories = ref([
  '田径',
  '球类',
  '马拉松',
  '游泳',
  '健身',
  '骑行',
  '冬季运动',
  '水上运动',
  '极限运动',
  '其他'
])

// 获取赛事列表
const fetchEvents = async () => {
  error.value = null
  
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value || undefined,
      category: categoryFilter.value || undefined,
      status: statusFilter.value || undefined
    }
    
    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    await eventStore.fetchPublicEvents(params)
  } catch (err) {
    error.value = err.response?.data?.message || '获取赛事列表失败'
    ElMessage.error(error.value)
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchEvents()
}

// 处理筛选
const handleFilterChange = () => {
  currentPage.value = 1
  fetchEvents()
}

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchEvents()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchEvents()
}

// 获取状态类型和文本
const getStatusType = (status) => {
  switch (status) {
    case 'UPCOMING': return 'success'
    case 'ONGOING': return 'warning'
    case 'COMPLETED': return 'info'
    case 'CANCELED': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'UPCOMING': return '报名中'
    case 'ONGOING': return '进行中'
    case 'COMPLETED': return '已结束'
    case 'CANCELED': return '已取消'
    default: return '未知'
  }
}

// 计算报名进度百分比
const calculateRegistrationPercentage = (event) => {
  if (!event.maxParticipants || event.maxParticipants <= 0) {
    return 0
  }
  const percentage = (event.currentParticipants || event.registeredCount || 0) / event.maxParticipants * 100
  return Math.min(Math.round(percentage), 100)
}

// 获取报名进度颜色
const getRegistrationProgressColor = (event) => {
  const percentage = calculateRegistrationPercentage(event)
  if (percentage >= 90) {
    return '#F56C6C' // 接近满
  } else if (percentage >= 70) {
    return '#E6A23C' // 较多
  } else {
    return '#67C23A' // 充足
  }
}

// 判断报名是否已关闭
const isRegistrationClosed = (event) => {
  if (!event.registrationDeadline) {
    return false
  }
  const deadline = new Date(event.registrationDeadline)
  return deadline < new Date()
}

// 判断报名是否即将截止
const isRegistrationDeadlineSoon = (deadline) => {
  if (!deadline) {
    return false
  }
  const deadlineDate = new Date(deadline)
  const now = new Date()
  const diffDays = Math.ceil((deadlineDate - now) / (1000 * 60 * 60 * 24))
  return diffDays >= 0 && diffDays <= 2 // 2天内截止
}

// 获取按钮文本
const getActionButtonText = (event) => {
  if (event.status === 'COMPLETED') {
    return '查看结果'
  } else if (isRegistrationClosed(event)) {
    return '报名已截止'
  } else {
    return '立即报名'
  }
}

// 导航到赛事详情
const goToEventDetail = (id) => {
  router.push(`/events/${id}`)
}

// 导航到报名页面
const goToRegistration = (id) => {
  // 修改为显示提示消息，未来可以实现报名功能
  ElMessage({
    message: '报名功能正在开发中',
    type: 'info'
  })
  // router.push(`/registration/${id}`)
}

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return '未设置'
  
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 页面加载时获取数据
onMounted(fetchEvents)
</script>

<style scoped>
/* 卡片悬停效果 */
.event-card {
  transition: all 0.3s ease;
}

.event-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* 标签样式增强 */
.el-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

/* 进度条自定义样式 */
:deep(.el-progress-bar__outer) {
  border-radius: 4px;
  background-color: #f0f0f0;
}

:deep(.el-progress-bar__inner) {
  border-radius: 4px;
}

/* 响应式布局优化 */
@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
  }
  
  .filter-container > * {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .el-date-picker {
    width: 100%;
  }
}

/* 图片占位符 */
.image-placeholder {
  background-image: linear-gradient(135deg, var(--primary-600) 0%, var(--primary-400) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
  padding: 0 1rem;
}

/* 截断长文本 */
.event-title {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-location {
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style> 