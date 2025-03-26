<template>
  <admin-layout>
    <div class="create-event">
      <div class="page-header">
        <h2>创建赛事</h2>
        <el-button @click="$router.push('/admin/events/list')">返回赛事列表</el-button>
      </div>
      
      <div class="event-form-container">
        <el-form 
          ref="eventFormRef"
          :model="eventForm"
          :rules="rules"
          label-width="120px"
          label-position="right"
          class="event-form"
        >
          <el-form-item label="赛事名称" prop="name">
            <el-input v-model="eventForm.name" placeholder="请输入赛事名称" />
          </el-form-item>
          
          <el-form-item label="赛事分类" prop="categoryId">
            <el-select v-model="eventForm.categoryId" placeholder="请选择赛事分类" style="width: 100%">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="赛事日期" prop="eventDates">
            <el-date-picker
              v-model="eventForm.eventDates"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="报名时间" prop="registrationDates">
            <el-date-picker
              v-model="eventForm.registrationDates"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="比赛地点" prop="location">
            <el-input v-model="eventForm.location" placeholder="请输入比赛地点" />
          </el-form-item>
          
          <el-form-item label="主办方" prop="organizer">
            <el-input v-model="eventForm.organizer" placeholder="请输入主办方" />
          </el-form-item>
          
          <el-form-item label="最大参与人数" prop="maxParticipants">
            <el-input-number 
              v-model="eventForm.maxParticipants" 
              :min="1" 
              :max="10000" 
              :step="1"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="赛事描述" prop="description">
            <el-input
              v-model="eventForm.description"
              type="textarea"
              :rows="6"
              placeholder="请输入赛事详细描述，包括赛事规则、奖励等信息"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting">创建赛事</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import { eventsAPI } from '../../../api/eventsAPI';
import { categoryAPI } from '../../../api/categoryAPI';
import AdminLayout from '../../../components/AdminLayout.vue';

interface EventCategory {
  id: number;
  name: string;
}

const router = useRouter();
const eventFormRef = ref<FormInstance>();
const submitting = ref(false);

// 分类数据
const categories = ref<EventCategory[]>([]);

// 赛事表单
const eventForm = reactive({
  name: '',
  categoryId: null as number | null,
  eventDates: [] as string[],
  registrationDates: [] as string[],
  location: '',
  organizer: '',
  maxParticipants: 100,
  description: ''
});

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入赛事名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应为2到50个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择赛事分类', trigger: 'change' }
  ],
  eventDates: [
    { required: true, message: '请选择赛事日期', trigger: 'change' }
  ],
  registrationDates: [
    { required: true, message: '请选择报名时间', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入比赛地点', trigger: 'blur' }
  ],
  organizer: [
    { required: true, message: '请输入主办方', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入赛事描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度应为10到2000个字符', trigger: 'blur' }
  ]
});

// 加载分类列表
const loadCategories = async () => {
  try {
    // 实际应用中调用API获取分类列表
    // const response = await categoryAPI.getAllCategories();
    // categories.value = response.data;
    
    // 使用模拟数据
    categories.value = [
      { id: 1, name: '田径赛事' },
      { id: 2, name: '球类赛事' },
      { id: 3, name: '水上赛事' },
      { id: 4, name: '冰雪赛事' },
      { id: 5, name: '格斗赛事' }
    ];
  } catch (error) {
    console.error('获取分类列表失败', error);
    ElMessage.error('获取分类列表失败');
  }
};

// 提交表单
const submitForm = async () => {
  if (!eventFormRef.value) return;

  await eventFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 处理日期
        const [startDate, endDate] = eventForm.eventDates;
        const [registrationStartDate, registrationEndDate] = eventForm.registrationDates;

        // 构建提交的数据
        const eventData = {
          name: eventForm.name,
          categoryId: eventForm.categoryId,
          startDate,
          endDate,
          registrationStartDate,
          registrationEndDate,
          location: eventForm.location,
          organizer: eventForm.organizer,
          maxParticipants: eventForm.maxParticipants,
          description: eventForm.description,
          status: 'NOT_STARTED'  // 默认状态为未开始
        };

        // 实际应用中调用API创建赛事
        // const response = await eventsAPI.createEvent(eventData);
        
        // 模拟成功
        setTimeout(() => {
          ElMessage.success('赛事创建成功');
          router.push('/admin/events/list');
          submitting.value = false;
        }, 1000);
      } catch (error) {
        console.error('创建赛事失败', error);
        ElMessage.error('创建赛事失败，请重试');
        submitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确');
      return false;
    }
  });
};

// 重置表单
const resetForm = () => {
  if (eventFormRef.value) {
    eventFormRef.value.resetFields();
  }
};

// 初始化加载
onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.create-event {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.event-form-container {
  background-color: #fff;
  padding: 30px;
  border-radius: 4px;
}

.event-form {
  max-width: 800px;
  margin: 0 auto;
}
</style> 