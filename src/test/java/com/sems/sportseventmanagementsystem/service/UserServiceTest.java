package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.model.dto.UserDTO;
import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private UserDTO mockUserDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 创建模拟用户
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encoded_password");
        mockUser.setEmail("test@example.com");
        mockUser.setPhone("13800138000");
        mockUser.setRealName("测试用户");
        mockUser.setRole("USER");
        mockUser.setStatus(1);
        mockUser.setCreateTime(LocalDateTime.now());
        mockUser.setUpdateTime(LocalDateTime.now());

        // 创建模拟用户DTO
        mockUserDTO = new UserDTO();
        mockUserDTO.setUsername("testuser");
        mockUserDTO.setPassword("password");
        mockUserDTO.setEmail("test@example.com");
        mockUserDTO.setPhone("13800138000");
        mockUserDTO.setRealName("测试用户");
        mockUserDTO.setRole("USER");
        mockUserDTO.setStatus(1);
    }

    @Test
    void getUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getUserByIdNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(1L);
        });

        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void getUserByUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void getUserByUsernameNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByUsername("testuser");
        });

        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void getAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(mockUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void createUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User result = userService.createUser(mockUserDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUserUsernameExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(mockUserDTO);
        });

        assertTrue(exception.getMessage().contains("用户名已存在"));
    }

    @Test
    void createUserEmailExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.createUser(mockUserDTO);
        });

        assertTrue(exception.getMessage().contains("邮箱已被注册"));
    }

    @Test
    void updateUser() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPhone("13811138111");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User result = userService.updateUser(1L, updateDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("13811138111", result.getPhone());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(1L, mockUserDTO);
        });

        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void deleteUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        doNothing().when(userRepository).deleteById(anyLong());

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void changePassword() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        boolean result = userService.changePassword(1L, "oldPassword", "newPassword");

        assertTrue(result);
        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePasswordUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.changePassword(1L, "oldPassword", "newPassword");
        });

        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void changePasswordIncorrectOldPassword() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        boolean result = userService.changePassword(1L, "wrongPassword", "newPassword");

        assertFalse(result);
        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserStatus() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User result = userService.updateUserStatus(1L, 0);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserStatusNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserStatus(1L, 0);
        });

        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    void checkUsernameExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        boolean result = userService.checkUsernameExists("testuser");

        assertTrue(result);
        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    void checkEmailExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        boolean result = userService.checkEmailExists("test@example.com");

        assertTrue(result);
        verify(userRepository, times(1)).findByEmail(anyString());
    }
} 