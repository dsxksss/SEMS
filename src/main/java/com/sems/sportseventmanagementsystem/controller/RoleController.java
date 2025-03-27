package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.Role;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        System.out.println("Creating role: " + role.getName() + ", displayName: " + role.getDisplayName());
        
        // 验证必须提供的字段
        if (role.getName() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 角色标识不能为空!"));
        }
        
        // 验证角色名称是否已存在
        Optional<Role> existingRole = roleRepository.findByName(role.getName());
        
        // 检查是否有相同displayName的角色
        boolean displayNameExists = false;
        if (role.getDisplayName() != null && !role.getDisplayName().isEmpty()) {
            List<Role> roles = roleRepository.findAll();
            displayNameExists = roles.stream()
                    .anyMatch(r -> role.getDisplayName().equals(r.getDisplayName()));
        }
        
        // 情况1: 如果是创建使用现有ERole值但使用不同displayName的角色
        if (existingRole.isPresent() && role.getDisplayName() != null && !role.getDisplayName().isEmpty()) {
            // 如果displayName已存在，返回错误
            if (displayNameExists) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("错误: 该角色显示名称已存在!"));
            }
            
            // 创建一个新角色，复用现有的ERole枚举值，但使用新的displayName
            Role newRole = new Role();
            newRole.setName(role.getName());
            newRole.setDisplayName(role.getDisplayName());
            newRole.setDescription(role.getDescription());
            
            Role savedRole = roleRepository.save(newRole);
            return ResponseEntity.ok(savedRole);
        } 
        // 情况2: 如果是创建一个新的ERole值，但已存在
        else if (existingRole.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 角色标识 " + role.getName() + " 已存在!"));
        }
        // 情况3: 如果displayName已存在，返回错误
        else if (displayNameExists) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 角色显示名称 " + role.getDisplayName() + " 已存在!"));
        }
        
        // 情况4: 全新的角色
        System.out.println("Saving new role: " + role.getName());
        Role savedRole = roleRepository.save(role);
        return ResponseEntity.ok(savedRole);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        return roleRepository.findById(id)
                .map(role -> {
                    // 只更新不为null的字段
                    if (roleDetails.getName() != null) {
                        role.setName(roleDetails.getName());
                    }
                    
                    if (roleDetails.getDisplayName() != null) {
                        role.setDisplayName(roleDetails.getDisplayName());
                    }
                    
                    if (roleDetails.getDescription() != null) {
                        role.setDescription(roleDetails.getDescription());
                    }
                    
                    // 处理权限更新
                    if (roleDetails.getPermissions() != null) {
                        role.setPermissions(roleDetails.getPermissions());
                    }
                    
                    return ResponseEntity.ok(roleRepository.save(role));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(role -> {
                    roleRepository.delete(role);
                    return ResponseEntity.ok(new MessageResponse("角色删除成功!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllPermissions() {
        // 这里返回系统中所有可用的权限列表
        // 在实际应用中，这些权限通常来自数据库或配置文件
        List<String> permissions = Arrays.asList(
            "user:create", "user:read", "user:update", "user:delete",
            "role:create", "role:read", "role:update", "role:delete",
            "event:create", "event:read", "event:update", "event:delete",
            "category:create", "category:read", "category:update", "category:delete",
            "registration:create", "registration:read", "registration:update", "registration:delete",
            "result:create", "result:read", "result:update", "result:delete",
            "announcement:create", "announcement:read", "announcement:update", "announcement:delete",
            "statistics:read", "file:upload", "file:download"
        );
        
        return ResponseEntity.ok(permissions);
    }
    
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addPermissionToRole(@PathVariable Long id, @RequestBody PermissionRequest request) {
        if (request.getPermission() == null || request.getPermission().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("权限名称不能为空"));
        }
        
        return roleRepository.findById(id)
                .map(role -> {
                    // 这里我们假设Role对象有一个getPermissions和setPermissions方法
                    // 在实际应用中，您需要根据您的数据模型进行调整
                    List<String> permissions = role.getPermissions();
                    if (permissions == null) {
                        permissions = new ArrayList<>();
                    }
                    
                    if (!permissions.contains(request.getPermission())) {
                        permissions.add(request.getPermission());
                        role.setPermissions(permissions);
                        roleRepository.save(role);
                    }
                    
                    return ResponseEntity.ok(new MessageResponse("权限添加成功"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}/permissions/{permission}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removePermissionFromRole(@PathVariable Long id, @PathVariable String permission) {
        return roleRepository.findById(id)
                .map(role -> {
                    List<String> permissions = role.getPermissions();
                    if (permissions != null && permissions.contains(permission)) {
                        permissions.remove(permission);
                        role.setPermissions(permissions);
                        roleRepository.save(role);
                    }
                    
                    return ResponseEntity.ok(new MessageResponse("权限移除成功"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // 权限请求内部类
    static class PermissionRequest {
        private String permission;
        
        public String getPermission() {
            return permission;
        }
        
        public void setPermission(String permission) {
            this.permission = permission;
        }
    }
} 