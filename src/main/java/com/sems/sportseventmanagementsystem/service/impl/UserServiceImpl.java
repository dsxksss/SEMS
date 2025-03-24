package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，用户名: " + username));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            throw new ResourceNotFoundException("用户名已存在");
        }

        // 检查邮箱是否已被注册
        Optional<User> existingEmail = userRepository.findByEmail(userDTO.getEmail());
        if (existingEmail.isPresent()) {
            throw new ResourceNotFoundException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRealName(userDTO.getRealName());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : "USER");
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + id));

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getRealName() != null) {
            user.setRealName(userDTO.getRealName());
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }
        user.setUpdateTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + id));

        userRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + id));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public User updateUserStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + id));

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> getUsersByStatus(Integer status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
} 