package com.mooket.social.mysql.mapper;

import com.mooket.social.entity.mysql.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MySQL 字典表 Mapper
 */
@Mapper
public interface SysDictMapper {

    /**
     * 根据字典类型和key获取值
     */
    @Select("SELECT dict_key, dict_value, dict_name_en FROM sys_dict " +
            "WHERE dict_name_en = #{dictNameEn} AND dict_key = #{dictKey}")
    SysDict selectByDictNameEnAndKey(@Param("dictNameEn") String dictNameEn, @Param("dictKey") String dictKey);

    /**
     * 批量获取字典值
     */
    @Select("SELECT dict_key, dict_value, dict_name_en FROM sys_dict " +
            "WHERE dict_name_en = #{dictNameEn}")
    List<SysDict> selectByDictNameEn(@Param("dictNameEn") String dictNameEn);
}
