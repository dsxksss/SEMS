<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">赛事管理</h1>
      <el-button type="primary" @click="navigateToCreate">
        <el-icon><Plus /></el-icon> 创建新赛事
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="mb-6 flex flex-wrap gap-4">
      <el-input
        v-model="searchQuery"
        placeholder="搜索赛事名称"
        class="w-64"
        clearable
        @clear="loadEvents"
      >
        <template #suffix>
          <el-icon @click="loadEvents"><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="statusFilter" placeholder="状态筛选" class="w-40" @change="loadEvents">
        <el-option label="全部" value="" />
        <el-option label="即将开始" value="PENDING" />
        <el-option label="进行中" value="ONGOING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>

      <el-select v-model="categoryFilter" placeholder="分类筛选" class="w-40" @change="loadEvents">
        <el-option label="全部" value="" />
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
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
    />

    <!-- 事件列表 -->
    <el-table
      v-if="!loading && events.length > 0"
      :data="events"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="200">
        <template #default="scope">
          <div class="font-medium">{{ scope.row.name }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="category.name" label="分类" width="120" />
      <el-table-column label="时间" width="200">
        <template #default="scope">
          <div>{{ formatDate(scope.row.startTime) }}</div>
          <div class="text-xs text-gray-500">至 {{ formatDate(scope.row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="地点" width="150" />
      <el-table-column label="参与人数" width="120" align="center">
        <template #default="scope">
          <div>{{ scope.row.currentParticipants || 0 }}/{{ scope.row.maxParticipants }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <div class="flex space-x-2">
            <el-button size="small" @click="navigateToDetail(scope.row.id)">
              查看
            </el-button>
            <el-button size="small" type="primary" @click="navigateToEdit(scope.row.id)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              @click="navigateToParticipants(scope.row.id)"
            >
              参与者
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="confirmDelete(scope.row)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空数据提示 -->
    <el-empty v-if="!loading && events.length === 0" description="暂无赛事数据" />

    <!-- 分页 -->
    <div class="mt-4 flex justify-end" v-if="!loading && totalEvents > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :layout="'total, sizes, prev, pager, next, jumper'"
        :total="totalEvents"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确定要删除赛事 "{{ eventToDelete?.name }}" 吗？此操作不可恢复。</span>
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
import { useRouter } from 'vue-router';
import { Plus, Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import eventsAPI from '@/api/eventsAPI';
import categoryAPI from '@/api/categoryAPI';
import type { Event } from '@/api/eventsAPI';
import type { EventCategory } from '@/types/event';

const router = useRouter();
const events = ref<Event[]>([]);
const loading = ref(false);
const error = ref('');
const categories = ref<EventCategory[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalEvents = ref(0);
const searchQuery = ref('');
const statusFilter = ref('');
const categoryFilter = ref('');
const deleteDialogVisible = ref(false);
const eventToDelete = ref<Event | null>(null);

// 加载分类
const loadCategories = async () => {
  try {
    categories.value = await categoryAPI.getAllCategories();
  } catch (err) {
    console.error('加载分类失败', err);
    // 如果管理员API失败，尝试使用公共API
    try {
      categories.value = await categoryAPI.getPublicCategories();
    } catch (publicErr) {
      console.error('加载公共分类也失败', publicErr);
    }
  }
};

// 加载事件
const loadEvents = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await eventsAPI.getAllEvents();
    events.value = response;
    totalEvents.value = response.length;
    
    // 应用搜索过滤
    if (searchQuery.value) {
      events.value = events.value.filter(event => 
        event.name.toLowerCase().includes(searchQuery.value.toLowerCase())
      );
    }
    
    // 应用状态过滤
    if (statusFilter.value) {
      events.value = events.value.filter(event => event.status === statusFilter.value);
    }
    
    // 应用分类过滤
    if (categoryFilter.value) {
      events.value = events.value.filter(event => 
        event.category.id === parseInt(categoryFilter.value)
      );
    }
    
    totalEvents.value = events.value.length;
    
    // 手动分页
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    events.value = events.value.slice(start, end);
  } catch (err) {
    console.error('加载事件失败', err);
    error.value = '加载赛事数据失败，请重试';
    events.value = [];
  } finally {
    loading.value = false;
  }
};

// 分页处理
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize;
  loadEvents();
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
  loadEvents();
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
const navigateToCreate = () => {
  router.push('/admin/events/create');
};

const navigateToEdit = (id: number) => {
  router.push(`/admin/events/edit/${id}`);
};

const navigateToDetail = (id: number) => {
  router.push(`/admin/events/${id}`);
};

const navigateToParticipants = (id: number) => {
  router.push(`/admin/events/${id}/participants`);
};

// 删除事件
const confirmDelete = (event: Event) => {
  eventToDelete.value = event;
  deleteDialogVisible.value = true;
};

const deleteEvent = async () => {
  if (!eventToDelete.value) return;
  
  try {
    await eventsAPI.deleteEvent(eventToDelete.value.id);
    ElMessage.success('赛事删除成功');
    deleteDialogVisible.value = false;
    loadEvents(); // 重新加载列表
  } catch (err) {
    console.error('删除赛事失败', err);
    ElMessage.error('删除赛事失败，请重试');
  }
};

onMounted(() => {
  loadEvents();
  loadCategories();
});
</script>
