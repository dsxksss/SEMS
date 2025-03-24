import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useRegistrationStore = defineStore('registration', () => {
  // 状态
  const registrations = ref([])
  const myRegistrations = ref([])
  const currentRegistration = ref(null)
  const isLoading = ref(false)
  const error = ref(null)
  
  // actions
  async function fetchMyRegistrations() {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get('/api/registrations/my')
      myRegistrations.value = response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '获取我的报名记录失败'
    } finally {
      isLoading.value = false
    }
  }
  
  async function fetchEventRegistrations(eventId) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`/api/registrations/event/${eventId}`)
      registrations.value = response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '获取赛事报名记录失败'
    } finally {
      isLoading.value = false
    }
  }
  
  async function fetchRegistrationById(id) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.get(`/api/registrations/${id}`)
      currentRegistration.value = response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '获取报名详情失败'
    } finally {
      isLoading.value = false
    }
  }
  
  async function createRegistration(registrationData) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post('/api/registrations', registrationData)
      return response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || '报名失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  async function cancelRegistration(id) {
    isLoading.value = true
    error.value = null
    
    try {
      await axios.delete(`/api/registrations/${id}`)
      
      myRegistrations.value = myRegistrations.value.filter(r => r.id !== id)
      registrations.value = registrations.value.filter(r => r.id !== id)
      
      if (currentRegistration.value && currentRegistration.value.id === id) {
        currentRegistration.value = null
      }
      
      return true
    } catch (err) {
      error.value = err.response?.data?.message || '取消报名失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  async function reviewRegistration(id, status, remark) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.put(`/api/registrations/${id}/review`, null, {
        params: { status, remark }
      })
      
      const updatedRegistration = response.data.data
      
      if (currentRegistration.value && currentRegistration.value.id === id) {
        currentRegistration.value = updatedRegistration
      }
      
      const regIndex = registrations.value.findIndex(r => r.id === id)
      if (regIndex !== -1) {
        registrations.value[regIndex] = updatedRegistration
      }
      
      return updatedRegistration
    } catch (err) {
      error.value = err.response?.data?.message || '审核报名失败'
      throw error.value
    } finally {
      isLoading.value = false
    }
  }
  
  return {
    registrations,
    myRegistrations,
    currentRegistration,
    isLoading,
    error,
    fetchMyRegistrations,
    fetchEventRegistrations,
    fetchRegistrationById,
    createRegistration,
    cancelRegistration,
    reviewRegistration
  }
})