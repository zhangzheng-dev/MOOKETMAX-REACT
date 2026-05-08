package com.mooket.social.service;

import com.mooket.social.dto.FactoryDetailDTO;

/**
 * 厂号 Service
 */
public interface FactoryService {

    /**
     * 获取厂号详情
     * @param country 国家
     * @param factoryNo 厂号
     * @param category 品类（牛/猪）
     * @param offerType 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price(价格)
     * @param page 页码
     * @param pageSize 每页大小
     * @return 厂号详情
     */
    FactoryDetailDTO getFactoryDetail(String country, String factoryNo, String category,
                                     String offerType, String sortBy, int page, int pageSize);
}
