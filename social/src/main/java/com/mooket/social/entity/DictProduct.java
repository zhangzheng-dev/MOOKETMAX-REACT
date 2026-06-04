package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品字典实体
 */
@Data
@TableName("dict_product")
public class DictProduct {

    @TableId(type = IdType.AUTO)
    private Integer productId;
    private Long sourceGoodsId;

    private String category;       // 大类（牛/猪）

    private String productName;    // 产品标准名称

    private String aliasList;      // 产品别名列表

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
