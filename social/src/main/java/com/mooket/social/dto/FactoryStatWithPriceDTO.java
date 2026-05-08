package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 工厂维度统计DTO（带价格，用于首页卡片）
 */
@Data
public class FactoryStatWithPriceDTO {
    private String factoryNo;
    private Integer todayOfferCount;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
}
