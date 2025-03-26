<template>
  <admin-layout>
    <div class="report-center">
      <div class="page-header">
        <h2>报表汇总打印</h2>
      </div>
      
      <div class="cards-container">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="report-card">
              <template #header>
                <div class="card-header">
                  <span>赛事报名统计</span>
                  <el-tag type="success">实时更新</el-tag>
                </div>
              </template>
              <div class="card-content">
                <div class="chart-container">
                  <div ref="registrationChartRef" style="width: 100%; height: 300px;"></div>
                </div>
                <div class="card-actions">
                  <el-button type="primary" @click="handleExportRegistrationReport">
                    <el-icon><document /></el-icon>
                    导出报名统计报表
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="report-card">
              <template #header>
                <div class="card-header">
                  <span>赛事结果统计</span>
                  <el-tag type="warning">每日更新</el-tag>
                </div>
              </template>
              <div class="card-content">
                <div class="chart-container">
                  <div ref="resultChartRef" style="width: 100%; height: 300px;"></div>
                </div>
                <div class="card-actions">
                  <el-button type="primary" @click="handleExportResultReport">
                    <el-icon><document /></el-icon>
                    导出成绩统计报表
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="report-card">
              <template #header>
                <div class="card-header">
                  <span>系统用户活跃度</span>
                  <el-tag type="info">每周更新</el-tag>
                </div>
              </template>
              <div class="card-content">
                <div class="chart-container">
                  <div ref="userActivityChartRef" style="width: 100%; height: 300px;"></div>
                </div>
                <div class="card-actions">
                  <el-button type="primary" @click="handleExportUserActivityReport">
                    <el-icon><document /></el-icon>
                    导出用户活跃报表
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="report-table-container">
        <div class="section-header">
          <h3>赛事运营报表</h3>
          <div class="filter-controls">
            <el-select v-model="selectedYear" placeholder="选择年份" style="width: 120px">
              <el-option label="2023年" value="2023" />
              <el-option label="2022年" value="2022" />
              <el-option label="2021年" value="2021" />
            </el-select>
          </div>
        </div>
        
        <el-table
          v-loading="loading"
          :data="reportTableData"
          border
          style="width: 100%"
        >
          <el-table-column prop="month" label="月份" width="80" />
          <el-table-column prop="eventsCount" label="赛事数量" width="100" />
          <el-table-column prop="registrationsCount" label="报名人数" width="100" />
          <el-table-column prop="completionRate" label="完赛率" width="100">
            <template #default="scope">
              <el-progress :percentage="scope.row.completionRate * 100" :format="percentFormat" />
            </template>
          </el-table-column>
          <el-table-column prop="maleProportion" label="男女比例" width="160">
            <template #default="scope">
              <div class="gender-ratio">
                <div class="male-ratio" :style="{ width: `${scope.row.maleProportion * 100}%` }">
                  {{ Math.round(scope.row.maleProportion * 100) }}%
                </div>
                <div class="female-ratio" :style="{ width: `${(1 - scope.row.maleProportion) * 100}%` }">
                  {{ Math.round((1 - scope.row.maleProportion) * 100) }}%
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="avgDuration" label="平均完赛时间" width="120" />
          <el-table-column prop="satisfaction" label="满意度" width="200">
            <template #default="scope">
              <el-rate
                v-model="scope.row.satisfaction"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </template>
          </el-table-column>
          <el-table-column prop="expectedIncome" label="预期收入" width="120" />
          <el-table-column prop="actualIncome" label="实际收入" width="120" />
          <el-table-column prop="actions" label="操作" width="120">
            <template #default="scope">
              <el-button size="small" type="primary" @click="downloadMonthlyReport(scope.row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="print-section">
        <div class="section-header">
          <h3>赛事证书打印</h3>
        </div>
        
        <el-form :inline="true" :model="certificateForm" class="certificate-form">
          <el-form-item label="赛事名称">
            <el-select v-model="certificateForm.eventId" placeholder="选择赛事" style="width: 220px">
              <el-option
                v-for="event in events"
                :key="event.id"
                :label="event.name"
                :value="event.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="证书类型">
            <el-select v-model="certificateForm.type" placeholder="选择证书类型">
              <el-option label="参与证书" value="PARTICIPATION" />
              <el-option label="完赛证书" value="COMPLETION" />
              <el-option label="获奖证书" value="AWARD" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="generateCertificates">生成证书</el-button>
            <el-button type="success" @click="batchPrintCertificates">批量打印</el-button>
          </el-form-item>
        </el-form>
        
        <el-table
          v-if="certificateForm.eventId"
          v-loading="certificateLoading"
          :data="certificateList"
          border
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="athleteName" label="运动员姓名" width="120" />
          <el-table-column prop="eventName" label="赛事名称" width="200" show-overflow-tooltip />
          <el-table-column prop="type" label="证书类型" width="120">
            <template #default="scope">
              {{ formatCertificateType(scope.row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="rank" label="名次" width="80" />
          <el-table-column prop="generateTime" label="生成时间" width="180" />
          <el-table-column prop="printed" label="打印状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.printed ? 'success' : 'info'">
                {{ scope.row.printed ? '已打印' : '未打印' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" type="primary" @click="previewCertificate(scope.row)">预览</el-button>
              <el-button size="small" type="success" @click="printCertificate(scope.row)">打印</el-button>
              <el-button size="small" type="info" @click="downloadCertificate(scope.row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import AdminLayout from '../../../components/AdminLayout.vue';
import { Document } from '@element-plus/icons-vue';
import * as echarts from 'echarts';

// 图表引用
const registrationChartRef = ref();
const resultChartRef = ref();
const userActivityChartRef = ref();
let registrationChart: echarts.ECharts | null = null;
let resultChart: echarts.ECharts | null = null;
let userActivityChart: echarts.ECharts | null = null;

// 报表数据
const loading = ref(false);
const selectedYear = ref('2023');
const reportTableData = ref([
  {
    month: '1月',
    eventsCount: 2,
    registrationsCount: 120,
    completionRate: 0.92,
    maleProportion: 0.65,
    avgDuration: '45分钟',
    satisfaction: 4.2,
    expectedIncome: '¥12,000',
    actualIncome: '¥13,500'
  },
  {
    month: '2月',
    eventsCount: 3,
    registrationsCount: 180,
    completionRate: 0.88,
    maleProportion: 0.60,
    avgDuration: '42分钟',
    satisfaction: 4.5,
    expectedIncome: '¥18,000',
    actualIncome: '¥19,200'
  },
  {
    month: '3月',
    eventsCount: 4,
    registrationsCount: 220,
    completionRate: 0.85,
    maleProportion: 0.58,
    avgDuration: '48分钟',
    satisfaction: 4.1,
    expectedIncome: '¥22,000',
    actualIncome: '¥21,500'
  },
  {
    month: '4月',
    eventsCount: 5,
    registrationsCount: 280,
    completionRate: 0.90,
    maleProportion: 0.62,
    avgDuration: '40分钟',
    satisfaction: 4.7,
    expectedIncome: '¥28,000',
    actualIncome: '¥30,500'
  },
  {
    month: '5月',
    eventsCount: 4,
    registrationsCount: 240,
    completionRate: 0.86,
    maleProportion: 0.55,
    avgDuration: '44分钟',
    satisfaction: 4.3,
    expectedIncome: '¥24,000',
    actualIncome: '¥25,200'
  },
  {
    month: '6月',
    eventsCount: 6,
    registrationsCount: 350,
    completionRate: 0.91,
    maleProportion: 0.59,
    avgDuration: '38分钟',
    satisfaction: 4.6,
    expectedIncome: '¥35,000',
    actualIncome: '¥37,800'
  }
]);

// 证书打印相关
const events = ref([
  { id: 1, name: '2023年校园马拉松' },
  { id: 2, name: '大学生篮球联赛' },
  { id: 3, name: '游泳比赛' },
  { id: 4, name: '校园足球杯' },
  { id: 5, name: '冬季滑冰比赛' }
]);

const certificateForm = reactive({
  eventId: null as number | null,
  type: ''
});

const certificateLoading = ref(false);
const certificateList = ref([
  {
    id: 1,
    athleteName: '张三',
    eventName: '2023年校园马拉松',
    eventId: 1,
    type: 'AWARD',
    rank: 1,
    generateTime: '2023-05-16 10:30:00',
    printed: true
  },
  {
    id: 2,
    athleteName: '李四',
    eventName: '2023年校园马拉松',
    eventId: 1,
    type: 'AWARD',
    rank: 2,
    generateTime: '2023-05-16 10:30:10',
    printed: true
  },
  {
    id: 3,
    athleteName: '王五',
    eventName: '2023年校园马拉松',
    eventId: 1,
    type: 'AWARD',
    rank: 3,
    generateTime: '2023-05-16 10:30:20',
    printed: false
  },
  {
    id: 4,
    athleteName: '赵六',
    eventName: '2023年校园马拉松',
    eventId: 1,
    type: 'COMPLETION',
    rank: 0,
    generateTime: '2023-05-16 10:35:00',
    printed: true
  },
  {
    id: 5,
    athleteName: '钱七',
    eventName: '2023年校园马拉松',
    eventId: 1,
    type: 'COMPLETION',
    rank: 0,
    generateTime: '2023-05-16 10:35:10',
    printed: false
  }
]);

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 初始化报名统计图表
    if (registrationChartRef.value) {
      registrationChart = echarts.init(registrationChartRef.value);
      const registrationOption = {
        title: {
          text: '各赛事报名人数统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['校园马拉松', '篮球联赛', '游泳比赛', '足球杯', '滑冰比赛']
        },
        series: [
          {
            name: '报名人数',
            type: 'pie',
            radius: '50%',
            data: [
              { value: 85, name: '校园马拉松' },
              { value: 12, name: '篮球联赛' },
              { value: 32, name: '游泳比赛' },
              { value: 8, name: '足球杯' },
              { value: 28, name: '滑冰比赛' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      registrationChart.setOption(registrationOption);
    }

    // 初始化比赛结果图表
    if (resultChartRef.value) {
      resultChart = echarts.init(resultChartRef.value);
      const resultOption = {
        title: {
          text: '赛事完成率',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: ['马拉松', '篮球', '游泳', '足球', '滑冰']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '完成率',
            type: 'bar',
            data: [92, 85, 95, 88, 90],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            }
          }
        ]
      };
      resultChart.setOption(resultOption);
    }

    // 初始化用户活跃度图表
    if (userActivityChartRef.value) {
      userActivityChart = echarts.init(userActivityChartRef.value);
      const userActivityOption = {
        title: {
          text: '用户活跃度趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '活跃用户',
            type: 'line',
            data: [120, 132, 101, 134, 90, 230, 210],
            smooth: true,
            lineStyle: {
              width: 3,
              shadowColor: 'rgba(0,0,0,0.3)',
              shadowBlur: 10,
              shadowOffsetY: 8
            },
            itemStyle: {
              color: '#6086e0'
            }
          }
        ]
      };
      userActivityChart.setOption(userActivityOption);
    }
  });
};

// 格式化百分比
const percentFormat = (percentage: number) => {
  return percentage + '%';
};

// 格式化证书类型
const formatCertificateType = (type: string) => {
  switch (type) {
    case 'PARTICIPATION':
      return '参与证书';
    case 'COMPLETION':
      return '完赛证书';
    case 'AWARD':
      return '获奖证书';
    default:
      return type;
  }
};

// 导出报表
const handleExportRegistrationReport = () => {
  ElMessage.success('报名统计报表导出成功');
};

const handleExportResultReport = () => {
  ElMessage.success('成绩统计报表导出成功');
};

const handleExportUserActivityReport = () => {
  ElMessage.success('用户活跃报表导出成功');
};

// 下载月度报表
const downloadMonthlyReport = (row: any) => {
  ElMessage.success(`${selectedYear.value}年${row.month}的月度报表下载成功`);
};

// 证书相关功能
const generateCertificates = () => {
  if (!certificateForm.eventId || !certificateForm.type) {
    ElMessage.warning('请先选择赛事和证书类型');
    return;
  }
  
  certificateLoading.value = true;
  setTimeout(() => {
    ElMessage.success('证书生成成功');
    certificateLoading.value = false;
  }, 1000);
};

const batchPrintCertificates = () => {
  ElMessage.success('已发送批量打印任务到打印队列');
};

const previewCertificate = (certificate: any) => {
  ElMessage.success(`正在预览${certificate.athleteName}的${formatCertificateType(certificate.type)}`);
};

const printCertificate = (certificate: any) => {
  ElMessage.success(`正在打印${certificate.athleteName}的${formatCertificateType(certificate.type)}`);
};

const downloadCertificate = (certificate: any) => {
  ElMessage.success(`${certificate.athleteName}的${formatCertificateType(certificate.type)}下载成功`);
};

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  registrationChart?.resize();
  resultChart?.resize();
  userActivityChart?.resize();
};

onMounted(() => {
  initCharts();
  // 添加窗口resize事件监听
  window.addEventListener('resize', handleResize);
});
</script>

<style scoped>
.report-center {
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

.cards-container {
  margin-bottom: 30px;
}

.report-card {
  height: 100%;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  display: flex;
  flex-direction: column;
}

.chart-container {
  margin-bottom: 15px;
}

.card-actions {
  display: flex;
  justify-content: center;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.report-table-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 30px;
}

.gender-ratio {
  display: flex;
  width: 100%;
  height: 20px;
  border-radius: 10px;
  overflow: hidden;
}

.male-ratio {
  background-color: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.female-ratio {
  background-color: #ff9ef0;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.print-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.certificate-form {
  margin-bottom: 20px;
}
</style> 