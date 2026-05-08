package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 品牌字典实体
 */
@Data
@TableName("dict_brand")
public class DictBrand {

    @TableId(type = IdType.AUTO)
    private Integer brandId;

    private String brandName;      // 品牌名称

    private String category;        // 大类（牛/猪）

    private String aliasList;       // 别名列表

    private Integer factoryId;      // 厂号ID

    private String factoryNo;       // 厂号

    private String country;         // 国家

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
