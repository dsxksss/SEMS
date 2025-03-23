package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.mapper.EventMapper;
import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event mockEvent;
    private EventDTO mockEventDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
    void createEvent() {
        when(eventMapper.insert(any(Event.class))).thenReturn(1);

        Event result = eventService.createEvent(mockEventDTO);

        assertNotNull(result);
        assertEquals(mockEventDTO.getName(), result.getName());
        assertEquals(mockEventDTO.getDescription(), result.getDescription());
        assertEquals(0, result.getCurrentParticipants());
        verify(eventMapper, times(1)).insert(any(Event.class));
    }

    @Test
    void updateEvent() {
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);
        when(eventMapper.update(any(Event.class))).thenReturn(1);

        Event result = eventService.updateEvent(1L, mockEventDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(mockEventDTO.getName(), result.getName());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(eventMapper, times(1)).update(any(Event.class));
    }

    @Test
    void updateEventNotFound() {
        when(eventMapper.selectById(anyLong())).thenReturn(null);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.updateEvent(1L, mockEventDTO);
        });

        assertEquals("赛事不存在，ID: 1", exception.getMessage());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(eventMapper, never()).update(any(Event.class));
    }

    @Test
    void deleteEvent() {
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);
        when(eventMapper.delete(anyLong())).thenReturn(1);

        boolean result = eventService.deleteEvent(1L);

        assertTrue(result);
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(eventMapper, times(1)).delete(anyLong());
    }

    @Test
    void deleteEventNotFound() {
        when(eventMapper.selectById(anyLong())).thenReturn(null);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.deleteEvent(1L);
        });

        assertEquals("赛事不存在，ID: 1", exception.getMessage());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(eventMapper, never()).delete(anyLong());
    }

    @Test
    void getEventById() {
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);

        Event result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试赛事", result.getName());
        verify(eventMapper, times(1)).selectById(anyLong());
    }

    @Test
    void getAllEvents() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventMapper.selectAll()).thenReturn(events);

        List<Event> result = eventService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventMapper, times(1)).selectAll();
    }

    @Test
    void getEventsByPage() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventMapper.selectByPage(anyInt(), anyInt())).thenReturn(events);

        List<Event> result = eventService.getEventsByPage(1, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventMapper, times(1)).selectByPage(anyInt(), anyInt());
    }

    @Test
    void getEventsByStatus() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventMapper.selectByStatus(anyString())).thenReturn(events);

        List<Event> result = eventService.getEventsByStatus("OPEN");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventMapper, times(1)).selectByStatus(anyString());
    }

    @Test
    void getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventMapper.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(events);

        List<Event> result = eventService.getUpcomingEvents(7);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventMapper, times(1)).selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }
} 