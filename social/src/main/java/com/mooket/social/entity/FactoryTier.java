package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 厂号等级表
 */
@Data
@TableName("factory_tier")
public class FactoryTier {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String category;

    private String productName;

    private String factoryNo;

    private String tier;

    private String country;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}