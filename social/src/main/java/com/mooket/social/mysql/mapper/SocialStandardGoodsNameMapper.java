package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialStandardGoodsName;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MySQL 标准产品名表 Mapper
 */
@Mapper
public interface SocialStandardGoodsNameMapper {

    /**
     * 查询所有产品（用于全量同步 dict_product）
     */
    @Select("SELECT id, standard_goods_name, goods_category, created_time, update_time " +
            "FROM social_standard_goods_name")
    List<SocialStandardGoodsName> selectAll();

    /**
     * 根据ID列表批量查询
     */
    @Select("<script>" +
            "SELECT id, standard_goods_name, goods_category, created_time, update_time " +
            "FROM social_standard_goods_name WHERE id IN " +
            "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<SocialStandardGoodsName> selectByIds(@Param("ids") List<Long> ids);
}
