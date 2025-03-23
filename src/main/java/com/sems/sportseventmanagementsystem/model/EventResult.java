package com.sems.sportseventmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResult {
    private Long id;
    private Long eventId;
    private Long userId;
    private Integer rank;
    private String score;
    private String remark;
    private Date createTime;
    private Date updateTime;
} 