package com.sems.sportseventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sems.sportseventmanagementsystem.config.TestSecurityConfig;
import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.security.UserDetailsImpl;
import com.sems.sportseventmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User mockUser;
    private UserDTO mockUserDTO;
    private UserDetailsImpl userDetails;
    private List<User> mockUsers;

    @BeforeEach
    void setUp() {
        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("password");
        mockUser.setEmail("test@example.com");
        mockUser.setRealName("Test User");
        mockUser.setRole("USER");
        mockUser.setStatus(1);
        mockUser.setCreateTime(LocalDateTime.now());
        mockUser.setUpdateTime(LocalDateTime.now());

        // 创建用户DTO
        mockUserDTO = new UserDTO();
        mockUserDTO.setId(1L);
        mockUserDTO.setUsername("testuser");
        mockUserDTO.setEmail("test@example.com");
        mockUserDTO.setRealName("Test User");
        mockUserDTO.setRole("USER");
        mockUserDTO.setStatus(1);
        
        // 创建UserDetails
        userDetails = UserDetailsImpl.build(mockUser);
        
        // 创建用户列表
        mockUsers = new ArrayList<>();
        mockUsers.add(mockUser);
        
        // 设置测试数据和Mock服务响应
        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);
        when(userService.getAllUsers()).thenReturn(mockUsers);
        when(userService.getUsersByRole(anyString())).thenReturn(mockUsers);
        when(userService.getUsersByStatus(anyInt())).thenReturn(mockUsers);
        when(userService.createUser(any(UserDTO.class))).thenReturn(mockUser);
        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(mockUser);
        when(userService.updateUserStatus(eq(1L), anyInt())).thenReturn(mockUser);
        when(userService.deleteUser(1L)).thenReturn(true);
        when(userService.changePassword(anyLong(), anyString(), anyString())).thenReturn(true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser() throws Exception {
        UserDTO newUserDTO = new UserDTO();
        newUserDTO.setUsername("newuser");
        newUserDTO.setPassword("password");
        newUserDTO.setEmail("new@example.com");
        newUserDTO.setRealName("新用户");
        newUserDTO.setRole("USER");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void updateProfile() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");
        updateDTO.setRealName("更新用户");

        mockMvc.perform(put("/api/users/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser() throws Exception {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");
        updateDTO.setRole("ADMIN");
        updateDTO.setStatus(0);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus() throws Exception {
        mockMvc.perform(put("/api/users/1/status/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void changePassword() throws Exception {
        String requestBody = "{\"oldPassword\":\"oldPassword\",\"newPassword\":\"newPassword\"}";

        mockMvc.perform(put("/api/users/me/password")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersByRole() throws Exception {
        mockMvc.perform(get("/api/users/role/USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersByStatus() throws Exception {
        mockMvc.perform(get("/api/users/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value(1));
    }
} 