package com.sems.sportseventmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
    private Long id;
    private Long userId;
    private Long eventId;
    private String registrationType; // ATHLETE, SPECTATOR
    private Integer status; // 0-待审核, 1-已通过, 2-已拒绝
    private Date registerTime;
    private Date updateTime;
    private String remark;
} 