package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.EventResult;
import com.sems.sportseventmanagementsystem.entity.User;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.repository.EventResultRepository;
import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/results")
public class EventResultController {
    @Autowired
    EventResultRepository resultRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/public/event/{eventId}")
    public ResponseEntity<List<EventResult>> getPublicEventResults(@PathVariable Long eventId) {
        List<EventResult> results = resultRepository.findByEventIdOrderByRankAsc(eventId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/public/athlete/{athleteId}")
    public ResponseEntity<List<EventResult>> getPublicAthleteResults(@PathVariable Long athleteId) {
        List<EventResult> results = resultRepository.findByAthleteId(athleteId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EventResult>> getEventResults(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<EventResult> results = resultRepository.findByEventId(eventId, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addEventResult(@PathVariable Long eventId, @RequestBody EventResult result) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    User user = userRepository.findById(userDetails.getId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    if (resultRepository.existsByEventIdAndAthleteId(eventId, result.getAthlete().getId())) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Error: Result for this athlete already exists for this event!"));
                    }
                    
                    result.setEvent(event);
                    result.setRecordedBy(user);
                    EventResult savedResult = resultRepository.save(result);
                    return ResponseEntity.ok(savedResult);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEventResult(@PathVariable Long id, @RequestBody EventResult resultDetails) {
        return resultRepository.findById(id)
                .map(result -> {
                    if (resultDetails.getRank() != null) {
                        result.setRank(resultDetails.getRank());
                    }
                    if (resultDetails.getScore() != null) {
                        result.setScore(resultDetails.getScore());
                    }
                    if (resultDetails.getRemarks() != null) {
                        result.setRemarks(resultDetails.getRemarks());
                    }
                    
                    return ResponseEntity.ok(resultRepository.save(result));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEventResult(@PathVariable Long id) {
        return resultRepository.findById(id)
                .map(result -> {
                    resultRepository.delete(result);
                    return ResponseEntity.ok(new MessageResponse("Result deleted successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<List<EventResult>> getMyResults() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<EventResult> results = resultRepository.findByAthleteId(userDetails.getId());
        return ResponseEntity.ok(results);
    }
} 