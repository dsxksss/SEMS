package com.sems.sportseventmanagementsystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
public class SimplePingController {

    @GetMapping
    public String ping() {
        return "Pong!";
    }
} 