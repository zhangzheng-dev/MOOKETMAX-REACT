package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 品牌产品汇总 DTO
 * 用于品牌详情页的产品列表
 */
@Data
public class BrandProductSummaryDTO {

    private Integer productId;          // 产品ID
    private String productName;         // 产品名称
    private BigDecimal priceMin;        // 价格区间最低
    private BigDecimal priceMax;        // 价格区间最高
    private String factoryNos;          // 厂号列表（逗号分隔）
    private Integer factoryCount;        // 厂号数量
    private Integer offerCount;         // 报盘数量

    // ========== 国家厂号信息（按 country + factory 分组时使用）==========
    private String country;             // 国家（如"巴西"）
    private String factoryNo;           // 厂号（如"SIF1440"）
    private String countryFactory;      // 组合显示："巴西 SIF1440"

    // ========== 商家列表（去重）==========
    private List<String> merchantNames; // 商家名称列表
    private Integer merchantCount;      // 商家数
}