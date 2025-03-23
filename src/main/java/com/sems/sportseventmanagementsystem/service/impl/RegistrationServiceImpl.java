package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.mapper.EventMapper;
import com.sems.sportseventmanagementsystem.mapper.RegistrationMapper;
import com.sems.sportseventmanagementsystem.model.dto.RegistrationDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.model.entity.Registration;
import com.sems.sportseventmanagementsystem.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private EventMapper eventMapper;

    @Override
    @Transactional
    public Registration register(Long userId, RegistrationDTO registrationDTO) {
        // 检查赛事是否存在
        Event event = eventMapper.selectById(registrationDTO.getEventId());
        if (event == null) {
            throw new ResourceNotFoundException("赛事不存在");
        }
        
        // 检查赛事是否已结束
        if ("ENDED".equals(event.getStatus()) || LocalDateTime.now().isAfter(event.getEndTime())) {
            throw new ResourceNotFoundException("赛事已结束，无法报名");
        }
        
        // 检查是否已达到最大参与人数
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new ResourceNotFoundException("赛事报名人数已满");
        }
        
        // 检查用户是否已经报名该赛事
        Registration existingRegistration = registrationMapper.selectByUserIdAndEventId(userId, registrationDTO.getEventId());
        if (existingRegistration != null) {
            throw new ResourceNotFoundException("您已报名此赛事");
        }
        
        // 创建报名记录
        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setEventId(registrationDTO.getEventId());
        registration.setRegistrationType(registrationDTO.getRegistrationType());
        registration.setStatus("PENDING"); // 初始状态为待审核
        registration.setRegisterTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registration.setRemark(registrationDTO.getRemark());
        
        registrationMapper.insert(registration);
        
        // 更新赛事参与人数
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventMapper.update(event);
        
        return registration;
    }

    @Override
    @Transactional
    public boolean cancelRegistration(Long userId, Long registrationId) {
        // 获取报名记录
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new ResourceNotFoundException("报名记录不存在");
        }
        
        // 检查是否为该用户的报名记录
        if (!registration.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("无权操作此报名记录");
        }
        
        // 检查赛事状态
        Event event = eventMapper.selectById(registration.getEventId());
        if ("ENDED".equals(event.getStatus()) || LocalDateTime.now().isAfter(event.getEndTime())) {
            throw new ResourceNotFoundException("赛事已结束，无法取消报名");
        }
        
        // 删除报名记录
        int result = registrationMapper.delete(registrationId);
        
        if (result > 0) {
            // 更新赛事参与人数
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
            eventMapper.update(event);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public Registration reviewRegistration(Long registrationId, String status, String remark) {
        // 获取报名记录
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new ResourceNotFoundException("报名记录不存在");
        }
        
        // 更新状态
        registration.setStatus(status);
        registration.setRemark(remark);
        registration.setUpdateTime(LocalDateTime.now());
        
        registrationMapper.update(registration);
        
        // 如果拒绝报名，需要减少参与人数
        if ("REJECTED".equals(status)) {
            Event event = eventMapper.selectById(registration.getEventId());
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
            eventMapper.update(event);
        }
        
        return registration;
    }

    @Override
    public List<Registration> getUserRegistrations(Long userId) {
        return registrationMapper.selectWithEventByUserId(userId);
    }

    @Override
    public List<Registration> getEventRegistrations(Long eventId) {
        return registrationMapper.selectWithUserByEventId(eventId);
    }

    @Override
    public Registration getRegistrationById(Long registrationId) {
        Registration registration = registrationMapper.selectWithUserAndEventById(registrationId);
        if (registration == null) {
            throw new ResourceNotFoundException("报名记录不存在");
        }
        return registration;
    }

    @Override
    public List<Registration> getRegistrationsByStatus(String status) {
        return registrationMapper.selectByStatus(status);
    }
} 