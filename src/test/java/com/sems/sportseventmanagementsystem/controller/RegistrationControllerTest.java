package com.sems.sportseventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sems.sportseventmanagementsystem.config.JpaTestConfig;
import com.sems.sportseventmanagementsystem.config.TestSecurityConfig;
import com.sems.sportseventmanagementsystem.config.TestUtilsConfig;
import com.sems.sportseventmanagementsystem.model.dto.RegistrationDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.model.entity.Registration;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.security.UserDetailsImpl;
import com.sems.sportseventmanagementsystem.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, TestUtilsConfig.class, JpaTestConfig.class})
@ActiveProfiles("test")
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    private Registration mockRegistration;
    private RegistrationDTO mockRegistrationDTO;
    private Event mockEvent;
    private User mockUser;
    private UserDetailsImpl mockUserDetails;

    @BeforeEach
    void setUp() {
        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setRole("USER");

        // 创建UserDetails
        mockUserDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "encoded_password",
                "test@example.com",
                "测试用户",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // 创建模拟赛事
        mockEvent = new Event();
        mockEvent.setId(1L);
        mockEvent.setName("测试赛事");
        mockEvent.setStartTime(LocalDateTime.now().plusDays(1));
        mockEvent.setEndTime(LocalDateTime.now().plusDays(2));

        // 创建模拟报名DTO
        mockRegistrationDTO = new RegistrationDTO();
        mockRegistrationDTO.setEventId(1L);
        mockRegistrationDTO.setRegistrationType("INDIVIDUAL");
        mockRegistrationDTO.setRemark("测试备注");

        // 创建模拟报名记录
        mockRegistration = new Registration();
        mockRegistration.setId(1L);
        mockRegistration.setUserId(1L);
        mockRegistration.setEventId(1L);
        mockRegistration.setRegistrationType("INDIVIDUAL");
        mockRegistration.setStatus("PENDING");
        mockRegistration.setRegisterTime(LocalDateTime.now());
        mockRegistration.setUpdateTime(LocalDateTime.now());
        mockRegistration.setRemark("测试备注");
        mockRegistration.setEvent(mockEvent);
        mockRegistration.setUser(mockUser);
    }

    @Test
    void register() throws Exception {
        when(registrationService.register(anyLong(), any(RegistrationDTO.class))).thenReturn(mockRegistration);

        mockMvc.perform(post("/api/registrations")
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRegistrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.eventId").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void cancelRegistration() throws Exception {
        when(registrationService.cancelRegistration(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/registrations/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reviewRegistration() throws Exception {
        when(registrationService.reviewRegistration(anyLong(), anyString(), anyString())).thenReturn(mockRegistration);

        mockMvc.perform(put("/api/registrations/1/review")
                        .param("status", "APPROVED")
                        .param("remark", "审核通过"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.eventId").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reviewRegistrationWithoutAdminRole() throws Exception {
        mockMvc.perform(put("/api/registrations/1/review")
                        .param("status", "APPROVED")
                        .param("remark", "审核通过"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyRegistrations() throws Exception {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationService.getUserRegistrations(anyLong())).thenReturn(registrations);

        mockMvc.perform(get("/api/registrations/my")
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].eventId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEventRegistrations() throws Exception {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationService.getEventRegistrations(anyLong())).thenReturn(registrations);

        mockMvc.perform(get("/api/registrations/event/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].eventId").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getEventRegistrationsWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/registrations/event/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRegistrationById() throws Exception {
        when(registrationService.getRegistrationById(anyLong())).thenReturn(mockRegistration);

        mockMvc.perform(get("/api/registrations/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.eventId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRegistrationsByStatus() throws Exception {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationService.getRegistrationsByStatus(anyString())).thenReturn(registrations);

        mockMvc.perform(get("/api/registrations/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].eventId").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRegistrationsByStatusWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/registrations/status/PENDING"))
                .andExpect(status().isForbidden());
    }
} 