package com.mooket.social.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupedOfferFilterOptionsDTO {

    private List<OptionItem> merchants;
    private List<String> regions;
    private List<String> goodsTypes;
    private List<String> feedingMethods;
    private List<String> tags;

    @Data
    public static class OptionItem {
        private String key;
        private String label;
    }
}
