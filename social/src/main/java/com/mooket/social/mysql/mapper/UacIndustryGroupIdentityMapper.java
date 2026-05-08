package com.mooket.social.mysql.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * MySQL uac_industry_group_identity 表 Mapper (mallee.muji.uac)
 */
@Mapper
public interface UacIndustryGroupIdentityMapper {

    /**
     * 根据 industry_group_id 查询 industry_identity
     */
    @Select("SELECT industry_identity FROM mallee_muji_uac.uac_industry_group_identity " +
            "WHERE industry_group_id = #{industryGroupId}")
    String selectIndustryIdentityByGroupId(@Param("industryGroupId") Integer industryGroupId);
}
