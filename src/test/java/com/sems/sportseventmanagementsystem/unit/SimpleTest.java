package com.sems.sportseventmanagementsystem.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 最简单的测试类，验证JUnit框架是否正常工作
 */
public class SimpleTest {
    
    @Test
    @DisplayName("测试基本断言")
    public void testBasicAssertions() {
        // 简单的基本断言
        assertTrue(true, "true应该是true");
        assertEquals(4, 2 + 2, "2加2应该等于4");
        assertEquals("hello", "hello", "相同的字符串应该相等");
    }
    
    @Test
    @DisplayName("测试字符串操作")
    public void testStringOperations() {
        // 字符串操作测试
        String str = "Hello, World";
        assertEquals(12, str.length(), "字符串长度应该是12");
        assertTrue(str.contains("World"), "字符串应该包含'World'");
        assertEquals("HELLO, WORLD", str.toUpperCase(), "转大写应该正确");
    }
    
    @Test
    @DisplayName("测试数学计算")
    public void testMathOperations() {
        // 数学运算测试
        assertEquals(4, Math.pow(2, 2), "2的平方应该是4");
        assertEquals(3, Math.sqrt(9), "9的平方根应该是3");
        assertEquals(5, Math.max(3, 5), "3和5中的最大值应该是5");
    }
} 