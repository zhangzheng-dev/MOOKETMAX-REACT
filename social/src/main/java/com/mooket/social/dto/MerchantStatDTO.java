package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商家维度统计DTO（用于SQL聚合结果）
 */
@Data
public class MerchantStatDTO {
    private Long merchantId;
    private Integer todayOfferCount;
    private Integer todayInquiryCount;
    private Integer todayProductCount;
    private Integer todayFactoryCount;
}
