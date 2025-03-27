import apiClient from './axios';
import type { PaginatedResponse } from './eventsAPI';

export interface Attachment {
  name: string;
  url: string;
  size?: number;
}

export interface Announcement {
  id: number;
  title: string;
  content: string;
  type?: string;
  event?: {
    id: number;
    name: string;
  };
  author?: {
    id: number;
    username: string;
  };
  createdBy?: {
    id: number;
    username: string;
  };
  isPublished: boolean;
  createdAt: string;
  updatedAt: string;
  viewCount?: number;
  attachments?: Attachment[];
}

export const announcementAPI = {
  /**
   * 获取公开公告列表（分页）
   */
  getPublicAnnouncements: async (page = 0, size = 10) => {
    const response = await apiClient.get<PaginatedResponse<Announcement>>('/announcements/public', {
      params: { page, size }
    });
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
   * 获取赛事相关公告
   */
  getAnnouncementsByEvent: async (eventId: number) => {
    const response = await apiClient.get<Announcement[]>(`/announcements/public/event/${eventId}`);
    return response.data;
  },

  /**
   * 获取公告详情
   */
  getAnnouncementById: async (id: number) => {
    const response = await apiClient.get<Announcement>(`/announcements/${id}`);
    return response.data;
  },

  /**
   * 管理员: 获取所有公告
   */
  getAllAnnouncements: async () => {
    const response = await apiClient.get<Announcement[]>('/announcements');
    return response.data;
  },

  /**
   * 管理员: 创建公告
   */
  createAnnouncement: async (announcement: Partial<Announcement>) => {
    const response = await apiClient.post<Announcement>('/announcements', announcement);
    return response.data;
  },

  /**
   * 管理员: 更新公告
   */
  updateAnnouncement: async (id: number, announcement: Partial<Announcement>) => {
    const response = await apiClient.put<Announcement>(`/announcements/${id}`, announcement);
    return response.data;
  },

  /**
   * 管理员: 发布/撤回公告
   */
  toggleAnnouncementPublished: async (id: number, isPublished: boolean) => {
    const response = await apiClient.put<Announcement>(`/announcements/${id}/publish`, { published: isPublished });
    return response.data;
  },

  /**
   * 管理员: 删除公告
   */
  deleteAnnouncement: async (id: number) => {
    const response = await apiClient.delete(`/announcements/${id}`);
    return response.data;
  }
};

export default announcementAPI; 