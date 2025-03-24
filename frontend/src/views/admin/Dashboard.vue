<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-800 mb-6">管理员仪表盘</h1>
    
    <!-- 数据统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-blue-500 rounded-md p-3">
              <el-icon class="h-6 w-6 text-white"><user /></el-icon>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">用户总数</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">{{ dashboardData.userCount || 0 }}</div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-4 sm:px-6">
          <router-link to="/admin/users" class="text-sm font-medium text-blue-600 hover:text-blue-500">查看详情</router-link>
        </div>
      </div>
      
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-green-500 rounded-md p-3">
              <el-icon class="h-6 w-6 text-white"><calendar /></el-icon>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">赛事总数</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">{{ dashboardData.eventCount || 0 }}</div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-4 sm:px-6">
          <router-link to="/admin/events" class="text-sm font-medium text-blue-600 hover:text-blue-500">查看详情</router-link>
        </div>
      </div>
      
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-indigo-500 rounded-md p-3">
              <el-icon class="h-6 w-6 text-white"><ticket /></el-icon>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">报名总数</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">{{ dashboardData.registrationCount || 0 }}</div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-4 sm:px-6">
          <router-link to="/admin/registrations" class="text-sm font-medium text-blue-600 hover:text-blue-500">查看详情</router-link>
        </div>
      </div>
      
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-yellow-500 rounded-md p-3">
              <el-icon class="h-6 w-6 text-white"><medal /></el-icon>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">已完成赛事</dt>
                <dd class="flex items-baseline">
                  <div class="text-2xl font-semibold text-gray-900">{{ dashboardData.completedEventCount || 0 }}</div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-4 sm:px-6">
          <router-link to="/admin/results" class="text-sm font-medium text-blue-600 hover:text-blue-500">查看详情</router-link>
        </div>
      </div>
    </div>
    
    <!-- 近期赛事和待审核报名 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
      <!-- 近期赛事 -->
      <div class="bg-white shadow rounded-lg p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-medium text-gray-900">近期赛事</h2>
          <router-link to="/admin/events" class="text-sm font-medium text-blue-600 hover:text-blue-500">查看全部</router-link>
        </div>
        
        <div v-if="loadingEvents" class="py-4 text-center">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="recentEvents.length === 0" class="py-4 text-center text-gray-500">
          暂无近期赛事
        </div>
        
        <div v-else class="space-y-4">
          <div v-for="event in recentEvents" :key="event.id" class="p-3 border border-gray-200 rounded-md hover:bg-gray-50">
            <div class="flex justify-between">
              <div>
                <div class="font-medium text-gray-900">{{ event.name }}</div>
                <div class="text-sm text-gray-500">{{ formatDate(event.startTime) }}</div>
              </div>
              <el-tag :type="getEventStatusType(event)">{{ getEventStatusText(event) }}</el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 待审核报名 -->
      <div class="bg-white shadow rounded-lg p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-medium text-gray-900">待审核报名</h2>
          <router-link to="/admin/registrations" class="text-sm font-medium text-blue-600 hover:text-blue-500">查看全部</router-link>
        </div>
        
        <div v-if="loadingRegistrations" class="py-4 text-center">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="pendingRegistrations.length === 0" class="py-4 text-center text-gray-500">
          暂无待审核报名
        </div>
        
        <div v-else class="space-y-4">
          <div v-for="registration in pendingRegistrations" :key="registration.id" class="p-3 border border-gray-200 rounded-md hover:bg-gray-50">
            <div class="flex justify-between">
              <div>
                <div class="font-medium text-gray-900">{{ registration.eventName }}</div>
                <div class="text-sm text-gray-500">
                  {{ registration.realName }} / {{ registration.type === 'ATHLETE' ? '运动员' : '观众' }}
                </div>
              </div>
              <div class="flex space-x-2">
                <el-button size="small" type="success" @click="approveRegistration(registration.id)">通过</el-button>
                <el-button size="small" type="danger" @click="rejectRegistration(registration.id)">拒绝</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 赛事趋势图表 -->
    <div class="bg-white shadow rounded-lg p-6">
      <h2 class="text-lg font-medium text-gray-900 mb-4">赛事统计趋势</h2>
      <div ref="chartContainer" class="h-80"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { User, Calendar, Ticket, Medal } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 仪表盘数据
const dashboardData = reactive({
  userCount: 0,
  eventCount: 0,
  registrationCount: 0,
  completedEventCount: 0
})

// 近期赛事
const recentEvents = ref([])
const loadingEvents = ref(true)

// 待审核报名
const pendingRegistrations = ref([])
const loadingRegistrations = ref(true)

// 图表容器
const chartContainer = ref(null)
let chart = null

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    const response = await axios.get('/api/admin/dashboard')
    Object.assign(dashboardData, response.data.data)
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

// 加载近期赛事
const loadRecentEvents = async () => {
  loadingEvents.value = true
  try {
    const response = await axios.get('/api/events/upcoming', { params: { days: 30, limit: 5 } })
    recentEvents.value = response.data.data
  } catch (error) {
    console.error('加载近期赛事失败:', error)
  } finally {
    loadingEvents.value = false
  }
}

// 加载待审核报名
const loadPendingRegistrations = async () => {
  loadingRegistrations.value = true
  try {
    const response = await axios.get('/api/registrations/status/PENDING', { params: { limit: 5 } })
    pendingRegistrations.value = response.data.data
  } catch (error) {
    console.error('加载待审核报名失败:', error)
  } finally {
    loadingRegistrations.value = false
  }
}

// 审核报名
const approveRegistration = async (id) => {
  try {
    await axios.put(`/api/registrations/${id}/review`, null, {
      params: { status: 'APPROVED' }
    })
    ElMessage.success('已通过该报名')
    loadPendingRegistrations()
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

const rejectRegistration = async (id) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝报名',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
      }
    )
    
    await axios.put(`/api/registrations/${id}/review`, null, {
      params: { status: 'REJECTED', remark: reason }
    })
    
    ElMessage.success('已拒绝该报名')
    loadPendingRegistrations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
    }
  }
}

// 初始化图表
const initChart = () => {
  // 模拟数据
  const months = ['1月', '2月', '3月', '4月', '5月', '6月']
  const eventData = [5, 8, 12, 7, 10, 15]
  const registrationData = [30, 45, 70, 42, 55, 80]
  
  // 使用echarts或其他图表库绘制图表
  // 这里仅为示例，实际应使用图表库如echarts等
  const chartData = {
    months,
    eventData,
    registrationData
  }
  
  console.log('图表数据准备完成', chartData)
  // 实际项目中这里使用echarts等图表库初始化并渲染图表
}

// 获取赛事状态样式和文本
const getEventStatusType = (event) => {
  const now = new Date()
  const startTime = new Date(event.startTime)
  const endTime = new Date(event.endTime)
  
  if (now < startTime) {
    return 'info'
  } else if (now >= startTime && now <= endTime) {
    return 'success'
  } else {
    return 'danger'
  }
}

const getEventStatusText = (event) => {
  const now = new Date()
  const startTime = new Date(event.startTime)
  const endTime = new Date(event.endTime)
  
  if (now < startTime) {
    return '即将开始'
  } else if (now >= startTime && now <= endTime) {
    return '进行中'
  } else {
    return '已结束'
  }
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

// 页面挂载完成后加载数据
onMounted(async () => {
  // 加载仪表盘数据
  loadDashboardData()
  
  // 加载近期赛事和待审核报名
  loadRecentEvents()
  loadPendingRegistrations()
  
  // 由于缺乏真实数据，这里使用模拟数据
  dashboardData.userCount = 125
  dashboardData.eventCount = 24
  dashboardData.registrationCount = 378
  dashboardData.completedEventCount = 15
  
  recentEvents.value = [
    {
      id: 1,
      name: '校园马拉松比赛',
      startTime: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000),
      endTime: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000 + 5 * 60 * 60 * 1000)
    },
    {
      id: 2,
      name: '篮球联赛',
      startTime: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000),
      endTime: new Date(Date.now() + 12 * 24 * 60 * 60 * 1000)
    },
    {
      id: 3,
      name: '游泳比赛',
      startTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
      endTime: new Date(Date.now() + 1 * 24 * 60 * 60 * 1000)
    }
  ]
  
  pendingRegistrations.value = [
    {
      id: 1,
      eventName: '校园马拉松比赛',
      realName: '张三',
      type: 'ATHLETE'
    },
    {
      id: 2,
      eventName: '篮球联赛',
      realName: '李四',
      type: 'ATHLETE'
    },
    {
      id: 3,
      eventName: '游泳比赛',
      realName: '王五',
      type: 'AUDIENCE'
    }
  ]
  
  loadingEvents.value = false
  loadingRegistrations.value = false
  
  // 初始化图表
  nextTick(() => {
    initChart()
  })
})
</script> 