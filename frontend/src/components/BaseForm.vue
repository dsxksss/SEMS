<template>
  <form @submit.prevent="handleSubmit">
    <slot :errors="errors" :isValid="isValid" :isSubmitting="isSubmitting" />
    
    <div class="mt-6 flex justify-center">
      <el-button 
        type="primary" 
        :loading="isSubmitting" 
        native-type="submit"
        :disabled="!isValid && !ignoreValidation"
      >
        {{ submitText }}
      </el-button>
    </div>
  </form>
</template>

<script setup>
import { ref, reactive, watch, useSlots } from 'vue'
import { useForm } from 'vee-validate'
import { toFormValidator } from '@vee-validate/yup'
import { ElMessage } from 'element-plus'

const props = defineProps({
  // 表单验证schema
  schema: {
    type: Object,
    required: true
  },
  // 初始值
  initialValues: {
    type: Object,
    default: () => ({})
  },
  // 提交按钮文本
  submitText: {
    type: String,
    default: '提交'
  },
  // 是否忽略验证
  ignoreValidation: {
    type: Boolean,
    default: false
  },
  // 提交处理函数
  onSubmit: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['success', 'error'])
const slots = useSlots()

// 表单状态
const isSubmitting = ref(false)
const errors = reactive({})

// 使用vee-validate处理表单
const { handleSubmit: validateAndSubmit, values, isValid } = useForm({
  validationSchema: toFormValidator(props.schema),
  initialValues: props.initialValues
})

// 处理表单提交
const handleSubmit = validateAndSubmit(async (values) => {
  if (isSubmitting.value) return
  
  isSubmitting.value = true
  
  try {
    const result = await props.onSubmit(values)
    ElMessage.success('提交成功')
    emit('success', result)
  } catch (error) {
    console.error('表单提交错误:', error)
    ElMessage.error(error.message || '提交失败，请稍后重试')
    emit('error', error)
  } finally {
    isSubmitting.value = false
  }
})

// 监听slots变化，确保表单内容变化时重新验证
watch(() => slots.default?.(), () => {
  // 触发表单重新验证
}, { deep: true })
</script> 