package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.model.entity.Announcement;
import com.sems.sportseventmanagementsystem.service.AnnouncementService;
import com.sems.sportseventmanagementsystem.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public Result<List<Announcement>> getAllAnnouncements() {
        return Result.success(announcementService.getAllAnnouncements());
    }

    @GetMapping("/active")
    public Result<List<Announcement>> getActiveAnnouncements() {
        return Result.success(announcementService.getActiveAnnouncements());
    }

    @GetMapping("/type/{type}")
    public Result<List<Announcement>> getAnnouncementsByType(@PathVariable String type) {
        return Result.success(announcementService.getAnnouncementsByType(type));
    }

    @GetMapping("/latest")
    public Result<List<Announcement>> getLatestAnnouncements(
            @RequestParam(defaultValue = "5") int limit) {
        return Result.success(announcementService.getLatestAnnouncements(limit));
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        return Result.success(announcementService.createAnnouncement(announcement));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        return Result.success(announcementService.updateAnnouncement(announcement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
} 