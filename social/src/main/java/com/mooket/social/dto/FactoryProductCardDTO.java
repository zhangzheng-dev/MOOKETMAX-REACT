package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 国家厂号产品卡片
 * 显示内容：报价区间、涨跌、趋势图、热门商家x3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FactoryProductCardDTO extends HomeCardItemDTO {
    /**
     * 国家
     */
    private String country;

    /**
     * 国家别名（用于显示国旗）
     */
    private String countryAlias;

    /**
     * 厂号
     */
    private String factoryNo;

    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 报价区间-最低
     */
    private BigDecimal priceMin;

    /**
     * 报价区间-最高
     */
    private BigDecimal priceMax;

    /**
     * 涨跌值
     */
    private BigDecimal priceChange;

    /**
     * 涨跌幅百分比
     */
    private BigDecimal priceChangeRate;

    /**
     * 近30天趋势数据（用于迷你趋势图）
     */
    private List<TrendPointDTO> trendPoints;

    /**
     * 热门商家列表（最多3个）
     */
    private List<HotMerchantDTO> hotMerchants;

    /**
     * 趋势点DTO
     */
    @Data
    public static class TrendPointDTO {
        private String date;
        private Double avgPrice;
    }

    /**
     * 热门商家DTO
     */
    @Data
    public static class HotMerchantDTO {
        private Long merchantId;
        private String merchantName;
        private Integer offerCount;
        private BigDecimal priceMin;
        private BigDecimal priceMax;
    }

    /**
     * 今日报盘数
     */
    private Integer todayOfferCount;

    /**
     * 今日求购数
     */
    private Integer inquiryCount;
}
