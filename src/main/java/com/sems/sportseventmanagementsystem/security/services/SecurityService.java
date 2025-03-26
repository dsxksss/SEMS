package com.sems.sportseventmanagementsystem.security.services;

import com.sems.sportseventmanagementsystem.entity.Registration;
import com.sems.sportseventmanagementsystem.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    @Autowired
    private RegistrationRepository registrationRepository;

    public boolean isCurrentUser(Long userId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId().equals(userId);
    }
    
    public boolean isOwner(Long objectId, String objectType) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getId();
        
        switch(objectType) {
            case "Registration":
                Optional<Registration> registration = registrationRepository.findById(objectId);
                return registration.isPresent() && registration.get().getUser().getId().equals(currentUserId);
            // Add more object types as needed
            default:
                return false;
        }
    }
} 