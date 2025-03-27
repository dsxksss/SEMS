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
import java.util.Random;

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

    private final Random random = new Random();

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
                
                // 获取所有赛事和用户
                List<Event> events = eventRepository.findAll();
                List<User> athletes = userRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == ERole.ROLE_ATHLETE))
                    .toList();
                
                if (!events.isEmpty() && !athletes.isEmpty()) {
                    // 为每个运动员随机报名一些赛事
                    initRegistrations(athletes, events);
                    // 为已完成的赛事添加成绩
                    initResults(athletes, events, admin);
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
        
        // 创建多个管理员
        createUserWithRole("admin2", "管理员2", ERole.ROLE_ADMIN);
        createUserWithRole("admin3", "管理员3", ERole.ROLE_ADMIN);
        
        // 创建多个运动员
        createUserWithRole("athlete", "张三", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete2", "李四", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete3", "王五", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete4", "赵六", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete5", "钱七", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete6", "孙八", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete7", "周九", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete8", "吴十", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete9", "郑十一", ERole.ROLE_ATHLETE);
        createUserWithRole("athlete10", "王十二", ERole.ROLE_ATHLETE);
        
        // 创建多个观众
        createUserWithRole("spectator", "观众1", ERole.ROLE_SPECTATOR);
        createUserWithRole("spectator2", "观众2", ERole.ROLE_SPECTATOR);
        createUserWithRole("spectator3", "观众3", ERole.ROLE_SPECTATOR);
        createUserWithRole("spectator4", "观众4", ERole.ROLE_SPECTATOR);
        createUserWithRole("spectator5", "观众5", ERole.ROLE_SPECTATOR);
    }
    
    private void createUserWithRole(String username, String realName, ERole roleType) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(encoder.encode(username + "123"));
        user.setPhone("139" + String.format("%08d", random.nextInt(100000000)));
        user.setRealName(realName);
        user.setEnabled(true);
        
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        userRepository.save(user);
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
        
        // 添加更多的即将开始的赛事
        createEvent("2024校园足球联赛", "2024年校园足球联赛，欢迎各院系组队参加！", 
                now.plusDays(7), now.plusDays(14), "北校区足球场", 16, 8, 
                EventStatus.PENDING, categories.get(0), admin);
        
        createEvent("篮球三人赛", "三人制篮球比赛，快速、激烈的对抗", 
                now.plusDays(5), now.plusDays(6), "东校区篮球场", 24, 12, 
                EventStatus.PENDING, categories.get(1), admin);
        
        createEvent("新生游泳挑战赛", "针对大一新生的游泳入门赛事", 
                now.plusDays(10), now.plusDays(11), "游泳馆", 30, 15, 
                EventStatus.PENDING, categories.get(5), admin);
        
        // 添加更多进行中的赛事
        createEvent("2024羽毛球校际邀请赛", "邀请周边高校参加的羽毛球友谊赛", 
                now.minusDays(2), now.plusDays(3), "主体育馆", 32, 28, 
                EventStatus.ONGOING, categories.get(2), admin);
        
        createEvent("乒乓球单打锦标赛", "校内乒乓球精英对决", 
                now.minusDays(1), now.plusDays(2), "乒乓球馆", 16, 16, 
                EventStatus.ONGOING, categories.get(3), admin);
        
        createEvent("网球双打友谊赛", "校际网球双打比赛", 
                now.minusDays(3), now.plusDays(1), "网球场", 20, 18, 
                EventStatus.ONGOING, categories.get(4), admin);
        
        // 添加更多已完成的赛事
        createEvent("2023年田径运动会", "年度田径运动会，包括短跑、长跑、跳高、跳远等项目", 
                now.minusDays(30), now.minusDays(28), "田径场", 100, 95, 
                EventStatus.COMPLETED, categories.get(6), admin);
        
        createEvent("冬季长跑挑战赛", "5公里和10公里长跑比赛", 
                now.minusDays(45), now.minusDays(45), "环校跑道", 50, 48, 
                EventStatus.COMPLETED, categories.get(6), admin);
        
        createEvent("羽毛球单打秋季赛", "校内羽毛球单打比赛", 
                now.minusDays(60), now.minusDays(58), "体育馆", 32, 30, 
                EventStatus.COMPLETED, categories.get(2), admin);
                
        createEvent("篮球校队选拔赛", "校队球员选拔测试", 
                now.minusDays(90), now.minusDays(88), "主体育馆", 40, 38, 
                EventStatus.COMPLETED, categories.get(1), admin);
    }
    
    private void createEvent(String name, String description, LocalDateTime startTime, LocalDateTime endTime, 
                           String location, int maxParticipants, int currentParticipants, 
                           EventStatus status, EventCategory category, User admin) {
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setLocation(location);
        event.setMaxParticipants(maxParticipants);
        event.setCurrentParticipants(currentParticipants);
        event.setIsActive(true);
        event.setStatus(status);
        event.setCategory(category);
        event.setCreatedBy(admin);
        eventRepository.save(event);
    }

    private void initAnnouncements(User admin) {
        System.out.println("Initializing announcements...");
        
        List<Event> events = eventRepository.findAll();
        
        // 全局公告
        createAnnouncement(
            "关于2024年体育赛事安排的通知", 
            "各位师生，2024年体育赛事安排已确定，请关注网站更新，及时报名参加您感兴趣的赛事活动。", 
            true, admin, null);
            
        createAnnouncement(
            "体育场馆维护通知", 
            "下周一至周三，主体育馆将进行年度维护，期间所有赛事和训练将暂停。", 
            true, admin, null);
            
        createAnnouncement(
            "运动员体检通知", 
            "所有参加正式比赛的运动员需在赛前一周内完成体检，请各位运动员提前安排时间。", 
            true, admin, null);
        
        // 赛事相关公告
        if (!events.isEmpty()) {
            for (int i = 0; i < Math.min(events.size(), 6); i++) {
                Event event = events.get(i);
                String title = "关于" + event.getName() + "的通知";
                String content = "亲爱的参赛者，" + event.getName() + "将于" + 
                    event.getStartTime().toLocalDate() + "开始，请做好准备工作。比赛地点：" + 
                    event.getLocation() + "。";
                
                createAnnouncement(title, content, true, admin, event);
            }
        }
    }
    
    private void createAnnouncement(String title, String content, boolean isPublished, User admin, Event event) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setIsPublished(isPublished);
        announcement.setCreatedBy(admin);
        announcement.setEvent(event);
        announcementRepository.save(announcement);
    }

    private void initRegistrations(List<User> athletes, List<Event> events) {
        System.out.println("Initializing registrations...");
        
        for (User athlete : athletes) {
            // 为每个运动员随机报名2-5个赛事
            int registrationCount = 2 + random.nextInt(4);
            
            for (int i = 0; i < registrationCount; i++) {
                Event event = events.get(random.nextInt(events.size()));
                
                // 避免重复报名
                if (!registrationRepository.existsByUserIdAndEventId(athlete.getId(), event.getId())) {
                    Registration registration = new Registration();
                    registration.setUser(athlete);
                    registration.setEvent(event);
                    
                    // 随机设置报名状态
                    RegistrationStatus[] statuses = {
                        RegistrationStatus.PENDING, 
                        RegistrationStatus.APPROVED, 
                        RegistrationStatus.REJECTED, 
                        RegistrationStatus.CANCELLED
                    };
                    // 已完成的赛事报名为已批准
                    if (event.getStatus() == EventStatus.COMPLETED) {
                        registration.setStatus(RegistrationStatus.APPROVED);
                    } else {
                        // 其他赛事随机状态，但70%概率为已批准
                        int randomIndex = random.nextInt(10) < 7 ? 1 : random.nextInt(statuses.length);
                        registration.setStatus(statuses[randomIndex]);
                    }
                    
                    registration.setContactPhone(athlete.getPhone());
                    registration.setRemarks("希望参加" + event.getName() + "比赛");
                    registration.setHasPaid(random.nextBoolean());
                    
                    registrationRepository.save(registration);
                }
            }
        }
    }

    private void initResults(List<User> athletes, List<Event> events, User admin) {
        System.out.println("Initializing results...");
        
        // 仅为已完成的赛事添加成绩
        List<Event> completedEvents = events.stream()
                .filter(event -> event.getStatus() == EventStatus.COMPLETED)
                .toList();
        
        for (Event event : completedEvents) {
            // 获取该赛事已批准的报名
            List<Registration> approvedRegistrations = registrationRepository.findByEventId(event.getId())
                    .stream()
                    .filter(reg -> reg.getStatus() == RegistrationStatus.APPROVED)
                    .toList();
            
            // 如果没有已批准的报名，则为一些运动员随机创建成绩
            if (approvedRegistrations.isEmpty()) {
                int resultsCount = 5 + random.nextInt(10);
                
                for (int i = 0; i < Math.min(resultsCount, athletes.size()); i++) {
                    User athlete = athletes.get(i);
                    createRandomResult(event, athlete, i + 1, admin);
                }
            } else {
                // 为已批准的报名创建成绩
                for (int i = 0; i < approvedRegistrations.size(); i++) {
                    Registration registration = approvedRegistrations.get(i);
                    createRandomResult(event, registration.getUser(), i + 1, admin);
                }
            }
        }
    }
    
    private void createRandomResult(Event event, User athlete, int rank, User admin) {
        // 避免重复记录成绩
        if (!resultRepository.existsByEventIdAndAthleteId(event.getId(), athlete.getId())) {
            EventResult result = new EventResult();
            result.setEvent(event);
            result.setAthlete(athlete);
            result.setRank(rank);
            
            // 根据不同的比赛类别生成不同的成绩格式
            String categoryName = event.getCategory().getName();
            String score;
            
            switch (categoryName) {
                case "游泳":
                case "田径":
                    // 时间格式 (分:秒.毫秒)
                    int minutes = random.nextInt(3);
                    int seconds = random.nextInt(60);
                    int milliseconds = random.nextInt(100);
                    score = String.format("%d:%02d.%02d", minutes, seconds, milliseconds);
                    break;
                case "篮球":
                    // 得分
                    score = String.valueOf(10 + random.nextInt(30));
                    break;
                case "足球":
                    // 进球数
                    score = String.valueOf(random.nextInt(5));
                    break;
                default:
                    // 默认得分制
                    score = String.valueOf(70 + random.nextInt(30));
            }
            
            result.setScore(score);
            
            // 随机评论
            String[] remarkOptions = {
                "出色的表现", "良好发挥", "稳定发挥", "技术精湛", "战术执行到位",
                "有待提高", "潜力选手", "进步明显", "表现突出", "状态良好"
            };
            result.setRemarks(remarkOptions[random.nextInt(remarkOptions.length)]);
            
            result.setRecordedBy(admin);
            resultRepository.save(result);
        }
    }
} 