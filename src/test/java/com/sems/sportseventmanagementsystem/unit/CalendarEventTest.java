package com.sems.sportseventmanagementsystem.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 赛事日历功能测试
 */
public class CalendarEventTest {

    // 赛事类
    private static class CalendarEvent {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String location;
        private String status;
        
        public CalendarEvent(Long id, String name, String description, LocalDateTime startTime, 
                       LocalDateTime endTime, String location) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.status = "未开始";
        }
        
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
        
        public void setStatus(String status) { this.status = status; }
        
        public LocalDate getStartDate() {
            return startTime.toLocalDate();
        }
        
        public long getDurationInHours() {
            return ChronoUnit.HOURS.between(startTime, endTime);
        }
        
        public boolean isOngoing() {
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(startTime) && now.isBefore(endTime);
        }
        
        public boolean isInFuture() {
            return LocalDateTime.now().isBefore(startTime);
        }
        
        public boolean isInPast() {
            return LocalDateTime.now().isAfter(endTime);
        }
    }
    
    // 日历服务类
    private static class CalendarService {
        private List<CalendarEvent> events;
        
        public CalendarService() {
            this.events = new ArrayList<>();
        }
        
        public void addEvent(CalendarEvent event) {
            events.add(event);
        }
        
        public List<CalendarEvent> getEventsByDate(LocalDate date) {
            return events.stream()
                    .filter(event -> event.getStartDate().equals(date))
                    .collect(Collectors.toList());
        }
        
        public List<CalendarEvent> getEventsByMonth(int year, int month) {
            return events.stream()
                    .filter(event -> {
                        LocalDate startDate = event.getStartDate();
                        return startDate.getYear() == year && startDate.getMonthValue() == month;
                    })
                    .collect(Collectors.toList());
        }
        
        public Map<LocalDate, List<CalendarEvent>> getMonthlyCalendar(int year, int month) {
            Map<LocalDate, List<CalendarEvent>> calendar = new HashMap<>();
            LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
            int daysInMonth = firstDayOfMonth.lengthOfMonth();
            
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                List<CalendarEvent> eventsForDate = getEventsByDate(date);
                calendar.put(date, eventsForDate);
            }
            
            return calendar;
        }
        
        public List<CalendarEvent> getUpcomingEvents(int daysAhead) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusDays(daysAhead);
            
            return events.stream()
                    .filter(event -> {
                        return event.getStartTime().isAfter(now) && 
                               event.getStartTime().isBefore(future);
                    })
                    .collect(Collectors.toList());
        }
    }
    
    // 测试变量
    private CalendarService calendarService;
    private List<CalendarEvent> testEvents;
    
    @BeforeEach
    public void setUp() {
        calendarService = new CalendarService();
        testEvents = new ArrayList<>();
        
        // 添加测试赛事
        LocalDateTime now = LocalDateTime.now();
        
        // 今天的赛事
        testEvents.add(new CalendarEvent(
            1L, 
            "今日篮球赛", 
            "校内篮球友谊赛", 
            now.plusHours(1), 
            now.plusHours(3), 
            "体育馆A区"
        ));
        
        // 明天的赛事
        testEvents.add(new CalendarEvent(
            2L, 
            "明日足球赛", 
            "校际足球比赛", 
            now.plusDays(1), 
            now.plusDays(1).plusHours(2), 
            "足球场"
        ));
        
        // 下周的赛事
        testEvents.add(new CalendarEvent(
            3L, 
            "下周马拉松", 
            "城市半程马拉松", 
            now.plusDays(7), 
            now.plusDays(7).plusHours(5), 
            "市中心"
        ));
        
        // 下个月的赛事
        testEvents.add(new CalendarEvent(
            4L, 
            "月度游泳比赛", 
            "大学生游泳锦标赛", 
            now.plusMonths(1), 
            now.plusMonths(1).plusHours(4), 
            "游泳中心"
        ));
        
        // 将测试赛事添加到服务中
        testEvents.forEach(calendarService::addEvent);
    }
    
    @Test
    @DisplayName("测试按日期获取赛事")
    public void testGetEventsByDate() {
        // 获取今天的赛事
        LocalDate today = LocalDate.now();
        List<CalendarEvent> todayEvents = calendarService.getEventsByDate(today);
        
        // 应该有一个今天的赛事
        assertEquals(1, todayEvents.size());
        assertEquals("今日篮球赛", todayEvents.get(0).getName());
        
        // 获取明天的赛事
        LocalDate tomorrow = today.plusDays(1);
        List<CalendarEvent> tomorrowEvents = calendarService.getEventsByDate(tomorrow);
        
        // 应该有一个明天的赛事
        assertEquals(1, tomorrowEvents.size());
        assertEquals("明日足球赛", tomorrowEvents.get(0).getName());
        
        // 获取后天的赛事
        LocalDate dayAfterTomorrow = today.plusDays(2);
        List<CalendarEvent> dayAfterTomorrowEvents = calendarService.getEventsByDate(dayAfterTomorrow);
        
        // 应该没有后天的赛事
        assertEquals(0, dayAfterTomorrowEvents.size());
    }
    
    @Test
    @DisplayName("测试按月份获取赛事")
    public void testGetEventsByMonth() {
        // 获取本月的赛事
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        List<CalendarEvent> currentMonthEvents = calendarService.getEventsByMonth(currentYear, currentMonth);
        
        // 本月应该有今天、明天和下周的赛事（如果它们在同一个月）
        int expectedEventsInCurrentMonth = (int) testEvents.stream()
                .filter(event -> {
                    LocalDate startDate = event.getStartDate();
                    return startDate.getYear() == currentYear && startDate.getMonthValue() == currentMonth;
                })
                .count();
        
        assertEquals(expectedEventsInCurrentMonth, currentMonthEvents.size());
        
        // 获取下个月的赛事
        int nextMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        int nextMonthYear = currentMonth == 12 ? currentYear + 1 : currentYear;
        List<CalendarEvent> nextMonthEvents = calendarService.getEventsByMonth(nextMonthYear, nextMonth);
        
        // 下个月应该有一个赛事（如果月度游泳比赛在下个月）
        int expectedEventsInNextMonth = (int) testEvents.stream()
                .filter(event -> {
                    LocalDate startDate = event.getStartDate();
                    return startDate.getYear() == nextMonthYear && startDate.getMonthValue() == nextMonth;
                })
                .count();
        
        assertEquals(expectedEventsInNextMonth, nextMonthEvents.size());
    }
    
    @Test
    @DisplayName("测试获取未来赛事")
    public void testGetUpcomingEvents() {
        // 获取未来7天的赛事
        List<CalendarEvent> upcomingEvents = calendarService.getUpcomingEvents(7);
        
        // 应该包括今天和明天的赛事，但不包括下周的赛事
        int expectedCount = (int) testEvents.stream()
                .filter(event -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime future = now.plusDays(7);
                    return event.getStartTime().isAfter(now) && 
                           event.getStartTime().isBefore(future);
                })
                .count();
        
        assertEquals(expectedCount, upcomingEvents.size());
        assertTrue(upcomingEvents.stream().anyMatch(event -> event.getName().equals("今日篮球赛")));
        assertTrue(upcomingEvents.stream().anyMatch(event -> event.getName().equals("明日足球赛")));
    }
    
    @Test
    @DisplayName("测试赛事状态检查")
    public void testEventStatusCheck() {
        // 获取第一个赛事（今日篮球赛）
        CalendarEvent basketballEvent = testEvents.get(0);
        
        // 由于赛事设置为未来1小时开始，因此应该是未来赛事
        assertTrue(basketballEvent.isInFuture());
        assertFalse(basketballEvent.isOngoing());
        assertFalse(basketballEvent.isInPast());
        
        // 手动修改赛事时间到过去
        LocalDateTime now = LocalDateTime.now();
        CalendarEvent pastEvent = new CalendarEvent(
            5L, 
            "过去赛事", 
            "已经结束的赛事", 
            now.minusHours(3), 
            now.minusHours(1), 
            "体育馆B区"
        );
        
        // 验证状态检查
        assertFalse(pastEvent.isInFuture());
        assertFalse(pastEvent.isOngoing());
        assertTrue(pastEvent.isInPast());
    }
    
    @Test
    @DisplayName("测试获取月度日历")
    public void testGetMonthlyCalendar() {
        // 获取本月日历
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        Map<LocalDate, List<CalendarEvent>> monthlyCalendar = calendarService.getMonthlyCalendar(currentYear, currentMonth);
        
        // 验证日历包含本月所有日期
        int daysInMonth = LocalDate.of(currentYear, currentMonth, 1).lengthOfMonth();
        assertEquals(daysInMonth, monthlyCalendar.size());
        
        // 验证今天的日期包含正确的赛事
        LocalDate today = LocalDate.now();
        List<CalendarEvent> todayEvents = monthlyCalendar.get(today);
        assertEquals(1, todayEvents.size());
        assertEquals("今日篮球赛", todayEvents.get(0).getName());
    }
} 