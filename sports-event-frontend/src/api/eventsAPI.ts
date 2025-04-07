import apiClient from './axios';

export interface Event {
  id: number;
  name: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
  registrationDeadline: string;
  maxParticipants: number;
  currentParticipants?: number;
  status: 'UPCOMING' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';
  isActive: boolean;
  category: EventCategory;
  createdAt: string;
  updatedAt: string;
}

export interface EventCategory {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  last: boolean;
  first: boolean;
  empty: boolean;
}

export const eventsAPI = {
  /**
   * 获取公开事件列表（分页）
   */
  getPublicEvents: async (page = 0, size = 10, sortBy = 'startTime', sortDir = 'asc') => {
    const response = await apiClient.get<PaginatedResponse<Event>>(`/events/public`, {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  /**
   * 搜索公开事件（分页）
   */
  searchPublicEvents: async (name: string, page = 0, size = 10) => {
    const response = await apiClient.get<PaginatedResponse<Event>>(`/events/public/search`, {
      params: { name, page, size }
    });
    return response.data;
  },

  /**
   * 按类别获取公开事件（分页）
   */
  getEventsByCategory: async (categoryId: number, page = 0, size = 10) => {
    const response = await apiClient.get<PaginatedResponse<Event>>(`/events/public/category/${categoryId}`, {
      params: { page, size }
    });
    return response.data;
  },

  /**
   * 获取即将到来的事件
   */
  getUpcomingEvents: async () => {
    const response = await apiClient.get<Event[]>('/events/public/upcoming');
    return response.data;
  },

  /**
   * 按状态获取公开事件（分页）
   */
  getEventsByStatus: async (status: string, page = 0, size = 10) => {
    const response = await apiClient.get<PaginatedResponse<Event>>(`/events/public/status/${status}`, {
      params: { page, size }
    });
    return response.data;
  },

  /**
   * 获取公开事件详情
   */
  getPublicEventById: async (id: number) => {
    const response = await apiClient.get<Event>(`/events/public/${id}`);
    return response.data;
  },

  /**
   * 管理员: 获取所有事件
   */
  getAllEvents: async () => {
    const response = await apiClient.get<Event[]>('/events');
    return response.data;
  },

  /**
   * 管理员: 获取事件详情
   */
  getEventById: async (id: number) => {
    try {
      // 先尝试使用管理员API获取详细信息
      const response = await apiClient.get<Event>(`/events/${id}`);
      return response.data;
    } catch (error: any) {
      // 如果是401认证错误，尝试使用公共API获取基本信息
      if (error.response && error.response.status === 401) {
        console.warn('使用管理员API获取事件失败，尝试使用公共API');
        try {
          const response = await apiClient.get<Event>(`/events/public/${id}`);
          return response.data;
        } catch (publicError) {
          console.error('公共API获取事件也失败:', publicError);
          throw publicError;
        }
      }
      // 其他错误则继续抛出
      throw error;
    }
  },

  /**
   * 管理员: 创建事件
   */
  createEvent: async (event: Omit<Event, 'id' | 'createdAt' | 'updatedAt'>) => {
    const response = await apiClient.post<Event>('/events', event);
    return response.data;
  },

  /**
   * 管理员: 更新事件
   */
  updateEvent: async (id: number, event: Partial<Event>) => {
    const response = await apiClient.put<Event>(`/events/${id}`, event);
    return response.data;
  },

  /**
   * 管理员: 更新事件状态
   */
  updateEventStatus: async (id: number, status: 'PENDING' | 'ONGOING' | 'COMPLETED' | 'CANCELLED') => {
    const response = await apiClient.put<Event>(`/events/${id}/status`, { status });
    return response.data;
  },

  /**
   * 管理员: 取消事件
   */
  cancelEvent: async (id: number) => {
    const response = await apiClient.put<Event>(`/events/${id}/cancel`);
    return response.data;
  },

  /**
   * 管理员: 删除事件
   */
  deleteEvent: async (id: number) => {
    const response = await apiClient.delete(`/events/${id}`);
    return response.data;
  }
};

export default eventsAPI; 