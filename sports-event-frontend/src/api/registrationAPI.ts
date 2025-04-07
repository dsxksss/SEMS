import apiClient from './axios';
import type { PaginatedResponse } from './eventsAPI';

export interface Registration {
  id: number;
  event: {
    id: number;
    name: string;
  };
  user: {
    id: number;
    username: string;
    email?: string;
    avatar?: string;
  };
  registrationTime: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  notes: string;
  createdAt: string;
  updatedAt: string;
}

// 为管理赛事参与者添加接口
export interface Participant {
  id: number;
  user: {
    id: number;
    username: string;
    email: string;
    avatar?: string;
  };
  event: {
    id: number;
    name: string;
  };
  registrationTime: string;
  checkInTime?: string;
  status: 'REGISTERED' | 'CHECKED_IN' | 'CANCELLED';
  notes?: string;
}

export interface ParticipantQueryParams {
  page?: number;
  size?: number;
  status?: string;
  search?: string;
}

export const registrationAPI = {
  /**
   * 用户: 获取自己的报名列表
   */
  getMyRegistrations: async () => {
    const response = await apiClient.get<Registration[]>('/registrations/my');
    return response.data;
  },

  /**
   * 用户: 报名参加活动
   */
  registerForEvent: async (eventId: number, notes: string = '') => {
    const response = await apiClient.post<Registration>('/registrations', { eventId, notes });
    return response.data;
  },

  /**
   * 用户: 取消报名
   */
  cancelRegistration: async (registrationId: number) => {
    const response = await apiClient.delete(`/registrations/${registrationId}`);
    return response.data;
  },

  /**
   * 管理员: 获取所有报名
   */
  getAllRegistrations: async () => {
    const response = await apiClient.get<Registration[]>('/registrations');
    return response.data;
  },

  /**
   * 获取赛事的报名列表
   */
  getRegistrationsByEvent: async (eventId: number, page = 0, size = 10) => {
    try {
      // 先尝试使用管理员API获取报名信息
      const response = await apiClient.get<PaginatedResponse<Registration>>(`/registrations/event/${eventId}`, {
        params: { page, size }
      });
      return response.data;
    } catch (error: any) {
      // 如果是401认证错误，尝试使用公共API
      if (error.response && error.response.status === 401) {
        console.warn('使用管理员API获取报名信息失败，尝试使用公共API');
        // 使用公共API获取报名信息
        const response = await apiClient.get<PaginatedResponse<Registration>>(`/registrations/public/event/${eventId}`, {
          params: { page, size }
        });
        return response.data;
      }
      // 其他错误则继续抛出
      throw error;
    }
  },

  /**
   * 管理员: 按事件和状态获取报名（分页）
   */
  getRegistrationsByEventAndStatus: async (eventId: number, status: string, page = 0, size = 10) => {
    const response = await apiClient.get<PaginatedResponse<Registration>>(
      `/registrations/event/${eventId}/status/${status}`,
      { params: { page, size } }
    );
    return response.data;
  },

  /**
   * 管理员: 更新报名状态
   */
  updateRegistrationStatus: async (registrationId: number, status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED', notes?: string) => {
    const response = await apiClient.put<Registration>(`/registrations/${registrationId}/status`, {
      status,
      notes
    });
    return response.data;
  },

  /**
   * 管理员: 获取赛事参与者列表
   */
  getEventParticipants: async (eventId: number, params: ParticipantQueryParams = {}) => {
    const response = await apiClient.get<PaginatedResponse<Participant>>(`/participants/event/${eventId}`, {
      params: {
        page: params.page || 0,
        size: params.size || 10,
        status: params.status || '',
        search: params.search || ''
      }
    });
    return response.data;
  },

  /**
   * 管理员: 签到参与者
   */
  checkInParticipant: async (participantId: number) => {
    const response = await apiClient.put<Participant>(`/participants/${participantId}/check-in`);
    return response.data;
  },

  /**
   * 管理员: 更新参与者备注
   */
  updateParticipantNotes: async (participantId: number, notes: string) => {
    const response = await apiClient.put<Participant>(`/participants/${participantId}/notes`, { notes });
    return response.data;
  },

  /**
   * 管理员: 取消参与者资格
   */
  cancelParticipation: async (participantId: number) => {
    const response = await apiClient.put<Participant>(`/participants/${participantId}/cancel`);
    return response.data;
  }
};

export default registrationAPI; 