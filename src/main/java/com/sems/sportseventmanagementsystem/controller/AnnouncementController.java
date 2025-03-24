package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.model.entity.Announcement;
import com.sems.sportseventmanagementsystem.service.AnnouncementService;
import com.sems.sportseventmanagementsystem.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*", allowCredentials = "false", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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
    public Result<List<Announcement>> getLatestAnnouncements() {
        return Result.success(announcementService.getLatestAnnouncements());
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        return Result.success(announcementService.createAnnouncement(announcement));
    }

    @PutMapping("/{id}")
    public Result<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        return Result.success(announcementService.updateAnnouncement(announcement));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
} 