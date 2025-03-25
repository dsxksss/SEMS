<template>
  <div class="max-w-4xl mx-auto p-4">
    <h2 class="text-2xl font-bold mb-4">API通信测试</h2>
    
    <!-- 用户认证测试 -->
    <div class="mb-8 p-4 border rounded-lg">
      <h3 class="text-xl font-bold mb-4">用户认证测试</h3>
      <div class="space-y-4">
        <div class="flex gap-4 items-center">
          <el-input v-model="loginForm.username" placeholder="用户名" />
          <el-input v-model="loginForm.password" type="password" placeholder="密码" />
          <el-button type="primary" @click="testLogin">登录测试</el-button>
        </div>
        <div v-if="loginResult" class="mt-2">
          <div class="text-sm text-gray-600">Token:</div>
          <div class="bg-gray-100 p-2 rounded break-all">{{ loginResult }}</div>
        </div>
      </div>
    </div>
    
    <!-- 赛事API测试 -->
    <div class="mb-8 p-4 border rounded-lg">
      <h3 class="text-xl font-bold mb-4">赛事API测试</h3>
      <div class="space-y-4">
        <div class="flex gap-4">
          <el-button @click="testGetEvents">获取赛事列表</el-button>
          <el-button @click="testGetEventsByStatus">获取报名中的赛事</el-button>
          <el-button @click="testGetEventCategories">获取赛事分类</el-button>
        </div>
      </div>
      
      <div v-if="eventTestResult" class="mt-4">
        <div class="flex justify-between items-center mb-2">
          <div class="text-sm text-gray-600">测试结果:</div>
          <el-button type="text" @click="eventTestResult = null">清除</el-button>
        </div>
        <pre class="bg-gray-100 p-4 rounded-lg overflow-auto max-h-60">{{ JSON.stringify(eventTestResult, null, 2) }}</pre>
      </div>
    </div>
    
    <!-- 报名API测试 -->
    <div class="mb-8 p-4 border rounded-lg">
      <h3 class="text-xl font-bold mb-4">报名API测试</h3>
      <div class="space-y-4">
        <div class="flex gap-4">
          <el-button @click="testGetUserRegistrations">获取报名历史</el-button>
          <el-button type="primary" @click="testRegister">测试报名提交</el-button>
        </div>
      </div>
      
      <div v-if="registrationTestResult" class="mt-4">
        <div class="flex justify-between items-center mb-2">
          <div class="text-sm text-gray-600">测试结果:</div>
          <el-button type="text" @click="registrationTestResult = null">清除</el-button>
        </div>
        <pre class="bg-gray-100 p-4 rounded-lg overflow-auto max-h-60">{{ JSON.stringify(registrationTestResult, null, 2) }}</pre>
      </div>
    </div>

    <!-- 错误日志 -->
    <div class="mb-8 p-4 border rounded-lg">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-xl font-bold">错误日志</h3>
        <el-button type="text" @click="errorLogs = []">清除日志</el-button>
      </div>
      <div v-if="errorLogs.length" class="space-y-2">
        <div v-for="(log, index) in errorLogs" :key="index" 
          class="p-2 rounded" 
          :class="log.type === 'error' ? 'bg-red-50 text-red-700' : 'bg-yellow-50 text-yellow-700'"
        >
          <div class="font-bold">{{ log.title }}</div>
          <div class="text-sm">{{ log.message }}</div>
          <div class="text-xs opacity-75">{{ log.time }}</div>
        </div>
      </div>
      <div v-else class="text-gray-500 text-center py-4">
        暂无错误日志
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi, eventApi, registrationApi } from '@/api'
import { ElMessage } from 'element-plus'

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})
const loginResult = ref(null)

// 测试结果
const eventTestResult = ref(null)
const registrationTestResult = ref(null)
const errorLogs = ref([])

// 添加错误日志
const addErrorLog = (title, message, type = 'error') => {
  errorLogs.value.unshift({
    title,
    message,
    type,
    time: new Date().toLocaleString()
  })
}

// 测试登录
const testLogin = async () => {
  try {
    const result = await userApi.login(loginForm.value)
    loginResult.value = result.token
    localStorage.setItem('token', result.token)
    ElMessage.success('登录成功')
  } catch (error) {
    addErrorLog('登录失败', error.message)
  }
}

// 测试获取赛事列表
const testGetEvents = async () => {
  try {
    const result = await eventApi.getAll()
    eventTestResult.value = result
    ElMessage.success('获取赛事列表成功')
  } catch (error) {
    addErrorLog('获取赛事列表失败', error.message)
  }
}

// 测试获取报名中的赛事
const testGetEventsByStatus = async () => {
  try {
    const result = await eventApi.getByStatus('REGISTERING')
    eventTestResult.value = result
    ElMessage.success('获取报名中的赛事成功')
  } catch (error) {
    addErrorLog('获取报名中的赛事失败', error.message)
  }
}

// 测试获取赛事分类
const testGetEventCategories = async () => {
  try {
    const result = await eventApi.getCategories()
    eventTestResult.value = result
    ElMessage.success('获取赛事分类成功')
  } catch (error) {
    addErrorLog('获取赛事分类失败', error.message)
  }
}

// 测试获取用户报名历史
const testGetUserRegistrations = async () => {
  try {
    const result = await registrationApi.getUserRegistrations()
    registrationTestResult.value = result
    ElMessage.success('获取用户报名历史成功')
  } catch (error) {
    addErrorLog('获取用户报名历史失败', error.message)
  }
}

// 测试报名提交
const testRegister = async () => {
  try {
    const testData = {
      eventId: 1,
      category: 'TEST_CATEGORY',
      realName: '测试用户',
      idNumber: '110101199001011234',
      phoneNumber: '13800138000',
      gender: 'MALE',
      birthdate: '1990-01-01',
      height: 175,
      weight: 65,
      emergencyContact: '紧急联系人',
      emergencyPhone: '13900139000',
      emergencyRelation: '父母',
      experience: '测试运动经历',
      healthCondition: '健康状况良好',
      agreement: true
    }
    
    const result = await registrationApi.register(testData)
    registrationTestResult.value = result
    ElMessage.success('报名提交成功')
  } catch (error) {
    addErrorLog('报名提交失败', error.message)
  }
}

onMounted(() => {
  // 测试页面加载时默认执行的操作
  testGetEvents()
})
</script> 