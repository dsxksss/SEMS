package com.sems.sportseventmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long id;
    private String name;
    private String description;
    private Date startTime;
    private Date endTime;
    private String location;
    private Integer status; // 0-未开始, 1-进行中, 2-已结束
    private Date createTime;
    private Date updateTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
} 