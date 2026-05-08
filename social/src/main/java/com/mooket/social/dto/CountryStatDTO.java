package com.mooket.social.dto;

import lombok.Data;

/**
 * 国家维度统计DTO（用于SQL聚合结果）
 */
@Data
public class CountryStatDTO {
    private String country;
    private String category;
    private Integer todayOfferCount;
    private Integer todayInquiryCount;
    private Integer todayFactoryCount;
    private Integer todayMerchantCount;
}
