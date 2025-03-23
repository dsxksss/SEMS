package com.sems.sportseventmanagementsystem.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务的模拟测试，不依赖Spring上下文
 * 演示如何使用模拟对象进行服务层测试
 */
public class MockUserServiceTest {
    
    // 模拟数据对象
    private static class User {
        private Long id;
        private String username;
        private String email;
        private String password;
        private Integer status;
        
        public User(Long id, String username, String email, String password) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.status = 1; // 默认激活状态
        }
        
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Integer getStatus() { return status; }
        
        public void setStatus(Integer status) { this.status = status; }
    }
    
    // 模拟DAO层
    private static class MockUserDAO {
        private User savedUser;
        
        public User findByUsername(String username) {
            if (savedUser != null && savedUser.getUsername().equals(username)) {
                return savedUser;
            }
            return null;
        }
        
        public User findByEmail(String email) {
            if (savedUser != null && savedUser.getEmail().equals(email)) {
                return savedUser;
            }
            return null;
        }
        
        public void save(User user) {
            savedUser = user;
        }
        
        public User findById(Long id) {
            if (savedUser != null && savedUser.getId().equals(id)) {
                return savedUser;
            }
            return null;
        }
    }
    
    // 被测试的服务类
    private static class UserService {
        private MockUserDAO userDAO;
        
        public UserService(MockUserDAO userDAO) {
            this.userDAO = userDAO;
        }
        
        public User register(String username, String email, String password) throws Exception {
            // 验证用户名是否已存在
            if (userDAO.findByUsername(username) != null) {
                throw new Exception("用户名已存在");
            }
            
            // 验证邮箱是否已存在
            if (userDAO.findByEmail(email) != null) {
                throw new Exception("邮箱已存在");
            }
            
            // 创建新用户
            User newUser = new User(1L, username, email, password);
            userDAO.save(newUser);
            return newUser;
        }
        
        public boolean updateStatus(Long userId, Integer status) {
            User user = userDAO.findById(userId);
            if (user == null) {
                return false;
            }
            
            user.setStatus(status);
            userDAO.save(user);
            return true;
        }
        
        public User findByUsername(String username) {
            return userDAO.findByUsername(username);
        }
    }
    
    // 测试相关成员变量
    private MockUserDAO mockUserDAO;
    private UserService userService;
    
    @BeforeEach
    public void setUp() {
        // 创建模拟DAO和服务
        mockUserDAO = new MockUserDAO();
        userService = new UserService(mockUserDAO);
    }
    
    @Test
    @DisplayName("测试用户注册成功")
    public void testUserRegisterSuccess() throws Exception {
        // 执行注册操作
        User registeredUser = userService.register("testuser", "test@example.com", "password123");
        
        // 验证结果
        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        assertEquals("test@example.com", registeredUser.getEmail());
        assertEquals(1, registeredUser.getStatus()); // 确认用户状态为活跃
    }
    
    @Test
    @DisplayName("测试用户名重复注册")
    public void testRegisterWithDuplicateUsername() throws Exception {
        // 先注册一个用户
        userService.register("testuser", "test@example.com", "password123");
        
        // 尝试使用相同用户名注册
        Exception exception = assertThrows(Exception.class, () -> {
            userService.register("testuser", "another@example.com", "password456");
        });
        
        // 验证异常信息
        assertEquals("用户名已存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试邮箱重复注册")
    public void testRegisterWithDuplicateEmail() throws Exception {
        // 先注册一个用户
        userService.register("testuser", "test@example.com", "password123");
        
        // 尝试使用相同邮箱注册
        Exception exception = assertThrows(Exception.class, () -> {
            userService.register("anotheruser", "test@example.com", "password456");
        });
        
        // 验证异常信息
        assertEquals("邮箱已存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试更新用户状态")
    public void testUpdateUserStatus() throws Exception {
        // 先注册一个用户
        User user = userService.register("testuser", "test@example.com", "password123");
        
        // 更新用户状态到禁用(0)
        boolean updateResult = userService.updateStatus(user.getId(), 0);
        
        // 验证更新结果
        assertTrue(updateResult);
        
        // 获取更新后的用户并验证状态
        User updatedUser = userService.findByUsername("testuser");
        assertNotNull(updatedUser);
        assertEquals(0, updatedUser.getStatus());
    }
    
    @Test
    @DisplayName("测试更新不存在用户的状态")
    public void testUpdateNonExistentUserStatus() {
        // 尝试更新不存在的用户状态
        boolean updateResult = userService.updateStatus(999L, 0);
        
        // 验证更新失败
        assertFalse(updateResult);
    }
} 