<template>
  <div class="max-w-4xl mx-auto py-8 px-4">
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-xl font-bold text-gray-800">
          {{ isEdit ? '编辑赛事' : '创建赛事' }}
        </h2>
      </div>

      <div class="p-6">
        <BaseForm
          :schema="eventSchemas.create"
          :submit-text="isEdit ? '保存修改' : '创建赛事'"
          @success="handleSuccess"
          :on-submit="handleSubmit"
        >
          <template #default="{ errors }">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="md:col-span-2">
                <BaseFormField
                  name="name"
                  label="赛事名称"
                  required
                  placeholder="请输入赛事名称"
                />
              </div>

              <BaseFormField
                name="startTime"
                label="开始时间"
                type="datetime"
                required
                placeholder="请选择开始时间"
              />

              <BaseFormField
                name="endTime"
                label="结束时间"
                type="datetime"
                required
                placeholder="请选择结束时间"
              />

              <BaseFormField
                name="registrationDeadline"
                label="报名截止时间"
                type="datetime"
                required
                placeholder="请选择报名截止时间"
              />

              <BaseFormField
                name="maxParticipants"
                label="最大参与人数"
                type="number"
                required
                :min="1"
                :max="10000"
                placeholder="请输入最大参与人数"
              />

              <div class="md:col-span-2">
                <BaseFormField
                  name="location"
                  label="比赛地点"
                  required
                  placeholder="请输入比赛地点"
                />
              </div>

              <div class="md:col-span-2">
                <BaseFormField
                  name="description"
                  label="赛事描述"
                  type="textarea"
                  required
                  :rows="4"
                  placeholder="请输入赛事描述"
                />
              </div>

              <div class="md:col-span-2">
                <BaseFormField
                  name="rules"
                  label="比赛规则"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入比赛规则"
                  help-text="详细说明比赛规则、计分方式等"
                />
              </div>

              <BaseFormField
                name="type"
                label="赛事类型"
                type="select"
                required
                :options="eventTypes"
                placeholder="请选择赛事类型"
              />

              <BaseFormField
                name="status"
                label="赛事状态"
                type="select"
                required
                :options="eventStatus"
                placeholder="请选择赛事状态"
              />

              <div class="md:col-span-2">
                <BaseFormField
                  name="categories"
                  label="比赛项目"
                  type="select"
                  required
                  :options="eventCategories"
                  multiple
                  placeholder="请选择比赛项目"
                  help-text="可以选择多个比赛项目"
                />
              </div>

              <div class="md:col-span-2">
                <BaseFormField
                  name="requirements"
                  label="参赛要求"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入参赛要求"
                  help-text="说明参赛选手需要满足的条件"
                />
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
import { useRoute, useRouter } from 'vue-router'
import { eventApi } from '@/api'
import { eventSchemas } from '@/utils/validation'
import BaseForm from '@/components/BaseForm.vue'
import BaseFormField from '@/components/BaseFormField.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 判断是否是编辑模式
const isEdit = computed(() => !!route.params.id)

// 赛事类型选项
const eventTypes = [
  { label: '田径比赛', value: 'TRACK_AND_FIELD' },
  { label: '游泳比赛', value: 'SWIMMING' },
  { label: '球类比赛', value: 'BALL_GAMES' },
  { label: '武术比赛', value: 'MARTIAL_ARTS' },
  { label: '其他', value: 'OTHER' }
]

// 赛事状态选项
const eventStatus = [
  { label: '筹备中', value: 'PREPARING' },
  { label: '报名中', value: 'REGISTERING' },
  { label: '报名截止', value: 'REGISTRATION_CLOSED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已结束', value: 'FINISHED' },
  { label: '已取消', value: 'CANCELLED' }
]

// 比赛项目选项
const eventCategories = [
  { label: '100米跑', value: '100M' },
  { label: '200米跑', value: '200M' },
  { label: '400米跑', value: '400M' },
  { label: '800米跑', value: '800M' },
  { label: '1500米跑', value: '1500M' },
  { label: '跳高', value: 'HIGH_JUMP' },
  { label: '跳远', value: 'LONG_JUMP' },
  { label: '铅球', value: 'SHOT_PUT' },
  { label: '标枪', value: 'JAVELIN' },
  { label: '自由泳', value: 'FREESTYLE' },
  { label: '蛙泳', value: 'BREASTSTROKE' },
  { label: '仰泳', value: 'BACKSTROKE' },
  { label: '蝶泳', value: 'BUTTERFLY' }
]

// 处理表单提交
const handleSubmit = async (values) => {
  if (isEdit.value) {
    await eventApi.update(route.params.id, values)
  } else {
    await eventApi.create(values)
  }
}

// 处理提交成功
const handleSuccess = () => {
  ElMessage.success(isEdit.value ? '赛事更新成功' : '赛事创建成功')
  router.push('/admin/events')
}

// 如果是编辑模式，获取赛事详情
onMounted(async () => {
  if (isEdit.value) {
    const event = await eventApi.getById(route.params.id)
    // TODO: 设置表单初始值
  }
})
</script> 