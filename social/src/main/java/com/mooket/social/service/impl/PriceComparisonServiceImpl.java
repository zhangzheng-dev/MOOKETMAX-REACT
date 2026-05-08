package com.mooket.social.service.impl;

import com.mooket.social.dto.FactoryPriceComparisonDTO;
import com.mooket.social.mapper.DictProductMapper;
import com.mooket.social.mapper.StatPriceTrendMapper;
import com.mooket.social.service.PriceComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 价格对比服务实现
 */
@Service
public class PriceComparisonServiceImpl implements PriceComparisonService {

    @Autowired
    private StatPriceTrendMapper trendMapper;

    @Autowired
    private DictProductMapper productMapper;

    @Override
    public FactoryPriceComparisonDTO getFactoryPriceComparison(
            String country,
            List<String> factoryNos,
            String productName,
            String category,
            String offerType,
            int days) {

        FactoryPriceComparisonDTO dto = new FactoryPriceComparisonDTO();
        dto.setCountry(country);
        dto.setProductName(productName);
        dto.setCategory(category);
        dto.setOfferType(offerType);

        // 获取产品ID
        Integer productId = null;
        var product = productMapper.selectByProductName(productName);
        if (product != null) {
            productId = product.getProductId();
        }

        // 构建结果列表
        List<FactoryPriceComparisonDTO.FactoryTrendData> factories = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        for (String factoryNo : factoryNos) {
            // 查询该厂号的价格趋势（含报盘数）
            List<StatPriceTrendMapper.PriceTrendPoint> historicalPoints =
                    trendMapper.selectTrendPointsWithOfferCount(
                            StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT,
                            country,
                            productId,
                            factoryNo,
                            productName,
                            offerType
                    );

            // 构建趋势点 map
            Map<LocalDate, BigDecimal> trendMap = new LinkedHashMap<>();
            Map<LocalDate, Integer> offerCountMap = new LinkedHashMap<>();
            for (var point : historicalPoints) {
                if (point.date != null && point.avgPrice != null) {
                    trendMap.put(point.date, point.avgPrice);
                    offerCountMap.put(point.date, point.offerCount != null ? point.offerCount : 0);
                }
            }

            // 转换为趋势点列表（补全所有日期）
            List<FactoryPriceComparisonDTO.TrendPoint> trendPoints = new ArrayList<>();
            BigDecimal latestPrice = null;  // 最近一天有数据的均价
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i);
                BigDecimal avgPrice = trendMap.get(date);
                Integer offerCount = offerCountMap.get(date);
                trendPoints.add(new FactoryPriceComparisonDTO.TrendPoint(date, avgPrice, offerCount));
                if (avgPrice != null) {
                    latestPrice = avgPrice;  // 保留最近一次有数据的值
                }
            }

            // 创建厂号数据
            FactoryPriceComparisonDTO.FactoryTrendData factoryData = new FactoryPriceComparisonDTO.FactoryTrendData();
            factoryData.setFactoryNo(factoryNo);
            factoryData.setTrend(trendPoints);
            factoryData.setAvgPrice(latestPrice);  // 用最近一天的日均价
            factories.add(factoryData);
        }

        // 按均价升序排序
        factories.sort((a, b) -> {
            if (a.getAvgPrice() == null && b.getAvgPrice() == null) return 0;
            if (a.getAvgPrice() == null) return 1;
            if (b.getAvgPrice() == null) return -1;
            return a.getAvgPrice().compareTo(b.getAvgPrice());
        });

        dto.setFactories(factories);
        return dto;
    }
}
