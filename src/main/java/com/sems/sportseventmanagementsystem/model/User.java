package com.sems.sportseventmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private String role; // ADMIN, ATHLETE, SPECTATOR
    private Date createTime;
    private Date updateTime;
    private Integer status; // 0-禁用, 1-正常
} 