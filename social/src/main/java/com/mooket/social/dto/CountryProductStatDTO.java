package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 国家产品维度统计DTO（用于SQL聚合结果）
 */
@Data
public class CountryProductStatDTO {
    private String country;
    private Integer productId;
    private String productName;
    private String category;
    private Integer todayOfferCount;
    private Integer todayInquiryCount;
    private Integer todayFactoryCount;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private BigDecimal avgPrice;
    private BigDecimal avgPriceYesterday;
}
