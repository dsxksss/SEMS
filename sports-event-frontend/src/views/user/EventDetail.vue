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
                <div class="detail-value">
                  <span :class="{'deadline-warning': isDeadlineApproaching}">
                    {{ formatDate(event.registrationDeadline) }}
                    <el-tag v-if="isDeadlineApproaching" size="small" type="warning">即将截止</el-tag>
                  </span>
                </div>
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
                  <div class="title-wrapper">
                    <h4 class="announcement-title">{{ announcement.title }}</h4>
                    <el-tag v-if="isNewAnnouncement(announcement.createdAt)" size="small" type="danger">新</el-tag>
                  </div>
                  <span class="announcement-date">{{ formatDate(announcement.createdAt) }}</span>
                </div>
                <div class="announcement-content" v-html="processContentForPreview(announcement.content)"></div>
                <div class="announcement-footer">
                  <span v-if="announcement.createdBy" class="publisher">发布者：{{ getPublisherName(announcement.createdBy) }}</span>
                </div>
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
import type { Event } from '../../api/eventsAPI';
import { announcementAPI } from '../../api/announcementAPI';
import type { Announcement } from '../../api/announcementAPI';
import { registrationAPI } from '../../api/registrationAPI';
import type { Registration } from '../../api/registrationAPI';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();

// 数据状态
const event = ref<Event | null>(null);
const loading = ref(true);
const eventAnnouncements = ref<Announcement[]>([]);
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
    
    // 数据处理和验证
    if (Array.isArray(data) && data.length > 0) {
      eventAnnouncements.value = data.map(announcement => {
        // 处理内容中的图片URL
        let processedContent = announcement.content || '';
        if (processedContent) {
          // 查找所有图片URL，确保路径正确
          const imgRegex = /<img[^>]+src="([^"]+)"/g;
          processedContent = processedContent.replace(imgRegex, (match, url) => {
            // 移除URL中可能存在的token参数
            let cleanUrl = url.split('?')[0];
            
            // 确保图片URL包含/api前缀
            if (cleanUrl.includes('/files/download/') && !cleanUrl.startsWith('/api')) {
              cleanUrl = `/api${cleanUrl}`;
            }
            
            return match.replace(url, cleanUrl);
          });
        }
        
        // 处理可能存在的日期问题和数据完整性
        return {
          ...announcement,
          id: announcement.id || Date.now(), // 确保有ID用于v-for的key
          title: announcement.title || '无标题公告',
          content: processedContent || '无内容',
          createdAt: announcement.createdAt || new Date().toISOString(),
          createdBy: announcement.createdBy || '系统'
        };
      });
    } else {
      console.warn('获取到的公告数据为空或格式不正确', data);
      eventAnnouncements.value = [];
    }
  } catch (error) {
    console.error('获取赛事公告失败:', error);
    eventAnnouncements.value = [];
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
  if (!event.value || !event.value.currentParticipants) return false;
  return event.value.currentParticipants >= event.value.maxParticipants;
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

// 判断是否临近截止时间（24小时内）
const isDeadlineApproaching = computed(() => {
  if (!event.value || !event.value.registrationDeadline) return false;
  
  try {
    const deadline = new Date(event.value.registrationDeadline);
    
    // 检查日期是否有效
    if (isNaN(deadline.getTime())) {
      return false;
    }
    
    const now = new Date();
    const oneDayLater = new Date(now.getTime() + 24 * 60 * 60 * 1000);
    
    // 如果截止时间在当前时间和24小时后之间，则显示警告
    return now < deadline && deadline < oneDayLater;
  } catch (error) {
    console.error('截止时间计算错误:', error);
    return false;
  }
});

// 辅助函数
// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '数据加载中';
  
  try {
    const date = new Date(dateString);
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.warn(`无效的日期格式: ${dateString}`);
      return '数据加载中';
    }
    
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    console.error(`日期格式化错误: ${error}`, dateString);
    return '数据加载中';
  }
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

// 新增公告判断
const isNewAnnouncement = (createdAt: string) => {
  if (!createdAt) return false;
  
  try {
    const createdDate = new Date(createdAt);
    
    // 检查日期是否有效
    if (isNaN(createdDate.getTime())) {
      console.warn(`无效的日期格式: ${createdAt}`);
      return false;
    }
    
    const now = new Date();
    const threeDaysAgo = new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000);
    return createdDate > threeDaysAgo;
  } catch (error) {
    console.error(`日期比较错误: ${error}`, createdAt);
    return false;
  }
};

interface Publisher {
  realName?: string;
  username?: string;
  [key: string]: any;
}

// 获取发布者名称
const getPublisherName = (publisher: string | Publisher | undefined): string => {
  // 如果未定义
  if (!publisher) return '系统';
  
  // 如果是字符串，可能已经是姓名
  if (typeof publisher === 'string') {
    // 尝试解析JSON字符串
    try {
      const obj = JSON.parse(publisher) as Publisher;
      if (obj.realName) {
        return obj.realName;
      } else if (obj.username) {
        return obj.username;
      }
      // 无法解析有效信息，返回原字符串
      return publisher;
    } catch (e) {
      // 不是有效JSON，直接返回原字符串
      return publisher;
    }
  }
  
  // 如果是对象，直接获取属性
  return publisher.realName || publisher.username || '系统';
};

// 处理内容预览，保留图片并截断文本
const processContentForPreview = (content: string) => {
  if (!content) return '';
  
  // 先处理图片URL，确保路径正确
  let processedContent = content;
  const imgRegex = /<img[^>]+src="([^"]+)"/g;
  processedContent = processedContent.replace(imgRegex, (match, url) => {
    // 移除URL中可能存在的token参数
    let cleanUrl = url.split('?')[0];
    
    // 确保图片URL包含/api前缀
    if (cleanUrl.includes('/files/download/') && !cleanUrl.startsWith('/api')) {
      cleanUrl = `/api${cleanUrl}`;
    }
    
    return match.replace(url, cleanUrl);
  });
  
  return processedContent;
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
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
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
  color: #303133;
}

.event-body {
  margin-bottom: 40px;
}

.main-info, .registration-card, .announcement-card {
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
}

.main-info:hover, .registration-card:hover, .announcement-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8fafc;
  padding: 12px 20px;
}

.card-header h2, .card-header h3 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.event-details {
  margin-bottom: 24px;
  padding: 16px 0;
}

.detail-item {
  display: flex;
  margin-bottom: 14px;
  align-items: flex-start;
}

.detail-label {
  width: 100px;
  color: #606266;
  font-weight: 500;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.description-section {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.description-section h3 {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 16px;
  color: #303133;
}

.event-description {
  white-space: pre-line;
  line-height: 1.6;
  color: #606266;
}

.announcement-card {
  margin-top: 24px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  padding: 16px;
  border-radius: 8px;
  background-color: #f9f9f9;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.announcement-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.announcement-date {
  font-size: 13px;
  color: #909399;
}

.announcement-content {
  font-size: 14px;
  line-height: 1.6;
  margin: 10px 0;
  word-break: break-word;
}

/* 确保图片在查看时正常显示 */
.announcement-content img {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}

.publisher {
  font-style: italic;
}

.empty-data {
  padding: 30px 0;
}

.loading-placeholder {
  padding: 20px 0;
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

@media (max-width: 768px) {
  .title-section {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-label {
    width: 100%;
  }
}

.deadline-warning {
  color: #e6a23c;
  font-weight: 600;
}
</style> 