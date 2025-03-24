package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.model.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);
    
    List<Registration> findByUserId(Long userId);
    
    List<Registration> findByEventId(Long eventId);
    
    List<Registration> findByStatus(String status);
    
    List<Registration> findByEventIdAndStatus(Long eventId, String status);
    
    List<Registration> findByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT r FROM Registration r JOIN FETCH r.event WHERE r.userId = :userId")
    List<Registration> findWithEventByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Registration r JOIN FETCH r.user WHERE r.eventId = :eventId")
    List<Registration> findWithUserByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT r FROM Registration r JOIN FETCH r.user JOIN FETCH r.event WHERE r.id = :id")
    Registration findWithUserAndEventById(@Param("id") Long id);
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.eventId = :eventId AND r.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);
    
    @Query("SELECT DISTINCT r.userId FROM Registration r WHERE r.eventId = :eventId AND r.status = :status")
    List<Long> findDistinctUserIdsByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);
} 