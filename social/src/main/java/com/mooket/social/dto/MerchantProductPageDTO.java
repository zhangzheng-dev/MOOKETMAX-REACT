package com.mooket.social.dto;

import lombok.Data;

import java.util.List;

@Data
public class MerchantProductPageDTO {

    private List<OfferSummaryDTO> products;
    private Integer totalCount;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private String offerType;
}
