package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.repository.EventRepository;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Override
    @Transactional
    public Event createEvent(EventDTO eventDTO) {
        Event event = new Event();
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setLocation(eventDTO.getLocation());
        event.setStatus(eventDTO.getStatus());
        event.setMaxParticipants(eventDTO.getMaxParticipants());
        event.setCurrentParticipants(0);
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event updateEvent(Long id, EventDTO eventDTO) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("赛事不存在，ID: " + id));

        if (eventDTO.getName() != null) {
            event.setName(eventDTO.getName());
        }
        if (eventDTO.getDescription() != null) {
            event.setDescription(eventDTO.getDescription());
        }
        if (eventDTO.getStartTime() != null) {
            event.setStartTime(eventDTO.getStartTime());
        }
        if (eventDTO.getEndTime() != null) {
            event.setEndTime(eventDTO.getEndTime());
        }
        if (eventDTO.getLocation() != null) {
            event.setLocation(eventDTO.getLocation());
        }
        if (eventDTO.getStatus() != null) {
            event.setStatus(eventDTO.getStatus());
        }
        if (eventDTO.getMaxParticipants() != null) {
            event.setMaxParticipants(eventDTO.getMaxParticipants());
        }
        event.setUpdateTime(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public boolean deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("赛事不存在，ID: " + id));

        eventRepository.deleteById(id);
        return true;
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("赛事不存在，ID: " + id));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> getEventsByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return eventRepository.findByPage(pageable);
    }

    @Override
    public List<Event> getEventsByStatus(String status) {
        return eventRepository.findByStatus(status);
    }

    @Override
    public List<Event> getUpcomingEvents(int days) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(days);
        return eventRepository.findByTimeRange(start, end);
    }

    @Override
    public List<Event> getLatestEvents(int limit) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "startTime"));
        return eventRepository.findAll(pageable).getContent();
    }
} 