package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品详情 DTO
 * 用于产品详情页（搜索结果/产品聚合页）
 */
@Data
public class ProductDetailDTO {

    // ========== 产品基本信息 ==========
    private Integer productId;
    private String productName;      // 产品标准名称
    private String category;          // 大类（牛/猪）

    // ========== 统计数据（近2日） ==========
    private Long offerCount;         // 报盘数
    private BigDecimal priceMin;     // 价格区间最低
    private BigDecimal priceMax;     // 价格区间最高
    private Integer merchantCount;    // 商家数
    private Integer factoryCount;    // 工厂数

    // ========== 聚合列表（按国家厂号分组） ==========
    private List<ProductSummaryDTO> summaries;

    // ========== 分页信息 ==========
    private Integer totalCount;      // 总条目数
    private Integer page;            // 当前页
    private Integer pageSize;        // 每页大小
    private Integer totalPages;      // 总页数
}
