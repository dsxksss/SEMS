package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

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

        // 创建模拟事件DTO
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
    void getEventById() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));

        Event result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试赛事", result.getName());
        verify(eventRepository, times(1)).findById(anyLong());
    }

    @Test
    void getEventByIdNotFound() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.getEventById(1L);
        });

        assertTrue(exception.getMessage().contains("赛事不存在"));
    }

    @Test
    void createEvent() {
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);

        Event result = eventService.createEvent(mockEventDTO);

        assertNotNull(result);
        assertEquals("测试赛事", result.getName());
        assertEquals("这是一个测试赛事", result.getDescription());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateEvent() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setName("更新的赛事名称");
        updateDTO.setDescription("更新的赛事描述");

        Event result = eventService.updateEvent(1L, updateDTO);

        assertNotNull(result);
        assertEquals("更新的赛事名称", result.getName());
        assertEquals("更新的赛事描述", result.getDescription());
        verify(eventRepository, times(1)).findById(anyLong());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateEventNotFound() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.updateEvent(1L, mockEventDTO);
        });

        assertTrue(exception.getMessage().contains("赛事不存在"));
    }

    @Test
    void deleteEvent() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        doNothing().when(eventRepository).deleteById(anyLong());

        boolean result = eventService.deleteEvent(1L);

        assertTrue(result);
        verify(eventRepository, times(1)).findById(anyLong());
        verify(eventRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteEventNotFound() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.deleteEvent(1L);
        });

        assertTrue(exception.getMessage().contains("赛事不存在"));
    }

    @Test
    void getAllEvents() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getEventsByPage() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventRepository.findByPage(any(Pageable.class))).thenReturn(events);

        List<Event> result = eventService.getEventsByPage(1, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventRepository, times(1)).findByPage(any(Pageable.class));
    }

    @Test
    void getEventsByStatus() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventRepository.findByStatus(anyString())).thenReturn(events);

        List<Event> result = eventService.getEventsByStatus("OPEN");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventRepository, times(1)).findByStatus(anyString());
    }

    @Test
    void getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        events.add(mockEvent);
        when(eventRepository.findByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(events);

        List<Event> result = eventService.getUpcomingEvents(7);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试赛事", result.get(0).getName());
        verify(eventRepository, times(1)).findByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }
} 