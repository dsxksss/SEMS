import { defineStore } from 'pinia'
import { ref } from 'vue'
import { eventApi } from '@/api'
import { useAppStore } from './app'

export const useEventStore = defineStore('event', () => {
  const appStore = useAppStore()
  
  // 赛事列表
  const events = ref([])
  
  // 当前页码等分页信息
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0
  })
  
  // 加载状态
  const loading = ref(false)
  
  // 获取赛事列表
  const fetchEvents = async (params = {}) => {
    try {
      loading.value = true
      const { page = 1, size = 10, keyword, category, status } = params
      
      const result = await eventApi.getEventList({
        page,
        size,
        keyword,
        category,
        status
      })
      
      events.value = result.data || []
      pagination.value = {
        page: params.page || page,
        size: params.size || size,
        total: result.total || 0
      }
      
      return result
    } catch (error) {
      console.error('获取赛事列表失败:', error)
      appStore.showError('获取赛事列表失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 获取即将开始的赛事
  const fetchUpcomingEvents = async (days = 7) => {
    try {
      loading.value = true
      const result = await eventApi.getUpcomingEvents(days)
      return result.data || []
    } catch (error) {
      console.error('获取即将开始的赛事失败:', error)
      appStore.showError('获取即将开始的赛事失败，请稍后重试')
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 获取推荐赛事
  const fetchRecommendedEvents = async (limit = 3) => {
    try {
      loading.value = true
      const result = await eventApi.getRecommendedEvents(limit)
      return result.data || []
    } catch (error) {
      console.error('获取推荐赛事失败:', error)
      appStore.showError('获取推荐赛事失败，请稍后重试')
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 获取公开赛事列表（无需登录）
  const fetchPublicEvents = async (params = {}) => {
    try {
      loading.value = true
      const result = await eventApi.getPublicEvents(params)
      
      events.value = result.data?.list || []
      pagination.value = {
        page: params.page || 1,
        size: params.size || 10,
        total: result.data?.total || 0
      }
      
      return result
    } catch (error) {
      console.error('获取公开赛事列表失败:', error)
      appStore.showError('获取公开赛事列表失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 获取赛事详情
  const fetchEventDetail = async (id) => {
    try {
      loading.value = true
      const result = await eventApi.getEventDetail(id)
      return result.data
    } catch (error) {
      console.error('获取赛事详情失败:', error)
      appStore.showError('获取赛事详情失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 创建赛事
  const createEvent = async (eventData) => {
    try {
      loading.value = true
      const result = await eventApi.createEvent(eventData)
      appStore.showSuccess('创建赛事成功')
      return result.data
    } catch (error) {
      console.error('创建赛事失败:', error)
      appStore.showError('创建赛事失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 更新赛事
  const updateEvent = async (id, eventData) => {
    try {
      loading.value = true
      const result = await eventApi.updateEvent(id, eventData)
      appStore.showSuccess('更新赛事成功')
      return result.data
    } catch (error) {
      console.error('更新赛事失败:', error)
      appStore.showError('更新赛事失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 删除赛事
  const deleteEvent = async (id) => {
    try {
      loading.value = true
      await eventApi.deleteEvent(id)
      appStore.showSuccess('删除赛事成功')
      return true
    } catch (error) {
      console.error('删除赛事失败:', error)
      appStore.showError('删除赛事失败，请稍后重试')
      return false
    } finally {
      loading.value = false
    }
  }
  
  // 获取赛事统计数据
  const fetchEventStats = async () => {
    try {
      const result = await eventApi.getEventStats()
      return result.data
    } catch (error) {
      console.error('获取赛事统计数据失败:', error)
      appStore.showError('获取赛事统计数据失败，请稍后重试')
      return null
    }
  }
  
  return {
    events,
    pagination,
    loading,
    fetchEvents,
    fetchUpcomingEvents,
    fetchRecommendedEvents,
    fetchPublicEvents,
    fetchEventDetail,
    createEvent,
    updateEvent,
    deleteEvent,
    fetchEventStats
  }
}) 