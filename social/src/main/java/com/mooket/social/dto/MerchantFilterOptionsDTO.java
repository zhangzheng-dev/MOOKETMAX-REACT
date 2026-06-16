package com.mooket.social.dto;

import lombok.Data;

import java.util.List;

@Data
public class MerchantFilterOptionsDTO {

    private List<String> countries;
    private List<String> countryFactories;
    private List<String> regions;
    private List<String> products;
    private List<String> goodsTypes;
    private List<String> feedingMethods;
    private List<String> tags;
}
