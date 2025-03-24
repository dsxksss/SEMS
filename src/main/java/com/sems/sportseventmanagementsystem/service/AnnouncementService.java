package com.sems.sportseventmanagementsystem.service;

import com.sems.sportseventmanagementsystem.model.entity.Announcement;
import java.util.List;

public interface AnnouncementService {
    
    List<Announcement> getAllAnnouncements();
    
    List<Announcement> getActiveAnnouncements();
    
    List<Announcement> getAnnouncementsByType(String type);
    
    List<Announcement> getLatestAnnouncements();
    
    Announcement getAnnouncementById(Long id);
    
    Announcement createAnnouncement(Announcement announcement);
    
    Announcement updateAnnouncement(Announcement announcement);
    
    void deleteAnnouncement(Long id);
} 