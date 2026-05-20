package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 国家+厂号+产品详情 DTO
 * 用于国家+厂号+产品详情页
 */
@Data
public class CountryFactoryProductDetailDTO {

    // ========== 基本信息 ==========
    private String country;           // 国家名称（如"巴西"）
    private String factoryNo;         // 厂号（如"SIF1440"）
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

    // ========== 报盘列表（分页，按商家分组） ==========
    private List<MerchantOfferGroup> merchantOffers;    // 按商家分组的报盘列表

    // ========== 分页信息 ==========
    private Integer totalCount;        // 总报盘数
    private Integer page;             // 当前页
    private Integer pageSize;         // 每页大小
    private Integer totalPages;       // 总页数

    // ========== 平替产品标识 ==========
    private Boolean hasSubstitute;    // 是否有平替产品

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
     * 商家报盘分组
     * 一个商家可能有多条员工报价，展示为一个可展开的分组
     */
    @Data
    public static class MerchantOfferGroup {
        private Long merchantId;          // 商家ID（员工报价时为null）
        private String merchantName;     // 商家名称
        private String merchantPhone;    // 商家电话
        private Integer offerCount;     // 该商家下的报盘数
        private Boolean isFamousMerchant; // 是否知名商家
        private List<EmployeeOfferDTO> employeeOffers;  // 员工报价列表
    }

    /**
     * 员工报价数据
     */
    @Data
    public static class EmployeeOfferDTO {
        private Long offerId;         // 报盘ID
        private String userNickname;  // 用户昵称
        private String contactPhone;  // 联系电话
        private String price;         // 价格
        private String weight;        // 重量
        private String goodsLocation; // 货物所在地
        private String goodsType;    // 货物类型
        private String tags;         // 标签列表
        private String offerType;    // 报盘类型：报盘/求购
        private String publishTime;  // 发布时间
        private String offerOriginalText; // 原文内容
    }
}
