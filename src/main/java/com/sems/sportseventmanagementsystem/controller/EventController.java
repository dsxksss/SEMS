package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
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
} 