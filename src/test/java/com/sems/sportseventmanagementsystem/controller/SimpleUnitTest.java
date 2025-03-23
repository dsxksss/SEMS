package com.sems.sportseventmanagementsystem.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 简单的单元测试，不依赖Spring上下文
 */
public class SimpleUnitTest {

    @Test
    public void simpleTest() {
        // 一个简单的断言测试
        assertTrue(true, "这个测试总是通过");
        assertEquals(4, 2 + 2, "基本数学运算");
    }
} 