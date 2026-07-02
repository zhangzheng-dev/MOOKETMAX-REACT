package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialOnlineBusinessTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MySQL source mapper for social_online_business_tag.
 * 仅按当前同步窗口的报盘 id 批量查询标签，不触碰历史数据。
 */
@Mapper
public interface SocialOnlineBusinessTagMapper {

    /**
     * 按 online_business_id 批量查询标签。
     * 用 foreach 拼 IN，命中 idx_online_business_id，避免 N+1。
     */
    @Select({"<script>",
            "SELECT online_business_id AS onlineBusinessId, business_tag AS businessTag",
            "FROM social_online_business_tag",
            "WHERE business_tag IS NOT NULL AND business_tag != ''",
            "AND online_business_id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"})
    List<SocialOnlineBusinessTag> selectByBusinessIds(@Param("ids") List<Long> ids);
}
