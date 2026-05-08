package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 厂号维度统计DTO（用于SQL聚合结果）
 */
@Data
public class FactoryStatDTO {
    private String country;
    private String factoryNo;
    private Integer factoryId;
    private String category;
    private Integer todayOfferCount;
    private Integer todayInquiryCount;
    private Integer todayMerchantCount;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
}
