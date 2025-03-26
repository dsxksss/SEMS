<script setup lang="ts">
defineProps({
  title: {
    type: String,
    required: true
  }
});
</script>

<template>
  <div class="auth-layout">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <div class="logo-container">
            <div class="logo-icon">S</div>
          </div>
          <h1 class="auth-logo">体育赛事管理系统</h1>
          <h2 class="auth-title">{{ title }}</h2>
        </div>
        
        <div class="auth-content">
          <slot></slot>
        </div>
        
        <div class="auth-footer">
          <p>&copy; {{ new Date().getFullYear() }} 体育赛事管理系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 20px;
}

.auth-container {
  width: 100%;
  max-width: 440px;
  perspective: 1000px;
}

.auth-card {
  background-color: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  padding: 40px;
  transition: all 0.3s ease;
  transform-style: preserve-3d;
  overflow: hidden;
  position: relative;
}

.auth-card:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
  transform: translateY(-5px);
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.logo-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #1989fa 100%);
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.auth-header {
  text-align: center;
  margin-bottom: 36px;
}

.auth-logo {
  font-size: 20px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
  margin-top: 16px;
}

.auth-title {
  font-size: 18px;
  font-weight: 400;
  color: var(--text-secondary);
}

.auth-content {
  margin-bottom: 24px;
}

.auth-footer {
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 20px;
}
</style> 