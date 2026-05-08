package com.mooket.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 厂号详情 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryDetailDTO {

    private Integer factoryId;
    private String country;
    private String countryAlias;    // 国家别名（用于显示）
    private String factoryNo;
    private Integer productCount;   // 产品数
    private Integer inquiryCount;   // 求购数
    private Integer recentOfferCount; // 近2日报盘数

    private List<FactoryProductDTO> products;
    private Integer totalCount;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FactoryProductDTO {
        private Integer productId;
        private String productName;
        private Double priceMin;
        private Double priceMax;
        private List<String> merchantNames;  // 商家名称列表（最多显示3个）
        private Integer merchantCount;       // 商家数
        private Integer offerCount;         // 报盘数
    }
}
