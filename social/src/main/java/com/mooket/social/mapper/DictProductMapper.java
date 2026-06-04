package com.mooket.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mooket.social.entity.DictProduct;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
/* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/mapper/DictProductMapper.class */
public interface DictProductMapper extends BaseMapper<DictProduct> {
    @Select({"SELECT * FROM dict_product WHERE REPLACE(product_name, ' ', '') = REPLACE(#{productName}, ' ', '')"})
    DictProduct selectByProductName(@Param("productName") String productName);

    @Select({"SELECT * FROM dict_product WHERE source_goods_id = #{sourceGoodsId}"})
    DictProduct selectBySourceGoodsId(@Param("sourceGoodsId") Long sourceGoodsId);

    @Select({"SELECT * FROM dict_product WHERE category = #{category}"})
    List<DictProduct> selectByCategory(@Param("category") String category);

    @Select({"SELECT * FROM dict_product"})
    List<DictProduct> selectAll();

    @Select({"SELECT * FROM dict_product WHERE category = #{category} AND (REPLACE(product_name, ' ', '') LIKE CONCAT('%', REPLACE(#{keyword}, ' ', ''), '%') OR REPLACE(alias_list, ' ', '') LIKE CONCAT('%', REPLACE(#{keyword}, ' ', ''), '%'))"})
    List<DictProduct> searchByKeyword(@Param("category") String category, @Param("keyword") String keyword);

    @Select({"SELECT * FROM dict_product WHERE category = #{category} AND product_name = #{productName}"})
    DictProduct findByName(@Param("category") String category, @Param("productName") String productName);
}
