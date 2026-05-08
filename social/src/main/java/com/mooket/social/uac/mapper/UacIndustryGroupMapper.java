package com.mooket.social.uac.mapper;

import com.mooket.social.entity.uac.UacIndustryGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MySQL uac_industry_group 表 Mapper (mallee_muji_uac)
 */
@Mapper
public interface UacIndustryGroupMapper {

    /**
     * 查询所有数据（全量同步用）
     */
    @Select("SELECT id, industry_group_name, industry_group_name_abbreviation, created_time, update_time " +
            "FROM uac_industry_group")
    List<UacIndustryGroup> selectAll();

    /**
     * 查询增量数据（根据更新时间筛选）
     */
    @Select("SELECT id, industry_group_name, industry_group_name_abbreviation, created_time, update_time " +
            "FROM uac_industry_group WHERE update_time > #{lastSyncTime}")
    List<UacIndustryGroup> selectActiveAfter(@Param("lastSyncTime") LocalDateTime lastSyncTime);
}
