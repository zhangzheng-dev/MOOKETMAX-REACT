package com.mooket.social.service;

import com.mooket.social.dto.CountryProductDetailDTO;

/**
 * 国家+产品服务接口
 */
public interface CountryProductService {

    /**
     * 获取国家+产品详情
     * @param country 国家名称
     * @param productName 产品名称
     * @param type 类型（offer/报盘 或 inquiry/求购）
     * @param category 分类（牛/猪）
     * @param sortBy 排序（comprehensive/price_asc/price_desc）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 国家+产品详情DTO
     */
    CountryProductDetailDTO getCountryProductDetail(String country, String productName, String type,
                                                     String category, String sortBy, int page, int pageSize);
}
