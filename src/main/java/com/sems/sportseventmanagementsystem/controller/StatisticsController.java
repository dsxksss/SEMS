package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.*;
import com.sems.sportseventmanagementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private EventCategoryRepository categoryRepository;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> dashboardStats = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 用户总数
            long userCount = userRepository.count();
            dashboardStats.put("userCount", userCount);

            // 获取赛事相关数据
            List<Event> allEvents = eventRepository.findAll();
            dashboardStats.put("eventCount", allEvents.size());
            
            long completedEventCount = allEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.COMPLETED)
                .count();
            dashboardStats.put("completedEventCount", completedEventCount);

            // 获取报名总数
            long registrationCount = registrationRepository.count();
            dashboardStats.put("registrationCount", registrationCount);

            // 最近赛事 - 按创建时间排序获取最近5条
            List<Map<String, Object>> recentEvents = allEvents.stream()
                .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
                .limit(5)
                .map(event -> {
                    Map<String, Object> eventMap = new HashMap<>();
                    eventMap.put("id", event.getId());
                    eventMap.put("name", event.getName());
                    eventMap.put("categoryName", event.getCategory() != null ? event.getCategory().getName() : "未分类");
                    eventMap.put("startDate", event.getStartTime().format(formatter));
                    eventMap.put("status", event.getStatus().toString());
                    return eventMap;
                })
                .collect(Collectors.toList());
            dashboardStats.put("recentEvents", recentEvents);

            // 最近报名 - 按创建时间排序获取最近5条
            List<Registration> registrations = registrationRepository.findAll();
            List<Map<String, Object>> recentRegistrations = registrations.stream()
                .sorted(Comparator.comparing(Registration::getCreatedAt).reversed())
                .limit(5)
                .map(reg -> {
                    Map<String, Object> regMap = new HashMap<>();
                    regMap.put("id", reg.getId());
                    regMap.put("username", reg.getUser() != null ? reg.getUser().getUsername() : "未知用户");
                    regMap.put("eventName", reg.getEvent() != null ? reg.getEvent().getName() : "未知赛事");
                    regMap.put("registrationDate", reg.getCreatedAt().format(formatter));
                    regMap.put("status", reg.getStatus().toString());
                    return regMap;
                })
                .collect(Collectors.toList());
            dashboardStats.put("recentRegistrations", recentRegistrations);

            // 系统公告 - 按创建时间排序获取最近5条
            List<Announcement> announcements = announcementRepository.findAll();
            List<Map<String, Object>> recentAnnouncements = announcements.stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt).reversed())
                .limit(5)
                .map(ann -> {
                    Map<String, Object> annMap = new HashMap<>();
                    annMap.put("id", ann.getId());
                    annMap.put("title", ann.getTitle());
                    annMap.put("content", ann.getContent());
                    annMap.put("createdDate", ann.getCreatedAt().format(formatter));
                    annMap.put("authorName", ann.getCreatedBy() != null ? ann.getCreatedBy().getUsername() : "未知用户");
                    return annMap;
                })
                .collect(Collectors.toList());
            dashboardStats.put("recentAnnouncements", recentAnnouncements);

            return ResponseEntity.ok(dashboardStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取仪表盘统计数据失败: " + e.getMessage());
        }
    }
} 