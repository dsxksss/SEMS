<template>
  <div class="container mx-auto px-4 py-6">
    <div class="mb-6">
      <el-page-header @back="goBack" title="返回赛事列表">
        <template #content>
          <span class="text-lg font-medium">编辑赛事</span>
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

    <!-- 赛事表单 -->
    <el-form
      v-if="!loading && eventLoaded"
      ref="formRef"
      :model="eventForm"
      :rules="rules"
      label-width="120px"
      label-position="top"
      class="max-w-3xl mx-auto"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      
      <el-form-item label="赛事ID" prop="id">
        <el-input v-model="eventForm.id" disabled />
      </el-form-item>

      <el-form-item label="赛事名称" prop="name">
        <el-input v-model="eventForm.name" placeholder="请输入赛事名称" />
      </el-form-item>
      
      <el-form-item label="赛事分类" prop="category">
        <el-select
          v-model="eventForm.category"
          placeholder="请选择赛事分类"
          class="w-full"
          filterable
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          >
            <span>{{ category.name }}</span>
            <span class="text-gray-400 text-xs ml-2">{{ category.description }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="赛事描述" prop="description">
        <el-input
          v-model="eventForm.description"
          type="textarea"
          rows="4"
          placeholder="请输入赛事描述"
        />
      </el-form-item>
      
      <el-form-item label="赛事地点" prop="location">
        <el-input v-model="eventForm.location" placeholder="请输入赛事地点" />
      </el-form-item>

      <!-- 时间信息 -->
      <el-divider content-position="left">时间信息</el-divider>
      
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="eventForm.startTime"
          type="datetime"
          placeholder="选择开始时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="w-full"
        />
      </el-form-item>
      
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker
          v-model="eventForm.endTime"
          type="datetime"
          placeholder="选择结束时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="w-full"
        />
      </el-form-item>
      
      <el-form-item label="报名截止时间" prop="registrationDeadline">
        <el-date-picker
          v-model="eventForm.registrationDeadline"
          type="datetime"
          placeholder="选择报名截止时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="w-full"
        />
      </el-form-item>

      <!-- 参与信息 -->
      <el-divider content-position="left">参与信息</el-divider>
      
      <el-form-item label="最大参与人数" prop="maxParticipants">
        <el-input-number 
          v-model="eventForm.maxParticipants" 
          :min="1" 
          :max="10000"
          class="w-full"
        />
      </el-form-item>
      
      <el-form-item label="当前参与人数" prop="currentParticipants">
        <el-input-number 
          v-model="eventForm.currentParticipants" 
          :min="0" 
          :max="eventForm.maxParticipants"
          class="w-full"
          disabled
        />
      </el-form-item>

      <el-form-item label="赛事状态" prop="status">
        <el-select v-model="eventForm.status" placeholder="请选择赛事状态" class="w-full">
          <el-option
            v-for="status in ['UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED']"
            :key="status"
            :label="{
              'UPCOMING': '即将开始',
              'ONGOING': '进行中',
              'COMPLETED': '已完成',
              'CANCELLED': '已取消'
            }[status]"
            :value="status"
          ></el-option>
        </el-select>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <div class="flex justify-end space-x-4">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">保存修改</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import eventsAPI from '@/api/eventsAPI';
import categoryAPI from '@/api/categoryAPI';
import type { EventCategory } from '@/types/event';

const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();
const loading = ref(true);
const submitting = ref(false);
const error = ref('');
const categories = ref<EventCategory[]>([]);
const eventLoaded = ref(false);

// 获取事件ID
const eventId = parseInt(route.params.id as string);

// 表单数据
const eventForm = reactive({
  id: 0,
  name: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  maxParticipants: 100,
  currentParticipants: 0,
  status: 'UPCOMING' as 'UPCOMING' | 'ONGOING' | 'COMPLETED' | 'CANCELLED',
  category: null as null | number,
  isActive: true
});

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入赛事名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入赛事描述', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入赛事地点', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (value && eventForm.startTime && new Date(value) <= new Date(eventForm.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  registrationDeadline: [
    { required: true, message: '请选择报名截止时间', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (value && eventForm.startTime && new Date(value) >= new Date(eventForm.startTime)) {
          callback(new Error('报名截止时间必须早于开始时间'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '参与人数必须大于0', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择赛事分类', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择赛事状态', trigger: 'change' }
  ]
});

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

// 加载事件数据
const loadEvent = async () => {
  loading.value = true;
  error.value = '';
  try {
    // 首先加载分类
    await loadCategories();
    
    // 然后加载事件
    const event = await eventsAPI.getEventById(eventId);
    
    // 填充表单
    eventForm.id = event.id;
    eventForm.name = event.name;
    eventForm.description = event.description;
    eventForm.location = event.location;
    eventForm.startTime = event.startTime;
    eventForm.endTime = event.endTime;
    eventForm.registrationDeadline = event.registrationDeadline;
    eventForm.maxParticipants = event.maxParticipants;
    eventForm.currentParticipants = 0; // 默认值，因为API不提供该字段
    eventForm.status = event.status;
    eventForm.category = event.category.id;
    eventForm.isActive = event.isActive;
    
    eventLoaded.value = true;
  } catch (err: any) {
    console.error('加载事件数据失败', err);
    error.value = err.response?.data?.message || '加载赛事数据失败，请重试';
  } finally {
    loading.value = false;
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitting.value = true;
      error.value = '';
      
      try {
        // 构建要发送的事件对象
        const eventToUpdate = {
          name: eventForm.name,
          description: eventForm.description,
          location: eventForm.location,
          startTime: eventForm.startTime,
          endTime: eventForm.endTime,
          registrationDeadline: eventForm.registrationDeadline,
          maxParticipants: eventForm.maxParticipants,
          status: eventForm.status,
          isActive: eventForm.isActive,
          category: { id: eventForm.category } as any
        };
        
        await eventsAPI.updateEvent(eventId, eventToUpdate);
        ElMessage.success('赛事更新成功！');
        router.push('/admin/events/list');
      } catch (err: any) {
        console.error('更新赛事失败', err);
        error.value = err.response?.data?.message || '更新赛事失败，请重试';
      } finally {
        submitting.value = false;
      }
    } else {
      console.log('表单验证失败', fields);
    }
  });
};

// 返回列表
const goBack = () => {
  router.push('/admin/events/list');
};

onMounted(() => {
  if (!eventId || isNaN(eventId)) {
    error.value = '无效的赛事ID';
    loading.value = false;
    return;
  }
  
  loadEvent();
});
</script>
