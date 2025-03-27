<template>
  <div class="announcement-list">
    <div class="filter-container">
      <el-form :inline="true" :model="filterForm" class="form-inline">
        <el-form-item label="标题">
          <el-input v-model="filterForm.title" placeholder="输入标题搜索" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterForm.type" placeholder="选择类型" clearable>
            <el-option label="赛事通知" value="EVENT" />
            <el-option label="报名通知" value="REGISTRATION" />
            <el-option label="规则通知" value="RULE" />
            <el-option label="系统通知" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchAnnouncements">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <div class="table-header">
        <h3 class="text-lg font-medium text-gray-800">公告列表</h3>
        <el-button type="primary" @click="handleCreate">发布公告</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="announcementList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="250" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ formatType(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="createdBy" label="创建者" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                size="small"
                type="primary"
                @click="handleView(scope.row)"
              >查看</el-button>
              <el-button
                size="small"
                type="success"
                @click="handleEdit(scope.row)"
              >编辑</el-button>
              <el-button
                v-if="scope.row.status !== 'PUBLISHED'"
                size="small"
                type="warning"
                @click="handlePublish(scope.row)"
              >发布</el-button>
              <el-button
                v-else
                size="small"
                type="info"
                @click="handleSetExpired(scope.row)"
              >撤回</el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </div>
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

    <!-- 公告详情弹窗 -->
    <el-dialog
      title="公告详情"
      v-model="detailDialogVisible"
      width="800px"
    >
      <div v-if="currentAnnouncement" class="announcement-detail">
        <div class="announcement-header">
          <h2>{{ currentAnnouncement.title }}</h2>
          <div class="announcement-meta">
            <span>发布时间：{{ currentAnnouncement.publishTime || '未发布' }}</span>
            <span>类型：{{ formatType(currentAnnouncement.type) }}</span>
            <span>浏览量：{{ currentAnnouncement.viewCount }}</span>
          </div>
        </div>
        <div class="announcement-content" v-html="currentAnnouncement.content"></div>
        <div class="announcement-attachments" v-if="currentAnnouncement.attachments && currentAnnouncement.attachments.length > 0">
          <h4>附件：</h4>
          <ul class="attachment-list">
            <li v-for="(attachment, index) in currentAnnouncement.attachments" :key="index">
              <a :href="attachment.url" target="_blank">{{ attachment.name }}</a>
            </li>
          </ul>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentAnnouncement && currentAnnouncement.status !== 'EXPIRED'"
            type="primary" 
            @click="handleEdit(currentAnnouncement)"
          >编辑</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑/创建公告弹窗 -->
    <el-dialog
      :title="isEdit ? '编辑公告' : '发布新公告'"
      v-model="formDialogVisible"
      width="800px"
    >
      <el-form 
        ref="announcementFormRef"
        :model="announcementForm"
        :rules="rules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        
        <el-form-item label="类型" prop="type">
          <el-select v-model="announcementForm.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="赛事通知" value="EVENT" />
            <el-option label="报名通知" value="REGISTRATION" />
            <el-option label="规则通知" value="RULE" />
            <el-option label="系统通知" value="SYSTEM" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入公告内容，支持HTML格式"
          />
        </el-form-item>
        
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="announcementForm.status">
            <el-radio label="PUBLISHED">立即发布</el-radio>
            <el-radio label="DRAFT">保存草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="附件">
          <el-upload
            action="/api/files/upload"
            :headers="{ Authorization: `Bearer ${authStore.token}` }"
            :auto-upload="true"
            :file-list="fileList"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleFileRemove"
            multiple
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                可上传任意类型文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="formDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAnnouncementForm" :loading="submitting">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      title="确认删除"
      v-model="deleteDialogVisible"
      width="400px"
    >
      <p>确定要删除该公告吗？删除后无法恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleting">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import dayjs from 'dayjs';
import { announcementAPI } from '../../../api';
import { ElMessageBox } from 'element-plus';
import { useAuthStore } from '../../../stores/auth';

const authStore = useAuthStore();

interface Attachment {
  name: string;
  url: string;
  size?: number;
}

interface Announcement {
  id: number;
  title: string;
  content: string;
  type: string;
  status: string;
  createTime: string;
  publishTime?: string;
  createdBy: string;
  viewCount: number;
  attachments?: Attachment[];
}

// 过滤表单
const filterForm = reactive({
  title: '',
  type: '',
  status: ''
});

// 公告列表数据
const announcementList = ref<Announcement[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// 详情弹窗
const detailDialogVisible = ref(false);
const currentAnnouncement = ref<Announcement | null>(null);

// 表单弹窗
const formDialogVisible = ref(false);
const announcementFormRef = ref<FormInstance>();
const isEdit = ref(false);
const submitting = ref(false);
const fileList = ref<any[]>([]);

// 删除确认对话框
const deleteDialogVisible = ref(false);
const deleteTarget = ref<Announcement | null>(null);
const deleting = ref(false);

// 公告表单
const announcementForm = reactive({
  id: 0,
  title: '',
  content: '',
  type: '',
  status: 'DRAFT',
  attachments: [] as Attachment[]
});

// 表单验证规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度应为2到100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择公告类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 10, message: '内容至少10个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择发布状态', trigger: 'change' }
  ]
});

// 获取公告列表
const fetchAnnouncementList = async () => {
  loading.value = true;
  try {
    // 调用API获取所有公告
    const announcements = await announcementAPI.getAllAnnouncements();
    
    // 根据搜索条件过滤
    let filteredAnnouncements = announcements.filter(item => {
      let matches = true;
      if (filterForm.title && !item.title.toLowerCase().includes(filterForm.title.toLowerCase())) {
        matches = false;
      }
      if (filterForm.type && item.type !== filterForm.type) {
        matches = false;
      }
      if (filterForm.status && item.status !== filterForm.status) {
        matches = false;
      }
      return matches;
    });
    
    // 计算总数
    total.value = filteredAnnouncements.length;
    
    // 分页处理
    const startIndex = (currentPage.value - 1) * pageSize.value;
    const endIndex = startIndex + pageSize.value;
    
    // 格式化数据
    announcementList.value = filteredAnnouncements.slice(startIndex, endIndex).map(ann => ({
      id: ann.id,
      title: ann.title,
      content: ann.content,
      type: ann.event ? 'EVENT' : (ann.type || 'SYSTEM'),
      status: ann.isPublished ? 'PUBLISHED' : 'DRAFT',
      createTime: ann.createdAt ? dayjs(ann.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-',
      publishTime: ann.isPublished && ann.updatedAt ? dayjs(ann.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '-',
      createdBy: ann.createdBy?.username || '管理员',
      viewCount: ann.viewCount || 0,
      attachments: ann.attachments || []
    }));
  } catch (error) {
    console.error('获取公告列表失败', error);
    ElMessage.error('获取公告列表失败，请刷新重试');
    
    // 清空数据
    announcementList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索公告
const searchAnnouncements = () => {
  currentPage.value = 1;
  fetchAnnouncementList();
};

// 重置表单
const resetForm = () => {
  filterForm.title = '';
  filterForm.type = '';
  filterForm.status = '';
  searchAnnouncements();
};

// 格式化类型显示
const formatType = (type: string) => {
  switch (type) {
    case 'EVENT':
      return '赛事通知';
    case 'REGISTRATION':
      return '报名通知';
    case 'RULE':
      return '规则通知';
    case 'SYSTEM':
      return '系统通知';
    default:
      return type;
  }
};

// 获取类型对应的标签类型
const getTypeTag = (type: string) => {
  switch (type) {
    case 'EVENT':
      return 'primary';
    case 'REGISTRATION':
      return 'success';
    case 'RULE':
      return 'warning';
    case 'SYSTEM':
      return 'info';
    default:
      return '';
  }
};

// 格式化状态显示
const formatStatus = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return '已发布';
    case 'DRAFT':
      return '草稿';
    case 'EXPIRED':
      return '已过期';
    default:
      return status;
  }
};

// 获取状态对应的标签类型
const getStatusTag = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return 'success';
    case 'DRAFT':
      return 'info';
    case 'EXPIRED':
      return 'danger';
    default:
      return '';
  }
};

// 查看公告详情
const handleView = async (row: Announcement) => {
  try {
    // 调用API获取详细信息
    const announcement = await announcementAPI.getAnnouncementById(row.id);
    
    // 格式化数据
    currentAnnouncement.value = {
      id: announcement.id,
      title: announcement.title, 
      content: announcement.content,
      type: announcement.event ? 'EVENT' : (announcement.type || 'SYSTEM'),
      status: announcement.isPublished ? 'PUBLISHED' : 'DRAFT',
      createTime: announcement.createdAt ? dayjs(announcement.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-',
      publishTime: announcement.isPublished && announcement.updatedAt ? dayjs(announcement.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '-',
      createdBy: announcement.createdBy?.username || '管理员',
      viewCount: announcement.viewCount || 0,
      attachments: announcement.attachments || []
    };
    
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('获取公告详情失败', error);
    ElMessage.error('获取公告详情失败，请重试');
  }
};

// 创建新公告
const handleCreate = () => {
  isEdit.value = false;
  resetAnnouncementForm();
  fileList.value = [];
  formDialogVisible.value = true;
};

// 编辑公告
const handleEdit = async (row: Announcement) => {
  try {
    // 获取最新的公告详情
    const announcement = await announcementAPI.getAnnouncementById(row.id);
    
    isEdit.value = true;
    resetAnnouncementForm();
    
    // 设置表单数据
    announcementForm.id = announcement.id;
    announcementForm.title = announcement.title;
    announcementForm.content = announcement.content;
    announcementForm.type = announcement.event ? 'EVENT' : (announcement.type || 'SYSTEM');
    announcementForm.status = announcement.isPublished ? 'PUBLISHED' : 'DRAFT';
    
    // 处理附件
    announcementForm.attachments = announcement.attachments || [];
    fileList.value = announcementForm.attachments.map(a => ({
      name: a.name,
      url: a.url
    }));
    
    formDialogVisible.value = true;
  } catch (error) {
    console.error('获取公告详情失败', error);
    ElMessage.error('获取公告详情失败，请重试');
  }
};

// 发布公告
const handlePublish = (row: Announcement) => {
  ElMessageBox.confirm(
    `确定要发布公告 "${row.title}" 吗？`,
    '发布确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  )
    .then(async () => {
      try {
        // 使用专用的发布API
        await announcementAPI.toggleAnnouncementPublished(row.id, true);
        ElMessage.success('公告发布成功');
        fetchAnnouncementList();
      } catch (error) {
        console.error('公告发布失败', error);
        ElMessage.error('公告发布失败，请重试');
      }
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 撤回公告（设为已过期）
const handleSetExpired = (row: Announcement) => {
  ElMessageBox.confirm(
    `确定要撤回公告 "${row.title}" 吗？`,
    '撤回确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        // 使用专用的发布API，设置为未发布
        await announcementAPI.toggleAnnouncementPublished(row.id, false);
        ElMessage.success('公告已撤回');
        fetchAnnouncementList();
      } catch (error) {
        console.error('公告撤回失败', error);
        ElMessage.error('公告撤回失败，请重试');
      }
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 删除公告
const handleDelete = (row: Announcement) => {
  deleteTarget.value = row;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  if (!deleteTarget.value) return;
  
  deleting.value = true;
  try {
    // 调用API删除公告
    await announcementAPI.deleteAnnouncement(deleteTarget.value.id);
    
    ElMessage.success('公告删除成功');
    deleteDialogVisible.value = false;
    
    // 刷新列表
    fetchAnnouncementList();
    
    // 如果正在查看该公告，关闭详情弹窗
    if (currentAnnouncement.value && currentAnnouncement.value.id === deleteTarget.value?.id) {
      detailDialogVisible.value = false;
    }
  } catch (error) {
    console.error('删除公告失败', error);
    ElMessage.error('删除公告失败，请重试');
  } finally {
    deleting.value = false;
  }
};

// 重置公告表单
const resetAnnouncementForm = () => {
  announcementForm.id = 0;
  announcementForm.title = '';
  announcementForm.content = '';
  announcementForm.type = '';
  announcementForm.status = 'DRAFT';
  announcementForm.attachments = [];
  
  if (announcementFormRef.value) {
    announcementFormRef.value.resetFields();
  }
};

// 处理文件变更
const handleFileChange = (file: any, fileList: any[]) => {
  console.log('File changed:', file);
  fileList.value = fileList;
};

// 处理文件上传成功
const handleUploadSuccess = (response: any, file: any) => {
  console.log('Upload success:', response);
  announcementForm.attachments.push({
    name: file.name,
    url: response.url || `/api/files/download/${response.id || response.fileId || response.filename}`,
    size: file.size
  });
  ElMessage.success(`文件 ${file.name} 上传成功`);
};

// 处理文件上传失败
const handleUploadError = (error: any, file: any) => {
  console.error('Upload error:', error);
  ElMessage.error(`文件 ${file.name} 上传失败`);
};

// 处理文件移除
const handleFileRemove = (file: any) => {
  const index = announcementForm.attachments.findIndex(item => item.name === file.name);
  if (index !== -1) {
    announcementForm.attachments.splice(index, 1);
  }
};

// 提交公告表单
const submitAnnouncementForm = async () => {
  if (!announcementFormRef.value) return;
  
  await announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 准备公告数据
        const announcementData = {
          title: announcementForm.title,
          content: announcementForm.content,
          type: announcementForm.type,
          isPublished: announcementForm.status === 'PUBLISHED',
          attachments: announcementForm.attachments
        };
        
        if (isEdit.value) {
          // 更新公告
          await announcementAPI.updateAnnouncement(announcementForm.id, announcementData);
          ElMessage.success('公告更新成功');
        } else {
          // 创建公告
          await announcementAPI.createAnnouncement(announcementData);
          ElMessage.success('公告创建成功');
        }
        
        formDialogVisible.value = false;
        fetchAnnouncementList(); // 刷新公告列表
      } catch (error) {
        console.error('保存公告失败', error);
        ElMessage.error(isEdit.value ? '更新公告失败，请重试' : '创建公告失败，请重试');
      } finally {
        submitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确');
      return false;
    }
  });
};

// 分页相关
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchAnnouncementList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchAnnouncementList();
};

// 初始化加载
onMounted(() => {
  fetchAnnouncementList();
});
</script>

<style scoped>
.announcement-list {
  width: 100%;
}

.filter-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.table-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.announcement-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.announcement-header h2 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.announcement-meta {
  color: #909399;
  font-size: 14px;
  display: flex;
  gap: 20px;
}

.announcement-content {
  line-height: 1.6;
  margin-bottom: 20px;
}

.announcement-attachments {
  background-color: #f7f7f7;
  padding: 15px;
  border-radius: 4px;
}

.announcement-attachments h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
}

.attachment-list {
  padding-left: 20px;
  margin: 0;
}

.attachment-list li {
  margin-bottom: 5px;
}

.attachment-list a {
  color: #409eff;
  text-decoration: none;
}

.attachment-list a:hover {
  text-decoration: underline;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 新增样式 */
.flex {
  display: flex;
}

.space-x-2 > * + * {
  margin-left: 8px;
}

.flex .el-button {
  margin-left: 0;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  justify-content: flex-start;
}

.action-buttons .el-button {
  margin-left: 0;
  margin-right: 5px;
  margin-bottom: 5px;
}

/* 确保最后一个按钮没有右边距 */
.action-buttons .el-button:last-child {
  margin-right: 0;
}
</style> 