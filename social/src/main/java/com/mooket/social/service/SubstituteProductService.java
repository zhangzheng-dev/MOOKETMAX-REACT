package com.mooket.social.service;

import com.mooket.social.dto.SubstituteProductDTO;

/**
 * 平替产品服务接口
 */
public interface SubstituteProductService {

    /**
     * 获取平替产品列表（同产品同等级的所有厂号）
     */
    SubstituteProductDTO getSubstituteProducts(String country, String factoryNo, String productName, String category);

    /**
     * 获取平替产品详情（带报盘数据）
     */
    SubstituteProductDTO.SubstituteProductDetailDTO getSubstituteProductDetail(
            String country, String factoryNo, String productName, String category,
            String type, String sortBy, int page, int pageSize);
}