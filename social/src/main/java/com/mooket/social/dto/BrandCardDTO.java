package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 品牌卡片
 * 显示内容：品牌LOGO、今日报盘数、产品数、工厂数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BrandCardDTO extends HomeCardItemDTO {
    /**
     * 品牌ID
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 今日报盘数
     */
    private Integer todayOfferCount;

    /**
     * 产品数
     */
    private Integer productCount;

    /**
     * 工厂数
     */
    private Integer factoryCount;
}
