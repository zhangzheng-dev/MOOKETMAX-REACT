package com.mooket.social.service;

import com.mooket.social.dto.PriceTrendDTO;

import java.util.List;

/**
 * 价格趋势服务接口
 */
public interface PriceTrendService {

    /**
     * 获取价格趋势（近30天历史 + 当天实时）
     *
     * @param dimensionType 维度类型: country_product / country_factory_product
     * @param country 国家
     * @param productId 产品ID
     * @param factoryNo 厂号（可为空）
     * @param offerType 报盘/求购: 报盘 / 求购
     * @return 价格趋势DTO
     */
    PriceTrendDTO getPriceTrend(String dimensionType, String country, Integer productId, String factoryNo, String offerType);

    /**
     * 计算并保存当天实时价格趋势（每2分钟执行一次）
     */
    void calculateAndSaveTodayTrends();

    /**
     * 回填历史数据（用于初始化）
     *
     * @param days 回填天数
     */
    void backfillHistoricalData(int days);

    /**
     * 回填单个产品的历史数据
     */
    void backfillSingleProduct(String dimensionType, String country, Integer productId, String factoryNo, String offerType, int days);

    /**
     * 回填昨天的数据（每天00:05执行，固化昨天的最终数据）
     * 如果昨天没有biz_offer数据无法计算日均价，会沿用前一天的日均价
     */
    void backfillYesterday();
}
