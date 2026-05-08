package com.mooket.social.dto;

import lombok.Data;

import java.util.List;

/**
 * 商家产品分页响应 DTO
 */
@Data
public class MerchantProductPageDTO {

    // 产品摘要列表（按分组）
    private List<OfferSummaryDTO> products;

    // 总产品数（分组后的数量）
    private Integer totalCount;

    // 当前页码（从1开始）
    private Integer page;

    // 每页大小
    private Integer pageSize;

    // 总页数
    private Integer totalPages;

    // 报盘类型：offer 或 inquiry
    private String offerType;
}
