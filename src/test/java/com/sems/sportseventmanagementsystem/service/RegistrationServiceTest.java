package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.mapper.EventMapper;
import com.sems.sportseventmanagementsystem.mapper.RegistrationMapper;
import com.sems.sportseventmanagementsystem.model.dto.RegistrationDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.model.entity.Registration;
import com.sems.sportseventmanagementsystem.service.impl.RegistrationServiceImpl;
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

class RegistrationServiceTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private EventMapper eventMapper;

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

        // 创建模拟赛事
        mockEvent = new Event();
        mockEvent.setId(eventId);
        mockEvent.setName("测试赛事");
        mockEvent.setStartTime(LocalDateTime.now().plusDays(1));
        mockEvent.setEndTime(LocalDateTime.now().plusDays(2));
        mockEvent.setStatus("OPEN");
        mockEvent.setMaxParticipants(100);
        mockEvent.setCurrentParticipants(10);

        // 创建模拟报名DTO
        mockRegistrationDTO = new RegistrationDTO();
        mockRegistrationDTO.setEventId(eventId);
        mockRegistrationDTO.setRegistrationType("INDIVIDUAL");
        mockRegistrationDTO.setRemark("测试备注");

        // 创建模拟报名记录
        mockRegistration = new Registration();
        mockRegistration.setId(1L);
        mockRegistration.setUserId(userId);
        mockRegistration.setEventId(eventId);
        mockRegistration.setRegistrationType("INDIVIDUAL");
        mockRegistration.setStatus("PENDING");
        mockRegistration.setRegisterTime(LocalDateTime.now());
        mockRegistration.setUpdateTime(LocalDateTime.now());
        mockRegistration.setRemark("测试备注");
    }

    @Test
    void register() {
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);
        when(registrationMapper.selectByUserIdAndEventId(anyLong(), anyLong())).thenReturn(null);
        when(registrationMapper.insert(any(Registration.class))).thenReturn(1);
        when(eventMapper.update(any(Event.class))).thenReturn(1);

        Registration result = registrationService.register(userId, mockRegistrationDTO);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(eventId, result.getEventId());
        assertEquals("PENDING", result.getStatus());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, times(1)).selectByUserIdAndEventId(anyLong(), anyLong());
        verify(registrationMapper, times(1)).insert(any(Registration.class));
        verify(eventMapper, times(1)).update(any(Event.class));
    }

    @Test
    void registerEventNotFound() {
        when(eventMapper.selectById(anyLong())).thenReturn(null);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertEquals("赛事不存在", exception.getMessage());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, never()).selectByUserIdAndEventId(anyLong(), anyLong());
        verify(registrationMapper, never()).insert(any(Registration.class));
    }

    @Test
    void registerEventEnded() {
        mockEvent.setStatus("ENDED");
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertEquals("赛事已结束，无法报名", exception.getMessage());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, never()).insert(any(Registration.class));
    }

    @Test
    void registerEventFull() {
        mockEvent.setCurrentParticipants(mockEvent.getMaxParticipants());
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertEquals("赛事报名人数已满", exception.getMessage());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, never()).insert(any(Registration.class));
    }

    @Test
    void registerAlreadyRegistered() {
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);
        when(registrationMapper.selectByUserIdAndEventId(anyLong(), anyLong())).thenReturn(mockRegistration);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.register(userId, mockRegistrationDTO);
        });

        assertEquals("您已报名此赛事", exception.getMessage());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, times(1)).selectByUserIdAndEventId(anyLong(), anyLong());
        verify(registrationMapper, never()).insert(any(Registration.class));
    }

    @Test
    void cancelRegistration() {
        when(registrationMapper.selectById(anyLong())).thenReturn(mockRegistration);
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);
        when(registrationMapper.delete(anyLong())).thenReturn(1);
        when(eventMapper.update(any(Event.class))).thenReturn(1);

        boolean result = registrationService.cancelRegistration(userId, 1L);

        assertTrue(result);
        verify(registrationMapper, times(1)).selectById(anyLong());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, times(1)).delete(anyLong());
        verify(eventMapper, times(1)).update(any(Event.class));
    }

    @Test
    void cancelRegistrationNotFound() {
        when(registrationMapper.selectById(anyLong())).thenReturn(null);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.cancelRegistration(userId, 1L);
        });

        assertEquals("报名记录不存在", exception.getMessage());
        verify(registrationMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, never()).delete(anyLong());
    }

    @Test
    void cancelRegistrationUnauthorized() {
        mockRegistration.setUserId(2L); // 不是当前用户的报名
        when(registrationMapper.selectById(anyLong())).thenReturn(mockRegistration);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.cancelRegistration(userId, 1L);
        });

        assertEquals("无权操作此报名记录", exception.getMessage());
        verify(registrationMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, never()).delete(anyLong());
    }

    @Test
    void cancelRegistrationEventEnded() {
        mockEvent.setStatus("ENDED");
        when(registrationMapper.selectById(anyLong())).thenReturn(mockRegistration);
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.cancelRegistration(userId, 1L);
        });

        assertEquals("赛事已结束，无法取消报名", exception.getMessage());
        verify(registrationMapper, times(1)).selectById(anyLong());
        verify(eventMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, never()).delete(anyLong());
    }

    @Test
    void reviewRegistration() {
        when(registrationMapper.selectById(anyLong())).thenReturn(mockRegistration);
        when(registrationMapper.update(any(Registration.class))).thenReturn(1);

        Registration result = registrationService.reviewRegistration(1L, "APPROVED", "审核通过");

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals("审核通过", result.getRemark());
        verify(registrationMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, times(1)).update(any(Registration.class));
        verify(eventMapper, never()).update(any(Event.class)); // 通过不需要减少人数
    }

    @Test
    void reviewRegistrationReject() {
        when(registrationMapper.selectById(anyLong())).thenReturn(mockRegistration);
        when(registrationMapper.update(any(Registration.class))).thenReturn(1);
        when(eventMapper.selectById(anyLong())).thenReturn(mockEvent);
        when(eventMapper.update(any(Event.class))).thenReturn(1);

        Registration result = registrationService.reviewRegistration(1L, "REJECTED", "不符合条件");

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        assertEquals("不符合条件", result.getRemark());
        verify(registrationMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, times(1)).update(any(Registration.class));
        verify(eventMapper, times(1)).selectById(anyLong()); // 拒绝需要获取事件以减少人数
        verify(eventMapper, times(1)).update(any(Event.class)); // 拒绝需要减少人数
    }

    @Test
    void reviewRegistrationNotFound() {
        when(registrationMapper.selectById(anyLong())).thenReturn(null);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.reviewRegistration(1L, "APPROVED", "审核通过");
        });

        assertEquals("报名记录不存在", exception.getMessage());
        verify(registrationMapper, times(1)).selectById(anyLong());
        verify(registrationMapper, never()).update(any(Registration.class));
    }

    @Test
    void getUserRegistrations() {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationMapper.selectWithEventByUserId(anyLong())).thenReturn(registrations);

        List<Registration> result = registrationService.getUserRegistrations(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(registrationMapper, times(1)).selectWithEventByUserId(anyLong());
    }

    @Test
    void getEventRegistrations() {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationMapper.selectWithUserByEventId(anyLong())).thenReturn(registrations);

        List<Registration> result = registrationService.getEventRegistrations(eventId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(registrationMapper, times(1)).selectWithUserByEventId(anyLong());
    }

    @Test
    void getRegistrationById() {
        when(registrationMapper.selectWithUserAndEventById(anyLong())).thenReturn(mockRegistration);

        Registration result = registrationService.getRegistrationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(registrationMapper, times(1)).selectWithUserAndEventById(anyLong());
    }

    @Test
    void getRegistrationsByStatus() {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(mockRegistration);
        when(registrationMapper.selectByStatus(anyString())).thenReturn(registrations);

        List<Registration> result = registrationService.getRegistrationsByStatus("PENDING");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(registrationMapper, times(1)).selectByStatus(anyString());
    }

    @Test
    void getRegistrationByIdNotFound() {
        when(registrationMapper.selectWithUserAndEventById(anyLong())).thenReturn(null);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.getRegistrationById(1L);
        });

        assertEquals("报名记录不存在", exception.getMessage());
        verify(registrationMapper, times(1)).selectWithUserAndEventById(anyLong());
    }
} 