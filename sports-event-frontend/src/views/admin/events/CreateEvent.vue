<template>
  <div class="container mx-auto px-4 py-6">
    <div class="mb-6">
      <el-page-header @back="goBack" title="返回赛事列表">
        <template #content>
          <span class="text-lg font-medium">创建新赛事</span>
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
      v-if="!loading"
      ref="formRef"
      :model="eventForm"
      :rules="rules"
      label-width="120px"
      label-position="top"
      class="max-w-3xl mx-auto"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      
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
      
      <el-form-item label="赛事图片" prop="eventImage">
        <el-input
          v-model="eventForm.eventImage"
          placeholder="请输入赛事图片URL"
        />
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <div class="flex justify-end space-x-4">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">创建赛事</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import eventsAPI from '@/api/eventsAPI';
import categoryAPI from '@/api/categoryAPI';
import type { EventCategory } from '@/types/event';

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const submitting = ref(false);
const error = ref('');
const categories = ref<EventCategory[]>([]);

// 表单数据
const eventForm = reactive({
  name: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  maxParticipants: 100,
  eventImage: '',
  category: null as null | number,
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
      validator: (rule, value, callback) => {
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
      validator: (rule, value, callback) => {
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
  ]
});

// 加载分类
const loadCategories = async () => {
  loading.value = true;
  error.value = '';
  try {
    categories.value = await categoryAPI.getAllCategories();
  } catch (err) {
    console.error('加载分类失败', err);
    error.value = '加载分类数据失败，请重试';
    
    // 如果管理员API失败，尝试使用公共API
    try {
      categories.value = await categoryAPI.getPublicCategories();
      if (categories.value.length > 0) {
        error.value = '';
      }
    } catch (publicErr) {
      console.error('加载公共分类也失败', publicErr);
    }
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
        const eventToCreate = {
          name: eventForm.name,
          description: eventForm.description,
          location: eventForm.location,
          startTime: eventForm.startTime,
          endTime: eventForm.endTime,
          registrationDeadline: eventForm.registrationDeadline,
          maxParticipants: eventForm.maxParticipants,
          eventImage: eventForm.eventImage || null,
          category: { id: eventForm.category } as any
        };
        
        const result = await eventsAPI.createEvent(eventToCreate);
        ElMessage.success('赛事创建成功！');
        router.push('/admin/events');
      } catch (err: any) {
        console.error('创建赛事失败', err);
        error.value = err.response?.data?.message || '创建赛事失败，请重试';
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
  router.push('/admin/events');
};

onMounted(() => {
  loadCategories();
});
</script>
