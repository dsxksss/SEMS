package com.sems.sportseventmanagementsystem.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成流程测试示例
 * 此测试演示如何测试用户-赛事-报名的完整流程
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationFlowTest {

    // 简单用户类
    private static class User {
        private final Long id;
        private final String name;
        private final String email;
        private boolean active;

        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.active = true;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    // 简单赛事类
    private static class Event {
        private final Long id;
        private final String name;
        private String status;
        private int capacity;
        private int registeredCount;

        public Event(Long id, String name, int capacity) {
            this.id = id;
            this.name = name;
            this.status = "准备中";
            this.capacity = capacity;
            this.registeredCount = 0;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public int getCapacity() { return capacity; }
        public int getRegisteredCount() { return registeredCount; }

        public void setStatus(String status) { this.status = status; }
        public void increaseRegisteredCount() { this.registeredCount++; }
        public boolean isFull() { return registeredCount >= capacity; }
    }

    // 简单报名类
    private static class Registration {
        private final Long userId;
        private final Long eventId;
        private String status;

        public Registration(Long userId, Long eventId) {
            this.userId = userId;
            this.eventId = eventId;
            this.status = "待审核";
        }

        public Long getUserId() { return userId; }
        public Long getEventId() { return eventId; }
        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }
    }

    // 测试数据
    private static User user1;
    private static User user2;
    private static Event event;
    private static Registration registration1;
    private static Registration registration2;

    @BeforeEach
    void setUp() {
        // 每次测试前不重置状态，这样测试可以按顺序共享状态
    }

    @Test
    @Order(1)
    @DisplayName("1. 创建用户")
    void testCreateUsers() {
        // 创建两个用户
        user1 = new User(1L, "张三", "zhangsan@example.com");
        user2 = new User(2L, "李四", "lisi@example.com");

        // 验证用户创建成功
        assertNotNull(user1);
        assertNotNull(user2);
        assertEquals("张三", user1.getName());
        assertEquals("李四", user2.getName());
        assertTrue(user1.isActive());
        assertTrue(user2.isActive());
    }

    @Test
    @Order(2)
    @DisplayName("2. 创建赛事")
    void testCreateEvent() {
        // 检查用户是否已创建
        assertNotNull(user1, "用户1应该已创建");
        assertNotNull(user2, "用户2应该已创建");

        // 创建赛事
        event = new Event(1L, "春季马拉松", 2);

        // 验证赛事创建成功
        assertNotNull(event);
        assertEquals("春季马拉松", event.getName());
        assertEquals("准备中", event.getStatus());
        assertEquals(2, event.getCapacity());
        assertEquals(0, event.getRegisteredCount());
    }

    @Test
    @Order(3)
    @DisplayName("3. 用户报名赛事")
    void testRegisterForEvent() {
        // 检查用户和赛事是否已创建
        assertNotNull(user1, "用户1应该已创建");
        assertNotNull(user2, "用户2应该已创建");
        assertNotNull(event, "赛事应该已创建");

        // 用户1报名
        registration1 = new Registration(user1.getId(), event.getId());
        event.increaseRegisteredCount();

        // 验证报名1成功
        assertNotNull(registration1);
        assertEquals(user1.getId(), registration1.getUserId());
        assertEquals(event.getId(), registration1.getEventId());
        assertEquals("待审核", registration1.getStatus());
        assertEquals(1, event.getRegisteredCount());

        // 用户2报名
        registration2 = new Registration(user2.getId(), event.getId());
        event.increaseRegisteredCount();

        // 验证报名2成功
        assertNotNull(registration2);
        assertEquals(user2.getId(), registration2.getUserId());
        assertEquals(event.getId(), registration2.getEventId());
        assertEquals("待审核", registration2.getStatus());
        assertEquals(2, event.getRegisteredCount());

        // 验证赛事已满
        assertTrue(event.isFull());
    }

    @Test
    @Order(4)
    @DisplayName("4. 审核报名")
    void testReviewRegistrations() {
        // 检查报名记录是否已创建
        assertNotNull(registration1, "报名1应该已创建");
        assertNotNull(registration2, "报名2应该已创建");

        // 审核通过用户1的报名
        registration1.setStatus("已通过");

        // 验证报名1状态已更新
        assertEquals("已通过", registration1.getStatus());

        // 拒绝用户2的报名
        registration2.setStatus("已拒绝");

        // 验证报名2状态已更新
        assertEquals("已拒绝", registration2.getStatus());
    }

    @Test
    @Order(5)
    @DisplayName("5. 开始赛事")
    void testStartEvent() {
        // 检查赛事是否已创建
        assertNotNull(event, "赛事应该已创建");

        // 开始赛事
        event.setStatus("进行中");

        // 验证赛事状态已更新
        assertEquals("进行中", event.getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("6. 结束赛事")
    void testEndEvent() {
        // 检查赛事是否已开始
        assertNotNull(event, "赛事应该已创建");
        assertEquals("进行中", event.getStatus(), "赛事应该处于进行中状态");

        // 结束赛事
        event.setStatus("已结束");

        // 验证赛事状态已更新
        assertEquals("已结束", event.getStatus());
    }

    @Test
    @Order(7)
    @DisplayName("7. 禁用用户")
    void testDeactivateUser() {
        // 检查用户是否已创建
        assertNotNull(user1, "用户1应该已创建");

        // 禁用用户1
        user1.setActive(false);

        // 验证用户1状态已更新
        assertFalse(user1.isActive());
    }
} 