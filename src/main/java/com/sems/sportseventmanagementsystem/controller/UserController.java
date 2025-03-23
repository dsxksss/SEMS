package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.common.Result;
import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.security.UserDetailsImpl;
import com.sems.sportseventmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserById(userDetails.getId());
        // 敏感信息置空
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 获取指定用户信息（管理员）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 敏感信息置空
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 获取所有用户（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // 敏感信息置空
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }

    /**
     * 创建用户（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@RequestBody UserDTO userDTO) {
        User createdUser = userService.createUser(userDTO);
        // 敏感信息置空
        createdUser.setPassword(null);
        return Result.success(createdUser);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/me")
    public Result<User> updateCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(userDetails.getId(), userDTO);
        // 敏感信息置空
        updatedUser.setPassword(null);
        return Result.success(updatedUser);
    }

    /**
     * 更新指定用户信息（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(id, userDTO);
        // 敏感信息置空
        updatedUser.setPassword(null);
        return Result.success(updatedUser);
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userService.deleteUser(id);
        return Result.success(result);
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/me/password")
    public Result<Boolean> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody Map<String, String> passwords) {
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("旧密码和新密码不能为空");
        }
        
        boolean result = userService.changePassword(userDetails.getId(), oldPassword, newPassword);
        return Result.success(result);
    }

    /**
     * 修改用户状态（管理员）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User updatedUser = userService.updateUserStatus(id, status);
        return Result.success(updatedUser != null);
    }
} 