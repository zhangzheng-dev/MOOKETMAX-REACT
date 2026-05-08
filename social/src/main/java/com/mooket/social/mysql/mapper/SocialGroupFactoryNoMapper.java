package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SocialGroupFactoryNo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SocialGroupFactoryNoMapper {

    @Select("SELECT id, group_name, group_alias, factory_no, goods_category FROM social_group_factory_no")
    List<SocialGroupFactoryNo> selectAllActive();

    @Select("SELECT id, group_name, group_alias, factory_no, goods_category FROM social_group_factory_no WHERE deleted = 0 AND goods_category = #{goodsCategory}")
    List<SocialGroupFactoryNo> selectActiveByCategory(@Param("goodsCategory") Integer goodsCategory);
}
