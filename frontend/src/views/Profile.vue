<template>
  <div>
    <Header />
    <div class="container mx-auto px-4 py-12">
      <div class="bg-white rounded-lg shadow-md overflow-hidden">
        <!-- 封面和头像区域 -->
        <div class="bg-gradient-to-r from-blue-600 to-purple-600 h-48 relative">
          <div class="absolute -bottom-16 left-8">
            <div class="w-32 h-32 rounded-full border-4 border-white bg-gray-200 flex items-center justify-center overflow-hidden">
              <el-avatar :size="120" :src="userAvatar">
                {{ userStore.username?.charAt(0).toUpperCase() }}
              </el-avatar>
            </div>
          </div>
          
          <!-- 编辑头像按钮 -->
          <div class="absolute -bottom-10 left-28">
            <el-button circle size="small" @click="handleAvatarUpload">
              <el-icon><Edit /></el-icon>
            </el-button>
          </div>
          
          <!-- 隐藏的文件上传 -->
          <input
            ref="avatarInputRef"
            type="file"
            accept="image/*"
            class="hidden"
            @change="uploadAvatar"
          />
        </div>
        
        <!-- 用户信息区域 -->
        <div class="pt-20 px-8 pb-8">
          <div class="flex flex-wrap items-start justify-between">
            <div>
              <h1 class="text-2xl font-bold text-gray-800 mb-1">{{ userStore.username }}</h1>
              <p class="text-gray-600 mb-4">{{ userStore.email }}</p>
              
              <div class="flex flex-wrap gap-2">
                <!-- 用户角色标签 -->
                <el-tag v-if="userStore.role === 'ADMIN'" type="danger">管理员</el-tag>
                <el-tag v-else-if="userStore.role === 'ATHLETE'" type="success">运动员</el-tag>
                <el-tag v-else type="info">观众</el-tag>
                
                <!-- 运动员状态标签 -->
                <el-tag v-if="userStore.athleteStatus === 'PENDING'" type="warning">审核中</el-tag>
                <el-tag v-else-if="userStore.athleteStatus === 'APPROVED'" type="success">已认证</el-tag>
                <el-tag v-else-if="userStore.athleteStatus === 'REJECTED'" type="danger">未通过</el-tag>
                
                <!-- 注册时间 -->
                <div class="text-gray-500 text-sm">
                  加入时间: {{ formatDate(userStore.user?.createTime) }}
                </div>
              </div>
            </div>
            
            <!-- 快捷操作按钮 -->
            <div class="mt-4 md:mt-0">
              <el-button-group>
                <el-button v-if="userStore.role !== 'ATHLETE' && !userStore.athleteStatus" 
                  type="primary" 
                  @click="$router.push('/athlete-registration')"
                >
                  成为运动员
                </el-button>
                <el-button 
                  type="success" 
                  @click="$router.push('/events')"
                >
                  浏览赛事
                </el-button>
              </el-button-group>
            </div>
          </div>
          
          <!-- 选项卡 -->
          <el-tabs v-model="activeTab" class="mt-8">
            <el-tab-pane label="个人信息" name="info">
              <div class="py-4 max-w-3xl">
                <el-form 
                  ref="userFormRef"
                  :model="userForm"
                  :rules="userRules"
                  label-width="100px"
                  class="mt-4"
                >
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <el-form-item label="用户名">
                      <el-input v-model="userForm.username" disabled />
                    </el-form-item>
                    
                    <el-form-item label="邮箱">
                      <el-input v-model="userForm.email" disabled />
                    </el-form-item>
                    
                    <el-form-item label="真实姓名" prop="realName">
                      <el-input v-model="userForm.realName" placeholder="请输入您的真实姓名" />
                    </el-form-item>
                    
                    <el-form-item label="联系电话" prop="phone">
                      <el-input v-model="userForm.phone" placeholder="请输入您的联系电话" />
                    </el-form-item>
                    
                    <el-form-item label="性别" prop="gender">
                      <el-radio-group v-model="userForm.gender">
                        <el-radio label="MALE">男</el-radio>
                        <el-radio label="FEMALE">女</el-radio>
                        <el-radio label="OTHER">其他</el-radio>
                      </el-radio-group>
                    </el-form-item>
                    
                    <el-form-item label="生日" prop="birthdate">
                      <el-date-picker 
                        v-model="userForm.birthdate" 
                        type="date" 
                        placeholder="请选择您的生日"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </div>
                  
                  <el-form-item label="个人简介" prop="bio">
                    <el-input 
                      v-model="userForm.bio" 
                      type="textarea" 
                      rows="4"
                      placeholder="介绍一下您自己吧" 
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" @click="updateUserInfo" :loading="updating">
                      更新信息
                    </el-button>
                    <el-button @click="resetForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="我的报名" name="registrations">
              <div class="py-4">
                <div class="flex justify-between items-center mb-4">
                  <h2 class="text-xl font-bold">我的赛事报名</h2>
                  <el-button type="primary" @click="refreshRegistrations">
                    <el-icon><Refresh /></el-icon> 刷新
                  </el-button>
                </div>
                
                <div v-if="registrationStore.isLoading" class="py-10">
                  <el-skeleton :rows="5" animated />
                </div>
                
                <div v-else-if="registrationStore.error" class="py-10 text-center">
                  <el-alert
                    type="error"
                    :title="registrationStore.error"
                    show-icon
                  />
                </div>
                
                <div v-else-if="registrationStore.myRegistrations.length === 0" class="py-10 text-center">
                  <el-empty description="您还没有报名任何赛事">
                    <template #extra>
                      <el-button type="primary" @click="$router.push('/events')">浏览赛事</el-button>
                    </template>
                  </el-empty>
                </div>
                
                <div v-else>
                  <el-table :data="registrationStore.myRegistrations" border style="width: 100%">
                    <el-table-column prop="eventName" label="赛事名称" min-width="150">
                      <template #default="scope">
                        <router-link 
                          :to="`/events/${scope.row.eventId}`" 
                          class="text-blue-600 hover:underline"
                        >
                          {{ scope.row.eventName }}
                        </router-link>
                      </template>
                    </el-table-column>
                    
                    <el-table-column prop="type" label="报名类型" width="120">
                      <template #default="scope">
                        <el-tag :type="scope.row.type === 'ATHLETE' ? 'success' : 'info'">
                          {{ scope.row.type === 'ATHLETE' ? '运动员' : '观众' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    
                    <el-table-column prop="status" label="审核状态" width="120">
                      <template #default="scope">
                        <el-tag :type="getRegistrationStatusType(scope.row.status)">
                          {{ getRegistrationStatusText(scope.row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    
                    <el-table-column prop="createTime" label="报名时间" width="180">
                      <template #default="scope">
                        {{ formatDate(scope.row.createTime) }}
                      </template>
                    </el-table-column>
                    
                    <el-table-column label="操作" width="150" fixed="right">
                      <template #default="scope">
                        <div class="flex space-x-2">
                          <el-button 
                            link 
                            type="primary" 
                            @click="viewRegistration(scope.row)"
                          >
                            查看
                          </el-button>
                          
                          <el-button 
                            link 
                            type="danger" 
                            @click="confirmCancelRegistration(scope.row)"
                            :disabled="!canCancel(scope.row)"
                          >
                            取消
                          </el-button>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="我的成绩" name="results" v-if="userStore.role === 'ATHLETE'">
              <div class="py-4">
                <div class="flex justify-between items-center mb-4">
                  <h2 class="text-xl font-bold">我的比赛成绩</h2>
                  <el-button type="primary" @click="refreshResults">
                    <el-icon><Refresh /></el-icon> 刷新
                  </el-button>
                </div>
                
                <div v-if="resultsLoading" class="py-10">
                  <el-skeleton :rows="5" animated />
                </div>
                
                <div v-else-if="resultsError" class="py-10 text-center">
                  <el-alert
                    type="error"
                    :title="resultsError"
                    show-icon
                  />
                </div>
                
                <div v-else-if="athleteResults.length === 0" class="py-10 text-center">
                  <el-empty description="您还没有参加过比赛或暂无成绩记录" />
                </div>
                
                <div v-else>
                  <el-table :data="athleteResults" border style="width: 100%">
                    <el-table-column prop="eventName" label="赛事名称" min-width="150">
                      <template #default="scope">
                        <router-link 
                          :to="`/events/${scope.row.eventId}`" 
                          class="text-blue-600 hover:underline"
                        >
                          {{ scope.row.eventName }}
                        </router-link>
                      </template>
                    </el-table-column>
                    
                    <el-table-column prop="rank" label="排名" width="80">
                      <template #default="scope">
                        <span 
                          :class="{
                            'text-red-500 font-bold': scope.row.rank === 1,
                            'text-orange-500 font-bold': scope.row.rank === 2,
                            'text-yellow-600 font-bold': scope.row.rank === 3,
                          }"
                        >
                          {{ scope.row.rank || '未排名' }}
                        </span>
                      </template>
                    </el-table-column>
                    
                    <el-table-column prop="score" label="成绩/得分" width="120" />
                    
                    <el-table-column prop="category" label="比赛项目" width="120" />
                    
                    <el-table-column prop="eventDate" label="比赛日期" width="150">
                      <template #default="scope">
                        {{ formatDate(scope.row.eventDate) }}
                      </template>
                    </el-table-column>
                    
                    <el-table-column label="证书" width="100">
                      <template #default="scope">
                        <el-button 
                          link 
                          type="primary" 
                          @click="viewCertificate(scope.row)"
                          :disabled="!scope.row.certificateUrl"
                        >
                          查看
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="修改密码" name="password">
              <div class="py-4 max-w-md">
                <el-form 
                  ref="passwordFormRef"
                  :model="passwordForm"
                  :rules="passwordRules"
                  label-width="120px"
                >
                  <el-form-item label="当前密码" prop="currentPassword">
                    <el-input 
                      v-model="passwordForm.currentPassword" 
                      type="password" 
                      show-password
                      placeholder="请输入当前密码"
                    />
                  </el-form-item>
                  
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                      v-model="passwordForm.newPassword" 
                      type="password" 
                      show-password
                      placeholder="请输入新密码"
                    />
                  </el-form-item>
                  
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input 
                      v-model="passwordForm.confirmPassword" 
                      type="password" 
                      show-password
                      placeholder="请再次输入新密码"
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" @click="updatePassword" :loading="updatingPassword">
                      更新密码
                    </el-button>
                    <el-button @click="resetPasswordForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
    
    <!-- 报名详情对话框 -->
    <el-dialog
      v-model="registrationDialogVisible"
      title="报名详情"
      width="500px"
    >
      <div v-if="currentRegistration" class="space-y-4">
        <div>
          <h3 class="text-lg font-bold text-gray-800 mb-2">赛事信息</h3>
          <p>{{ currentRegistration.eventName }}</p>
        </div>
        
        <div class="grid grid-cols-2 gap-4">
          <div>
            <h3 class="font-bold text-gray-700">报名类型</h3>
            <p>{{ currentRegistration.type === 'ATHLETE' ? '运动员' : '观众' }}</p>
          </div>
          
          <div>
            <h3 class="font-bold text-gray-700">状态</h3>
            <el-tag :type="getRegistrationStatusType(currentRegistration.status)">
              {{ getRegistrationStatusText(currentRegistration.status) }}
            </el-tag>
          </div>
        </div>
        
        <div>
          <h3 class="font-bold text-gray-700">报名人信息</h3>
          <p>{{ currentRegistration.realName }} / {{ currentRegistration.phone }}</p>
        </div>
        
        <div v-if="currentRegistration.remark">
          <h3 class="font-bold text-gray-700">备注</h3>
          <p>{{ currentRegistration.remark }}</p>
        </div>
        
        <div v-if="currentRegistration.adminRemark">
          <h3 class="font-bold text-gray-700">审核备注</h3>
          <p>{{ currentRegistration.adminRemark }}</p>
        </div>
        
        <div class="grid grid-cols-2 gap-4">
          <div>
            <h3 class="font-bold text-gray-700">报名时间</h3>
            <p>{{ formatDate(currentRegistration.createTime) }}</p>
          </div>
          
          <div v-if="currentRegistration.updateTime">
            <h3 class="font-bold text-gray-700">更新时间</h3>
            <p>{{ formatDate(currentRegistration.updateTime) }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 取消报名确认框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消报名"
      width="400px"
    >
      <div class="py-4">
        <p>确定要取消报名吗？取消后需要重新报名。</p>
      </div>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="cancelDialogVisible = false">返回</el-button>
          <el-button type="danger" :loading="cancelling" @click="handleCancelRegistration">
            确认取消
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 证书查看对话框 -->
    <el-dialog
      v-model="certificateDialogVisible"
      title="成绩证书"
      width="600px"
    >
      <div v-if="currentCertificate" class="text-center">
        <img 
          :src="currentCertificate.certificateUrl" 
          alt="证书" 
          class="max-w-full h-auto my-4 rounded-md shadow-md"
        />
        <div class="mt-4">
          <el-button type="primary" @click="downloadCertificate">
            下载证书
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRegistrationStore } from '@/stores/registration'
import Header from '@/components/Header.vue'
import axios from 'axios'

const userStore = useUserStore()
const registrationStore = useRegistrationStore()

// 用户头像
const userAvatar = computed(() => {
  return userStore.user?.avatar || ''
})

// 选项卡
const activeTab = ref('info')

// 用户信息表单
const userFormRef = ref(null)
const updating = ref(false)
const userForm = reactive({
  username: userStore.username,
  email: userStore.user?.email || '',
  realName: userStore.user?.realName || '',
  phone: userStore.user?.phone || '',
  gender: userStore.user?.gender || 'MALE',
  birthdate: userStore.user?.birthdate ? new Date(userStore.user.birthdate) : null,
  bio: userStore.user?.bio || ''
})

const userRules = {
  realName: [
    { max: 20, message: '真实姓名不能超过20个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ]
}

// 密码表单
const passwordFormRef = ref(null)
const updatingPassword = ref(false)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6到20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 报名相关
const registrationDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const currentRegistration = ref(null)
const cancelling = ref(false)

// 页面加载时获取数据
onMounted(async () => {
  // 获取用户报名信息
  await registrationStore.fetchMyRegistrations()
  
  // 获取用户详情
  try {
    const response = await axios.get(`/api/users/${userStore.userId}`)
    const userData = response.data.data
    
    // 更新表单数据
    userForm.realName = userData.realName || ''
    userForm.phone = userData.phone || ''
    
    // 更新store中的用户数据
    userStore.setUser({
      ...userStore.user,
      realName: userData.realName,
      phone: userData.phone
    })
  } catch (error) {
    console.error('获取用户详情失败', error)
  }
})

// 更新用户信息
const updateUserInfo = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      updating.value = true
      
      try {
        const updateData = {
          realName: userForm.realName,
          phone: userForm.phone,
          gender: userForm.gender,
          birthdate: userForm.birthdate ? userForm.birthdate.toISOString().split('T')[0] : null,
          bio: userForm.bio
        }
        
        const response = await axios.put(`/api/users/${userStore.userId}`, updateData)
        const updatedUser = response.data.data
        
        // 更新store中的用户数据
        userStore.setUser({
          ...userStore.user,
          realName: updatedUser.realName,
          phone: updatedUser.phone,
          gender: updatedUser.gender,
          birthdate: updatedUser.birthdate ? new Date(updatedUser.birthdate) : null,
          bio: updatedUser.bio
        })
        
        ElMessage.success('个人信息更新成功')
      } catch (error) {
        ElMessage.error('更新失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        updating.value = false
      }
    }
  })
}

// 更新密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      updatingPassword.value = true
      
      try {
        await axios.put(`/api/users/${userStore.userId}/password`, {
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        })
        
        ElMessage.success('密码更新成功')
        
        // 重置表单
        passwordForm.currentPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error) {
        ElMessage.error('更新失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        updatingPassword.value = false
      }
    }
  })
}

// 查看报名详情
const viewRegistration = (registration) => {
  currentRegistration.value = registration
  registrationDialogVisible.value = true
}

// 判断是否可以取消报名
const canCancel = (registration) => {
  if (!registration) return false
  
  // 只有待审核和已通过的报名可以取消
  return ['PENDING', 'APPROVED'].includes(registration.status)
}

// 确认取消报名
const confirmCancelRegistration = (registration) => {
  if (!canCancel(registration)) return
  
  currentRegistration.value = registration
  cancelDialogVisible.value = true
}

// 取消报名
const handleCancelRegistration = async () => {
  if (!currentRegistration.value) return
  
  cancelling.value = true
  
  try {
    await registrationStore.cancelRegistration(currentRegistration.value.id)
    ElMessage.success('已取消报名')
    cancelDialogVisible.value = false
    registrationDialogVisible.value = false
    
    // 刷新报名列表
    await registrationStore.fetchMyRegistrations()
  } catch (error) {
    ElMessage.error('取消失败: ' + error)
  } finally {
    cancelling.value = false
  }
}

// 获取报名状态样式和文本
const getRegistrationStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

const getRegistrationStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    default: return '未知状态'
  }
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 处理头像上传
const avatarInputRef = ref(null)
const handleAvatarUpload = () => {
  avatarInputRef.value.click()
}

const uploadAvatar = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      userStore.setUser({
        ...userStore.user,
        avatar: e.target.result
      })
    }
    reader.readAsDataURL(file)
  }
}

// 重置表单
const resetForm = () => {
  userFormRef.value.resetFields()
}

const resetPasswordForm = () => {
  passwordFormRef.value.resetFields()
}

// 刷新报名列表
const refreshRegistrations = async () => {
  await registrationStore.fetchMyRegistrations()
}

const resultsLoading = ref(false)
const resultsError = ref(null)
const athleteResults = ref([])

const refreshResults = async () => {
  resultsLoading.value = true
  resultsError.value = null
  athleteResults.value = []
  
  try {
    const response = await axios.get(`/api/users/${userStore.userId}/results`)
    const resultsData = response.data.data
    
    athleteResults.value = resultsData.map(result => ({
      ...result,
      eventDate: new Date(result.eventDate).toLocaleDateString('zh-CN', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }))
  } catch (error) {
    resultsError.value = error.response?.data?.message || '获取成绩失败'
  } finally {
    resultsLoading.value = false
  }
}

const certificateDialogVisible = ref(false)
const currentCertificate = ref(null)

const viewCertificate = (result) => {
  currentCertificate.value = result
  certificateDialogVisible.value = true
}

const downloadCertificate = () => {
  // 实现下载证书的逻辑
}
</script> 