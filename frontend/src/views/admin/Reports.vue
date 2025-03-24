<template>
  <div>
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">报表统计</h1>
    </div>
    
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500">用户总数</p>
            <p class="text-2xl font-bold mt-1">{{ statistics.userCount }}</p>
          </div>
          <el-icon class="text-blue-500 text-3xl"><user /></el-icon>
        </div>
        <div class="mt-2 text-sm text-gray-500">
          <span class="text-green-500 mr-1">+{{ statistics.userGrowth }}%</span>较上月
        </div>
      </div>
      
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500">赛事总数</p>
            <p class="text-2xl font-bold mt-1">{{ statistics.eventCount }}</p>
          </div>
          <el-icon class="text-orange-500 text-3xl"><medal /></el-icon>
        </div>
        <div class="mt-2 text-sm text-gray-500">
          <span :class="statistics.eventGrowth >= 0 ? 'text-green-500' : 'text-red-500'">
            {{ statistics.eventGrowth >= 0 ? '+' : '' }}{{ statistics.eventGrowth }}%
          </span>
          较上月
        </div>
      </div>
      
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500">本月报名数</p>
            <p class="text-2xl font-bold mt-1">{{ statistics.registrationCount }}</p>
          </div>
          <el-icon class="text-green-500 text-3xl"><ticket /></el-icon>
        </div>
        <div class="mt-2 text-sm text-gray-500">
          <span :class="statistics.registrationGrowth >= 0 ? 'text-green-500' : 'text-red-500'">
            {{ statistics.registrationGrowth >= 0 ? '+' : '' }}{{ statistics.registrationGrowth }}%
          </span>
          较上月
        </div>
      </div>
      
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500">平均参与人数</p>
            <p class="text-2xl font-bold mt-1">{{ statistics.avgParticipants }}</p>
          </div>
          <el-icon class="text-purple-500 text-3xl"><data-line /></el-icon>
        </div>
        <div class="mt-2 text-sm text-gray-500">
          较上月
          <span :class="statistics.participantsGrowth >= 0 ? 'text-green-500' : 'text-red-500'">
            {{ statistics.participantsGrowth >= 0 ? '+' : '' }}{{ statistics.participantsGrowth }}%
          </span>
        </div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 用户增长趋势 -->
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-medium">用户增长趋势</h2>
          <el-radio-group v-model="userChartPeriod" size="small">
            <el-radio-button label="week">周</el-radio-button>
            <el-radio-button label="month">月</el-radio-button>
            <el-radio-button label="year">年</el-radio-button>
          </el-radio-group>
        </div>
        <div id="userGrowthChart" class="w-full h-72"></div>
      </div>
      
      <!-- 赛事类型分布 -->
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-medium">赛事类型分布</h2>
          <el-select v-model="eventChartYear" size="small" @change="loadEventCategoryData">
            <el-option
              v-for="year in availableYears"
              :key="year"
              :label="year + '年'"
              :value="year"
            />
          </el-select>
        </div>
        <div id="eventCategoryChart" class="w-full h-72"></div>
      </div>
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 报名状态统计 -->
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-medium">报名状态统计</h2>
          <el-select v-model="registrationEventId" size="small" placeholder="选择赛事" @change="loadRegistrationData">
            <el-option
              v-for="event in events"
              :key="event.id"
              :label="event.name"
              :value="event.id"
            />
          </el-select>
        </div>
        <div v-if="registrationEventId" id="registrationStatusChart" class="w-full h-72"></div>
        <el-empty v-else description="请选择赛事查看报名状态统计" />
      </div>
      
      <!-- 报名时间分布 -->
      <div class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-medium">报名时间分布</h2>
          <el-select v-model="timeDistributionEventId" size="small" placeholder="选择赛事" @change="loadTimeDistributionData">
            <el-option
              v-for="event in events"
              :key="event.id"
              :label="event.name"
              :value="event.id"
            />
          </el-select>
        </div>
        <div v-if="timeDistributionEventId" id="timeDistributionChart" class="w-full h-72"></div>
        <el-empty v-else description="请选择赛事查看报名时间分布" />
      </div>
    </div>
    
    <!-- 热门赛事排行 -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-medium">热门赛事排行</h2>
        <el-radio-group v-model="rankingPeriod" size="small" @change="loadPopularEvents">
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="quarter">本季度</el-radio-button>
          <el-radio-button label="year">本年度</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="popularEvents" style="width: 100%">
        <el-table-column type="index" width="50" />
        <el-table-column prop="name" label="赛事名称" min-width="200" />
        <el-table-column prop="location" label="举办地点" min-width="150" />
        <el-table-column prop="eventDate" label="举办日期" min-width="120">
          <template #default="scope">
            {{ formatDate(scope.row.eventDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="registeredCount" label="报名人数" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Medal, Ticket, DataLine } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts/core'
import { TooltipComponent, LegendComponent, GridComponent, DataZoomComponent } from 'echarts/components'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

// 注册ECharts组件
echarts.use([
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  LineChart,
  BarChart,
  PieChart,
  UniversalTransition,
  CanvasRenderer
])

// 统计数据
const statistics = ref({
  userCount: 0,
  userGrowth: 0,
  eventCount: 0,
  eventGrowth: 0,
  registrationCount: 0,
  registrationGrowth: 0,
  avgParticipants: 0,
  participantsGrowth: 0
})

// 图表相关状态
const userChartPeriod = ref('month')
const eventChartYear = ref(new Date().getFullYear())
const availableYears = ref([])
const registrationEventId = ref(null)
const timeDistributionEventId = ref(null)
const rankingPeriod = ref('month')

// 赛事列表
const events = ref([])
const popularEvents = ref([])

// 图表实例
let userGrowthChart = null
let eventCategoryChart = null
let registrationStatusChart = null
let timeDistributionChart = null

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await axios.get('/api/statistics/overview')
    statistics.value = response.data.data
  } catch (error) {
    ElMessage.error('获取统计数据失败')
    
    // 模拟数据
    statistics.value = {
      userCount: 756,
      userGrowth: 12.5,
      eventCount: 24,
      eventGrowth: 8.3,
      registrationCount: 385,
      registrationGrowth: -5.2,
      avgParticipants: 48,
      participantsGrowth: 15.7
    }
  }
}

// 获取赛事列表
const fetchEvents = async () => {
  try {
    const response = await axios.get('/api/events')
    events.value = response.data.data.list || []
  } catch (error) {
    ElMessage.error('获取赛事列表失败')
    
    // 模拟数据
    events.value = [
      {
        id: 1,
        name: '2023年春季校园运动会',
        location: '中央体育场',
        eventDate: '2023-04-15',
        registeredCount: 320,
        status: 'COMPLETED'
      },
      {
        id: 2,
        name: '2023年秋季马拉松挑战赛',
        location: '城市环路',
        eventDate: '2023-10-20',
        registeredCount: 578,
        status: 'UPCOMING'
      },
      {
        id: 3,
        name: '2023年暑期篮球联赛',
        location: '市体育馆',
        eventDate: '2023-07-10',
        registeredCount: 185,
        status: 'COMPLETED'
      }
    ]
  }
  
  // 获取所有年份
  const years = new Set()
  events.value.forEach(event => {
    if (event.eventDate) {
      const year = new Date(event.eventDate).getFullYear()
      years.add(year)
    }
  })
  availableYears.value = Array.from(years).sort((a, b) => b - a) // 降序排列
  
  if (availableYears.value.length > 0) {
    eventChartYear.value = availableYears.value[0]
  }
}

// 获取热门赛事
const loadPopularEvents = async () => {
  try {
    const response = await axios.get('/api/statistics/popular-events', {
      params: { period: rankingPeriod.value }
    })
    popularEvents.value = response.data.data || []
  } catch (error) {
    ElMessage.error('获取热门赛事失败')
    
    // 模拟数据
    popularEvents.value = events.value
      .slice()
      .sort((a, b) => b.registeredCount - a.registeredCount)
      .slice(0, 5)
  }
}

// 初始化用户增长趋势图表
const initUserGrowthChart = () => {
  if (userGrowthChart) {
    userGrowthChart.dispose()
  }
  
  userGrowthChart = echarts.init(document.getElementById('userGrowthChart'))
  loadUserGrowthData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    userGrowthChart && userGrowthChart.resize()
  })
}

// 加载用户增长数据
const loadUserGrowthData = async () => {
  try {
    const response = await axios.get('/api/statistics/user-growth', {
      params: { period: userChartPeriod.value }
    })
    renderUserGrowthChart(response.data.data)
  } catch (error) {
    ElMessage.error('获取用户增长数据失败')
    
    // 模拟数据
    let dates = []
    let userData = []
    let athleteData = []
    
    if (userChartPeriod.value === 'week') {
      dates = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      userData = [5, 8, 3, 10, 15, 20, 12]
      athleteData = [2, 3, 1, 5, 8, 12, 6]
    } else if (userChartPeriod.value === 'month') {
      dates = Array.from({ length: 30 }, (_, i) => `${i + 1}日`)
      userData = Array.from({ length: 30 }, () => Math.floor(Math.random() * 20 + 5))
      athleteData = Array.from({ length: 30 }, () => Math.floor(Math.random() * 10 + 2))
    } else {
      dates = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
      userData = [30, 35, 42, 55, 68, 75, 80, 95, 110, 125, 138, 150]
      athleteData = [10, 15, 18, 25, 30, 35, 38, 45, 50, 55, 60, 65]
    }
    
    renderUserGrowthChart({
      dates,
      series: [
        { name: '普通用户', data: userData },
        { name: '运动员', data: athleteData }
      ]
    })
  }
}

// 渲染用户增长图表
const renderUserGrowthChart = (data) => {
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: data.series.map(item => item.name)
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates
    },
    yAxis: {
      type: 'value',
      name: '新增用户数'
    },
    series: data.series.map(item => ({
      name: item.name,
      type: 'line',
      data: item.data,
      smooth: true,
      showSymbol: false,
      lineStyle: {
        width: 3
      },
      areaStyle: {
        opacity: 0.2
      },
      emphasis: {
        focus: 'series'
      }
    }))
  }
  
  userGrowthChart.setOption(option)
}

// 初始化赛事类型分布图表
const initEventCategoryChart = () => {
  if (eventCategoryChart) {
    eventCategoryChart.dispose()
  }
  
  eventCategoryChart = echarts.init(document.getElementById('eventCategoryChart'))
  loadEventCategoryData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    eventCategoryChart && eventCategoryChart.resize()
  })
}

// 加载赛事类型数据
const loadEventCategoryData = async () => {
  try {
    const response = await axios.get('/api/statistics/event-categories', {
      params: { year: eventChartYear.value }
    })
    renderEventCategoryChart(response.data.data)
  } catch (error) {
    ElMessage.error('获取赛事类型数据失败')
    
    // 模拟数据
    renderEventCategoryChart([
      { name: '田径', value: 12 },
      { name: '球类', value: 8 },
      { name: '马拉松', value: 3 },
      { name: '游泳', value: 5 },
      { name: '其他', value: 2 }
    ])
  }
}

// 渲染赛事类型分布图表
const renderEventCategoryChart = (data) => {
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '赛事类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  eventCategoryChart.setOption(option)
}

// 初始化报名状态图表
const initRegistrationStatusChart = () => {
  registrationStatusChart = null // 延迟初始化，等待选择赛事后再创建
}

// 加载报名状态数据
const loadRegistrationData = async () => {
  if (!registrationEventId.value) return
  
  try {
    const response = await axios.get('/api/statistics/registration-status', {
      params: { eventId: registrationEventId.value }
    })
    renderRegistrationStatusChart(response.data.data)
  } catch (error) {
    ElMessage.error('获取报名状态数据失败')
    
    // 模拟数据
    renderRegistrationStatusChart([
      { name: '已通过', value: 280 },
      { name: '待审核', value: 45 },
      { name: '已拒绝', value: 30 },
      { name: '已取消', value: 25 }
    ])
  }
}

// 渲染报名状态图表
const renderRegistrationStatusChart = (data) => {
  if (!registrationStatusChart) {
    registrationStatusChart = echarts.init(document.getElementById('registrationStatusChart'))
    
    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      registrationStatusChart && registrationStatusChart.resize()
    })
  }
  
  // 设置颜色映射
  const colorMap = {
    '已通过': '#67C23A',
    '待审核': '#409EFF',
    '已拒绝': '#F56C6C',
    '已取消': '#909399'
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 10,
      left: 'center',
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '报名状态',
        type: 'pie',
        radius: '70%',
        center: ['50%', '45%'],
        data: data.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: colorMap[item.name] || null
          }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  registrationStatusChart.setOption(option)
}

// 初始化报名时间分布图表
const initTimeDistributionChart = () => {
  timeDistributionChart = null // 延迟初始化，等待选择赛事后再创建
}

// 加载报名时间分布数据
const loadTimeDistributionData = async () => {
  if (!timeDistributionEventId.value) return
  
  try {
    const response = await axios.get('/api/statistics/registration-time', {
      params: { eventId: timeDistributionEventId.value }
    })
    renderTimeDistributionChart(response.data.data)
  } catch (error) {
    ElMessage.error('获取报名时间分布数据失败')
    
    // 模拟数据
    const dates = []
    const counts = []
    
    // 生成最近30天的日期
    const today = new Date()
    for (let i = 29; i >= 0; i--) {
      const date = new Date(today)
      date.setDate(today.getDate() - i)
      dates.push(date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' }))
      
      // 模拟报名趋势：开始少，中间多，结束前再增加
      let count
      if (i > 20) {
        count = Math.floor(Math.random() * 5 + 1)
      } else if (i > 10) {
        count = Math.floor(Math.random() * 15 + 5)
      } else if (i > 5) {
        count = Math.floor(Math.random() * 10 + 3)
      } else {
        count = Math.floor(Math.random() * 20 + 10)
      }
      counts.push(count)
    }
    
    renderTimeDistributionChart({
      dates,
      counts
    })
  }
}

// 渲染报名时间分布图表
const renderTimeDistributionChart = (data) => {
  if (!timeDistributionChart) {
    timeDistributionChart = echarts.init(document.getElementById('timeDistributionChart'))
    
    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      timeDistributionChart && timeDistributionChart.resize()
    })
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisLabel: {
        interval: Math.floor(data.dates.length / 10)
      }
    },
    yAxis: {
      type: 'value',
      name: '报名人数'
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        start: 0,
        end: 100
      }
    ],
    series: [
      {
        name: '报名人数',
        type: 'bar',
        data: data.counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  
  timeDistributionChart.setOption(option)
}

// 获取状态类型和文本
const getStatusType = (status) => {
  switch (status) {
    case 'UPCOMING': return 'info'
    case 'ONGOING': return 'success'
    case 'COMPLETED': return ''
    case 'CANCELED': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'UPCOMING': return '未开始'
    case 'ONGOING': return '进行中'
    case 'COMPLETED': return '已结束'
    case 'CANCELED': return '已取消'
    default: return '未知'
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

// 监听切换图表周期
watch(userChartPeriod, () => {
  loadUserGrowthData()
})

watch(eventChartYear, () => {
  loadEventCategoryData()
})

watch(registrationEventId, () => {
  loadRegistrationData()
})

watch(timeDistributionEventId, () => {
  loadTimeDistributionData()
})

watch(rankingPeriod, () => {
  loadPopularEvents()
})

// 生命周期钩子
onMounted(async () => {
  // 获取基础数据
  await fetchStatistics()
  await fetchEvents()
  loadPopularEvents()
  
  // 初始化图表
  initUserGrowthChart()
  initEventCategoryChart()
  initRegistrationStatusChart()
  initTimeDistributionChart()
})

onBeforeUnmount(() => {
  // 销毁图表实例
  userGrowthChart && userGrowthChart.dispose()
  eventCategoryChart && eventCategoryChart.dispose()
  registrationStatusChart && registrationStatusChart.dispose()
  timeDistributionChart && timeDistributionChart.dispose()
  
  // 移除窗口大小变化监听
  window.removeEventListener('resize', () => {})
})
</script>