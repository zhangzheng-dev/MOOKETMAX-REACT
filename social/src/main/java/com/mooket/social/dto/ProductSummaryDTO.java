package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品汇总 DTO
 * 用于产品详情页中的聚合列表项（按"国家+厂号"分组）
 */
@Data
public class ProductSummaryDTO {

    // ========== 国家厂号信息 ==========
    private String country;          // 国家（如"巴西"）
    private String factoryNo;        // 厂号（如"SIF1440"）
    private String countryFactory;   // 组合显示："巴西 SIF1440"

    // ========== 价格信息 ==========
    private BigDecimal priceMin;     // 价格区间最低
    private BigDecimal priceMax;     // 价格区间最高

    // ========== 商家列表（去重，显示前几个） ==========
    private List<String> merchantNames;   // 商家名称列表

    // ========== 统计数据 ==========
    private Integer merchantCount;    // 商家数
    private Integer offerCount;      // 报盘数
}
