package com.sems.sportseventmanagementsystem.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 简单的流程测试示例，不依赖Spring上下文
 * 模拟体育赛事管理系统的基本流程
 */
@TestMethodOrder(OrderAnnotation.class)
public class WorkflowTest {
    
    // 模拟实体类
    private static class Event {
        private Long id;
        private String name;
        private String description;
        private String status;
        
        public Event(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.status = "未开始";
        }
        
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        
        public void setStatus(String status) { this.status = status; }
    }
    
    private static class Registration {
        private Long id;
        private Long eventId;
        private String participantName;
        private boolean confirmed;
        
        public Registration(Long id, Long eventId, String participantName) {
            this.id = id;
            this.eventId = eventId;
            this.participantName = participantName;
            this.confirmed = false;
        }
        
        public Long getId() { return id; }
        public Long getEventId() { return eventId; }
        public String getParticipantName() { return participantName; }
        public boolean isConfirmed() { return confirmed; }
        
        public void confirm() { this.confirmed = true; }
    }
    
    // 测试数据
    private Event testEvent;
    private Registration testRegistration;
    
    @BeforeEach
    public void setUp() {
        // 每个测试方法运行前初始化测试数据
        testEvent = null;
        testRegistration = null;
    }
    
    @Test
    @Order(1)
    @DisplayName("第1步：创建赛事")
    public void testCreateEvent() {
        // 创建赛事
        testEvent = new Event(1L, "春季校园运动会", "一年一度的校园运动会");
        
        // 验证赛事创建成功
        assertNotNull(testEvent);
        assertEquals(1L, testEvent.getId());
        assertEquals("春季校园运动会", testEvent.getName());
        assertEquals("未开始", testEvent.getStatus());
        
        // 为下一个测试存储数据
        WorkflowTestData.setEvent(testEvent);
    }
    
    @Test
    @Order(2)
    @DisplayName("第2步：报名参加赛事")
    public void testRegisterForEvent() {
        // 获取上一步创建的赛事
        testEvent = WorkflowTestData.getEvent();
        assertNotNull(testEvent, "赛事应该已在上一步创建");
        
        // 创建报名记录
        testRegistration = new Registration(1L, testEvent.getId(), "张三");
        
        // 验证报名记录创建成功
        assertNotNull(testRegistration);
        assertEquals(testEvent.getId(), testRegistration.getEventId());
        assertEquals("张三", testRegistration.getParticipantName());
        assertEquals(false, testRegistration.isConfirmed());
        
        // 为下一个测试存储数据
        WorkflowTestData.setRegistration(testRegistration);
    }
    
    @Test
    @Order(3)
    @DisplayName("第3步：确认报名")
    public void testConfirmRegistration() {
        // 获取上一步创建的报名记录
        testRegistration = WorkflowTestData.getRegistration();
        assertNotNull(testRegistration, "报名记录应该已在上一步创建");
        
        // 确认报名
        testRegistration.confirm();
        
        // 验证报名已确认
        assertTrue(testRegistration.isConfirmed());
        
        // 更新存储的数据
        WorkflowTestData.setRegistration(testRegistration);
    }
    
    @Test
    @Order(4)
    @DisplayName("第4步：开始赛事")
    public void testStartEvent() {
        // 获取之前创建的赛事
        testEvent = WorkflowTestData.getEvent();
        assertNotNull(testEvent, "赛事应该已在第一步创建");
        
        // 获取确认的报名记录
        testRegistration = WorkflowTestData.getRegistration();
        assertNotNull(testRegistration, "报名记录应该已在前面步骤创建并确认");
        assertTrue(testRegistration.isConfirmed(), "报名应该已确认");
        
        // 开始赛事
        testEvent.setStatus("进行中");
        
        // 验证赛事状态已更新
        assertEquals("进行中", testEvent.getStatus());
        
        // 更新存储的数据
        WorkflowTestData.setEvent(testEvent);
    }
    
    @Test
    @Order(5)
    @DisplayName("第5步：结束赛事")
    public void testEndEvent() {
        // 获取之前创建的赛事
        testEvent = WorkflowTestData.getEvent();
        assertNotNull(testEvent, "赛事应该已在前面步骤创建并开始");
        assertEquals("进行中", testEvent.getStatus(), "赛事应该处于进行中状态");
        
        // 结束赛事
        testEvent.setStatus("已结束");
        
        // 验证赛事状态已更新
        assertEquals("已结束", testEvent.getStatus());
    }
    
    /**
     * 用于在测试方法之间共享数据的辅助类
     */
    private static class WorkflowTestData {
        private static Event event;
        private static Registration registration;
        
        public static void setEvent(Event e) {
            event = e;
        }
        
        public static Event getEvent() {
            return event;
        }
        
        public static void setRegistration(Registration r) {
            registration = r;
        }
        
        public static Registration getRegistration() {
            return registration;
        }
    }
} 