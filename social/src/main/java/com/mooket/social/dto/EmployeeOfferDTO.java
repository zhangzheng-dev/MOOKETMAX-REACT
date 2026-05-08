package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 员工报价明细 DTO
 */
@Data
public class EmployeeOfferDTO {

    private Long offerId;
    private String userNickname;
    private BigDecimal price;
    private BigDecimal priceMax;
    private String weight;
    private String goodsLocation;
    private String tags;
    private String goodsType;
    private String feedingMethod;
    private String offerOriginalText;
    private String publishTime;
}
