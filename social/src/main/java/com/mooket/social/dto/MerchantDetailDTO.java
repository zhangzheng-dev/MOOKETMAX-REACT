package com.mooket.social.dto;

import lombok.Data;

import java.util.List;

@Data
public class MerchantDetailDTO {

    private Long merchantId;
    private String merchantName;
    private String merchantShortName;
    private String merchantTags;
    private String contactPhone;
    private Integer todayOfferCount;
    private Integer todayInquiryCount;
    private Integer todayProductCount;
    private Integer todayFactoryCount;
    private List<OfferSummaryDTO> offers;
    private List<OfferSummaryDTO> inquiries;
    private MerchantFilterOptionsDTO offerFilterOptions;
    private MerchantFilterOptionsDTO inquiryFilterOptions;
    private Integer totalOffers;
    private Integer totalInquiries;
}
