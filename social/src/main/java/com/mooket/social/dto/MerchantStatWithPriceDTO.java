package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商家维度统计DTO（带价格，用于首页卡片）
 */
@Data
public class MerchantStatWithPriceDTO {
    private Long merchantId;
    private String merchantName;
    private Integer todayOfferCount;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
}
