package com.sems.sportseventmanagementsystem;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

/**
 * 测试配置类，用于简化测试上下文的加载
 */
@TestConfiguration
@EnableAutoConfiguration
@ComponentScan("com.sems.sportseventmanagementsystem")
@ActiveProfiles("test")
public class TestConfig {
} 