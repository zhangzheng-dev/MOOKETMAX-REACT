package com.mooket.social.service.impl;

import com.mooket.social.dto.PriceTrendDTO;
import com.mooket.social.entity.DictProduct;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictProductMapper;
import com.mooket.social.mapper.StatPriceTrendMapper;
import com.mooket.social.service.PriceTrendService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/service/impl/PriceTrendServiceImpl.class */
public class PriceTrendServiceImpl implements PriceTrendService {

    @Autowired
    private StatPriceTrendMapper trendMapper;

    @Autowired
    private BizOfferMapper offerMapper;

    @Autowired
    private DictProductMapper productMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Override // com.mooket.social.service.PriceTrendService
    public PriceTrendDTO getPriceTrend(String dimensionType, String country, Integer productId, String factoryNo, String offerType) {
        List<StatPriceTrendMapper.PriceTrendPoint> historicalPoints;
        DictProduct product;
        PriceTrendDTO dto = new PriceTrendDTO();
        dto.setDimensionType(dimensionType);
        dto.setCountry(country);
        dto.setProductId(productId);
        dto.setFactoryNo(factoryNo);
        dto.setOfferType(offerType);
        if (productId != null && (product = this.productMapper.selectById(productId)) != null) {
            dto.setProductName(product.getProductName());
        }
        if (factoryNo != null && !factoryNo.isEmpty()) {
            historicalPoints = this.trendMapper.selectTrendPointsByCountryFactoryProduct(dimensionType, country, productId, factoryNo, offerType);
        } else {
            historicalPoints = this.trendMapper.selectTrendPointsByCountryProduct(dimensionType, country, productId, offerType);
        }
        Map<LocalDate, BigDecimal> trendMap = new LinkedHashMap<>();
        for (StatPriceTrendMapper.PriceTrendPoint point : historicalPoints) {
            if (point.date != null && point.avgPrice != null) {
                trendMap.put(point.date, point.avgPrice);
            }
        }
        LocalDate today = LocalDate.now();
        BigDecimal todayPrice = calculateTodayAvgPrice(country, productId, factoryNo, offerType);
        trendMap.put(today, todayPrice);
        List<PriceTrendDTO.TrendPoint> trendPoints = new ArrayList<>();
        LocalDate startDate = today.minusDays(29L);
        for (int i = 0; i < 30; i++) {
            LocalDate date = startDate.plusDays(i);
            BigDecimal avgPrice = trendMap.get(date);
            trendPoints.add(new PriceTrendDTO.TrendPoint(date, avgPrice));
        }
        dto.setTrend(trendPoints);
        return dto;
    }

    private BigDecimal calculateTodayAvgPrice(String country, Integer productId, String factoryNo, String offerType) {
        List<BizOfferMapper.DailyPriceStats> stats;
        if (factoryNo != null && !factoryNo.isEmpty()) {
            stats = this.offerMapper.selectTodayAvgPriceByFactory(country, productId, factoryNo, offerType);
        } else {
            stats = this.offerMapper.selectTodayAvgPrice(country, productId, offerType);
        }
        if (stats == null || stats.isEmpty()) {
            return null;
        }
        return stats.get(0).avgPrice;
    }

    @Override // com.mooket.social.service.PriceTrendService
    @Transactional
    public void calculateAndSaveTodayTrends() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[PriceTrend] Starting calculation at " + String.valueOf(now));
        int count = 0 + calculateCountryProductTrends(today);
        int count2 = count + calculateCountryFactoryProductTrends(today);
        int deleted = this.trendMapper.deleteOldRecords();
        if (deleted > 0) {
            System.out.println("[PriceTrend] Cleaned up " + deleted + " old records");
        }
        System.out.println("[PriceTrend] Calculation completed. Saved " + count2 + " records at " + String.valueOf(LocalDateTime.now()));
    }

    private int calculateCountryProductTrends(LocalDate today) {
        int count = 0;
        List<BizOfferMapper.CountryProductCombo> combos = this.offerMapper.selectActiveCountryProductCombos();
        for (BizOfferMapper.CountryProductCombo combo : combos) {
            if (combo.country != null && combo.productId != null) {
                BigDecimal offerPrice = calculateRealTimeAvgPrice(combo.country, combo.productId, null, "报盘");
                if (offerPrice != null) {
                    this.trendMapper.upsertPriceTrend(today, StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, combo.productName, "", "报盘", offerPrice, today);
                    count++;
                }
                BigDecimal inquiryPrice = calculateRealTimeAvgPrice(combo.country, combo.productId, null, "求购");
                if (inquiryPrice != null) {
                    this.trendMapper.upsertPriceTrend(today, StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, combo.productName, "", "求购", inquiryPrice, today);
                    count++;
                }
            }
        }
        return count;
    }

    private int calculateCountryFactoryProductTrends(LocalDate today) {
        int count = 0;
        List<BizOfferMapper.CountryFactoryProductCombo> combos = this.offerMapper.selectActiveCountryFactoryProductCombos();
        for (BizOfferMapper.CountryFactoryProductCombo combo : combos) {
            if (combo.country != null && combo.productId != null && combo.factoryNo != null) {
                BigDecimal offerPrice = calculateRealTimeAvgPrice(combo.country, combo.productId, combo.factoryNo, "报盘");
                if (offerPrice != null) {
                    this.trendMapper.upsertPriceTrend(today, StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo.country, combo.productId, combo.productName, combo.factoryNo, "报盘", offerPrice, today);
                    count++;
                }
                BigDecimal inquiryPrice = calculateRealTimeAvgPrice(combo.country, combo.productId, combo.factoryNo, "求购");
                if (inquiryPrice != null) {
                    this.trendMapper.upsertPriceTrend(today, StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo.country, combo.productId, combo.productName, combo.factoryNo, "求购", inquiryPrice, today);
                    count++;
                }
            }
        }
        return count;
    }

    private BigDecimal calculateRealTimeAvgPrice(String country, Integer productId, String factoryNo, String offerType) {
        List<BizOfferMapper.DailyPriceStats> stats;
        if (factoryNo != null && !factoryNo.isEmpty()) {
            stats = this.offerMapper.selectTodayAvgPriceByFactory(country, productId, factoryNo, offerType);
        } else {
            stats = this.offerMapper.selectTodayAvgPrice(country, productId, offerType);
        }
        if (stats == null || stats.isEmpty()) {
            return null;
        }
        return stats.get(0).avgPrice;
    }

    @Override // com.mooket.social.service.PriceTrendService
    @Transactional
    public void backfillHistoricalData(int days) {
        System.out.println("[PriceTrend] Starting backfill for " + days + " days");
        LocalDate today = LocalDate.now();
        int count = 0;
        for (int i = 1; i <= days; i++) {
            LocalDate targetDate = today.minusDays(i);
            System.out.println("[PriceTrend] Backfilling date: " + String.valueOf(targetDate));
            List<BizOfferMapper.CountryProductCombo> combos = this.offerMapper.selectActiveCountryProductCombos();
            for (BizOfferMapper.CountryProductCombo combo : combos) {
                if (combo.country != null && combo.productId != null) {
                    BigDecimal offerPrice = calculateHistoricalAvgPrice(targetDate, combo.country, combo.productId, null, "报盘");
                    if (offerPrice != null) {
                        this.trendMapper.upsertPriceTrend(targetDate, StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, combo.productName, "", "报盘", offerPrice, today);
                        count++;
                    }
                    BigDecimal inquiryPrice = calculateHistoricalAvgPrice(targetDate, combo.country, combo.productId, null, "求购");
                    if (inquiryPrice != null) {
                        this.trendMapper.upsertPriceTrend(targetDate, StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, combo.productName, "", "求购", inquiryPrice, today);
                        count++;
                    }
                }
            }
            List<BizOfferMapper.CountryFactoryProductCombo> factoryCombos = this.offerMapper.selectActiveCountryFactoryProductCombos();
            for (BizOfferMapper.CountryFactoryProductCombo combo2 : factoryCombos) {
                if (combo2.country != null && combo2.productId != null && combo2.factoryNo != null) {
                    BigDecimal offerPrice2 = calculateHistoricalAvgPrice(targetDate, combo2.country, combo2.productId, combo2.factoryNo, "报盘");
                    if (offerPrice2 != null) {
                        this.trendMapper.upsertPriceTrend(targetDate, StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo2.country, combo2.productId, combo2.productName, combo2.factoryNo, "报盘", offerPrice2, today);
                        count++;
                    }
                    BigDecimal inquiryPrice2 = calculateHistoricalAvgPrice(targetDate, combo2.country, combo2.productId, combo2.factoryNo, "求购");
                    if (inquiryPrice2 != null) {
                        this.trendMapper.upsertPriceTrend(targetDate, StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo2.country, combo2.productId, combo2.productName, combo2.factoryNo, "求购", inquiryPrice2, today);
                        count++;
                    }
                }
            }
        }
        System.out.println("[PriceTrend] Backfill completed. Saved " + count + " records");
    }

    @Override // com.mooket.social.service.PriceTrendService
    @Transactional
    public void backfillSingleProduct(String dimensionType, String country, Integer productId, String factoryNo, String offerType, int days) {
        DictProduct product;
        System.out.println("[PriceTrend] Starting backfill for single product: " + country + ", productId=" + productId + ", factoryNo=" + factoryNo + ", days=" + days);
        LocalDate today = LocalDate.now();
        int count = 0;
        String productName = null;
        if (productId != null && (product = this.productMapper.selectById(productId)) != null) {
            productName = product.getProductName();
        }
        for (int i = 1; i <= days; i++) {
            LocalDate targetDate = today.minusDays(i);
            BigDecimal avgPrice = calculateHistoricalAvgPrice(targetDate, country, productId, factoryNo, offerType);
            if (avgPrice != null) {
                this.trendMapper.upsertPriceTrend(targetDate, dimensionType, country, productId, productName, factoryNo, offerType, avgPrice, today);
                count++;
                System.out.println("[PriceTrend] Backfilled " + String.valueOf(targetDate) + ": " + String.valueOf(avgPrice));
            }
        }
        System.out.println("[PriceTrend] Single product backfill completed. Saved " + count + " records");
    }

    private BigDecimal calculateHistoricalAvgPrice(LocalDate targetDate, String country, Integer productId, String factoryNo, String offerType) {
        List<BizOfferMapper.DailyPriceStats> stats;
        if (factoryNo != null && !factoryNo.isEmpty()) {
            stats = this.offerMapper.selectHistoricalAvgPriceByFactory(targetDate, country, productId, factoryNo, offerType);
        } else {
            stats = this.offerMapper.selectHistoricalAvgPrice(targetDate, country, productId, offerType);
        }
        if (stats == null || stats.isEmpty()) {
            return null;
        }
        return stats.get(0).avgPrice;
    }

    @Override // com.mooket.social.service.PriceTrendService
    @Transactional
    public void backfillYesterday() {
        StatPriceTrendMapper.PriceTrendPoint prevPoint;
        StatPriceTrendMapper.PriceTrendPoint prevPoint2;
        StatPriceTrendMapper.PriceTrendPoint prevPoint3;
        StatPriceTrendMapper.PriceTrendPoint prevPoint4;
        LocalDate yesterday = LocalDate.now().minusDays(1L);
        LocalDate dayBeforeYesterday = yesterday.minusDays(1L);
        System.out.println("[PriceTrend] Starting backfill for yesterday: " + String.valueOf(yesterday));
        int count = 0;
        List<BizOfferMapper.CountryProductCombo> combos = this.offerMapper.selectActiveCountryProductCombos();
        for (BizOfferMapper.CountryProductCombo combo : combos) {
            if (combo.country != null && combo.productId != null) {
                BigDecimal offerPrice = calculateHistoricalAvgPrice(yesterday, combo.country, combo.productId, null, "报盘");
                if (offerPrice == null && (prevPoint4 = this.trendMapper.selectPricePoint(StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, "", "报盘", dayBeforeYesterday)) != null && prevPoint4.avgPrice != null) {
                    offerPrice = prevPoint4.avgPrice;
                    System.out.println("[PriceTrend] Forward fill 报盘 " + combo.country + " " + combo.productName + " at " + String.valueOf(yesterday) + " from " + String.valueOf(dayBeforeYesterday) + ": " + String.valueOf(offerPrice));
                }
                if (offerPrice != null) {
                    this.trendMapper.upsertPriceTrend(yesterday, StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, combo.productName, "", "报盘", offerPrice, yesterday);
                    count++;
                }
                BigDecimal inquiryPrice = calculateHistoricalAvgPrice(yesterday, combo.country, combo.productId, null, "求购");
                if (inquiryPrice == null && (prevPoint3 = this.trendMapper.selectPricePoint(StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, "", "求购", dayBeforeYesterday)) != null && prevPoint3.avgPrice != null) {
                    inquiryPrice = prevPoint3.avgPrice;
                    System.out.println("[PriceTrend] Forward fill 求购 " + combo.country + " " + combo.productName + " at " + String.valueOf(yesterday) + " from " + String.valueOf(dayBeforeYesterday) + ": " + String.valueOf(inquiryPrice));
                }
                if (inquiryPrice != null) {
                    this.trendMapper.upsertPriceTrend(yesterday, StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT, combo.country, combo.productId, combo.productName, "", "求购", inquiryPrice, yesterday);
                    count++;
                }
            }
        }
        List<BizOfferMapper.CountryFactoryProductCombo> factoryCombos = this.offerMapper.selectActiveCountryFactoryProductCombos();
        for (BizOfferMapper.CountryFactoryProductCombo combo2 : factoryCombos) {
            if (combo2.country != null && combo2.productId != null && combo2.factoryNo != null) {
                BigDecimal offerPrice2 = calculateHistoricalAvgPrice(yesterday, combo2.country, combo2.productId, combo2.factoryNo, "报盘");
                if (offerPrice2 == null && (prevPoint2 = this.trendMapper.selectPricePoint(StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo2.country, combo2.productId, combo2.factoryNo, "报盘", dayBeforeYesterday)) != null && prevPoint2.avgPrice != null) {
                    offerPrice2 = prevPoint2.avgPrice;
                    System.out.println("[PriceTrend] Forward fill 报盘 " + combo2.factoryNo + " at " + String.valueOf(yesterday) + " from " + String.valueOf(dayBeforeYesterday) + ": " + String.valueOf(offerPrice2));
                }
                if (offerPrice2 != null) {
                    this.trendMapper.upsertPriceTrend(yesterday, StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo2.country, combo2.productId, combo2.productName, combo2.factoryNo, "报盘", offerPrice2, yesterday);
                    count++;
                }
                BigDecimal inquiryPrice2 = calculateHistoricalAvgPrice(yesterday, combo2.country, combo2.productId, combo2.factoryNo, "求购");
                if (inquiryPrice2 == null && (prevPoint = this.trendMapper.selectPricePoint(StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo2.country, combo2.productId, combo2.factoryNo, "求购", dayBeforeYesterday)) != null && prevPoint.avgPrice != null) {
                    inquiryPrice2 = prevPoint.avgPrice;
                    System.out.println("[PriceTrend] Forward fill 求购 " + combo2.factoryNo + " at " + String.valueOf(yesterday) + " from " + String.valueOf(dayBeforeYesterday) + ": " + String.valueOf(inquiryPrice2));
                }
                if (inquiryPrice2 != null) {
                    this.trendMapper.upsertPriceTrend(yesterday, StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT, combo2.country, combo2.productId, combo2.productName, combo2.factoryNo, "求购", inquiryPrice2, yesterday);
                    count++;
                }
            }
        }
        System.out.println("[PriceTrend] Yesterday backfill completed. Saved " + count + " records for date: " + String.valueOf(yesterday));
    }
}