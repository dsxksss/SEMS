package com.sems.sportseventmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;
    
    @Column(length = 50)
    private String displayName;
    
    @Column(length = 200)
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", length = 50)
    private List<String> permissions = new ArrayList<>();

    public Role(ERole name) {
        this.name = name;
    }
    
    public Role(ERole name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
} 