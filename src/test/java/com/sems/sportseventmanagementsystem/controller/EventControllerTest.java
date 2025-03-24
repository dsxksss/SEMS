package com.sems.sportseventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sems.sportseventmanagementsystem.config.JpaTestConfig;
import com.sems.sportseventmanagementsystem.config.TestSecurityConfig;
import com.sems.sportseventmanagementsystem.config.TestUtilsConfig;
import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, TestUtilsConfig.class, JpaTestConfig.class})
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private Event mockEvent;
    private EventDTO mockEventDTO;

    @BeforeEach
    void setUp() {
        // 创建模拟事件
        mockEvent = new Event();
        mockEvent.setId(1L);
        mockEvent.setName("测试赛事");
        mockEvent.setDescription("这是一个测试赛事");
        mockEvent.setStartTime(LocalDateTime.now().plusDays(1));
        mockEvent.setEndTime(LocalDateTime.now().plusDays(2));
        mockEvent.setLocation("测试地点");
        mockEvent.setStatus("OPEN");
        mockEvent.setMaxParticipants(100);
        mockEvent.setCurrentParticipants(0);
        mockEvent.setCreateTime(LocalDateTime.now());
        mockEvent.setUpdateTime(LocalDateTime.now());

        // 创建模拟DTO
        mockEventDTO = new EventDTO();
        mockEventDTO.setName("测试赛事");
        mockEventDTO.setDescription("这是一个测试赛事");
        mockEventDTO.setStartTime(LocalDateTime.now().plusDays(1));
        mockEventDTO.setEndTime(LocalDateTime.now().plusDays(2));
        mockEventDTO.setLocation("测试地点");
        mockEventDTO.setStatus("OPEN");
        mockEventDTO.setMaxParticipants(100);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEvent() throws Exception {
        when(eventService.createEvent(any(EventDTO.class))).thenReturn(mockEvent);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockEventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试赛事"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createEventWithoutAdminRole() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockEventDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEvent() throws Exception {
        when(eventService.updateEvent(anyLong(), any(EventDTO.class))).thenReturn(mockEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockEventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试赛事"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEvent() throws Exception {
        when(eventService.deleteEvent(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getEventById() throws Exception {
        when(eventService.getEventById(anyLong())).thenReturn(mockEvent);

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试赛事"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllEvents() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试赛事"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getEventsByPage() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventService.getEventsByPage(1, 10)).thenReturn(events);

        mockMvc.perform(get("/api/events/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试赛事"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getEventsByStatus() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventService.getEventsByStatus("OPEN")).thenReturn(events);

        mockMvc.perform(get("/api/events/status/OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试赛事"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUpcomingEvents() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventService.getUpcomingEvents(7)).thenReturn(events);

        mockMvc.perform(get("/api/events/upcoming")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试赛事"));
    }
} 