package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 国家维度统计实体
 */
@Data
@TableName("stat_country")
public class StatCountry {

    @TableId(type = IdType.AUTO)
    private Integer statId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 大类：牛/猪
     */
    private String category;

    /**
     * 国家
     */
    private String country;

    /**
     * 今日报盘数
     */
    private Integer todayOfferCount;

    /**
     * 今日求购数
     */
    private Integer todayInquiryCount;

    /**
     * 今日活跃厂号数
     */
    private Integer todayFactoryCount;

    /**
     * 今日报盘商家数
     */
    private Integer todayMerchantCount;

    /**
     * 热门厂号（JSON格式：[{"factoryNo":"xxx","offerCount":10},...])
     */
    private String hotFactories;

    /**
     * 热门产品（JSON格式：[{"productName":"xxx","offerCount":10},...])
     */
    private String hotProducts;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
