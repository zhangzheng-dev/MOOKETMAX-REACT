package com.mooket.social.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报盘摘要 DTO
 */
@Data
public class OfferSummaryDTO {

    private Long offerId;
    private String productName;
    private String country;
    private String factoryNo;
    private BigDecimal price;
    private BigDecimal priceMax;
    private String goodsLocation;
    private String tags;
    private String goodsType;
    private String feedingType;
    private LocalDateTime publishTime;

    // 员工报价明细列表
    private List<EmployeeOfferDTO> employeeOffers;
}
