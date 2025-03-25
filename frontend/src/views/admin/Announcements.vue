<template>
  <div class="container mx-auto py-8 px-4">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">公告管理</h1>
      <el-button type="primary" @click="showAddDialog">添加公告</el-button>
    </div>

    <!-- 公告列表 -->
    <el-table :data="announcements" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="content" label="内容" min-width="300">
        <template #default="scope">
          <div class="line-clamp-2">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.publishTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="isPublished" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isPublished ? 'success' : 'info'">
            {{ scope.row.isPublished ? '已发布' : '未发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editAnnouncement(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="confirmDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="flex justify-end mt-4">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '添加公告'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-switch v-model="form.isPublished" active-text="发布" inactive-text="草稿" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { announcementApi } from '@/api'

// 数据列表
const announcements = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  title: '',
  content: '',
  isPublished: true
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度为2-100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await announcementApi.getAnnouncementList({
      page: currentPage.value,
      size: pageSize.value
    })
    announcements.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('获取公告列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  form.id = null
  form.title = ''
  form.content = ''
  form.isPublished = true
  dialogVisible.value = true
}

// 编辑公告
const editAnnouncement = (row) => {
  isEdit.value = true
  form.id = row.id
  form.title = row.title
  form.content = row.content
  form.isPublished = row.isPublished
  dialogVisible.value = true
}

// 确认删除
const confirmDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteAnnouncement(row.id)
  }).catch(() => {})
}

// 删除公告
const deleteAnnouncement = async (id) => {
  try {
    await announcementApi.deleteAnnouncement(id)
    ElMessage.success('删除成功')
    loadAnnouncements()
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (isEdit.value) {
        await announcementApi.updateAnnouncement(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await announcementApi.createAnnouncement(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadAnnouncements()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
      console.error(error)
    }
  })
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  loadAnnouncements()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadAnnouncements()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

// 初始化
onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 