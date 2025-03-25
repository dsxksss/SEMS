package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;

import java.util.List;

public interface EventService {
    
    /**
     * 创建新赛事
     * @param eventDTO 赛事信息
     * @return 创建后的赛事
     */
    Event createEvent(EventDTO eventDTO);
    
    /**
     * 更新赛事信息
     * @param id 赛事ID
     * @param eventDTO 赛事信息
     * @return 更新后的赛事
     */
    Event updateEvent(Long id, EventDTO eventDTO);
    
    /**
     * 删除赛事
     * @param id 赛事ID
     * @return 是否删除成功
     */
    boolean deleteEvent(Long id);
    
    /**
     * 获取赛事详情
     * @param id 赛事ID
     * @return 赛事详情
     */
    Event getEventById(Long id);
    
    /**
     * 获取所有赛事列表
     * @return 赛事列表
     */
    List<Event> getAllEvents();
    
    /**
     * 分页获取赛事列表
     * @param page 页码
     * @param size 每页大小
     * @return 赛事列表
     */
    List<Event> getEventsByPage(int page, int size);
    
    /**
     * 通过状态筛选赛事
     * @param status 赛事状态
     * @return 符合状态的赛事列表
     */
    List<Event> getEventsByStatus(String status);
    
    /**
     * 获取即将开始的赛事
     * @param days 未来天数
     * @return 即将开始的赛事列表
     */
    List<Event> getUpcomingEvents(int days);

    List<Event> getLatestEvents(int limit);
} 