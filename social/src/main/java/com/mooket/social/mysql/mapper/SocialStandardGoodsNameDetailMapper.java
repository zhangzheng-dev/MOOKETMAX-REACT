package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialStandardGoodsNameDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * MySQL 标准产品名详情表 Mapper (用于获取别名)
 */
@Mapper
public interface SocialStandardGoodsNameDetailMapper {

    /**
     * 根据产品ID列表查询所有别名
     */
    @Select("<script>" +
            "SELECT id, standard_goods_name_id, associated_goods_name " +
            "FROM social_standard_goods_name_detail WHERE standard_goods_name_id IN " +
            "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<SocialStandardGoodsNameDetail> selectByGoodsNameIds(@Param("ids") List<Long> ids);

    /**
     * 通过 SQL JOIN 直接按产品名聚合别名（避免 ID 截断问题）
     * 返回 Map: productName → comma-separated aliases
     */
    @Select("SELECT g.standard_goods_name AS productName, d.associated_goods_name AS aliasName " +
            "FROM social_standard_goods_name_detail d " +
            "JOIN social_standard_goods_name g ON d.standard_goods_name_id = g.id " +
            "WHERE d.associated_goods_name IS NOT NULL AND d.associated_goods_name != ''")
    List<ProductAliasDTO> selectAliasJoinByProductName();
}
