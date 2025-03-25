<template>
  <div v-if="loading" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
    <div class="bg-white p-6 rounded-lg shadow-xl flex flex-col items-center">
      <div class="animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent"></div>
      <p class="mt-4 text-gray-700">{{ message || '加载中...' }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: ''
  }
})
</script> 