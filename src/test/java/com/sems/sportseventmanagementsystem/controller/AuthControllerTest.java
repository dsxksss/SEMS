package com.sems.sportseventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sems.sportseventmanagementsystem.config.MyBatisTestConfig;
import com.sems.sportseventmanagementsystem.config.TestSecurityConfig;
import com.sems.sportseventmanagementsystem.config.TestUtilsConfig;
import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.security.JwtResponse;
import com.sems.sportseventmanagementsystem.security.JwtUtils;
import com.sems.sportseventmanagementsystem.security.LoginRequest;
import com.sems.sportseventmanagementsystem.security.UserDetailsImpl;
import com.sems.sportseventmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, TestUtilsConfig.class, MyBatisTestConfig.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

    private LoginRequest loginRequest;
    private UserDTO registerRequest;
    private User mockUser;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // 创建登录请求
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        // 创建注册请求
        registerRequest = new UserDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPhone("13800138000");
        registerRequest.setRealName("新用户");

        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encoded_password");
        mockUser.setEmail("test@example.com");
        mockUser.setPhone("13800138000");
        mockUser.setRealName("测试用户");
        mockUser.setRole("USER");
        mockUser.setStatus(1);
        mockUser.setCreateTime(LocalDateTime.now());
        mockUser.setUpdateTime(LocalDateTime.now());

        // 创建UserDetails
        userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "encoded_password",
                "test@example.com",
                "测试用户",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // 创建认证对象
        authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void login() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class)))
                .thenReturn("mock.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void register() throws Exception {
        when(userService.checkUsernameExists(anyString())).thenReturn(false);
        when(userService.checkEmailExists(anyString())).thenReturn(false);
        when(userService.createUser(any(UserDTO.class))).thenReturn(mockUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class)))
                .thenReturn("mock.jwt.token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void registerWithDuplicateUsername() throws Exception {
        when(userService.checkUsernameExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void registerWithDuplicateEmail() throws Exception {
        when(userService.checkUsernameExists(anyString())).thenReturn(false);
        when(userService.checkEmailExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("邮箱已被注册"));
    }
} 