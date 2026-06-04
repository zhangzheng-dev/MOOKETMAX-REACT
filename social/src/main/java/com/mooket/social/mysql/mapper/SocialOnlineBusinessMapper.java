package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialOnlineBusiness;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MySQL source mapper for social_online_business.
 */
@Mapper
public interface SocialOnlineBusinessMapper {

    @Select("SELECT * FROM social_online_business " +
            "WHERE status = 3 AND is_deleted = 0 AND display_flag = 1 " +
            "AND update_time >= #{startTime} " +
            "ORDER BY id")
    List<SocialOnlineBusiness> selectRecentTwoDays(@Param("startTime") LocalDateTime startTime);

    @Select("SELECT * FROM social_online_business " +
            "WHERE status = 3 AND is_deleted = 0 AND display_flag = 1 " +
            "ORDER BY id")
    List<SocialOnlineBusiness> selectAll();

    @Select("SELECT * FROM social_online_business " +
            "WHERE status = 3 AND is_deleted = 0 AND display_flag = 1 " +
            "AND update_time > #{lastSyncTime} " +
            "ORDER BY id")
    List<SocialOnlineBusiness> selectIncrementData(@Param("lastSyncTime") LocalDateTime lastSyncTime);

    @Select("SELECT id FROM social_online_business " +
            "WHERE status = 3 AND is_deleted = 0 AND display_flag = 1 " +
            "AND offer_date >= #{startTime} " +
            "ORDER BY id")
    List<Long> selectActiveIdsByOfferDate(@Param("startTime") LocalDateTime startTime);

    @Select("SELECT * FROM social_online_business " +
            "WHERE id IN (#{ids}) AND status = 3 AND is_deleted = 0")
    List<SocialOnlineBusiness> selectByIds(@Param("ids") String ids);
}
