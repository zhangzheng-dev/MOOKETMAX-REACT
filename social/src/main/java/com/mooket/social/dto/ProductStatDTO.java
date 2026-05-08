package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 产品维度统计DTO（用于SQL聚合结果）
 */
@Data
public class ProductStatDTO {
    private Integer productId;
    private String productName;
    private String category;
    private Integer todayOfferCount;
    private Integer todayInquiryCount;
    private Integer todayMerchantCount;
    private Integer todayFactoryCount;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
}
