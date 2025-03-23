package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.security.JwtResponse;
import com.sems.sportseventmanagementsystem.security.JwtUtils;
import com.sems.sportseventmanagementsystem.security.LoginRequest;
import com.sems.sportseventmanagementsystem.security.UserDetailsImpl;
import com.sems.sportseventmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());
        
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成JWT令牌
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // 获取用户详情
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 返回JWT响应
        JwtResponse response = new JwtResponse(
                userDetails.getId(),
                jwt,
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole());
        
        return Result.success(response);
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<JwtResponse> register(@Valid @RequestBody UserDTO registerRequest) {
        log.info("用户注册: {}", registerRequest.getUsername());
        
        // 检查用户名是否已存在
        if (userService.checkUsernameExists(registerRequest.getUsername())) {
            return Result.error(HttpStatus.BAD_REQUEST.value(), "用户名已存在");
        }
        
        // 检查邮箱是否已被注册
        if (userService.checkEmailExists(registerRequest.getEmail())) {
            return Result.error(HttpStatus.BAD_REQUEST.value(), "邮箱已被注册");
        }
        
        // 设置默认角色为普通用户
        registerRequest.setRole("USER");
        registerRequest.setStatus(1);
        
        // 创建用户
        User user = userService.createUser(registerRequest);
        
        // 自动登录
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成JWT令牌
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // 获取用户详情
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 返回JWT响应
        JwtResponse response = new JwtResponse(
                userDetails.getId(),
                jwt,
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole());
        
        return Result.success(response);
    }
} 