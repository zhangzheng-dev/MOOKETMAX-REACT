package com.mooket.social.service;

/**
 * 国家 Service 接口
 */
public interface CountryService {

    /**
     * 获取国家详情
     * @param country 国家名称
     * @param category 品类（牛/猪）
     * @param offerType 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price(价格)
     * @param page 页码
     * @param pageSize 每页大小
     * @return 国家详情DTO
     */
    com.mooket.social.dto.CountryDetailDTO getCountryDetail(String country, String category,
                                                            String offerType, String sortBy,
                                                            int page, int pageSize);
}
