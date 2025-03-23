package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "首页", description = "首页相关接口")
public class IndexController {

    @GetMapping
    @Operation(summary = "获取API信息", description = "返回API基本信息，不需要认证")
    public Result<Map<String, Object>> index() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "体育赛事管理系统");
        info.put("version", "1.0.0");
        info.put("description", "体育赛事管理系统API");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("认证", "/api/auth");
        endpoints.put("用户", "/api/users");
        endpoints.put("赛事", "/api/events");
        endpoints.put("报名", "/api/registrations");
        endpoints.put("API文档", "/swagger-ui.html");
        
        info.put("endpoints", endpoints);
        
        return Result.success(info);
    }
} 