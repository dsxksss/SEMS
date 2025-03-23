package com.sems.sportseventmanagementsystem.mapper;

import com.sems.sportseventmanagementsystem.model.entity.Event;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EventMapper {
    
    /**
     * 插入新赛事
     * @param event 赛事信息
     * @return 影响的行数
     */
    int insert(Event event);
    
    /**
     * 更新赛事信息
     * @param event 更新后的赛事信息
     * @return 影响的行数
     */
    int update(Event event);
    
    /**
     * 删除赛事
     * @param id 赛事ID
     * @return 影响的行数
     */
    int delete(Long id);
    
    /**
     * 根据ID查询赛事
     * @param id 赛事ID
     * @return 赛事信息
     */
    Event selectById(Long id);
    
    /**
     * 查询所有赛事
     * @return 赛事列表
     */
    List<Event> selectAll();
    
    /**
     * 分页查询赛事
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 赛事列表
     */
    List<Event> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 根据状态查询赛事
     * @param status 赛事状态
     * @return 符合状态的赛事列表
     */
    List<Event> selectByStatus(@Param("status") String status);
    
    /**
     * 根据时间范围查询赛事
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 符合时间范围的赛事列表
     */
    List<Event> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
} 