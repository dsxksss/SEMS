<template>
  <transition
    enter-active-class="transform ease-out duration-300 transition"
    enter-from-class="translate-y-2 opacity-0 sm:translate-y-0 sm:translate-x-2"
    enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
    leave-active-class="transition ease-in duration-200"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div v-if="show" class="fixed top-4 right-4 z-50">
      <div class="max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto">
        <div class="rounded-lg shadow-xs overflow-hidden">
          <div class="p-4">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <el-icon class="h-6 w-6 text-red-500"><Warning /></el-icon>
              </div>
              <div class="ml-3 w-0 flex-1">
                <p class="text-sm font-medium text-gray-900">
                  {{ title }}
                </p>
                <p class="mt-1 text-sm text-gray-500">
                  {{ message }}
                </p>
              </div>
              <div class="ml-4 flex-shrink-0 flex">
                <button
                  @click="$emit('close')"
                  class="inline-flex text-gray-400 hover:text-gray-500 focus:outline-none"
                >
                  <el-icon class="h-5 w-5"><Close /></el-icon>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { Warning, Close } from '@element-plus/icons-vue'

defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '错误'
  },
  message: {
    type: String,
    required: true
  }
})

defineEmits(['close'])
</script> 