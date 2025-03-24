package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByStatus(Integer status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.id = :id")
    void updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword);
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
} 