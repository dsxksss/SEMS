<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-800">
    <Header />
    <main>
      <router-view v-slot="{ Component }">
        <transition
          enter-active-class="transform transition duration-300 ease-out"
          enter-from-class="opacity-0"
          enter-to-class="opacity-100"
          leave-active-class="transform transition duration-200 ease-in"
          leave-from-class="opacity-100"
          leave-to-class="opacity-0"
        >
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <Footer />
    
    <!-- 全局加载状态 -->
    <div v-if="appStore.loading" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-8 max-w-sm w-full flex flex-col items-center shadow-2xl">
        <div class="w-16 h-16 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin mb-4"></div>
        <p class="text-gray-700">{{ appStore.loadingMessage || '加载中...' }}</p>
      </div>
    </div>
    
    <!-- 全局错误提示 -->
    <transition
      enter-active-class="transform transition duration-300 ease-out"
      enter-from-class="opacity-0 translate-y-4"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transform transition duration-200 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-4"
    >
      <div v-if="appStore.error.show" 
        class="fixed bottom-4 right-4 bg-white rounded-lg p-4 shadow-xl border-l-4 border-red-500 max-w-md z-50">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-gray-900">{{ appStore.error.type === 'error' ? '出错了' : '成功' }}</h3>
            <div class="mt-1 text-sm text-gray-700">
              {{ appStore.error.message }}
            </div>
            <div class="mt-2">
              <button type="button" 
                class="text-sm text-red-600 hover:text-red-500 font-medium"
                @click="appStore.hideError">
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { useAppStore } from '@/stores/app'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const appStore = useAppStore()
</script>

<style>
@import './assets/css/index.css';

html {
  scroll-behavior: smooth;
}

body {
  font-family: var(--font-sans);
}

/* 隐藏element-plus的focus outline */
.el-button:focus,
.el-input__inner:focus {
  outline: none !important;
}

/* 美化element-plus组件 */
.el-button--primary {
  background-color: var(--primary-600) !important;
  border-color: var(--primary-600) !important;
}

.el-button--primary:hover {
  background-color: var(--primary-700) !important;
  border-color: var(--primary-700) !important;
}

.el-input__inner {
  border-radius: var(--rounded-md) !important;
}
</style> 