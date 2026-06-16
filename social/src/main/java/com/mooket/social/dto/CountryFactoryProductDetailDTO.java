package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CountryFactoryProductDetailDTO {

    private String country;
    private String factoryNo;
    private Integer productId;
    private String productName;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private BigDecimal priceChange;
    private BigDecimal priceChangeRate;
    private Long offerCount;
    private Long inquiryCount;
    private Integer merchantCount;
    private List<DailyPrice> priceHistory7Days;
    private List<DailyPrice> priceHistory30Days;
    private List<MerchantOfferGroup> merchantOffers;
    private GroupedOfferFilterOptionsDTO filterOptions;
    private Integer totalCount;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Boolean hasSubstitute;

    @Data
    public static class DailyPrice {
        private String date;
        private String fullDate;
        private BigDecimal avgPrice;
        private String priceUnit;
        private Integer offerCount;
    }

    @Data
    public static class MerchantOfferGroup {
        private Long merchantId;
        private String merchantName;
        private String merchantPhone;
        private Integer offerCount;
        private Boolean isFamousMerchant;
        private List<EmployeeOfferDTO> employeeOffers;
    }

    @Data
    public static class EmployeeOfferDTO {
        private Long offerId;
        private String userNickname;
        private String contactPhone;
        private String price;
        private String weight;
        private String goodsLocation;
        private String goodsType;
        private String feedingType;
        private String fatRatio;
        private String cattleBreed;
        private String tags;
        private String remark;
        private String offerType;
        private String publishTime;
        private String offerOriginalText;
    }
}
