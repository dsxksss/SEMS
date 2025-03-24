package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.model.entity.Announcement;
import com.sems.sportseventmanagementsystem.repository.AnnouncementRepository;
import com.sems.sportseventmanagementsystem.service.AnnouncementService;
import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Override
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    @Override
    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public List<Announcement> getAnnouncementsByType(String type) {
        return announcementRepository.findByTypeAndStatusOrderByCreateTimeDesc(type, 1);
    }

    @Override
    public List<Announcement> getLatestAnnouncements() {
        return announcementRepository.findTop5ByStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在，ID: " + id));
    }

    @Override
    public Announcement createAnnouncement(Announcement announcement) {
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcement.setStatus(1);
        return announcementRepository.save(announcement);
    }

    @Override
    public Announcement updateAnnouncement(Announcement announcement) {
        Announcement existingAnnouncement = getAnnouncementById(announcement.getId());
        
        existingAnnouncement.setTitle(announcement.getTitle());
        existingAnnouncement.setContent(announcement.getContent());
        existingAnnouncement.setType(announcement.getType());
        existingAnnouncement.setStatus(announcement.getStatus());
        existingAnnouncement.setUpdateTime(LocalDateTime.now());
        
        return announcementRepository.save(existingAnnouncement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = getAnnouncementById(id);
        announcementRepository.delete(announcement);
    }
} 