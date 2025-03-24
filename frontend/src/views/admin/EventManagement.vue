<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">赛事管理</h1>
      <el-button type="primary" @click="handleAddEvent">创建赛事</el-button>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="bg-white p-4 rounded-lg shadow-sm mb-6">
      <div class="flex flex-wrap gap-4">
        <el-input
          v-model="searchQuery"
          placeholder="搜索赛事名称/地点"
          class="max-w-xs"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="statusFilter" placeholder="赛事状态" clearable @change="handleFilterChange">
          <el-option label="全部状态" value="" />
          <el-option label="未开始" value="UPCOMING" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="COMPLETED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleFilterChange"
        />
      </div>
    </div>
    
    <!-- 赛事列表 -->
    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <el-table
        v-loading="loading"
        :data="filteredEvents"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="赛事名称" min-width="180" />
        
        <el-table-column prop="location" label="举办地点" min-width="150" />
        
        <el-table-column prop="eventDate" label="举办日期" min-width="120">
          <template #default="scope">
            {{ formatDate(scope.row.eventDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="registrationDeadline" label="报名截止日期" min-width="120">
          <template #default="scope">
            {{ formatDate(scope.row.registrationDeadline) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="maxParticipants" label="参赛人数限制" width="120" align="center">
          <template #default="scope">
            {{ scope.row.maxParticipants || '不限' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="registeredCount" label="已报名人数" width="120" align="center" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="success" @click="handleManageRegistrations(scope.row)">报名管理</el-button>
            <el-button link type="info" @click="handleManageResults(scope.row)" :disabled="scope.row.status !== 'ONGOING' && scope.row.status !== 'COMPLETED'">成绩管理</el-button>
            <el-button 
              link 
              :type="scope.row.status === 'CANCELED' ? 'success' : 'danger'"
              @click="handleToggleStatus(scope.row)"
              :disabled="scope.row.status === 'COMPLETED'"
            >
              {{ scope.row.status === 'CANCELED' ? '恢复' : '取消' }}
            </el-button>
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
          :total="totalEvents"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 赛事表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingEvent.id ? '编辑赛事' : '创建赛事'"
      width="650px"
    >
      <el-form
        ref="eventFormRef"
        :model="eventForm"
        :rules="eventRules"
        label-width="120px"
      >
        <el-form-item label="赛事名称" prop="name">
          <el-input v-model="eventForm.name" />
        </el-form-item>
        
        <el-form-item label="举办地点" prop="location">
          <el-input v-model="eventForm.location" />
        </el-form-item>
        
        <el-form-item label="赛事描述" prop="description">
          <el-input v-model="eventForm.description" type="textarea" :rows="4" />
        </el-form-item>
        
        <el-form-item label="举办日期" prop="eventDate" required>
          <el-date-picker
            v-model="eventForm.eventDate"
            type="date"
            placeholder="选择举办日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="报名截止日期" prop="registrationDeadline" required>
          <el-date-picker
            v-model="eventForm.registrationDeadline"
            type="date"
            placeholder="选择报名截止日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="参赛人数限制" prop="maxParticipants">
          <el-input-number v-model="eventForm.maxParticipants" :min="0" :step="10" />
          <span class="text-gray-500 ml-2">（0表示不限制）</span>
        </el-form-item>
        
        <el-form-item label="是否在首页推荐" prop="featured">
          <el-switch v-model="eventForm.featured" />
        </el-form-item>
        
        <el-form-item label="项目类别" prop="categories">
          <el-select
            v-model="eventForm.categories"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或创建项目类别"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="赛事状态" prop="status">
          <el-select v-model="eventForm.status" placeholder="请选择赛事状态">
            <el-option label="未开始" value="UPCOMING" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已结束" value="COMPLETED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEventForm" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// 赛事列表
const events = ref([])
const loading = ref(true)
const error = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalEvents = ref(0)

// 搜索和筛选
const searchQuery = ref('')
const statusFilter = ref('')
const dateRange = ref(null)

// 赛事表单
const dialogVisible = ref(false)
const eventFormRef = ref(null)
const submitting = ref(false)
const editingEvent = reactive({
  id: null
})

const eventForm = reactive({
  name: '',
  location: '',
  description: '',
  eventDate: '',
  registrationDeadline: '',
  maxParticipants: 0,
  featured: false,
  categories: [],
  status: 'UPCOMING'
})

const eventRules = {
  name: [
    { required: true, message: '请输入赛事名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符之间', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入举办地点', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入赛事描述', trigger: 'blur' }
  ],
  eventDate: [
    { required: true, message: '请选择举办日期', trigger: 'change' }
  ],
  registrationDeadline: [
    { required: true, message: '请选择报名截止日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择赛事状态', trigger: 'change' }
  ]
}

// 项目类别选项
const categoryOptions = ref([
  { value: '短跑', label: '短跑' },
  { value: '长跑', label: '长跑' },
  { value: '跳高', label: '跳高' },
  { value: '跳远', label: '跳远' },
  { value: '铅球', label: '铅球' },
  { value: '接力赛', label: '接力赛' },
  { value: '马拉松', label: '马拉松' }
])

// 筛选后的赛事列表
const filteredEvents = computed(() => {
  return events.value
})

// 获取赛事列表
const fetchEvents = async () => {
  loading.value = true
  error.value = null
  
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value || undefined,
      status: statusFilter.value || undefined
    }
    
    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await axios.get('/api/events', { params })
    
    events.value = response.data.data.list
    totalEvents.value = response.data.data.total
  } catch (err) {
    error.value = err.response?.data?.message || '获取赛事列表失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchEvents()
}

// 处理筛选
const handleFilterChange = () => {
  currentPage.value = 1
  fetchEvents()
}

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchEvents()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchEvents()
}

// 添加赛事
const handleAddEvent = () => {
  // 重置表单
  Object.assign(eventForm, {
    name: '',
    location: '',
    description: '',
    eventDate: '',
    registrationDeadline: '',
    maxParticipants: 0,
    featured: false,
    categories: [],
    status: 'UPCOMING'
  })
  
  editingEvent.id = null
  dialogVisible.value = true
}

// 编辑赛事
const handleEdit = (event) => {
  // 填充表单数据
  Object.assign(eventForm, {
    name: event.name,
    location: event.location,
    description: event.description,
    eventDate: event.eventDate,
    registrationDeadline: event.registrationDeadline,
    maxParticipants: event.maxParticipants,
    featured: event.featured,
    categories: event.categories || [],
    status: event.status
  })
  
  editingEvent.id = event.id
  dialogVisible.value = true
}

// 切换赛事状态
const handleToggleStatus = async (event) => {
  if (event.status === 'COMPLETED') {
    ElMessage.warning('已结束的赛事不能修改状态')
    return
  }
  
  try {
    const newStatus = event.status === 'CANCELED' ? 'UPCOMING' : 'CANCELED'
    const action = event.status === 'CANCELED' ? '恢复' : '取消'
    
    await ElMessageBox.confirm(
      `确定要${action}赛事 "${event.name}" 吗？`,
      `${action}赛事`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await axios.put(`/api/events/${event.id}/status`, { status: newStatus })
    
    // 更新本地数据
    event.status = newStatus
    
    ElMessage.success(`${action}赛事成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
    }
  }
}

// 管理报名
const handleManageRegistrations = (event) => {
  router.push(`/admin/registrations?eventId=${event.id}`)
}

// 管理成绩
const handleManageResults = (event) => {
  router.push(`/admin/results?eventId=${event.id}`)
}

// 提交赛事表单
const submitEventForm = async () => {
  if (!eventFormRef.value) return
  
  await eventFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      try {
        let response
        
        if (editingEvent.id) {
          // 编辑赛事
          response = await axios.put(`/api/events/${editingEvent.id}`, eventForm)
          
          // 更新本地数据
          const index = events.value.findIndex(e => e.id === editingEvent.id)
          if (index !== -1) {
            events.value[index] = response.data.data
          }
          
          ElMessage.success('更新赛事成功')
        } else {
          // 添加赛事
          response = await axios.post('/api/events', eventForm)
          
          // 添加到列表头部
          events.value.unshift(response.data.data)
          totalEvents.value++
          
          ElMessage.success('创建赛事成功')
        }
        
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        submitting.value = false
      }
    }
  })
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

// 初始化
onMounted(() => {
  fetchEvents()
  
  // 模拟数据
  events.value = [
    {
      id: 1,
      name: '2023年春季校园运动会',
      location: '中央体育场',
      description: '校园传统体育盛事，包含田径、球类等多种项目',
      eventDate: '2023-04-15',
      registrationDeadline: '2023-04-01',
      maxParticipants: 500,
      registeredCount: 320,
      featured: true,
      categories: ['短跑', '长跑', '跳高', '跳远', '铅球'],
      status: 'COMPLETED'
    },
    {
      id: 2,
      name: '2023年秋季马拉松挑战赛',
      location: '城市环路',
      description: '城市环城马拉松比赛，全程42.195公里',
      eventDate: '2023-10-20',
      registrationDeadline: '2023-10-01',
      maxParticipants: 1000,
      registeredCount: 578,
      featured: true,
      categories: ['马拉松'],
      status: 'UPCOMING'
    },
    {
      id: 3,
      name: '2023年暑期篮球联赛',
      location: '市体育馆',
      description: '暑期篮球爱好者联赛，分团体和个人组',
      eventDate: '2023-07-10',
      registrationDeadline: '2023-06-30',
      maxParticipants: 200,
      registeredCount: 185,
      featured: false,
      categories: ['篮球'],
      status: 'CANCELED'
    }
  ]
  
  totalEvents.value = events.value.length
  loading.value = false
})
</script> 