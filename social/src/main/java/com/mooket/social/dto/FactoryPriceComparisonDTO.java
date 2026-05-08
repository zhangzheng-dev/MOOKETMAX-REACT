package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 厂号价格对比 DTO
 */
@Data
public class FactoryPriceComparisonDTO {

    /**
     * 国家
     */
    private String country;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 品类
     */
    private String category;

    /**
     * 报盘/求购类型
     */
    private String offerType;

    /**
     * 厂号对比数据列表
     */
    private List<FactoryTrendData> factories;

    /**
     * 单个厂号的价格趋势数据
     */
    @Data
    public static class FactoryTrendData {
        /**
         * 厂号
         */
        private String factoryNo;

        /**
         * 趋势点列表
         */
        private List<TrendPoint> trend;

        /**
         * 均价（用于排序）
         */
        private BigDecimal avgPrice;
    }

    /**
     * 趋势点
     */
    @Data
    public static class TrendPoint {
        /**
         * 日期 (MM-dd 格式)
         */
        private String date;

        /**
         * 日期完整格式 (yyyy-MM-dd)
         */
        private String fullDate;

        /**
         * 当日均价
         */
        private BigDecimal avgPrice;

        /**
         * 当日报盘数
         */
        private Integer offerCount;

        public TrendPoint() {}

        public TrendPoint(java.time.LocalDate date, BigDecimal avgPrice) {
            this.date = date.toString().substring(5); // MM-dd
            this.fullDate = date.toString();
            this.avgPrice = avgPrice;
        }

        public TrendPoint(java.time.LocalDate date, BigDecimal avgPrice, Integer offerCount) {
            this.date = date.toString().substring(5); // MM-dd
            this.fullDate = date.toString();
            this.avgPrice = avgPrice;
            this.offerCount = offerCount;
        }
    }
}
