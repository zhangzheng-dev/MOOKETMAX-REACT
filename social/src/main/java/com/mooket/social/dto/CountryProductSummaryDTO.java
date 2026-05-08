package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 国家详情页 - 产品聚合汇总 DTO
 */
@Data
public class CountryProductSummaryDTO {

    // ========== 产品信息 ==========
    private Integer productId;        // 产品ID
    private String productName;        // 产品名称

    // ========== 价格区间 ==========
    private BigDecimal priceMin;       // 价格区间最低
    private BigDecimal priceMax;       // 价格区间最高

    // ========== 聚合数据 ==========
    private List<String> factoryNos;  // 厂号列表（去重）
    private Integer factoryCount;     // 厂号数量
    private Integer offerCount;       // 报盘数量
}
