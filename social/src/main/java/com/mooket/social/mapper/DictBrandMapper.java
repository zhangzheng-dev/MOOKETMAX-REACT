package com.mooket.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mooket.social.entity.DictBrand;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
/* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/mapper/DictBrandMapper.class */
public interface DictBrandMapper extends BaseMapper<DictBrand> {
    @Select({"SELECT * FROM dict_brand WHERE REPLACE(brand_name, ' ', '') LIKE CONCAT('%', REPLACE(#{keyword}, ' ', ''), '%') OR REPLACE(alias_list, ' ', '') LIKE CONCAT('%', REPLACE(#{keyword}, ' ', ''), '%')"})
    List<DictBrand> searchByKeyword(@Param("keyword") String keyword);

    @Select({"SELECT * FROM dict_brand"})
    List<DictBrand> selectAll();

    @Select({"SELECT * FROM dict_brand WHERE factory_id = #{factoryId}"})
    List<DictBrand> selectByFactoryId(@Param("factoryId") Integer factoryId);

    @Select({"SELECT * FROM dict_brand WHERE REPLACE(factory_no, ' ', '') = REPLACE(#{factoryNo}, ' ') AND category = #{category}"})
    List<DictBrand> selectByFactoryNoAndCategory(@Param("factoryNo") String factoryNo, @Param("category") String category);

    @Select({"SELECT * FROM dict_brand WHERE REPLACE(brand_name, ' ', '') = REPLACE(#{brandName}, ' ', '')"})
    List<DictBrand> selectByName(@Param("brandName") String brandName);

    @Select({"SELECT * FROM dict_brand WHERE REPLACE(brand_name, ' ', '') = REPLACE(#{brandName}, ' ', '') LIMIT 1"})
    Optional<DictBrand> findByName(@Param("brandName") String brandName);
}