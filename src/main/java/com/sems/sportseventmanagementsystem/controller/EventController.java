package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * 创建新赛事
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Event> createEvent(@RequestBody EventDTO eventDTO) {
        return Result.success(eventService.createEvent(eventDTO));
    }

    /**
     * 更新赛事信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Event> updateEvent(@PathVariable Long id, @RequestBody EventDTO eventDTO) {
        return Result.success(eventService.updateEvent(id, eventDTO));
    }

    /**
     * 删除赛事
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteEvent(@PathVariable Long id) {
        boolean result = eventService.deleteEvent(id);
        return Result.success(result);
    }

    /**
     * 获取赛事详情
     */
    @GetMapping("/{id}")
    public Result<Event> getEventById(@PathVariable Long id) {
        return Result.success(eventService.getEventById(id));
    }

    /**
     * 获取所有赛事
     */
    @GetMapping
    public Result<List<Event>> getAllEvents() {
        return Result.success(eventService.getAllEvents());
    }

    /**
     * 分页获取赛事
     */
    @GetMapping("/page")
    public Result<List<Event>> getEventsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(eventService.getEventsByPage(page, size));
    }

    /**
     * 通过状态筛选赛事
     */
    @GetMapping("/status/{status}")
    public Result<List<Event>> getEventsByStatus(@PathVariable String status) {
        return Result.success(eventService.getEventsByStatus(status));
    }

    /**
     * 获取即将开始的赛事
     */
    @GetMapping("/upcoming")
    public Result<List<Event>> getUpcomingEvents(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(eventService.getUpcomingEvents(days));
    }

    /**
     * 获取最新赛事
     */
    @GetMapping("/latest")
    public Result<List<Event>> getLatestEvents(
            @RequestParam(defaultValue = "3") int limit) {
        return Result.success(eventService.getLatestEvents(limit));
    }
    
    /**
     * 公开接口：获取公开赛事列表（分页）
     * 此接口不需要身份验证，返回符合条件的赛事信息
     */
    @GetMapping("/public")
    public Result<Map<String, Object>> getPublicEvents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<Event> events = eventService.getEventsByPage(page, size);
        // 这里可以根据查询参数进行更复杂的筛选，目前简单返回分页数据
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", events);
        result.put("total", events.size()); // 实际项目中应该返回总数
        result.put("page", page);
        result.put("size", size);
        
        return Result.success(result);
    }
} 