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
    private String contactPhone;
    private BigDecimal price;
    private BigDecimal priceMax;
    private String weight;
    private String goodsLocation;
    private String tags;
    private String goodsType;
    private String feedingMethod;
    private String feedingType;
    private String fatRatio;
    private String cattleBreed;
    private String remark;
    private String offerOriginalText;
    private String publishTime;
}
