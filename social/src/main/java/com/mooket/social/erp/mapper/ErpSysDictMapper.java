package com.mooket.social.erp.mapper;

import com.mooket.social.entity.mysql.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ERP 数据库 sys_dict 表 Mapper (mallee_muji_erp)
 */
@Mapper
public interface ErpSysDictMapper {

    /**
     * 批量获取字典值
     */
    @Select("SELECT dict_key, dict_value, dict_name_en FROM sys_dict " +
            "WHERE dict_name_en = #{dictNameEn}")
    List<SysDict> selectByDictNameEn(@Param("dictNameEn") String dictNameEn);
}
