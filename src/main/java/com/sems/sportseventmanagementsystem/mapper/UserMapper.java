package com.sems.sportseventmanagementsystem.mapper;

import com.sems.sportseventmanagementsystem.model.entity.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    User selectById(Long id);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
    
    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> selectAll();
    
    /**
     * 插入用户
     * @param user 用户信息
     * @return 影响的行数
     */
    int insert(User user);
    
    /**
     * 更新用户
     * @param user 用户信息
     * @return 影响的行数
     */
    int update(User user);
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响的行数
     */
    int delete(Long id);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int checkUsernameExists(String username);
    
    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(*) FROM user WHERE email = #{email}")
    int checkEmailExists(String email);
    
    /**
     * 更新用户密码
     * @param id 用户ID
     * @param newPassword 新密码（已加密）
     * @return 影响的行数
     */
    int updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword);
    
    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     * @return 影响的行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User findById(Long id);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User findByEmail(String email);

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 根据角色查找用户
     *
     * @param role 角色
     * @return 用户列表
     */
    List<User> findByRole(String role);

    /**
     * 根据状态查找用户
     *
     * @param status 状态
     * @return 用户列表
     */
    List<User> findByStatus(Integer status);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 影响的行数
     */
    int deleteById(Long id);
} 