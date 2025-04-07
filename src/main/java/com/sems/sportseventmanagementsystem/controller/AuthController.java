package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.ERole;
import com.sems.sportseventmanagementsystem.entity.RefreshToken;
import com.sems.sportseventmanagementsystem.entity.Role;
import com.sems.sportseventmanagementsystem.entity.User;
import com.sems.sportseventmanagementsystem.payload.request.LoginRequest;
import com.sems.sportseventmanagementsystem.payload.request.SignupRequest;
import com.sems.sportseventmanagementsystem.payload.request.TokenRefreshRequest;
import com.sems.sportseventmanagementsystem.payload.response.JwtResponse;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.payload.response.TokenRefreshResponse;
import com.sems.sportseventmanagementsystem.repository.RoleRepository;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.security.exception.TokenRefreshException;
import com.sems.sportseventmanagementsystem.security.jwt.JwtUtils;
import com.sems.sportseventmanagementsystem.security.services.RefreshTokenService;
import com.sems.sportseventmanagementsystem.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        
        // 创建刷新令牌
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 refreshToken.getToken(),
                                                 userDetails.getId(), 
                                                 userDetails.getUsername(), 
                                                 userDetails.getEmail(), 
                                                 roles));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        refreshTokenService.deleteByUserId(userDetails.getId());
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                             signUpRequest.getEmail(),
                             encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // 如果没有指定角色，默认为普通用户
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                try {
                    // 处理角色名称
                    String normalizedRole = role.toLowerCase();
                    
                    // 根据角色名判断对应的ERole
                    switch (normalizedRole) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Admin role not found."));
                            roles.add(adminRole);
                            break;
                        case "athlete":
                            Role athleteRole = roleRepository.findByName(ERole.ROLE_ATHLETE)
                                    .orElseThrow(() -> new RuntimeException("Error: Athlete role not found."));
                            roles.add(athleteRole);
                            break;
                        case "spectator":
                            Role spectatorRole = roleRepository.findByName(ERole.ROLE_SPECTATOR)
                                    .orElseThrow(() -> new RuntimeException("Error: Spectator role not found."));
                            roles.add(spectatorRole);
                            break;
                        case "user":
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: User role not found."));
                            roles.add(userRole);
                            break;
                        default:
                            // 对于其他角色，检查是否是完整的ROLE_格式
                            // 由于我们不能直接通过字符串查找角色，因此需要先查找所有角色，然后手动过滤
                            if (normalizedRole.startsWith("role_")) {
                                String fullRoleName = normalizedRole.toUpperCase();
                                boolean foundRole = false;
                                // 遍历所有角色，查找匹配的角色名
                                for (Role existingRole : roleRepository.findAll()) {
                                    if (existingRole.getName().toString().equals(fullRoleName)) {
                                        roles.add(existingRole);
                                        foundRole = true;
                                        break;
                                    }
                                }
                                if (!foundRole) {
                                    // 如果找不到角色，使用默认用户角色
                                    System.out.println("未找到角色 " + fullRoleName + "，使用默认用户角色");
                                    Role defaultRole = roleRepository.findByName(ERole.ROLE_USER)
                                            .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
                                    roles.add(defaultRole);
                                }
                            } else {
                                // 如果不是ROLE_格式，则尝试查找所有角色中是否有匹配的displayName
                                boolean foundRole = false;
                                for (Role existingRole : roleRepository.findAll()) {
                                    // 检查角色的displayName属性（如果有的话）
                                    if (existingRole.getDisplayName() != null && 
                                        existingRole.getDisplayName().toLowerCase().equals(normalizedRole)) {
                                        roles.add(existingRole);
                                        foundRole = true;
                                        break;
                                    }
                                }
                                if (!foundRole) {
                                    // 如果找不到角色，使用默认用户角色
                                    System.out.println("未找到角色名称 " + normalizedRole + "，使用默认用户角色");
                                    Role defaultRole = roleRepository.findByName(ERole.ROLE_USER)
                                            .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
                                    roles.add(defaultRole);
                                }
                            }
                    }
                } catch (Exception e) {
                    System.out.println("处理角色时出错: " + e.getMessage());
                    // 如果发生异常，默认为普通用户
                    try {
                        Role defaultRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
                        roles.add(defaultRole);
                    } catch (Exception ex) {
                        System.out.println("获取默认角色失败: " + ex.getMessage());
                    }
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
} 