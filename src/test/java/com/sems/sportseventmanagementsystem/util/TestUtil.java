package com.sems.sportseventmanagementsystem.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 测试工具类
 */
public class TestUtil {

    /**
     * 使用管理员账号登录并获取JWT令牌
     */
    public static String getAdminToken(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        String loginJson = "{\"username\":\"admin\",\"password\":\"password\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        return objectMapper.readTree(content).path("data").path("token").asText();
    }

    /**
     * 为请求添加认证头
     */
    public static MockHttpServletRequestBuilder addAuth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }
} 