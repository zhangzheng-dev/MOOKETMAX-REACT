package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 国家厂号维度统计实体
 */
@Data
@TableName("stat_factory")
public class StatFactory {

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
     * 厂号
     */
    private String factoryNo;

    /**
     * 厂号ID
     */
    private Integer factoryId;

    /**
     * 今日报盘数
     */
    private Integer todayOfferCount;

    /**
     * 今日求购数
     */
    private Integer todayInquiryCount;

    /**
     * 今日报盘商家数
     */
    private Integer todayMerchantCount;

    /**
     * 今日最低价
     */
    private BigDecimal priceMin;

    /**
     * 今日最高价
     */
    private BigDecimal priceMax;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
