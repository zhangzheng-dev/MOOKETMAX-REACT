package com.mooket.social.dto;

import lombok.Data;

/**
 * 热门产品 DTO
 */
@Data
public class HotProductDTO {
    private String productName;   // 产品名称
    private Integer offerCount;  // 报盘数
    private Integer rank;        // 排名 1/2/3
}
