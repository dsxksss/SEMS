package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.entity.Registration;
import com.sems.sportseventmanagementsystem.entity.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserId(Long userId);
    
    List<Registration> findByEventId(Long eventId);
    
    Page<Registration> findByEventId(Long eventId, Pageable pageable);
    
    Page<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status, Pageable pageable);
    
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);
    
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = ?1 AND r.status = ?2")
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
    
    Page<Registration> findByUserIdAndStatus(Long userId, RegistrationStatus status, Pageable pageable);
} 