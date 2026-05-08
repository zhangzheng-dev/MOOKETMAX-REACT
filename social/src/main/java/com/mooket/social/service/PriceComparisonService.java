package com.mooket.social.service;

import com.mooket.social.dto.FactoryPriceComparisonDTO;

import java.util.List;

/**
 * 价格对比服务接口
 */
public interface PriceComparisonService {

    /**
     * 获取多厂号价格对比数据
     *
     * @param country 国家
     * @param factoryNos 厂号列表
     * @param productName 产品名称
     * @param category 品类（默认牛）
     * @param offerType 报盘/求购（默认报盘）
     * @param days 天数（默认30）
     * @return 价格对比DTO
     */
    FactoryPriceComparisonDTO getFactoryPriceComparison(
            String country,
            List<String> factoryNos,
            String productName,
            String category,
            String offerType,
            int days
    );
}
