package com.mooket.social.dto;

import lombok.Data;

/**
 * 首页卡片基类
 */
@Data
public class HomeCardItemDTO {
    /**
     * 卡片类型: product/country/brand/merchant/factory/brandProduct/factoryProduct/countryProduct
     */
    private String cardType;

    /**
     * 卡片排序
     */
    private Integer rank;

    /**
     * 今日报盘数
     */
    private Integer todayOfferCount;

    /**
     * 历史记录ID（用于删除和添加到自选）
     */
    private Long historyId;
}
