package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialOnlineBusinessContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MySQL 业务内容表 Mapper
 */
@Mapper
public interface SocialOnlineBusinessContentMapper {

    /**
     * 根据ID列表批量查询
     */
    @Select("<script>" +
            "SELECT id, content FROM social_online_business_content WHERE id IN " +
            "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<SocialOnlineBusinessContent> selectByIds(@Param("ids") List<Long> ids);
}
