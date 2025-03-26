package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.entity.EventCategory;
import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import com.sems.sportseventmanagementsystem.repository.EventCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class EventCategoryController {
    @Autowired
    EventCategoryRepository categoryRepository;

    @GetMapping("/public")
    public ResponseEntity<List<EventCategory>> getAllPublicCategories() {
        List<EventCategory> categories = categoryRepository.findByIsActiveTrue();
        return ResponseEntity.ok(categories);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventCategory>> getAllCategories() {
        List<EventCategory> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody EventCategory category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Category name already exists!"));
        }
        
        category.setIsActive(true);
        EventCategory savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody EventCategory categoryDetails) {
        return categoryRepository.findById(id)
                .map(category -> {
                    if (categoryDetails.getName() != null && 
                            !category.getName().equalsIgnoreCase(categoryDetails.getName()) && 
                            categoryRepository.existsByNameIgnoreCase(categoryDetails.getName())) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Error: Category name already exists!"));
                    }
                    
                    if (categoryDetails.getName() != null) {
                        category.setName(categoryDetails.getName());
                    }
                    if (categoryDetails.getDescription() != null) {
                        category.setDescription(categoryDetails.getDescription());
                    }
                    if (categoryDetails.getIcon() != null) {
                        category.setIcon(categoryDetails.getIcon());
                    }
                    
                    return ResponseEntity.ok(categoryRepository.save(category));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    category.setIsActive(false);
                    categoryRepository.save(category);
                    return ResponseEntity.ok(new MessageResponse("Category disabled successfully!"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 