package com.sems.sportseventmanagementsystem.security;

import com.sems.sportseventmanagementsystem.model.entity.User;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        User userEntity = user.get();
        
        return new UserDetailsImpl(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getEmail(),
                userEntity.getRealName(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole()))
        );
    }
} 