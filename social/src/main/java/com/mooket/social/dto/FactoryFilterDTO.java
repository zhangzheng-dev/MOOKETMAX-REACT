package com.mooket.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 厂号筛选 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryFilterDTO {

    private List<String> countries;  // 国家列表

    private List<FactoryItem> factories;  // 厂号列表

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FactoryItem {
        private String country;
        private String factoryNo;
    }
}
