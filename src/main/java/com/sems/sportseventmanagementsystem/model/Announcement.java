package com.sems.sportseventmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private Long creatorId;
    private Date createTime;
    private Date updateTime;
    private Integer status; // 0-草稿, 1-已发布, 2-已下架
} 