package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.entity.Event;
import com.sems.sportseventmanagementsystem.entity.EventCategory;
import com.sems.sportseventmanagementsystem.entity.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByIsActiveTrue(Pageable pageable);
    
    Page<Event> findByIsActiveTrueAndStatus(EventStatus status, Pageable pageable);
    
    Page<Event> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Event> findByIsActiveTrueAndCategory(EventCategory category, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.startTime BETWEEN ?1 AND ?2")
    List<Event> findUpcomingEvents(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.startTime > ?1")
    Page<Event> findFutureEvents(LocalDateTime now, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.startTime < ?1 AND e.endTime > ?1")
    Page<Event> findOngoingEvents(LocalDateTime now, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.endTime < ?1")
    Page<Event> findPastEvents(LocalDateTime now, Pageable pageable);
    
    List<Event> findByCreatedBy_Id(Long userId);
} 