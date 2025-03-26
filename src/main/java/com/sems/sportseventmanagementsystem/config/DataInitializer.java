package com.sems.sportseventmanagementsystem.config;

import com.sems.sportseventmanagementsystem.entity.*;
import com.sems.sportseventmanagementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventCategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventResultRepository resultRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        // 只有当没有角色数据时才初始化
        if (roleRepository.count() == 0) {
            initRoles();
            initUsers();
            User admin = userRepository.findByUsername("admin").orElse(null);
            
            if (admin != null) {
                initCategories();
                initEvents(admin);
                initAnnouncements(admin);
                
                // 获取一个赛事用于初始化报名和结果数据
                List<Event> events = eventRepository.findAll();
                if (!events.isEmpty()) {
                    Event event = events.get(0);
                    
                    // 获取运动员进行报名初始化
                    User athlete = userRepository.findByUsername("athlete").orElse(null);
                    if (athlete != null) {
                        initRegistrations(athlete, event);
                        initResults(athlete, event, admin);
                    }
                }
            }
        }
    }

    private void initRoles() {
        System.out.println("Initializing roles...");
        Arrays.asList(ERole.values()).forEach(role -> 
            roleRepository.save(new Role(role))
        );
    }

    private void initUsers() {
        System.out.println("Initializing users...");
        
        // 创建管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(encoder.encode("admin123"));
        admin.setPhone("13800138000");
        admin.setRealName("系统管理员");
        admin.setEnabled(true);
        
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        admin.setRoles(adminRoles);
        
        userRepository.save(admin);
        
        // 创建运动员
        User athlete = new User();
        athlete.setUsername("athlete");
        athlete.setEmail("athlete@example.com");
        athlete.setPassword(encoder.encode("athlete123"));
        athlete.setPhone("13900139000");
        athlete.setRealName("张三");
        athlete.setEnabled(true);
        
        Role athleteRole = roleRepository.findByName(ERole.ROLE_ATHLETE)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> athleteRoles = new HashSet<>();
        athleteRoles.add(athleteRole);
        athlete.setRoles(athleteRoles);
        
        userRepository.save(athlete);
        
        // 创建观众
        User spectator = new User();
        spectator.setUsername("spectator");
        spectator.setEmail("spectator@example.com");
        spectator.setPassword(encoder.encode("spectator123"));
        spectator.setPhone("13700137000");
        spectator.setRealName("李四");
        spectator.setEnabled(true);
        
        Role spectatorRole = roleRepository.findByName(ERole.ROLE_SPECTATOR)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> spectatorRoles = new HashSet<>();
        spectatorRoles.add(spectatorRole);
        spectator.setRoles(spectatorRoles);
        
        userRepository.save(spectator);
    }

    private void initCategories() {
        System.out.println("Initializing categories...");
        
        List<String> categoryNames = Arrays.asList("足球", "篮球", "羽毛球", "乒乓球", "网球", "游泳", "田径", "健身");
        List<String> categoryDescs = Arrays.asList(
                "11人制足球比赛与活动", 
                "5v5篮球比赛与活动", 
                "羽毛球单打、双打比赛", 
                "乒乓球单打、双打、团体赛", 
                "网球单打、双打比赛", 
                "游泳各泳姿竞速赛", 
                "田径各项目比赛", 
                "健身活动与挑战赛");
        
        for (int i = 0; i < categoryNames.size(); i++) {
            EventCategory category = new EventCategory();
            category.setName(categoryNames.get(i));
            category.setDescription(categoryDescs.get(i));
            category.setIsActive(true);
            categoryRepository.save(category);
        }
    }

    private void initEvents(User admin) {
        System.out.println("Initializing events...");
        
        List<EventCategory> categories = categoryRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        // 添加即将开始的赛事
        Event upcoming = new Event();
        upcoming.setName("2024校园足球联赛");
        upcoming.setDescription("2024年校园足球联赛，欢迎各院系组队参加！");
        upcoming.setStartTime(now.plusDays(7));
        upcoming.setEndTime(now.plusDays(14));
        upcoming.setLocation("北校区足球场");
        upcoming.setMaxParticipants(16);
        upcoming.setCurrentParticipants(8);
        upcoming.setIsActive(true);
        upcoming.setStatus(EventStatus.PENDING);
        upcoming.setCategory(categories.get(0)); // 足球
        upcoming.setCreatedBy(admin);
        eventRepository.save(upcoming);
        
        // 添加进行中的赛事
        Event ongoing = new Event();
        ongoing.setName("2024羽毛球校际邀请赛");
        ongoing.setDescription("邀请周边高校参加的羽毛球友谊赛");
        ongoing.setStartTime(now.minusDays(2));
        ongoing.setEndTime(now.plusDays(3));
        ongoing.setLocation("主体育馆");
        ongoing.setMaxParticipants(32);
        ongoing.setCurrentParticipants(28);
        ongoing.setIsActive(true);
        ongoing.setStatus(EventStatus.ONGOING);
        ongoing.setCategory(categories.get(2)); // 羽毛球
        ongoing.setCreatedBy(admin);
        eventRepository.save(ongoing);
        
        // 添加已完成的赛事
        Event completed = new Event();
        completed.setName("2023年田径运动会");
        completed.setDescription("年度田径运动会，包括短跑、长跑、跳高、跳远等项目");
        completed.setStartTime(now.minusDays(30));
        completed.setEndTime(now.minusDays(28));
        completed.setLocation("田径场");
        completed.setMaxParticipants(100);
        completed.setCurrentParticipants(95);
        completed.setIsActive(true);
        completed.setStatus(EventStatus.COMPLETED);
        completed.setCategory(categories.get(6)); // 田径
        completed.setCreatedBy(admin);
        eventRepository.save(completed);
    }

    private void initAnnouncements(User admin) {
        System.out.println("Initializing announcements...");
        
        List<Event> events = eventRepository.findAll();
        
        Announcement announcement1 = new Announcement();
        announcement1.setTitle("关于2024年体育赛事安排的通知");
        announcement1.setContent("各位师生，2024年体育赛事安排已确定，请关注网站更新，及时报名参加您感兴趣的赛事活动。");
        announcement1.setIsPublished(true);
        announcement1.setCreatedBy(admin);
        announcementRepository.save(announcement1);
        
        if (!events.isEmpty()) {
            Announcement announcement2 = new Announcement();
            announcement2.setTitle("足球联赛报名开始通知");
            announcement2.setContent("2024校园足球联赛报名现已开始，请各参赛队伍于5月10日前完成报名手续。");
            announcement2.setIsPublished(true);
            announcement2.setCreatedBy(admin);
            announcement2.setEvent(events.get(0));
            announcementRepository.save(announcement2);
            
            Announcement announcement3 = new Announcement();
            announcement3.setTitle("羽毛球邀请赛赛程公布");
            announcement3.setContent("2024羽毛球校际邀请赛的详细赛程安排已公布，请参赛选手查看并准时参加比赛。");
            announcement3.setIsPublished(true);
            announcement3.setCreatedBy(admin);
            announcement3.setEvent(events.get(1));
            announcementRepository.save(announcement3);
        }
    }

    private void initRegistrations(User athlete, Event event) {
        System.out.println("Initializing registrations...");
        
        Registration registration = new Registration();
        registration.setUser(athlete);
        registration.setEvent(event);
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setContactPhone(athlete.getPhone());
        registration.setRemarks("我希望参加本次比赛");
        registration.setHasPaid(true);
        
        registrationRepository.save(registration);
    }

    private void initResults(User athlete, Event event, User admin) {
        System.out.println("Initializing results...");
        
        EventResult result = new EventResult();
        result.setEvent(event);
        result.setAthlete(athlete);
        result.setRank(1);
        result.setScore("90");
        result.setRemarks("优秀表现");
        result.setRecordedBy(admin);
        
        resultRepository.save(result);
    }
} 