import apiClient from './axios';
import type { PaginatedResponse } from './eventsAPI';

export interface EventResult {
  id: number;
  event: {
    id: number;
    name: string;
  };
  athlete: {
    id: number;
    username: string;
  };
  rank: number;
  score: string;
  remarks: string;
  recordedBy: {
    id: number;
    username: string;
  };
  createdAt: string;
  updatedAt: string;
}

export const resultAPI = {
  /**
   * 获取赛事公开成绩
   */
  getPublicEventResults: async (eventId: number) => {
    const response = await apiClient.get<EventResult[]>(`/results/public/event/${eventId}`);
    return response.data;
  },

  /**
   * 获取运动员公开成绩
   */
  getPublicAthleteResults: async (athleteId: number) => {
    const response = await apiClient.get<EventResult[]>(`/results/public/athlete/${athleteId}`);
    return response.data;
  },

  /**
   * 获取所有赛事结果
   */
  getAllResults: async (page = 0, size = 10) => {
    const response = await apiClient.get<PaginatedResponse<EventResult>>('/results', {
      params: { page, size }
    });
    return response.data;
  },

  /**
   * 管理员: 获取赛事成绩（分页）
   */
  getEventResults: async (eventId: number, page = 0, size = 10) => {
    const response = await apiClient.get<PaginatedResponse<EventResult>>(`/results/event/${eventId}`, {
      params: { page, size }
    });
    return response.data;
  },

  /**
   * 管理员: 获取运动员成绩
   */
  getAthleteResults: async (athleteId: number) => {
    const response = await apiClient.get<EventResult[]>(`/results/athlete/${athleteId}`);
    return response.data;
  },

  /**
   * 管理员: 记录成绩
   */
  recordResult: async (result: Omit<EventResult, 'id' | 'recordedBy' | 'createdAt' | 'updatedAt'>) => {
    try {
      const response = await apiClient.post<EventResult>('/results', result);
      return response.data;
    } catch (error: any) {
      console.error('记录成绩失败:', error);
      // 重新抛出错误，让调用方处理
      throw error;
    }
  },

  /**
   * 管理员: 更新成绩
   */
  updateResult: async (id: number, result: Partial<EventResult>) => {
    try {
      const response = await apiClient.put<EventResult>(`/results/${id}`, result);
      return response.data;
    } catch (error: any) {
      console.error('更新成绩失败:', error);
      throw error;
    }
  },

  /**
   * 管理员: 删除成绩
   */
  deleteResult: async (id: number) => {
    try {
      const response = await apiClient.delete(`/results/${id}`);
      return response.data;
    } catch (error: any) {
      console.error('删除成绩失败:', error);
      throw error;
    }
  }
};

export default resultAPI; 