import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { eventsAPI, categoryAPI } from '../api';
import type { Event, EventCategory, PaginatedResponse } from '../api/eventsAPI';

export const useEventStore = defineStore('event', () => {
  // 状态
  const events = ref<Event[]>([]);
  const eventDetails = ref<Map<number, Event>>(new Map());
  const categories = ref<EventCategory[]>([]);
  const upcomingEvents = ref<Event[]>([]);
  const totalEvents = ref(0);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 获取活动列表
  async function fetchEvents(page = 0, size = 10, sortBy = 'startTime', sortDir = 'asc') {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await eventsAPI.getPublicEvents(page, size, sortBy, sortDir);
      events.value = response.content;
      totalEvents.value = response.totalElements;
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取活动列表失败';
      return null;
    } finally {
      loading.value = false;
    }
  }
  
  // 搜索活动
  async function searchEvents(name: string, page = 0, size = 10) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await eventsAPI.searchPublicEvents(name, page, size);
      events.value = response.content;
      totalEvents.value = response.totalElements;
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || '搜索活动失败';
      return null;
    } finally {
      loading.value = false;
    }
  }
  
  // 按类别获取活动
  async function fetchEventsByCategory(categoryId: number, page = 0, size = 10) {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await eventsAPI.getEventsByCategory(categoryId, page, size);
      events.value = response.content;
      totalEvents.value = response.totalElements;
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取类别活动失败';
      return null;
    } finally {
      loading.value = false;
    }
  }
  
  // 获取即将到来的活动
  async function fetchUpcomingEvents() {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await eventsAPI.getUpcomingEvents();
      upcomingEvents.value = response;
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取即将到来的活动失败';
      return null;
    } finally {
      loading.value = false;
    }
  }
  
  // 获取活动详情
  async function fetchEventDetails(id: number) {
    loading.value = true;
    error.value = null;
    
    try {
      const cachedEvent = eventDetails.value.get(id);
      if (cachedEvent) return cachedEvent;
      
      const event = await eventsAPI.getPublicEventById(id);
      eventDetails.value.set(id, event);
      return event;
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取活动详情失败';
      return null;
    } finally {
      loading.value = false;
    }
  }
  
  // 获取所有类别
  async function fetchCategories() {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await categoryAPI.getPublicCategories();
      categories.value = response;
      return response;
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取类别列表失败';
      return null;
    } finally {
      loading.value = false;
    }
  }
  
  return {
    // 状态
    events,
    eventDetails,
    categories,
    upcomingEvents,
    totalEvents,
    loading,
    error,
    
    // 方法
    fetchEvents,
    searchEvents,
    fetchEventsByCategory,
    fetchUpcomingEvents,
    fetchEventDetails,
    fetchCategories
  };
});