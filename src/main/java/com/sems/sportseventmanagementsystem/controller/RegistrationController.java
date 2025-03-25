package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import com.sems.sportseventmanagementsystem.model.dto.RegistrationDTO;
import com.sems.sportseventmanagementsystem.model.entity.Registration;
import com.sems.sportseventmanagementsystem.security.UserDetailsImpl;
import com.sems.sportseventmanagementsystem.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 用户报名赛事
     */
    @PostMapping
    public Result<Registration> register(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody RegistrationDTO registrationDTO) {
        Registration registration = registrationService.register(userDetails.getId(), registrationDTO);
        return Result.success(registration);
    }

    /**
     * 取消报名
     */
    @DeleteMapping("/{registrationId}")
    public Result<Boolean> cancelRegistration(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long registrationId) {
        boolean result = registrationService.cancelRegistration(userDetails.getId(), registrationId);
        return Result.success(result);
    }

    /**
     * 管理员审核报名
     */
    @PutMapping("/{registrationId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Registration> reviewRegistration(
            @PathVariable Long registrationId,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        Registration registration = registrationService.reviewRegistration(registrationId, status, remark);
        return Result.success(registration);
    }

    /**
     * 获取当前用户的报名记录
     */
    @GetMapping("/my")
    public Result<List<Registration>> getMyRegistrations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Registration> registrations = registrationService.getUserRegistrations(userDetails.getId());
        return Result.success(registrations);
    }

    /**
     * 获取指定赛事的所有报名记录（管理员）
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Registration>> getEventRegistrations(@PathVariable Long eventId) {
        List<Registration> registrations = registrationService.getEventRegistrations(eventId);
        return Result.success(registrations);
    }

    /**
     * 获取报名详情
     */
    @GetMapping("/{registrationId}")
    public Result<Registration> getRegistrationById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        
        // 非管理员只能查看自己的报名信息
        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !registration.getUserId().equals(userDetails.getId())) {
            return Result.error("您无权查看此报名信息");
        }
        
        return Result.success(registration);
    }

    /**
     * 根据状态筛选报名记录（管理员）
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Registration>> getRegistrationsByStatus(@PathVariable String status) {
        List<Registration> registrations = registrationService.getRegistrationsByStatus(status);
        return Result.success(registrations);
    }

    /**
     * 导出赛事报名数据（管理员）
     */
    @GetMapping("/export/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportRegistrations(@PathVariable Long eventId) {
        // 获取赛事报名列表
        List<Registration> registrations = registrationService.getEventRegistrations(eventId);
        
        // 导出为CSV格式
        StringBuilder csv = new StringBuilder();
        // 添加CSV头
        csv.append("报名ID,用户ID,用户名,赛事ID,赛事名称,注册类型,报名时间,更新时间,状态,备注\n");
        
        // 添加数据行
        for (Registration reg : registrations) {
            String username = reg.getUser() != null ? reg.getUser().getUsername() : "";
            String eventName = reg.getEvent() != null ? reg.getEvent().getName() : "";
            
            csv.append(reg.getId()).append(",")
               .append(reg.getUserId()).append(",")
               .append(escapeCSV(username)).append(",")
               .append(reg.getEventId()).append(",")
               .append(escapeCSV(eventName)).append(",")
               .append(escapeCSV(reg.getRegistrationType())).append(",")
               .append(reg.getRegisterTime()).append(",")
               .append(reg.getUpdateTime()).append(",")
               .append(reg.getStatus()).append(",")
               .append(escapeCSV(reg.getRemark() != null ? reg.getRemark() : "")).append("\n");
        }
        
        byte[] data = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registrations-" + eventId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    /**
     * CSV字段转义，防止逗号和换行符干扰CSV格式
     */
    private String escapeCSV(String field) {
        if (field == null) {
            return "";
        }
        // 如果字段包含逗号、引号或换行符，用引号包裹并将引号替换为两个引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
} 