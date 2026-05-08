package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 国家产品卡片
 * 显示内容：报盘工厂数、报价区间（前3工厂）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CountryProductCardDTO extends HomeCardItemDTO {
    /**
     * 国家
     */
    private String country;

    /**
     * 国家别名（用于显示国旗）
     */
    private String countryAlias;

    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 报盘工厂数
     */
    private Integer factoryCount;

    /**
     * 报价区间-最低
     */
    private BigDecimal priceMin;

    /**
     * 报价区间-最高
     */
    private BigDecimal priceMax;

    /**
     * 前3工厂报价区间
     */
    private List<FactoryPriceDTO> topFactories;

    /**
     * 工厂报价DTO
     */
    @Data
    public static class FactoryPriceDTO {
        private String factoryNo;
        private BigDecimal priceMin;
        private BigDecimal priceMax;
    }
}
