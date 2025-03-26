<template>
  <div class="category-list">
    <div class="main-container">
      <div class="header">
        <h3>赛事分类管理</h3>
        <el-button type="primary" @click="handleAddCategory">添加分类</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="categoryList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'danger'">
              {{ scope.row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventCount" label="赛事数量" width="120" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
              >编辑</el-button>
            <el-button
              size="small"
              :type="scope.row.isActive ? 'danger' : 'success'"
              @click="handleToggleStatus(scope.row)"
              >{{ scope.row.isActive ? '禁用' : '启用' }}</el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
              >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      :title="isEdit ? '编辑分类' : '添加分类'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        :model="categoryForm"
        :rules="categoryRules"
        ref="categoryFormRef"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
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
        <el-form-item label="状态" prop="isActive">
          <el-radio-group v-model="categoryForm.isActive">
            <el-radio :label="true">启用</el-radio>
            <el-radio :label="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCategory">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { categoryAPI } from '../../../api/categoryAPI';
import type { EventCategory } from '@/types/event';

// 分类列表数据
const categoryList = ref<EventCategory[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// 对话框相关
const dialogVisible = ref(false);
const isEdit = ref(false);
const categoryFormRef = ref();
const categoryForm = reactive({
  id: 0,
  name: '',
  description: '',
  isActive: true
});

// 表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入分类描述', trigger: 'blur' }
  ]
};

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true;
  try {
    // 调用API获取分类列表
    const response = await categoryAPI.getAllCategories();
    categoryList.value = response;
    total.value = response.length;
    loading.value = false;
  } catch (error) {
    console.error('获取分类列表失败', error);
    ElMessage.error('获取分类列表失败，请刷新重试');
    loading.value = false;
  }
};

// 添加分类
const handleAddCategory = () => {
  isEdit.value = false;
  categoryForm.id = 0;
  categoryForm.name = '';
  categoryForm.description = '';
  categoryForm.isActive = true;
  dialogVisible.value = true;
};

// 编辑分类
const handleEdit = (row: EventCategory) => {
  isEdit.value = true;
  categoryForm.id = row.id;
  categoryForm.name = row.name;
  categoryForm.description = row.description;
  categoryForm.isActive = row.isActive;
  dialogVisible.value = true;
};

// 启用/禁用分类
const handleToggleStatus = (row: EventCategory) => {
  const newStatus = !row.isActive;
  const actionText = newStatus ? '启用' : '禁用';
  
  ElMessageBox.confirm(
    `确认要${actionText}分类 "${row.name}" 吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        // 调用API更新分类状态
        await categoryAPI.updateCategory(row.id, { isActive: newStatus });
        ElMessage.success(`${actionText}分类成功`);
        fetchCategoryList(); // 刷新列表
      } catch (error) {
        console.error(`${actionText}分类失败`, error);
        ElMessage.error(`${actionText}分类失败，请重试`);
      }
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 删除分类
const handleDelete = async (category: EventCategory) => {
  // 如果有关联的赛事，提示不能删除
  if (category.eventCount && category.eventCount > 0) {
    ElMessage.warning(`该分类下有${category.eventCount}个赛事，不能删除`);
    return;
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    // 调用API删除分类
    await categoryAPI.deleteCategory(category.id);
    ElMessage.success('删除分类成功');
    fetchCategoryList(); // 刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败', error);
      ElMessage.error('删除分类失败，请重试');
    }
  }
};

// 保存分类
const saveCategory = async () => {
  const formEl = categoryFormRef.value as any;
  if (!formEl) return;
  
  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑分类
          await categoryAPI.updateCategory(
            categoryForm.id, 
            {
              name: categoryForm.name,
              description: categoryForm.description,
              isActive: categoryForm.isActive
            }
          );
          ElMessage.success('更新分类成功');
        } else {
          // 添加分类
          await categoryAPI.createCategory({
            name: categoryForm.name,
            description: categoryForm.description,
            isActive: categoryForm.isActive
          } as Omit<EventCategory, 'id'>);
          ElMessage.success('添加分类成功');
        }
        dialogVisible.value = false;
        fetchCategoryList(); // 刷新分类列表
      } catch (error) {
        console.error('保存分类失败', error);
        ElMessage.error('保存分类失败，请重试');
      }
    } else {
      return false;
    }
  });
};

// 分页相关
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchCategoryList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchCategoryList();
};

// 初始化加载
onMounted(() => {
  fetchCategoryList();
});
</script>

<style scoped>
.category-list {
  width: 100%;
}

.main-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 