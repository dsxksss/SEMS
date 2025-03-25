import { defineStore } from 'pinia'
import { ref } from 'vue'
import { announcementApi } from '@/api'
import { useAppStore } from './app'

export const useAnnouncementStore = defineStore('announcement', () => {
  const appStore = useAppStore()
  
  // 公告列表
  const announcements = ref([])
  
  // 当前页码等分页信息
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0
  })
  
  // 加载状态
  const loading = ref(false)
  
  // 获取公告列表
  const fetchAnnouncements = async (params = {}) => {
    try {
      loading.value = true
      const { page = 1, size = 10, keyword } = params
      
      const result = await announcementApi.getAnnouncementList({
        page,
        size,
        keyword
      })
      
      announcements.value = result.data || []
      pagination.value = {
        page: params.page || page,
        size: params.size || size,
        total: result.total || 0
      }
      
      return result
    } catch (error) {
      console.error('获取公告列表失败:', error)
      appStore.showError('获取公告列表失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 获取最新公告
  const fetchLatestAnnouncements = async (limit = 4) => {
    try {
      loading.value = true
      const result = await announcementApi.getLatestAnnouncements(limit)
      return result.data || []
    } catch (error) {
      console.error('获取最新公告失败:', error)
      appStore.showError('获取最新公告失败，请稍后重试')
      return []
    } finally {
      loading.value = false
    }
  }
  
  // 获取公告详情
  const fetchAnnouncementDetail = async (id) => {
    try {
      loading.value = true
      const result = await announcementApi.getAnnouncementDetail(id)
      return result.data
    } catch (error) {
      console.error('获取公告详情失败:', error)
      appStore.showError('获取公告详情失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 创建公告
  const createAnnouncement = async (data) => {
    try {
      loading.value = true
      const result = await announcementApi.createAnnouncement(data)
      appStore.showSuccess('创建公告成功')
      return result.data
    } catch (error) {
      console.error('创建公告失败:', error)
      appStore.showError('创建公告失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 更新公告
  const updateAnnouncement = async (id, data) => {
    try {
      loading.value = true
      const result = await announcementApi.updateAnnouncement(id, data)
      appStore.showSuccess('更新公告成功')
      return result.data
    } catch (error) {
      console.error('更新公告失败:', error)
      appStore.showError('更新公告失败，请稍后重试')
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 删除公告
  const deleteAnnouncement = async (id) => {
    try {
      loading.value = true
      await announcementApi.deleteAnnouncement(id)
      appStore.showSuccess('删除公告成功')
      return true
    } catch (error) {
      console.error('删除公告失败:', error)
      appStore.showError('删除公告失败，请稍后重试')
      return false
    } finally {
      loading.value = false
    }
  }
  
  return {
    announcements,
    pagination,
    loading,
    fetchAnnouncements,
    fetchLatestAnnouncements,
    fetchAnnouncementDetail,
    createAnnouncement,
    updateAnnouncement,
    deleteAnnouncement
  }
})