// 赛事分类
export interface EventCategory {
  id: number
  name: string
  description: string
  isActive: boolean
  eventCount?: number // 添加可选的eventCount字段
}

// 赛事
export interface Event {
  id: number
  name: string
  categoryId: number
  categoryName: string
  startDate: string
  endDate: string
  location: string
  maxParticipants: number
  currentParticipants: number
  status: 'draft' | 'published' | 'ongoing' | 'completed' | 'cancelled'
  description: string
  rules: string
  registrationDeadline: string
  createdAt: string
  updatedAt: string
}

// 报名
export interface Registration {
  id: number
  eventId: number
  eventName: string
  athleteId: number
  athleteName: string
  gender: 'male' | 'female'
  phone: string
  status: 'pending' | 'approved' | 'rejected' | 'cancelled'
  registrationTime: string
  remarks: string
}

// 成绩
export interface Result {
  id: number
  eventId: number
  eventName: string
  athleteId: number
  athleteName: string
  score: number
  rank: number
  unit: string
  category: string
  recordTime: string
  recorder: string
  remarks: string
}

// 分页响应
export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
} 