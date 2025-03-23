package com.sems.sportseventmanagementsystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * JWT认证响应对象
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    private Long id;
    private String token;
    private String username;
    private String email;
    private String role;
} 