<template>
  <div>
    <Header />
    <div class="container mx-auto px-4 py-12">
      <div class="max-w-2xl mx-auto">
        <div class="mb-8 text-center">
          <h1 class="text-3xl font-bold text-gray-800 mb-2">观众注册</h1>
          <p class="text-gray-600">成为认证观众，提前预订座位，参与互动，享受更好的观赛体验</p>
        </div>
        
        <el-card shadow="hover">
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton :rows="6" animated />
          </div>
          
          <div v-else-if="registered" class="py-8 text-center">
            <el-result
              icon="success"
              title="注册成功"
              sub-title="您已成功注册为认证观众，可以参与更多观赛活动"
            >
              <template #extra>
                <div class="flex flex-col md:flex-row justify-center gap-4">
                  <el-button type="primary" @click="$router.push('/profile')">查看个人资料</el-button>
                  <el-button @click="$router.push('/events')">浏览赛事活动</el-button>
                </div>
              </template>
            </el-result>
          </div>
          
          <div v-else>
            <el-form 
              ref="formRef" 
              :model="form" 
              :rules="rules" 
              label-position="top"
              class="p-4"
            >
              <div class="mb-8">
                <h2 class="text-xl font-bold mb-4 border-b pb-2">基本信息</h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="form.realName" placeholder="请输入您的真实姓名" />
                  </el-form-item>
                  
                  <el-form-item label="身份证号" prop="idNumber">
                    <el-input v-model="form.idNumber" placeholder="请输入您的身份证号" />
                  </el-form-item>
                  
                  <el-form-item label="性别" prop="gender">
                    <el-radio-group v-model="form.gender">
                      <el-radio label="MALE">男</el-radio>
                      <el-radio label="FEMALE">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  
                  <el-form-item label="年龄段" prop="ageGroup">
                    <el-select
                      v-model="form.ageGroup"
                      placeholder="请选择您的年龄段"
                      style="width: 100%"
                    >
                      <el-option label="18岁以下" value="UNDER_18" />
                      <el-option label="18-25岁" value="18_25" />
                      <el-option label="26-35岁" value="26_35" />
                      <el-option label="36-45岁" value="36_45" />
                      <el-option label="46-55岁" value="46_55" />
                      <el-option label="56岁以上" value="OVER_55" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入您的手机号码" />
                  </el-form-item>
                  
                  <el-form-item label="邮箱地址" prop="email">
                    <el-input v-model="form.email" placeholder="请输入您的邮箱地址" />
                  </el-form-item>
                </div>
                
                <el-form-item label="常住城市" prop="city">
                  <el-cascader
                    v-model="form.city"
                    :options="cityOptions"
                    placeholder="请选择您的常住城市"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              
              <div class="mb-8">
                <h2 class="text-xl font-bold mb-4 border-b pb-2">偏好设置</h2>
                <el-form-item label="感兴趣的运动类型" prop="interestedSports">
                  <el-select
                    v-model="form.interestedSports"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    placeholder="请选择您感兴趣的运动类型"
                    style="width: 100%"
                  >
                    <el-option 
                      v-for="item in sportOptions" 
                      :key="item.value" 
                      :label="item.label" 
                      :value="item.value" 
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="座位偏好" prop="seatPreference">
                  <el-radio-group v-model="form.seatPreference">
                    <el-radio label="FRONT">前排</el-radio>
                    <el-radio label="MIDDLE">中间</el-radio>
                    <el-radio label="BACK">后排</el-radio>
                    <el-radio label="ANY">无特殊要求</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="通知偏好" prop="notificationPreferences">
                  <el-checkbox-group v-model="form.notificationPreferences">
                    <el-checkbox label="EMAIL">邮件通知</el-checkbox>
                    <el-checkbox label="SMS">短信通知</el-checkbox>
                    <el-checkbox label="PUSH">应用推送</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                
                <el-form-item label="特殊需求(可选)" prop="specialRequirements">
                  <el-input
                    v-model="form.specialRequirements"
                    type="textarea"
                    rows="3"
                    placeholder="如有特殊需求请说明，如无障碍设施等"
                  />
                </el-form-item>
              </div>
              
              <div class="mb-6">
                <el-form-item prop="agreement">
                  <el-checkbox v-model="form.agreement">
                    我已阅读并同意<a href="#" class="text-blue-500" @click.prevent="showAgreement = true">《观众服务协议》</a>
                  </el-checkbox>
                </el-form-item>
              </div>
              
              <div class="flex justify-center">
                <el-button 
                  type="primary" 
                  size="large" 
                  :loading="submitting" 
                  @click="submitForm"
                >
                  提交注册
                </el-button>
              </div>
            </el-form>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 协议对话框 -->
    <el-dialog
      v-model="showAgreement"
      title="观众服务协议"
      width="500px"
    >
      <div class="agreement-content">
        <h3 class="font-bold mb-2">一、总则</h3>
        <p class="mb-4">本协议是观众与平台之间关于观众注册及参与观赛活动的协议。通过注册成为认证观众，表示您已经阅读并同意本协议的所有条款。</p>
        
        <h3 class="font-bold mb-2">二、观众资格</h3>
        <p class="mb-4">任何人士均可申请成为认证观众，平台将根据申请人提供的信息进行审核，并保留最终审批权。未成年人应当在监护人同意下进行注册。</p>
        
        <h3 class="font-bold mb-2">三、信息真实性</h3>
        <p class="mb-4">观众承诺提供的所有注册信息均为真实、准确、完整的资料。如提供虚假信息，平台有权取消观众资格。</p>
        
        <h3 class="font-bold mb-2">四、观赛服务</h3>
        <p class="mb-4">认证观众可以享受预定座位、赛事提醒、专属优惠等服务，但需遵守具体赛事的观赛规则和要求，服从赛事组织方的安排。</p>
        
        <h3 class="font-bold mb-2">五、隐私保护</h3>
        <p class="mb-4">平台将严格保护观众的个人隐私，不会未经许可向第三方泄露个人资料，但法律法规另有规定的除外。</p>
        
        <h3 class="font-bold mb-2">六、协议修改</h3>
        <p class="mb-4">平台保留修改本协议的权利，修改后的协议将通过网站公告的形式通知观众。观众继续使用平台服务表示接受修改后的协议。</p>
      </div>
      
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="showAgreement = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import Header from '@/components/Header.vue'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

// 页面状态
const loading = ref(true)
const submitting = ref(false)
const registered = ref(false)
const showAgreement = ref(false)

// 表单引用
const formRef = ref(null)

// 表单数据
const form = reactive({
  realName: '',
  idNumber: '',
  gender: '',
  ageGroup: '',
  phone: '',
  email: '',
  city: [],
  interestedSports: [],
  seatPreference: 'ANY',
  notificationPreferences: ['EMAIL'],
  specialRequirements: '',
  agreement: false
})

// 运动项目选项
const sportOptions = [
  { label: '田径', value: 'TRACK_AND_FIELD' },
  { label: '游泳', value: 'SWIMMING' },
  { label: '足球', value: 'FOOTBALL' },
  { label: '篮球', value: 'BASKETBALL' },
  { label: '乒乓球', value: 'TABLE_TENNIS' },
  { label: '羽毛球', value: 'BADMINTON' },
  { label: '网球', value: 'TENNIS' },
  { label: '排球', value: 'VOLLEYBALL' },
  { label: '棒球', value: 'BASEBALL' },
  { label: '武术', value: 'MARTIAL_ARTS' },
  { label: '健身', value: 'FITNESS' },
  { label: '瑜伽', value: 'YOGA' },
  { label: '自行车', value: 'CYCLING' },
  { label: '滑雪', value: 'SKIING' },
  { label: '冰球', value: 'ICE_HOCKEY' },
  { label: '高尔夫', value: 'GOLF' },
  { label: '其他', value: 'OTHER' }
]

// 省市选项 (简化版)
const cityOptions = [
  {
    value: 'beijing',
    label: '北京',
    children: [
      { value: 'beijing', label: '北京市' }
    ]
  },
  {
    value: 'shanghai',
    label: '上海',
    children: [
      { value: 'shanghai', label: '上海市' }
    ]
  },
  {
    value: 'guangdong',
    label: '广东省',
    children: [
      { value: 'guangzhou', label: '广州市' },
      { value: 'shenzhen', label: '深圳市' },
      { value: 'dongguan', label: '东莞市' },
      { value: 'foshan', label: '佛山市' },
      { value: 'other', label: '其他' }
    ]
  },
  {
    value: 'jiangsu',
    label: '江苏省',
    children: [
      { value: 'nanjing', label: '南京市' },
      { value: 'suzhou', label: '苏州市' },
      { value: 'wuxi', label: '无锡市' },
      { value: 'other', label: '其他' }
    ]
  },
  {
    value: 'zhejiang',
    label: '浙江省',
    children: [
      { value: 'hangzhou', label: '杭州市' },
      { value: 'ningbo', label: '宁波市' },
      { value: 'wenzhou', label: '温州市' },
      { value: 'other', label: '其他' }
    ]
  },
  {
    value: 'other',
    label: '其他',
    children: [
      { value: 'other', label: '其他' }
    ]
  }
]

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符之间', trigger: 'blur' }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  ageGroup: [
    { required: true, message: '请选择年龄段', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请选择常住城市', trigger: 'change' }
  ],
  interestedSports: [
    { required: true, message: '请至少选择一种感兴趣的运动', trigger: 'change' }
  ],
  seatPreference: [
    { required: true, message: '请选择座位偏好', trigger: 'change' }
  ],
  notificationPreferences: [
    { required: true, message: '请至少选择一种通知方式', trigger: 'change' }
  ],
  agreement: [
    { type: 'boolean', enum: [true], message: '请阅读并同意协议', trigger: 'change' }
  ]
}

// 初始化表单
const initForm = () => {
  form.realName = userStore.realName || ''
  form.email = userStore.email || ''
  form.phone = userStore.phone || ''
}

// 获取用户观众信息
const fetchUserAudienceInfo = async () => {
  loading.value = true
  
  try {
    // 检查用户是否已登录
    if (!userStore.isLoggedIn) {
      router.push('/login?redirect=/audience-registration')
      return
    }
    
    // 获取观众信息
    if (userStore.role === 'USER') {
      // 如果用户已是认证观众
      const response = await axios.get('/api/audience/info')
      
      if (response.data.success && response.data.data) {
        registered.value = true
      } else {
        // 用户未注册为观众，初始化表单
        initForm()
      }
    } else {
      // 用户可能是运动员或管理员
      ElMessage.warning('您当前的角色无法注册为观众')
      setTimeout(() => {
        router.push('/profile')
      }, 2000)
    }
  } catch (error) {
    console.error(error)
    // 假设用户未注册，初始化表单
    initForm()
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }
    
    submitting.value = true
    
    try {
      // 处理城市数据
      const formattedForm = {
        ...form,
        province: form.city[0],
        city: form.city[1]
      }
      
      // 调用API提交申请
      const response = await axios.post('/api/audience/register', formattedForm)
      
      if (response.data.success) {
        ElMessage.success('注册成功')
        registered.value = true
        // 刷新用户信息
        await userStore.fetchUserInfo()
      } else {
        ElMessage.error(response.data.message || '注册失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '注册失败，请稍后再试')
    } finally {
      submitting.value = false
    }
  })
}

// 页面加载时获取信息
onMounted(() => {
  fetchUserAudienceInfo()
})
</script>

<style scoped>
.agreement-content h3 {
  color: #303133;
  font-size: 1rem;
}
.agreement-content p {
  color: #606266;
  line-height: 1.6;
}
</style> 