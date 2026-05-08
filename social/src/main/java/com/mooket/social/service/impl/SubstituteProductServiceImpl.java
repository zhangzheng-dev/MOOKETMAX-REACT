package com.mooket.social.service.impl;

import com.mooket.social.dto.SubstituteProductDTO;
import com.mooket.social.dto.SubstituteProductDTO.*;
import com.mooket.social.entity.BizOffer;
import com.mooket.social.entity.DictMerchant;
import com.mooket.social.entity.DictProduct;
import com.mooket.social.entity.FactoryTier;
import com.mooket.social.mapper.*;
import com.mooket.social.service.SubstituteProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 平替产品服务实现
 */
@Service
public class SubstituteProductServiceImpl implements SubstituteProductService {

    private final FactoryTierMapper factoryTierMapper;
    private final BizOfferMapper offerMapper;
    private final StatPriceTrendMapper trendMapper;
    private final DictProductMapper productMapper;
    private final DictMerchantMapper merchantMapper;

    public SubstituteProductServiceImpl(FactoryTierMapper factoryTierMapper,
                                         BizOfferMapper offerMapper,
                                         StatPriceTrendMapper trendMapper,
                                         DictProductMapper productMapper,
                                         DictMerchantMapper merchantMapper) {
        this.factoryTierMapper = factoryTierMapper;
        this.offerMapper = offerMapper;
        this.trendMapper = trendMapper;
        this.productMapper = productMapper;
        this.merchantMapper = merchantMapper;
    }

    @Override
    public SubstituteProductDTO getSubstituteProducts(String country, String factoryNo, String productName, String category) {
        // 1. 查询当前厂号的等级
        String tier = factoryTierMapper.selectTierByFactoryNo(category, productName, factoryNo);
        if (tier == null) {
            return new SubstituteProductDTO();
        }

        // 2. 查询同产品同等级的所有厂号
        List<String> factoryNos = factoryTierMapper.selectFactoryNosByTier(category, productName, tier);
        if (factoryNos == null || factoryNos.isEmpty()) {
            return new SubstituteProductDTO();
        }

        // 3. 构建平替产品DTO
        SubstituteProductDTO dto = new SubstituteProductDTO();
        dto.setCategory(category);
        dto.setProductName(productName);
        dto.setCurrentFactoryNo(factoryNo);
        dto.setTier(tier);

        // 4. 获取每个厂号的价格区间和统计数据
        List<SubstituteFactory> factories = new ArrayList<>();
        for (String fn : factoryNos) {
            BizOfferMapper.PriceRange priceRange = offerMapper.selectFilteredPriceRangeByCountryFactoryProduct(
                    country, fn, productName, category, "报盘");
            BizOfferMapper.CountryFactoryProductStats stats = offerMapper.selectCountryFactoryProductStats(
                    country, fn, productName, category);

            SubstituteFactory sf = new SubstituteFactory();
            sf.setFactoryNo(fn);
            sf.setPriceMin(priceRange != null ? priceRange.priceMin : null);
            sf.setPriceMax(priceRange != null ? priceRange.priceMax : null);
            sf.setOfferCount(stats != null && stats.totalOfferCount != null ? stats.totalOfferCount : 0L);
            sf.setMerchantCount(stats != null && stats.merchantCount != null ? stats.merchantCount : 0);
            sf.setSelected(fn.equals(factoryNo));
            factories.add(sf);
        }

        dto.setFactories(factories);

        // 5. 计算总体价格区间
        BigDecimal minPrice = factories.stream()
                .map(SubstituteFactory::getPriceMin)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal maxPrice = factories.stream()
                .map(SubstituteFactory::getPriceMax)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(null);
        dto.setPriceMin(minPrice);
        dto.setPriceMax(maxPrice);

        // 6. 统计总报盘数和商家数
        long totalOfferCount = factories.stream()
                .mapToLong(SubstituteFactory::getOfferCount)
                .sum();
        int totalMerchantCount = factories.stream()
                .mapToInt(SubstituteFactory::getMerchantCount)
                .sum();
        dto.setOfferCount(totalOfferCount);
        dto.setMerchantCount(totalMerchantCount);

        return dto;
    }

    @Override
    public SubstituteProductDetailDTO getSubstituteProductDetail(String country, String factoryNo,
                                                                   String productName, String category,
                                                                   String type, String sortBy,
                                                                   int page, int pageSize) {
        SubstituteProductDetailDTO dto = new SubstituteProductDetailDTO();
        dto.setCountry(country);
        dto.setFactoryNo(factoryNo);
        dto.setProductName(productName);

        // 获取 productId
        DictProduct product = productMapper.findByName(category, productName);
        if (product != null && product.getProductId() != null) {
            dto.setProductId(product.getProductId());
        }

        // 获取等级
        String tier = factoryTierMapper.selectTierByFactoryNo(category, productName, factoryNo);
        dto.setTier(tier);

        // 看板统计
        BizOfferMapper.CountryFactoryProductStats stats = offerMapper.selectCountryFactoryProductStats(
                country, factoryNo, productName, category);
        dto.setOfferCount(stats != null && stats.totalOfferCount != null ? stats.totalOfferCount : 0L);
        dto.setInquiryCount(stats != null && stats.totalInquiryCount != null ? stats.totalInquiryCount : 0L);
        dto.setMerchantCount(stats != null && stats.merchantCount != null ? stats.merchantCount : 0);

        // 价格区间
        String offerType = "offer".equalsIgnoreCase(type) ? "报盘" : ("inquiry".equalsIgnoreCase(type) ? "求购" : null);
        BizOfferMapper.PriceRange priceRange = offerMapper.selectFilteredPriceRangeByCountryFactoryProduct(
                country, factoryNo, productName, category, offerType);
        dto.setPriceMin(priceRange != null ? priceRange.priceMin : null);
        dto.setPriceMax(priceRange != null ? priceRange.priceMax : null);

        // 涨跌
        calculatePriceChange(country, factoryNo, dto.getProductId(), productName, category, offerType, dto);

        // 价格走势
        dto.setPriceHistory7Days(getPriceHistory7Days(country, factoryNo, dto.getProductId(), productName, category, offerType));
        dto.setPriceHistory30Days(getPriceHistory30Days(country, factoryNo, dto.getProductId(), productName, category, offerType));

        // 报盘列表
        String dbSortBy = "price_asc".equals(sortBy) || "price_desc".equals(sortBy) ? "comprehensive" : sortBy;
        int fetchLimit = 1000;
        List<BizOffer> offers = offerMapper.selectOfferListByCountryFactoryProduct(
                country, factoryNo, productName, category, offerType, dbSortBy, fetchLimit, 0);

        List<MerchantOfferGroup> merchantGroups = groupOffersByMerchant(offers);

        // 内存排序
        if ("price_asc".equals(sortBy)) {
            // 升序：取每个产品近两日报盘价格区间的最小值，升序排列
            merchantGroups.sort((a, b) -> {
                BigDecimal priceA = getMinPrice(a.getEmployeeOffers());
                BigDecimal priceB = getMinPrice(b.getEmployeeOffers());
                return priceA.compareTo(priceB);
            });
        } else if ("price_desc".equals(sortBy)) {
            // 降序：取每个产品近两日报盘价格区间的最大值，降序排列
            merchantGroups.sort((a, b) -> {
                BigDecimal priceA = getMaxPrice(a.getEmployeeOffers());
                BigDecimal priceB = getMaxPrice(b.getEmployeeOffers());
                return priceB.compareTo(priceA);
            });
        } else if ("publish_time".equals(sortBy)) {
            merchantGroups.sort((a, b) -> {
                String timeA = a.getEmployeeOffers().stream()
                        .map(EmployeeOfferDTO::getPublishTime)
                        .filter(Objects::nonNull)
                        .max(String::compareTo)
                        .orElse("");
                String timeB = b.getEmployeeOffers().stream()
                        .map(EmployeeOfferDTO::getPublishTime)
                        .filter(Objects::nonNull)
                        .max(String::compareTo)
                        .orElse("");
                return timeB.compareTo(timeA);
            });
        } else {
            merchantGroups.sort((a, b) -> b.getOfferCount().compareTo(a.getOfferCount()));
        }

        int totalCount = merchantGroups.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, merchantGroups.size());
        List<MerchantOfferGroup> pagedGroups = fromIndex < merchantGroups.size()
                ? merchantGroups.subList(fromIndex, toIndex)
                : Collections.emptyList();

        dto.setMerchantOffers(pagedGroups);
        dto.setTotalCount(totalCount);
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setTotalPages(totalPages);

        return dto;
    }

    private void calculatePriceChange(String country, String factoryNo, Integer productId,
                                       String productName, String category, String offerType,
                                       SubstituteProductDetailDTO dto) {
        List<StatPriceTrendMapper.PriceTrendPoint> trendPoints = trendMapper.selectTrendPointsByCountryFactoryProduct(
                StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT,
                country,
                productId,
                factoryNo,
                offerType
        );

        if (trendPoints == null || trendPoints.isEmpty()) {
            dto.setPriceChange(null);
            dto.setPriceChangeRate(null);
            return;
        }

        trendPoints.sort(Comparator.comparing(p -> p.date));

        BigDecimal todayPrice = null;
        BigDecimal yesterdayPrice = null;

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        for (StatPriceTrendMapper.PriceTrendPoint point : trendPoints) {
            if (point.date.equals(today)) {
                todayPrice = point.avgPrice;
            } else if (point.date.equals(yesterday)) {
                yesterdayPrice = point.avgPrice;
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

    private List<DailyPrice> getPriceHistory7Days(String country, String factoryNo, Integer productId,
                                                   String productName, String category, String offerType) {
        if (productId == null) {
            return Collections.emptyList();
        }

        List<StatPriceTrendMapper.PriceTrendPoint> trendPoints = trendMapper.selectTrendPointsByCountryFactoryProduct(
                StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT,
                country,
                productId,
                factoryNo,
                offerType
        );

        if (trendPoints == null || trendPoints.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate sevenDaysAgo = LocalDate.now().minusDays(6);
        List<StatPriceTrendMapper.PriceTrendPoint> recentPoints = trendPoints.stream()
                .filter(p -> p.date != null && !p.date.isBefore(sevenDaysAgo))
                .collect(Collectors.toList());

        if (recentPoints.isEmpty()) {
            return Collections.emptyList();
        }

        java.time.format.DateTimeFormatter shortFormatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd");
        java.time.format.DateTimeFormatter fullFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return recentPoints.stream()
                .map(point -> {
                    DailyPrice dp = new DailyPrice();
                    dp.setDate(point.date.format(shortFormatter));
                    dp.setFullDate(point.date.format(fullFormatter));
                    dp.setAvgPrice(point.avgPrice != null ? point.avgPrice.setScale(1, RoundingMode.HALF_UP) : null);
                    dp.setPriceUnit("元/kg");
                    dp.setOfferCount(null);
                    return dp;
                })
                .collect(Collectors.toList());
    }

    private List<DailyPrice> getPriceHistory30Days(String country, String factoryNo, Integer productId,
                                                    String productName, String category, String offerType) {
        if (productId == null) {
            return Collections.emptyList();
        }

        List<StatPriceTrendMapper.PriceTrendPoint> trendPoints = trendMapper.selectTrendPointsByCountryFactoryProduct(
                StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT,
                country,
                productId,
                factoryNo,
                offerType
        );

        if (trendPoints == null || trendPoints.isEmpty()) {
            return Collections.emptyList();
        }

        java.time.format.DateTimeFormatter shortFormatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd");
        java.time.format.DateTimeFormatter fullFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return trendPoints.stream()
                .map(point -> {
                    DailyPrice dp = new DailyPrice();
                    dp.setDate(point.date.format(shortFormatter));
                    dp.setFullDate(point.date.format(fullFormatter));
                    dp.setAvgPrice(point.avgPrice != null ? point.avgPrice.setScale(1, RoundingMode.HALF_UP) : null);
                    dp.setPriceUnit("元/kg");
                    dp.setOfferCount(null);
                    return dp;
                })
                .collect(Collectors.toList());
    }

    private List<MerchantOfferGroup> groupOffersByMerchant(List<BizOffer> offers) {
        Map<String, List<BizOffer>> groupedByKey = new LinkedHashMap<>();

        for (BizOffer offer : offers) {
            String groupKey;
            if (offer.getMerchantId() != null) {
                groupKey = "merchant_" + offer.getMerchantId();
            } else {
                groupKey = "NO_MERCHANT";
            }
            groupedByKey.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(offer);
        }

        List<MerchantOfferGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<BizOffer>> entry : groupedByKey.entrySet()) {
            List<BizOffer> groupOffers = entry.getValue();
            if (groupOffers.isEmpty()) continue;

            BizOffer firstOffer = groupOffers.get(0);
            MerchantOfferGroup group = new MerchantOfferGroup();

            if (entry.getKey().startsWith("merchant_")) {
                Long merchantId = firstOffer.getMerchantId();
                group.setMerchantId(merchantId);
                group.setMerchantPhone(firstOffer.getContactPhone());

                DictMerchant merchant = merchantMapper.selectById(merchantId);
                if (merchant != null) {
                    group.setMerchantName(merchant.getMerchantName());
                    boolean isFamous = merchant.getMerchantTags() != null &&
                            merchant.getMerchantTags().contains("知名商家");
                    group.setFamousMerchant(isFamous);
                } else {
                    group.setMerchantName(firstOffer.getContactPhone());
                    group.setFamousMerchant(false);
                }
            } else {
                group.setMerchantId(null);
                group.setMerchantName("暂未关联行业商家");
                group.setMerchantPhone(firstOffer.getContactPhone());
                group.setFamousMerchant(false);
            }

            group.setOfferCount(groupOffers.size());

            List<EmployeeOfferDTO> employeeOfferDTOs = groupOffers.stream()
                    .map(this::convertToEmployeeOfferDTO)
                    .collect(Collectors.toList());
            group.setEmployeeOffers(employeeOfferDTOs);

            groups.add(group);
        }

        groups.sort((a, b) -> b.getOfferCount().compareTo(a.getOfferCount()));
        return groups;
    }

    private EmployeeOfferDTO convertToEmployeeOfferDTO(BizOffer offer) {
        EmployeeOfferDTO dto = new EmployeeOfferDTO();
        dto.setOfferId(offer.getOfferId());
        dto.setUserNickname(offer.getUserNickname());
        dto.setWeight(offer.getWeight());
        dto.setGoodsLocation(offer.getGoodsLocation());
        dto.setGoodsType(offer.getGoodsType());
        dto.setTags(offer.getTags());
        dto.setOfferType(offer.getOfferType());
        dto.setOfferOriginalText(offer.getOfferOriginalText());

        if (offer.getPublishTime() != null) {
            dto.setPublishTime(offer.getPublishTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        if (offer.getPrice() != null) {
            if (offer.getPriceMax() != null && !offer.getPriceMax().equals(offer.getPrice())) {
                dto.setPrice(String.format("%.1f-%.1f", offer.getPrice(), offer.getPriceMax()));
            } else {
                dto.setPrice(String.format("%.1f", offer.getPrice()));
            }
        } else {
            dto.setPrice("协商报价");
        }

        return dto;
    }

    private BigDecimal getMinPrice(List<EmployeeOfferDTO> offers) {
        return offers.stream()
                .map(EmployeeOfferDTO::getPrice)
                .filter(p -> p != null && !p.equals("协商报价"))
                .map(p -> {
                    if (p.contains("-")) {
                        return new BigDecimal(p.split("-")[0]);
                    }
                    return new BigDecimal(p);
                })
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getMaxPrice(List<EmployeeOfferDTO> offers) {
        return offers.stream()
                .map(EmployeeOfferDTO::getPrice)
                .filter(p -> p != null && !p.equals("协商报价"))
                .map(p -> {
                    if (p.contains("-")) {
                        String[] parts = p.split("-");
                        return new BigDecimal(parts[parts.length - 1]);
                    }
                    return new BigDecimal(p);
                })
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}