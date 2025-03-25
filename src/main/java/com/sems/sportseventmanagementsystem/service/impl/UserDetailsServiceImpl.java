package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("未找到用户名: " + username));

        return UserDetailsImpl.build(user);
    }
    
    // 通过用户ID加载用户
    @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("未找到用户ID: " + id));

        return UserDetailsImpl.build(user);
    }
}