package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.model.entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(String status);
    
    @Query("SELECT e FROM Event e WHERE e.startTime >= :startTime AND e.startTime <= :endTime ORDER BY e.startTime ASC")
    List<Event> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT e FROM Event e ORDER BY e.startTime DESC")
    List<Event> findByPage(Pageable pageable);
} 