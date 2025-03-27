<template>
  <div class="event-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="!event" class="not-found-container">
      <el-result
        icon="error"
        title="找不到赛事"
        sub-title="您请求的赛事不存在或已被删除"
      >
        <template #extra>
          <router-link to="/user/events">
            <el-button type="primary">返回赛事列表</el-button>
          </router-link>
        </template>
      </el-result>
    </div>
    
    <div v-else class="event-content">
      <div class="event-header">
        <el-breadcrumb class="breadcrumb">
          <el-breadcrumb-item to="/user/dashboard">首页</el-breadcrumb-item>
          <el-breadcrumb-item to="/user/events">赛事列表</el-breadcrumb-item>
          <el-breadcrumb-item>赛事详情</el-breadcrumb-item>
        </el-breadcrumb>
        
        <div class="title-section">
          <h1 class="event-title">{{ event.name }}</h1>
          <el-tag :type="getStatusType(event.status)" size="large">{{ getStatusText(event.status) }}</el-tag>
        </div>
      </div>
      
      <el-row :gutter="20" class="event-body">
        <el-col :xs="24" :md="16">
          <el-card class="main-info">
            <template #header>
              <div class="card-header">
                <h2>赛事信息</h2>
              </div>
            </template>
            
            <div class="event-details">
              <div class="detail-item">
                <div class="detail-label">赛事类别</div>
                <div class="detail-value">
                  <el-tag type="info">{{ event.category.name }}</el-tag>
                </div>
              </div>
              
              <div class="detail-item">
                <div class="detail-label">地点</div>
                <div class="detail-value">{{ event.location }}</div>
              </div>
              
              <div class="detail-item">
                <div class="detail-label">开始时间</div>
                <div class="detail-value">{{ formatDate(event.startTime) }}</div>
              </div>
              
              <div class="detail-item">
                <div class="detail-label">结束时间</div>
                <div class="detail-value">{{ formatDate(event.endTime) }}</div>
              </div>
              
              <div class="detail-item">
                <div class="detail-label">报名截止</div>
                <div class="detail-value">{{ formatDate(event.registrationDeadline) }}</div>
              </div>
              
              <div class="detail-item">
                <div class="detail-label">参与人数上限</div>
                <div class="detail-value">{{ event.maxParticipants }}人</div>
              </div>
            </div>
            
            <div class="description-section">
              <h3>赛事描述</h3>
              <div class="event-description">{{ event.description }}</div>
            </div>
          </el-card>
          
          <el-card class="announcement-card">
            <template #header>
              <div class="card-header">
                <h3>赛事公告</h3>
              </div>
            </template>
            
            <div v-if="loadingAnnouncements" class="loading-placeholder">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="eventAnnouncements.length === 0" class="empty-data">
              <el-empty description="暂无公告" />
            </div>
            <div v-else class="announcement-list">
              <div v-for="announcement in eventAnnouncements" :key="announcement.id" class="announcement-item">
                <div class="announcement-header">
                  <h4 class="announcement-title">{{ announcement.title }}</h4>
                  <span class="announcement-date">{{ formatDate(announcement.createdAt) }}</span>
                </div>
                <div class="announcement-content">{{ announcement.content }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :md="8">
          <el-card class="registration-card">
            <template #header>
              <div class="card-header">
                <h3>赛事报名</h3>
              </div>
            </template>
            
            <div class="registration-status">
              <template v-if="isRegistrationOpen">
                <div class="deadline-info">
                  <el-alert
                    title="报名进行中"
                    type="success"
                    :closable="false"
                    show-icon
                  >
                    <div>截止时间: {{ formatDate(event.registrationDeadline) }}</div>
                  </el-alert>
                </div>
                
                <div v-if="myRegistration" class="my-registration">
                  <div class="registration-info">
                    <h4>您的报名状态</h4>
                    <el-tag :type="getRegistrationStatusType(myRegistration.status)">
                      {{ getRegistrationStatusText(myRegistration.status) }}
                    </el-tag>
                    <div class="registration-date">
                      报名时间: {{ formatDate(myRegistration.registrationTime) }}
                    </div>
                    <div v-if="myRegistration.notes" class="registration-notes">
                      备注: {{ myRegistration.notes }}
                    </div>
                  </div>
                  
                  <div class="registration-actions" v-if="canCancelRegistration">
                    <el-button type="danger" @click="handleCancelRegistration">取消报名</el-button>
                  </div>
                </div>
                
                <div v-else class="register-form">
                  <el-form :model="registerForm" label-position="top">
                    <el-form-item label="备注信息 (选填)">
                      <el-input
                        v-model="registerForm.notes"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入您的特殊要求或其他补充信息"
                      />
                    </el-form-item>
                    
                    <el-form-item>
                      <el-button 
                        type="primary" 
                        :loading="submitting" 
                        @click="handleRegister"
                        :disabled="!canRegister"
                      >
                        立即报名
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </template>
              
              <template v-else>
                <el-alert
                  :title="registrationClosedReason"
                  type="warning"
                  :closable="false"
                  show-icon
                />
              </template>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 取消报名确认对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消报名确认"
      width="30%"
    >
      <span>您确定要取消此赛事的报名吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmCancelRegistration" :loading="cancelling">
            确认取消
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 成功提示 -->
    <el-dialog
      v-model="successDialogVisible"
      :title="successDialogTitle"
      width="30%"
    >
      <div>{{ successDialogMessage }}</div>
      <template #footer>
        <el-button type="primary" @click="successDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { eventsAPI } from '../../api/eventsAPI';
import { announcementAPI } from '../../api/announcementAPI';
import { registrationAPI } from '../../api/registrationAPI';
import type { Registration } from '../../api/registrationAPI';

const route = useRoute();
const router = useRouter();

// 数据状态
const event = ref<any>(null);
const loading = ref(true);
const eventAnnouncements = ref<any[]>([]);
const loadingAnnouncements = ref(true);
const myRegistration = ref<Registration | null>(null);
const loadingRegistration = ref(true);

// 报名表单
const registerForm = ref({
  notes: ''
});

// 对话框状态
const submitting = ref(false);
const cancelling = ref(false);
const cancelDialogVisible = ref(false);
const successDialogVisible = ref(false);
const successDialogTitle = ref('');
const successDialogMessage = ref('');

// 初始化
onMounted(async () => {
  const eventId = Number(route.params.id);
  if (isNaN(eventId)) {
    router.push('/user/events');
    return;
  }
  
  await fetchEventDetails(eventId);
  await Promise.all([
    fetchEventAnnouncements(eventId),
    fetchMyRegistration()
  ]);
});

// 获取赛事详情
const fetchEventDetails = async (eventId: number) => {
  try {
    const data = await eventsAPI.getPublicEventById(eventId);
    event.value = data;
  } catch (error) {
    console.error('获取赛事详情失败:', error);
  } finally {
    loading.value = false;
  }
};

// 获取赛事公告
const fetchEventAnnouncements = async (eventId: number) => {
  loadingAnnouncements.value = true;
  try {
    const data = await announcementAPI.getAnnouncementsByEvent(eventId);
    eventAnnouncements.value = data;
  } catch (error) {
    console.error('获取赛事公告失败:', error);
  } finally {
    loadingAnnouncements.value = false;
  }
};

// 获取我的报名信息
const fetchMyRegistration = async () => {
  loadingRegistration.value = true;
  try {
    const registrations = await registrationAPI.getMyRegistrations();
    if (event.value) {
      myRegistration.value = registrations.find(reg => reg.event.id === event.value.id) || null;
    }
  } catch (error) {
    console.error('获取我的报名信息失败:', error);
  } finally {
    loadingRegistration.value = false;
  }
};

// 报名操作
const handleRegister = async () => {
  if (!event.value || !canRegister.value) return;
  
  submitting.value = true;
  try {
    await registrationAPI.registerForEvent(event.value.id, registerForm.value.notes);
    
    // 重新获取报名信息
    await fetchMyRegistration();
    
    // 显示成功提示
    successDialogTitle.value = '报名成功';
    successDialogMessage.value = '您已成功报名此赛事，请等待审核结果';
    successDialogVisible.value = true;
    
    // 清空表单
    registerForm.value.notes = '';
  } catch (error: any) {
    let errorMsg = '报名失败，请稍后再试';
    if (error.response && error.response.data && error.response.data.message) {
      errorMsg = error.response.data.message;
    }
    // 使用Element Plus的消息通知组件
    ElMessage.error(errorMsg);
  } finally {
    submitting.value = false;
  }
};

// 取消报名对话框
const handleCancelRegistration = () => {
  cancelDialogVisible.value = true;
};

// 确认取消报名
const confirmCancelRegistration = async () => {
  if (!myRegistration.value) return;
  
  cancelling.value = true;
  try {
    await registrationAPI.cancelRegistration(myRegistration.value.id);
    
    // 关闭对话框
    cancelDialogVisible.value = false;
    
    // 更新报名状态
    myRegistration.value = null;
    
    // 显示成功提示
    successDialogTitle.value = '取消成功';
    successDialogMessage.value = '您已成功取消此赛事的报名';
    successDialogVisible.value = true;
  } catch (error: any) {
    let errorMsg = '取消报名失败，请稍后再试';
    if (error.response && error.response.data && error.response.data.message) {
      errorMsg = error.response.data.message;
    }
    ElMessage.error(errorMsg);
  } finally {
    cancelling.value = false;
  }
};

// 计算属性
// 是否可以报名
const isRegistrationOpen = computed(() => {
  if (!event.value) return false;
  
  const now = new Date();
  const deadline = new Date(event.value.registrationDeadline);
  
  // 检查赛事状态和截止日期
  return (
    event.value.status === 'UPCOMING' || event.value.status === 'ONGOING'
  ) && now <= deadline;
});

// 是否已达到人数上限
const isEventFull = computed(() => {
  // 假设后端会返回当前报名人数信息，如果没有，则这里需要调整
  return false;
});

// 报名关闭原因
const registrationClosedReason = computed(() => {
  if (!event.value) return '无法获取报名信息';
  
  if (event.value.status === 'COMPLETED') {
    return '此赛事已结束';
  } else if (event.value.status === 'CANCELLED') {
    return '此赛事已取消';
  } else {
    const now = new Date();
    const deadline = new Date(event.value.registrationDeadline);
    
    if (now > deadline) {
      return '报名已截止';
    } else if (isEventFull.value) {
      return '报名人数已达上限';
    }
  }
  
  return '报名暂未开放';
});

// 是否可以报名
const canRegister = computed(() => {
  return isRegistrationOpen.value && !myRegistration.value && !isEventFull.value;
});

// 是否可以取消报名
const canCancelRegistration = computed(() => {
  if (!myRegistration.value) return false;
  
  return ['PENDING', 'APPROVED'].includes(myRegistration.value.status);
});

// 辅助函数
// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'UPCOMING': return 'warning';
    case 'ONGOING': return 'success';
    case 'COMPLETED': return 'info';
    case 'CANCELLED': return 'danger';
    default: return '';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'UPCOMING': return '即将开始';
    case 'ONGOING': return '进行中';
    case 'COMPLETED': return '已结束';
    case 'CANCELLED': return '已取消';
    default: return status;
  }
};

// 获取报名状态类型
const getRegistrationStatusType = (status: string) => {
  switch (status) {
    case 'APPROVED': return 'success';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'danger';
    case 'CANCELLED': return 'info';
    default: return '';
  }
};

// 获取报名状态文本
const getRegistrationStatusText = (status: string) => {
  switch (status) {
    case 'APPROVED': return '已批准';
    case 'PENDING': return '待审核';
    case 'REJECTED': return '已拒绝';
    case 'CANCELLED': return '已取消';
    default: return status;
  }
};
</script>

<style scoped>
.event-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container, .not-found-container {
  padding: 40px 0;
}

.event-header {
  margin-bottom: 24px;
}

.breadcrumb {
  margin-bottom: 16px;
}

.title-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.event-title {
  margin: 0;
  font-size: 28px;
}

.event-body {
  margin-bottom: 40px;
}

.main-info {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
}

.event-details {
  margin-bottom: 24px;
}

.detail-item {
  display: flex;
  margin-bottom: 12px;
  align-items: flex-start;
}

.detail-label {
  width: 100px;
  color: #909399;
  font-weight: 500;
}

.detail-value {
  flex: 1;
}

.description-section h3 {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 18px;
}

.event-description {
  white-space: pre-line;
  line-height: 1.6;
}

.announcement-card {
  margin-top: 20px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.announcement-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
}

.announcement-date {
  font-size: 12px;
  color: #909399;
}

.announcement-content {
  white-space: pre-line;
  line-height: 1.5;
}

.registration-card {
  height: 100%;
}

.deadline-info {
  margin-bottom: 20px;
}

.my-registration {
  padding: 16px;
  border-radius: 4px;
  background-color: #f8f9fa;
  margin-bottom: 16px;
}

.registration-info h4 {
  margin-top: 0;
  margin-bottom: 12px;
}

.registration-date {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
}

.registration-notes {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.registration-actions {
  margin-top: 16px;
}

.loading-placeholder, .empty-data {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .title-section {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-item {
    flex-direction: column;
  }
  
  .detail-label {
    width: 100%;
    margin-bottom: 4px;
  }
}
</style> 