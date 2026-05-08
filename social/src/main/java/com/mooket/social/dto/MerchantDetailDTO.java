package com.mooket.social.dto;

import lombok.Data;

import java.util.List;

/**
 * 商家详情 DTO
 */
@Data
public class MerchantDetailDTO {

    private Long merchantId;
    private String merchantName;
    private String merchantShortName;
    private String merchantTags;
    private String contactPhone;

    // 统计数据
    private Integer todayOfferCount;
    private Integer todayInquiryCount;
    private Integer todayProductCount;
    private Integer todayFactoryCount;

    // 报盘列表
    private List<OfferSummaryDTO> offers;

    // 求购列表
    private List<OfferSummaryDTO> inquiries;

    // 分页信息（用于前端分页）
    private Integer totalOffers;
    private Integer totalInquiries;
}
