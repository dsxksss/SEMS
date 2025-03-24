import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useEventStore = defineStore('event', () => {
  // 状态
  const events = ref([])
  const currentEvent = ref(null)
  const isLoading = ref(false)
  const error = ref(null)
  
  // getters
  const upcomingEvents = computed(() => 
    events.value.filter(event => new Date(event.startTime) > new Date())
  )
  
  const pastEvents = computed(() => 
    events.value.filter(event => new Date(event.endTime) < new Date())
  )
  
  const ongoingEvents = computed(() => 
    events.value.filter(event => {
      const now = new Date()
      return new Date(event.startTime) <= now && new Date(event.endTime) >= now
    })
  )
  
  // actions
  async function fetchAllEvents() {
    isLoading.value = true
    error.value = null
    
    try {
      console.log('正在获取赛事数据...');
      const response = await axios.get('/api/events');
      console.log('获取赛事数据成功:', response.data);
      events.value = response.data.data || [];
    } catch (err) {
      console.error('获取赛事数据失败:', err);
      error.value = err.response?.data?.message || '获取赛事列表失败';
    } finally {
      isLoading.value = false;
    }
  }
  
  async function fetchEventById(id) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`/api/events/${id}`)
      currentEvent.value = response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '获取赛事详情失败'
    } finally {
      isLoading.value = false
    }
  }
  
  async function createEvent(eventData) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post('/api/events', eventData)
      return response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '创建赛事失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  async function updateEvent(id, eventData) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.put(`/api/events/${id}`, eventData)
      if (currentEvent.value && currentEvent.value.id === id) {
        currentEvent.value = response.data.data
      }
      
      const index = events.value.findIndex(e => e.id === id)
      if (index !== -1) {
        events.value[index] = response.data.data
      }
      
      return response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '更新赛事失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  async function deleteEvent(id) {
    isLoading.value = true
    error.value = null
    
    try {
      await axios.delete(`/api/events/${id}`)
      
      if (currentEvent.value && currentEvent.value.id === id) {
        currentEvent.value = null
      }
      
      events.value = events.value.filter(e => e.id !== id)
      return true
    } catch (err) {
      error.value = err.response?.data?.message || '删除赛事失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  return {
    events,
    currentEvent,
    isLoading,
    error,
    upcomingEvents,
    pastEvents,
    ongoingEvents,
    fetchAllEvents,
    fetchEventById,
    createEvent,
    updateEvent,
    deleteEvent
  }
}) 