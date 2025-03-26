<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const goBack = () => {
  router.back();
};

const goHome = () => {
  router.push('/');
};
</script>

<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-description">您访问的页面不存在或已被移除</p>
      
      <div class="action-buttons">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.error-code {
  font-size: 72px;
  font-weight: 700;
  color: #409eff;
  margin: 0;
  line-height: 1;
}

.error-title {
  font-size: 24px;
  margin: 16px 0;
  color: #333;
}

.error-description {
  color: #606266;
  margin-bottom: 32px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style> 