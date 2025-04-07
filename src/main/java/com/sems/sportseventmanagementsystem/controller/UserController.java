package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.Role;
import com.sems.sportseventmanagementsystem.entity.User;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.repository.RoleRepository;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    RoleRepository roleRepository;
    
    @Autowired
    PasswordEncoder encoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    if (userDetails.getEmail() != null) {
                        user.setEmail(userDetails.getEmail());
                    }
                    if (userDetails.getPhone() != null) {
                        user.setPhone(userDetails.getPhone());
                    }
                    if (userDetails.getRealName() != null) {
                        user.setRealName(userDetails.getRealName());
                    }
                    if (userDetails.getAvatar() != null) {
                        user.setAvatar(userDetails.getAvatar());
                    }
                    if (userDetails.getEnabled() != null) {
                        user.setEnabled(userDetails.getEnabled());
                    }
                    
                    // 处理角色更新
                    if (userDetails.getRoles() != null) {
                        Set<Role> roles = new HashSet<>();
                        
                        System.out.println("收到的角色数据: " + userDetails.getRoles());
                        
                        // 如果角色列表不为空，处理角色更新
                        if (!userDetails.getRoles().isEmpty()) {
                            // 遍历前端传来的角色列表
                            for (Role role : userDetails.getRoles()) {
                                System.out.println("处理角色: " + role);
                                
                                // 如果角色对象有name属性，通过name查找对应的角色
                                if (role.getName() != null) {
                                    System.out.println("通过角色名查找: " + role.getName());
                                    // 查找数据库中对应的角色
                                    Optional<Role> dbRole = roleRepository.findByName(role.getName());
                                    if (dbRole.isPresent()) {
                                        System.out.println("找到角色: " + dbRole.get().getName());
                                        roles.add(dbRole.get());
                                    } else {
                                        System.out.println("未找到角色: " + role.getName());
                                    }
                                } else if (role.getId() != null) {
                                    System.out.println("通过角色ID查找: " + role.getId());
                                    // 如果角色对象有ID，通过ID查找
                                    Optional<Role> dbRole = roleRepository.findById(role.getId());
                                    if (dbRole.isPresent()) {
                                        System.out.println("找到角色: " + dbRole.get().getName());
                                        roles.add(dbRole.get());
                                    } else {
                                        System.out.println("未找到角色ID: " + role.getId());
                                    }
                                }
                            }
                        }
                        
                        // 不管是否找到角色，都清空用户现有角色
                        System.out.println("更新角色列表，找到角色: " + roles.size() + "个");
                        // 清空现有角色，然后添加新角色
                        user.getRoles().clear();
                        user.getRoles().addAll(roles);
                    }
                    
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .map(user -> {
                    user.setPassword(encoder.encode(request.getNewPassword()));
                    userRepository.save(user);
                    return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(request.isEnabled());
                    userRepository.save(user);
                    return ResponseEntity.ok(new MessageResponse("User status updated successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCurrentUser(@RequestBody User userDetails) {
        UserDetailsImpl userDetails1 = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails1.getId())
                .map(user -> {
                    if (userDetails.getEmail() != null) {
                        user.setEmail(userDetails.getEmail());
                    }
                    if (userDetails.getPhone() != null) {
                        user.setPhone(userDetails.getPhone());
                    }
                    if (userDetails.getRealName() != null) {
                        user.setRealName(userDetails.getRealName());
                    }
                    if (userDetails.getAvatar() != null) {
                        user.setAvatar(userDetails.getAvatar());
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 专门用于更新当前用户头像的端点
    @PutMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCurrentUserAvatar(@RequestBody AvatarRequest avatarRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        System.out.println("正在更新用户头像. 用户ID: " + userDetails.getId() + ", 头像URL: " + avatarRequest.getAvatarUrl());
        
        return userRepository.findById(userDetails.getId())
                .map(user -> {
                    user.setAvatar(avatarRequest.getAvatarUrl());
                    userRepository.save(user);
                    return ResponseEntity.ok(new MessageResponse("Avatar updated successfully"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Inner class for password change request
    static class PasswordChangeRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    // 状态请求内部类
    static class StatusRequest {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    // 头像请求内部类
    static class AvatarRequest {
        private String avatarUrl;

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
} 