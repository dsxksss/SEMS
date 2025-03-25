<template>
  <div class="container mx-auto py-8 px-4">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">赛事管理</h1>
      <el-button type="primary" @click="showAddDialog">添加赛事</el-button>
    </div>

    <!-- 筛选区域 -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <el-form :inline="true" :model="searchForm" class="flex flex-wrap items-center">
        <el-form-item label="赛事名称">
          <el-input v-model="searchForm.name" placeholder="赛事名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="赛事状态" clearable>
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="报名中" value="REGISTERING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已结束" value="FINISHED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 赛事列表 -->
    <el-table :data="events" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="赛事名称" min-width="180" />
      <el-table-column prop="location" label="地点" width="150" />
      <el-table-column label="时间" width="280">
        <template #default="scope">
          {{ formatDate(scope.row.startTime) }} 至 {{ formatDate(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="maxParticipants" label="人数限制" width="100" align="center" />
      <el-table-column prop="registeredCount" label="已报名" width="100" align="center" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ translateStatus(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editEvent(scope.row)">编辑</el-button>
          <el-button type="success" size="small" @click="manageRegistrations(scope.row)">报名管理</el-button>
          <el-button type="danger" size="small" @click="confirmDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="flex justify-end mt-4">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑赛事对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑赛事' : '添加赛事'" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="赛事名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入赛事名称" />
        </el-form-item>
        <el-form-item label="赛事描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入赛事描述" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入赛事地点" />
        </el-form-item>
        <el-form-item label="赛事时间" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="报名时间" prop="registrationTimeRange">
          <el-date-picker
            v-model="form.registrationTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="人数限制" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="赛事状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="报名中" value="REGISTERING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已结束" value="FINISHED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { eventApi } from '@/api'

const router = useRouter()

// 数据列表
const events = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  description: '',
  location: '',
  timeRange: [],
  registrationTimeRange: [],
  maxParticipants: 100,
  status: 'NOT_STARTED'
})

// 监听日期范围变化
const watchDateRange = computed(() => {
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startDate = formatDateForSearch(dateRange.value[0])
    searchForm.endDate = formatDateForSearch(dateRange.value[1])
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
  return dateRange.value
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入赛事名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度为2-100个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入赛事描述', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入赛事地点', trigger: 'blur' }
  ],
  timeRange: [
    { required: true, message: '请选择赛事时间', trigger: 'change' }
  ],
  registrationTimeRange: [
    { required: true, message: '请选择报名时间', trigger: 'change' }
  ],
  maxParticipants: [
    { required: true, message: '请输入人数限制', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择赛事状态', trigger: 'change' }
  ]
}

// 加载赛事列表
const loadEvents = async () => {
  loading.value = true
  try {
    const res = await eventApi.getEventList({
      page: currentPage.value - 1,
      size: pageSize.value,
      name: searchForm.name,
      status: searchForm.status,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    })
    events.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取赛事列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.description = ''
  form.location = ''
  form.timeRange = []
  form.registrationTimeRange = []
  form.maxParticipants = 100
  form.status = 'NOT_STARTED'
  dialogVisible.value = true
}

// 编辑赛事
const editEvent = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.description = row.description
  form.location = row.location
  form.timeRange = [row.startTime, row.endTime]
  form.registrationTimeRange = [row.registrationStartTime, row.registrationEndTime]
  form.maxParticipants = row.maxParticipants
  form.status = row.status
  dialogVisible.value = true
}

// 报名管理
const manageRegistrations = (row) => {
  router.push({ name: 'AdminRegistrationManagement', params: { id: row.id } })
}

// 确认删除
const confirmDelete = (row) => {
  ElMessageBox.confirm('确定要删除该赛事吗？相关的报名信息也将被删除！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteEvent(row.id)
  }).catch(() => {})
}

// 删除赛事
const deleteEvent = async (id) => {
  try {
    await eventApi.deleteEvent(id)
    ElMessage.success('删除成功')
    loadEvents()
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const eventData = {
        name: form.name,
        description: form.description,
        location: form.location,
        startTime: form.timeRange[0],
        endTime: form.timeRange[1],
        registrationStartTime: form.registrationTimeRange[0],
        registrationEndTime: form.registrationTimeRange[1],
        maxParticipants: form.maxParticipants,
        status: form.status
      }
      
      if (isEdit.value) {
        await eventApi.updateEvent(form.id, eventData)
        ElMessage.success('更新成功')
      } else {
        await eventApi.createEvent(eventData)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadEvents()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
      console.error(error)
    }
  })
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadEvents()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = ''
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  currentPage.value = 1
  loadEvents()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  loadEvents()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadEvents()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期为搜索参数
const formatDateForSearch = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 获取状态对应的Tag类型
const getStatusType = (status) => {
  const statusMap = {
    'NOT_STARTED': 'info',
    'REGISTERING': 'warning',
    'IN_PROGRESS': 'success',
    'FINISHED': '',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || ''
}

// 翻译状态枚举
const translateStatus = (status) => {
  const statusMap = {
    'NOT_STARTED': '未开始',
    'REGISTERING': '报名中',
    'IN_PROGRESS': '进行中',
    'FINISHED': '已结束',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 初始化
onMounted(() => {
  loadEvents()
})
</script> 