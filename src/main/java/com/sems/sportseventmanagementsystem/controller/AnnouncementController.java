package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.Announcement;
import com.sems.sportseventmanagementsystem.entity.User;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.repository.AnnouncementRepository;
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

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/public")
    public ResponseEntity<Page<Announcement>> getAllPublicAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Announcement> announcements = announcementRepository.findByIsPublishedTrue(pageable);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/public/latest")
    public ResponseEntity<List<Announcement>> getLatestAnnouncements() {
        List<Announcement> announcements = announcementRepository.findTop5ByIsPublishedTrueOrderByCreatedAtDesc();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/public/event/{eventId}")
    public ResponseEntity<Page<Announcement>> getPublicAnnouncementsByEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Announcement> announcements = announcementRepository.findByEventIdAndIsPublishedTrue(eventId, pageable);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAnnouncementById(@PathVariable Long id) {
        return announcementRepository.findById(id)
                .map(announcement -> {
                    // 非管理员访问时增加浏览量
                    boolean isAdmin = SecurityContextHolder.getContext().getAuthentication() != null &&
                            SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
                    if (!isAdmin && announcement.getIsPublished()) {
                        announcement.setViewCount(announcement.getViewCount() + 1);
                        announcement = announcementRepository.save(announcement);
                    }
                    return ResponseEntity.ok(announcement);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        announcement.setCreatedBy(user);
        if (announcement.getIsPublished() == null) {
            announcement.setIsPublished(true);
        }
        announcement.setViewCount(0);
        
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return ResponseEntity.ok(savedAnnouncement);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcementDetails) {
        return announcementRepository.findById(id)
                .map(announcement -> {
                    if (announcementDetails.getTitle() != null) {
                        announcement.setTitle(announcementDetails.getTitle());
                    }
                    if (announcementDetails.getContent() != null) {
                        announcement.setContent(announcementDetails.getContent());
                    }
                    if (announcementDetails.getIsPublished() != null) {
                        announcement.setIsPublished(announcementDetails.getIsPublished());
                    }
                    if (announcementDetails.getType() != null) {
                        announcement.setType(announcementDetails.getType());
                    }
                    if (announcementDetails.getEvent() != null) {
                        announcement.setEvent(announcementDetails.getEvent());
                    }
                    if (announcementDetails.getAttachments() != null) {
                        announcement.setAttachments(announcementDetails.getAttachments());
                    }
                    
                    return ResponseEntity.ok(announcementRepository.save(announcement));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        return announcementRepository.findById(id)
                .map(announcement -> {
                    announcementRepository.delete(announcement);
                    return ResponseEntity.ok(new MessageResponse("Announcement deleted successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleAnnouncementPublished(@PathVariable Long id, @RequestBody PublishRequest request) {
        return announcementRepository.findById(id)
                .map(announcement -> {
                    announcement.setIsPublished(request.isPublished());
                    Announcement savedAnnouncement = announcementRepository.save(announcement);
                    return ResponseEntity.ok(savedAnnouncement);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    static class PublishRequest {
        private boolean published;

        public boolean isPublished() {
            return published;
        }

        public void setPublished(boolean published) {
            this.published = published;
        }
    }
} 