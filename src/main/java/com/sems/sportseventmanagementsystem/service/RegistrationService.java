package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.model.dto.RegistrationDTO;
import com.sems.sportseventmanagementsystem.model.entity.Registration;

import java.util.List;

public interface RegistrationService {
    
    /**
     * 用户报名赛事
     * @param userId 用户ID
     * @param registrationDTO 报名信息
     * @return 报名记录
     */
    Registration register(Long userId, RegistrationDTO registrationDTO);
    
    /**
     * 取消报名
     * @param userId 用户ID
     * @param registrationId 报名ID
     * @return 是否成功
     */
    boolean cancelRegistration(Long userId, Long registrationId);
    
    /**
     * 管理员审核报名
     * @param registrationId 报名ID
     * @param status 审核状态
     * @param remark 备注
     * @return 更新后的报名记录
     */
    Registration reviewRegistration(Long registrationId, String status, String remark);
    
    /**
     * 获取用户的所有报名记录
     * @param userId 用户ID
     * @return 报名记录列表
     */
    List<Registration> getUserRegistrations(Long userId);
    
    /**
     * 获取赛事的所有报名记录
     * @param eventId 赛事ID
     * @return 报名记录列表
     */
    List<Registration> getEventRegistrations(Long eventId);
    
    /**
     * 获取报名详情
     * @param registrationId 报名ID
     * @return 报名详情
     */
    Registration getRegistrationById(Long registrationId);
    
    /**
     * 根据状态筛选报名记录
     * @param status 状态
     * @return 报名记录列表
     */
    List<Registration> getRegistrationsByStatus(String status);
} 