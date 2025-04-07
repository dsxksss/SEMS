<template>
  <div class="container mx-auto px-4 py-6">
    <div class="mb-6">
      <el-page-header @back="goBack" title="返回赛事列表">
        <template #content>
          <span class="text-lg font-medium">赛事参与者管理</span>
        </template>
      </el-page-header>
    </div>

    <!-- 事件信息卡片 -->
    <el-card v-if="event" class="mb-6">
      <template #header>
        <div class="flex items-center justify-between">
          <h2 class="text-xl font-bold">{{ event.name }}</h2>
          <el-tag :type="getStatusType(event.status)">{{ getStatusText(event.status) }}</el-tag>
        </div>
      </template>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <p><strong>时间：</strong>{{ formatDate(event.startTime) }} 至 {{ formatDate(event.endTime) }}</p>
          <p><strong>地点：</strong>{{ event.location }}</p>
          <p><strong>分类：</strong>{{ event.category?.name }}</p>
        </div>
        <div>
          <p><strong>参与人数：</strong>{{ event.currentParticipants || 0 }}/{{ event.maxParticipants }}</p>
          <p><strong>报名截止：</strong>{{ formatDate(event.registrationDeadline) }}</p>
          <p v-if="isRegistrationOpen"><strong class="text-green-600">报名进行中</strong></p>
          <p v-else><strong class="text-red-600">报名已结束</strong></p>
        </div>
      </div>
    </el-card>

    <!-- 加载状态 -->
    <div v-if="loading" class="py-10 text-center">
      <el-spinner size="large" />
    </div>

    <!-- 错误提示 -->
    <el-alert
      v-if="error"
      type="error"
      :title="error"
      show-icon
      class="mb-4"
    />

    <!-- 搜索栏 -->
    <div class="mb-6 flex gap-4">
      <el-input
        v-model="searchQuery"
        placeholder="搜索参与者"
        class="w-64"
        clearable
        @clear="loadParticipants"
      >
        <template #suffix>
          <el-icon @click="loadParticipants"><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="statusFilter" placeholder="状态筛选" class="w-40" @change="loadParticipants">
        <el-option label="全部" value="" />
        <el-option label="已报名" value="REGISTERED" />
        <el-option label="已签到" value="CHECKED_IN" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>

    <!-- 参与者列表 -->
    <el-table
      v-if="!loading && participants.length > 0"
      :data="participants"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="参与者" min-width="160">
        <template #default="scope">
          <div class="flex items-center">
            <el-avatar :size="32" :src="scope.row.user.avatar || ''" class="mr-2">
              {{ scope.row.user.username.charAt(0).toUpperCase() }}
            </el-avatar>
            <div>
              <div class="font-medium">{{ scope.row.user.username }}</div>
              <div class="text-xs text-gray-500">{{ scope.row.user.email }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="registrationTime" label="注册时间" width="160">
        <template #default="scope">
          {{ formatDate(scope.row.registrationTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getParticipantStatusType(scope.row.status)">
            {{ getParticipantStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="checkInTime" label="签到时间" width="160">
        <template #default="scope">
          {{ scope.row.checkInTime ? formatDate(scope.row.checkInTime) : '未签到' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <div class="flex space-x-2">
            <el-button 
              v-if="scope.row.status === 'REGISTERED'"
              size="small" 
              type="primary" 
              @click="checkInParticipant(scope.row)"
            >
              签到
            </el-button>
            <el-button 
              v-if="scope.row.status !== 'CANCELLED'"
              size="small" 
              type="danger" 
              @click="confirmCancel(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              size="small" 
              @click="showNotes(scope.row)"
            >
              备注
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空数据提示 -->
    <el-empty v-if="!loading && participants.length === 0" description="暂无参与者数据" />

    <!-- 分页 -->
    <div class="mt-4 flex justify-end" v-if="!loading && totalParticipants > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :layout="'total, sizes, prev, pager, next, jumper'"
        :total="totalParticipants"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 备注对话框 -->
    <el-dialog
      v-model="notesDialogVisible"
      title="参与者备注"
      width="30%"
    >
      <el-form v-if="selectedParticipant">
        <el-form-item label="参与者">
          {{ selectedParticipant.user?.username }}
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="participantNotes"
            type="textarea"
            rows="4"
            placeholder="输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="notesDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNotes" :loading="submitting">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 取消确认对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="确认取消"
      width="30%"
    >
      <span>确定要取消 {{ selectedParticipant?.user?.username }} 的参与资格吗？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="cancelParticipation" :loading="submitting">确认取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import eventsAPI from '@/api/eventsAPI';
import registrationAPI from '@/api/registrationAPI';
import type { Event } from '@/api/eventsAPI';
import type { Participant, ParticipantQueryParams } from '@/api/registrationAPI';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const submitting = ref(false);
const error = ref('');
const event = ref<Event | null>(null);
const participants = ref<Participant[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalParticipants = ref(0);
const searchQuery = ref('');
const statusFilter = ref('');
const notesDialogVisible = ref(false);
const cancelDialogVisible = ref(false);
const selectedParticipant = ref<Participant | null>(null);
const participantNotes = ref('');

// 获取事件ID
const eventId = parseInt(route.params.id as string);

// 计算报名是否开放
const isRegistrationOpen = computed(() => {
  if (!event.value || !event.value.registrationDeadline) return false;
  
  const now = new Date();
  const deadline = new Date(event.value.registrationDeadline);
  
  return now <= deadline && 
         event.value.status !== 'CANCELLED' && 
         (event.value.currentParticipants || 0) < event.value.maxParticipants;
});

// 加载事件信息
const loadEvent = async () => {
  try {
    event.value = await eventsAPI.getEventById(eventId);
  } catch (err) {
    console.error('加载事件数据失败', err);
    error.value = '加载赛事数据失败，请重试';
  }
};

// 加载参与者
const loadParticipants = async () => {
  loading.value = true;
  error.value = '';
  try {
    // 首先加载事件信息（如果还没有加载）
    if (!event.value) {
      await loadEvent();
    }
    
    // 然后加载参与者
    const queryParams: ParticipantQueryParams = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: statusFilter.value,
      search: searchQuery.value
    };
    
    const response = await registrationAPI.getEventParticipants(eventId, queryParams);
    
    participants.value = response.content;
    totalParticipants.value = response.totalElements;
  } catch (err) {
    console.error('加载参与者数据失败', err);
    error.value = '加载参与者数据失败，请重试';
    participants.value = [];
  } finally {
    loading.value = false;
  }
};

// 分页处理
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize;
  loadParticipants();
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
  loadParticipants();
};

// 状态格式化
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'UPCOMING': 'warning',
    'ONGOING': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  };
  return map[status] || 'default';
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'UPCOMING': '即将开始',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  };
  return map[status] || status;
};

const getParticipantStatusType = (status: string) => {
  const map: Record<string, string> = {
    'REGISTERED': 'warning',
    'CHECKED_IN': 'success',
    'CANCELLED': 'danger'
  };
  return map[status] || 'default';
};

const getParticipantStatusText = (status: string) => {
  const map: Record<string, string> = {
    'REGISTERED': '已报名',
    'CHECKED_IN': '已签到',
    'CANCELLED': '已取消'
  };
  return map[status] || status;
};

// 日期格式化
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 签到参与者
const checkInParticipant = async (participant: Participant) => {
  submitting.value = true;
  try {
    await registrationAPI.checkInParticipant(participant.id);
    ElMessage.success('签到成功！');
    // 重新加载数据
    loadParticipants();
  } catch (err) {
    console.error('签到失败', err);
    ElMessage.error('签到失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 显示备注对话框
const showNotes = (participant: Participant) => {
  selectedParticipant.value = participant;
  participantNotes.value = participant.notes || '';
  notesDialogVisible.value = true;
};

// 保存备注
const saveNotes = async () => {
  if (!selectedParticipant.value) return;
  
  submitting.value = true;
  try {
    await registrationAPI.updateParticipantNotes(
      selectedParticipant.value.id, 
      participantNotes.value
    );
    
    ElMessage.success('备注已保存');
    notesDialogVisible.value = false;
    
    // 更新本地数据
    const index = participants.value.findIndex(p => p.id === selectedParticipant.value?.id);
    if (index !== -1) {
      participants.value[index].notes = participantNotes.value;
    }
  } catch (err) {
    console.error('保存备注失败', err);
    ElMessage.error('保存备注失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 显示取消确认对话框
const confirmCancel = (participant: Participant) => {
  selectedParticipant.value = participant;
  cancelDialogVisible.value = true;
};

// 取消参与
const cancelParticipation = async () => {
  if (!selectedParticipant.value) return;
  
  submitting.value = true;
  try {
    await registrationAPI.cancelParticipation(selectedParticipant.value.id);
    ElMessage.success('已取消参与者资格');
    cancelDialogVisible.value = false;
    
    // 重新加载数据
    loadParticipants();
  } catch (err) {
    console.error('取消参与者资格失败', err);
    ElMessage.error('取消参与者资格失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 返回列表
const goBack = () => {
  router.push('/admin/events/list');
};

onMounted(() => {
  if (!eventId || isNaN(eventId)) {
    error.value = '无效的赛事ID';
    return;
  }
  
  loadParticipants();
});
</script>
