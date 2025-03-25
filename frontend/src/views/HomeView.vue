<template>
  <div>
    <!-- 英雄区 -->
    <section class="bg-gradient-to-r from-primary-600 to-primary-800 text-white">
      <div class="container mx-auto px-4 py-16 md:py-24">
        <div class="flex flex-col md:flex-row items-center justify-between">
          <div class="md:w-1/2 mb-10 md:mb-0">
            <h1 class="text-4xl md:text-5xl font-bold mb-4">
              体育赛事管理系统
            </h1>
            <p class="text-xl md:text-2xl text-primary-100 mb-8">
              简单高效的体育赛事管理解决方案
            </p>
            <p class="mb-8 text-primary-50 max-w-lg">
              提供赛事策划、报名管理、赛程安排、成绩统计等全流程服务，让体育赛事组织变得简单高效。
            </p>
            <div class="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
              <router-link 
                to="/events" 
                class="px-6 py-3 rounded-lg bg-white text-primary-700 font-medium hover:bg-primary-50 transition-colors shadow-lg"
              >
                浏览赛事
              </router-link>
              <router-link 
                to="/register" 
                class="px-6 py-3 rounded-lg bg-primary-500 text-white font-medium hover:bg-primary-400 transition-colors border border-primary-400 shadow-lg"
              >
                立即注册
              </router-link>
            </div>
          </div>
          <div class="md:w-1/2 flex justify-center">
            <img src="@/assets/images/hero-image.svg" alt="体育赛事管理" class="w-full max-w-md">
          </div>
        </div>
      </div>
    </section>

    <!-- 特色功能 -->
    <section class="py-16 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">强大功能，助力赛事成功</h2>
          <p class="text-gray-600 max-w-2xl mx-auto">体育赛事管理系统提供全面的赛事管理功能，满足各类体育赛事的组织需求。</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <!-- 功能卡片 1 -->
          <div class="bg-gray-50 rounded-xl p-6 border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <div class="w-12 h-12 bg-primary-100 text-primary-600 rounded-lg flex items-center justify-center mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </div>
            <h3 class="text-xl font-bold mb-2 text-gray-900">赛事策划</h3>
            <p class="text-gray-600">
              轻松创建和设计各类体育赛事，自定义赛制、规则、流程，满足不同需求。
            </p>
          </div>

          <!-- 功能卡片 2 -->
          <div class="bg-gray-50 rounded-xl p-6 border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <div class="w-12 h-12 bg-primary-100 text-primary-600 rounded-lg flex items-center justify-center mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold mb-2 text-gray-900">报名管理</h3>
            <p class="text-gray-600">
              便捷的在线报名系统，支持个人和团队报名，自动审核，减少报名环节繁琐操作。
            </p>
          </div>

          <!-- 功能卡片 3 -->
          <div class="bg-gray-50 rounded-xl p-6 border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <div class="w-12 h-12 bg-primary-100 text-primary-600 rounded-lg flex items-center justify-center mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold mb-2 text-gray-900">成绩统计</h3>
            <p class="text-gray-600">
              实时记录和更新比赛成绩，自动生成排名和统计报表，提供多维度数据分析。
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新赛事 -->
    <section class="py-16 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center mb-10">
          <h2 class="text-3xl font-bold text-gray-900">最新赛事</h2>
          <router-link to="/events" class="text-primary-600 hover:text-primary-800 font-medium flex items-center">
            查看全部
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </router-link>
        </div>

        <div v-if="isLoading" class="flex justify-center py-12">
          <div class="w-12 h-12 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin"></div>
        </div>

        <div v-else-if="latestEvents.length === 0" class="text-center py-12">
          <p class="text-gray-500">暂无赛事数据</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <!-- 赛事卡片 -->
          <div 
            v-for="event in latestEvents" 
            :key="event.id" 
            class="bg-white rounded-xl overflow-hidden shadow hover:shadow-md transition-shadow border border-gray-100"
          >
            <div class="h-48 overflow-hidden">
              <img 
                :src="event.coverImage || 'https://via.placeholder.com/800x400?text=No+Image'" 
                :alt="event.title" 
                class="w-full h-full object-cover"
              >
            </div>
            <div class="p-6">
              <div class="flex items-center text-sm text-gray-500 mb-2">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary-100 text-primary-800 mr-2">
                  {{ event.category }}
                </span>
                <span>{{ formatDate(event.startTime) }}</span>
              </div>
              <h3 class="text-xl font-bold mb-2 text-gray-900">{{ event.title }}</h3>
              <p class="text-gray-600 mb-4 line-clamp-2">{{ event.description }}</p>
              <div class="flex justify-between items-center">
                <span class="text-gray-500 text-sm">{{ event.location }}</span>
                <router-link 
                  :to="`/events/${event.id}`" 
                  class="text-primary-600 hover:text-primary-800 font-medium"
                >
                  查看详情
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 赛事统计 -->
    <section class="py-16 bg-white">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">赛事规模不断扩大</h2>
          <p class="text-gray-600 max-w-2xl mx-auto">我们已成功举办众多大型体育赛事，获得广泛好评。</p>
        </div>

        <div v-if="isStatsLoading" class="flex justify-center py-12">
          <div class="w-12 h-12 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin"></div>
        </div>

        <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div class="text-center">
            <div class="text-4xl md:text-5xl font-bold text-primary-600 mb-2">{{ stats.events }}+</div>
            <div class="text-gray-500">成功赛事</div>
          </div>
          <div class="text-center">
            <div class="text-4xl md:text-5xl font-bold text-primary-600 mb-2">{{ stats.participants }}+</div>
            <div class="text-gray-500">参与人次</div>
          </div>
          <div class="text-center">
            <div class="text-4xl md:text-5xl font-bold text-primary-600 mb-2">{{ stats.schools }}+</div>
            <div class="text-gray-500">合作院校</div>
          </div>
          <div class="text-center">
            <div class="text-4xl md:text-5xl font-bold text-primary-600 mb-2">{{ stats.satisfaction }}%</div>
            <div class="text-gray-500">满意度</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 用户评价 -->
    <section class="py-16 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">用户的声音</h2>
          <p class="text-gray-600 max-w-2xl mx-auto">听听我们的用户如何评价体育赛事管理系统。</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div 
            v-for="(testimonial, index) in testimonials" 
            :key="index"
            class="bg-white rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow border border-gray-100"
          >
            <div class="flex items-center mb-4">
              <img 
                :src="testimonial.avatar" 
                :alt="testimonial.name"
                class="w-12 h-12 rounded-full object-cover mr-4"
              >
              <div>
                <h4 class="font-bold text-gray-900">{{ testimonial.name }}</h4>
                <p class="text-gray-500 text-sm">{{ testimonial.role }}</p>
              </div>
            </div>
            <p class="text-gray-600 italic">{{ testimonial.content }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 开始使用 -->
    <section class="py-16 bg-primary-600 text-white">
      <div class="container mx-auto px-4 text-center">
        <h2 class="text-3xl font-bold mb-4">准备好开始使用了吗？</h2>
        <p class="mb-8 max-w-2xl mx-auto text-primary-100">
          立即注册体育赛事管理系统，简化赛事组织流程，提高管理效率。
        </p>
        <div class="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4 justify-center">
          <router-link 
            to="/register" 
            class="px-6 py-3 rounded-lg bg-white text-primary-700 font-medium hover:bg-primary-50 transition-colors"
          >
            免费注册
          </router-link>
          <router-link 
            to="/contact" 
            class="px-6 py-3 rounded-lg bg-transparent text-white font-medium border border-white hover:bg-primary-700 transition-colors"
          >
            联系我们
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { eventApi } from '@/api'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

// 加载状态
const isLoading = ref(false)
const isStatsLoading = ref(false)

// 最新赛事数据
const latestEvents = ref([])

// 赛事统计数据
const stats = ref({
  events: 0,
  participants: 0,
  schools: 0,
  satisfaction: 0
})

// 用户评价
const testimonials = ref([
  {
    name: '李教练',
    role: '高校篮球教练',
    avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
    content: '这个系统大大简化了我组织校内篮球赛的工作量，从报名到成绩统计全部自动化，省时又省力。'
  },
  {
    name: '王主任',
    role: '学校体育部门负责人',
    avatar: 'https://randomuser.me/api/portraits/men/46.jpg',
    content: '我们学校每年要举办多项体育赛事，使用这个系统后，工作效率提升了至少50%，强烈推荐！'
  },
  {
    name: '张老师',
    role: '小学体育教师',
    avatar: 'https://randomuser.me/api/portraits/women/65.jpg',
    content: '界面简洁易用，即使是不懂技术的老师也能快速上手。学生们也很喜欢使用这个系统查看赛事信息。'
  }
])

// 格式化日期函数
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 获取最新赛事
const fetchLatestEvents = async () => {
  try {
    isLoading.value = true
    const data = await eventApi.getLatestEvents(3)
    latestEvents.value = data || []
  } catch (error) {
    console.error('获取最新赛事失败:', error)
    appStore.showError('获取最新赛事失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

// 获取赛事统计数据
const fetchEventStats = async () => {
  try {
    isStatsLoading.value = true
    const data = await eventApi.getEventStats()
    if (data) {
      stats.value = data
    }
  } catch (error) {
    console.error('获取赛事统计数据失败:', error)
    // 使用默认数据兜底
    stats.value = {
      events: 120,
      participants: 5000,
      schools: 50,
      satisfaction: 98
    }
  } finally {
    isStatsLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchLatestEvents()
  fetchEventStats()
})
</script> 