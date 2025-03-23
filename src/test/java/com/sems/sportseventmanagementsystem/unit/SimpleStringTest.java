package com.sems.sportseventmanagementsystem.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 一个极简的测试类，只包含纯Java代码测试
 */
public class SimpleStringTest {

    @Test
    public void testStringEquals() {
        String s1 = "Hello";
        String s2 = "Hello";
        Assertions.assertEquals(s1, s2);
    }

    @Test
    public void testStringConcat() {
        String s1 = "Hello";
        String s2 = " World";
        String result = s1 + s2;
        Assertions.assertEquals("Hello World", result);
    }
} 