<template>
  <div class="event-list">
    <div class="filter-container">
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
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="报名中" value="REGISTRATION" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已结束" value="ENDED" />
            <el-option label="已取消" value="CANCELED" />
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
        <el-button type="primary" @click="$router.push('/admin/events/create')">创建赛事</el-button>
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
              v-if="scope.row.status === 'NOT_STARTED'"
              size="small"
              type="info"
              @click="handleChangeStatus(scope.row, 'REGISTRATION')"
              >开放报名</el-button>
            <el-button
              v-if="scope.row.status === 'REGISTRATION'"
              size="small"
              type="warning"
              @click="handleChangeStatus(scope.row, 'IN_PROGRESS')"
              >开始比赛</el-button>
            <el-button
              v-if="scope.row.status === 'IN_PROGRESS'"
              size="small"
              type="danger"
              @click="handleChangeStatus(scope.row, 'ENDED')"
              >结束比赛</el-button>
            <el-button
              v-if="canCancel(scope.row)"
              size="small"
              type="danger"
              @click="handleChangeStatus(scope.row, 'CANCELED')"
              >取消赛事</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { eventsAPI } from '../../../api/eventsAPI';
import { categoryAPI } from '../../../api/categoryAPI';

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
    case 'IN_PROGRESS':
      return `确定要将赛事 "${statusChangeEvent.value.name}" 标记为进行中吗？这将关闭报名通道。`;
    case 'ENDED':
      return `确定要将赛事 "${statusChangeEvent.value.name}" 标记为已结束吗？`;
    case 'CANCELED':
      return `确定要取消赛事 "${statusChangeEvent.value.name}" 吗？取消后无法恢复。`;
    default:
      return `确定要修改赛事 "${statusChangeEvent.value.name}" 的状态吗？`;
  }
});

// 加载分类列表
const loadCategories = async () => {
  try {
    // 实际应用中调用API获取分类列表
    // const response = await categoryAPI.getAllCategories();
    // categories.value = response.data;
    
    // 使用模拟数据
    categories.value = [
      { id: 1, name: '田径赛事' },
      { id: 2, name: '球类赛事' },
      { id: 3, name: '水上赛事' },
      { id: 4, name: '冰雪赛事' },
      { id: 5, name: '格斗赛事' }
    ];
  } catch (error) {
    console.error('获取分类列表失败', error);
    ElMessage.error('获取分类列表失败');
  }
};

// 获取赛事列表
const fetchEventList = async () => {
  loading.value = true;
  try {
    // 实际应用中调用API获取赛事列表
    // const response = await eventsAPI.getEventList({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   name: filterForm.name,
    //   categoryId: filterForm.categoryId,
    //   status: filterForm.status
    // });
    // eventList.value = response.content;
    // total.value = response.totalElements;
    
    // 使用模拟数据
    setTimeout(() => {
      eventList.value = [
        {
          id: 1,
          name: '2023年校园马拉松',
          category: '田径赛事',
          categoryId: 1,
          startDate: '2023-05-15',
          endDate: '2023-05-15',
          registrationStartDate: '2023-04-15',
          registrationEndDate: '2023-05-10',
          location: '校园田径场',
          organizer: '体育部',
          description: '校园马拉松活动，分为3公里、5公里和10公里三个组别',
          registrationCount: 85,
          maxParticipants: 200,
          status: 'REGISTRATION',
          createdAt: '2023-04-01 10:00:00',
          createdBy: 'admin'
        },
        {
          id: 2,
          name: '大学生篮球联赛',
          category: '球类赛事',
          categoryId: 2,
          startDate: '2023-06-01',
          endDate: '2023-06-15',
          registrationStartDate: '2023-05-01',
          registrationEndDate: '2023-05-20',
          location: '体育馆',
          organizer: '校体育协会',
          description: '大学生篮球联赛，各学院组队参加',
          registrationCount: 12,
          maxParticipants: 16,
          status: 'NOT_STARTED',
          createdAt: '2023-04-05 14:30:00',
          createdBy: 'admin'
        },
        {
          id: 3,
          name: '游泳比赛',
          category: '水上赛事',
          categoryId: 3,
          startDate: '2023-04-20',
          endDate: '2023-04-20',
          registrationStartDate: '2023-03-20',
          registrationEndDate: '2023-04-15',
          location: '游泳馆',
          organizer: '游泳协会',
          description: '游泳比赛，包括自由泳、蛙泳、蝶泳等项目',
          registrationCount: 32,
          maxParticipants: 50,
          status: 'IN_PROGRESS',
          createdAt: '2023-03-10 09:15:00',
          createdBy: 'admin'
        },
        {
          id: 4,
          name: '校园足球杯',
          category: '球类赛事',
          categoryId: 2,
          startDate: '2023-05-10',
          endDate: '2023-05-25',
          registrationStartDate: '2023-04-10',
          registrationEndDate: '2023-05-05',
          location: '足球场',
          organizer: '足球协会',
          description: '校园足球杯比赛，各学院组队参加',
          registrationCount: 8,
          maxParticipants: 16,
          status: 'REGISTRATION',
          createdAt: '2023-04-01 08:30:00',
          createdBy: 'event_manager'
        },
        {
          id: 5,
          name: '冬季滑冰比赛',
          category: '冰雪赛事',
          categoryId: 4,
          startDate: '2023-01-15',
          endDate: '2023-01-15',
          registrationStartDate: '2022-12-15',
          registrationEndDate: '2023-01-10',
          location: '冰场',
          organizer: '冰雪协会',
          description: '冬季滑冰比赛，包括速滑和花样滑冰',
          registrationCount: 28,
          maxParticipants: 40,
          status: 'ENDED',
          createdAt: '2022-12-01 11:20:00',
          createdBy: 'admin'
        }
      ];
      total.value = 5;
      loading.value = false;
    }, 500);
  } catch (error) {
    console.error('获取赛事列表失败', error);
    ElMessage.error('获取赛事列表失败，请刷新重试');
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
    case 'NOT_STARTED':
      return '未开始';
    case 'REGISTRATION':
      return '报名中';
    case 'IN_PROGRESS':
      return '进行中';
    case 'ENDED':
      return '已结束';
    case 'CANCELED':
      return '已取消';
    default:
      return status;
  }
};

// 获取状态对应的标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'NOT_STARTED':
      return 'info';
    case 'REGISTRATION':
      return 'warning';
    case 'IN_PROGRESS':
      return 'success';
    case 'ENDED':
      return 'danger';
    case 'CANCELED':
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
  return ['NOT_STARTED', 'REGISTRATION'].includes(event.status);
};

// 判断赛事是否可取消
const canCancel = (event: Event) => {
  return ['NOT_STARTED', 'REGISTRATION'].includes(event.status);
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
    // 实际应用中调用API更新赛事状态
    // await eventsAPI.updateEventStatus(statusChangeEvent.value.id, targetStatus.value);
    
    // 模拟成功
    statusChangeEvent.value.status = targetStatus.value;
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
}
</style> 