<template>
  <admin-layout>
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
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import AdminLayout from '../../../components/AdminLayout.vue';

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
    // 实际应用中调用API获取赛事列表
    // const response = await eventsAPI.getAllEvents();
    // events.value = response.data;
    
    // 使用模拟数据
    events.value = [
      { id: 1, name: '2023年校园马拉松' },
      { id: 2, name: '大学生篮球联赛' },
      { id: 3, name: '游泳比赛' },
      { id: 4, name: '校园足球杯' },
      { id: 5, name: '冬季滑冰比赛' }
    ];
  } catch (error) {
    console.error('获取赛事列表失败', error);
    ElMessage.error('获取赛事列表失败');
  }
};

// 获取报名列表
const fetchRegistrationList = async () => {
  loading.value = true;
  try {
    // 实际应用中调用API获取报名列表
    // const response = await registrationAPI.getRegistrationList({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   athleteName: filterForm.athleteName,
    //   eventId: filterForm.eventId,
    //   status: filterForm.status
    // });
    // registrationList.value = response.content;
    // total.value = response.totalElements;
    
    // 使用模拟数据
    setTimeout(() => {
      registrationList.value = [
        {
          id: 1,
          athleteName: '张三',
          eventId: 1,
          eventName: '2023年校园马拉松',
          gender: 'MALE',
          phoneNumber: '13800138001',
          email: 'zhangsan@example.com',
          idCard: '110101199001011234',
          age: 33,
          registrationTime: '2023-04-20 10:30:00',
          status: 'APPROVED'
        },
        {
          id: 2,
          athleteName: '李四',
          eventId: 1,
          eventName: '2023年校园马拉松',
          gender: 'MALE',
          phoneNumber: '13800138002',
          email: 'lisi@example.com',
          idCard: '110101199203034321',
          age: 31,
          registrationTime: '2023-04-21 14:20:00',
          status: 'PENDING'
        },
        {
          id: 3,
          athleteName: '王五',
          eventId: 2,
          eventName: '大学生篮球联赛',
          gender: 'MALE',
          phoneNumber: '13800138003',
          email: 'wangwu@example.com',
          idCard: '110101199506067890',
          age: 28,
          registrationTime: '2023-05-05 09:15:00',
          status: 'PENDING'
        },
        {
          id: 4,
          athleteName: '赵六',
          eventId: 3,
          eventName: '游泳比赛',
          gender: 'MALE',
          phoneNumber: '13800138004',
          email: 'zhaoliu@example.com',
          idCard: '110101199607082345',
          age: 27,
          registrationTime: '2023-03-25 16:45:00',
          status: 'APPROVED'
        },
        {
          id: 5,
          athleteName: '钱七',
          eventId: 4,
          eventName: '校园足球杯',
          gender: 'MALE',
          phoneNumber: '13800138005',
          email: 'qianqi@example.com',
          idCard: '110101199708097890',
          age: 26,
          registrationTime: '2023-04-15 11:20:00',
          status: 'REJECTED',
          remark: '提交的身份证信息不匹配，请重新提交正确的身份证信息。'
        },
        {
          id: 6,
          athleteName: '孙八',
          eventId: 1,
          eventName: '2023年校园马拉松',
          gender: 'MALE',
          phoneNumber: '13800138006',
          email: 'sunba@example.com',
          idCard: '110101199809108765',
          age: 25,
          registrationTime: '2023-04-22 08:30:00',
          status: 'CANCELED'
        },
        {
          id: 7,
          athleteName: '周九',
          eventId: 3,
          eventName: '游泳比赛',
          gender: 'FEMALE',
          phoneNumber: '13800138007',
          email: 'zhoujiu@example.com',
          idCard: '110101199910119876',
          age: 24,
          registrationTime: '2023-03-26 13:10:00',
          status: 'APPROVED'
        },
        {
          id: 8,
          athleteName: '吴十',
          eventId: 5,
          eventName: '冬季滑冰比赛',
          gender: 'FEMALE',
          phoneNumber: '13800138008',
          email: 'wushi@example.com',
          idCard: '110101200011126543',
          age: 23,
          registrationTime: '2023-01-05 09:40:00',
          status: 'APPROVED'
        }
      ];
      total.value = 8;
      loading.value = false;
    }, 500);
  } catch (error) {
    console.error('获取报名列表失败', error);
    ElMessage.error('获取报名列表失败，请刷新重试');
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
    const remark = targetStatus.value === 'REJECTED' ? rejectReason.value : '';
    
    // 实际应用中调用API更新报名状态
    // await registrationAPI.updateRegistrationStatus(
    //   statusChangeRegistration.value.id, 
    //   targetStatus.value,
    //   remark
    // );
    
    // 模拟成功
    // 更新状态
    if (currentRegistration.value && currentRegistration.value.id === statusChangeRegistration.value.id) {
      currentRegistration.value.status = targetStatus.value;
      if (targetStatus.value === 'REJECTED') {
        currentRegistration.value.remark = rejectReason.value;
      }
    }
    
    // 更新列表数据
    const index = registrationList.value.findIndex(item => item.id === statusChangeRegistration.value?.id);
    if (index > -1) {
      registrationList.value[index].status = targetStatus.value;
      if (targetStatus.value === 'REJECTED') {
        registrationList.value[index].remark = rejectReason.value;
      }
    }
    
    ElMessage.success('报名状态更新成功');
    statusDialogVisible.value = false;
    
    // 刷新列表
    // fetchRegistrationList();
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