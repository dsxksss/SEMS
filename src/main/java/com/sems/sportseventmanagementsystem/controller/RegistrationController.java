package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.Event;
import com.sems.sportseventmanagementsystem.entity.Registration;
import com.sems.sportseventmanagementsystem.entity.RegistrationStatus;
import com.sems.sportseventmanagementsystem.entity.User;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.repository.RegistrationRepository;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    @Autowired
    RegistrationRepository registrationRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Registration>> getAllRegistrations() {
        List<Registration> registrations = registrationRepository.findAll();
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Registration>> getRegistrationsByEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Registration> registrations = registrationRepository.findByEventId(eventId, pageable);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/event/{eventId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Registration>> getRegistrationsByEventAndStatus(
            @PathVariable Long eventId,
            @PathVariable RegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Registration> registrations = registrationRepository.findByEventIdAndStatus(eventId, status, pageable);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Registration>> getMyRegistrations() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Registration> registrations = registrationRepository.findByUserId(userDetails.getId());
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/my/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Registration>> getMyRegistrationsByStatus(
            @PathVariable RegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<Registration> registrations = registrationRepository.findByUserIdAndStatus(userDetails.getId(), status, pageable);
        return ResponseEntity.ok(registrations);
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ATHLETE', 'SPECTATOR')")
    @Transactional
    public ResponseEntity<?> registerForEvent(@PathVariable Long eventId, @RequestBody Registration registrationRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (registrationRepository.existsByUserIdAndEventId(userDetails.getId(), eventId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You are already registered for this event!"));
        }
        
        return eventRepository.findById(eventId)
                .map(event -> {
                    if (event.getMaxParticipants() != null && event.getCurrentParticipants() >= event.getMaxParticipants()) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Event is already full!"));
                    }
                    
                    User user = userRepository.findById(userDetails.getId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    Registration registration = new Registration();
                    registration.setUser(user);
                    registration.setEvent(event);
                    registration.setStatus(RegistrationStatus.PENDING);
                    registration.setRemarks(registrationRequest.getRemarks());
                    registration.setContactPhone(registrationRequest.getContactPhone());
                    registration.setHasPaid(false);
                    
                    registrationRepository.save(registration);
                    
                    event.setCurrentParticipants(event.getCurrentParticipants() + 1);
                    eventRepository.save(event);
                    
                    return ResponseEntity.ok(new MessageResponse("Registered for event successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateRegistrationStatus(
            @PathVariable Long id,
            @RequestParam RegistrationStatus status) {

        return registrationRepository.findById(id)
                .map(registration -> {
                    RegistrationStatus oldStatus = registration.getStatus();
                    registration.setStatus(status);
                    registrationRepository.save(registration);
                    
                    // Update event participants count if needed
                    Event event = registration.getEvent();
                    if (oldStatus != RegistrationStatus.APPROVED && status == RegistrationStatus.APPROVED) {
                        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
                        eventRepository.save(event);
                    } else if (oldStatus == RegistrationStatus.APPROVED && status != RegistrationStatus.APPROVED) {
                        event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
                        eventRepository.save(event);
                    }
                    
                    return ResponseEntity.ok(new MessageResponse("Registration status updated successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id, @RequestParam boolean hasPaid) {
        return registrationRepository.findById(id)
                .map(registration -> {
                    registration.setHasPaid(hasPaid);
                    registrationRepository.save(registration);
                    return ResponseEntity.ok(new MessageResponse("Payment status updated successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id, 'Registration')")
    @Transactional
    public ResponseEntity<?> cancelRegistration(@PathVariable Long id) {
        return registrationRepository.findById(id)
                .map(registration -> {
                    if (registration.getStatus() == RegistrationStatus.APPROVED) {
                        Event event = registration.getEvent();
                        event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
                        eventRepository.save(event);
                    }
                    
                    registration.setStatus(RegistrationStatus.CANCELLED);
                    registrationRepository.save(registration);
                    
                    return ResponseEntity.ok(new MessageResponse("Registration cancelled successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 