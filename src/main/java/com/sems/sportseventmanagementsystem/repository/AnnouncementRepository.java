package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Page<Announcement> findByIsPublishedTrue(Pageable pageable);
    
    Page<Announcement> findByEventIdAndIsPublishedTrue(Long eventId, Pageable pageable);
    
    List<Announcement> findTop5ByIsPublishedTrueOrderByCreatedAtDesc();
} 