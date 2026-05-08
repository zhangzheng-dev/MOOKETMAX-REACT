package com.mooket.social.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 品牌详情 DTO
 * 用于品牌详情页
 */
@Data
public class BrandDetailDTO {

    // ========== 品牌基本信息 ==========
    private String brandName;          // 品牌名称

    // ========== 统计数据（近2日） ==========
    private Long todayOfferCount;       // 今日报盘数
    private Long yesterdayOfferCount;   // 昨日报盘数
    private Long totalOfferCount;       // 近2日报盘总数
    private Long todayInquiryCount;      // 今日求购数
    private Long yesterdayInquiryCount;  // 昨日求购数
    private Long totalInquiryCount;      // 近2日求购总数
private Integer factoryCount;       // 工厂数（该品牌下的不同 brand_id 数量）
    private Integer productCount;       // 产品数
    private BigDecimal priceMin;       // 价格区间最低
    private BigDecimal priceMax;       // 价格区间最高
    private Integer merchantCount;

    // ========== 聚合列表（按产品分组） ==========
    private List<BrandProductSummaryDTO> summaries;

    // ========== 分页信息 ==========
    private Integer totalCount;        // 总条目数
    private Integer page;               // 当前页
    private Integer pageSize;           // 每页大小
    private Integer totalPages;         // 总页数
}