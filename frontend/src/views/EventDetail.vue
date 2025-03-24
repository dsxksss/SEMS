<template>
  <div>
    <Header />
    <!-- 加载状态 -->
    <div v-if="eventStore.isLoading" class="container mx-auto px-4 py-12">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 错误提示 -->
    <div v-else-if="eventStore.error" class="container mx-auto px-4 py-12 text-center">
      <el-result
        icon="error"
        :title="eventStore.error"
        sub-title="请稍后再试或联系管理员"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/events')">返回赛事列表</el-button>
        </template>
      </el-result>
    </div>
    
    <!-- 赛事详情 -->
    <div v-else-if="eventStore.currentEvent" class="container mx-auto px-4 py-12">
      <!-- 顶部信息区域 -->
      <div class="bg-white rounded-lg shadow-md overflow-hidden mb-6">
        <div class="relative">
          <div class="h-64 bg-gradient-to-r from-blue-500 to-purple-500">
            <img 
              v-if="eventStore.currentEvent.coverImage" 
              :src="eventStore.currentEvent.coverImage" 
              :alt="eventStore.currentEvent.name" 
              class="w-full h-full object-cover"
            >
          </div>
          
          <div class="absolute top-4 right-4">
            <el-tag 
              :type="getEventStatusType(eventStore.currentEvent)" 
              effect="dark"
              size="large"
              class="px-3 py-1"
            >
              {{ getEventStatusText(eventStore.currentEvent) }}
            </el-tag>
          </div>
        </div>
        
        <div class="p-6">
          <div class="flex flex-wrap justify-between items-start">
            <div class="mb-4 md:mb-0">
              <h1 class="text-3xl font-bold text-gray-800 mb-2">{{ eventStore.currentEvent.name }}</h1>
              <div class="flex flex-wrap gap-2 mb-4">
                <el-tag v-for="tag in (eventStore.currentEvent.categories || [])" :key="tag" type="info" effect="plain">
                  {{ tag }}
                </el-tag>
                <el-tag v-if="eventStore.currentEvent.featured" type="warning">推荐赛事</el-tag>
              </div>
            </div>
            
            <div class="flex items-center gap-4">
              <template v-if="canRegister">
                <el-button 
                  type="primary" 
                  size="large" 
                  @click="showRegistrationDialog = true"
                >
                  立即报名
                </el-button>
              </template>
              <template v-else-if="!userStore.isLoggedIn">
                <router-link :to="{ name: 'Login', query: { redirect: $route.fullPath } }">
                  <el-button type="primary" size="large">登录后报名</el-button>
                </router-link>
              </template>
              <template v-else-if="isRegistrationClosed">
                <el-button type="info" size="large" disabled>报名已截止</el-button>
              </template>
              <template v-else-if="isEventEnded">
                <el-button type="info" size="large" disabled>赛事已结束</el-button>
              </template>
              <template v-else-if="isFullyBooked">
                <el-button type="info" size="large" disabled>名额已满</el-button>
              </template>
              <template v-else-if="isRegistered">
                <el-button type="danger" @click="handleCancelRegistration">取消报名</el-button>
              </template>
              
              <el-button type="info" size="large" @click="handleShare">
                分享
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左侧主要内容 -->
        <div class="lg:col-span-2">
          <el-tabs v-model="activeTab" class="bg-white rounded-lg shadow-md p-6">
            <el-tab-pane label="赛事详情" name="details">
              <div class="rich-text-content">{{ eventStore.currentEvent.description }}</div>
            </el-tab-pane>
            
            <el-tab-pane label="报名须知" name="registration">
              <div v-if="eventStore.currentEvent.registrationGuidelines" class="rich-text-content">
                {{ eventStore.currentEvent.registrationGuidelines }}
              </div>
              <div v-else>
                <h3 class="text-xl font-medium mb-4">参赛要求</h3>
                <ul class="list-disc pl-6 mb-6 space-y-2">
                  <li>参赛选手需年满18周岁（特殊赛事可能有不同年龄要求）</li>
                  <li>参赛选手须保证身体健康，适合参加体育比赛</li>
                  <li>参赛选手需在报名截止日期前完成报名并缴纳相关费用</li>
                  <li>参赛选手须遵守赛事规则和组织方安排</li>
                </ul>
                
                <h3 class="text-xl font-medium mb-4">报名流程</h3>
                <ol class="list-decimal pl-6 mb-6 space-y-2">
                  <li>在线填写报名信息</li>
                  <li>提交身份证明和健康声明</li>
                  <li>等待组织方审核</li>
                  <li>收到确认通知后完成报名</li>
                </ol>
                
                <h3 class="text-xl font-medium mb-4">注意事项</h3>
                <ul class="list-disc pl-6 space-y-2">
                  <li>报名成功后，如需取消，请按照规定时间申请退款</li>
                  <li>赛事当天请携带身份证明和参赛证件</li>
                  <li>因天气等不可抗力因素，组织方有权调整赛事时间或取消赛事</li>
                </ul>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="赛事图片" name="images">
              <div v-if="eventStore.currentEvent.images && eventStore.currentEvent.images.length > 0" class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div 
                  v-for="(image, index) in eventStore.currentEvent.images" 
                  :key="index" 
                  class="relative aspect-video rounded-md overflow-hidden cursor-pointer"
                  @click="previewImage(index)"
                >
                  <img :src="image" class="w-full h-full object-cover">
                </div>
              </div>
              <el-empty v-else description="暂无赛事图片" />
            </el-tab-pane>
          </el-tabs>
          
          <!-- 相关赛事 -->
          <div v-if="relatedEvents.length > 0" class="bg-white rounded-lg shadow-md p-6 mt-6">
            <h2 class="text-xl font-bold mb-4">相关赛事</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div 
                v-for="relatedEvent in relatedEvents" 
                :key="relatedEvent.id" 
                class="border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
                @click="goToEvent(relatedEvent.id)"
              >
                <h3 class="text-lg font-medium mb-2">{{ relatedEvent.name }}</h3>
                <div class="flex items-center text-sm text-gray-500 mb-2">
                  <el-icon><Calendar /></el-icon>
                  <span class="ml-1">{{ formatDate(relatedEvent.startTime || relatedEvent.eventDate) }}</span>
                </div>
                <div class="flex items-center text-sm text-gray-500">
                  <el-icon><Location /></el-icon>
                  <span class="ml-1">{{ relatedEvent.location }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧信息栏 -->
        <div>
          <div class="bg-white rounded-lg shadow-md p-6 mb-6">
            <h2 class="text-xl font-bold mb-4">赛事信息</h2>
            
            <div class="space-y-4">
              <div>
                <h3 class="text-gray-500 mb-1">举办时间</h3>
                <p class="font-medium">{{ formatDate(eventStore.currentEvent.startTime) }}</p>
                <p v-if="eventStore.currentEvent.endTime" class="font-medium">
                  至 {{ formatDate(eventStore.currentEvent.endTime) }}
                </p>
              </div>
              
              <div>
                <h3 class="text-gray-500 mb-1">举办地点</h3>
                <p class="font-medium">{{ eventStore.currentEvent.location }}</p>
                <div v-if="eventStore.currentEvent.locationMap" class="mt-2 aspect-video rounded-md overflow-hidden">
                  <img :src="eventStore.currentEvent.locationMap" alt="地点地图" class="w-full h-full object-cover">
                </div>
              </div>
              
              <div>
                <h3 class="text-gray-500 mb-1">报名截止</h3>
                <p class="font-medium">{{ formatDateTime(eventStore.currentEvent.registrationDeadline) }}</p>
                <p v-if="isRegistrationDeadlineSoon()" class="text-red-500 text-sm mt-1">
                  仅剩 {{ getRemainingDays() }} 天截止报名
                </p>
              </div>
              
              <div>
                <h3 class="text-gray-500 mb-1">参赛人数</h3>
                <div class="flex items-center">
                  <span class="font-medium mr-2">
                    {{ eventStore.currentEvent.currentParticipants || 0 }}/{{ eventStore.currentEvent.maxParticipants || '不限' }}
                  </span>
                  <el-progress 
                    :percentage="calculateRegistrationPercentage()" 
                    :color="getRegistrationProgressColor()"
                    :stroke-width="10" 
                    class="flex-grow"
                  />
                </div>
              </div>
              
              <div>
                <h3 class="text-gray-500 mb-1">组织方</h3>
                <p class="font-medium">{{ eventStore.currentEvent.organizer || '未提供' }}</p>
              </div>
              
              <div>
                <h3 class="text-gray-500 mb-1">联系方式</h3>
                <p class="font-medium">{{ eventStore.currentEvent.contactInfo || '未提供' }}</p>
              </div>
            </div>
          </div>
          
          <!-- 二维码分享 -->
          <div class="bg-white rounded-lg shadow-md p-6 text-center">
            <h2 class="text-xl font-bold mb-4">分享赛事</h2>
            <div class="flex justify-center mb-4">
              <div class="w-36 h-36 bg-gray-200 flex items-center justify-center">
                <span class="text-sm text-gray-500">赛事二维码</span>
              </div>
            </div>
            <p class="text-gray-500 text-sm">扫描二维码分享赛事</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 无数据提示 -->
    <div v-else class="container mx-auto px-4 py-12 text-center">
      <el-result
        icon="info"
        title="未找到赛事信息"
        sub-title="该赛事可能不存在或已被删除"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/events')">返回赛事列表</el-button>
        </template>
      </el-result>
    </div>
    
    <!-- 报名对话框 -->
    <el-dialog
      v-model="showRegistrationDialog"
      title="赛事报名"
      width="500px"
    >
      <el-form
        ref="registrationFormRef"
        :model="registrationForm"
        :rules="registrationRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="registrationForm.realName" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="registrationForm.phone" />
        </el-form-item>
        
        <el-form-item label="报名类型" prop="type">
          <el-radio-group v-model="registrationForm.type">
            <el-radio v-if="userStore.role === 'ATHLETE'" label="ATHLETE">运动员</el-radio>
            <el-radio v-if="userStore.role === 'USER'" label="AUDIENCE">观众</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="registrationForm.remark" 
            type="textarea" 
            placeholder="可选，如有特殊需求请说明"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="showRegistrationDialog = false">取消</el-button>
          <el-button type="primary" :loading="registering" @click="handleSubmitRegistration">
            确认报名
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 取消报名确认框 -->
    <el-dialog
      v-model="showCancelDialog"
      title="取消报名"
      width="400px"
    >
      <div class="py-4">
        <p>确定要取消报名吗？取消后需要重新报名。</p>
      </div>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="showCancelDialog = false">返回</el-button>
          <el-button type="danger" :loading="cancelling" @click="confirmCancelRegistration">
            确认取消
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showPreview"
      :url-list="eventStore.currentEvent.images || []"
      :initial-index="previewIndex"
      @close="showPreview = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useEventStore } from '@/stores/event'
import { useUserStore } from '@/stores/user'
import { useRegistrationStore } from '@/stores/registration'
import Header from '@/components/Header.vue'

const route = useRoute()
const router = useRouter()
const eventStore = useEventStore()
const userStore = useUserStore()
const registrationStore = useRegistrationStore()

// 获取赛事ID
const eventId = computed(() => route.params.id)

// 报名表单相关
const registrationFormRef = ref(null)
const showRegistrationDialog = ref(false)
const registering = ref(false)
const registrationForm = ref({
  realName: '',
  phone: '',
  type: userStore.role === 'ATHLETE' ? 'ATHLETE' : 'AUDIENCE',
  remark: ''
})

// 取消报名相关
const showCancelDialog = ref(false)
const cancelling = ref(false)
const currentRegistration = ref(null)

// 表单验证规则
const registrationRules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择报名类型', trigger: 'change' }
  ]
}

// 获取赛事详情和用户报名信息
onMounted(async () => {
  if (eventId.value) {
    await eventStore.fetchEventById(eventId.value)
    
    if (userStore.isLoggedIn) {
      await registrationStore.fetchMyRegistrations()
      checkUserRegistration()
    }
  }
})

// 检查用户是否已报名该赛事
const isRegistered = ref(false)

const checkUserRegistration = () => {
  const registration = registrationStore.myRegistrations.find(
    r => r.eventId === Number(eventId.value)
  )
  
  if (registration) {
    isRegistered.value = true
    currentRegistration.value = registration
  } else {
    isRegistered.value = false
    currentRegistration.value = null
  }
}

// 计算属性：判断是否可以报名
const canRegister = computed(() => {
  return userStore.isLoggedIn && 
         !isRegistered.value && 
         !isRegistrationClosed.value && 
         !isEventEnded.value && 
         !isFullyBooked.value
})

const isRegistrationClosed = computed(() => {
  if (!eventStore.currentEvent || !eventStore.currentEvent.registrationDeadline) return false
  return new Date() > new Date(eventStore.currentEvent.registrationDeadline)
})

const isEventEnded = computed(() => {
  if (!eventStore.currentEvent || !eventStore.currentEvent.endTime) return false
  return new Date() > new Date(eventStore.currentEvent.endTime)
})

const isFullyBooked = computed(() => {
  if (!eventStore.currentEvent) return false
  
  const { maxParticipants, currentParticipants } = eventStore.currentEvent
  if (!maxParticipants) return false // 不限制人数
  
  return currentParticipants >= maxParticipants
})

// 报名提交
const handleSubmitRegistration = async () => {
  if (!registrationFormRef.value) return
  
  await registrationFormRef.value.validate(async (valid) => {
    if (valid) {
      registering.value = true
      
      try {
        const registrationData = {
          ...registrationForm.value,
          eventId: Number(eventId.value)
        }
        
        await registrationStore.createRegistration(registrationData)
        ElMessage.success('报名成功，请等待审核')
        showRegistrationDialog.value = false
        
        // 刷新注册信息
        await registrationStore.fetchMyRegistrations()
        checkUserRegistration()
        
        // 刷新赛事信息
        await eventStore.fetchEventById(eventId.value)
      } catch (error) {
        ElMessage.error(error)
      } finally {
        registering.value = false
      }
    }
  })
}

// 取消报名
const handleCancelRegistration = () => {
  showCancelDialog.value = true
}

const confirmCancelRegistration = async () => {
  if (!currentRegistration.value) return
  
  cancelling.value = true
  
  try {
    await registrationStore.cancelRegistration(currentRegistration.value.id)
    ElMessage.success('已取消报名')
    showCancelDialog.value = false
    
    // 刷新注册信息
    await registrationStore.fetchMyRegistrations()
    checkUserRegistration()
    
    // 刷新赛事信息
    await eventStore.fetchEventById(eventId.value)
  } catch (error) {
    ElMessage.error(error)
  } finally {
    cancelling.value = false
  }
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

const formatDateTime = (dateString) => {
  if (!dateString) return '不限'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取赛事状态样式和文本
const getEventStatusType = (event) => {
  const now = new Date()
  const startTime = new Date(event.startTime)
  const endTime = new Date(event.endTime)
  
  if (now < startTime) {
    return 'info'
  } else if (now >= startTime && now <= endTime) {
    return 'success'
  } else {
    return 'danger'
  }
}

const getEventStatusText = (event) => {
  const now = new Date()
  const startTime = new Date(event.startTime)
  const endTime = new Date(event.endTime)
  
  if (now < startTime) {
    return '即将开始'
  } else if (now >= startTime && now <= endTime) {
    return '进行中'
  } else {
    return '已结束'
  }
}

// 获取相关赛事
const relatedEvents = computed(() => {
  if (!eventStore.currentEvent || !eventStore.currentEvent.relatedEvents) return []
  return eventStore.currentEvent.relatedEvents.map(id => eventStore.events.find(e => e.id === id))
})

// 获取图片预览
const showPreview = ref(false)
const previewIndex = ref(0)

const previewImage = (index) => {
  previewIndex.value = index
  showPreview.value = true
}

// 获取二维码分享
const handleShare = () => {
  // 实现分享逻辑
}

// 获取报名进度
const calculateRegistrationPercentage = () => {
  if (!eventStore.currentEvent) return 0
  const { currentParticipants, maxParticipants } = eventStore.currentEvent
  if (!maxParticipants) return 100
  return Math.round((currentParticipants / maxParticipants) * 100)
}

const getRegistrationProgressColor = () => {
  const percentage = calculateRegistrationPercentage()
  if (percentage < 50) return 'success'
  if (percentage < 80) return 'warning'
  return 'danger'
}

const isRegistrationDeadlineSoon = () => {
  if (!eventStore.currentEvent || !eventStore.currentEvent.registrationDeadline) return false
  const deadline = new Date(eventStore.currentEvent.registrationDeadline)
  const now = new Date()
  const remainingDays = Math.ceil((deadline - now) / (1000 * 60 * 60 * 24))
  return remainingDays <= 7
}

const getRemainingDays = () => {
  if (!eventStore.currentEvent || !eventStore.currentEvent.registrationDeadline) return 0
  const deadline = new Date(eventStore.currentEvent.registrationDeadline)
  const now = new Date()
  const remainingDays = Math.ceil((deadline - now) / (1000 * 60 * 60 * 24))
  return remainingDays
}

// 获取相关赛事
const goToEvent = (id) => {
  router.push({ name: 'EventDetail', params: { id } })
}
</script> 