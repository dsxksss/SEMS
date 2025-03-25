package com.sems.sportseventmanagementsystem;

import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Announcement;
import com.sems.sportseventmanagementsystem.service.AnnouncementService;
import com.sems.sportseventmanagementsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SemsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemsApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner initData(AnnouncementService announcementService, EventService eventService) {
        return args -> {
            // 检查是否已经有数据
            List<Announcement> existingAnnouncements = announcementService.getAllAnnouncements();
            if (!existingAnnouncements.isEmpty()) {
                System.out.println("数据库中已存在数据，跳过初始化。");
                return;
            }
            
            // 初始化公告数据
            initAnnouncements(announcementService);
            
            // 初始化赛事数据
            initEvents(eventService);
            
            System.out.println("示例数据初始化完成！");
        };
    }
    
    private void initAnnouncements(AnnouncementService announcementService) {
        List<Announcement> announcements = new ArrayList<>();
        
        // 创建系统公告
        Announcement announcement1 = new Announcement();
        announcement1.setTitle("欢迎使用体育赛事管理系统");
        announcement1.setContent("感谢您使用本系统，如有任何问题请联系管理员。");
        announcement1.setType("系统通知");
        announcement1.setStatus(1);
        announcements.add(announcement1);
        
        // 创建赛事公告
        Announcement announcement2 = new Announcement();
        announcement2.setTitle("2023年校园足球联赛即将开始");
        announcement2.setContent("2023年校园足球联赛将于本月15日开始，请各参赛队伍做好准备。");
        announcement2.setType("赛事通知");
        announcement2.setStatus(1);
        announcements.add(announcement2);
        
        // 创建规则公告
        Announcement announcement3 = new Announcement();
        announcement3.setTitle("参赛守则");
        announcement3.setContent("所有参赛选手必须遵守比赛规则，保持良好的体育精神。违反规则的选手将被取消比赛资格。");
        announcement3.setType("规则说明");
        announcement3.setStatus(1);
        announcements.add(announcement3);
        
        // 创建更新公告
        Announcement announcement4 = new Announcement();
        announcement4.setTitle("系统功能更新");
        announcement4.setContent("系统新增了在线报名功能，参赛者可以直接在线完成报名流程。");
        announcement4.setType("系统通知");
        announcement4.setStatus(1);
        announcements.add(announcement4);
        
        // 创建紧急公告
        Announcement announcement5 = new Announcement();
        announcement5.setTitle("场地变更通知");
        announcement5.setContent("由于天气原因，原定于室外举行的篮球比赛改为在体育馆进行，请相关人员知悉。");
        announcement5.setType("紧急通知");
        announcement5.setStatus(1);
        announcements.add(announcement5);
        
        // 保存所有公告
        for (Announcement announcement : announcements) {
            announcementService.createAnnouncement(announcement);
        }
    }
    
    private void initEvents(EventService eventService) {
        List<EventDTO> events = new ArrayList<>();
        
        // 创建已结束的赛事
        EventDTO event1 = new EventDTO();
        event1.setName("2023年春季田径运动会");
        event1.setDescription("2023年春季校园田径运动会，包含短跑、长跑、跳高、跳远等多个项目。");
        event1.setStartTime(LocalDateTime.now().minusDays(30));
        event1.setEndTime(LocalDateTime.now().minusDays(28));
        event1.setLocation("学校田径场");
        event1.setStatus("已结束");
        event1.setMaxParticipants(200);
        events.add(event1);
        
        // 创建正在进行的赛事
        EventDTO event2 = new EventDTO();
        event2.setName("2023年校园篮球联赛");
        event2.setDescription("各学院参与的篮球联赛，采用淘汰赛制。");
        event2.setStartTime(LocalDateTime.now().minusDays(5));
        event2.setEndTime(LocalDateTime.now().plusDays(10));
        event2.setLocation("学校体育馆");
        event2.setStatus("进行中");
        event2.setMaxParticipants(100);
        events.add(event2);
        
        // 创建即将开始的赛事
        EventDTO event3 = new EventDTO();
        event3.setName("2023年秋季足球友谊赛");
        event3.setDescription("校际足球友谊赛，增进校际交流。");
        event3.setStartTime(LocalDateTime.now().plusDays(2));
        event3.setEndTime(LocalDateTime.now().plusDays(3));
        event3.setLocation("学校足球场");
        event3.setStatus("报名中");
        event3.setMaxParticipants(60);
        events.add(event3);
        
        // 创建计划中的赛事
        EventDTO event4 = new EventDTO();
        event4.setName("2023年冬季游泳比赛");
        event4.setDescription("校内游泳比赛，项目包括自由泳、蛙泳、仰泳和蝶泳等。");
        event4.setStartTime(LocalDateTime.now().plusDays(15));
        event4.setEndTime(LocalDateTime.now().plusDays(16));
        event4.setLocation("学校游泳馆");
        event4.setStatus("筹备中");
        event4.setMaxParticipants(50);
        events.add(event4);
        
        // 创建另一个即将开始的赛事
        EventDTO event5 = new EventDTO();
        event5.setName("校园乒乓球锦标赛");
        event5.setDescription("校园乒乓球锦标赛，男女单打、双打以及团体赛项目。");
        event5.setStartTime(LocalDateTime.now().plusDays(5));
        event5.setEndTime(LocalDateTime.now().plusDays(7));
        event5.setLocation("学校乒乓球室");
        event5.setStatus("报名中");
        event5.setMaxParticipants(80);
        events.add(event5);
        
        // 保存所有赛事
        for (EventDTO event : events) {
            try {
                eventService.createEvent(event);
            } catch (Exception e) {
                System.err.println("创建赛事失败: " + event.getName() + ", 错误: " + e.getMessage());
            }
        }
    }
} 