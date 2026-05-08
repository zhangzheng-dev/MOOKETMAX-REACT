package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialOnlineBusiness;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MySQL 源数据 Mapper
 */
@Mapper
public interface SocialOnlineBusinessMapper {

    /**
     * 查询最近2天已上架的数据（首次同步）
     */
    @Select("SELECT * FROM social_online_business " +
            "WHERE status = 3 AND is_deleted = 0 AND display_flag = 1 " +
            "AND update_time >= #{startTime} " +
            "ORDER BY id")
    List<SocialOnlineBusiness> selectRecentTwoDays(@Param("startTime") LocalDateTime startTime);

    /**
     * 全量查询所有已上架数据（首次全量同步）
     */
    @Select("SELECT * FROM social_online_business " +
            "WHERE status = 3 AND is_deleted = 0 AND display_flag = 1 " +
            "ORDER BY id")
    List<SocialOnlineBusiness> selectAll();

    /**
     * 查询增量数据（基于更新时间）
     */
    @Select("SELECT * FROM social_online_business " +
            "WHERE status = 3 AND is_deleted = 0 AND display_flag = 1 " +
            "AND update_time > #{lastSyncTime} " +
            "ORDER BY id")
    List<SocialOnlineBusiness> selectIncrementData(@Param("lastSyncTime") LocalDateTime lastSyncTime);

    /**
     * 查询指定ID列表的数据
     */
    @Select("SELECT * FROM social_online_business " +
            "WHERE id IN (#{ids}) AND status = 3 AND is_deleted = 0")
    List<SocialOnlineBusiness> selectByIds(@Param("ids") String ids);
}
