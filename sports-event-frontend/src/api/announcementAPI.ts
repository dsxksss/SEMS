import apiClient from './axios';
import type { PaginatedResponse } from './eventsAPI';

export interface Announcement {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  published: boolean;
  eventId?: number;
  type?: string;
  attachments?: string[];
}

export const announcementAPI = {
  /**
   * 获取公告列表 (带分页)
   */
  getAnnouncements: async (params: {
    page?: number;
    size?: number;
    keyword?: string;
    startDate?: string;
    endDate?: string;
  }): Promise<PaginatedResponse<Announcement>> => {
    const { page = 0, size = 10, keyword, startDate, endDate } = params;
    let url = `/announcements/public?page=${page}&size=${size}`;
    
    if (keyword) {
      url += `&keyword=${encodeURIComponent(keyword)}`;
    }
    
    if (startDate) {
      url += `&startDate=${startDate}`;
    }
    
    if (endDate) {
      url += `&endDate=${endDate}`;
    }
    
    const response = await apiClient.get(url);
    return response.data;
  },

  /**
   * 获取最新公告
   */
  getLatestAnnouncements: async () => {
    const response = await apiClient.get<Announcement[]>('/announcements/public/latest');
    return response.data;
  },

  /**
   * 获取公告详情
   */
  getAnnouncementById: async (id: number): Promise<Announcement> => {
    const response = await apiClient.get(`/announcements/${id}`);
    return response.data;
  },

  /**
   * 获取与特定赛事相关的公告
   */
  getAnnouncementsByEvent: async (eventId: number): Promise<Announcement[]> => {
    const response = await apiClient.get(`/announcements/public/event/${eventId}`);
    return response.data;
  },

  /**
   * 获取所有公告 (管理员)
   */
  getAllAnnouncements: async () => {
    const response = await apiClient.get<Announcement[]>('/announcements');
    return response.data;
  },

  /**
   * 创建公告
   */
  createAnnouncement: async (announcement: Partial<Announcement>) => {
    const response = await apiClient.post<Announcement>('/announcements', announcement);
    return response.data;
  },

  /**
   * 更新公告
   */
  updateAnnouncement: async (id: number, announcement: Partial<Announcement>) => {
    const response = await apiClient.put<Announcement>(`/announcements/${id}`, announcement);
    return response.data;
  },

  /**
   * 切换公告发布状态
   */
  toggleAnnouncementPublished: async (id: number, isPublished: boolean) => {
    const response = await apiClient.put<Announcement>(`/announcements/${id}/publish`, { published: isPublished });
    return response.data;
  },

  /**
   * 删除公告
   */
  deleteAnnouncement: async (id: number) => {
    const response = await apiClient.delete(`/announcements/${id}`);
    return response.data;
  }
};

export default announcementAPI; 