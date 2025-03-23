package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.mapper.UserMapper;
import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        return user;
    }

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (checkUsernameExists(userDTO.getUsername())) {
            throw new ResourceNotFoundException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (checkEmailExists(userDTO.getEmail())) {
            throw new ResourceNotFoundException("邮箱已被注册");
        }
        
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        // 默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        // 插入用户并返回
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = getUserById(id);
        
        // 除了密码外的其他属性都可以更新
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhone() != null) {
            existingUser.setPhone(userDTO.getPhone());
        }
        if (userDTO.getRealName() != null) {
            existingUser.setRealName(userDTO.getRealName());
        }
        if (userDTO.getRole() != null) {
            existingUser.setRole(userDTO.getRole());
        }
        if (userDTO.getStatus() != null) {
            existingUser.setStatus(userDTO.getStatus());
        }
        
        // 更新时间
        existingUser.setUpdateTime(LocalDateTime.now());
        
        // 更新用户
        userMapper.update(existingUser);
        return existingUser;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = getUserById(id);
        return userMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        return userMapper.update(user) > 0;
    }

    @Override
    @Transactional
    public User updateUserStatus(Long id, Integer status) {
        User user = getUserById(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userMapper.findByRole(role);
    }

    @Override
    public List<User> getUsersByStatus(Integer status) {
        return userMapper.findByStatus(status);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userMapper.findByUsername(username) != null;
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userMapper.findByEmail(email) != null;
    }
} 