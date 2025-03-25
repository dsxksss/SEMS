import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

export const useAppStore = defineStore('app', () => {
  // 全局加载状态
  const loading = ref(false)
  const loadingMessage = ref('')

  // 全局错误状态
  const error = ref({
    show: false,
    message: '',
    type: 'error'
  })

  // 设置加载状态
  function setLoading(isLoading, message = '加载中...') {
    loading.value = isLoading
    loadingMessage.value = message
  }

  // 显示消息提示
  function showError(message, type = 'error') {
    if (type === 'success') {
      ElMessage.success(message)
    } else if (type === 'warning') {
      ElMessage.warning(message)
    } else if (type === 'info') {
      ElMessage.info(message)
    } else {
      ElMessage.error(message)
    }
    
    error.value = {
      show: true,
      message,
      type
    }
    
    // 自动关闭
    setTimeout(hideError, 3000)
  }
  
  // 显示成功提示
  function showSuccess(message) {
    showError(message, 'success')
  }

  // 关闭错误提示
  function hideError() {
    error.value.show = false
  }

  return {
    loading,
    loadingMessage,
    error,
    setLoading,
    showError,
    showSuccess,
    hideError
  }
}) 