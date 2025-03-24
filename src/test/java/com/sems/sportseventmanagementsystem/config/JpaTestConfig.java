package com.sems.sportseventmanagementsystem.config;

import com.sems.sportseventmanagementsystem.repository.UserRepository;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.repository.RegistrationRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackages = "com.sems.sportseventmanagementsystem.repository")
public class JpaTestConfig {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private RegistrationRepository registrationRepository;
} 