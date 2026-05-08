package com.mooket.social.service;

import com.mooket.social.dto.ProductDetailDTO;

/**
 * 产品服务接口
 */
public interface ProductService {

    /**
     * 获取产品详情（按产品聚合所有商家的报盘/求购）
     * @param productId 产品ID
     * @param category 品类（牛/猪）
     * @param offerType 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price(价格)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 产品详情
     */
    ProductDetailDTO getProductDetail(Integer productId, String category, String offerType,
                                       String sortBy, int page, int pageSize);
}
