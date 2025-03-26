package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.entity.EventResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventResultRepository extends JpaRepository<EventResult, Long> {
    List<EventResult> findByEventId(Long eventId);
    
    Page<EventResult> findByEventId(Long eventId, Pageable pageable);
    
    List<EventResult> findByEventIdOrderByRankAsc(Long eventId);
    
    List<EventResult> findByAthleteId(Long athleteId);
    
    Optional<EventResult> findByEventIdAndAthleteId(Long eventId, Long athleteId);
    
    @Query("SELECT er FROM EventResult er JOIN er.event e WHERE e.category.id = ?1")
    List<EventResult> findByCategoryId(Long categoryId);
    
    boolean existsByEventIdAndAthleteId(Long eventId, Long athleteId);
} 