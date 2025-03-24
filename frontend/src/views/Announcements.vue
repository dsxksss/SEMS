<template>
  <div>
    <Header />
    <div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold mb-6">公告信息</h1>
      
      <!-- 搜索区域 -->
      <div class="bg-white rounded-lg shadow-md p-4 mb-6">
        <el-input
          v-model="searchQuery"
          placeholder="搜索公告标题"
          class="max-w-lg"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="announcementStore.isLoading" class="text-center py-10">
        <el-skeleton :rows="5" animated />
      </div>
      
      <!-- 错误提示 -->
      <div v-else-if="announcementStore.error" class="text-center py-10 text-red-500">
        {{ announcementStore.error }}
      </div>
      
      <!-- 无数据提示 -->
      <div v-else-if="filteredAnnouncements.length === 0" class="text-center py-10 text-gray-500">
        暂无公告信息
      </div>
      
      <!-- 公告列表 -->
      <div v-else class="space-y-4">
        <div 
          v-for="announcement in filteredAnnouncements" 
          :key="announcement.id" 
          class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
          @click="viewAnnouncement(announcement)"
        >
          <div class="p-6">
            <div class="flex justify-between items-start mb-3">
              <h2 class="text-xl font-bold text-gray-800">{{ announcement.title }}</h2>
              <span class="text-sm text-gray-500">{{ formatDate(announcement.publishTime) }}</span>
            </div>
            <p class="text-gray-600 mb-4 line-clamp-3">{{ announcement.content }}</p>
            <div class="flex justify-end">
              <button class="text-blue-600 hover:text-blue-800 font-medium">
                阅读全文
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="mt-6 flex justify-center">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalAnnouncements"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentAnnouncement?.title"
      width="600px"
    >
      <div class="text-gray-500 mb-4">
        发布时间: {{ formatDate(currentAnnouncement?.publishTime) }}
      </div>
      <div class="text-gray-700 whitespace-pre-wrap">
        {{ currentAnnouncement?.content }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useAnnouncementStore } from '@/stores/announcement'
import Header from '@/components/Header.vue'

const announcementStore = useAnnouncementStore()

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalAnnouncements = ref(0)

// 搜索相关
const searchQuery = ref('')

// 对话框相关
const dialogVisible = ref(false)
const currentAnnouncement = ref(null)

// 获取公告数据
onMounted(() => {
  announcementStore.fetchAllAnnouncements()
})

// 计算属性：筛选后的公告列表
const filteredAnnouncements = computed(() => {
  if (!announcementStore.announcements.length) return []
  
  let result = [...announcementStore.announcements]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(announcement => 
      announcement.title.toLowerCase().includes(query) || 
      announcement.content.toLowerCase().includes(query)
    )
  }
  
  // 排序：按发布时间降序
  result.sort((a, b) => new Date(b.publishTime) - new Date(a.publishTime))
  
  // 更新总数
  totalAnnouncements.value = result.length
  
  // 返回当前页的数据
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

// 事件处理方法
const handleSearch = () => {
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const viewAnnouncement = (announcement) => {
  currentAnnouncement.value = announcement
  dialogVisible.value = true
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script> 