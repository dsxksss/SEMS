<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">成绩管理</h1>
      <div class="flex gap-3">
        <el-select v-model="currentEvent" filterable placeholder="选择赛事" @change="handleEventChange" class="min-w-[250px]">
          <el-option
            v-for="item in events"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-button type="success" @click="handleExport" :disabled="!currentEvent">导出成绩表</el-button>
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
          placeholder="搜索用户名/姓名"
          class="max-w-xs"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="categoryFilter" placeholder="比赛项目" clearable @change="handleFilterChange">
          <el-option label="全部项目" value="" />
          <el-option 
            v-for="category in selectedEvent?.categories || []" 
            :key="category" 
            :label="category" 
            :value="category" 
          />
        </el-select>
        
        <el-select v-model="resultFilter" placeholder="成绩状态" clearable @change="handleFilterChange">
          <el-option label="全部状态" value="" />
          <el-option label="未录入" value="NOT_RECORDED" />
          <el-option label="已录入" value="RECORDED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
      </div>
    </div>
    
    <!-- 成绩列表 -->
    <div v-if="currentEvent" class="bg-white rounded-lg shadow-sm overflow-hidden">
      <el-table
        v-loading="loading"
        :data="filteredResults"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="number" label="选手号" width="80" align="center" />
        
        <el-table-column prop="username" label="用户名" min-width="120" />
        
        <el-table-column prop="realName" label="姓名" min-width="100" />
        
        <el-table-column prop="category" label="比赛项目" min-width="120" />
        
        <el-table-column prop="result" label="成绩" min-width="150">
          <template #default="scope">
            <template v-if="scope.row.resultStatus === 'RECORDED'">
              {{ formatResult(scope.row.result, scope.row.category) }}
            </template>
            <el-tag v-else-if="scope.row.resultStatus === 'CANCELED'" type="danger">已取消</el-tag>
            <el-tag v-else type="info">未录入</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="rank" label="排名" width="80" align="center">
          <template #default="scope">
            {{ scope.row.rank || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="recordTime" label="录入时间" min-width="180">
          <template #default="scope">
            {{ scope.row.recordTime ? formatDateTime(scope.row.recordTime) : '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button 
              link 
              type="primary" 
              @click="handleRecordResult(scope.row)" 
              :disabled="scope.row.resultStatus === 'CANCELED'"
            >
              {{ scope.row.resultStatus === 'RECORDED' ? '修改成绩' : '录入成绩' }}
            </el-button>
            <el-button 
              link 
              :type="scope.row.resultStatus === 'CANCELED' ? 'success' : 'danger'" 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.resultStatus === 'CANCELED' ? '恢复参赛' : '取消参赛' }}
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
          :total="totalResults"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 空状态 -->
    <el-empty v-if="!currentEvent" description="请选择赛事查看成绩信息" />
    
    <!-- 录入成绩对话框 -->
    <el-dialog
      v-model="resultDialogVisible"
      :title="`${editingResult.resultStatus === 'RECORDED' ? '修改' : '录入'}成绩`"
      width="500px"
    >
      <div v-if="editingResult.id" class="mb-4">
        <div class="bg-gray-100 p-3 rounded">
          <p><span class="font-medium">选手：</span>{{ editingResult.realName }} ({{ editingResult.username }})</p>
          <p><span class="font-medium">项目：</span>{{ editingResult.category }}</p>
        </div>
      </div>
      
      <el-form ref="resultFormRef" :model="resultForm" :rules="resultRules" label-width="100px">
        <el-form-item 
          :label="isTimeBasedCategory(editingResult.category) ? '完成时间' : '成绩数值'" 
          prop="result"
        >
          <template v-if="isTimeBasedCategory(editingResult.category)">
            <el-time-picker
              v-model="resultForm.timeResult"
              format="HH:mm:ss"
              value-format="HH:mm:ss"
              placeholder="时:分:秒"
            />
            <el-input
              v-model="resultForm.milliseconds"
              placeholder="毫秒"
              style="width: 80px;"
              class="ml-2"
            >
              <template #append>.ms</template>
            </el-input>
          </template>
          <template v-else>
            <el-input-number
              v-model="resultForm.numericResult"
              :precision="2"
              :step="0.01"
              :min="0"
            />
            <span class="ml-2">{{ getCategoryUnit(editingResult.category) }}</span>
          </template>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input v-model="resultForm.remark" type="textarea" :rows="2" placeholder="备注信息（选填）" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="resultDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResult" :loading="submitting">确定</el-button>
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

// 成绩列表
const results = ref([])
const loading = ref(true)
const error = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalResults = ref(0)

// 搜索和筛选
const searchQuery = ref('')
const categoryFilter = ref('')
const resultFilter = ref('')

// 录入成绩对话框
const resultDialogVisible = ref(false)
const resultFormRef = ref(null)
const submitting = ref(false)
const editingResult = reactive({
  id: null,
  resultStatus: null,
  category: ''
})

const resultForm = reactive({
  timeResult: '', // 时分秒
  milliseconds: '', // 毫秒部分
  numericResult: 0, // 数值型成绩
  remark: ''
})

const resultRules = {
  result: [
    { required: true, message: '请输入成绩', trigger: 'blur' }
  ]
}

// 筛选后的成绩列表
const filteredResults = computed(() => {
  return results.value
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

// 获取成绩列表
const fetchResults = async () => {
  if (!currentEvent.value) return
  
  loading.value = true
  error.value = null
  
  try {
    const params = {
      eventId: currentEvent.value,
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value || undefined,
      category: categoryFilter.value || undefined,
      status: resultFilter.value || undefined
    }
    
    const response = await axios.get('/api/results', { params })
    
    results.value = response.data.data.list
    totalResults.value = response.data.data.total
  } catch (err) {
    error.value = err.response?.data?.message || '获取成绩列表失败'
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
    categoryFilter.value = ''
    searchQuery.value = ''
    resultFilter.value = ''
    fetchResults()
  } else {
    selectedEvent.value = null
    results.value = []
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchResults()
}

// 处理筛选
const handleFilterChange = () => {
  currentPage.value = 1
  fetchResults()
}

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchResults()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchResults()
}

// 录入成绩
const handleRecordResult = (item) => {
  // 设置当前编辑的成绩
  Object.assign(editingResult, item)
  
  // 根据项目类型初始化表单
  if (isTimeBasedCategory(item.category)) {
    // 时间型成绩
    if (item.resultStatus === 'RECORDED' && item.result) {
      // 已有成绩，解析成时分秒和毫秒
      const [time, ms] = item.result.split('.')
      resultForm.timeResult = time
      resultForm.milliseconds = ms || '0'
    } else {
      resultForm.timeResult = '00:00:00'
      resultForm.milliseconds = '0'
    }
    resultForm.numericResult = 0
  } else {
    // 数值型成绩
    resultForm.numericResult = item.resultStatus === 'RECORDED' ? parseFloat(item.result) || 0 : 0
    resultForm.timeResult = ''
    resultForm.milliseconds = ''
  }
  
  resultForm.remark = item.remark || ''
  
  resultDialogVisible.value = true
}

// 切换参赛状态
const handleToggleStatus = async (item) => {
  const action = item.resultStatus === 'CANCELED' ? '恢复' : '取消'
  const newStatus = item.resultStatus === 'CANCELED' ? 'NOT_RECORDED' : 'CANCELED'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}该选手的参赛资格吗？`,
      `${action}参赛`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await axios.put(`/api/results/${item.id}/status`, { status: newStatus })
    
    // 更新本地数据
    item.resultStatus = newStatus
    if (newStatus === 'CANCELED') {
      item.result = null
      item.rank = null
    }
    
    ElMessage.success(`已${action}选手参赛资格`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
    }
  }
}

// 提交成绩
const submitResult = async () => {
  if (!editingResult.id) return
  
  // 组合成绩值
  let resultValue
  if (isTimeBasedCategory(editingResult.category)) {
    // 时间型成绩
    const ms = resultForm.milliseconds.padStart(3, '0').substring(0, 3)
    resultValue = `${resultForm.timeResult}.${ms}`
  } else {
    // 数值型成绩
    resultValue = resultForm.numericResult.toString()
  }
  
  submitting.value = true
  
  try {
    await axios.put(`/api/results/${editingResult.id}`, {
      result: resultValue,
      remark: resultForm.remark
    })
    
    // 更新本地数据
    const result = results.value.find(r => r.id === editingResult.id)
    if (result) {
      result.result = resultValue
      result.resultStatus = 'RECORDED'
      result.remark = resultForm.remark
      result.recordTime = new Date().toISOString()
      
      // 此处需要后端重新计算排名，前端模拟更新
      updateRankings()
    }
    
    ElMessage.success('成绩录入成功')
    resultDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 更新排名（模拟）
const updateRankings = () => {
  // 按项目分组
  const categories = {}
  results.value.forEach(r => {
    if (r.resultStatus === 'RECORDED') {
      if (!categories[r.category]) {
        categories[r.category] = []
      }
      categories[r.category].push(r)
    } else {
      r.rank = null
    }
  })
  
  // 对每个项目的成绩排序并分配排名
  Object.keys(categories).forEach(category => {
    const isTimeBased = isTimeBasedCategory(category)
    
    // 根据项目类型排序
    categories[category].sort((a, b) => {
      if (isTimeBased) {
        // 时间型项目，成绩越小越好
        return parseFloat(a.result) - parseFloat(b.result)
      } else {
        // 距离型项目，成绩越大越好
        return parseFloat(b.result) - parseFloat(a.result)
      }
    })
    
    // 分配排名
    categories[category].forEach((r, index) => {
      r.rank = index + 1
    })
  })
}

// 导出成绩表
const handleExport = async () => {
  if (!currentEvent.value) return
  
  try {
    const response = await axios.get(`/api/results/export`, {
      params: { eventId: currentEvent.value },
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `成绩表-${selectedEvent.value?.name || '赛事'}.xlsx`
    link.click()
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

// 判断是否为时间型项目
const isTimeBasedCategory = (category) => {
  if (!category) return false
  const timeBasedCategories = ['短跑', '长跑', '马拉松', '接力赛']
  return timeBasedCategories.some(c => category.includes(c))
}

// 获取项目单位
const getCategoryUnit = (category) => {
  if (!category) return ''
  
  if (category.includes('跳远') || category.includes('三级跳远')) {
    return '米'
  } else if (category.includes('铅球') || category.includes('标枪') || category.includes('铁饼')) {
    return '米'
  } else if (category.includes('跳高')) {
    return '米'
  }
  
  return ''
}

// 格式化成绩显示
const formatResult = (result, category) => {
  if (!result) return '-'
  
  if (isTimeBasedCategory(category)) {
    // 时间型成绩，显示为 00:00:00.000
    return result
  } else {
    // 数值型成绩，显示为数字+单位
    return `${result} ${getCategoryUnit(category)}`
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
      categories: ['短跑', '长跑', '跳高', '跳远', '铅球'],
      status: 'COMPLETED'
    },
    {
      id: 2,
      name: '2023年秋季马拉松挑战赛',
      location: '城市环路',
      eventDate: '2023-10-20',
      categories: ['马拉松'],
      status: 'UPCOMING'
    }
  ]
  
  // 检查URL参数中是否有eventId
  const eventId = route.query.eventId
  if (eventId) {
    currentEvent.value = parseInt(eventId)
    selectedEvent.value = events.value.find(e => e.id === currentEvent.value)
    
    // 模拟成绩数据
    results.value = [
      {
        id: 1,
        eventId: currentEvent.value,
        registrationId: 1,
        userId: 2,
        username: 'athlete1',
        realName: '张三',
        number: '1001',
        category: '马拉松',
        result: '02:45:30.210',
        resultStatus: 'RECORDED',
        rank: 1,
        remark: '个人最好成绩',
        recordTime: '2023-10-20T15:30:00'
      },
      {
        id: 2,
        eventId: currentEvent.value,
        registrationId: 2,
        userId: 3,
        username: 'user1',
        realName: '李四',
        number: '1002',
        category: '马拉松',
        result: null,
        resultStatus: 'NOT_RECORDED',
        rank: null,
        remark: '',
        recordTime: null
      }
    ]
    
    totalResults.value = results.value.length
    loading.value = false
  }
})
</script> 