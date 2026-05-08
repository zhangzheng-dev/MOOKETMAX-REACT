package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 价格趋势 DTO
 */
@Data
public class PriceTrendDTO {

    /**
     * 维度类型
     */
    private String dimensionType;

    /**
     * 国家
     */
    private String country;

    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 厂号
     */
    private String factoryNo;

    /**
     * 报盘/求购类型
     */
    private String offerType;

    /**
     * 价格趋势点列表
     */
    private List<TrendPoint> trend;

    /**
     * 价格趋势点
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

        public TrendPoint() {}

        public TrendPoint(LocalDate date, BigDecimal avgPrice) {
            this.date = date.toString().substring(5); // MM-dd
            this.fullDate = date.toString();
            this.avgPrice = avgPrice;
        }
    }
}
