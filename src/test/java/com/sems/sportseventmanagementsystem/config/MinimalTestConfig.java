package com.sems.sportseventmanagementsystem.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 最小化测试配置，仅提供必要的安全配置
 */
@TestConfiguration
public class MinimalTestConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        // 禁用所有安全限制
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
    
    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        // 创建一个简单的身份验证管理器，对测试来说总是通过验证
        return new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                // 不做任何验证，直接返回传入的认证对象
                authentication.setAuthenticated(true);
                return authentication;
            }
        };
    }
} 