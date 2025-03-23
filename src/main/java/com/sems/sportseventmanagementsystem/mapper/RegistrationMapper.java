package com.sems.sportseventmanagementsystem.mapper;

import com.sems.sportseventmanagementsystem.model.entity.Registration;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistrationMapper {
    
    /**
     * 插入报名记录
     * @param registration 报名信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO registration(user_id, event_id, registration_type, status, register_time, update_time, remark) " +
            "VALUES(#{userId}, #{eventId}, #{registrationType}, #{status}, #{registerTime}, #{updateTime}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Registration registration);
    
    /**
     * 更新报名记录
     * @param registration 报名信息
     * @return 影响的行数
     */
    @Update("UPDATE registration SET status = #{status}, update_time = #{updateTime}, remark = #{remark} WHERE id = #{id}")
    int update(Registration registration);
    
    /**
     * 删除报名记录
     * @param id 报名ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM registration WHERE id = #{id}")
    int delete(Long id);
    
    /**
     * 根据ID查询报名记录
     * @param id 报名ID
     * @return 报名记录
     */
    @Select("SELECT * FROM registration WHERE id = #{id}")
    Registration selectById(Long id);
    
    /**
     * 根据用户ID查询报名记录
     * @param userId 用户ID
     * @return 报名记录列表
     */
    @Select("SELECT * FROM registration WHERE user_id = #{userId}")
    List<Registration> selectByUserId(Long userId);
    
    /**
     * 根据赛事ID查询报名记录
     * @param eventId 赛事ID
     * @return 报名记录列表
     */
    @Select("SELECT * FROM registration WHERE event_id = #{eventId}")
    List<Registration> selectByEventId(Long eventId);
    
    /**
     * 根据状态查询报名记录
     * @param status 状态
     * @return 报名记录列表
     */
    @Select("SELECT * FROM registration WHERE status = #{status}")
    List<Registration> selectByStatus(String status);
    
    /**
     * 根据用户ID和赛事ID查询报名记录
     * @param userId 用户ID
     * @param eventId 赛事ID
     * @return 报名记录
     */
    @Select("SELECT * FROM registration WHERE user_id = #{userId} AND event_id = #{eventId}")
    Registration selectByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    /**
     * 查询带有用户和赛事信息的报名记录
     * @param id 报名ID
     * @return 报名记录（包含用户和赛事信息）
     */
    @Select("SELECT * FROM registration WHERE id = #{id}")
    Registration selectWithUserAndEventById(Long id);
    
    /**
     * 查询用户的报名记录（包含赛事信息）
     * @param userId 用户ID
     * @return 报名记录列表
     */
    @Select("SELECT * FROM registration WHERE user_id = #{userId}")
    List<Registration> selectWithEventByUserId(Long userId);
    
    /**
     * 查询赛事的报名记录（包含用户信息）
     * @param eventId 赛事ID
     * @return 报名记录列表
     */
    @Select("SELECT * FROM registration WHERE event_id = #{eventId}")
    List<Registration> selectWithUserByEventId(Long eventId);
} 