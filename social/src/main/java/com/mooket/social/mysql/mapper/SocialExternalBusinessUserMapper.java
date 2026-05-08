package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialExternalBusinessUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MySQL social_external_business_user 表 Mapper (mallee_muji_social)
 */
@Mapper
public interface SocialExternalBusinessUserMapper {

    /**
     * 查询所有数据（全量同步用）
     */
    @Select("SELECT id, mobile_no, nick_name, industry_group_id, created_time, update_time " +
            "FROM social_external_business_user")
    List<SocialExternalBusinessUser> selectAll();

    /**
     * 查询增量数据（根据更新时间筛选）
     */
    @Select("SELECT id, mobile_no, nick_name, industry_group_id, created_time, update_time " +
            "FROM social_external_business_user WHERE update_time > #{lastSyncTime}")
    List<SocialExternalBusinessUser> selectActiveAfter(@Param("lastSyncTime") LocalDateTime lastSyncTime);
}
