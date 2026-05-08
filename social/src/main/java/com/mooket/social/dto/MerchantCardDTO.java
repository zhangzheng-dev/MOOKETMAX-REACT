package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 商家卡片
 * 显示内容：商家标签、最新2个报盘、今日报盘数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MerchantCardDTO extends HomeCardItemDTO {
    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 商家简称
     */
    private String merchantShortName;

    /**
     * 商家标签
     */
    private String merchantTags;

    /**
     * 今日报盘数
     */
    private Integer todayOfferCount;

    /**
     * 最新报盘列表（最多2个）
     */
    private List<LatestOfferDTO> latestOffers;

    /**
     * 最新报盘DTO
     */
    @Data
    public static class LatestOfferDTO {
        private String productName;
        private String country;
        private String factoryNo;
        private Double price;
        private String weight;
        private String publishTime;
    }
}
