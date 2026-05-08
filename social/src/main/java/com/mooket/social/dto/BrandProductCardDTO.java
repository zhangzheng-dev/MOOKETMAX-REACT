package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 品牌产品卡片
 * 显示内容：报价区间、涨跌、近30天趋势图、热门工厂x3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BrandProductCardDTO extends HomeCardItemDTO {
    /**
     * 品牌ID
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

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
     * 热门工厂列表（最多3个）
     */
    private List<HotFactoryDTO> hotFactories;

    /**
     * 趋势点DTO
     */
    @Data
    public static class TrendPointDTO {
        private String date;
        private Double avgPrice;
    }

    /**
     * 热门工厂DTO
     */
    @Data
    public static class HotFactoryDTO {
        private String factoryNo;
        private Integer offerCount;
        private BigDecimal priceMin;
        private BigDecimal priceMax;
    }
}
