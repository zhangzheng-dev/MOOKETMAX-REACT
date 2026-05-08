package com.mooket.social.mysql.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * MySQL uac dict 表 Mapper (mallee.muji.uac)
 */
@Mapper
public interface UacDictMapper {

    /**
     * 根据 dict_name_en 和 dict_key 查询 dict_value
     */
    @Select("SELECT dict_value FROM mallee_muji_uac.dict " +
            "WHERE dict_name_en = #{dictNameEn} AND dict_key = #{dictKey}")
    String selectDictValueByNameEnAndKey(@Param("dictNameEn") String dictNameEn, @Param("dictKey") String dictKey);
}
