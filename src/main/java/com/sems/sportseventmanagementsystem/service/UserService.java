package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.model.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

    /**
     * 创建用户
     *
     * @param userDTO 用户DTO
     * @return 创建的用户对象
     */
    User createUser(UserDTO userDTO);

    /**
     * 更新用户信息
     *
     * @param id       用户ID
     * @param userDTO  用户DTO
     * @return 更新后的用户对象
     */
    User updateUser(Long id, UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long id);

    /**
     * 修改密码
     *
     * @param id          用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态
     * @return 更新后的用户对象
     */
    User updateUserStatus(Long id, Integer status);

    /**
     * 获取所有用户
     *
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 根据角色获取用户
     *
     * @param role 角色
     * @return 用户列表
     */
    List<User> getUsersByRole(String role);

    /**
     * 根据状态获取用户
     *
     * @param status 状态
     * @return 用户列表
     */
    List<User> getUsersByStatus(Integer status);
    
    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean checkEmailExists(String email);
} 