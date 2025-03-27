import apiClient from './axios';

// 统计数据接口定义
export interface DashboardStats {
  userCount: number;
  eventCount: number;
  registrationCount: number;
  completedEventCount: number;
  recentEvents: Array<{
    id: number;
    name: string;
    categoryName: string;
    startDate: string;
    status: 'ONGOING' | 'UPCOMING' | 'COMPLETED' | 'CANCELLED';
  }>;
  recentRegistrations: Array<{
    id: number;
    username: string;
    eventName: string;
    registrationDate: string;
    status: 'APPROVED' | 'PENDING' | 'REJECTED';
  }>;
  recentAnnouncements: Array<{
    id: number;
    title: string;
    content: string;
    createdDate: string;
    authorName: string;
  }>;
}

export const statsAPI = {
  /**
   * 获取Dashboard统计数据
   */
  getDashboardStats: async () => {
    const response = await apiClient.get<DashboardStats>('/stats/dashboard');
    return response.data;
  }
};

export default statsAPI; 