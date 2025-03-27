<template>
  <div class="registration-list">
    <div class="filter-container">
      <el-form :inline="true" :model="filterForm" class="form-inline">
        <el-form-item label="运动员姓名">
          <el-input v-model="filterForm.athleteName" placeholder="输入运动员姓名搜索" clearable />
        </el-form-item>
        <el-form-item label="赛事名称">
          <el-select v-model="filterForm.eventId" placeholder="选择赛事" clearable style="width: 220px">
            <el-option
              v-for="event in events"
              :key="event.id"
              :label="event.name"
              :value="event.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchRegistrations">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3>运动员报名列表</h3>
        <div class="table-actions">
          <el-button type="primary" @click="exportRegistrations">导出报名数据</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="registrationList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="athleteName" label="运动员姓名" width="120" />
        <el-table-column prop="eventName" label="赛事名称" width="200" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ scope.row.gender === 'MALE' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="phoneNumber" label="联系电话" width="130" />
        <el-table-column prop="registrationTime" label="报名时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleView(scope.row)"
              >详情</el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              size="small"
              type="success"
              @click="handleChangeStatus(scope.row, 'APPROVED')"
              >通过</el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="handleChangeStatus(scope.row, 'REJECTED')"
              >拒绝</el-button>
            <el-button
              v-if="['PENDING', 'APPROVED'].includes(scope.row.status)"
              size="small"
              type="warning"
              @click="handleChangeStatus(scope.row, 'CANCELED')"
              >取消</el-button>
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
      title="报名详情"
      v-model="detailDialogVisible"
      width="700px"
    >
      <div v-if="currentRegistration" class="registration-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="运动员姓名">{{ currentRegistration.athleteName }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentRegistration.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="赛事名称" :span="2">{{ currentRegistration.eventName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRegistration.phoneNumber }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentRegistration.email }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentRegistration.idCard }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentRegistration.age }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ currentRegistration.registrationTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentRegistration.status)">
              {{ formatStatus(currentRegistration.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            <div class="registration-remark">{{ currentRegistration.remark || '无' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentRegistration.status === 'PENDING'"
            type="success"
            @click="handleChangeStatus(currentRegistration, 'APPROVED')"
            >通过</el-button>
          <el-button
            v-if="currentRegistration.status === 'PENDING'"
            type="danger"
            @click="handleChangeStatus(currentRegistration, 'REJECTED')"
            >拒绝</el-button>
          <el-button
            v-if="['PENDING', 'APPROVED'].includes(currentRegistration.status)"
            type="warning"
            @click="handleChangeStatus(currentRegistration, 'CANCELED')"
            >取消</el-button>
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
        <el-form v-if="targetStatus === 'REJECTED'" label-width="80px">
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
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import { registrationAPI } from '../../../api/registrationAPI';
import { eventsAPI } from '../../../api/eventsAPI';

interface Event {
  id: number;
  name: string;
}

interface Registration {
  id: number;
  athleteName: string;
  eventId: number;
  eventName: string;
  gender: 'MALE' | 'FEMALE';
  phoneNumber: string;
  email: string;
  idCard: string;
  age: number;
  registrationTime: string;
  status: string;
  remark?: string;
}

// 过滤表单
const filterForm = reactive({
  athleteName: '',
  eventId: null as number | null,
  status: ''
});

// 报名列表数据
const registrationList = ref<Registration[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// 赛事数据
const events = ref<Event[]>([]);

// 详情弹窗
const detailDialogVisible = ref(false);
const currentRegistration = ref<Registration | null>(null);

// 状态变更弹窗
const statusDialogVisible = ref(false);
const statusChangeRegistration = ref<Registration | null>(null);
const targetStatus = ref('');
const rejectReason = ref('');
const statusChangeTitle = computed(() => {
  return `修改报名状态为 ${formatStatus(targetStatus.value)}`;
});
const statusChangeMessage = computed(() => {
  if (!statusChangeRegistration.value) return '';
  
  switch (targetStatus.value) {
    case 'APPROVED':
      return `确定要通过 ${statusChangeRegistration.value.athleteName} 的报名申请吗？`;
    case 'REJECTED':
      return `确定要拒绝 ${statusChangeRegistration.value.athleteName} 的报名申请吗？`;
    case 'CANCELED':
      return `确定要取消 ${statusChangeRegistration.value.athleteName} 的报名吗？`;
    default:
      return `确定要修改 ${statusChangeRegistration.value.athleteName} 的报名状态吗？`;
  }
});

// 加载赛事列表
const loadEvents = async () => {
  try {
    // 获取所有赛事
    const allEvents = await eventsAPI.getAllEvents();
    
    // 格式化赛事数据
    events.value = allEvents.map(event => ({
      id: event.id,
      name: event.name
    }));
  } catch (error) {
    console.error('获取赛事列表失败', error);
    ElMessage.error('获取赛事列表失败');
  }
};

// 获取报名列表
const fetchRegistrationList = async () => {
  loading.value = true;
  try {
    let registrations = [];
    
    if (filterForm.eventId) {
      // 按赛事ID获取报名
      const eventId = filterForm.eventId;
      let status = filterForm.status || undefined;
      
      // 如果有状态筛选
      if (status) {
        const response = await registrationAPI.getRegistrationsByEventAndStatus(
          eventId, 
          status,
          currentPage.value - 1, 
          pageSize.value
        );
        registrations = response.content;
        total.value = response.totalElements;
      } else {
        // 没有状态筛选
        const response = await registrationAPI.getRegistrationsByEvent(
          eventId,
          currentPage.value - 1,
          pageSize.value
        );
        registrations = response.content;
        total.value = response.totalElements;
      }
    } else {
      // 获取所有报名
      const allRegistrations = await registrationAPI.getAllRegistrations();
      
      // 手动筛选
      let filteredRegistrations = allRegistrations;
      
      // 按状态筛选
      if (filterForm.status) {
        filteredRegistrations = filteredRegistrations.filter(reg => reg.status === filterForm.status);
      }
      
      // 按运动员姓名筛选
      if (filterForm.athleteName) {
        filteredRegistrations = filteredRegistrations.filter(reg => 
          reg.user.username.toLowerCase().includes(filterForm.athleteName.toLowerCase())
        );
      }
      
      // 计算总数
      total.value = filteredRegistrations.length;
      
      // 手动分页
      const startIndex = (currentPage.value - 1) * pageSize.value;
      const endIndex = startIndex + pageSize.value;
      registrations = filteredRegistrations.slice(startIndex, endIndex);
    }
    
    // 格式化报名数据
    registrationList.value = registrations.map(reg => ({
      id: reg.id,
      athleteName: reg.user.username,
      eventId: reg.event.id,
      eventName: reg.event.name,
      gender: 'MALE', // 默认值，实际应从用户信息中获取
      phoneNumber: '',  // 默认值，实际应从用户信息中获取
      email: '', // 默认值，实际应从用户信息中获取
      idCard: '', // 默认值，实际应从用户信息中获取
      age: 0, // 默认值，实际应从用户信息中获取
      registrationTime: dayjs(reg.registrationTime || reg.createdAt).format('YYYY-MM-DD HH:mm:ss'),
      status: reg.status,
      remark: reg.notes // 使用notes作为备注字段
    }));
  } catch (error) {
    console.error('获取报名列表失败', error);
    ElMessage.error('获取报名列表失败，请刷新重试');
  } finally {
    loading.value = false;
  }
};

// 搜索报名
const searchRegistrations = () => {
  currentPage.value = 1;
  fetchRegistrationList();
};

// 重置表单
const resetForm = () => {
  filterForm.athleteName = '';
  filterForm.eventId = null;
  filterForm.status = '';
  searchRegistrations();
};

// 格式化状态显示
const formatStatus = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '待审核';
    case 'APPROVED':
      return '已通过';
    case 'REJECTED':
      return '已拒绝';
    case 'CANCELED':
      return '已取消';
    default:
      return status;
  }
};

// 获取状态对应的标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning';
    case 'APPROVED':
      return 'success';
    case 'REJECTED':
      return 'danger';
    case 'CANCELED':
      return 'info';
    default:
      return 'info';
  }
};

// 查看报名详情
const handleView = (row: Registration) => {
  currentRegistration.value = row;
  detailDialogVisible.value = true;
};

// 修改报名状态
const handleChangeStatus = (row: Registration, status: string) => {
  statusChangeRegistration.value = row;
  targetStatus.value = status;
  rejectReason.value = ''; // 重置拒绝原因
  statusDialogVisible.value = true;
};

// 确认修改状态
const confirmStatusChange = async () => {
  if (!statusChangeRegistration.value) return;
  
  try {
    // 获取拒绝原因
    const notes = targetStatus.value === 'REJECTED' ? rejectReason.value : undefined;
    
    // 调用API更新报名状态
    await registrationAPI.updateRegistrationStatus(
      statusChangeRegistration.value.id, 
      targetStatus.value as 'APPROVED' | 'REJECTED',
      notes
    );
    
    ElMessage.success('报名状态更新成功');
    statusDialogVisible.value = false;
    
    // 关闭详情弹窗（如果打开的话）
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false;
    }
    
    // 刷新列表
    fetchRegistrationList();
  } catch (error) {
    console.error('更新报名状态失败', error);
    ElMessage.error('更新报名状态失败，请重试');
  }
};

// 导出报名数据
const exportRegistrations = () => {
  ElMessage.success('导出成功，请在下载列表中查看');
};

// 分页相关
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchRegistrationList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchRegistrationList();
};

// 初始化加载
onMounted(() => {
  loadEvents();
  fetchRegistrationList();
});
</script>

<style scoped>
.registration-list {
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

.registration-remark {
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