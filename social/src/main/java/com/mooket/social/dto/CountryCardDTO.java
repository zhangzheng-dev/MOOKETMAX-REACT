package com.mooket.social.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 国家卡片
 * 显示内容：国旗、热门厂号x3、热门产品x3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CountryCardDTO extends HomeCardItemDTO {
    /**
     * 国家名称
     */
    private String country;

    /**
     * 国家别名（用于显示国旗）
     */
    private String countryAlias;

    /**
     * 热门厂号列表（最多3个）
     */
    private List<HotFactoryDTO> hotFactories;

    /**
     * 热门产品列表（最多3个）
     */
    private List<HotProductDTO> hotProducts;

    /**
     * 热门厂号DTO
     */
    @Data
    public static class HotFactoryDTO {
        private String factoryNo;
        private Integer offerCount;
    }

    /**
     * 热门产品DTO
     */
    @Data
    public static class HotProductDTO {
        private String productName;
        private Integer offerCount;
    }
}
