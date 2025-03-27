<template>
  <div class="event-participants">
    <div class="page-header">
      <h2>赛事参与者管理</h2>
      <el-button @click="$router.push('/admin/events/list')">返回赛事列表</el-button>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton style="width: 100%" :rows="10" animated />
    </div>
    
    <div v-else>
      <div class="event-info">
        <el-card class="event-card">
          <template #header>
            <div class="card-header">
              <span>赛事信息</span>
              <el-button
                v-if="canEdit(event)"
                type="primary"
                size="small"
                @click="$router.push(`/admin/events/edit/${eventId}`)"
              >编辑赛事</el-button>
            </div>
          </template>
          <el-descriptions :column="2" border size="medium">
            <el-descriptions-item label="赛事名称" :span="2">{{ event.name }}</el-descriptions-item>
            <el-descriptions-item label="赛事分类">{{ event.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="赛事状态">
              <el-tag :type="getStatusType(event.status)">
                {{ formatStatus(event.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ event.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ event.endDate }}</el-descriptions-item>
            <el-descriptions-item label="报名人数">{{ participants.length }}</el-descriptions-item>
            <el-descriptions-item label="最大人数">{{ event.maxParticipants }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
      
      <div class="participants-section">
        <div class="section-header">
          <h3>参与者列表</h3>
          <div class="header-actions">
            <el-button type="primary" @click="exportParticipants">导出参与者列表</el-button>
            <el-button type="success" @click="showAddParticipantDialog">添加参与者</el-button>
          </div>
        </div>
        
        <div class="filter-container">
          <el-form :inline="true" :model="filterForm" class="form-inline">
            <el-form-item label="参与者姓名">
              <el-input v-model="filterForm.name" placeholder="输入参与者姓名搜索" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
                <el-option label="待审核" value="PENDING" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已拒绝" value="REJECTED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchParticipants">搜索</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-table
          v-loading="tableLoading"
          :data="filteredParticipants"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="athleteName" label="参与者姓名" width="150" />
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="scope">
              {{ scope.row.gender === 'MALE' ? '男' : '女' }}
            </template>
          </el-table-column>
          <el-table-column prop="phoneNumber" label="联系电话" width="150" />
          <el-table-column prop="registrationTime" label="报名时间" width="180" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getRegistrationStatusType(scope.row.status)">
                {{ formatRegistrationStatus(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="240">
            <template #default="scope">
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="success"
                size="small"
                @click="handleStatusChange(scope.row, 'APPROVED')"
              >通过</el-button>
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="danger"
                size="small"
                @click="handleStatusChange(scope.row, 'REJECTED')"
              >拒绝</el-button>
              <el-button
                v-if="scope.row.status !== 'CANCELLED'"
                type="warning"
                size="small"
                @click="handleStatusChange(scope.row, 'CANCELLED')"
              >取消</el-button>
              <el-button
                type="info"
                size="small"
                @click="handleEdit(scope.row)"
              >编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    
    <!-- 状态变更确认弹窗 -->
    <el-dialog
      :title="statusChangeTitle"
      v-model="statusDialogVisible"
      width="500px"
    >
      <div v-if="targetStatus === 'REJECTED'">
        <el-form>
          <el-form-item label="拒绝原因">
            <el-input
              v-model="rejectReason"
              type="textarea"
              :rows="3"
              placeholder="请输入拒绝原因"
            />
          </el-form-item>
        </el-form>
      </div>
      <div v-else class="status-change-content">
        <p>{{ statusChangeMessage }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmStatusChange">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑参与者弹窗 -->
    <el-dialog
      title="编辑参与者信息"
      v-model="editDialogVisible"
      width="600px"
    >
      <el-form
        v-if="currentParticipant"
        ref="participantFormRef"
        :model="participantForm"
        :rules="participantRules"
        label-width="100px"
      >
        <el-form-item label="参与者姓名" prop="athleteName">
          <el-input v-model="participantForm.athleteName" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="participantForm.gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话" prop="phoneNumber">
          <el-input v-model="participantForm.phoneNumber" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="participantForm.remark"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveParticipant">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加参与者弹窗 -->
    <el-dialog
      title="添加参与者"
      v-model="addDialogVisible"
      width="600px"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="participantRules"
        label-width="100px"
      >
        <el-form-item label="参与者姓名" prop="athleteName">
          <el-input v-model="addForm.athleteName" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="addForm.gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话" prop="phoneNumber">
          <el-input v-model="addForm.phoneNumber" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="addForm.remark"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addParticipant">添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';
import { eventsAPI } from '../../../api/eventsAPI';
import { registrationAPI } from '../../../api/registrationAPI';
import dayjs from 'dayjs';

// 路由相关
const router = useRouter();
const route = useRoute();
const eventId = ref(Number(route.params.id));

// 状态
const loading = ref(true);
const tableLoading = ref(false);
const event = ref<any>({});
const participants = ref<any[]>([]);
const filteredParticipants = ref<any[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 过滤
const filterForm = reactive({
  name: '',
  status: ''
});

// 状态变更弹窗
const statusDialogVisible = ref(false);
const statusChangeParticipant = ref<any>(null);
const targetStatus = ref('');
const rejectReason = ref('');

// 编辑弹窗
const editDialogVisible = ref(false);
const currentParticipant = ref<any>(null);
const participantForm = reactive({
  id: 0,
  athleteName: '',
  gender: 'MALE',
  phoneNumber: '',
  remark: ''
});

// 添加弹窗
const addDialogVisible = ref(false);
const addForm = reactive({
  athleteName: '',
  gender: 'MALE',
  phoneNumber: '',
  remark: ''
});

// 表单ref
const participantFormRef = ref<FormInstance>();
const addFormRef = ref<FormInstance>();

// 表单验证规则
const participantRules = reactive<FormRules>({
  athleteName: [
    { required: true, message: '请输入参与者姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应在2到50个字符之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phoneNumber: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ]
});

// 计算属性
const statusChangeTitle = computed(() => {
  return `修改参与者状态为 ${formatRegistrationStatus(targetStatus.value)}`;
});

const statusChangeMessage = computed(() => {
  if (!statusChangeParticipant.value) return '';
  
  switch (targetStatus.value) {
    case 'APPROVED':
      return `确定要通过 ${statusChangeParticipant.value.athleteName} 的参与申请吗？`;
    case 'REJECTED':
      return `确定要拒绝 ${statusChangeParticipant.value.athleteName} 的参与申请吗？`;
    case 'CANCELLED':
      return `确定要取消 ${statusChangeParticipant.value.athleteName} 的参与吗？`;
    default:
      return `确定要修改 ${statusChangeParticipant.value.athleteName} 的参与状态吗？`;
  }
});

// 加载数据
onMounted(async () => {
  await Promise.all([
    fetchEventDetails(),
    fetchParticipants()
  ]);
  loading.value = false;
});

// 获取赛事详情
const fetchEventDetails = async (retryCount = 0) => {
  try {
    const eventData = await eventsAPI.getEventById(eventId.value);
    event.value = {
      id: eventData.id,
      name: eventData.name,
      categoryName: eventData.category?.name || '未分类',
      startDate: dayjs(eventData.startTime).format('YYYY-MM-DD'),
      endDate: dayjs(eventData.endTime).format('YYYY-MM-DD'),
      maxParticipants: eventData.maxParticipants || 0,
      currentParticipants: eventData.currentParticipants || 0,
      status: eventData.status || 'UNKNOWN'
    };
    return true; // 成功获取数据
  } catch (error: any) {
    console.error('获取赛事详情失败', error);
    
    // 重试逻辑，最多重试2次
    if (retryCount < 2 && (!error.response || error.response.status !== 401)) {
      console.log(`尝试重新获取赛事详情，第${retryCount + 1}次重试`);
      await new Promise(resolve => setTimeout(resolve, 1000)); // 等待1秒后重试
      return fetchEventDetails(retryCount + 1);
    }
    
    // 判断是否是认证错误
    if (error.response && error.response.status === 401) {
      ElMessage.error('您的登录已过期，请重新登录');
      // 不再显示额外的错误信息，避免多个提示
    } else {
      ElMessage.error('获取赛事详情失败，请刷新重试');
    }
    
    // 设置基本信息防止页面显示异常
    if (!event.value) {
      event.value = {
        id: eventId.value,
        name: '加载失败',
        categoryName: '',
        startDate: '',
        endDate: '',
        maxParticipants: 0,
        currentParticipants: 0,
        status: ''
      };
    }
    return false; // 获取数据失败
  }
};

// 获取参与者列表
const fetchParticipants = async (retryCount = 0) => {
  tableLoading.value = true;
  try {
    const response = await registrationAPI.getRegistrationsByEvent(eventId.value, currentPage.value - 1, pageSize.value);
    participants.value = response.content.map(reg => ({
      id: reg.id,
      athleteId: reg.user?.id || 0,
      athleteName: reg.user?.username || '未知用户',
      gender: reg.user?.gender || 'UNKNOWN',
      phoneNumber: reg.user?.phone || reg.contactPhone || '未知',
      email: reg.user?.email || '', 
      registrationTime: dayjs(reg.createdAt || reg.registrationTime).format('YYYY-MM-DD HH:mm:ss'),
      status: reg.status || 'UNKNOWN',
      remark: reg.remarks || reg.notes || ''
    }));
    
    total.value = response.totalElements;
    applyFilters();
    return true; // 成功获取数据
  } catch (error: any) {
    console.error('获取参与者列表失败', error);
    
    // 重试逻辑，最多重试2次
    if (retryCount < 2 && (!error.response || error.response.status !== 401)) {
      console.log(`尝试重新获取参与者列表，第${retryCount + 1}次重试`);
      await new Promise(resolve => setTimeout(resolve, 1000)); // 等待1秒后重试
      tableLoading.value = false; // 重置加载状态
      return fetchParticipants(retryCount + 1);
    }
    
    // 判断是否是认证错误
    if (error.response && error.response.status === 401) {
      // 401错误已在fetchEventDetails或axios拦截器中处理，此处不重复显示
      console.warn('认证错误，跳过参与者列表加载');
    } else {
      ElMessage.error('获取参与者列表失败，请刷新重试');
    }
    
    // 清空参与者列表但保持页面不崩溃
    participants.value = [];
    filteredParticipants.value = [];
    total.value = 0;
    return false; // 获取数据失败
  } finally {
    tableLoading.value = false;
  }
};

// 应用过滤器
const applyFilters = () => {
  filteredParticipants.value = participants.value.filter(participant => {
    let matches = true;
    
    if (filterForm.name && !participant.athleteName.toLowerCase().includes(filterForm.name.toLowerCase())) {
      matches = false;
    }
    
    if (filterForm.status && participant.status !== filterForm.status) {
      matches = false;
    }
    
    return matches;
  });
};

// 搜索参与者
const searchParticipants = () => {
  applyFilters();
};

// 重置过滤器
const resetFilter = () => {
  filterForm.name = '';
  filterForm.status = '';
  applyFilters();
};

// 处理页面变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchParticipants();
};

// 格式化赛事状态显示
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

// 获取赛事状态标签类型
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

// 格式化报名状态显示
const formatRegistrationStatus = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '待审核';
    case 'APPROVED':
      return '已通过';
    case 'REJECTED':
      return '已拒绝';
    case 'CANCELLED':
      return '已取消';
    default:
      return status;
  }
};

// 获取报名状态标签类型
const getRegistrationStatusType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning';
    case 'APPROVED':
      return 'success';
    case 'REJECTED':
      return 'danger';
    case 'CANCELLED':
      return 'info';
    default:
      return '';
  }
};

// 判断赛事是否可编辑
const canEdit = (eventData: any) => {
  return ['UPCOMING', 'REGISTRATION'].includes(eventData.status);
};

// 处理状态变更
const handleStatusChange = (row: any, status: string) => {
  statusChangeParticipant.value = row;
  targetStatus.value = status;
  
  if (status === 'REJECTED') {
    rejectReason.value = '';
  }
  
  statusDialogVisible.value = true;
};

// 确认状态变更
const confirmStatusChange = async () => {
  if (!statusChangeParticipant.value) return;
  
  try {
    const notes = targetStatus.value === 'REJECTED' ? rejectReason.value : undefined;
    
    await registrationAPI.updateRegistrationStatus(
      statusChangeParticipant.value.id,
      targetStatus.value,
      notes
    );
    
    ElMessage.success('参与者状态已更新');
    statusDialogVisible.value = false;
    
    // 刷新数据
    await fetchParticipants();
    
    // 如果是批准参与，可能需要刷新赛事详情以更新当前参与人数
    if (targetStatus.value === 'APPROVED') {
      await fetchEventDetails();
    }
  } catch (error) {
    console.error('更新参与者状态失败', error);
    ElMessage.error('更新参与者状态失败，请重试');
  }
};

// 处理编辑参与者
const handleEdit = (row: any) => {
  currentParticipant.value = row;
  participantForm.id = row.id;
  participantForm.athleteName = row.athleteName;
  participantForm.gender = row.gender;
  participantForm.phoneNumber = row.phoneNumber;
  participantForm.remark = row.remark;
  
  editDialogVisible.value = true;
};

// 保存参与者编辑
const saveParticipant = async () => {
  if (!participantFormRef.value) return;
  
  await participantFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 在实际应用中，这里应该调用API来更新参与者信息
        // 目前我们只是模拟更新
        const index = participants.value.findIndex(p => p.id === participantForm.id);
        
        if (index !== -1) {
          participants.value[index] = {
            ...participants.value[index],
            athleteName: participantForm.athleteName,
            gender: participantForm.gender,
            phoneNumber: participantForm.phoneNumber,
            remark: participantForm.remark
          };
          
          applyFilters();
          ElMessage.success('参与者信息已更新');
          editDialogVisible.value = false;
        }
      } catch (error) {
        console.error('更新参与者信息失败', error);
        ElMessage.error('更新参与者信息失败，请重试');
      }
    }
  });
};

// 显示添加参与者对话框
const showAddParticipantDialog = () => {
  addForm.athleteName = '';
  addForm.gender = 'MALE';
  addForm.phoneNumber = '';
  addForm.remark = '';
  
  addDialogVisible.value = true;
};

// 添加参与者
const addParticipant = async () => {
  if (!addFormRef.value) return;
  
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 在实际应用中，这里应该调用API来添加参与者
        // 目前我们只是模拟添加
        const newParticipant = {
          id: Date.now(), // 模拟ID生成
          athleteId: Date.now(),
          athleteName: addForm.athleteName,
          gender: addForm.gender,
          phoneNumber: addForm.phoneNumber,
          email: '',
          registrationTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
          status: 'APPROVED',
          remark: addForm.remark
        };
        
        participants.value.unshift(newParticipant);
        total.value += 1;
        applyFilters();
        
        ElMessage.success('参与者已添加');
        addDialogVisible.value = false;
        
        // 在实际应用中，可能需要更新赛事当前参与人数
        event.value.currentParticipants += 1;
      } catch (error) {
        console.error('添加参与者失败', error);
        ElMessage.error('添加参与者失败，请重试');
      }
    }
  });
};

// 导出参与者列表
const exportParticipants = () => {
  ElMessage.success('参与者列表导出功能已触发');
  // 实际应用中，这里应该调用导出API或生成并下载CSV文件
};
</script>

<style scoped>
.event-participants {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.event-info {
  margin-bottom: 30px;
}

.event-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.participants-section {
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.status-change-content {
  min-height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style> 