<template>
  <div class="registration-list-container">
    <div class="page-header">
      <h1>我的报名</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="registrations.length === 0" class="empty-container">
      <el-empty description="您还没有报名任何赛事">
        <template #extra>
          <router-link to="/user/events">
            <el-button type="primary">浏览赛事</el-button>
          </router-link>
        </template>
      </el-empty>
    </div>
    
    <div v-else>
      <el-card>
        <el-table 
          :data="registrations" 
          style="width: 100%" 
          :default-sort="{ prop: 'registrationTime', order: 'descending' }"
          row-key="id"
        >
          <el-table-column label="赛事名称" min-width="200">
            <template #default="scope">
              <router-link :to="`/user/events/${scope.row.event.id}`" class="event-link">
                {{ scope.row.event.name }}
              </router-link>
            </template>
          </el-table-column>
          
          <el-table-column prop="registrationTime" label="报名时间" min-width="180" sortable>
            <template #default="scope">
              {{ formatDate(scope.row.registrationTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="notes" label="备注" min-width="200">
            <template #default="scope">
              <span v-if="scope.row.notes">{{ scope.row.notes }}</span>
              <span v-else class="text-muted">无</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <div class="table-actions">
                <router-link :to="`/user/events/${scope.row.event.id}`">
                  <el-button type="primary" text>查看赛事</el-button>
                </router-link>
                
                <el-button 
                  v-if="canCancelRegistration(scope.row)"
                  type="danger" 
                  text
                  @click="handleCancelRegistration(scope.row)"
                >
                  取消报名
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 取消报名确认对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消报名确认"
      width="30%"
    >
      <span>您确定要取消 <strong>{{ selectedRegistration?.event?.name }}</strong> 的报名吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmCancelRegistration" :loading="cancelling">
            确认取消
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { registrationAPI } from '../../api/registrationAPI';
import type { Registration } from '../../api/registrationAPI';
import { ElMessage } from 'element-plus';

// 数据状态
const registrations = ref<Registration[]>([]);
const loading = ref(true);

// 取消报名相关
const selectedRegistration = ref<Registration | null>(null);
const cancelDialogVisible = ref(false);
const cancelling = ref(false);

// 初始化
onMounted(async () => {
  await fetchMyRegistrations();
});

// 获取我的报名列表
const fetchMyRegistrations = async () => {
  loading.value = true;
  try {
    const data = await registrationAPI.getMyRegistrations();
    registrations.value = data;
  } catch (error) {
    console.error('获取报名列表失败:', error);
    ElMessage.error('获取报名列表失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};

// 取消报名对话框
const handleCancelRegistration = (registration: Registration) => {
  selectedRegistration.value = registration;
  cancelDialogVisible.value = true;
};

// 确认取消报名
const confirmCancelRegistration = async () => {
  if (!selectedRegistration.value) return;
  
  cancelling.value = true;
  try {
    await registrationAPI.cancelRegistration(selectedRegistration.value.id);
    
    // 更新列表
    registrations.value = registrations.value.filter(
      reg => reg.id !== selectedRegistration.value?.id
    );
    
    // 提示信息
    ElMessage.success('已成功取消报名');
    
    // 关闭对话框
    cancelDialogVisible.value = false;
  } catch (error: any) {
    let errorMsg = '取消报名失败，请稍后再试';
    if (error.response && error.response.data && error.response.data.message) {
      errorMsg = error.response.data.message;
    }
    ElMessage.error(errorMsg);
  } finally {
    cancelling.value = false;
    selectedRegistration.value = null;
  }
};

// 判断是否可以取消报名
const canCancelRegistration = (registration: Registration) => {
  return ['PENDING', 'APPROVED'].includes(registration.status);
};

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
    case 'APPROVED': return 'success';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'danger';
    case 'CANCELLED': return 'info';
    default: return '';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
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
.registration-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
}

.event-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
}

.event-link:hover {
  text-decoration: underline;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.text-muted {
  color: #909399;
  font-style: italic;
}
</style> 