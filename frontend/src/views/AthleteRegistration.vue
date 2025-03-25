<template>
  <div class="max-w-4xl mx-auto py-8 px-4">
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-xl font-bold text-gray-800">运动员报名</h2>
        <p class="mt-2 text-sm text-gray-600">
          请填写以下信息完成报名，带 <span class="text-red-500">*</span> 的字段为必填项
        </p>
      </div>

      <div class="p-6">
        <BaseForm
          :schema="registrationSchemas.athlete"
          submit-text="提交报名"
          @success="handleSuccess"
          :on-submit="handleSubmit"
        >
          <template #default="{ errors }">
            <div class="space-y-6">
              <!-- 赛事信息 -->
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="text-lg font-medium text-gray-900 mb-4">赛事信息</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <BaseFormField
                    name="eventId"
                    label="选择赛事"
                    type="select"
                    required
                    :options="availableEvents"
                    placeholder="请选择要报名的赛事"
                    @change="handleEventChange"
                  />

                  <BaseFormField
                    name="category"
                    label="比赛项目"
                    type="select"
                    required
                    :options="selectedEventCategories"
                    placeholder="请选择比赛项目"
                    :disabled="!selectedEvent"
                  />
                </div>
              </div>

              <!-- 个人信息 -->
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="text-lg font-medium text-gray-900 mb-4">个人信息</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <BaseFormField
                    name="realName"
                    label="真实姓名"
                    required
                    placeholder="请输入真实姓名"
                  />

                  <BaseFormField
                    name="idNumber"
                    label="身份证号"
                    required
                    placeholder="请输入身份证号"
                  />

                  <BaseFormField
                    name="phoneNumber"
                    label="联系电话"
                    required
                    placeholder="请输入手机号码"
                  />

                  <BaseFormField
                    name="gender"
                    label="性别"
                    type="select"
                    required
                    :options="[
                      { label: '男', value: 'MALE' },
                      { label: '女', value: 'FEMALE' }
                    ]"
                    placeholder="请选择性别"
                  />

                  <BaseFormField
                    name="birthdate"
                    label="出生日期"
                    type="date"
                    required
                    placeholder="请选择出生日期"
                  />

                  <BaseFormField
                    name="height"
                    label="身高(cm)"
                    type="number"
                    required
                    :min="100"
                    :max="250"
                    placeholder="请输入身高"
                  />

                  <BaseFormField
                    name="weight"
                    label="体重(kg)"
                    type="number"
                    required
                    :min="30"
                    :max="200"
                    placeholder="请输入体重"
                  />
                </div>
              </div>

              <!-- 紧急联系人 -->
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="text-lg font-medium text-gray-900 mb-4">紧急联系人</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <BaseFormField
                    name="emergencyContact"
                    label="联系人姓名"
                    required
                    placeholder="请输入紧急联系人姓名"
                  />

                  <BaseFormField
                    name="emergencyPhone"
                    label="联系人电话"
                    required
                    placeholder="请输入紧急联系人电话"
                  />

                  <div class="md:col-span-2">
                    <BaseFormField
                      name="emergencyRelation"
                      label="与联系人关系"
                      required
                      placeholder="请输入与紧急联系人的关系"
                    />
                  </div>
                </div>
              </div>

              <!-- 运动经历 -->
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="text-lg font-medium text-gray-900 mb-4">运动经历</h3>
                <div class="space-y-6">
                  <BaseFormField
                    name="experience"
                    label="运动经历"
                    type="textarea"
                    :rows="4"
                    placeholder="请简述您的运动经历和成绩"
                    help-text="包括参加过的比赛、获得的奖项等"
                  />

                  <BaseFormField
                    name="bestScore"
                    label="最好成绩"
                    placeholder="请输入您在该项目的最好成绩"
                    help-text="例如：100米 11秒2"
                  />
                </div>
              </div>

              <!-- 健康状况 -->
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="text-lg font-medium text-gray-900 mb-4">健康状况</h3>
                <div class="space-y-6">
                  <BaseFormField
                    name="healthCondition"
                    label="身体状况"
                    type="textarea"
                    :rows="3"
                    placeholder="请说明您的身体状况"
                    help-text="包括是否有特殊病史、过敏史等"
                  />

                  <BaseFormField
                    name="medicalHistory"
                    label="既往病史"
                    type="textarea"
                    :rows="3"
                    placeholder="请说明您的既往病史"
                    help-text="如有重大疾病史请如实填写"
                  />
                </div>
              </div>

              <!-- 声明与承诺 -->
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="text-lg font-medium text-gray-900 mb-4">声明与承诺</h3>
                <div class="space-y-4">
                  <BaseFormField
                    name="agreement"
                    type="switch"
                    required
                    label="我已阅读并同意《参赛协议》和《免责声明》"
                  />
                </div>
              </div>
            </div>
          </template>
        </BaseForm>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { registrationApi, eventApi } from '@/api'
import { registrationSchemas } from '@/utils/validation'
import { ElMessage } from 'element-plus'
import BaseForm from '@/components/BaseForm.vue'
import BaseFormField from '@/components/BaseFormField.vue'

const router = useRouter()

// 可用赛事列表
const availableEvents = ref([])
// 当前选中的赛事
const selectedEvent = ref(null)
// 当前选中赛事的比赛项目
const selectedEventCategories = computed(() => {
  if (!selectedEvent.value) return []
  return selectedEvent.value.categories.map(category => ({
    label: category.name,
    value: category.id
  }))
})

// 获取可报名的赛事列表
const fetchAvailableEvents = async () => {
  try {
    const events = await eventApi.getByStatus('REGISTERING')
    availableEvents.value = events.map(event => ({
      label: event.name,
      value: event.id
    }))
  } catch (error) {
    ElMessage.error('获取赛事列表失败')
  }
}

// 处理赛事选择变化
const handleEventChange = async (eventId) => {
  if (!eventId) {
    selectedEvent.value = null
    return
  }

  try {
    selectedEvent.value = await eventApi.getById(eventId)
  } catch (error) {
    ElMessage.error('获取赛事详情失败')
  }
}

// 处理表单提交
const handleSubmit = async (values) => {
  await registrationApi.register({
    ...values,
    type: 'ATHLETE'
  })
}

// 处理提交成功
const handleSuccess = () => {
  ElMessage.success('报名提交成功，请等待审核')
  router.push('/profile')
}

// 页面加载时获取赛事列表
onMounted(fetchAvailableEvents)
</script> 