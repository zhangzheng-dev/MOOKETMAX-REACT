package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 厂号字典实体
 */
@Data
@TableName("dict_factory")
public class DictFactory {

    @TableId(type = IdType.AUTO)
    private Integer factoryId;

    private String category;       // 大类（牛/猪）

    private String country;         // 国家

    private String countryAlias;    // 国家别名

    private String factoryNo;       // 厂号

    private Integer brandId;       // 品牌ID

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
