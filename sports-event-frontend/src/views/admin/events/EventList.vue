<template>
  <div class="event-list">
    <div class="filter-container">
      <div class="filter-header">
        <h3>赛事过滤</h3>
        <el-button type="primary" @click="showCreateEventDialog">创建赛事</el-button>
      </div>
      <el-form :inline="true" :model="filterForm" class="form-inline">
        <el-form-item label="赛事名称">
          <el-input v-model="filterForm.name" placeholder="输入赛事名称搜索" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filterForm.categoryId" placeholder="选择分类" clearable>
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="未开始" value="UPCOMING" />
            <el-option label="报名中" value="REGISTRATION" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已结束" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchEvents">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3>赛事列表</h3>
      </div>

      <el-table
        v-loading="loading"
        :data="eventList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="赛事名称" width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="日期" width="220">
          <template #default="scope">
            {{ scope.row.startDate }} 至 {{ scope.row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="150" show-overflow-tooltip />
        <el-table-column prop="registrationCount" label="报名人数" width="100" />
        <el-table-column prop="maxParticipants" label="最大人数" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleView(scope.row)"
            >详情</el-button>
            <el-button
              v-if="canEdit(scope.row)"
              size="small"
              type="success"
              @click="handleEdit(scope.row)"
            >编辑</el-button>
            <el-button
              size="small"
              type="warning"
              @click="$router.push(`/admin/events/${scope.row.id}/participants`)"
            >参与者</el-button>
            <el-dropdown split-button type="info" size="small" @command="(command) => handleCommand(command, scope.row)">
              更多操作
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="scope.row.status === 'UPCOMING'" command="start-registration">开始报名</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 'REGISTRATION'" command="end-registration">结束报名</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 'REGISTRATION' || scope.row.status === 'UPCOMING'" command="start-event">开始比赛</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 'ONGOING'" command="end-event">结束比赛</el-dropdown-item>
                  <el-dropdown-item v-if="canCancel(scope.row)" command="cancel-event">取消赛事</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      title="赛事详情"
      v-model="detailDialogVisible"
      width="800px"
    >
      <div v-if="currentEvent" class="event-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="赛事名称" :span="2">{{ currentEvent.name }}</el-descriptions-item>
          <el-descriptions-item label="赛事分类">{{ currentEvent.category }}</el-descriptions-item>
          <el-descriptions-item label="赛事状态">
            <el-tag :type="getStatusType(currentEvent.status)">
              {{ formatStatus(currentEvent.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ currentEvent.startDate }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ currentEvent.endDate }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentEvent.location }}</el-descriptions-item>
          <el-descriptions-item label="主办方">{{ currentEvent.organizer }}</el-descriptions-item>
          <el-descriptions-item label="报名开始日期">{{ currentEvent.registrationStartDate }}</el-descriptions-item>
          <el-descriptions-item label="报名截止日期">{{ currentEvent.registrationEndDate }}</el-descriptions-item>
          <el-descriptions-item label="已报名人数">{{ currentEvent.registrationCount }}</el-descriptions-item>
          <el-descriptions-item label="最大人数">{{ currentEvent.maxParticipants }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentEvent.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="创建者">{{ currentEvent.createdBy }}</el-descriptions-item>
          <el-descriptions-item label="赛事描述" :span="2">
            <div class="event-description">{{ currentEvent.description }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="canEdit(currentEvent)"
            type="primary"
            @click="handleEdit(currentEvent)"
          >编辑赛事</el-button>
          <el-button
            type="info"
            @click="$router.push(`/admin/events/${currentEvent.id}/participants`)"
          >查看参与者</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 状态变更确认弹窗 -->
    <el-dialog
      :title="statusChangeTitle"
      v-model="statusDialogVisible"
      width="500px"
    >
      <div class="status-change-content">
        <p>{{ statusChangeMessage }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmStatusChange">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 创建赛事弹窗 -->
    <el-dialog
      title="创建赛事"
      v-model="createDialogVisible"
      width="800px"
    >
      <div class="event-form-container">
        <el-form 
          ref="eventFormRef"
          :model="eventForm"
          :rules="eventFormRules"
          label-width="120px"
          label-position="right"
          class="event-form"
        >
          <el-form-item label="赛事名称" prop="name">
            <el-input v-model="eventForm.name" placeholder="请输入赛事名称" />
          </el-form-item>
          
          <el-form-item label="赛事分类" prop="categoryId">
            <el-select v-model="eventForm.categoryId" placeholder="请选择赛事分类" style="width: 100%">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="赛事日期" prop="eventDates">
            <el-date-picker
              v-model="eventForm.eventDates"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="报名时间" prop="registrationDates">
            <el-date-picker
              v-model="eventForm.registrationDates"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="比赛地点" prop="location">
            <el-input v-model="eventForm.location" placeholder="请输入比赛地点" />
          </el-form-item>
          
          <el-form-item label="主办方" prop="organizer">
            <el-input v-model="eventForm.organizer" placeholder="请输入主办方" />
          </el-form-item>
          
          <el-form-item label="最大参与人数" prop="maxParticipants">
            <el-input-number 
              v-model="eventForm.maxParticipants" 
              :min="1" 
              :max="10000" 
              :step="1"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="赛事描述" prop="description">
            <el-input
              v-model="eventForm.description"
              type="textarea"
              :rows="6"
              placeholder="请输入赛事详细描述，包括赛事规则、奖励等信息"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button @click="resetEventForm">重置</el-button>
          <el-button type="primary" @click="submitEventForm" :loading="submitting">创建赛事</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';
import { eventsAPI, categoryAPI } from '../../../api';
import dayjs from 'dayjs';

interface EventCategory {
  id: number;
  name: string;
}

interface Event {
  id: number;
  name: string;
  category: string;
  categoryId: number;
  startDate: string;
  endDate: string;
  registrationStartDate: string;
  registrationEndDate: string;
  location: string;
  organizer: string;
  description: string;
  registrationCount: number;
  maxParticipants: number;
  status: string;
  createdAt: string;
  createdBy: string;
}

const router = useRouter();

// 过滤表单
const filterForm = reactive({
  name: '',
  categoryId: null as number | null,
  status: ''
});

// 赛事列表数据
const eventList = ref<Event[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// 分类数据
const categories = ref<EventCategory[]>([]);

// 详情弹窗
const detailDialogVisible = ref(false);
const currentEvent = ref<Event | null>(null);

// 状态变更弹窗
const statusDialogVisible = ref(false);
const statusChangeEvent = ref<Event | null>(null);
const targetStatus = ref('');
const statusChangeTitle = computed(() => {
  return `修改赛事状态为 ${formatStatus(targetStatus.value)}`;
});
const statusChangeMessage = computed(() => {
  if (!statusChangeEvent.value) return '';
  
  switch (targetStatus.value) {
    case 'REGISTRATION':
      return `确定要开放赛事 "${statusChangeEvent.value.name}" 的报名吗？开放后，用户可以进行报名。`;
    case 'UPCOMING':
      return `确定要关闭赛事 "${statusChangeEvent.value.name}" 的报名吗？关闭后，用户将无法报名。`;
    case 'ONGOING':
      return `确定要将赛事 "${statusChangeEvent.value.name}" 标记为进行中吗？这将关闭报名通道。`;
    case 'COMPLETED':
      return `确定要将赛事 "${statusChangeEvent.value.name}" 标记为已结束吗？`;
    case 'CANCELLED':
      return `确定要取消赛事 "${statusChangeEvent.value.name}" 吗？取消后无法恢复。`;
    default:
      return `确定要修改赛事 "${statusChangeEvent.value.name}" 的状态吗？`;
  }
});

// 创建赛事相关
const createDialogVisible = ref(false);
const eventFormRef = ref<FormInstance>();
const submitting = ref(false);

// 赛事表单
const eventForm = reactive({
  name: '',
  categoryId: null as number | null,
  eventDates: [] as string[],
  registrationDates: [] as string[],
  location: '',
  organizer: '',
  maxParticipants: 100,
  description: ''
});

// 表单验证规则
const eventFormRules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入赛事名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应为2到50个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择赛事分类', trigger: 'change' }
  ],
  eventDates: [
    { required: true, message: '请选择赛事日期', trigger: 'change' }
  ],
  registrationDates: [
    { required: true, message: '请选择报名时间', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入比赛地点', trigger: 'blur' }
  ],
  organizer: [
    { required: true, message: '请输入主办方', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入赛事描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度应为10到2000个字符', trigger: 'blur' }
  ]
});

// 显示创建赛事弹窗
const showCreateEventDialog = () => {
  resetEventForm();
  createDialogVisible.value = true;
};

// 重置创建赛事表单
const resetEventForm = () => {
  if (eventFormRef.value) {
    eventFormRef.value.resetFields();
  } else {
    eventForm.name = '';
    eventForm.categoryId = null;
    eventForm.eventDates = [];
    eventForm.registrationDates = [];
    eventForm.location = '';
    eventForm.organizer = '';
    eventForm.maxParticipants = 100;
    eventForm.description = '';
  }
};

// 提交创建赛事表单
const submitEventForm = async () => {
  if (!eventFormRef.value) return;

  await eventFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 处理日期
        const [startDate, endDate] = eventForm.eventDates;
        const [registrationStartDate, registrationEndDate] = eventForm.registrationDates;

        // 构建提交的数据
        const eventData = {
          name: eventForm.name,
          category: {
            id: eventForm.categoryId
          },
          startTime: dayjs(startDate).format('YYYY-MM-DD HH:mm:ss'),
          endTime: dayjs(endDate).format('YYYY-MM-DD HH:mm:ss'),
          registrationDeadline: dayjs(registrationEndDate).format('YYYY-MM-DD HH:mm:ss'),
          location: eventForm.location,
          organizer: eventForm.organizer,
          maxParticipants: eventForm.maxParticipants,
          description: eventForm.description,
          status: 'UPCOMING'  // 默认状态为未开始
        };

        // 调用API创建赛事
        await eventsAPI.createEvent(eventData);
        ElMessage.success('赛事创建成功');
        createDialogVisible.value = false;
        
        // 刷新赛事列表
        fetchEventList();
      } catch (error) {
        console.error('创建赛事失败', error);
        ElMessage.error('创建赛事失败，请重试');
      } finally {
        submitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确');
      return false;
    }
  });
};

// 加载分类列表
const loadCategories = async () => {
  try {
    // 调用API获取分类列表
    const response = await categoryAPI.getAllCategories();
    categories.value = response;
  } catch (error) {
    console.error('获取分类列表失败', error);
    ElMessage.error('获取分类列表失败');
  }
};

// 获取赛事列表
const fetchEventList = async () => {
  loading.value = true;
  try {
    // 调用API获取所有赛事
    const events = await eventsAPI.getAllEvents();
    
    // 筛选处理
    let filteredEvents = events.filter(event => {
      let matches = true;
      if (filterForm.name && !event.name.toLowerCase().includes(filterForm.name.toLowerCase())) {
        matches = false;
      }
      
      if (filterForm.categoryId && event.category.id !== filterForm.categoryId) {
        matches = false;
      }
      
      if (filterForm.status && event.status !== filterForm.status) {
        matches = false;
      }
      
      return matches;
    });
    
    // 计算总数
    total.value = filteredEvents.length;
    
    // 分页处理
    const startIndex = (currentPage.value - 1) * pageSize.value;
    const endIndex = startIndex + pageSize.value;
    
    // 格式化数据
    eventList.value = filteredEvents.slice(startIndex, endIndex).map(event => ({
      id: event.id,
      name: event.name,
      category: event.category.name,
      categoryId: event.category.id,
      startDate: dayjs(event.startTime).format('YYYY-MM-DD'),
      endDate: dayjs(event.endTime).format('YYYY-MM-DD'),
      registrationStartDate: dayjs(event.registrationDeadline).subtract(14, 'day').format('YYYY-MM-DD'), // 假设报名开始时间为截止前14天
      registrationEndDate: dayjs(event.registrationDeadline).format('YYYY-MM-DD'),
      location: event.location,
      organizer: '赛事组织者', // 假设API没有提供组织者信息
      description: event.description,
      registrationCount: 0, // 假设API没有提供报名数量信息
      maxParticipants: event.maxParticipants,
      status: event.status,
      createdAt: dayjs(event.createdAt).format('YYYY-MM-DD HH:mm:ss'),
      createdBy: 'admin' // 假设API没有提供创建者信息
    }));
  } catch (error) {
    console.error('获取赛事列表失败', error);
    ElMessage.error('获取赛事列表失败，请刷新重试');
  } finally {
    loading.value = false;
  }
};

// 搜索赛事
const searchEvents = () => {
  currentPage.value = 1;
  fetchEventList();
};

// 重置表单
const resetForm = () => {
  filterForm.name = '';
  filterForm.categoryId = null;
  filterForm.status = '';
  searchEvents();
};

// 格式化状态显示
const formatStatus = (status: string) => {
  switch (status) {
    case 'UPCOMING':
      return '未开始';
    case 'REGISTRATION':
      return '报名中';
    case 'ONGOING':
      return '进行中';
    case 'COMPLETED':
      return '已结束';
    case 'CANCELLED':
      return '已取消';
    default:
      return status;
  }
};

// 获取状态对应的标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'UPCOMING':
      return 'info';
    case 'REGISTRATION':
      return 'warning';
    case 'ONGOING':
      return 'success';
    case 'COMPLETED':
      return 'danger';
    case 'CANCELLED':
      return '';
    default:
      return 'info';
  }
};

// 查看赛事详情
const handleView = (row: Event) => {
  currentEvent.value = row;
  detailDialogVisible.value = true;
};

// 编辑赛事
const handleEdit = (row: Event) => {
  router.push(`/admin/events/edit/${row.id}`);
};

// 判断赛事是否可编辑
const canEdit = (event: Event) => {
  return ['UPCOMING', 'REGISTRATION'].includes(event.status);
};

// 判断赛事是否可取消
const canCancel = (event: Event) => {
  return ['UPCOMING', 'REGISTRATION'].includes(event.status);
};

// 修改赛事状态
const handleChangeStatus = (row: Event, status: string) => {
  statusChangeEvent.value = row;
  targetStatus.value = status;
  statusDialogVisible.value = true;
};

// 确认修改状态
const confirmStatusChange = async () => {
  if (!statusChangeEvent.value) return;
  
  try {
    // 根据不同的状态调用不同的API
    if (targetStatus.value === 'CANCELLED') {
      // 取消赛事
      await eventsAPI.cancelEvent(statusChangeEvent.value.id);
    } else {
      // 更新赛事状态 - 使用专用的状态更新API
      await eventsAPI.updateEventStatus(statusChangeEvent.value.id, targetStatus.value);
    }
    
    ElMessage.success('赛事状态更新成功');
    statusDialogVisible.value = false;
    
    // 刷新列表
    fetchEventList();
  } catch (error) {
    console.error('更新赛事状态失败', error);
    ElMessage.error('更新赛事状态失败，请重试');
  }
};

// 分页相关
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchEventList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchEventList();
};

// 处理状态变更的命令
const handleCommand = (command: string, row: Event) => {
  switch (command) {
    case 'start-registration':
      handleChangeStatus(row, 'REGISTRATION');
      break;
    case 'end-registration':
      ElMessageBox.confirm('结束报名后，用户将无法再报名参加此赛事，确定继续？', '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        handleChangeStatus(row, 'UPCOMING');
      }).catch(() => {});
      break;
    case 'start-event':
      handleChangeStatus(row, 'ONGOING');
      break;
    case 'end-event':
      handleChangeStatus(row, 'COMPLETED');
      break;
    case 'cancel-event':
      handleChangeStatus(row, 'CANCELLED');
      break;
  }
};

// 初始化加载
onMounted(() => {
  loadCategories();
  fetchEventList();
});
</script>

<style scoped>
.event-list {
  width: 100%;
}

.filter-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.table-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.event-description {
  white-space: pre-line;
  font-size: 14px;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.status-change-content {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.event-form-container {
  padding: 20px 0;
}

.event-form {
  max-width: 100%;
}
</style> 