package com.sems.sportseventmanagementsystem.service.impl;

import com.sems.sportseventmanagementsystem.exception.ResourceNotFoundException;
import com.sems.sportseventmanagementsystem.mapper.EventMapper;
import com.sems.sportseventmanagementsystem.model.dto.EventDTO;
import com.sems.sportseventmanagementsystem.model.entity.Event;
import com.sems.sportseventmanagementsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper eventMapper;

    @Override
    @Transactional
    public Event createEvent(EventDTO eventDTO) {
        Event event = convertToEntity(eventDTO);
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());
        event.setCurrentParticipants(0);
        eventMapper.insert(event);
        return event;
    }

    @Override
    @Transactional
    public Event updateEvent(Long id, EventDTO eventDTO) {
        Event existingEvent = eventMapper.selectById(id);
        if (existingEvent == null) {
            throw new ResourceNotFoundException("赛事不存在，ID: " + id);
        }
        
        Event updatedEvent = convertToEntity(eventDTO);
        updatedEvent.setId(id);
        updatedEvent.setUpdateTime(LocalDateTime.now());
        updatedEvent.setCreateTime(existingEvent.getCreateTime());
        updatedEvent.setCurrentParticipants(existingEvent.getCurrentParticipants());
        
        eventMapper.update(updatedEvent);
        return updatedEvent;
    }

    @Override
    @Transactional
    public boolean deleteEvent(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            throw new ResourceNotFoundException("赛事不存在，ID: " + id);
        }
        return eventMapper.delete(id) > 0;
    }

    @Override
    public Event getEventById(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            throw new ResourceNotFoundException("赛事不存在，ID: " + id);
        }
        return event;
    }

    @Override
    public List<Event> getAllEvents() {
        return eventMapper.selectAll();
    }

    @Override
    public List<Event> getEventsByPage(int page, int size) {
        int offset = (page - 1) * size;
        return eventMapper.selectByPage(offset, size);
    }

    @Override
    public List<Event> getEventsByStatus(String status) {
        return eventMapper.selectByStatus(status);
    }

    @Override
    public List<Event> getUpcomingEvents(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plus(days, ChronoUnit.DAYS);
        return eventMapper.selectByTimeRange(now, future);
    }
    
    private Event convertToEntity(EventDTO dto) {
        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setLocation(dto.getLocation());
        event.setStatus(dto.getStatus());
        event.setMaxParticipants(dto.getMaxParticipants());
        return event;
    }
} 