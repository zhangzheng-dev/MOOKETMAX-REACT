package com.mooket.social.dto;

import lombok.Data;

/**
 * 热门厂号 DTO
 */
@Data
public class HotFactoryDTO {
    private String factoryNo;     // 厂号
    private Integer offerCount;   // 报盘数
    private Integer rank;         // 排名 1/2/3
}
