<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">报名管理</h1>
      <div class="flex gap-3">
        <el-select v-model="currentEvent" filterable placeholder="选择赛事" @change="handleEventChange" class="min-w-[250px]">
          <el-option
            v-for="item in events"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-button type="success" @click="handleExport" :disabled="!currentEvent">导出报名信息</el-button>
      </div>
    </div>
    
    <!-- 赛事信息 -->
    <div v-if="selectedEvent" class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <p class="text-gray-500 mb-1">赛事名称</p>
          <p class="font-medium">{{ selectedEvent.name }}</p>
        </div>
        <div>
          <p class="text-gray-500 mb-1">举办地点</p>
          <p class="font-medium">{{ selectedEvent.location }}</p>
        </div>
        <div>
          <p class="text-gray-500 mb-1">举办日期</p>
          <p class="font-medium">{{ formatDate(selectedEvent.eventDate) }}</p>
        </div>
        <div>
          <p class="text-gray-500 mb-1">报名截止日期</p>
          <p class="font-medium">{{ formatDate(selectedEvent.registrationDeadline) }}</p>
        </div>
        <div>
          <p class="text-gray-500 mb-1">已报名/限制人数</p>
          <p class="font-medium">{{ selectedEvent.registeredCount || 0 }}/{{ selectedEvent.maxParticipants || '不限' }}</p>
        </div>
        <div>
          <p class="text-gray-500 mb-1">状态</p>
          <el-tag :type="getStatusType(selectedEvent.status)">
            {{ getStatusText(selectedEvent.status) }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <!-- 搜索和筛选 -->
    <div v-if="currentEvent" class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="flex flex-wrap gap-4">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名/姓名/联系方式"
          class="max-w-xs"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="statusFilter" placeholder="报名状态" clearable @change="handleFilterChange">
          <el-option label="全部状态" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
        
        <el-select v-model="categoryFilter" placeholder="报名项目" clearable @change="handleFilterChange">
          <el-option label="全部项目" value="" />
          <el-option 
            v-for="category in selectedEvent?.categories || []" 
            :key="category" 
            :label="category" 
            :value="category" 
          />
        </el-select>
      </div>
    </div>
    
    <!-- 报名列表 -->
    <div v-if="currentEvent" class="bg-white rounded-lg shadow-sm overflow-hidden">
      <el-table
        v-loading="loading"
        :data="filteredRegistrations"
        stripe
        style="width: 100%"
      >
        <el-table-column type="expand">
          <template #default="props">
            <div class="p-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                <div>
                  <h4 class="font-medium text-gray-500 mb-2">报名信息</h4>
                  <p><span class="text-gray-500">报名项目：</span>{{ props.row.category }}</p>
                  <p><span class="text-gray-500">报名时间：</span>{{ formatDateTime(props.row.createTime) }}</p>
                  <p v-if="props.row.reviewTime"><span class="text-gray-500">审核时间：</span>{{ formatDateTime(props.row.reviewTime) }}</p>
                  <p v-if="props.row.reviewComment"><span class="text-gray-500">审核备注：</span>{{ props.row.reviewComment }}</p>
                </div>
                <div>
                  <h4 class="font-medium text-gray-500 mb-2">运动员信息</h4>
                  <p><span class="text-gray-500">证件类型：</span>{{ getIdTypeText(props.row.idType) }}</p>
                  <p><span class="text-gray-500">证件号码：</span>{{ props.row.idNumber }}</p>
                  <p><span class="text-gray-500">性别：</span>{{ props.row.gender === 'M' ? '男' : '女' }}</p>
                  <p><span class="text-gray-500">年龄：</span>{{ props.row.age }}</p>
                  <p><span class="text-gray-500">紧急联系人：</span>{{ props.row.emergencyContact }}</p>
                  <p><span class="text-gray-500">紧急联系电话：</span>{{ props.row.emergencyPhone }}</p>
                </div>
              </div>
              <div v-if="props.row.medicalInfo || props.row.note">
                <h4 class="font-medium text-gray-500 mb-2">附加信息</h4>
                <p v-if="props.row.medicalInfo"><span class="text-gray-500">医疗信息：</span>{{ props.row.medicalInfo }}</p>
                <p v-if="props.row.note"><span class="text-gray-500">备注：</span>{{ props.row.note }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="username" label="用户名" min-width="120" />
        
        <el-table-column prop="realName" label="姓名" min-width="100" />
        
        <el-table-column prop="phone" label="联系电话" min-width="120" />
        
        <el-table-column prop="category" label="报名项目" min-width="120" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getRegistrationStatusType(scope.row.status)">
              {{ getRegistrationStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="报名时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <div v-if="scope.row.status === 'PENDING'">
              <el-button link type="success" @click="handleApprove(scope.row)">通过</el-button>
              <el-button link type="danger" @click="handleReject(scope.row)">拒绝</el-button>
            </div>
            <div v-else-if="scope.row.status === 'APPROVED' || scope.row.status === 'REJECTED'">
              <el-button link type="warning" @click="handleResetStatus(scope.row)">重置状态</el-button>
              <el-button link type="info" @click="handleAddResult(scope.row)" :disabled="scope.row.status !== 'APPROVED'">记录成绩</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="flex justify-end p-4">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalRegistrations"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 空状态 -->
    <el-empty v-if="!currentEvent" description="请选择赛事查看报名信息" />
    
    <!-- 操作对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewAction === 'approve' ? '通过报名' : '拒绝报名'"
      width="500px"
    >
      <el-form ref="reviewFormRef" :model="reviewForm" label-width="100px">
        <el-form-item label="审核备注">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请输入审核备注（选填）" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// 赛事列表和当前选中的赛事
const events = ref([])
const currentEvent = ref(null)
const selectedEvent = ref(null)

// 报名列表
const registrations = ref([])
const loading = ref(true)
const error = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalRegistrations = ref(0)

// 搜索和筛选
const searchQuery = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')

// 审核对话框
const reviewDialogVisible = ref(false)
const reviewAction = ref('approve') // 'approve' 或 'reject'
const reviewingRegistration = reactive({
  id: null
})
const reviewForm = reactive({
  comment: ''
})
const submitting = ref(false)

// 筛选后的报名列表
const filteredRegistrations = computed(() => {
  return registrations.value
})

// 获取赛事列表
const fetchEvents = async () => {
  try {
    const response = await axios.get('/api/events')
    events.value = response.data.data.list || []
    
    // 如果URL中有eventId参数，则自动选中对应赛事
    const eventId = route.query.eventId
    if (eventId) {
      currentEvent.value = parseInt(eventId)
      handleEventChange()
    }
  } catch (err) {
    ElMessage.error('获取赛事列表失败')
  }
}

// 获取赛事详情
const fetchEventDetail = async (eventId) => {
  try {
    const response = await axios.get(`/api/events/${eventId}`)
    selectedEvent.value = response.data.data
  } catch (err) {
    ElMessage.error('获取赛事详情失败')
  }
}

// 获取报名列表
const fetchRegistrations = async () => {
  if (!currentEvent.value) return
  
  loading.value = true
  error.value = null
  
  try {
    const params = {
      eventId: currentEvent.value,
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value || undefined,
      status: statusFilter.value || undefined,
      category: categoryFilter.value || undefined
    }
    
    const response = await axios.get('/api/registrations', { params })
    
    registrations.value = response.data.data.list
    totalRegistrations.value = response.data.data.total
  } catch (err) {
    error.value = err.response?.data?.message || '获取报名列表失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 处理赛事变更
const handleEventChange = () => {
  if (currentEvent.value) {
    fetchEventDetail(currentEvent.value)
    currentPage.value = 1
    statusFilter.value = ''
    searchQuery.value = ''
    categoryFilter.value = ''
    fetchRegistrations()
  } else {
    selectedEvent.value = null
    registrations.value = []
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRegistrations()
}

// 处理筛选
const handleFilterChange = () => {
  currentPage.value = 1
  fetchRegistrations()
}

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchRegistrations()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchRegistrations()
}

// 通过报名
const handleApprove = (registration) => {
  reviewAction.value = 'approve'
  reviewingRegistration.id = registration.id
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

// 拒绝报名
const handleReject = (registration) => {
  reviewAction.value = 'reject'
  reviewingRegistration.id = registration.id
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

// 重置报名状态
const handleResetStatus = async (registration) => {
  try {
    await ElMessageBox.confirm(
      '确定要将此报名重置为待审核状态吗？',
      '重置状态',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await axios.put(`/api/registrations/${registration.id}/status`, { 
      status: 'PENDING',
      comment: '状态已重置'
    })
    
    // 更新本地数据
    registration.status = 'PENDING'
    registration.reviewComment = '状态已重置'
    registration.reviewTime = new Date().toISOString()
    
    ElMessage.success('报名状态已重置')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
    }
  }
}

// 记录成绩
const handleAddResult = (registration) => {
  router.push(`/admin/results?registrationId=${registration.id}`)
}

// 提交审核
const submitReview = async () => {
  if (!reviewingRegistration.id) return
  
  submitting.value = true
  
  try {
    await axios.put(`/api/registrations/${reviewingRegistration.id}/status`, {
      status: reviewAction.value === 'approve' ? 'APPROVED' : 'REJECTED',
      comment: reviewForm.comment
    })
    
    // 更新本地数据
    const registration = registrations.value.find(r => r.id === reviewingRegistration.id)
    if (registration) {
      registration.status = reviewAction.value === 'approve' ? 'APPROVED' : 'REJECTED'
      registration.reviewComment = reviewForm.comment
      registration.reviewTime = new Date().toISOString()
    }
    
    ElMessage.success(reviewAction.value === 'approve' ? '已通过报名' : '已拒绝报名')
    reviewDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 导出报名信息
const handleExport = async () => {
  if (!currentEvent.value) return
  
  try {
    const response = await axios.get(`/api/registrations/export`, {
      params: { eventId: currentEvent.value },
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `报名信息-${selectedEvent.value?.name || '赛事'}.xlsx`
    link.click()
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

// 获取状态类型和文本
const getStatusType = (status) => {
  switch (status) {
    case 'UPCOMING': return 'info'
    case 'ONGOING': return 'success'
    case 'COMPLETED': return ''
    case 'CANCELED': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'UPCOMING': return '未开始'
    case 'ONGOING': return '进行中'
    case 'COMPLETED': return '已结束'
    case 'CANCELED': return '已取消'
    default: return '未知'
  }
}

// 获取报名状态类型和文本
const getRegistrationStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'info'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'CANCELED': return 'warning'
    default: return 'info'
  }
}

const getRegistrationStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    case 'CANCELED': return '已取消'
    default: return '未知'
  }
}

// 获取证件类型文本
const getIdTypeText = (idType) => {
  switch (idType) {
    case 'ID_CARD': return '身份证'
    case 'PASSPORT': return '护照'
    case 'STUDENT_CARD': return '学生证'
    default: return '其他'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 监听路由变化，处理eventId参数
watch(() => route.query.eventId, (newEventId) => {
  if (newEventId) {
    currentEvent.value = parseInt(newEventId)
    handleEventChange()
  }
})

// 初始化
onMounted(() => {
  fetchEvents()
  
  // 模拟赛事数据
  events.value = [
    {
      id: 1,
      name: '2023年春季校园运动会',
      location: '中央体育场',
      eventDate: '2023-04-15',
      registrationDeadline: '2023-04-01',
      maxParticipants: 500,
      registeredCount: 320,
      categories: ['短跑', '长跑', '跳高', '跳远', '铅球'],
      status: 'COMPLETED'
    },
    {
      id: 2,
      name: '2023年秋季马拉松挑战赛',
      location: '城市环路',
      eventDate: '2023-10-20',
      registrationDeadline: '2023-10-01',
      maxParticipants: 1000,
      registeredCount: 578,
      categories: ['马拉松'],
      status: 'UPCOMING'
    }
  ]
  
  // 检查URL参数中是否有eventId
  const eventId = route.query.eventId
  if (eventId) {
    currentEvent.value = parseInt(eventId)
    selectedEvent.value = events.value.find(e => e.id === currentEvent.value)
    
    // 模拟报名数据
    registrations.value = [
      {
        id: 1,
        eventId: currentEvent.value,
        userId: 2,
        username: 'athlete1',
        realName: '张三',
        phone: '13800138001',
        idType: 'ID_CARD',
        idNumber: '4****************X',
        gender: 'M',
        age: 25,
        category: '马拉松',
        emergencyContact: '李四',
        emergencyPhone: '13800138002',
        medicalInfo: '无特殊医疗情况',
        note: '已参加过多次马拉松比赛',
        status: 'APPROVED',
        createTime: '2023-09-15T10:30:00',
        reviewTime: '2023-09-16T14:20:00',
        reviewComment: '资料完整，符合参赛条件'
      },
      {
        id: 2,
        eventId: currentEvent.value,
        userId: 3,
        username: 'user1',
        realName: '李四',
        phone: '13800138002',
        idType: 'ID_CARD',
        idNumber: '3****************1',
        gender: 'M',
        age: 30,
        category: '马拉松',
        emergencyContact: '王五',
        emergencyPhone: '13800138003',
        medicalInfo: '',
        note: '',
        status: 'PENDING',
        createTime: '2023-09-18T09:15:00'
      }
    ]
    
    totalRegistrations.value = registrations.value.length
    loading.value = false
  }
})
</script> 