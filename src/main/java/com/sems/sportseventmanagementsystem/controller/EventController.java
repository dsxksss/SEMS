package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.Event;
import com.sems.sportseventmanagementsystem.entity.EventStatus;
import com.sems.sportseventmanagementsystem.entity.User;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.repository.EventCategoryRepository;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventCategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/public")
    public ResponseEntity<Page<Event>> getAllPublicEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Event> events = eventRepository.findByIsActiveTrue(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<Event>> searchPublicEvents(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findByIsActiveTrueAndNameContainingIgnoreCase(name, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/public/category/{categoryId}")
    public ResponseEntity<Page<Event>> getEventsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return categoryRepository.findById(categoryId)
                .map(category -> {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<Event> events = eventRepository.findByIsActiveTrueAndCategory(category, pageable);
                    return ResponseEntity.ok(events);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/public/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);
        List<Event> events = eventRepository.findUpcomingEvents(now, nextWeek);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/public/status/{status}")
    public ResponseEntity<Page<Event>> getEventsByStatus(
            @PathVariable EventStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findByIsActiveTrueAndStatus(status, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return ResponseEntity.ok(events);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        
        event.setCreatedBy(user);
        event.setCurrentParticipants(0);
        event.setIsActive(true);
        event.setStatus(EventStatus.PENDING);
        
        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.ok(savedEvent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        return eventRepository.findById(id)
                .map(event -> {
                    if (eventDetails.getName() != null) {
                        event.setName(eventDetails.getName());
                    }
                    if (eventDetails.getDescription() != null) {
                        event.setDescription(eventDetails.getDescription());
                    }
                    if (eventDetails.getStartTime() != null) {
                        event.setStartTime(eventDetails.getStartTime());
                    }
                    if (eventDetails.getEndTime() != null) {
                        event.setEndTime(eventDetails.getEndTime());
                    }
                    if (eventDetails.getLocation() != null) {
                        event.setLocation(eventDetails.getLocation());
                    }
                    if (eventDetails.getMaxParticipants() != null) {
                        event.setMaxParticipants(eventDetails.getMaxParticipants());
                    }
                    if (eventDetails.getEventImage() != null) {
                        event.setEventImage(eventDetails.getEventImage());
                    }
                    if (eventDetails.getStatus() != null) {
                        event.setStatus(eventDetails.getStatus());
                    }
                    if (eventDetails.getCategory() != null) {
                        event.setCategory(eventDetails.getCategory());
                    }
                    
                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(event -> {
                    event.setIsActive(false);
                    eventRepository.save(event);
                    return ResponseEntity.ok(new MessageResponse("Event deleted successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Event>> getMyEvents() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Event> events = eventRepository.findByCreatedBy_Id(userDetails.getId());
        return ResponseEntity.ok(events);
    }
} 