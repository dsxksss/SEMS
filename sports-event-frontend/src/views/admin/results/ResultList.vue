<template>
  <admin-layout>
    <div class="result-list">
      <div class="filter-container">
        <el-form :inline="true" :model="filterForm" class="form-inline">
          <el-form-item label="赛事名称">
            <el-select v-model="filterForm.eventId" placeholder="选择赛事" clearable style="width: 220px">
              <el-option
                v-for="event in events"
                :key="event.id"
                :label="event.name"
                :value="event.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="运动员姓名">
            <el-input v-model="filterForm.athleteName" placeholder="输入运动员姓名搜索" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchResults">搜索</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <div class="table-header">
          <h3>赛事结果列表</h3>
          <div class="table-actions">
            <el-button type="primary" @click="showAddResultDialog">录入成绩</el-button>
            <el-button type="success" @click="exportResults">导出成绩数据</el-button>
          </div>
        </div>

        <el-tabs v-model="activeTabName" class="result-tabs" @tab-click="handleTabClick">
          <el-tab-pane label="所有赛事" name="all"></el-tab-pane>
          <el-tab-pane
            v-for="event in activeEvents"
            :key="event.id"
            :label="event.name"
            :name="event.id.toString()"
          ></el-tab-pane>
        </el-tabs>

        <el-table
          v-loading="loading"
          :data="resultList"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="eventName" label="赛事名称" width="200" show-overflow-tooltip />
          <el-table-column prop="athleteName" label="运动员" width="120" />
          <el-table-column prop="score" label="成绩" width="120" />
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="unit" label="单位" width="120" />
          <el-table-column prop="category" label="组别" width="120" />
          <el-table-column prop="recordTime" label="记录时间" width="180" />
          <el-table-column prop="recorder" label="记录人" width="120" />
          <el-table-column label="操作" fixed="right" width="150">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click="handleEdit(scope.row)"
                >编辑</el-button>
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

      <!-- 添加/编辑结果弹窗 -->
      <el-dialog
        :title="isEdit ? '编辑成绩' : '录入成绩'"
        v-model="resultDialogVisible"
        width="600px"
      >
        <el-form 
          ref="resultFormRef"
          :model="resultForm"
          :rules="rules"
          label-width="100px"
          label-position="right"
        >
          <el-form-item label="赛事" prop="eventId">
            <el-select v-model="resultForm.eventId" placeholder="选择赛事" style="width: 100%">
              <el-option
                v-for="event in events"
                :key="event.id"
                :label="event.name"
                :value="event.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="运动员" prop="athleteId">
            <el-select 
              v-model="resultForm.athleteId" 
              placeholder="选择运动员" 
              filterable 
              remote
              :remote-method="searchAthletes"
              :loading="athleteLoading"
              style="width: 100%"
            >
              <el-option
                v-for="athlete in athletes"
                :key="athlete.id"
                :label="athlete.name"
                :value="athlete.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="成绩" prop="score">
            <el-input v-model="resultForm.score" placeholder="请输入成绩" />
          </el-form-item>

          <el-form-item label="排名" prop="rank">
            <el-input-number v-model="resultForm.rank" :min="1" style="width: 100%" />
          </el-form-item>
          
          <el-form-item label="单位" prop="unit">
            <el-input v-model="resultForm.unit" placeholder="请输入单位" />
          </el-form-item>

          <el-form-item label="组别" prop="category">
            <el-input v-model="resultForm.category" placeholder="请输入组别" />
          </el-form-item>

          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="resultForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="resultDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitResultForm" :loading="submitting">保存</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 删除确认对话框 -->
      <el-dialog
        title="确认删除"
        v-model="deleteDialogVisible"
        width="400px"
      >
        <p>确定要删除该成绩记录吗？删除后无法恢复。</p>
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
import { ref, reactive, onMounted, FormInstance, FormRules } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import AdminLayout from '../../../components/AdminLayout.vue';

interface Event {
  id: number;
  name: string;
  status: string;
}

interface Athlete {
  id: number;
  name: string;
}

interface Result {
  id: number;
  eventId: number;
  eventName: string;
  athleteId: number;
  athleteName: string;
  score: string;
  rank: number;
  unit: string;
  category: string;
  recordTime: string;
  recorder: string;
  remark?: string;
}

// 过滤表单
const filterForm = reactive({
  eventId: null as number | null,
  athleteName: ''
});

// 比赛结果列表数据
const resultList = ref<Result[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);
const activeTabName = ref('all');

// 赛事数据
const events = ref<Event[]>([]);
const activeEvents = ref<Event[]>([]);

// 运动员数据
const athletes = ref<Athlete[]>([]);
const athleteLoading = ref(false);

// 添加/编辑结果弹窗
const resultDialogVisible = ref(false);
const resultFormRef = ref<FormInstance>();
const isEdit = ref(false);
const submitting = ref(false);

// 删除确认对话框
const deleteDialogVisible = ref(false);
const deleteTarget = ref<Result | null>(null);
const deleting = ref(false);

// 结果表单
const resultForm = reactive({
  id: 0,
  eventId: null as number | null,
  athleteId: null as number | null,
  score: '',
  rank: 1,
  unit: '',
  category: '',
  remark: ''
});

// 表单验证规则
const rules = reactive<FormRules>({
  eventId: [
    { required: true, message: '请选择赛事', trigger: 'change' }
  ],
  athleteId: [
    { required: true, message: '请选择运动员', trigger: 'change' }
  ],
  score: [
    { required: true, message: '请输入成绩', trigger: 'blur' }
  ],
  rank: [
    { required: true, message: '请输入排名', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请输入组别', trigger: 'blur' }
  ]
});

// 加载赛事列表
const loadEvents = async () => {
  try {
    // 实际应用中调用API获取赛事列表
    // const response = await eventsAPI.getActiveEvents();
    // events.value = response.data;
    
    // 使用模拟数据
    events.value = [
      { id: 1, name: '2023年校园马拉松', status: 'IN_PROGRESS' },
      { id: 2, name: '大学生篮球联赛', status: 'IN_PROGRESS' },
      { id: 3, name: '游泳比赛', status: 'IN_PROGRESS' },
      { id: 4, name: '校园足球杯', status: 'REGISTRATION' },
      { id: 5, name: '冬季滑冰比赛', status: 'ENDED' }
    ];
    
    // 过滤出进行中和已结束的赛事作为标签
    activeEvents.value = events.value.filter(event => 
      ['IN_PROGRESS', 'ENDED'].includes(event.status)
    );
  } catch (error) {
    console.error('获取赛事列表失败', error);
    ElMessage.error('获取赛事列表失败');
  }
};

// 获取成绩列表
const fetchResultList = async () => {
  loading.value = true;
  try {
    // 实际应用中调用API获取成绩列表
    // const eventId = activeTabName.value === 'all' ? undefined : Number(activeTabName.value);
    // const response = await resultAPI.getResultList({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   eventId: eventId,
    //   athleteName: filterForm.athleteName
    // });
    // resultList.value = response.content;
    // total.value = response.totalElements;
    
    // 使用模拟数据
    setTimeout(() => {
      const mockResults = [
        {
          id: 1,
          eventId: 1,
          eventName: '2023年校园马拉松',
          athleteId: 1,
          athleteName: '张三',
          score: '00:35:42',
          rank: 1,
          unit: '分钟',
          category: '男子10公里组',
          recordTime: '2023-05-15 15:30:00',
          recorder: 'admin',
          remark: '破纪录成绩'
        },
        {
          id: 2,
          eventId: 1,
          eventName: '2023年校园马拉松',
          athleteId: 2,
          athleteName: '李四',
          score: '00:36:15',
          rank: 2,
          unit: '分钟',
          category: '男子10公里组',
          recordTime: '2023-05-15 15:30:10',
          recorder: 'admin'
        },
        {
          id: 3,
          eventId: 1,
          eventName: '2023年校园马拉松',
          athleteId: 6,
          athleteName: '孙八',
          score: '00:18:22',
          rank: 1,
          unit: '分钟',
          category: '男子5公里组',
          recordTime: '2023-05-15 14:15:00',
          recorder: 'admin'
        },
        {
          id: 4,
          eventId: 3,
          eventName: '游泳比赛',
          athleteId: 4,
          athleteName: '赵六',
          score: '00:01:05',
          rank: 1,
          unit: '分钟',
          category: '男子100米自由泳',
          recordTime: '2023-04-20 10:20:00',
          recorder: 'event_manager'
        },
        {
          id: 5,
          eventId: 3,
          eventName: '游泳比赛',
          athleteId: 7,
          athleteName: '周九',
          score: '00:01:12',
          rank: 1,
          unit: '分钟',
          category: '女子100米自由泳',
          recordTime: '2023-04-20 11:00:00',
          recorder: 'event_manager'
        },
        {
          id: 6,
          eventId: 5,
          eventName: '冬季滑冰比赛',
          athleteId: 8,
          athleteName: '吴十',
          score: '00:02:45',
          rank: 1,
          unit: '分钟',
          category: '女子500米速滑',
          recordTime: '2023-01-15 14:00:00',
          recorder: 'admin'
        }
      ];
      
      // 根据标签过滤数据
      if (activeTabName.value !== 'all') {
        const eventId = Number(activeTabName.value);
        resultList.value = mockResults.filter(result => result.eventId === eventId);
      } else {
        resultList.value = mockResults;
      }
      
      // 根据搜索条件过滤
      if (filterForm.eventId) {
        resultList.value = resultList.value.filter(result => result.eventId === filterForm.eventId);
      }
      if (filterForm.athleteName) {
        resultList.value = resultList.value.filter(result => 
          result.athleteName.toLowerCase().includes(filterForm.athleteName.toLowerCase())
        );
      }
      
      total.value = resultList.value.length;
      loading.value = false;
    }, 500);
  } catch (error) {
    console.error('获取成绩列表失败', error);
    ElMessage.error('获取成绩列表失败，请刷新重试');
    loading.value = false;
  }
};

// 搜索成绩
const searchResults = () => {
  currentPage.value = 1;
  fetchResultList();
};

// 重置表单
const resetForm = () => {
  filterForm.eventId = null;
  filterForm.athleteName = '';
  searchResults();
};

// 处理标签切换
const handleTabClick = () => {
  currentPage.value = 1;
  fetchResultList();
};

// 搜索运动员
const searchAthletes = async (query: string) => {
  if (query) {
    athleteLoading.value = true;
    // 实际应用中调用API搜索运动员
    // const response = await athleteAPI.searchAthletes({ name: query });
    // athletes.value = response.data;
    
    // 使用模拟数据
    setTimeout(() => {
      athletes.value = [
        { id: 1, name: '张三' },
        { id: 2, name: '李四' },
        { id: 3, name: '王五' },
        { id: 4, name: '赵六' },
        { id: 5, name: '钱七' },
        { id: 6, name: '孙八' },
        { id: 7, name: '周九' },
        { id: 8, name: '吴十' }
      ].filter(athlete => athlete.name.includes(query));
      athleteLoading.value = false;
    }, 300);
  } else {
    athletes.value = [];
  }
};

// 显示添加结果对话框
const showAddResultDialog = () => {
  isEdit.value = false;
  resetResultForm();
  resultDialogVisible.value = true;
};

// 编辑结果
const handleEdit = (row: Result) => {
  isEdit.value = true;
  resetResultForm();
  
  resultForm.id = row.id;
  resultForm.eventId = row.eventId;
  resultForm.athleteId = row.athleteId;
  resultForm.score = row.score;
  resultForm.rank = row.rank;
  resultForm.unit = row.unit;
  resultForm.category = row.category;
  resultForm.remark = row.remark || '';
  
  // 确保运动员选项中有当前运动员
  athletes.value = [{ id: row.athleteId, name: row.athleteName }];
  
  resultDialogVisible.value = true;
};

// 重置结果表单
const resetResultForm = () => {
  resultForm.id = 0;
  resultForm.eventId = null;
  resultForm.athleteId = null;
  resultForm.score = '';
  resultForm.rank = 1;
  resultForm.unit = '';
  resultForm.category = '';
  resultForm.remark = '';
  
  if (resultFormRef.value) {
    resultFormRef.value.resetFields();
  }
};

// 提交结果表单
const submitResultForm = async () => {
  if (!resultFormRef.value) return;
  
  await resultFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 实际应用中调用API保存结果
        // if (isEdit.value) {
        //   await resultAPI.updateResult(resultForm.id, resultForm);
        // } else {
        //   await resultAPI.createResult(resultForm);
        // }
        
        // 模拟成功
        setTimeout(() => {
          ElMessage.success(isEdit.value ? '成绩更新成功' : '成绩录入成功');
          resultDialogVisible.value = false;
          fetchResultList();
          submitting.value = false;
        }, 1000);
      } catch (error) {
        console.error('保存成绩失败', error);
        ElMessage.error(isEdit.value ? '更新成绩失败，请重试' : '录入成绩失败，请重试');
        submitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确');
      return false;
    }
  });
};

// 删除结果
const handleDelete = (row: Result) => {
  deleteTarget.value = row;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  if (!deleteTarget.value) return;
  
  deleting.value = true;
  try {
    // 实际应用中调用API删除结果
    // await resultAPI.deleteResult(deleteTarget.value.id);
    
    // 模拟成功
    setTimeout(() => {
      ElMessage.success('成绩删除成功');
      deleteDialogVisible.value = false;
      fetchResultList();
      deleting.value = false;
    }, 1000);
  } catch (error) {
    console.error('删除成绩失败', error);
    ElMessage.error('删除成绩失败，请重试');
    deleting.value = false;
  }
};

// 导出成绩数据
const exportResults = () => {
  ElMessage.success('导出成功，请在下载列表中查看');
};

// 分页相关
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchResultList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchResultList();
};

// 初始化加载
onMounted(() => {
  loadEvents();
  fetchResultList();
});
</script>

<style scoped>
.result-list {
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

.table-actions {
  display: flex;
  gap: 10px;
}

.result-tabs {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 