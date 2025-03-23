package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单的Ping控制器，用于测试系统连通性
 */
@RestController
public class PingController {

    /**
     * 简单的ping测试端点
     * 
     * @return Result对象，包含"pong"响应
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
} 