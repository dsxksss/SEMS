<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">赛事分类管理</h1>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加分类
      </el-button>
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
    />

    <!-- 分类列表 -->
    <el-table
      v-if="!loading && categories.length > 0"
      :data="categories"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="分类" min-width="200">
        <template #default="scope">
          <div class="flex items-center">
            <div v-if="scope.row.icon" class="mr-2">
              <el-icon><component :is="scope.row.icon" /></el-icon>
            </div>
            <div class="font-medium">{{ scope.row.name }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="300" />
      <el-table-column prop="isActive" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.isActive ? 'success' : 'danger'">
            {{ scope.row.isActive ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <div class="flex space-x-2">
            <el-button size="small" type="primary" @click="editCategory(scope.row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="scope.row.isActive ? 'danger' : 'success'" 
              @click="toggleCategoryStatus(scope.row)"
            >
              {{ scope.row.isActive ? '禁用' : '启用' }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空数据提示 -->
    <el-empty v-if="!loading && categories.length === 0" description="暂无分类数据" />

    <!-- 分类表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="30%"
    >
      <el-form
        ref="formRef"
        :model="categoryForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="categoryForm.icon" placeholder="请输入图标名称" />
          <div class="text-xs text-gray-500 mt-1">
            支持Element Plus图标名称，例如：Football、Basketball、Tennis等
          </div>
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="isActive">
          <el-switch v-model="categoryForm.isActive" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCategory" :loading="submitting">
            {{ isEdit ? '保存' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import categoryAPI from '@/api/categoryAPI';
import type { EventCategory } from '@/types/event';

const categories = ref<EventCategory[]>([]);
const loading = ref(false);
const submitting = ref(false);
const error = ref('');
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();

// 分类表单
const categoryForm = reactive({
  id: 0,
  name: '',
  description: '',
  icon: '',
  isActive: true
});

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入分类描述', trigger: 'blur' }
  ]
});

// 加载分类列表
const loadCategories = async () => {
  loading.value = true;
  error.value = '';
  try {
    categories.value = await categoryAPI.getAllCategories();
  } catch (err) {
    console.error('加载分类失败', err);
    error.value = '加载分类数据失败，请重试';
    categories.value = [];
  } finally {
    loading.value = false;
  }
};

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 编辑分类
const editCategory = (category: EventCategory) => {
  isEdit.value = true;
  resetForm();
  
  // 填充表单
  categoryForm.id = category.id;
  categoryForm.name = category.name;
  categoryForm.description = category.description;
  categoryForm.icon = category.icon || '';
  categoryForm.isActive = category.isActive;
  
  dialogVisible.value = true;
};

// 切换分类状态
const toggleCategoryStatus = async (category: EventCategory) => {
  try {
    if (category.isActive) {
      // 确认禁用
      await ElMessageBox.confirm(
        `确定要禁用分类 "${category.name}" 吗？禁用后，该分类将不会在用户界面显示。`,
        '禁用分类',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      );
      
      await categoryAPI.deleteCategory(category.id);
      ElMessage.success('分类已禁用');
    } else {
      // 启用分类
      await categoryAPI.updateCategory(category.id, { isActive: true });
      ElMessage.success('分类已启用');
    }
    
    // 重新加载数据
    loadCategories();
  } catch (err: any) {
    if (err === 'cancel') return;
    
    console.error('操作分类状态失败', err);
    ElMessage.error(err.response?.data?.message || '操作失败，请重试');
  }
};

// 重置表单
const resetForm = () => {
  categoryForm.id = 0;
  categoryForm.name = '';
  categoryForm.description = '';
  categoryForm.icon = '';
  categoryForm.isActive = true;
  
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 保存分类
const saveCategory = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitting.value = true;
      
      try {
        if (isEdit.value) {
          // 更新分类
          await categoryAPI.updateCategory(categoryForm.id, {
            name: categoryForm.name,
            description: categoryForm.description,
            icon: categoryForm.icon || null,
            isActive: categoryForm.isActive
          });
          ElMessage.success('分类更新成功');
        } else {
          // 创建分类
          await categoryAPI.createCategory({
            name: categoryForm.name,
            description: categoryForm.description,
            icon: categoryForm.icon || null,
            isActive: true
          });
          ElMessage.success('分类添加成功');
        }
        
        dialogVisible.value = false;
        loadCategories();
      } catch (err: any) {
        console.error('保存分类失败', err);
        ElMessage.error(err.response?.data?.message || '操作失败，请重试');
      } finally {
        submitting.value = false;
      }
    } else {
      console.log('表单验证失败', fields);
    }
  });
};

onMounted(() => {
  loadCategories();
});
</script>
