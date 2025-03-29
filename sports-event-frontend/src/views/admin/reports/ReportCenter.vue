<template>
  <div class="report-center">
    <div class="page-header">
      <h2>报表汇总打印</h2>
    </div>
    
    <el-alert
      v-if="authError"
      title="认证错误"
      type="warning"
      description="您的登录信息已过期，请重新登录后再试。"
      show-icon
      closable
      @close="authError = false"
      style="margin-bottom: 16px;"
    />
    
    <div class="cards-container">
      <el-row :gutter="20">
        <el-col :span="16">
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
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
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
      </el-row>
    </div>

    <div class="print-section">
      <div class="section-header">
        <h3>赛事证书打印</h3>
        <div class="button-group">
          <el-tooltip
            v-if="!isLoggedIn"
            content="登录后可使用完整的打印功能"
            placement="top"
            effect="light"
          >
            <span class="login-status">
              <el-icon class="status-icon"><Warning /></el-icon>
              未登录状态
            </span>
          </el-tooltip>
          <el-button type="warning" @click="handlePrintPreview">
            <el-icon><Printer /></el-icon>
            打印预览
          </el-button>
        </div>
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
          <el-button type="primary" @click="generateCertificates" :loading="generatingCertificates">
            <el-icon><document /></el-icon>
            生成证书
          </el-button>
          <el-button type="success" @click="batchPrintCertificates" :loading="batchPrinting">
            <el-icon><printer /></el-icon>
            批量打印
          </el-button>
        </el-form-item>
      </el-form>
      
      <el-table
        v-if="certificateForm.eventId"
        v-loading="certificateLoading"
        :data="paginatedCertificateList"
        border
        style="width: 100%"
        :stripe="true"
        :default-sort="{prop: 'generateTime', order: 'descending'}"
        :row-class-name="tableRowClassName"
        @row-click="handleRowClick"
        @sort-change="handleSortChange"
      >
        <template #empty>
          <div class="empty-data">
            <el-empty description="暂无证书数据，请先生成证书" :image-size="100" />
            <el-button v-if="certificateForm.eventId && certificateForm.type" 
                      type="primary" 
                      @click="generateCertificates" 
                      :loading="generatingCertificates" 
                      style="margin-top: 16px;">
              立即生成证书
            </el-button>
          </div>
        </template>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="athleteName" label="运动员姓名" width="140" />
        <el-table-column prop="eventName" label="赛事名称" width="220" show-overflow-tooltip />
        <el-table-column prop="type" label="证书类型" width="120">
          <template #default="scope">
            {{ formatCertificateType(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="rank" label="名次" width="80" sortable />
        <el-table-column prop="generateTime" label="生成时间" width="180" sortable />
        <el-table-column prop="printed" label="打印状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.printed ? 'success' : 'info'">
              {{ scope.row.printed ? '已打印' : '未打印' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click.stop="previewCertificate(scope.row)">预览</el-button>
            <el-button size="small" type="success" @click.stop="printCertificate(scope.row)">打印</el-button>
            <el-button size="small" type="warning" @click.stop="printSimpleCertificate(scope.row)">表彰证书</el-button>
            <el-button size="small" type="info" @click.stop="downloadCertificate(scope.row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="certificateList.length > 0"
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="certificateList.length"
        :page-size="pageSize"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch, onUnmounted, computed } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { Document, Printer, Download, Warning } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import axios from 'axios';
import { statsAPI, eventsAPI } from '../../../api';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../../stores/auth';

// 获取router和authStore
const router = useRouter();
const authStore = useAuthStore();

// API 基础URL
const API_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// 登录状态检查
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token');
});

// 认证错误处理
const authError = ref(false);

const handleAuthError = () => {
  authError.value = true;
  // 可以在此添加其他处理逻辑，如自动跳转到登录页等
};

// 图表引用
const registrationChartRef = ref();
const resultChartRef = ref();
const userActivityChartRef = ref();
let registrationChart: echarts.ECharts | null = null;
let resultChart: echarts.ECharts | null = null;
let userActivityChart: echarts.ECharts | null = null;

// 报表数据
const loading = ref(false);

// 默认数据 - 当API未实现或返回错误时使用
const DEFAULT_REGISTRATION_DATA = {
  categories: ['马拉松', '半程马拉松', '10公里跑', '5公里跑', '趣味跑'],
  values: [
    { value: 320, name: '马拉松' },
    { value: 240, name: '半程马拉松' },
    { value: 180, name: '10公里跑' },
    { value: 150, name: '5公里跑' },
    { value: 100, name: '趣味跑' }
  ]
};

const DEFAULT_RESULTS_DATA = {
  categories: ['马拉松', '半程马拉松', '10公里跑', '5公里跑', '趣味跑'],
  values: [92, 88, 95, 97, 99]
};

const DEFAULT_USER_ACTIVITY_DATA = {
  dates: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
  values: [120, 132, 101, 134, 90, 230, 210]
};

// 证书打印相关
const eventsLoading = ref(false);
const events = ref([]);
const certificateForm = reactive({
  eventId: null as number | null,
  type: ''
});

// 默认赛事列表数据
const DEFAULT_EVENTS = [
  {
    id: 1,
    name: '2023年城市马拉松',
    categoryName: '马拉松',
    startDate: '2023-10-01',
    status: 'COMPLETED'
  },
  {
    id: 2,
    name: '2023年半程马拉松挑战赛',
    categoryName: '半程马拉松',
    startDate: '2023-11-15',
    status: 'COMPLETED'
  },
  {
    id: 3,
    name: '2024年春季马拉松',
    categoryName: '马拉松',
    startDate: '2024-04-10',
    status: 'UPCOMING'
  },
  {
    id: 4,
    name: '2024年夏季10公里跑',
    categoryName: '10公里跑',
    startDate: '2024-07-01',
    status: 'UPCOMING'
  }
];

// 获取所有赛事
const fetchEvents = async () => {
  eventsLoading.value = true;
  try {
    // 调用API
    const data = await eventsAPI.getPublicEvents(0, 100).catch(error => {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        handleAuthError();
        ElMessage.warning('获取赛事列表失败: 认证错误，将使用默认数据');
      } else {
        ElMessage.warning('获取赛事列表失败，将使用默认数据');
      }
      return { content: DEFAULT_EVENTS };
    });
    events.value = data.content || DEFAULT_EVENTS;
  } catch (error) {
    console.error('获取赛事列表失败:', error);
    ElMessage.warning('获取赛事列表失败，将使用默认数据');
    events.value = DEFAULT_EVENTS;
  } finally {
    eventsLoading.value = false;
  }
};

const certificateLoading = ref(false);
const certificateList = ref([]);

// 默认证书数据
const generateDefaultCertificates = (eventId: number, type?: string) => {
  const types = ['PARTICIPATION', 'COMPLETION', 'AWARD'] as const;
  const names = ['张三', '李四', '王五', '赵六', '周七', '吴八'];
  const mockCertificates = [];
  
  // 过滤类型
  const filteredTypes = type ? [type as 'PARTICIPATION' | 'COMPLETION' | 'AWARD'] : types;
  
  // 生成模拟证书
  for (let i = 1; i <= 10; i++) {
    const certificateType = filteredTypes[i % filteredTypes.length];
    mockCertificates.push({
      id: i,
      athleteName: names[i % names.length],
      eventName: `测试赛事 ${eventId}`,
      eventId,
      type: certificateType,
      rank: certificateType === 'AWARD' ? (i % 3) + 1 : 0,
      generateTime: new Date().toISOString(),
      printed: i % 2 === 0
    });
  }
  
  return mockCertificates;
};

// 获取证书列表
const fetchCertificates = async () => {
  if (!certificateForm.eventId) {
    certificateList.value = [];
    return;
  }

  certificateLoading.value = true;
  try {
    const response = await statsAPI.getCertificates(
      certificateForm.eventId, 
      certificateForm.type || undefined
    ).catch(error => {
      if (error && error.isReportError) {
        if (error.message.includes('认证失败')) {
          handleAuthError();
        }
        ElMessage.warning('获取证书列表失败: ' + error.message + '，将使用默认数据');
      } else {
        ElMessage.warning('获取证书列表失败，将使用默认数据');
      }
      return generateDefaultCertificates(certificateForm.eventId!, certificateForm.type);
    });
    
    // 确保response始终是数组类型
    certificateList.value = Array.isArray(response) ? response : 
                            generateDefaultCertificates(certificateForm.eventId, certificateForm.type);
  } catch (error) {
    console.error('获取证书列表失败:', error);
    ElMessage.warning('获取证书列表失败，将使用默认数据');
    certificateList.value = generateDefaultCertificates(certificateForm.eventId, certificateForm.type);
  } finally {
    certificateLoading.value = false;
  }
};

// 监听证书表单变更
watch(() => [certificateForm.eventId, certificateForm.type], () => {
  if (certificateForm.eventId) {
    fetchCertificates();
  }
});

// 初始化图表
const initCharts = async () => {
  loading.value = true;
  
  try {
    // 从API获取图表数据
    const [registrationData, resultData, userActivityData] = await Promise.all([
      statsAPI.getRegistrationData().catch(error => {
        if (error && error.isReportError) {
          if (error.message.includes('认证失败')) {
            handleAuthError();
          }
          ElMessage.warning('获取报名数据失败: ' + error.message);
        }
        return DEFAULT_REGISTRATION_DATA;
      }),
      statsAPI.getResultsData().catch(error => {
        if (error && error.isReportError) {
          if (error.message.includes('认证失败')) {
            handleAuthError();
          }
          ElMessage.warning('获取结果数据失败: ' + error.message);
        }
        return DEFAULT_RESULTS_DATA;
      }),
      statsAPI.getUserActivityData().catch(error => {
        if (error && error.isReportError) {
          if (error.message.includes('认证失败')) {
            handleAuthError();
          }
          ElMessage.warning('获取用户活跃度数据失败: ' + error.message);
        }
        return DEFAULT_USER_ACTIVITY_DATA;
      })
    ]);
    
    nextTick(() => {
      // 初始化报名统计图表
      if (registrationChartRef.value) {
        try {
          // 先销毁可能存在的实例
          if (registrationChart) {
            registrationChart.dispose();
          }
          
          registrationChart = echarts.init(registrationChartRef.value);
          // 检查数据
          const validRegistrationData = {
            categories: Array.isArray(registrationData.categories) ? registrationData.categories : [],
            values: Array.isArray(registrationData.values) ? registrationData.values : []
          };
          
          const registrationOption = {
            title: {
              text: '各赛事报名人数统计',
              left: 'center',
              textStyle: {
                color: '#2c3e50',
                fontWeight: 'bold'
              }
            },
            tooltip: {
              trigger: 'item',
              formatter: '{a} <br/>{b}: {c} ({d}%)',
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              borderColor: '#eee',
              borderWidth: 1,
              textStyle: {
                color: '#333'
              }
            },
            legend: {
              orient: 'vertical',
              left: 'left',
              textStyle: {
                color: '#5e6d82'
              },
              data: validRegistrationData.categories
            },
            series: [
              {
                name: '报名人数',
                type: 'pie',
                radius: '55%',
                center: ['50%', '50%'],
                data: validRegistrationData.values,
                emphasis: {
                  itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                },
                itemStyle: {
                  borderRadius: 8,
                  borderColor: '#fff',
                  borderWidth: 2,
                  color: function(params) {
                    const colorList = [
                      '#4992ff', // 亮蓝色
                      '#3ecc6f', // 绿色
                      '#ffbb2c', // 黄色
                      '#965fe4', // 紫色
                      '#ff6a84', // 粉红色
                      '#2cd9db', // 青色
                      '#ff9f43', // 橙色
                      '#54a0ff', // 天蓝色
                      '#1abc9c', // 绿松石色
                      '#f368e0'  // 亮粉色
                    ];
                    return colorList[params.dataIndex % colorList.length];
                  }
                },
                label: {
                  formatter: '{b}: {c} ({d}%)',
                  fontSize: 14,
                  fontWeight: 'bold'
                }
              }
            ]
          };
          registrationChart.setOption(registrationOption);
        } catch (e) {
          console.error('初始化报名图表失败:', e);
        }
      }

      // 初始化比赛结果图表
      if (resultChartRef.value) {
        try {
          // 先销毁可能存在的实例
          if (resultChart) {
            resultChart.dispose();
          }
          
          resultChart = echarts.init(resultChartRef.value);
          // 检查数据
          const validResultData = {
            categories: Array.isArray(resultData.categories) ? resultData.categories : [],
            values: Array.isArray(resultData.values) ? resultData.values : []
          };
          
          const resultOption = {
            title: {
              text: '赛事完成率',
              left: 'center',
              textStyle: {
                color: '#2c3e50',
                fontWeight: 'bold'
              }
            },
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              },
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              borderColor: '#eee',
              borderWidth: 1,
              textStyle: {
                color: '#333'
              }
            },
            xAxis: {
              type: 'category',
              data: validResultData.categories,
              axisLine: {
                lineStyle: {
                  color: '#5e6d82'
                }
              },
              axisLabel: {
                color: '#5e6d82',
                rotate: 30,
                fontSize: 12
              }
            },
            yAxis: {
              type: 'value',
              axisLabel: {
                formatter: '{value}%',
                color: '#5e6d82'
              },
              splitLine: {
                lineStyle: {
                  color: 'rgba(0,0,0,0.05)'
                }
              }
            },
            series: [
              {
                name: '完成率',
                type: 'bar',
                data: validResultData.values,
                barWidth: '40%',
                itemStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#3ecc6f' },
                    { offset: 0.5, color: '#2cd9db' },
                    { offset: 1, color: '#4992ff' }
                  ]),
                  borderRadius: [4, 4, 0, 0]
                },
                emphasis: {
                  itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                      { offset: 0, color: '#2bb564' },
                      { offset: 0.5, color: '#20b7b9' },
                      { offset: 1, color: '#317fe2' }
                    ])
                  }
                }
              }
            ]
          };
          resultChart.setOption(resultOption);
        } catch (e) {
          console.error('初始化结果图表失败:', e);
        }
      }

      // 初始化用户活跃度图表
      if (userActivityChartRef.value) {
        try {
          // 先销毁可能存在的实例
          if (userActivityChart) {
            userActivityChart.dispose();
          }
          
          userActivityChart = echarts.init(userActivityChartRef.value);
          // 检查数据
          const validUserActivityData = {
            dates: Array.isArray(userActivityData.dates) ? userActivityData.dates : [],
            values: Array.isArray(userActivityData.values) ? userActivityData.values : []
          };
          
          const userActivityOption = {
            title: {
              text: '用户活跃度趋势',
              left: 'center',
              textStyle: {
                color: '#2c3e50',
                fontWeight: 'bold'
              }
            },
            tooltip: {
              trigger: 'axis',
              backgroundColor: 'rgba(255, 255, 255, 0.9)',
              borderColor: '#eee',
              borderWidth: 1,
              textStyle: {
                color: '#333'
              }
            },
            xAxis: {
              type: 'category',
              data: validUserActivityData.dates,
              axisLine: {
                lineStyle: {
                  color: '#5e6d82'
                }
              },
              axisLabel: {
                color: '#5e6d82'
              }
            },
            yAxis: {
              type: 'value',
              axisLabel: {
                color: '#5e6d82'
              },
              splitLine: {
                lineStyle: {
                  color: 'rgba(0,0,0,0.05)'
                }
              }
            },
            series: [
              {
                name: '活跃用户',
                type: 'line',
                data: validUserActivityData.values,
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: {
                  width: 4,
                  color: '#965fe4'
                },
                itemStyle: {
                  color: '#965fe4',
                  borderWidth: 2,
                  borderColor: '#fff'
                },
                areaStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(150, 95, 228, 0.5)' },
                    { offset: 1, color: 'rgba(150, 95, 228, 0.05)' }
                  ])
                },
                emphasis: {
                  itemStyle: {
                    color: '#7f45d4',
                    borderWidth: 3
                  }
                }
              }
            ]
          };
          userActivityChart.setOption(userActivityOption);
        } catch (e) {
          console.error('初始化用户活跃度图表失败:', e);
        }
      }
    });
  } catch (error) {
    console.error('加载图表数据失败:', error);
  } finally {
    loading.value = false;
  }
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

// 获取报名统计报表数据
const getRegistrationReportData = async () => {
  try {
    return await statsAPI.getRegistrationReportData();
  } catch (error) {
    console.error('获取报名统计数据失败:', error);
    ElMessage.error('获取报名统计数据失败，请检查网络连接和登录状态');
    
    // 检查是否是401错误，如果是则尝试重新登录
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthError();
    }
    
    return [];
  }
};

// 获取成绩统计报表数据
const getResultReportData = async () => {
  try {
    return await statsAPI.getResultReportData();
  } catch (error) {
    console.error('获取成绩统计数据失败:', error);
    ElMessage.error('获取成绩统计数据失败，请检查网络连接和登录状态');
    
    // 检查是否是401错误，如果是则尝试重新登录
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthError();
    }
    
    return [];
  }
};

// 获取用户活跃报表数据
const getUserActivityReportData = async () => {
  try {
    return await statsAPI.getUserActivityReportData();
  } catch (error) {
    console.error('获取用户活跃数据失败:', error);
    ElMessage.error('获取用户活跃数据失败，请检查网络连接和登录状态');
    
    // 检查是否是401错误，如果是则尝试重新登录
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthError();
    }
    
    return [];
  }
};

// 导出报表
const handleExportRegistrationReport = async () => {
  try {
    const loadingInstance = ElLoading.service({
      text: '导出报表中...',
      background: 'rgba(255, 255, 255, 0.7)'
    });
    
    // 获取报名统计数据
    const data = await getRegistrationReportData();
    if (data.length === 0) {
      ElMessage.warning('没有数据可导出');
      loadingInstance.close();
      return;
    }
    
    // 导出为CSV
    exportToCSV('报名统计报表.csv', data);
    
    loadingInstance.close();
    ElMessage.success('报名统计报表导出成功');
  } catch (error) {
    console.error('导出报表失败:', error);
    ElMessage.error('导出报表失败');
  }
};

const handleExportResultReport = async () => {
  try {
    const loadingInstance = ElLoading.service({
      text: '导出报表中...',
      background: 'rgba(255, 255, 255, 0.7)'
    });
    
    // 获取成绩统计数据
    const data = await getResultReportData();
    if (data.length === 0) {
      ElMessage.warning('没有数据可导出');
      loadingInstance.close();
      return;
    }
    
    // 导出为CSV
    exportToCSV('成绩统计报表.csv', data);
    
    loadingInstance.close();
    ElMessage.success('成绩统计报表导出成功');
  } catch (error) {
    console.error('导出报表失败:', error);
    ElMessage.error('导出报表失败');
  }
};

const handleExportUserActivityReport = async () => {
  try {
    const loadingInstance = ElLoading.service({
      text: '导出报表中...',
      background: 'rgba(255, 255, 255, 0.7)'
    });
    
    // 获取用户活跃数据
    const data = await getUserActivityReportData();
    if (data.length === 0) {
      ElMessage.warning('没有数据可导出');
      loadingInstance.close();
      return;
    }
    
    // 导出为CSV
    exportToCSV('用户活跃报表.csv', data);
    
    loadingInstance.close();
    ElMessage.success('用户活跃报表导出成功');
  } catch (error) {
    console.error('导出报表失败:', error);
    ElMessage.error('导出报表失败');
  }
};

// 打印预览功能
const generatingCertificates = ref(false);
const batchPrinting = ref(false);
const isPrinting = ref(false);

const handlePrintPreview = () => {
  // 标记正在打印
  isPrinting.value = true;
  ElMessage.info('正在准备打印...');
  
  // 在打印之前，先隐藏不需要打印的元素
  const printStyle = document.createElement('style');
  printStyle.id = 'print-preview-style';
  printStyle.innerHTML = `
    @media print {
      html, body {
        background-color: white !important;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
      
      .page-header, .card-actions, .filter-controls, .certificate-form, .section-header, 
      .pagination, .el-table__column-filter-trigger, .el-button, .el-tooltip__trigger {
        display: none !important;
      }
      
      .report-center {
        background-color: white !important;
        padding: 0 !important;
        width: 100% !important;
      }
      
      .report-card, .report-table-container, .print-section {
        box-shadow: none !important;
        padding: 0 !important;
        margin: 0 0 20px 0 !important;
        page-break-inside: avoid !important;
        width: 100% !important;
        border: none !important;
      }
      
      .chart-container {
        page-break-inside: avoid !important;
        break-inside: avoid !important;
        -webkit-column-break-inside: avoid !important;
        margin: 0 auto !important;
        max-width: 90% !important;
      }
      
      .el-table {
        border: 1px solid #dcdfe6 !important;
        width: 100% !important;
      }
      
      .el-table th {
        background-color: #f2f6fc !important;
        color: #000 !important;
        font-weight: bold !important;
      }
      
      .el-table td {
        border-bottom: 1px solid #ebeef5 !important;
      }
      
      .el-card__header {
        font-size: 18px !important;
        font-weight: bold !important;
        text-align: center !important;
        padding: 10px !important;
        background-color: #f8f9fc !important;
        color: #333 !important;
      }
      
      .el-card {
        border: none !important;
      }
      
      .cards-container .el-row {
        display: block !important;
      }
      
      .cards-container .el-col {
        width: 100% !important;
        max-width: 100% !important;
        display: block !important;
        float: none !important;
        margin-bottom: 20px !important;
      }
      
      /* 添加页面样式 */
      @page {
        size: A4 portrait;
        margin: 1cm;
      }
      
      /* 表格样式调整，使其看起来更清晰 */
      .el-table--striped .el-table__body tr.el-table__row--striped td {
        background-color: #f9f9f9 !important;
      }
      
      /* 确保图表可以正确显示 */
      .echarts-container {
        height: 300px !important;
        width: 80% !important;
        margin: 0 auto !important;
      }
      
      /* 表头样式 */
      .print-header {
        text-align: center;
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 2px solid #333;
        display: block !important;
      }
      
      /* 证书表格特殊样式 */
      .print-section .el-table {
        max-width: 90% !important;
        margin: 0 auto !important;
      }
      
      /* 打印页脚 */
      .print-footer {
        text-align: center;
        font-size: 12px;
        margin-top: 20px;
        padding-top: 10px;
        border-top: 1px solid #eee;
        color: #999;
        display: block !important;
      }
    }
  `;
  document.head.appendChild(printStyle);
  
  // 添加打印页眉和页脚
  const printHeader = document.createElement('div');
  printHeader.className = 'print-header';
  printHeader.style.display = 'none';
  printHeader.innerHTML = `体育赛事管理系统统计报表 - ${new Date().toLocaleDateString()}`;
  
  const printFooter = document.createElement('div');
  printFooter.className = 'print-footer';
  printFooter.style.display = 'none';
  printFooter.innerHTML = `打印时间: ${new Date().toLocaleString()} | 体育赛事管理系统`;
  
  // 将页眉和页脚添加到报表容器
  const reportCenter = document.querySelector('.report-center');
  reportCenter?.insertBefore(printHeader, reportCenter.firstChild);
  reportCenter?.appendChild(printFooter);
  
  // 触发重新渲染
  nextTick(() => {
    // 给图表一些调整布局的时间
    setTimeout(() => {
      // 确保图表在打印之前重新渲染
      if (registrationChart) registrationChart.resize();
      if (resultChart) resultChart.resize();
      if (userActivityChart) userActivityChart.resize();
      
      // 调用打印功能
      setTimeout(() => {
        window.print();
        
        // 打印完成后移除打印样式和额外添加的元素
        document.head.removeChild(printStyle);
        if (printHeader.parentNode) printHeader.parentNode.removeChild(printHeader);
        if (printFooter.parentNode) printFooter.parentNode.removeChild(printFooter);
        
        // 清除打印状态
        isPrinting.value = false;
        ElMessage.success('打印预览完成');
      }, 500);
    }, 300);
  });
};

// 监听打印媒体查询，以便在用户取消打印时清理资源
onMounted(() => {
  const mediaQueryList = window.matchMedia('print');
  const handlePrintChange = (mql) => {
    if (!mql.matches && isPrinting.value) {
      // 打印被取消或完成
      const printStyle = document.getElementById('print-preview-style');
      if (printStyle) document.head.removeChild(printStyle);
      
      const printHeader = document.querySelector('.print-header');
      if (printHeader && printHeader.parentNode) printHeader.parentNode.removeChild(printHeader);
      
      const printFooter = document.querySelector('.print-footer');
      if (printFooter && printFooter.parentNode) printFooter.parentNode.removeChild(printFooter);
      
      isPrinting.value = false;
    }
  };
  
  // 添加打印媒体查询监听器
  if (mediaQueryList.addEventListener) {
    mediaQueryList.addEventListener('change', handlePrintChange);
  } else {
    // 兼容旧版浏览器
    mediaQueryList.addListener(handlePrintChange);
  }
  
  // 在组件卸载时移除监听器
  onUnmounted(() => {
    if (mediaQueryList.removeEventListener) {
      mediaQueryList.removeEventListener('change', handlePrintChange);
    } else {
      // 兼容旧版浏览器
      mediaQueryList.removeListener(handlePrintChange);
    }
  });
});

// 更新生成证书和批量打印函数，以添加加载状态
const generateCertificates = async () => {
  if (!certificateForm.eventId || !certificateForm.type) {
    ElMessage.warning('请先选择赛事和证书类型');
    return;
  }
  
  certificateLoading.value = true;
  generatingCertificates.value = true;
  try {
    // 调用API生成证书
    await statsAPI.generateCertificates(certificateForm.eventId, certificateForm.type);
    ElMessage.success('证书生成成功');
    
    // 短暂延迟后再获取证书列表，确保后端处理完成
    setTimeout(() => {
      fetchCertificates();
    }, 500);
  } catch (error) {
    console.error('生成证书失败:', error);
    ElMessage.error('生成证书失败，请检查网络连接和登录状态');
    
    // 检查是否是401错误，如果是则尝试重新登录
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthError();
    }
  } finally {
    certificateLoading.value = false;
    generatingCertificates.value = false;
  }
};

const batchPrintCertificates = async () => {
  if (!certificateForm.eventId) {
    ElMessage.warning('请先选择赛事');
    return;
  }
  
  batchPrinting.value = true;
  try {
    // 调用API批量打印证书
    await statsAPI.batchPrintCertificates(
      certificateForm.eventId, 
      certificateForm.type || undefined
    );
    ElMessage.success('已发送批量打印任务到打印队列');
  } catch (error) {
    console.error('批量打印失败:', error);
    ElMessage.error('批量打印失败');
    
    // 检查是否是401错误，如果是则尝试重新登录
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthError();
    }
  } finally {
    batchPrinting.value = false;
  }
};

const previewCertificate = async (certificate: any) => {
  if (!certificate || !certificate.id) {
    ElMessage.warning('无效的证书数据');
    return;
  }

  const loadingInstance = ElLoading.service({
    text: '正在加载证书预览...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    const blob = await generateCertificatePDF(certificate);
    const url = URL.createObjectURL(blob);
    
    // 使用iframe预览，而不是打开新窗口
    // 这样可以避免被浏览器的弹窗拦截
    const previewWindow = window.open('', '_blank');
    if (previewWindow) {
      previewWindow.document.write(`
        <html>
          <head>
            <title>${certificate.athleteName}的${formatCertificateType(certificate.type)}</title>
            <style>
              body, html {
                margin: 0;
                padding: 0;
                height: 100%;
                overflow: hidden;
              }
              iframe {
                width: 100%;
                height: 100%;
                border: none;
              }
            </style>
          </head>
          <body>
            <iframe src="${url}" width="100%" height="100%"></iframe>
          </body>
        </html>
      `);
      previewWindow.document.close();
    } else {
      // 如果被浏览器拦截，使用普通的URL链接
      window.open(url, '_blank');
    }
    
    ElMessage.success(`正在预览${certificate.athleteName}的${formatCertificateType(certificate.type)}`);
  } catch (error) {
    console.error('预览证书失败:', error);
    // 错误信息已经在generateCertificatePDF函数中处理
  } finally {
    loadingInstance.close();
  }
};

const printCertificate = async (certificate: any) => {
  if (!certificate || !certificate.id) {
    ElMessage.warning('无效的证书数据');
    return;
  }

  const loadingInstance = ElLoading.service({
    text: '正在处理打印请求...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    // 先获取证书预览
    const blob = await generateCertificatePDF(certificate);
    const url = URL.createObjectURL(blob);
    
    // 创建一个隐藏的iframe用于打印
    const printFrame = document.createElement('iframe');
    printFrame.style.display = 'none';
    printFrame.src = url;
    document.body.appendChild(printFrame);
    
    // 等待iframe加载完成后打印
    printFrame.onload = async () => {
      try {
        // 等待一下确保PDF渲染完成
        setTimeout(async () => {
          try {
            // 执行打印
            printFrame.contentWindow?.print();
            
            // 调用API更新打印状态
            await statsAPI.printCertificate(certificate.id);
            ElMessage.success(`正在打印${certificate.athleteName}的${formatCertificateType(certificate.type)}`);
            
            // 刷新证书列表
            setTimeout(() => {
              fetchCertificates();
            }, 500);
          } catch (innerError) {
            console.error('打印过程中发生错误:', innerError);
            ElMessage.error('打印过程中发生错误');
          } finally {
            // 清理iframe
            document.body.removeChild(printFrame);
            loadingInstance.close();
          }
        }, 1000);
      } catch (frameError) {
        console.error('打印框架错误:', frameError);
        document.body.removeChild(printFrame);
        loadingInstance.close();
        ElMessage.error('打印准备过程中发生错误');
      }
    };
    
    // 处理iframe加载失败
    printFrame.onerror = () => {
      document.body.removeChild(printFrame);
      loadingInstance.close();
      ElMessage.error('无法加载打印内容');
    };
  } catch (error) {
    console.error('打印证书失败:', error);
    loadingInstance.close();
    
    // 检查是否为reportClient特殊错误
    if (error && typeof error === 'object' && 'isReportError' in error) {
      ElMessage.error(`打印失败: ${(error as any).message || '未知错误'}`);
    }
    
    // 检查是否是401错误，如果是则尝试重新登录
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthError();
    }
  }
};

// 添加新方法：打印简单表彰证书
const printSimpleCertificate = async (certificate: any) => {
  if (!certificate || !certificate.id) {
    ElMessage.warning('无效的证书数据');
    return;
  }
  
  // 标记正在打印
  isPrinting.value = true;
  ElMessage.info('正在准备打印表彰证书...');
  
  // 创建一个新的打印窗口
  const printWindow = window.open('', '_blank');
  if (!printWindow) {
    ElMessage.error('打印窗口被浏览器阻止，请允许弹出窗口');
    isPrinting.value = false;
    return;
  }
  
  // 获取证书类型和排名信息
  const certificateType = formatCertificateType(certificate.type);
  const rankText = certificate.type === 'AWARD' ? `第${certificate.rank}名` : '';
  
  // 在新窗口中写入内容
  printWindow.document.open();
  printWindow.document.write('<!DOCTYPE html><html><head><meta charset="utf-8">');
  printWindow.document.write(`<title>${certificate.athleteName}的${certificateType}</title>`);
  printWindow.document.write(`<style>
    @media print {
      @page {
        size: A4 landscape;
        margin: 0;
      }
      body {
        margin: 0.5cm;
      }
    }
    
    body {
      font-family: "Microsoft YaHei", SimSun, sans-serif;
      text-align: center;
      background-color: #fff;
      padding: 2cm;
      height: 100vh;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      justify-content: center;
      color: #333;
    }
    
    .certificate-container {
      border: 2px solid #94c2f8;
      padding: 20px;
      width: 100%;
      max-width: 800px;
      margin: 0 auto;
      background: #fff;
      position: relative;
      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
      background-image: linear-gradient(rgba(148, 194, 248, 0.05) 1px, transparent 1px), 
                      linear-gradient(90deg, rgba(148, 194, 248, 0.05) 1px, transparent 1px);
      background-size: 20px 20px;
    }
    
    .certificate-border {
      border: 1px solid #4992ff;
      padding: 50px;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    
    .certificate-header {
      font-size: 40px;
      font-weight: bold;
      color: #4992ff;
      margin-bottom: 20px;
      letter-spacing: 8px;
    }
    
    .certificate-subheader {
      font-size: 24px;
      color: #4992ff;
      margin-bottom: 40px;
    }
    
    .certificate-content {
      font-size: 20px;
      line-height: 1.8;
      margin-bottom: 40px;
    }
    
    .certificate-name {
      font-size: 30px;
      font-weight: bold;
      margin: 10px 0;
      color: #333;
    }
    
    .certificate-rank {
      font-size: 24px;
      font-weight: bold;
      color: #e9546b;
      margin: 10px 0;
    }
    
    .certificate-event {
      font-size: 20px;
      margin: 10px 0;
    }
    
    .certificate-date {
      font-size: 16px;
      margin-top: 40px;
    }
    
    .certificate-seal {
      position: absolute;
      right: 80px;
      bottom: 80px;
      width: 100px;
      height: 100px;
      border: 2px solid #e9546b;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #e9546b;
      font-size: 16px;
      transform: rotate(-15deg);
      opacity: 0.8;
    }
    
    .certificate-footer {
      display: flex;
      justify-content: space-between;
      margin-top: 60px;
    }
    
    .certificate-organizer, .certificate-date-issued {
      font-size: 16px;
    }
  </style>`);
  printWindow.document.write('</head><body>');
  
  // 证书内容
  printWindow.document.write('<div class="certificate-container">');
  printWindow.document.write('<div class="certificate-border">');
  
  // 标题部分
  printWindow.document.write('<div>');
  printWindow.document.write('<div class="certificate-header">表彰证书</div>');
  printWindow.document.write(`<div class="certificate-subheader">${certificateType}</div>`);
  printWindow.document.write('</div>');
  
  // 内容部分
  printWindow.document.write('<div class="certificate-content">');
  printWindow.document.write('兹证明');
  printWindow.document.write(`<div class="certificate-name">${certificate.athleteName}</div>`);
  
  // 如果是获奖证书，显示排名
  if (certificate.type === 'AWARD') {
    printWindow.document.write(`<div class="certificate-rank">荣获 ${rankText}</div>`);
  }
  
  printWindow.document.write(`<div class="certificate-event">在 ${certificate.eventName} 中</div>`);
  
  // 根据证书类型显示不同的成就描述
  let achievementText = '';
  if (certificate.type === 'PARTICIPATION') {
    achievementText = '全程参与并表现出色';
  } else if (certificate.type === 'COMPLETION') {
    achievementText = '成功完成全程比赛';
  } else {
    achievementText = '表现优异，取得卓越成绩';
  }
  
  printWindow.document.write(`<div class="certificate-achievement">${achievementText}</div>`);
  printWindow.document.write('</div>');
  
  // 页脚部分
  printWindow.document.write('<div class="certificate-footer">');
  printWindow.document.write('<div class="certificate-organizer">体育赛事管理系统组委会</div>');
  printWindow.document.write(`<div class="certificate-date-issued">颁发日期: ${new Date().toLocaleDateString()}</div>`);
  printWindow.document.write('</div>');
  
  // 结束证书容器
  printWindow.document.write('</div>');
  
  // 添加印章
  printWindow.document.write('<div class="certificate-seal">体育赛事<br>管理系统</div>');
  printWindow.document.write('</div>');
  
  // 添加自动打印和关闭脚本 - 使用分段添加，避免脚本结束标签被错误解析
  printWindow.document.write('<script>');
  printWindow.document.write('window.onload = function() {');
  printWindow.document.write('  setTimeout(function() {');
  printWindow.document.write('    window.print();');
  printWindow.document.write('    setTimeout(function() {');
  printWindow.document.write('      window.close();');
  printWindow.document.write('    }, 500);');
  printWindow.document.write('  }, 300);');
  printWindow.document.write('};');
  printWindow.document.write('</' + 'script>');
  
  printWindow.document.write('</body></html>');
  printWindow.document.close();
  
  // 更新证书打印状态
  try {
    await statsAPI.printCertificate(certificate.id);
    // 刷新证书列表
    setTimeout(() => {
      fetchCertificates();
    }, 1000);
  } catch (error) {
    console.error('更新证书打印状态失败:', error);
  }
  
  isPrinting.value = false;
};

const downloadCertificate = async (certificate: any) => {
  if (!certificate || !certificate.id) {
    ElMessage.warning('无效的证书数据');
    return;
  }

  const loadingInstance = ElLoading.service({
    text: '正在准备下载...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  
  try {
    const blob = await generateCertificatePDF(certificate);
    
    // 创建下载链接
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${certificate.athleteName}-${formatCertificateType(certificate.type)}.pdf`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success(`${certificate.athleteName}的${formatCertificateType(certificate.type)}下载成功`);
  } catch (error) {
    console.error('下载证书失败:', error);
    // 错误信息已经在generateCertificatePDF函数中处理
  } finally {
    loadingInstance.close();
  }
};

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  try {
    registrationChart?.resize();
    resultChart?.resize();
    userActivityChart?.resize();
  } catch (e) {
    console.error('调整图表大小失败:', e);
  }
};

// 组件挂载时加载数据
onMounted(async () => {
  try {
    // 检查登录状态
    if (!isLoggedIn.value) {
      ElMessage.warning({
        message: '您尚未登录，某些功能可能无法正常使用。将显示模拟数据。',
        duration: 5000,
        showClose: true
      });
    }
    
    // 初始化数据
    await Promise.all([
      initCharts(),
      fetchEvents()
    ]);
  } catch (error) {
    console.error('初始化数据失败:', error);
    ElMessage.error('初始化数据失败，请刷新页面重试');
  }
  
  // 添加窗口resize事件监听
  window.addEventListener('resize', handleResize);
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  // 销毁图表实例
  registrationChart?.dispose();
  resultChart?.dispose();
  userActivityChart?.dispose();
});

// 分页相关
const pageSize = ref(10);
const currentPage = ref(1);

// 排序相关
const sortBy = ref('generateTime');
const sortOrder = ref('descending');

const handleSortChange = (column) => {
  sortBy.value = column.prop;
  sortOrder.value = column.order;
};

const sortedCertificateList = computed(() => {
  if (!certificateList.value || certificateList.value.length === 0) {
    return [];
  }
  
  const list = [...certificateList.value];
  if (sortBy.value) {
    list.sort((a, b) => {
      if (!a || !b) return 0;
      
      let valueA = a[sortBy.value];
      let valueB = b[sortBy.value];
      
      // 处理undefined或null值
      if (valueA === undefined || valueA === null) valueA = '';
      if (valueB === undefined || valueB === null) valueB = '';
      
      // 特殊处理日期类型
      if (sortBy.value === 'generateTime') {
        try {
          valueA = new Date(valueA).getTime();
          valueB = new Date(valueB).getTime();
          
          // 处理无效日期
          if (isNaN(valueA)) valueA = 0;
          if (isNaN(valueB)) valueB = 0;
        } catch (e) {
          console.error('日期解析错误:', e);
          return 0;
        }
      }
      
      if (sortOrder.value === 'ascending') {
        return valueA > valueB ? 1 : -1;
      } else {
        return valueA < valueB ? 1 : -1;
      }
    });
  }
  return list;
});

const paginatedCertificateList = computed(() => {
  // 确保sortedCertificateList.value始终是数组类型
  const list = Array.isArray(sortedCertificateList.value) ? sortedCertificateList.value : [];
  
  if (list.length === 0) {
    return [];
  }
  
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return list.slice(startIndex, endIndex);
});

const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
};

const tableRowClassName = ({row}) => {
  return row.printed ? 'success-row' : '';
};

const handleRowClick = (row) => {
  // 行点击事件处理
  console.log('Clicked row:', row);
};

// CSV导出工具函数
const exportToCSV = (filename: string, rows: any[]) => {
  if (!rows || !rows.length) {
    ElMessage.warning('没有数据可导出');
    return;
  }

  // 获取表头（对象的所有键）
  const headers = Object.keys(rows[0]);
  
  // 创建CSV内容
  const csvContent = [
    // 添加表头行
    headers.join(','),
    // 添加数据行
    ...rows.map(row => 
      headers.map(header => {
        // 处理包含逗号、引号或换行符的字段
        const cell = row[header] !== undefined && row[header] !== null 
          ? String(row[header]) 
          : '';
        if (cell.includes(',') || cell.includes('"') || cell.includes('\n')) {
          return `"${cell.replace(/"/g, '""')}"`;
        }
        return cell;
      }).join(',')
    )
  ].join('\n');
  
  // 创建Blob对象并下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// 生成证书PDF的函数
const generateCertificatePDF = async (certificate: any): Promise<Blob> => {
  try {
    const blob = await statsAPI.getCertificatePreview(certificate.id);
    return blob;
  } catch (error) {
    console.error('获取证书预览失败:', error);
    
    // 检查是否为reportClient特殊错误
    if (error && typeof error === 'object' && 'isReportError' in error) {
      ElMessage.error(`证书预览失败: ${(error as any).message || '未知错误'}`);
    } else {
      ElMessage.error('获取证书预览失败，请检查网络连接');
    }
    
    // 检查是否是401错误，如果是则尝试重新登录
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthError();
    }
    
    throw error;
  }
};
</script>

<style scoped>
.report-center {
  width: 100%;
  background-color: #f5f7fa;
  padding: 20px;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.cards-container {
  margin-bottom: 30px;
}

.report-card {
  height: 100%;
  margin-bottom: 20px;
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.report-card:hover {
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
}

.card-content {
  display: flex;
  flex-direction: column;
}

.chart-container {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
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
  font-weight: 600;
  color: #2c3e50;
}

.report-table-container {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.gender-ratio {
  display: flex;
  width: 100%;
  height: 20px;
  border-radius: 20px;
  overflow: hidden;
}

.male-ratio {
  background-color: #4992ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
}

.female-ratio {
  background-color: #ff6a84;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
}

.print-section {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.certificate-form {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fc;
  border-radius: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-button) {
  border-radius: 4px;
}

:deep(.el-tag) {
  border-radius: 4px;
}

:deep(.el-card__header) {
  background-color: #f8f9fc;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-progress-bar__outer) {
  border-radius: 8px;
  background-color: #ebeef5;
}

:deep(.el-progress-bar__inner) {
  border-radius: 8px;
  background-color: #3ecc6f;
}

:deep(.el-rate__icon) {
  font-size: 18px;
  margin-right: 4px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f8f9fc;
  font-weight: bold;
}

:deep(.el-table .success-row) {
  background-color: rgba(62, 204, 111, 0.1);
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 4px;
}

/* 打印样式优化 */
@media print {
  .report-center {
    background-color: white;
    padding: 0;
  }
  
  .page-header, .card-actions, .filter-controls, .certificate-form {
    display: none !important;
  }
  
  .report-card, .report-table-container, .print-section {
    box-shadow: none;
    padding: 0;
    margin: 0 0 20px 0;
    page-break-inside: avoid;
  }
  
  .chart-container {
    page-break-inside: avoid;
  }
  
  :deep(.el-table) {
    border: 1px solid #dcdfe6;
  }
  
  :deep(.el-table th) {
    background-color: #f2f6fc;
  }
  
  .pagination {
    display: none !important;
  }
}

.empty-data {
  padding: 32px 0;
  text-align: center;
}

.button-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.login-status {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #e6a23c;
  background-color: #fdf6ec;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-icon {
  margin-right: 4px;
}
</style> 