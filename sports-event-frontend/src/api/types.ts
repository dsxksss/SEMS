// 通用类型定义

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