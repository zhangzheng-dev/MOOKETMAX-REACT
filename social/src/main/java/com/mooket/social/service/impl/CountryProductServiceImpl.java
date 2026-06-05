package com.mooket.social.service.impl;

import com.mooket.social.dto.CountryProductDetailDTO;
import com.mooket.social.dto.CountryProductDetailDTO.CountryProductFactoryDTO;
import com.mooket.social.dto.CountryProductDetailDTO.DailyPrice;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.entity.DictProduct;
import com.mooket.social.mapper.DictProductMapper;
import com.mooket.social.mapper.StatPriceTrendMapper;
import com.mooket.social.service.CountryProductService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 国家+产品服务实现
 */
@Service
public class CountryProductServiceImpl implements CountryProductService {

    private final BizOfferMapper offerMapper;
    private final StatPriceTrendMapper trendMapper;
    private final DictProductMapper productMapper;

    public CountryProductServiceImpl(BizOfferMapper offerMapper, StatPriceTrendMapper trendMapper, DictProductMapper productMapper) {
        this.offerMapper = offerMapper;
        this.trendMapper = trendMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Cacheable(value = "countryProductDetail", key = "#country + '_' + #productName + '_' + #type + '_' + #category + '_' + #sortBy + '_' + #page + '_' + #pageSize")
    public CountryProductDetailDTO getCountryProductDetail(String country, String productName, String type,
                                                             String category, String sortBy, int page, int pageSize) {
        return buildCountryProductDetail(country, productName, type, category, sortBy, page, pageSize);
    }

    /**
     * 构建国家+产品详情
     */
    private CountryProductDetailDTO buildCountryProductDetail(String country, String productName, String type,
                                                              String category, String sortBy, int page, int pageSize) {
        CountryProductDetailDTO dto = new CountryProductDetailDTO();
        dto.setCountry(country);
        dto.setProductName(productName);

        // 获取 productId
        DictProduct product = productMapper.findByName(category, productName);
        if (product != null && product.getProductId() != null) {
            dto.setProductId(product.getProductId());
        }

        // 1. 获取看板统计数据（报盘数、求购数、商家数）
        BizOfferMapper.CountryProductStats stats = offerMapper.selectCountryProductStats(country, productName, category);
        dto.setOfferCount(stats != null && stats.totalOfferCount != null ? stats.totalOfferCount : 0L);
        dto.setInquiryCount(stats != null && stats.totalInquiryCount != null ? stats.totalInquiryCount : 0L);
        dto.setMerchantCount(stats != null && stats.merchantCount != null ? stats.merchantCount : 0);

        // 2. 获取过滤后的价格区间（按type分开计算IQR）
        String priceOfferType = "offer".equalsIgnoreCase(type) ? "报盘" : ("inquiry".equalsIgnoreCase(type) ? "求购" : null);
        BizOfferMapper.PriceRange priceRange = offerMapper.selectFilteredPriceRangeByCountryProduct(country, productName, category, priceOfferType);
        dto.setPriceMin(priceRange != null ? priceRange.priceMin : null);
        dto.setPriceMax(priceRange != null ? priceRange.priceMax : null);

        // 3. 计算日均价涨跌（今日 vs 昨日）
        calculatePriceChange(country, productName, category, priceOfferType, dto);

        // 4. 获取近7日价格走势
        List<DailyPrice> priceHistory7Days = getPriceHistory7Days(country, productName, category, priceOfferType);
        dto.setPriceHistory7Days(priceHistory7Days);

        // 5. 获取近30日价格趋势
        List<DailyPrice> priceHistory30Days = getPriceHistory30Days(country, productName, category, priceOfferType);
        dto.setPriceHistory30Days(priceHistory30Days);

        // 6. 获取厂号聚合列表（按type过滤）
        int offset = (page - 1) * pageSize;
        String offerType = "offer".equalsIgnoreCase(type) ? "报盘" : ("inquiry".equalsIgnoreCase(type) ? "求购" : null);
        List<BizOfferMapper.CountryProductFactoryAgg> aggList = offerMapper.selectCountryProductFactoryAgg(
                country, productName, category, offerType, pageSize, offset, sortBy);

        List<CountryProductFactoryDTO> factories = aggList.stream()
                .map(agg -> convertToFactoryDTO(agg))
                .collect(Collectors.toList());

        // 排序
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            factories.sort((a, b) -> {
                boolean aHas = a.getPriceMin() != null && a.getPriceMin().compareTo(BigDecimal.ZERO) > 0;
                boolean bHas = b.getPriceMin() != null && b.getPriceMin().compareTo(BigDecimal.ZERO) > 0;
                if (!aHas && !bHas) return 0;
                if (!aHas) return 1;
                if (!bHas) return -1;
                return a.getPriceMin().compareTo(b.getPriceMin());
            });
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            factories.sort((a, b) -> {
                boolean aHas = a.getPriceMax() != null && a.getPriceMax().compareTo(BigDecimal.ZERO) > 0;
                boolean bHas = b.getPriceMax() != null && b.getPriceMax().compareTo(BigDecimal.ZERO) > 0;
                if (!aHas && !bHas) return 0;
                if (!aHas) return 1;
                if (!bHas) return -1;
                return b.getPriceMax().compareTo(a.getPriceMax());
            });
        }
        // else: 默认按报盘数降序已在SQL中处理

        // 6. 获取总数（按type过滤）
        int totalCount = offerMapper.countCountryProductFactoryAgg(country, productName, category, offerType);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        dto.setFactories(factories);
        dto.setTotalCount(totalCount);
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setTotalPages(totalPages);

        return dto;
    }

    /**
     * 计算日均价涨跌
     */
    private void calculatePriceChange(String country, String productName, String category, String offerType, CountryProductDetailDTO dto) {
        List<BizOfferMapper.DailyPriceStats> dailyStats = offerMapper.selectDailyPriceStats(country, productName, category, offerType);

        if (dailyStats == null || dailyStats.isEmpty()) {
            dto.setPriceChange(null);
            dto.setPriceChangeRate(null);
            return;
        }

        // 按日期排序
        dailyStats.sort(Comparator.comparing(s -> s.dataDate));

        // 获取今日和昨日的平均价
        BigDecimal todayPrice = null;
        BigDecimal yesterdayPrice = null;

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);

        for (BizOfferMapper.DailyPriceStats stats : dailyStats) {
            if (stats.dataDate.equals(today)) {
                todayPrice = stats.avgPrice;
            } else if (stats.dataDate.equals(yesterday)) {
                yesterdayPrice = stats.avgPrice;
            }
        }

        if (todayPrice != null && yesterdayPrice != null && yesterdayPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = todayPrice.subtract(yesterdayPrice).setScale(1, RoundingMode.HALF_UP);
            BigDecimal changeRate = change.multiply(new BigDecimal("100"))
                    .divide(yesterdayPrice, 1, RoundingMode.HALF_UP);

            dto.setPriceChange(change);
            dto.setPriceChangeRate(changeRate);
        } else {
            dto.setPriceChange(null);
            dto.setPriceChangeRate(null);
        }
    }

    /**
     * 获取近7日价格走势（使用 stat_price_trend 表，带IQR过滤）
     */
    private List<DailyPrice> getPriceHistory7Days(String country, String productName, String category, String offerType) {
        DictProduct product = productMapper.findByName(category, productName);
        if (product == null || product.getProductId() == null) {
            return Collections.emptyList();
        }

        List<StatPriceTrendMapper.PriceTrendPoint> trendPoints = trendMapper.selectTrendPointsByCountryProduct(
                StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT,
                country,
                product.getProductId(),
                offerType
        );

        if (trendPoints == null || trendPoints.isEmpty()) {
            return Collections.emptyList();
        }

        // 过滤只保留近7天的数据
        java.time.LocalDate sevenDaysAgo = java.time.LocalDate.now().minusDays(6);
        List<StatPriceTrendMapper.PriceTrendPoint> recentPoints = trendPoints.stream()
                .filter(p -> p.date != null && !p.date.isBefore(sevenDaysAgo))
                .collect(Collectors.toList());

        if (recentPoints.isEmpty()) {
            return Collections.emptyList();
        }

        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("MM-dd");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DailyPrice> history = new ArrayList<>();

        for (StatPriceTrendMapper.PriceTrendPoint point : recentPoints) {
            DailyPrice dp = new DailyPrice();
            dp.setDate(point.date.format(shortFormatter));
            dp.setFullDate(point.date.format(fullFormatter));
            dp.setAvgPrice(point.avgPrice != null ? point.avgPrice.setScale(1, RoundingMode.HALF_UP) : null);
            dp.setPriceUnit("元/kg");
            dp.setOfferCount(null);
            history.add(dp);
        }

        return history;
    }

    /**
     * 获取近30日价格趋势（使用 stat_price_trend 表，带IQR过滤）
     */
    private List<DailyPrice> getPriceHistory30Days(String country, String productName, String category, String offerType) {
        DictProduct product = productMapper.findByName(category, productName);
        if (product == null || product.getProductId() == null) {
            return Collections.emptyList();
        }

        List<StatPriceTrendMapper.PriceTrendPoint> trendPoints = trendMapper.selectTrendPointsByCountryProduct(
                StatPriceTrendMapper.DIMENSION_COUNTRY_PRODUCT,
                country,
                product.getProductId(),
                offerType
        );

        if (trendPoints == null || trendPoints.isEmpty()) {
            return Collections.emptyList();
        }

        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("MM-dd");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DailyPrice> history = new ArrayList<>();

        for (StatPriceTrendMapper.PriceTrendPoint point : trendPoints) {
            DailyPrice dp = new DailyPrice();
            dp.setDate(point.date.format(shortFormatter));
            dp.setFullDate(point.date.format(fullFormatter));
            dp.setAvgPrice(point.avgPrice != null ? point.avgPrice.setScale(1, RoundingMode.HALF_UP) : null);
            dp.setPriceUnit("元/kg");
            dp.setOfferCount(null);
            history.add(dp);
        }

        return history;
    }

    /**
     * 转换聚合数据为DTO
     */
    private CountryProductFactoryDTO convertToFactoryDTO(BizOfferMapper.CountryProductFactoryAgg agg) {
        CountryProductFactoryDTO dto = new CountryProductFactoryDTO();
        dto.setCountry(agg.country);
        dto.setFactoryNo(agg.factoryNo);
        dto.setCountryFactory((agg.country != null ? agg.country : "") + " " + (agg.factoryNo != null ? agg.factoryNo : ""));
        dto.setPriceMin(agg.priceMin);
        dto.setPriceMax(agg.priceMax);
        dto.setMerchantCount(agg.merchantCount);
        dto.setOfferCount(agg.offerCount);

        // 解析商家名称（shortName|fullName格式，最多取前3个）
        if (agg.merchantNames != null && !agg.merchantNames.isEmpty()) {
            List<String> names = Arrays.stream(agg.merchantNames.split(","))
                    .filter(n -> n != null && !n.isEmpty())
                    .map(raw -> {
                        int sep = raw.indexOf('|');
                        if (sep > 0) {
                            String shortName = raw.substring(0, sep);
                            String fullName = raw.substring(sep + 1);
                            return (!shortName.isEmpty() && !"NULL".equalsIgnoreCase(shortName)) ? shortName : fullName;
                        }
                        return raw;
                    })
                    .collect(Collectors.toList());
            dto.setMerchantNames(names);
        } else {
            dto.setMerchantNames(Collections.emptyList());
        }

        return dto;
    }
}
