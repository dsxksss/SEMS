<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuthStore } from './stores';

const authStore = useAuthStore();

onMounted(async () => {
  // 初始化认证状态
  await authStore.initializeUser();
});
</script>

<template>
  <div class="app">
    <router-view />
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

body {
  font-family: 'Noto Sans SC', sans-serif;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background-color: #f5f7fa;
}

#app {
  height: 100%;
}

.app {
  height: 100%;
}
</style>
