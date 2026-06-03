package com.mooket.social.service;

import com.mooket.social.dto.MerchantDetailDTO;
import com.mooket.social.dto.MerchantProductPageDTO;

/**
 * 商家服务接口
 */
public interface MerchantService {

    /**
     * 获取商家详情
     * @param merchantId 商家ID
     * @param category 品类筛选（可选，为空则不筛选）
     * @return 商家详情
     */
    MerchantDetailDTO getMerchantDetail(Long merchantId, String category);

    /**
     * 分页获取商家产品列表
     * @param merchantId 商家ID
     * @param category 品类筛选
     * @param offerType 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @param sortBy 排序方式：comprehensive/publish_time/price_asc/price_desc
     * @return 分页结果
     */
    MerchantProductPageDTO getMerchantProducts(Long merchantId, String category, String offerType, int page, int pageSize, String sortBy);
}
