<template>
  <div class="min-h-screen flex flex-col">
    <NavBar />
    
    <main class="flex-grow container mx-auto px-4 py-6">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
    <footer class="bg-gray-800 text-white py-6">
      <div class="container mx-auto px-4">
        <div class="flex flex-col md:flex-row justify-between items-center">
          <div class="mb-4 md:mb-0">
            <h3 class="text-xl font-bold">体育赛事管理系统</h3>
            <p class="text-gray-400 mt-2">© {{ currentYear }} SEMS. 保留所有权利。</p>
          </div>
          
          <div class="flex space-x-6">
            <a href="#" class="text-gray-400 hover:text-white">
              <span>关于我们</span>
            </a>
            <a href="#" class="text-gray-400 hover:text-white">
              <span>使用条款</span>
            </a>
            <a href="#" class="text-gray-400 hover:text-white">
              <span>隐私政策</span>
            </a>
            <a href="#" class="text-gray-400 hover:text-white">
              <span>联系我们</span>
            </a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import NavBar from '../components/NavBar.vue';

// 获取当前年份
const currentYear = computed(() => new Date().getFullYear());
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 