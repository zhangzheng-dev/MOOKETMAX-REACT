package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 国家详情 DTO
 * 用于国家详情页
 */
@Data
public class CountryDetailDTO {

    // ========== 国家基本信息 ==========
    private String country;           // 国家名称（如"巴西"）

    // ========== 统计数据（近2日） ==========
    private Long offerCount;          // 报盘数
    private Integer merchantCount;    // 商家数
    private Integer factoryCount;     // 工厂数
    private BigDecimal priceMin;      // 价格区间最低（过滤异常值）
    private BigDecimal priceMax;      // 价格区间最高（过滤异常值）

    // ========== 热门厂号/产品（Top 3） ==========
    private List<HotFactoryDTO> hotFactories;   // 热门厂号
    private List<HotProductDTO> hotProducts;   // 热门产品

    // ========== 聚合列表（按产品分组） ==========
    private List<CountryProductSummaryDTO> summaries;

    // ========== 分页信息 ==========
    private Integer totalCount;       // 总条目数
    private Integer page;             // 当前页
    private Integer pageSize;         // 每页大小
    private Integer totalPages;        // 总页数
}
