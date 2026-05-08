package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 国家厂号卡片
 * 显示内容：国旗、热门产品x3、今日报盘数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FactoryCardDTO extends HomeCardItemDTO {
    /**
     * 国家
     */
    private String country;

    /**
     * 国家别名（用于显示国旗）
     */
    private String countryAlias;

    /**
     * 厂号
     */
    private String factoryNo;

    /**
     * 今日报盘数
     */
    private Integer todayOfferCount;

    /**
     * 热门产品列表（最多3个）
     */
    private List<HotProductDTO> hotProducts;

    /**
     * 价格区间-最低
     */
    private BigDecimal priceMin;

    /**
     * 价格区间-最高
     */
    private BigDecimal priceMax;

    /**
     * 热门产品DTO
     */
    @Data
    public static class HotProductDTO {
        private String productName;
        private Integer offerCount;
    }
}
