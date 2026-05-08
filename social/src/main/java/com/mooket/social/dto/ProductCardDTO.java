package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 产品卡片
 * 显示内容：今日报盘量、商家数、工厂数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCardDTO extends HomeCardItemDTO {
    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 商家数
     */
    private Integer merchantCount;

    /**
     * 工厂数
     */
    private Integer factoryCount;

    /**
     * 价格区间-最低
     */
    private BigDecimal priceMin;

    /**
     * 价格区间-最高
     */
    private BigDecimal priceMax;
}
