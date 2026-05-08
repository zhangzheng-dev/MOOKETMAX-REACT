package com.mooket.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mooket.social.entity.FactoryTier;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
/* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/mapper/FactoryTierMapper.class */
public interface FactoryTierMapper extends BaseMapper<FactoryTier> {
    @Select({"SELECT factory_no FROM factory_tier WHERE category = #{category} AND product_name = #{productName} AND tier = #{tier}"})
    List<String> selectFactoryNosByTier(@Param("category") String category, @Param("productName") String productName, @Param("tier") String tier);

    @Select({"SELECT tier FROM factory_tier WHERE category = #{category} AND product_name = #{productName} AND factory_no = #{factoryNo} LIMIT 1"})
    String selectTierByFactoryNo(@Param("category") String category, @Param("productName") String productName, @Param("factoryNo") String factoryNo);
}