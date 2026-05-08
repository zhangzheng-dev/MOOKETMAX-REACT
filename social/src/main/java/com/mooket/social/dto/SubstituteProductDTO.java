package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 平替产品 DTO
 */
@Data
public class SubstituteProductDTO {

    private String category;
    private String productName;
    private String currentFactoryNo;
    private String tier;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Long offerCount;
    private Integer merchantCount;
    private List<SubstituteFactory> factories;

    /**
     * 平替厂号
     */
    @Data
    public static class SubstituteFactory {
        private String factoryNo;
        private BigDecimal priceMin;
        private BigDecimal priceMax;
        private Long offerCount;
        private Integer merchantCount;
        private boolean isSelected;
    }

    /**
     * 平替产品详情（带报盘数据）
     */
    @Data
    public static class SubstituteProductDetailDTO {
        private String country;
        private String factoryNo;
        private String productName;
        private String tier;
        private Integer productId;
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
        private Integer totalCount;
        private Integer page;
        private Integer pageSize;
        private Integer totalPages;
    }

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
        private boolean isFamousMerchant;
        private List<EmployeeOfferDTO> employeeOffers;
    }

    @Data
    public static class EmployeeOfferDTO {
        private Long offerId;
        private String userNickname;
        private String price;
        private String weight;
        private String goodsLocation;
        private String goodsType;
        private String tags;
        private String offerType;
        private String publishTime;
        private String offerOriginalText;
    }
}