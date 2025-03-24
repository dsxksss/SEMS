<template>
  <div>
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">系统设置</h1>
    </div>
    
    <el-tabs v-model="activeTab" tab-position="left" class="settings-tabs">
      <el-tab-pane label="基本设置" name="basic">
        <div class="p-4">
          <h2 class="text-xl font-medium mb-6">基本设置</h2>
          
          <el-form 
            ref="basicFormRef" 
            :model="basicForm" 
            :rules="basicRules" 
            label-width="120px"
          >
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            
            <el-form-item label="管理员邮箱" prop="adminEmail">
              <el-input v-model="basicForm.adminEmail" placeholder="请输入管理员邮箱" />
            </el-form-item>
            
            <el-form-item label="网站描述" prop="description">
              <el-input 
                v-model="basicForm.description" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入网站描述"
              />
            </el-form-item>
            
            <el-form-item label="备案信息" prop="icp">
              <el-input v-model="basicForm.icp" placeholder="请输入备案信息" />
            </el-form-item>
            
            <el-form-item label="开放注册" prop="allowRegister">
              <el-switch v-model="basicForm.allowRegister" />
            </el-form-item>
            
            <el-form-item label="首页展示赛事数" prop="homeEventCount">
              <el-input-number v-model="basicForm.homeEventCount" :min="1" :max="20" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings" :loading="basicSubmitting">保存设置</el-button>
              <el-button @click="resetBasicForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="邮件设置" name="email">
        <div class="p-4">
          <h2 class="text-xl font-medium mb-6">邮件设置</h2>
          
          <el-form 
            ref="emailFormRef" 
            :model="emailForm" 
            :rules="emailRules" 
            label-width="120px"
          >
            <el-form-item label="SMTP服务器" prop="smtpServer">
              <el-input v-model="emailForm.smtpServer" placeholder="请输入SMTP服务器地址" />
            </el-form-item>
            
            <el-form-item label="SMTP端口" prop="smtpPort">
              <el-input-number v-model="emailForm.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            
            <el-form-item label="发件人邮箱" prop="senderEmail">
              <el-input v-model="emailForm.senderEmail" placeholder="请输入发件人邮箱" />
            </el-form-item>
            
            <el-form-item label="发件人名称" prop="senderName">
              <el-input v-model="emailForm.senderName" placeholder="请输入发件人名称" />
            </el-form-item>
            
            <el-form-item label="邮箱密码" prop="emailPassword">
              <el-input v-model="emailForm.emailPassword" type="password" placeholder="请输入邮箱密码" show-password />
            </el-form-item>
            
            <el-form-item label="启用SSL" prop="enableSsl">
              <el-switch v-model="emailForm.enableSsl" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveEmailSettings" :loading="emailSubmitting">保存设置</el-button>
              <el-button @click="resetEmailForm">重置</el-button>
              <el-button type="success" @click="testEmailConnection" :loading="testingEmail">测试连接</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="安全设置" name="security">
        <div class="p-4">
          <h2 class="text-xl font-medium mb-6">安全设置</h2>
          
          <el-form 
            ref="securityFormRef" 
            :model="securityForm" 
            :rules="securityRules" 
            label-width="160px"
          >
            <el-form-item label="启用登录验证码" prop="enableCaptcha">
              <el-switch v-model="securityForm.enableCaptcha" />
            </el-form-item>
            
            <el-form-item label="密码最小长度" prop="passwordMinLength">
              <el-input-number v-model="securityForm.passwordMinLength" :min="6" :max="20" />
            </el-form-item>
            
            <el-form-item label="登录失败锁定次数" prop="loginFailLockCount">
              <el-input-number v-model="securityForm.loginFailLockCount" :min="0" :max="10" />
              <span class="text-gray-500 ml-2">（0表示不锁定）</span>
            </el-form-item>
            
            <el-form-item label="锁定时间（分钟）" prop="lockTime">
              <el-input-number v-model="securityForm.lockTime" :min="5" :max="1440" />
            </el-form-item>
            
            <el-form-item label="JWT密钥" prop="jwtSecret">
              <el-input v-model="securityForm.jwtSecret" placeholder="请输入JWT密钥" show-password />
              <div class="mt-1">
                <el-button size="small" @click="generateJwtSecret">生成新密钥</el-button>
                <span class="text-red-500 text-sm ml-2">修改密钥会导致所有用户需要重新登录</span>
              </div>
            </el-form-item>
            
            <el-form-item label="Token有效期（小时）" prop="tokenExpireHours">
              <el-input-number v-model="securityForm.tokenExpireHours" :min="1" :max="720" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveSecuritySettings" :loading="securitySubmitting">保存设置</el-button>
              <el-button @click="resetSecurityForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 当前激活的标签页
const activeTab = ref('basic')

// 表单引用
const basicFormRef = ref()
const emailFormRef = ref()
const securityFormRef = ref()

// 提交状态
const basicSubmitting = ref(false)
const emailSubmitting = ref(false)
const securitySubmitting = ref(false)
const testingEmail = ref(false)

// 基本设置表单
const basicForm = reactive({
  systemName: '体育赛事管理系统',
  adminEmail: 'admin@example.com',
  description: '一个面向各类体育赛事的管理和报名系统，支持多种赛事类型和灵活的报名审核流程。',
  icp: '粤ICP备xxxxxxxx号',
  allowRegister: true,
  homeEventCount: 6
})

// 邮件设置表单
const emailForm = reactive({
  smtpServer: 'smtp.example.com',
  smtpPort: 465,
  senderEmail: 'noreply@example.com',
  senderName: '体育赛事管理系统',
  emailPassword: '',
  enableSsl: true
})

// 安全设置表单
const securityForm = reactive({
  enableCaptcha: true,
  passwordMinLength: 8,
  loginFailLockCount: 5,
  lockTime: 30,
  jwtSecret: '',
  tokenExpireHours: 24
})

// 表单校验规则
const basicRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  adminEmail: [
    { required: true, message: '请输入管理员邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '不能超过500个字符', trigger: 'blur' }
  ],
  homeEventCount: [
    { required: true, message: '请输入首页展示赛事数', trigger: 'blur' }
  ]
}

const emailRules = {
  smtpServer: [
    { required: true, message: '请输入SMTP服务器地址', trigger: 'blur' }
  ],
  smtpPort: [
    { required: true, message: '请输入SMTP端口', trigger: 'blur' }
  ],
  senderEmail: [
    { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  senderName: [
    { required: true, message: '请输入发件人名称', trigger: 'blur' }
  ],
  emailPassword: [
    { required: true, message: '请输入邮箱密码', trigger: 'blur' }
  ]
}

const securityRules = {
  passwordMinLength: [
    { required: true, message: '请输入密码最小长度', trigger: 'blur' }
  ],
  jwtSecret: [
    { required: true, message: '请输入JWT密钥', trigger: 'blur' },
    { min: 16, message: '密钥长度不能少于16个字符', trigger: 'blur' }
  ],
  tokenExpireHours: [
    { required: true, message: '请输入Token有效期', trigger: 'blur' }
  ]
}

// 获取设置
const fetchSettings = async () => {
  try {
    // 获取基本设置
    const basicResponse = await axios.get('/api/settings/basic')
    Object.assign(basicForm, basicResponse.data.data)
    
    // 获取邮件设置
    const emailResponse = await axios.get('/api/settings/email')
    Object.assign(emailForm, emailResponse.data.data)
    
    // 获取安全设置
    const securityResponse = await axios.get('/api/settings/security')
    Object.assign(securityForm, securityResponse.data.data)
  } catch (error) {
    ElMessage.error('获取设置失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

// 保存基本设置
const saveBasicSettings = async () => {
  if (!basicFormRef.value) return
  
  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      basicSubmitting.value = true
      
      try {
        await axios.post('/api/settings/basic', basicForm)
        ElMessage.success('基本设置保存成功')
      } catch (error) {
        ElMessage.error('保存失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        basicSubmitting.value = false
      }
    }
  })
}

// 保存邮件设置
const saveEmailSettings = async () => {
  if (!emailFormRef.value) return
  
  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      emailSubmitting.value = true
      
      try {
        await axios.post('/api/settings/email', emailForm)
        ElMessage.success('邮件设置保存成功')
      } catch (error) {
        ElMessage.error('保存失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        emailSubmitting.value = false
      }
    }
  })
}

// 保存安全设置
const saveSecuritySettings = async () => {
  if (!securityFormRef.value) return
  
  await securityFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await ElMessageBox.confirm(
          '修改安全设置可能会影响系统运行，确定要保存吗？',
          '保存安全设置',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        securitySubmitting.value = true
        
        await axios.post('/api/settings/security', securityForm)
        ElMessage.success('安全设置保存成功')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('保存失败: ' + (error.response?.data?.message || '未知错误'))
        }
      } finally {
        securitySubmitting.value = false
      }
    }
  })
}

// 测试邮件连接
const testEmailConnection = async () => {
  if (!emailFormRef.value) return
  
  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      testingEmail.value = true
      
      try {
        await axios.post('/api/settings/test-email', emailForm)
        ElMessage.success('邮件服务器连接测试成功')
      } catch (error) {
        ElMessage.error('测试失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        testingEmail.value = false
      }
    }
  })
}

// 生成JWT密钥
const generateJwtSecret = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+';
  let result = '';
  for (let i = 0; i < 32; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  securityForm.jwtSecret = result;
}

// 重置表单
const resetBasicForm = () => {
  if (basicFormRef.value) {
    basicFormRef.value.resetFields()
  }
}

const resetEmailForm = () => {
  if (emailFormRef.value) {
    emailFormRef.value.resetFields()
  }
}

const resetSecurityForm = () => {
  if (securityFormRef.value) {
    securityFormRef.value.resetFields()
  }
}

// 生命周期钩子
onMounted(() => {
  fetchSettings()
})
</script>

<style scoped>
.settings-tabs :deep(.el-tabs__header) {
  margin-right: 0;
}

.settings-tabs :deep(.el-tabs__item) {
  padding: 12px 20px;
  height: auto;
  text-align: left;
  font-size: 15px;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  background-color: #f0f9ff;
  color: #409eff;
}

.settings-tabs :deep(.el-tabs__content) {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style> 