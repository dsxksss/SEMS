package com.sems.sportseventmanagementsystem.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求对象
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
} 