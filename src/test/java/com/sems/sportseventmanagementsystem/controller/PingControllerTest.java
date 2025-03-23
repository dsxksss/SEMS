package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * PingController的单元测试，不依赖Spring上下文
 */
public class PingControllerTest {
    
    private PingController pingController;
    
    @BeforeEach
    public void setUp() {
        // 手动创建控制器实例
        pingController = new PingController();
    }
    
    @Test
    public void testPing() {
        // 直接调用控制器方法
        Result<?> result = pingController.ping();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("pong", result.getData());
    }
} 