package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家字典实体
 */
@Data
@TableName("dict_merchant")
public class DictMerchant {

    @TableId(type = IdType.AUTO)
    private Long merchantId;

    private String merchantName;       // 商家全称

    private String merchantShortName;  // 商家简称

    private String merchantTags;        // 商家标签

    private String contactPhone;       // 联系电话

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
