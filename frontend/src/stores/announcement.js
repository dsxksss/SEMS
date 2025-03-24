import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useAnnouncementStore = defineStore('announcement', () => {
  // 状态
  const announcements = ref([])
  const currentAnnouncement = ref(null)
  const isLoading = ref(false)
  const error = ref(null)
  
  // actions
  async function fetchAllAnnouncements() {
    isLoading.value = true
    error.value = null
    
    try {
      console.log('正在获取公告数据...');
      const response = await axios.get('/api/announcements');
      console.log('获取公告数据成功:', response.data);
      announcements.value = response.data.data || [];
    } catch (err) {
      console.error('获取公告数据失败:', err);
      error.value = err.response?.data?.message || '获取公告列表失败';
    } finally {
      isLoading.value = false;
    }
  }
  
  async function fetchAnnouncementById(id) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`/api/announcements/${id}`)
      currentAnnouncement.value = response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '获取公告详情失败'
    } finally {
      isLoading.value = false
    }
  }
  
  async function createAnnouncement(announcementData) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post('/api/announcements', announcementData)
      return response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '创建公告失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  async function updateAnnouncement(id, announcementData) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.put(`/api/announcements/${id}`, announcementData)
      
      if (currentAnnouncement.value && currentAnnouncement.value.id === id) {
        currentAnnouncement.value = response.data.data
      }
      
      const index = announcements.value.findIndex(a => a.id === id)
      if (index !== -1) {
        announcements.value[index] = response.data.data
      }
      
      return response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '更新公告失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  async function deleteAnnouncement(id) {
    isLoading.value = true
    error.value = null
    
    try {
      await axios.delete(`/api/announcements/${id}`)
      
      if (currentAnnouncement.value && currentAnnouncement.value.id === id) {
        currentAnnouncement.value = null
      }
      
      announcements.value = announcements.value.filter(a => a.id !== id)
      return true
    } catch (err) {
      error.value = err.response?.data?.message || '删除公告失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  return {
    announcements,
    currentAnnouncement,
    isLoading,
    error,
    fetchAllAnnouncements,
    fetchAnnouncementById,
    createAnnouncement,
    updateAnnouncement,
    deleteAnnouncement
  }
}) 