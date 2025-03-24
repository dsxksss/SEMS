package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.model.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    List<Announcement> findByTypeAndStatusOrderByCreateTimeDesc(String type, Integer status);
    
    List<Announcement> findByStatusOrderByCreateTimeDesc(Integer status);
    
    List<Announcement> findTop5ByStatusOrderByCreateTimeDesc(Integer status);
} 