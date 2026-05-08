package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 国家+产品详情 DTO
 * 用于国家+产品详情页
 */
@Data
public class CountryProductDetailDTO {

    // ========== 国家+产品基本信息 ==========
    private String country;           // 国家名称（如"巴西"）
    private Integer productId;        // 产品ID
    private String productName;      // 产品名称（如"牛前八件套"）

    // ========== 价格区间（近2日有效价格） ==========
    private BigDecimal priceMin;      // 价格区间最低
    private BigDecimal priceMax;      // 价格区间最高

    // ========== 日均价涨跌 ==========
    private BigDecimal priceChange;    // 相比昨日涨跌值
    private BigDecimal priceChangeRate; // 涨跌幅百分比

    // ========== 统计数据（近2日） ==========
    private Long offerCount;          // 报盘数
    private Long inquiryCount;        // 求购数
    private Integer merchantCount;    // 商家数

    // ========== 7日报价走势 ==========
    private List<DailyPrice> priceHistory7Days;  // 近7日价格走势

    // ========== 近30日价格趋势 ==========
    private List<DailyPrice> priceHistory30Days;  // 近30日价格趋势

    // ========== 厂号聚合列表 ==========
    private List<CountryProductFactoryDTO> factories;

    // ========== 分页信息 ==========
    private Integer totalCount;       // 总厂号数
    private Integer page;             // 当前页
    private Integer pageSize;         // 每页大小
    private Integer totalPages;        // 总页数

    /**
     * 单日价格数据
     */
    @Data
    public static class DailyPrice {
        private String date;          // 日期（如"04-21"）
        private String fullDate;     // 完整日期（如"2026-04-21"）
        private BigDecimal avgPrice; // 日均价
        private String priceUnit;    // 价格单位（如"元/kg"）
        private Integer offerCount;  // 当日报盘数
    }

    /**
     * 厂号聚合数据
     */
    @Data
    public static class CountryProductFactoryDTO {
        private String country;        // 国家
        private String factoryNo;     // 厂号
        private String countryFactory; // 组合显示："国家 厂号"
        private BigDecimal priceMin;  // 价格区间最低
        private BigDecimal priceMax;  // 价格区间最高
        private List<String> merchantNames; // 商家名称列表（取前3个）
        private Integer merchantCount; // 商家数量
        private Integer offerCount;  // 报盘数
    }
}
