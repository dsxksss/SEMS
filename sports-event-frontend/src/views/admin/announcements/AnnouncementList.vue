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
              <template v-if="scope.row.status !== 'PUBLISHED'">
                <el-button
                  size="small"
                  type="warning"
                  @click="handlePublish(scope.row)"
                >发布</el-button>
              </template>
              <template v-else>
                <el-button
                  size="small"
                  type="warning"
                  @click="handleSetExpired(scope.row)"
                >撤回</el-button>
              </template>
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
          <div class="rich-editor-container">
            <el-input
              v-model="announcementForm.content"
              type="textarea"
              :rows="10"
              placeholder="请输入公告内容，支持HTML格式"
            />
            <div class="editor-tools">
              <el-upload
                action="/api/files/upload"
                :headers="{
                  Authorization: `Bearer ${authStore.token}`
                }"
                :show-file-list="false"
                :on-success="handleImageSuccess"
                :on-error="handleImageError"
                :before-upload="beforeImageUpload"
                :accept="'image/jpeg,image/png,image/gif'"
                name="file"
                :limit="1"
                :multiple="false"
              >
                <el-button type="primary" size="small">
                  <i class="el-icon-picture-outline"></i> 插入图片
                </el-button>
              </el-upload>
              <div class="editor-tip">
                提示：可以通过插入图片按钮上传图片，或直接粘贴HTML代码，如：&lt;img src="图片地址" alt="图片描述" style="width: 100%;"&gt;
              </div>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="announcementForm.status">
            <el-radio :label="'PUBLISHED'" value="PUBLISHED">立即发布</el-radio>
            <el-radio :label="'DRAFT'" value="DRAFT">保存草稿</el-radio>
          </el-radio-group>
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
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
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

interface EnhancedAnnouncement {
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
const announcementList = ref<EnhancedAnnouncement[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// 详情弹窗
const detailDialogVisible = ref(false);
const currentAnnouncement = ref<EnhancedAnnouncement | null>(null);

// 表单弹窗
const formDialogVisible = ref(false);
const announcementFormRef = ref<FormInstance>();
const isEdit = ref(false);
const submitting = ref(false);

// 删除确认对话框
const deleteDialogVisible = ref(false);
const deleteTarget = ref<EnhancedAnnouncement | null>(null);
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
      // 将API返回的published属性转为前端使用的status进行过滤
      if (filterForm.status) {
        // 根据published状态确定当前记录的status
        const itemStatus = item.published ? 'PUBLISHED' : 'DRAFT';
        if (itemStatus !== filterForm.status) {
          matches = false;
        }
      }
      return matches;
    });
    
    // 计算总数
    total.value = filteredAnnouncements.length;
    
    // 分页处理
    const startIndex = (currentPage.value - 1) * pageSize.value;
    const endIndex = startIndex + pageSize.value;
    
    // 格式化数据
    announcementList.value = filteredAnnouncements.slice(startIndex, endIndex).map(ann => {
      const formattedAttachments: Attachment[] = (ann.attachments || []).map((url: string) => ({
        name: url.split('/').pop() || '',
        url: url,
        size: 0
      }));
      
      return {
        id: ann.id,
        title: ann.title,
        content: ann.content,
        type: ann.type || (ann.eventId ? 'EVENT' : 'SYSTEM'),
        status: ann.published ? 'PUBLISHED' : 'DRAFT',
        createTime: ann.createdAt ? dayjs(ann.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-',
        publishTime: ann.published && ann.updatedAt ? dayjs(ann.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '-',
        createdBy: ann.createdBy || '管理员',
        viewCount: 0,
        attachments: formattedAttachments
      };
    });
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
const handleView = async (row: EnhancedAnnouncement) => {
  try {
    // 调用API获取详细信息
    const announcement = await announcementAPI.getAnnouncementById(row.id);
    
    // 处理内容中的图片URL，确保使用正确的路径格式
    let processedContent = announcement.content;
    if (processedContent) {
      // 查找所有图片URL，确保路径正确
      const imgRegex = /<img[^>]+src="([^"]+)"/g;
      processedContent = processedContent.replace(imgRegex, (match, url) => {
        // 移除URL中可能存在的token参数
        let cleanUrl = url.split('?')[0];
        
        // 确保图片URL包含/api前缀
        if (cleanUrl.includes('/files/download/') && !cleanUrl.startsWith('/api')) {
          cleanUrl = `/api${cleanUrl}`;
        }
        
        return match.replace(url, cleanUrl);
      });
    }
    
    // 格式化数据
    currentAnnouncement.value = {
      id: announcement.id,
      title: announcement.title, 
      content: processedContent || announcement.content,
      type: announcement.type || (announcement.eventId ? 'EVENT' : 'SYSTEM'),
      status: announcement.published ? 'PUBLISHED' : 'DRAFT',
      createTime: announcement.createdAt ? dayjs(announcement.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-',
      publishTime: announcement.published && announcement.updatedAt ? dayjs(announcement.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '-',
      createdBy: announcement.createdBy || '管理员',
      viewCount: 0,
      attachments: (announcement.attachments || []).map((url: string) => ({
        name: url.split('/').pop() || '',
        url: url,
        size: 0
      }))
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
  formDialogVisible.value = true;
};

// 编辑公告
const handleEdit = async (row: EnhancedAnnouncement) => {
  try {
    // 获取最新的公告详情
    const announcement = await announcementAPI.getAnnouncementById(row.id);
    
    isEdit.value = true;
    resetAnnouncementForm();
    
    // 设置表单数据
    announcementForm.id = announcement.id;
    announcementForm.title = announcement.title;
    announcementForm.content = announcement.content;
    announcementForm.type = announcement.type || (announcement.eventId ? 'EVENT' : 'SYSTEM');
    announcementForm.status = announcement.published ? 'PUBLISHED' : 'DRAFT';
    
    // 处理附件
    announcementForm.attachments = (announcement.attachments || []).map((url: string) => ({
      name: url.split('/').pop() || '',
      url: url,
      size: 0
    }));
    
    formDialogVisible.value = true;
  } catch (error) {
    console.error('获取公告详情失败', error);
    ElMessage.error('获取公告详情失败，请重试');
  }
};

// 发布公告
const handlePublish = (row: EnhancedAnnouncement) => {
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
const handleSetExpired = (row: EnhancedAnnouncement) => {
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
const handleDelete = (row: EnhancedAnnouncement) => {
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

// 提交公告表单
const submitAnnouncementForm = async () => {
  if (!announcementFormRef.value) return;
  
  await announcementFormRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 准备公告数据
        const announcementData = {
          title: announcementForm.title,
          content: announcementForm.content,
          type: announcementForm.type || 'SYSTEM',
          published: announcementForm.status === 'PUBLISHED',
          attachments: [] // 不使用附件功能
        };
        
        console.log('提交的公告数据:', announcementData);
        
        if (isEdit.value) {
          // 更新公告
          void announcementAPI.updateAnnouncement(announcementForm.id, announcementData);
          ElMessage.success('公告更新成功');
        } else {
          // 创建公告
          void announcementAPI.createAnnouncement(announcementData);
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

// 处理图片上传前的验证
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  
  return true;
};

// 处理图片上传成功
const handleImageSuccess = (response: any, file: any) => {
  console.log('上传成功，完整响应数据:', JSON.stringify(response));
  
  // 检查响应是否为undefined (防止重复调用)
  if (!response) {
    console.log('忽略undefined响应');
    return;
  }
  
  // 获取文件名
  let filename = null;
  
  // 处理后端FileUploadResponse格式的响应
  if (response.filename) {
    filename = response.filename;
    console.log('从FileUploadResponse获取文件名:', filename);
  } else if (response.data && response.data.filename) {
    filename = response.data.filename;
    console.log('从嵌套data对象获取文件名:', filename);
  } else if (typeof response === 'string') {
    filename = response;
    console.log('响应是字符串格式:', filename);
  }
  
  if (filename) {
    // 构建完整的图片URL
    const imageUrl = `/api/files/download/${filename}`;
    console.log('构建的图片URL:', imageUrl);
    
    // 在当前内容末尾插入图片HTML
    const imgHtml = `<img src="${imageUrl}" alt="${file.name || '上传图片'}" style="max-width: 100%; margin: 10px 0;" />`;
    announcementForm.content += imgHtml;
    ElMessage.success('图片插入成功');
  } else {
    ElMessage.error('无法从响应中获取文件名');
    console.error('无法解析的响应格式:', response);
  }
};

// 处理图片上传错误
const handleImageError = (error: any) => {
  console.error('图片上传失败:', error);
  
  // 分析错误类型
  if (error.status === 401 || (error.response && error.response.status === 401)) {
    ElMessage.error('认证已过期，请重新登录后再试');
    // 短暂延迟后登出
    setTimeout(() => {
      authStore.logout();
    }, 1500);
    return;
  } else if (error.status === 400 || (error.response && error.response.status === 400)) {
    // 处理请求参数错误
    const errorMessage = error.response?.data?.message || '文件格式不正确或大小超出限制';
    ElMessage.error(`上传失败: ${errorMessage}`);
    return;
  } else if (error.status === 413 || (error.response && error.response.status === 413)) {
    // 处理文件过大错误
    ElMessage.error('文件大小超出服务器限制，请使用小于10MB的图片');
    return;
  }
  
  // 网络错误
  if (error.message && error.message.includes('Network Error')) {
    ElMessage.error('网络连接错误，请检查您的网络连接后重试');
    return;
  }
  
  // 其他未知错误
  ElMessage.error('图片上传失败，请检查网络连接或重新登录后再试');
  
  // 输出详细错误信息以便调试
  console.log('错误详情:', {
    status: error.status || (error.response && error.response.status),
    statusText: error.statusText || (error.response && error.response.statusText),
    message: error.message,
    data: error.response && error.response.data
  });
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

/* 富文本编辑器相关样式 */
.rich-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-tools {
  padding: 8px;
  background-color: #f5f7fa;
  border-top: 1px solid #dcdfe6;
}

.editor-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

/* 确保图片在查看时正常显示 */
.announcement-content img {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style> 