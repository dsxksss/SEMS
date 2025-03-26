<template>
  <admin-layout>
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
          <h3>公告列表</h3>
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
          <el-table-column label="操作" fixed="right" width="220">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click="handleView(scope.row)"
                >查看</el-button>
              <el-button
                v-if="scope.row.status !== 'EXPIRED'"
                size="small"
                type="success"
                @click="handleEdit(scope.row)"
                >编辑</el-button>
              <el-button
                v-if="scope.row.status === 'DRAFT'"
                size="small"
                type="warning"
                @click="handlePublish(scope.row)"
                >发布</el-button>
              <el-button
                v-if="scope.row.status === 'PUBLISHED'"
                size="small"
                type="info"
                @click="handleSetExpired(scope.row)"
                >过期</el-button>
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
              action="#"
              :auto-upload="false"
              :file-list="fileList"
              :on-change="handleFileChange"
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
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import AdminLayout from '../../../components/AdminLayout.vue';

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
    // 实际应用中调用API获取公告列表
    // const response = await announcementAPI.getAnnouncementList({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   title: filterForm.title,
    //   type: filterForm.type,
    //   status: filterForm.status
    // });
    // announcementList.value = response.content;
    // total.value = response.totalElements;
    
    // 使用模拟数据
    setTimeout(() => {
      announcementList.value = [
        {
          id: 1,
          title: '2023年校园马拉松赛事报名开始',
          content: '<p>各位同学，2023年校园马拉松赛事报名现已开始，请有意参加的同学尽快报名。</p><p>报名时间：2023年4月15日至2023年5月10日</p><p>比赛时间：2023年5月15日</p><p>比赛地点：校园田径场</p>',
          type: 'EVENT',
          status: 'PUBLISHED',
          createTime: '2023-04-10 09:30:00',
          publishTime: '2023-04-10 10:00:00',
          createdBy: 'admin',
          viewCount: 356,
          attachments: [
            { name: '报名须知.pdf', url: '/uploads/registration_guide.pdf' },
            { name: '比赛规则.pdf', url: '/uploads/race_rules.pdf' }
          ]
        },
        {
          id: 2,
          title: '大学生篮球联赛报名通知',
          content: '<p>大学生篮球联赛将于5月1日开始报名，请各学院组织队伍参赛。</p><p>每队人数：10-15人</p><p>报名方式：通过系统报名</p>',
          type: 'REGISTRATION',
          status: 'PUBLISHED',
          createTime: '2023-04-15 14:20:00',
          publishTime: '2023-04-15 15:00:00',
          createdBy: 'admin',
          viewCount: 245
        },
        {
          id: 3,
          title: '关于游泳比赛裁判选拔的通知',
          content: '<p>我们正在招募游泳比赛的裁判，有意向者请填写申请表。</p><p>要求：熟悉游泳比赛规则，有相关经验</p>',
          type: 'EVENT',
          status: 'DRAFT',
          createTime: '2023-03-20 11:10:00',
          createdBy: 'event_manager',
          viewCount: 0
        },
        {
          id: 4,
          title: '系统维护通知',
          content: '<p>系统将于2023年4月30日凌晨2:00-4:00进行维护升级，期间系统将不可用，请提前安排相关工作。</p>',
          type: 'SYSTEM',
          status: 'PUBLISHED',
          createTime: '2023-04-28 16:45:00',
          publishTime: '2023-04-28 17:00:00',
          createdBy: 'admin',
          viewCount: 189
        },
        {
          id: 5,
          title: '冬季滑冰比赛成绩公布',
          content: '<p>冬季滑冰比赛已圆满结束，各项比赛成绩已公布在附件中，获奖选手请于下周一到体育部领取奖品。</p>',
          type: 'EVENT',
          status: 'EXPIRED',
          createTime: '2023-01-16 10:20:00',
          publishTime: '2023-01-16 11:00:00',
          createdBy: 'admin',
          viewCount: 312,
          attachments: [
            { name: '比赛成绩.xlsx', url: '/uploads/skating_results.xlsx' }
          ]
        }
      ];
      
      // 根据搜索条件过滤
      if (filterForm.title) {
        announcementList.value = announcementList.value.filter(item => 
          item.title.toLowerCase().includes(filterForm.title.toLowerCase())
        );
      }
      if (filterForm.type) {
        announcementList.value = announcementList.value.filter(item => 
          item.type === filterForm.type
        );
      }
      if (filterForm.status) {
        announcementList.value = announcementList.value.filter(item => 
          item.status === filterForm.status
        );
      }
      
      total.value = announcementList.value.length;
      loading.value = false;
    }, 500);
  } catch (error) {
    console.error('获取公告列表失败', error);
    ElMessage.error('获取公告列表失败，请刷新重试');
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
const handleView = (row: Announcement) => {
  currentAnnouncement.value = row;
  detailDialogVisible.value = true;
  
  // 实际应用中更新浏览量
  // announcementAPI.incrementViewCount(row.id);
  
  // 模拟更新浏览量
  const index = announcementList.value.findIndex(item => item.id === row.id);
  if (index > -1) {
    announcementList.value[index].viewCount += 1;
    if (currentAnnouncement.value) {
      currentAnnouncement.value.viewCount += 1;
    }
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
const handleEdit = (row: Announcement) => {
  isEdit.value = true;
  resetAnnouncementForm();
  
  announcementForm.id = row.id;
  announcementForm.title = row.title;
  announcementForm.content = row.content;
  announcementForm.type = row.type;
  announcementForm.status = row.status === 'EXPIRED' ? 'PUBLISHED' : row.status;
  announcementForm.attachments = row.attachments || [];
  
  // 设置文件列表
  fileList.value = row.attachments 
    ? row.attachments.map(attachment => ({
        name: attachment.name,
        url: attachment.url
      })) 
    : [];
  
  formDialogVisible.value = true;
};

// 发布草稿公告
const handlePublish = (row: Announcement) => {
  ElMessage.success(`公告"${row.title}"已发布`);
  
  // 实际应用中调用API更新状态
  // await announcementAPI.publishAnnouncement(row.id);
  
  // 模拟成功
  const index = announcementList.value.findIndex(item => item.id === row.id);
  if (index > -1) {
    announcementList.value[index].status = 'PUBLISHED';
    announcementList.value[index].publishTime = new Date().toLocaleString();
  }
};

// 设置公告为过期
const handleSetExpired = (row: Announcement) => {
  ElMessage.success(`公告"${row.title}"已设为过期`);
  
  // 实际应用中调用API更新状态
  // await announcementAPI.expireAnnouncement(row.id);
  
  // 模拟成功
  const index = announcementList.value.findIndex(item => item.id === row.id);
  if (index > -1) {
    announcementList.value[index].status = 'EXPIRED';
  }
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
    // 实际应用中调用API删除公告
    // await announcementAPI.deleteAnnouncement(deleteTarget.value.id);
    
    // 模拟成功
    setTimeout(() => {
      announcementList.value = announcementList.value.filter(
        item => item.id !== deleteTarget.value?.id
      );
      total.value -= 1;
      
      ElMessage.success('公告删除成功');
      deleteDialogVisible.value = false;
      deleting.value = false;
      
      // 如果正在查看该公告，关闭详情弹窗
      if (currentAnnouncement.value && currentAnnouncement.value.id === deleteTarget.value?.id) {
        detailDialogVisible.value = false;
      }
    }, 500);
  } catch (error) {
    console.error('删除公告失败', error);
    ElMessage.error('删除公告失败，请重试');
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
  fileList.value = fileList;
};

// 提交公告表单
const submitAnnouncementForm = async () => {
  if (!announcementFormRef.value) return;
  
  await announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 处理附件
        announcementForm.attachments = fileList.value.map(file => ({
          name: file.name,
          url: file.url || `/uploads/${file.name}` // 模拟上传后的URL
        }));
        
        // 实际应用中调用API保存公告
        // if (isEdit.value) {
        //   await announcementAPI.updateAnnouncement(announcementForm.id, announcementForm);
        // } else {
        //   await announcementAPI.createAnnouncement(announcementForm);
        // }
        
        // 模拟成功
        setTimeout(() => {
          ElMessage.success(isEdit.value ? '公告更新成功' : '公告保存成功');
          formDialogVisible.value = false;
          fetchAnnouncementList();
          submitting.value = false;
        }, 1000);
      } catch (error) {
        console.error('保存公告失败', error);
        ElMessage.error(isEdit.value ? '更新公告失败，请重试' : '创建公告失败，请重试');
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
</style> 