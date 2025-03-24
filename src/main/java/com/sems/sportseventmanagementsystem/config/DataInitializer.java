package com.sems.sportseventmanagementsystem.config;

import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.model.entity.Announcement;
import com.sems.sportseventmanagementsystem.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, EventRepository eventRepository, AnnouncementRepository announcementRepository) {
        return args -> {
            // 只有在数据库为空时才初始化数据
            if (userRepository.count() == 0) {
                System.out.println("正在初始化用户数据...");
                initializeUsers(userRepository);
            }

            if (eventRepository.count() == 0) {
                System.out.println("正在初始化赛事数据...");
                initializeEvents(eventRepository);
            }
            
            if (announcementRepository.count() == 0) {
                System.out.println("正在初始化公告数据...");
                initializeAnnouncements(announcementRepository);
            }
        };
    }

    private void initializeUsers(UserRepository userRepository) {
        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setRealName("系统管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        userRepository.save(admin);

        // 创建普通用户
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("user123"));
        user1.setEmail("user1@example.com");
        user1.setPhone("13900139001");
        user1.setRealName("张三");
        user1.setRole("USER");
        user1.setStatus(1);
        user1.setCreateTime(LocalDateTime.now());
        user1.setUpdateTime(LocalDateTime.now());
        userRepository.save(user1);

        // 创建运动员用户
        User athlete1 = new User();
        athlete1.setUsername("athlete1");
        athlete1.setPassword(passwordEncoder.encode("athlete123"));
        athlete1.setEmail("athlete1@example.com");
        athlete1.setPhone("13700137001");
        athlete1.setRealName("李四");
        athlete1.setRole("ATHLETE");
        athlete1.setStatus(1);
        athlete1.setCreateTime(LocalDateTime.now());
        athlete1.setUpdateTime(LocalDateTime.now());
        userRepository.save(athlete1);
        
        // 创建观众用户
        User audience1 = new User();
        audience1.setUsername("audience1");
        audience1.setPassword(passwordEncoder.encode("audience123"));
        audience1.setEmail("audience1@example.com");
        audience1.setPhone("13600136001");
        audience1.setRealName("王五");
        audience1.setRole("AUDIENCE");
        audience1.setStatus(1);
        audience1.setCreateTime(LocalDateTime.now());
        audience1.setUpdateTime(LocalDateTime.now());
        userRepository.save(audience1);

        System.out.println("用户数据初始化完成");
    }

    private void initializeEvents(EventRepository eventRepository) {
        // 创建一些示例赛事
        
        // 已完成的赛事
        Event pastEvent1 = new Event();
        pastEvent1.setName("2023年春季校园运动会");
        pastEvent1.setDescription("校园传统体育盛事，包含田径、球类等多种项目。这是一个展示学生体育才能的平台，欢迎全校师生积极参与！");
        pastEvent1.setStartTime(LocalDateTime.of(2023, 4, 15, 9, 0));
        pastEvent1.setEndTime(LocalDateTime.of(2023, 4, 17, 18, 0));
        pastEvent1.setLocation("中央体育场");
        pastEvent1.setStatus("COMPLETED");
        pastEvent1.setMaxParticipants(500);
        pastEvent1.setCurrentParticipants(320);
        pastEvent1.setCreateTime(LocalDateTime.of(2023, 3, 1, 10, 0));
        pastEvent1.setUpdateTime(LocalDateTime.of(2023, 4, 18, 15, 0));
        Event savedPastEvent1 = eventRepository.save(pastEvent1);
        addEventCategories(savedPastEvent1.getId(), Arrays.asList("田径", "球类", "校园活动"));

        Event pastEvent2 = new Event();
        pastEvent2.setName("2023年暑期篮球联赛");
        pastEvent2.setDescription("暑期篮球爱好者联赛，分团体和个人组。比赛采用淘汰赛制，优胜者将获得丰厚奖励。欢迎篮球爱好者踊跃报名！");
        pastEvent2.setStartTime(LocalDateTime.of(2023, 7, 10, 14, 0));
        pastEvent2.setEndTime(LocalDateTime.of(2023, 7, 20, 20, 0));
        pastEvent2.setLocation("市体育馆");
        pastEvent2.setStatus("COMPLETED");
        pastEvent2.setMaxParticipants(200);
        pastEvent2.setCurrentParticipants(185);
        pastEvent2.setCreateTime(LocalDateTime.of(2023, 6, 1, 9, 0));
        pastEvent2.setUpdateTime(LocalDateTime.of(2023, 7, 21, 10, 0));
        Event savedPastEvent2 = eventRepository.save(pastEvent2);
        addEventCategories(savedPastEvent2.getId(), Arrays.asList("篮球", "团队赛", "个人赛"));

        // 正在进行的赛事
        Event ongoingEvent = new Event();
        ongoingEvent.setName("2023年秋季足球联赛");
        ongoingEvent.setDescription("秋季足球联赛，面向各年龄段足球爱好者。比赛将持续两个月，每周末进行，欢迎各队伍踊跃报名参与！");
        ongoingEvent.setStartTime(LocalDateTime.now().minusDays(10));
        ongoingEvent.setEndTime(LocalDateTime.now().plusDays(20));
        ongoingEvent.setLocation("市足球场");
        ongoingEvent.setStatus("ONGOING");
        ongoingEvent.setMaxParticipants(300);
        ongoingEvent.setCurrentParticipants(250);
        ongoingEvent.setCreateTime(LocalDateTime.now().minusDays(30));
        ongoingEvent.setUpdateTime(LocalDateTime.now().minusDays(5));
        Event savedOngoingEvent = eventRepository.save(ongoingEvent);
        addEventCategories(savedOngoingEvent.getId(), Arrays.asList("足球", "团队赛", "户外活动"));

        // 即将开始的赛事
        Event upcomingEvent1 = new Event();
        upcomingEvent1.setName("2023年秋季马拉松挑战赛");
        upcomingEvent1.setDescription("城市环城马拉松比赛，全程42.195公里。比赛途经市区多个著名景点，将设置多个补给站和医疗点，确保参赛者安全。");
        upcomingEvent1.setStartTime(LocalDateTime.now().plusDays(15));
        upcomingEvent1.setEndTime(LocalDateTime.now().plusDays(15).plusHours(8));
        upcomingEvent1.setLocation("城市环路");
        upcomingEvent1.setStatus("UPCOMING");
        upcomingEvent1.setMaxParticipants(1000);
        upcomingEvent1.setCurrentParticipants(578);
        upcomingEvent1.setCreateTime(LocalDateTime.now().minusDays(60));
        upcomingEvent1.setUpdateTime(LocalDateTime.now().minusDays(2));
        Event savedUpcomingEvent1 = eventRepository.save(upcomingEvent1);
        addEventCategories(savedUpcomingEvent1.getId(), Arrays.asList("马拉松", "长跑", "户外活动"));

        Event upcomingEvent2 = new Event();
        upcomingEvent2.setName("2023年冬季滑雪节");
        upcomingEvent2.setDescription("冬季户外运动盛事，包含多种雪上项目。活动设置初级、中级和高级三个难度的雪道，适合不同水平的滑雪爱好者参与。");
        upcomingEvent2.setStartTime(LocalDateTime.now().plusDays(45));
        upcomingEvent2.setEndTime(LocalDateTime.now().plusDays(50));
        upcomingEvent2.setLocation("雪山度假村");
        upcomingEvent2.setStatus("UPCOMING");
        upcomingEvent2.setMaxParticipants(400);
        upcomingEvent2.setCurrentParticipants(150);
        upcomingEvent2.setCreateTime(LocalDateTime.now().minusDays(20));
        upcomingEvent2.setUpdateTime(LocalDateTime.now());
        Event savedUpcomingEvent2 = eventRepository.save(upcomingEvent2);
        addEventCategories(savedUpcomingEvent2.getId(), Arrays.asList("滑雪", "冬季运动", "户外活动"));
        
        // 添加赛事图片
        addEventImages(savedPastEvent1.getId(), "https://placekitten.com/800/400");
        addEventImages(savedPastEvent2.getId(), "https://placekitten.com/801/400");
        addEventImages(savedOngoingEvent.getId(), "https://placekitten.com/802/400");
        addEventImages(savedUpcomingEvent1.getId(), "https://placekitten.com/803/400");
        addEventImages(savedUpcomingEvent2.getId(), "https://placekitten.com/804/400");

        System.out.println("赛事数据初始化完成");
    }
    
    private void addEventCategories(Long eventId, List<String> categories) {
        for (String category : categories) {
            jdbcTemplate.update(
                "INSERT INTO event_category (event_id, category) VALUES (?, ?)",
                eventId, category
            );
        }
    }
    
    private void addEventImages(Long eventId, String imageUrl) {
        jdbcTemplate.update(
            "INSERT INTO event_image (event_id, image_url, create_time) VALUES (?, ?, ?)",
            eventId, imageUrl, LocalDateTime.now()
        );
    }
    
    private void initializeAnnouncements(AnnouncementRepository announcementRepository) {
        // 系统公告
        Announcement systemAnnouncement1 = new Announcement();
        systemAnnouncement1.setTitle("系统升级通知");
        systemAnnouncement1.setContent("尊敬的用户，本系统将于2023年11月15日凌晨2:00-4:00进行例行维护升级，期间系统功能将暂停使用。给您带来的不便，敬请谅解。");
        systemAnnouncement1.setType("SYSTEM");
        systemAnnouncement1.setStatus(1);
        systemAnnouncement1.setCreateTime(LocalDateTime.now().minusDays(5));
        systemAnnouncement1.setUpdateTime(LocalDateTime.now().minusDays(5));
        announcementRepository.save(systemAnnouncement1);
        
        // 活动公告
        Announcement eventAnnouncement1 = new Announcement();
        eventAnnouncement1.setTitle("2023年秋季马拉松挑战赛报名开始");
        eventAnnouncement1.setContent("2023年秋季马拉松挑战赛报名通道已开放！本次比赛将于2023年11月15日在城市环路举行，全程42.195公里。参赛者将获得精美纪念T恤和完赛奖牌。名额有限，请尽快报名！");
        eventAnnouncement1.setType("EVENT");
        eventAnnouncement1.setStatus(1);
        eventAnnouncement1.setCreateTime(LocalDateTime.now().minusDays(10));
        eventAnnouncement1.setUpdateTime(LocalDateTime.now().minusDays(10));
        announcementRepository.save(eventAnnouncement1);
        
        Announcement eventAnnouncement2 = new Announcement();
        eventAnnouncement2.setTitle("2023年冬季滑雪节报名优惠");
        eventAnnouncement2.setContent("2023年冬季滑雪节早鸟优惠来袭！即日起至11月30日报名参与，可享受8折优惠。本次活动将在雪山度假村举行，包含多种雪上项目，适合不同水平的滑雪爱好者。");
        eventAnnouncement2.setType("EVENT");
        eventAnnouncement2.setStatus(1);
        eventAnnouncement2.setCreateTime(LocalDateTime.now().minusDays(3));
        eventAnnouncement2.setUpdateTime(LocalDateTime.now().minusDays(3));
        announcementRepository.save(eventAnnouncement2);
        
        // 紧急公告
        Announcement emergencyAnnouncement = new Announcement();
        emergencyAnnouncement.setTitle("紧急通知：足球场地变更");
        emergencyAnnouncement.setContent("由于原定足球场地维修原因，2023年秋季足球联赛本周末(11月11日-12日)的比赛场地临时变更为市体育中心南区足球场。请参赛队伍提前做好准备，按照新场地安排前往比赛。");
        emergencyAnnouncement.setType("EMERGENCY");
        emergencyAnnouncement.setStatus(1);
        emergencyAnnouncement.setCreateTime(LocalDateTime.now().minusHours(12));
        emergencyAnnouncement.setUpdateTime(LocalDateTime.now().minusHours(12));
        announcementRepository.save(emergencyAnnouncement);
        
        System.out.println("公告数据初始化完成");
    }
} 