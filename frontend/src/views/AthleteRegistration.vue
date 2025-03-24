<template>
  <div>
    <Header />
    <div class="container mx-auto px-4 py-12">
      <div class="max-w-3xl mx-auto">
        <div class="mb-8 text-center">
          <h1 class="text-3xl font-bold text-gray-800 mb-2">运动员注册</h1>
          <p class="text-gray-600">填写您的个人信息和运动能力，成为认证运动员参与各类专业赛事</p>
        </div>
        
        <el-card shadow="hover">
          <div v-if="loading" class="flex justify-center py-8">
            <el-skeleton :rows="10" animated />
          </div>
          
          <div v-else-if="registered" class="py-8 text-center">
            <el-result
              icon="success"
              title="注册成功"
              sub-title="您的运动员资格已提交审核，请等待管理员审核"
            >
              <template #extra>
                <div class="flex flex-col md:flex-row justify-center gap-4">
                  <el-button type="primary" @click="$router.push('/profile')">查看个人资料</el-button>
                  <el-button @click="$router.push('/events')">浏览赛事活动</el-button>
                </div>
              </template>
            </el-result>
          </div>
          
          <div v-else-if="userStore.athleteStatus === 'PENDING'" class="py-8 text-center">
            <el-result
              icon="info"
              title="审核中"
              sub-title="您的运动员资格申请正在审核中，请耐心等待"
            >
              <template #extra>
                <div class="flex flex-col md:flex-row justify-center gap-4">
                  <el-button type="primary" @click="$router.push('/profile')">查看个人资料</el-button>
                  <el-button @click="$router.push('/events')">浏览赛事活动</el-button>
                </div>
              </template>
            </el-result>
          </div>
          
          <div v-else-if="userStore.athleteStatus === 'APPROVED'" class="py-8 text-center">
            <el-result
              icon="success"
              title="已认证"
              sub-title="您已是认证运动员，可以参加专业赛事"
            >
              <template #extra>
                <div class="flex flex-col md:flex-row justify-center gap-4">
                  <el-button type="primary" @click="$router.push('/profile')">查看个人资料</el-button>
                  <el-button @click="$router.push('/events')">浏览赛事活动</el-button>
                </div>
              </template>
            </el-result>
          </div>
          
          <div v-else-if="userStore.athleteStatus === 'REJECTED'" class="py-8 text-center">
            <el-result
              icon="error"
              title="审核未通过"
              :sub-title="rejectReason || '您的运动员资格申请未通过审核'"
            >
              <template #extra>
                <div class="flex flex-col md:flex-row justify-center gap-4">
                  <el-button type="primary" @click="initForm">重新申请</el-button>
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
                  
                  <el-form-item label="出生日期" prop="birthdate">
                    <el-date-picker
                      v-model="form.birthdate"
                      type="date"
                      placeholder="请选择出生日期"
                      style="width: 100%"
                    />
                  </el-form-item>
                  
                  <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入您的手机号码" />
                  </el-form-item>
                  
                  <el-form-item label="邮箱地址" prop="email">
                    <el-input v-model="form.email" placeholder="请输入您的邮箱地址" />
                  </el-form-item>
                </div>
              </div>
              
              <div class="mb-8">
                <h2 class="text-xl font-bold mb-4 border-b pb-2">运动能力</h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <el-form-item label="主要运动项目" prop="primarySport">
                    <el-select
                      v-model="form.primarySport"
                      placeholder="请选择您的主要运动项目"
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
                  
                  <el-form-item label="次要运动项目" prop="secondarySports">
                    <el-select
                      v-model="form.secondarySports"
                      multiple
                      placeholder="可选择您的次要运动项目"
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
                  
                  <el-form-item label="运动级别" prop="skillLevel">
                    <el-select
                      v-model="form.skillLevel"
                      placeholder="请选择您的运动级别"
                      style="width: 100%"
                    >
                      <el-option label="初级" value="BEGINNER" />
                      <el-option label="中级" value="INTERMEDIATE" />
                      <el-option label="高级" value="ADVANCED" />
                      <el-option label="专业" value="PROFESSIONAL" />
                      <el-option label="精英" value="ELITE" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="运动经验(年)" prop="yearsOfExperience">
                    <el-input-number 
                      v-model="form.yearsOfExperience" 
                      :min="0" 
                      :max="50" 
                      style="width: 100%" 
                    />
                  </el-form-item>
                </div>
                
                <el-form-item label="参赛经历" prop="competitionExperience">
                  <el-input
                    v-model="form.competitionExperience"
                    type="textarea"
                    rows="4"
                    placeholder="请描述您的参赛经历，包括参加过的赛事和成绩"
                  />
                </el-form-item>
                
                <el-form-item label="个人成就" prop="achievements">
                  <el-input
                    v-model="form.achievements"
                    type="textarea"
                    rows="4"
                    placeholder="请描述您的个人成就，如获得的奖项、记录等"
                  />
                </el-form-item>
              </div>
              
              <div class="mb-8">
                <h2 class="text-xl font-bold mb-4 border-b pb-2">健康信息</h2>
                <el-form-item label="身高(cm)" prop="height">
                  <el-input-number v-model="form.height" :min="100" :max="250" style="width: 100%" />
                </el-form-item>
                
                <el-form-item label="体重(kg)" prop="weight">
                  <el-input-number v-model="form.weight" :min="30" :max="200" style="width: 100%" />
                </el-form-item>
                
                <el-form-item label="健康状况" prop="healthStatus">
                  <el-radio-group v-model="form.healthStatus">
                    <el-radio label="EXCELLENT">极好</el-radio>
                    <el-radio label="GOOD">良好</el-radio>
                    <el-radio label="FAIR">一般</el-radio>
                    <el-radio label="POOR">较差</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="医疗情况" prop="medicalConditions">
                  <el-input
                    v-model="form.medicalConditions"
                    type="textarea"
                    rows="3"
                    placeholder="请描述您的医疗情况，如过敏、慢性病等，若无可填'无'"
                  />
                </el-form-item>
              </div>
              
              <div class="mb-8">
                <h2 class="text-xl font-bold mb-4 border-b pb-2">证件上传</h2>
                <p class="text-sm text-gray-600 mb-4">请上传清晰的身份证正反面和相关证书照片，以便我们进行身份验证</p>
                
                <el-form-item label="身份证正面" prop="idCardFront">
                  <el-upload
                    class="upload-demo"
                    action="/api/upload"
                    :on-success="handleIdFrontSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    :limit="1"
                  >
                    <template #trigger>
                      <el-button type="primary">选择文件</el-button>
                    </template>
                    <template #tip>
                      <div class="text-xs text-gray-500 mt-1">
                        仅支持JPG/PNG文件，且不超过5MB
                      </div>
                    </template>
                    <el-image
                      v-if="form.idCardFront"
                      :src="form.idCardFront"
                      fit="cover"
                      class="mt-2 w-full max-w-xs h-40 object-cover rounded-md"
                    />
                  </el-upload>
                </el-form-item>
                
                <el-form-item label="身份证背面" prop="idCardBack">
                  <el-upload
                    class="upload-demo"
                    action="/api/upload"
                    :on-success="handleIdBackSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    :limit="1"
                  >
                    <template #trigger>
                      <el-button type="primary">选择文件</el-button>
                    </template>
                    <template #tip>
                      <div class="text-xs text-gray-500 mt-1">
                        仅支持JPG/PNG文件，且不超过5MB
                      </div>
                    </template>
                    <el-image
                      v-if="form.idCardBack"
                      :src="form.idCardBack"
                      fit="cover"
                      class="mt-2 w-full max-w-xs h-40 object-cover rounded-md"
                    />
                  </el-upload>
                </el-form-item>
                
                <el-form-item label="资格证书(可选)" prop="certificates">
                  <el-upload
                    class="upload-demo"
                    action="/api/upload"
                    :on-success="handleCertificatesSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    multiple
                    :limit="5"
                  >
                    <template #trigger>
                      <el-button type="primary">选择文件</el-button>
                    </template>
                    <template #tip>
                      <div class="text-xs text-gray-500 mt-1">
                        仅支持JPG/PNG文件，且不超过5MB，最多上传5张
                      </div>
                    </template>
                  </el-upload>
                  <div v-if="form.certificates && form.certificates.length > 0" class="grid grid-cols-2 md:grid-cols-3 gap-2 mt-2">
                    <el-image
                      v-for="(cert, index) in form.certificates"
                      :key="index"
                      :src="cert"
                      fit="cover"
                      class="w-full h-32 object-cover rounded-md"
                    />
                  </div>
                </el-form-item>
              </div>
              
              <div class="mb-6">
                <el-form-item prop="agreement">
                  <el-checkbox v-model="form.agreement">
                    我已阅读并同意<a href="#" class="text-blue-500" @click.prevent="showAgreement = true">《运动员注册协议》</a>
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
                  提交申请
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
      title="运动员注册协议"
      width="500px"
    >
      <div class="agreement-content">
        <h3 class="font-bold mb-2">一、总则</h3>
        <p class="mb-4">本协议是运动员与平台之间关于运动员注册及参与赛事活动的协议。通过注册成为认证运动员，表示您已经阅读并同意本协议的所有条款。</p>
        
        <h3 class="font-bold mb-2">二、运动员资格</h3>
        <p class="mb-4">申请人必须是年满18周岁的健康人士，具备参加体育赛事的身体条件，无妨碍体育活动的疾病。平台有权审核申请人提供的信息，并保留最终审批权。</p>
        
        <h3 class="font-bold mb-2">三、信息真实性</h3>
        <p class="mb-4">运动员承诺提供的所有注册信息均为真实、准确、完整的资料。如提供虚假信息，平台有权取消运动员资格并追究相关责任。</p>
        
        <h3 class="font-bold mb-2">四、赛事参与</h3>
        <p class="mb-4">认证运动员可以参与平台发布的各类赛事活动，但需遵守具体赛事的规则和要求，服从赛事组织方的安排和裁判。</p>
        
        <h3 class="font-bold mb-2">五、隐私保护</h3>
        <p class="mb-4">平台将严格保护运动员的个人隐私，不会未经许可向第三方泄露个人资料，但法律法规另有规定的除外。</p>
        
        <h3 class="font-bold mb-2">六、协议修改</h3>
        <p class="mb-4">平台保留修改本协议的权利，修改后的协议将通过网站公告的形式通知运动员。运动员继续使用平台服务表示接受修改后的协议。</p>
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
const rejectReason = ref('')
const showAgreement = ref(false)

// 表单引用
const formRef = ref(null)

// 表单数据
const form = reactive({
  realName: '',
  idNumber: '',
  gender: '',
  birthdate: '',
  phone: '',
  email: '',
  primarySport: '',
  secondarySports: [],
  skillLevel: '',
  yearsOfExperience: 0,
  competitionExperience: '',
  achievements: '',
  height: 170,
  weight: 60,
  healthStatus: 'GOOD',
  medicalConditions: '',
  idCardFront: '',
  idCardBack: '',
  certificates: [],
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
  birthdate: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  primarySport: [
    { required: true, message: '请选择主要运动项目', trigger: 'change' }
  ],
  skillLevel: [
    { required: true, message: '请选择运动级别', trigger: 'change' }
  ],
  idCardFront: [
    { required: true, message: '请上传身份证正面照片', trigger: 'change' }
  ],
  idCardBack: [
    { required: true, message: '请上传身份证背面照片', trigger: 'change' }
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
  
  registered.value = false
}

// 获取用户运动员信息
const fetchUserAthleteInfo = async () => {
  loading.value = true
  
  try {
    // 检查用户是否已登录
    if (!userStore.isLoggedIn) {
      router.push('/login?redirect=/athlete-registration')
      return
    }
    
    // 获取运动员信息
    if (userStore.role === 'ATHLETE') {
      // 如果用户已经是运动员，获取相关信息
      registered.value = true
    } else if (userStore.athleteStatus === 'REJECTED') {
      // 获取拒绝原因
      rejectReason.value = userStore.athleteRejectReason || '您的运动员资格申请未通过审核'
    } else if (userStore.athleteStatus === 'PENDING') {
      // 申请正在审核中
    } else {
      // 用户未申请，初始化表单
      initForm()
    }
  } catch (error) {
    ElMessage.error('获取运动员信息失败，请稍后再试')
    console.error(error)
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
      // 格式化日期
      const formattedForm = {
        ...form,
        birthdate: form.birthdate ? new Date(form.birthdate).toISOString().split('T')[0] : null
      }
      
      // 调用API提交申请
      const response = await axios.post('/api/athlete/register', formattedForm)
      
      if (response.data.success) {
        ElMessage.success('申请提交成功，请等待审核')
        registered.value = true
        // 刷新用户信息
        await userStore.fetchUserInfo()
      } else {
        ElMessage.error(response.data.message || '申请提交失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '申请提交失败，请稍后再试')
    } finally {
      submitting.value = false
    }
  })
}

// 文件上传前的处理
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传JPG/PNG格式的图片！')
    return false
  }
  
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB！')
    return false
  }
  
  return true
}

// 身份证正面上传成功
const handleIdFrontSuccess = (response) => {
  if (response.success) {
    form.idCardFront = response.data.url
    ElMessage.success('身份证正面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 身份证背面上传成功
const handleIdBackSuccess = (response) => {
  if (response.success) {
    form.idCardBack = response.data.url
    ElMessage.success('身份证背面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 证书上传成功
const handleCertificatesSuccess = (response) => {
  if (response.success) {
    if (!form.certificates) {
      form.certificates = []
    }
    form.certificates.push(response.data.url)
    ElMessage.success('证书上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

// 页面加载时获取信息
onMounted(() => {
  fetchUserAthleteInfo()
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