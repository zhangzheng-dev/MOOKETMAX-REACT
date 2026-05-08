package com.mooket.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 近30天价格趋势表
 */
@Data
@TableName("stat_price_trend")
public class StatPriceTrend {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期（历史日期或当天）
     */
    private LocalDate statDate;

    /**
     * 维度类型: country_product / country_factory_product
     */
    private String dimensionType;

    /**
     * 国家
     */
    private String country;

    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 厂号（为空表示国家+产品维度）
     */
    private String factoryNo;

    /**
     * 报盘/求购类型: 报盘 / 求购
     */
    private String offerType;

    /**
     * 当日均价
     */
    private BigDecimal avgPrice;

    /**
     * 记录日期（系统当前日期）
     */
    private LocalDate recordDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
