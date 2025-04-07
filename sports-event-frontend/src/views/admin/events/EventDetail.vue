<template>
  <div class="container mx-auto px-4 py-6">
    <div class="mb-6">
      <el-page-header @back="goBack" title="返回赛事列表">
        <template #content>
          <span class="text-lg font-medium">赛事详情</span>
        </template>
      </el-page-header>
    </div>

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
      :closable="true"
    />

    <!-- 赛事详情 -->
    <div v-if="!loading && event" class="bg-white rounded-lg shadow p-6">
      <div class="flex flex-col md:flex-row">
        <!-- 左侧信息 -->
        <div class="md:w-full pr-6">
          <h1 class="text-2xl font-bold mb-4">{{ event.name }}</h1>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="赛事ID">{{ event.id }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ event.category?.name }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(event.status)">
                {{ getStatusText(event.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="地点">{{ event.location }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDate(event.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDate(event.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="报名截止时间">{{ formatDate(event.registrationDeadline) }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              <p class="whitespace-pre-line">{{ event.description }}</p>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确定要删除赛事 "{{ event?.name }}" 吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deleteEvent">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import eventsAPI from '@/api/eventsAPI';
import type { Event } from '@/api/eventsAPI';

const router = useRouter();
const route = useRoute();
const event = ref<Event | null>(null);
const loading = ref(false);
const error = ref('');
const deleteDialogVisible = ref(false);

// 获取赛事详情
const loadEvent = async () => {
  const id = Number(route.params.id);
  if (!id) {
    error.value = '无效的赛事ID';
    return;
  }

  loading.value = true;
  error.value = '';
  
  try {
    event.value = await eventsAPI.getEventById(id);
  } catch (err: any) {
    console.error('加载赛事详情失败', err);
    error.value = err.response?.data?.message || '加载赛事详情失败，请重试';
  } finally {
    loading.value = false;
  }
};

// 状态格式化
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': 'warning',
    'ONGOING': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  };
  return map[status] || 'default';
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '即将开始',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
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

// 导航
const goBack = () => {
  router.push('/admin/events/list');
};


// 删除事件
const deleteEvent = async () => {
  if (!event.value) return;
  
  try {
    await eventsAPI.deleteEvent(event.value.id);
    ElMessage.success('赛事删除成功');
    deleteDialogVisible.value = false;
    router.push('/admin/events/list');
  } catch (err: any) {
    console.error('删除赛事失败', err);
    ElMessage.error(err.response?.data?.message || '删除赛事失败，请重试');
  }
};

onMounted(() => {
  loadEvent();
});
</script> 