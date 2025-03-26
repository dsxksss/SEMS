package com.sems.sportseventmanagementsystem.repository;

import com.sems.sportseventmanagementsystem.entity.ERole;
import com.sems.sportseventmanagementsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
} 