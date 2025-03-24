<template>
  <div>
    <Header />
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-800 mb-4">赛事活动</h1>
        <p class="text-gray-600">探索各类体育赛事活动，立即报名参与</p>
      </div>
      
      <!-- 搜索和筛选 -->
      <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
        <div class="flex flex-wrap gap-4 mb-4">
          <el-input
            v-model="searchQuery"
            placeholder="搜索赛事名称/地点"
            class="max-w-xs"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><search /></el-icon>
            </template>
          </el-input>
          
          <el-select v-model="categoryFilter" placeholder="赛事类别" clearable @change="handleFilterChange">
            <el-option label="全部类别" value="" />
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
          
          <el-select v-model="statusFilter" placeholder="赛事状态" clearable @change="handleFilterChange">
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
        <div v-for="event in events" :key="event.id" class="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow duration-300">
          <div class="relative h-48 bg-gray-200">
            <img 
              v-if="event.coverImage" 
              :src="event.coverImage" 
              :alt="event.name" 
              class="w-full h-full object-cover"
            >
            <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-r from-blue-500 to-purple-500">
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
              <h2 class="text-xl font-bold text-gray-800 hover:text-blue-600 transition-colors duration-300 cursor-pointer" @click="goToEventDetail(event.id)">
                {{ event.name }}
              </h2>
              <el-button v-if="event.featured" type="warning" size="small" plain>推荐</el-button>
            </div>
            
            <div class="mb-3 text-sm text-gray-500">
              <div class="flex items-center mb-1">
                <el-icon><location /></el-icon>
                <span class="ml-1">{{ event.location }}</span>
              </div>
              <div class="flex items-center mb-1">
                <el-icon><calendar /></el-icon>
                <span class="ml-1">{{ formatDate(event.eventDate) }}</span>
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
                :format="() => `${event.registeredCount || 0}/${event.maxParticipants || '不限'}`"
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
              <span class="text-sm text-gray-500">已有 {{ event.registeredCount || 0 }} 人报名</span>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Location, Calendar, Timer } from '@element-plus/icons-vue'
import axios from 'axios'
import Header from '@/components/Header.vue'

const router = useRouter()

// 赛事列表
const events = ref([])
const loading = ref(true)
const error = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(9)
const totalEvents = ref(0)

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
  loading.value = true
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
    
    const response = await axios.get('/api/events/public', { params })
    
    events.value = response.data.data.list || []
    totalEvents.value = response.data.data.total || 0
  } catch (err) {
    error.value = err.response?.data?.message || '获取赛事列表失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
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
  if (!event.maxParticipants || event.maxParticipants === 0) {
    return 0
  }
  
  const percentage = (event.registeredCount / event.maxParticipants) * 100
  return Math.min(100, percentage)
}

// 获取报名进度条颜色
const getRegistrationProgressColor = (event) => {
  if (!event.maxParticipants || event.maxParticipants === 0) {
    return '#409eff'
  }
  
  const percentage = (event.registeredCount / event.maxParticipants) * 100
  
  if (percentage >= 80) {
    return '#F56C6C'
  } else if (percentage >= 50) {
    return '#E6A23C'
  } else {
    return '#67C23A'
  }
}

// 判断报名是否即将截止
const isRegistrationDeadlineSoon = (deadline) => {
  if (!deadline) return false
  
  const today = new Date()
  const deadlineDate = new Date(deadline)
  const diffDays = Math.ceil((deadlineDate - today) / (1000 * 60 * 60 * 24))
  
  return diffDays > 0 && diffDays <= 3
}

// 判断报名是否已截止
const isRegistrationClosed = (event) => {
  if (event.status === 'CANCELED') return true
  if (!event.registrationDeadline) return false
  
  const today = new Date()
  const deadlineDate = new Date(event.registrationDeadline)
  
  return today > deadlineDate
}

// 获取操作按钮文本
const getActionButtonText = (event) => {
  if (event.status === 'COMPLETED') {
    return '查看详情'
  } else if (isRegistrationClosed(event)) {
    return '报名已截止'
  } else if (event.status === 'CANCELED') {
    return '已取消'
  } else {
    return '立即报名'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 跳转到赛事详情
const goToEventDetail = (eventId) => {
  router.push(`/events/${eventId}`)
}

// 跳转到报名页面
const goToRegistration = (eventId) => {
  router.push(`/registration/${eventId}`)
}

// 生命周期钩子
onMounted(() => {
  fetchEvents()
  
  // 模拟数据
  events.value = [
    {
      id: 1,
      name: '2023年春季校园运动会',
      location: '中央体育场',
      description: '校园传统体育盛事，包含田径、球类等多种项目',
      eventDate: '2023-04-15',
      registrationDeadline: '2023-04-01',
      maxParticipants: 500,
      registeredCount: 320,
      featured: true,
      categories: ['田径', '球类'],
      status: 'COMPLETED'
    },
    {
      id: 2,
      name: '2023年秋季马拉松挑战赛',
      location: '城市环路',
      description: '城市环城马拉松比赛，全程42.195公里',
      eventDate: '2023-10-20',
      registrationDeadline: new Date(new Date().getTime() + 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      maxParticipants: 1000,
      registeredCount: 578,
      featured: true,
      categories: ['马拉松'],
      status: 'UPCOMING'
    },
    {
      id: 3,
      name: '2023年暑期篮球联赛',
      location: '市体育馆',
      description: '暑期篮球爱好者联赛，分团体和个人组',
      eventDate: '2023-07-10',
      registrationDeadline: '2023-06-30',
      maxParticipants: 200,
      registeredCount: 185,
      featured: false,
      categories: ['球类'],
      status: 'COMPLETED'
    },
    {
      id: 4,
      name: '2023年游泳挑战赛',
      location: '奥林匹克游泳馆',
      description: '各年龄段游泳爱好者的竞技平台',
      eventDate: '2023-08-05',
      registrationDeadline: '2023-07-25',
      maxParticipants: 300,
      registeredCount: 210,
      featured: false,
      categories: ['游泳'],
      status: 'COMPLETED'
    },
    {
      id: 5,
      name: '2023年冬季滑雪节',
      location: '雪山度假村',
      description: '冬季户外运动盛事，包含多种雪上项目',
      eventDate: '2023-12-15',
      registrationDeadline: '2023-12-01',
      maxParticipants: 400,
      registeredCount: 150,
      featured: true,
      categories: ['冬季运动'],
      status: 'UPCOMING'
    },
    {
      id: 6,
      name: '2023年城市定向越野赛',
      location: '城市中心',
      description: '融合智力与体力的城市探索活动',
      eventDate: '2023-09-10',
      registrationDeadline: '2023-08-30',
      maxParticipants: 250,
      registeredCount: 230,
      featured: false,
      categories: ['田径', '极限运动'],
      status: 'COMPLETED'
    }
  ]
  
  totalEvents.value = events.value.length
  loading.value = false
})
</script> 