package com.sems.sportseventmanagementsystem.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 赛事和报名流程的模拟测试，模拟整个流程的交互
 */
public class EventRegistrationMockTest {

    // 模拟实体类
    private static class Event {
        private Long id;
        private String name;
        private String description;
        private String status;
        private Integer maxParticipants;
        private Integer currentParticipants = 0;

        public Event(Long id, String name, String description, Integer maxParticipants) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.status = "未开始";
            this.maxParticipants = maxParticipants;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public Integer getMaxParticipants() { return maxParticipants; }
        public Integer getCurrentParticipants() { return currentParticipants; }

        public void setStatus(String status) { this.status = status; }
        public void incrementParticipants() { this.currentParticipants++; }
    }

    private static class Registration {
        private Long id;
        private Long eventId;
        private Long userId;
        private String status;

        public Registration(Long id, Long eventId, Long userId) {
            this.id = id;
            this.eventId = eventId;
            this.userId = userId;
            this.status = "待审核";
        }

        public Long getId() { return id; }
        public Long getEventId() { return eventId; }
        public Long getUserId() { return userId; }
        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }
    }

    private static class User {
        private Long id;
        private String username;
        private String email;

        public User(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }

    // 模拟数据存储
    private static class EventRepository {
        private Map<Long, Event> events = new HashMap<>();

        public void save(Event event) {
            events.put(event.getId(), event);
        }

        public Event findById(Long id) {
            return events.get(id);
        }

        public List<Event> findAll() {
            return new ArrayList<>(events.values());
        }

        public List<Event> findByStatus(String status) {
            List<Event> result = new ArrayList<>();
            for (Event event : events.values()) {
                if (event.getStatus().equals(status)) {
                    result.add(event);
                }
            }
            return result;
        }
    }

    private static class RegistrationRepository {
        private Map<Long, Registration> registrations = new HashMap<>();
        private long nextId = 1;

        public Registration save(Registration registration) {
            if (registration.getId() == null) {
                registration = new Registration(nextId++, registration.getEventId(), registration.getUserId());
            }
            registrations.put(registration.getId(), registration);
            return registration;
        }

        public Registration findById(Long id) {
            return registrations.get(id);
        }

        public List<Registration> findByEventId(Long eventId) {
            List<Registration> result = new ArrayList<>();
            for (Registration registration : registrations.values()) {
                if (registration.getEventId().equals(eventId)) {
                    result.add(registration);
                }
            }
            return result;
        }

        public List<Registration> findByUserId(Long userId) {
            List<Registration> result = new ArrayList<>();
            for (Registration registration : registrations.values()) {
                if (registration.getUserId().equals(userId)) {
                    result.add(registration);
                }
            }
            return result;
        }

        public Registration findByEventIdAndUserId(Long eventId, Long userId) {
            for (Registration registration : registrations.values()) {
                if (registration.getEventId().equals(eventId) && registration.getUserId().equals(userId)) {
                    return registration;
                }
            }
            return null;
        }
    }

    private static class UserRepository {
        private Map<Long, User> users = new HashMap<>();

        public void save(User user) {
            users.put(user.getId(), user);
        }

        public User findById(Long id) {
            return users.get(id);
        }
    }

    // 服务类
    private static class EventService {
        private EventRepository eventRepository;
        private RegistrationRepository registrationRepository;

        public EventService(EventRepository eventRepository, RegistrationRepository registrationRepository) {
            this.eventRepository = eventRepository;
            this.registrationRepository = registrationRepository;
        }

        public Event createEvent(Event event) {
            eventRepository.save(event);
            return event;
        }

        public Event updateEventStatus(Long eventId, String status) throws Exception {
            Event event = eventRepository.findById(eventId);
            if (event == null) {
                throw new Exception("赛事不存在");
            }

            // 验证状态变更的有效性
            if ("已结束".equals(status) && !"进行中".equals(event.getStatus())) {
                throw new Exception("只有进行中的赛事才能结束");
            }

            event.setStatus(status);
            eventRepository.save(event);
            return event;
        }

        public List<Event> findEventsByStatus(String status) {
            return eventRepository.findByStatus(status);
        }

        public Event findEventById(Long id) {
            return eventRepository.findById(id);
        }
    }

    private static class RegistrationService {
        private RegistrationRepository registrationRepository;
        private EventRepository eventRepository;
        private UserRepository userRepository;

        public RegistrationService(RegistrationRepository registrationRepository, 
                                 EventRepository eventRepository,
                                 UserRepository userRepository) {
            this.registrationRepository = registrationRepository;
            this.eventRepository = eventRepository;
            this.userRepository = userRepository;
        }

        public Registration register(Long eventId, Long userId) throws Exception {
            // 验证赛事是否存在
            Event event = eventRepository.findById(eventId);
            if (event == null) {
                throw new Exception("赛事不存在");
            }

            // 验证用户是否存在
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new Exception("用户不存在");
            }

            // 验证赛事是否可以报名
            if (!"未开始".equals(event.getStatus())) {
                throw new Exception("只有未开始的赛事才能报名");
            }

            // 验证赛事是否已满
            if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
                throw new Exception("赛事报名人数已满");
            }

            // 验证用户是否已报名
            Registration existingRegistration = registrationRepository.findByEventIdAndUserId(eventId, userId);
            if (existingRegistration != null) {
                throw new Exception("用户已经报名该赛事");
            }

            // 创建报名记录
            Registration registration = new Registration(null, eventId, userId);
            registration = registrationRepository.save(registration);

            // 更新赛事参与人数
            event.incrementParticipants();
            eventRepository.save(event);

            return registration;
        }

        public Registration approveRegistration(Long registrationId) throws Exception {
            Registration registration = registrationRepository.findById(registrationId);
            if (registration == null) {
                throw new Exception("报名记录不存在");
            }

            if (!"待审核".equals(registration.getStatus())) {
                throw new Exception("只有待审核的报名才能被审核通过");
            }

            registration.setStatus("已通过");
            registrationRepository.save(registration);

            return registration;
        }

        public Registration rejectRegistration(Long registrationId) throws Exception {
            Registration registration = registrationRepository.findById(registrationId);
            if (registration == null) {
                throw new Exception("报名记录不存在");
            }

            if (!"待审核".equals(registration.getStatus())) {
                throw new Exception("只有待审核的报名才能被拒绝");
            }

            registration.setStatus("已拒绝");
            registrationRepository.save(registration);

            // 减少赛事报名人数
            Event event = eventRepository.findById(registration.getEventId());
            if (event != null) {
                event.setStatus(event.getStatus()); // 触发更新但不改变状态
                eventRepository.save(event);
            }

            return registration;
        }

        public List<Registration> findRegistrationsByEventId(Long eventId) {
            return registrationRepository.findByEventId(eventId);
        }

        public List<Registration> findRegistrationsByUserId(Long userId) {
            return registrationRepository.findByUserId(userId);
        }
    }

    // 测试变量
    private EventRepository eventRepository;
    private RegistrationRepository registrationRepository;
    private UserRepository userRepository;
    private EventService eventService;
    private RegistrationService registrationService;
    private User testUser1;
    private User testUser2;
    private Event testEvent;

    @BeforeEach
    public void setUp() {
        // 初始化存储库
        eventRepository = new EventRepository();
        registrationRepository = new RegistrationRepository();
        userRepository = new UserRepository();

        // 初始化服务
        eventService = new EventService(eventRepository, registrationRepository);
        registrationService = new RegistrationService(registrationRepository, eventRepository, userRepository);

        // 创建测试用户
        testUser1 = new User(1L, "user1", "user1@example.com");
        testUser2 = new User(2L, "user2", "user2@example.com");
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // 创建测试赛事
        testEvent = new Event(1L, "测试赛事", "这是一个测试赛事", 10);
        eventRepository.save(testEvent);
    }

    @Test
    @DisplayName("测试创建赛事")
    public void testCreateEvent() {
        // 创建新赛事
        Event newEvent = new Event(2L, "新赛事", "这是一个新创建的赛事", 20);
        Event savedEvent = eventService.createEvent(newEvent);

        // 验证赛事保存成功
        assertNotNull(savedEvent);
        assertEquals("新赛事", savedEvent.getName());
        assertEquals("未开始", savedEvent.getStatus());

        // 验证可以从数据库检索到该赛事
        Event retrievedEvent = eventService.findEventById(2L);
        assertNotNull(retrievedEvent);
        assertEquals("新赛事", retrievedEvent.getName());
    }

    @Test
    @DisplayName("测试用户报名赛事")
    public void testRegisterForEvent() throws Exception {
        // 用户报名赛事
        Registration registration = registrationService.register(testEvent.getId(), testUser1.getId());

        // 验证报名成功
        assertNotNull(registration);
        assertEquals(testEvent.getId(), registration.getEventId());
        assertEquals(testUser1.getId(), registration.getUserId());
        assertEquals("待审核", registration.getStatus());

        // 验证赛事参与人数增加
        Event updatedEvent = eventService.findEventById(testEvent.getId());
        assertEquals(1, updatedEvent.getCurrentParticipants());

        // 验证用户的报名记录可以被检索
        List<Registration> userRegistrations = registrationService.findRegistrationsByUserId(testUser1.getId());
        assertEquals(1, userRegistrations.size());
        assertEquals(testEvent.getId(), userRegistrations.get(0).getEventId());
    }

    @Test
    @DisplayName("测试审核通过报名")
    public void testApproveRegistration() throws Exception {
        // 用户报名赛事
        Registration registration = registrationService.register(testEvent.getId(), testUser1.getId());

        // 审核通过报名
        Registration approvedRegistration = registrationService.approveRegistration(registration.getId());

        // 验证报名状态已更新
        assertEquals("已通过", approvedRegistration.getStatus());

        // 验证从数据库检索的报名状态也已更新
        Registration retrievedRegistration = registrationRepository.findById(registration.getId());
        assertEquals("已通过", retrievedRegistration.getStatus());
    }

    @Test
    @DisplayName("测试拒绝报名申请")
    public void testRejectRegistration() throws Exception {
        // 用户报名赛事
        Registration registration = registrationService.register(testEvent.getId(), testUser1.getId());

        // 拒绝报名申请
        Registration rejectedRegistration = registrationService.rejectRegistration(registration.getId());

        // 验证报名状态已更新
        assertEquals("已拒绝", rejectedRegistration.getStatus());

        // 验证从数据库检索的报名状态也已更新
        Registration retrievedRegistration = registrationRepository.findById(registration.getId());
        assertEquals("已拒绝", retrievedRegistration.getStatus());
    }

    @Test
    @DisplayName("测试赛事状态更新流程")
    public void testEventStatusUpdateWorkflow() throws Exception {
        // 初始状态验证
        assertEquals("未开始", testEvent.getStatus());

        // 用户报名赛事
        registrationService.register(testEvent.getId(), testUser1.getId());
        registrationService.register(testEvent.getId(), testUser2.getId());

        // 更新赛事状态为进行中
        Event updatedEvent = eventService.updateEventStatus(testEvent.getId(), "进行中");
        assertEquals("进行中", updatedEvent.getStatus());

        // 结束赛事
        Event endedEvent = eventService.updateEventStatus(testEvent.getId(), "已结束");
        assertEquals("已结束", endedEvent.getStatus());
    }

    @Test
    @DisplayName("测试无法报名已开始的赛事")
    public void testCannotRegisterStartedEvent() throws Exception {
        // 创建一个新赛事
        Event newEvent = new Event(100L, "已开始赛事", "这是一个已开始的赛事", 10);
        eventService.createEvent(newEvent);
        
        // 将赛事状态设为进行中
        eventService.updateEventStatus(100L, "进行中");
        
        // 创建一个测试用户
        User testUser3 = new User(3L, "user3", "user3@example.com");
        userRepository.save(testUser3);
        
        // 尝试报名一个进行中的赛事，应该抛出异常
        Exception exception = assertThrows(Exception.class, () -> {
            registrationService.register(100L, 3L);
        });
        assertEquals("只有未开始的赛事才能报名", exception.getMessage());
    }

    @Test
    @DisplayName("测试赛事满员情况")
    public void testEventFullCapacity() throws Exception {
        // 创建一个只允许1人参加的赛事
        Event smallEvent = new Event(3L, "小型赛事", "这是一个只允许1人参加的赛事", 1);
        eventService.createEvent(smallEvent);

        // 第一个用户报名，应该成功
        Registration registration1 = registrationService.register(smallEvent.getId(), testUser1.getId());
        assertNotNull(registration1);

        // 第二个用户报名，应该失败
        Exception exception = assertThrows(Exception.class, () -> {
            registrationService.register(smallEvent.getId(), testUser2.getId());
        });
        assertEquals("赛事报名人数已满", exception.getMessage());
    }

    @Test
    @DisplayName("测试重复报名检测")
    public void testDuplicateRegistration() throws Exception {
        // 用户首次报名
        Registration registration = registrationService.register(testEvent.getId(), testUser1.getId());
        assertNotNull(registration);

        // 同一用户再次报名，应该失败
        Exception exception = assertThrows(Exception.class, () -> {
            registrationService.register(testEvent.getId(), testUser1.getId());
        });
        assertEquals("用户已经报名该赛事", exception.getMessage());
    }
} 