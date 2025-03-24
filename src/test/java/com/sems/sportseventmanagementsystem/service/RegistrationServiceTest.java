package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.repository.RegistrationRepository;
import com.sems.sportseventmanagementsystem.model.dto.RegistrationDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.model.entity.Registration;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private Registration mockRegistration;
    private RegistrationDTO mockRegistrationDTO;
    private Event mockEvent;
    private Long userId = 1L;
    private Long eventId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 创建模拟事件
        mockEvent = new Event();
        mockEvent.setId(eventId);
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

        // 创建模拟报名DTO
        mockRegistrationDTO = new RegistrationDTO();
        mockRegistrationDTO.setEventId(eventId);
        mockRegistrationDTO.setRemark("测试备注");
        mockRegistrationDTO.setRegistrationType("INDIVIDUAL");

        // 创建模拟报名
        mockRegistration = new Registration();
        mockRegistration.setId(1L);
        mockRegistration.setUserId(userId);
        mockRegistration.setEventId(eventId);
        mockRegistration.setStatus("PENDING");
        mockRegistration.setRegistrationType("INDIVIDUAL");
        mockRegistration.setRegisterTime(LocalDateTime.now());
        mockRegistration.setUpdateTime(LocalDateTime.now());
        mockRegistration.setRemark("测试备注");
    }

    @Test
    void register() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(registrationRepository.findByUserIdAndEventId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(registrationRepository.save(any(Registration.class))).thenReturn(mockRegistration);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);

        Registration result = registrationService.register(userId, mockRegistrationDTO);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(eventId, result.getEventId());
        assertEquals("PENDING", result.getStatus());
        verify(eventRepository, times(1)).findById(anyLong());
        verify(registrationRepository, times(1)).findByUserIdAndEventId(anyLong(), anyLong());
        verify(registrationRepository, times(1)).save(any(Registration.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void registerEventNotFound() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertTrue(exception.getMessage().contains("赛事不存在"));
    }

    @Test
    void registerEventEnded() {
        mockEvent.setEndTime(LocalDateTime.now().minusDays(1));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertTrue(exception.getMessage().contains("赛事已结束"));
    }

    @Test
    void registerEventFull() {
        mockEvent.setMaxParticipants(100);
        mockEvent.setCurrentParticipants(100);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertTrue(exception.getMessage().contains("赛事名额已满"));
    }

    @Test
    void registerAlreadyRegistered() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(registrationRepository.findByUserIdAndEventId(anyLong(), anyLong())).thenReturn(Optional.of(mockRegistration));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertTrue(exception.getMessage().contains("已经报名"));
    }

    @Test
    void cancelRegistration() {
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.of(mockRegistration));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(registrationRepository.save(any(Registration.class))).thenReturn(mockRegistration);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);

        boolean result = registrationService.cancelRegistration(userId, 1L);

        assertTrue(result);
        verify(registrationRepository, times(1)).findById(anyLong());
        verify(eventRepository, times(1)).findById(anyLong());
        verify(registrationRepository, times(1)).save(any(Registration.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void cancelRegistrationNotFound() {
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.cancelRegistration(userId, 1L);
        });

        assertTrue(exception.getMessage().contains("报名记录不存在"));
    }

    @Test
    void cancelRegistrationUnauthorized() {
        mockRegistration.setUserId(2L);
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.of(mockRegistration));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.cancelRegistration(userId, 1L);
        });

        assertTrue(exception.getMessage().contains("无权取消"));
    }

    @Test
    void cancelRegistrationEventEnded() {
        mockEvent.setEndTime(LocalDateTime.now().minusDays(1));
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.of(mockRegistration));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.cancelRegistration(userId, 1L);
        });

        assertTrue(exception.getMessage().contains("赛事已结束"));
    }

    @Test
    void reviewRegistration() {
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.of(mockRegistration));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(registrationRepository.save(any(Registration.class))).thenReturn(mockRegistration);

        Registration result = registrationService.reviewRegistration(1L, "APPROVED", "审核通过");

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        verify(registrationRepository, times(1)).findById(anyLong());
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    void reviewRegistrationReject() {
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.of(mockRegistration));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(registrationRepository.save(any(Registration.class))).thenReturn(mockRegistration);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);

        Registration result = registrationService.reviewRegistration(1L, "REJECTED", "不符合条件");

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        verify(registrationRepository, times(1)).findById(anyLong());
        verify(registrationRepository, times(1)).save(any(Registration.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void reviewRegistrationNotFound() {
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.reviewRegistration(1L, "APPROVED", "审核通过");
        });

        assertTrue(exception.getMessage().contains("报名记录不存在"));
    }

    @Test
    void getUserRegistrations() {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationRepository.findByUserId(anyLong())).thenReturn(registrations);

        List<Registration> result = registrationService.getUserRegistrations(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(registrationRepository, times(1)).findByUserId(anyLong());
    }

    @Test
    void getEventRegistrations() {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationRepository.findByEventId(anyLong())).thenReturn(registrations);

        List<Registration> result = registrationService.getEventRegistrations(eventId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(registrationRepository, times(1)).findByEventId(anyLong());
    }

    @Test
    void getRegistrationById() {
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.of(mockRegistration));

        Registration result = registrationService.getRegistrationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(registrationRepository, times(1)).findById(anyLong());
    }

    @Test
    void getRegistrationsByStatus() {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationRepository.findByStatus(anyString())).thenReturn(registrations);

        List<Registration> result = registrationService.getRegistrationsByStatus("PENDING");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(registrationRepository, times(1)).findByStatus(anyString());
    }

    @Test
    void getRegistrationByIdNotFound() {
        when(registrationRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.getRegistrationById(1L);
        });

        assertTrue(exception.getMessage().contains("报名记录不存在"));
    }
} 