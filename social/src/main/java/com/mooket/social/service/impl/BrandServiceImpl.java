package com.mooket.social.service.impl;

import com.mooket.social.dto.BrandDetailDTO;
import com.mooket.social.dto.BrandProductSummaryDTO;
import com.mooket.social.entity.DictBrand;
import com.mooket.social.entity.StatBrand;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictBrandMapper;
import com.mooket.social.mapper.StatBrandMapper;
import com.mooket.social.service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 品牌 Service 实现
 */
@Service
public class BrandServiceImpl implements BrandService {

private final BizOfferMapper offerMapper;
    private final StatBrandMapper statBrandMapper;
    private final DictBrandMapper dictBrandMapper;
    private static final Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

    public BrandServiceImpl(BizOfferMapper offerMapper, StatBrandMapper statBrandMapper, DictBrandMapper dictBrandMapper) {
        this.offerMapper = offerMapper;
        this.statBrandMapper = statBrandMapper;
        this.dictBrandMapper = dictBrandMapper;
    }

    @Override
    @Cacheable(value = {"brandDetail"}, key = "#brandName + '_' + #category + '_' + #type + '_' + #sortBy + '_' + #page + '_' + #pageSize")
    public BrandDetailDTO getBrandDetail(String brandName, String category, String type, String sortBy, int page, int pageSize) {
        log.info("getBrandDetail: brandName={}, category={}, type={}, sortBy={}, page={}, pageSize={}",
                brandName, category, type, sortBy, page, pageSize);

        BrandDetailDTO dto = new BrandDetailDTO();
        dto.setBrandName(brandName);
        dto.setPage(page);
        dto.setPageSize(pageSize);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        String dbOfferType = "offer".equalsIgnoreCase(type) ? "报盘" : "求购";

        // ========== 核心：判断是品牌还是产品，选择查询路径 ==========
        // 查 dict_brand：确认 brandName 是否是真正的品牌（可能有多条记录，一个品牌对应多个厂号）
        List<DictBrand> brandList = dictBrandMapper.selectByName(brandName);
        boolean isRealBrand = !brandList.isEmpty();

        // 统计数据和聚合结果
        Long todayOfferCount = 0L;
        Long yesterdayOfferCount = 0L;
        Long todayInquiryCount = 0L;
        Long yesterdayInquiryCount = 0L;
        int factoryCount = 0;

        List<BizOfferMapper.BrandProductAgg> aggList;

        if (isRealBrand) {
            // ========== 路径A：真实品牌（dict_brand 有记录）→ 用所有 brand_id 查 biz_offer ==========
            // 一个品牌 = 多个 brand_id（每个厂号一个 brand_id）
            List<Integer> brandIds = brandList.stream().map(DictBrand::getBrandId).collect(Collectors.toList());
            log.info("getBrandDetail: brandName={} 是真实品牌, brandIds={} (共{}个厂号)", brandName, brandIds, brandIds.size());

            // 报盘/求购计数（按所有 brand_id + type 精确查）
            BizOfferMapper.BrandStatByType offerStat = offerMapper.countByBrandIdsAndType(brandIds, category, "报盘");
            if (offerStat != null) {
                todayOfferCount = offerStat.todayCount != null ? offerStat.todayCount : 0L;
                yesterdayOfferCount = offerStat.yesterdayCount != null ? offerStat.yesterdayCount : 0L;
            }
            BizOfferMapper.BrandStatByType inquiryStat = offerMapper.countByBrandIdsAndType(brandIds, category, "求购");
            if (inquiryStat != null) {
                todayInquiryCount = inquiryStat.todayCount != null ? inquiryStat.todayCount : 0L;
                yesterdayInquiryCount = inquiryStat.yesterdayCount != null ? inquiryStat.yesterdayCount : 0L;
            }

            // 聚合查询（按所有 brand_id）
            aggList = offerMapper.selectBrandProductAggByBrandIds(brandIds, category, dbOfferType);

        } else {
            // ========== 路径B：产品名（非品牌，如"牛前八件套"）→ 用 product_name 查 biz_offer ==========
            log.info("getBrandDetail: brandName={} 是产品名（非品牌）", brandName);

            if ("inquiry".equalsIgnoreCase(type)) {
                // 求购：直接 SUM 聚合结果
                aggList = offerMapper.selectBrandProductAggByProductName(brandName, category, "求购");
                long totalInquiry = aggList.stream().mapToLong(a -> a.offerCount != null ? a.offerCount : 0L).sum();
                todayInquiryCount = totalInquiry;
                yesterdayInquiryCount = 0L;
            } else {
                // 报盘：按 product_name + type 精确查今日/昨日
                BizOfferMapper.BrandStatByType offerStat = offerMapper.countByProductNameAndType(brandName, category, "报盘");
                if (offerStat != null) {
                    todayOfferCount = offerStat.todayCount != null ? offerStat.todayCount : 0L;
                    yesterdayOfferCount = offerStat.yesterdayCount != null ? offerStat.yesterdayCount : 0L;
                }
                aggList = offerMapper.selectBrandProductAggByProductName(brandName, category, "报盘");
            }
        }

        // 设置统计数据
        dto.setFactoryCount(factoryCount);
        dto.setProductCount(0);
        dto.setTodayOfferCount(todayOfferCount);
        dto.setYesterdayOfferCount(yesterdayOfferCount);
        dto.setTotalOfferCount(todayOfferCount + yesterdayOfferCount);
        dto.setTodayInquiryCount(todayInquiryCount);
        dto.setYesterdayInquiryCount(yesterdayInquiryCount);
        dto.setTotalInquiryCount(todayInquiryCount + yesterdayInquiryCount);

        // 获取产品汇总列表
        List<BrandProductSummaryDTO> summaries = new ArrayList<>();
        for (BizOfferMapper.BrandProductAgg agg : aggList) {
            BrandProductSummaryDTO summary = new BrandProductSummaryDTO();
            summary.setProductId(agg.productId);
            summary.setProductName(agg.productName);
            summary.setPriceMin(agg.priceMin);
            summary.setPriceMax(agg.priceMax);
            summary.setFactoryNos(agg.factoryNos);
            summary.setFactoryCount(agg.factoryCount);
            summary.setOfferCount(agg.offerCount);
            summaries.add(summary);
        }

        // 从聚合结果推导 factoryCount（不同 factory_no 的去重数量）
        factoryCount = (int) aggList.stream()
                    .flatMap(a -> Arrays.stream(a.factoryNos.split(",")))
                    .filter(f -> f != null && !f.trim().isEmpty())
                    .distinct()
                    .count();

        // 设置最终的 factoryCount
        dto.setFactoryCount(factoryCount);

        // 按 offerCount 排序
        if ("price".equals(sortBy)) {
            // 价格排序时按价格区间排序（简化为按 priceMin 排序）
            summaries.sort((a, b) -> {
                if (a.getPriceMin() == null && b.getPriceMin() == null) return 0;
                if (a.getPriceMin() == null) return 1;
                if (b.getPriceMin() == null) return -1;
                return a.getPriceMin().compareTo(b.getPriceMin());
            });
        } else {
            // 综合排序按报盘数
            summaries.sort((a, b) -> Integer.compare(
                    b.getOfferCount() != null ? b.getOfferCount() : 0,
                    a.getOfferCount() != null ? a.getOfferCount() : 0
            ));
        }

        // ========== 先按 productId 合并，再分页（解决前端重复合并导致的列表抖动） ==========
        // groupBy productId：将同一产品的多个厂号合并为一条记录
        Map<Integer, BrandProductSummaryDTO> mergedMap = new LinkedHashMap<>();
        for (BrandProductSummaryDTO s : summaries) {
            if (mergedMap.containsKey(s.getProductId())) {
                BrandProductSummaryDTO existing = mergedMap.get(s.getProductId());
                // 追加厂号（逗号分隔去重）
                Set<String> factoryNoSet = new LinkedHashSet<>(Arrays.asList(existing.getFactoryNos().split(",")));
                Collections.addAll(factoryNoSet, s.getFactoryNos().split(","));
                String combinedFactoryNos = String.join(",", factoryNoSet.stream().filter(f -> f != null && !f.trim().isEmpty()).toArray(String[]::new));
                existing.setFactoryNos(combinedFactoryNos);
                existing.setFactoryCount(factoryNoSet.size());
                // 价格区间取全局 min/max
                if (s.getPriceMin() != null && (existing.getPriceMin() == null || s.getPriceMin().compareTo(existing.getPriceMin()) < 0)) {
                    existing.setPriceMin(s.getPriceMin());
                }
                if (s.getPriceMax() != null && (existing.getPriceMax() == null || s.getPriceMax().compareTo(existing.getPriceMax()) > 0)) {
                    existing.setPriceMax(s.getPriceMax());
                }
                // 报盘数累加
                existing.setOfferCount(existing.getOfferCount() + s.getOfferCount());
            } else {
                mergedMap.put(s.getProductId(), s);
            }
        }
        List<BrandProductSummaryDTO> mergedSummaries = new ArrayList<>(mergedMap.values());

        // 对合并后的数据重新排序
        if ("price_asc".equals(sortBy)) {
            // 升序：取每个产品近两日报盘价格区间的最小值，升序排列；协商报价放最后
            mergedSummaries.sort((a, b) -> {
                boolean aHas = a.getPriceMin() != null && a.getPriceMin().compareTo(BigDecimal.ZERO) > 0;
                boolean bHas = b.getPriceMin() != null && b.getPriceMin().compareTo(BigDecimal.ZERO) > 0;
                if (!aHas && !bHas) return 0;
                if (!aHas) return 1;
                if (!bHas) return -1;
                return a.getPriceMin().compareTo(b.getPriceMin());
            });
        } else if ("price_desc".equals(sortBy)) {
            // 降序：取每个产品近两日报盘价格区间的最大值，降序排列；协商报价放最后
            mergedSummaries.sort((a, b) -> {
                boolean aHas = a.getPriceMax() != null && a.getPriceMax().compareTo(BigDecimal.ZERO) > 0;
                boolean bHas = b.getPriceMax() != null && b.getPriceMax().compareTo(BigDecimal.ZERO) > 0;
                if (!aHas && !bHas) return 0;
                if (!aHas) return 1;
                if (!bHas) return -1;
                return b.getPriceMax().compareTo(a.getPriceMax());
            });
        } else {
            // 综合推荐：按报盘数降序
            mergedSummaries.sort((a, b) -> Integer.compare(
                    b.getOfferCount() != null ? b.getOfferCount() : 0,
                    a.getOfferCount() != null ? a.getOfferCount() : 0
            ));
        }

        // productCount = 合并后的产品数
        dto.setProductCount(mergedSummaries.size());

        // 分页（基于合并后的条数）
        int totalCount = mergedSummaries.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalCount);

        if (fromIndex >= totalCount) {
            dto.setSummaries(new ArrayList<>());
        } else {
            dto.setSummaries(new ArrayList<>(mergedSummaries.subList(fromIndex, toIndex)));
        }

        dto.setTotalCount(totalCount);
        dto.setTotalPages(totalPages);

        log.info("getBrandDetail result: factoryCount={}, productCount={}, todayOffer={}, yesterdayOffer={}, totalOffer={}, todayInquiry={}, yesterdayInquiry={}, totalInquiry={}, summaries.size={}, totalCount={}, totalPages={}",
                factoryCount, dto.getProductCount(), todayOfferCount, yesterdayOfferCount, dto.getTotalOfferCount(),
                todayInquiryCount, yesterdayInquiryCount, dto.getTotalInquiryCount(),
                dto.getSummaries().size(), dto.getTotalCount(), dto.getTotalPages());

        return dto;
    }

    @Override
    public BrandDetailDTO getBrandProductDetail(String brandName, String productName, String category, String type, String sortBy, int page, int pageSize) {
        log.info("getBrandProductDetail: brandName={}, productName={}, category={}, type={}, sortBy={}, page={}, pageSize={}",
                brandName, productName, category, type, sortBy, page, pageSize);

        BrandDetailDTO dto = new BrandDetailDTO();
        dto.setBrandName(brandName + " " + productName);
        dto.setPage(page);
        dto.setPageSize(pageSize);

        LocalDate today = LocalDate.now();
        String dbOfferType = "offer".equalsIgnoreCase(type) ? "报盘" : "求购";

        // 查询该品牌的所有 brand_id（一个品牌可能有多个厂号）
        List<DictBrand> brandList = dictBrandMapper.selectByName(brandName);
        if (brandList.isEmpty()) {
            log.info("getBrandProductDetail: brandName={} 在 dict_brand 中不存在", brandName);
            dto.setSummaries(new ArrayList<>());
            dto.setTotalCount(0);
            dto.setTotalPages(0);
            return dto;
        }

        List<Integer> brandIds = brandList.stream().map(DictBrand::getBrandId).collect(Collectors.toList());

        // 近2日计数
        Long todayOfferCount = 0L;
        Long yesterdayOfferCount = 0L;
        Long todayInquiryCount = 0L;
        Long yesterdayInquiryCount = 0L;

        BizOfferMapper.BrandStatByType offerStat = offerMapper.countByBrandIdsAndProductNameAndType(brandIds, productName, category, "报盘");
        if (offerStat != null) {
            todayOfferCount = offerStat.todayCount != null ? offerStat.todayCount : 0L;
            yesterdayOfferCount = offerStat.yesterdayCount != null ? offerStat.yesterdayCount : 0L;
        }
        BizOfferMapper.BrandStatByType inquiryStat = offerMapper.countByBrandIdsAndProductNameAndType(brandIds, productName, category, "求购");
        if (inquiryStat != null) {
            todayInquiryCount = inquiryStat.todayCount != null ? inquiryStat.todayCount : 0L;
            yesterdayInquiryCount = inquiryStat.yesterdayCount != null ? inquiryStat.yesterdayCount : 0L;
        }

        // 聚合查询（按 brandIds + productName，按 country + factory_no 分组）
        List<BizOfferMapper.BrandProductDetailAgg> aggList = offerMapper.selectBrandProductDetailByBrandIdsAndProductName(brandIds, productName, category, dbOfferType);

        // 设置统计数据
        int factoryCount = (int) aggList.stream()
                    .map(a -> a.country + "_" + a.factoryNo)
                    .filter(cf -> cf != null && !cf.contains("null") && !cf.trim().isEmpty())
                    .distinct()
                    .count();

        dto.setFactoryCount(factoryCount);
        dto.setProductCount(aggList.size());
        dto.setTodayOfferCount(todayOfferCount);
        dto.setYesterdayOfferCount(yesterdayOfferCount);
        dto.setTotalOfferCount(todayOfferCount + yesterdayOfferCount);
        dto.setTodayInquiryCount(todayInquiryCount);
        dto.setYesterdayInquiryCount(yesterdayInquiryCount);
        dto.setTotalInquiryCount(todayInquiryCount + yesterdayInquiryCount);

        // 构建 summaries（按 country + factory 分组，包含商家信息）
        List<BrandProductSummaryDTO> summaries = new ArrayList<>();
        for (BizOfferMapper.BrandProductDetailAgg agg : aggList) {
            BrandProductSummaryDTO summary = new BrandProductSummaryDTO();
            summary.setCountry(agg.country);
            summary.setFactoryNo(agg.factoryNo);
            // countryFactory 组合显示
            String countryFactory = "";
            if (agg.country != null && agg.factoryNo != null) {
                countryFactory = agg.country + " " + agg.factoryNo;
            } else if (agg.country != null) {
                countryFactory = agg.country;
            } else if (agg.factoryNo != null) {
                countryFactory = agg.factoryNo;
            }
            summary.setCountryFactory(countryFactory);
            summary.setProductId(agg.productId);
            summary.setProductName(productName); // 固定为搜索的产品名
            summary.setPriceMin(agg.priceMin);
            summary.setPriceMax(agg.priceMax);
            summary.setOfferCount(agg.offerCount);
            summary.setMerchantCount(agg.merchantCount);
            // merchantNames 是 "shortName|fullName,shortName|fullName" 格式，解析为 List
            if (agg.merchantNames != null && !agg.merchantNames.isEmpty()) {
                summary.setMerchantNames(Arrays.asList(agg.merchantNames.split(",")));
            } else {
                summary.setMerchantNames(new ArrayList<>());
            }
            summaries.add(summary);
        }

// ========== 从全量 aggList 聚合看板数据（在排序/pagination 之前计算，保持稳定）==========
        BigDecimal fullPriceMin = aggList.stream()
                .map(a -> a.priceMin)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal fullPriceMax = aggList.stream()
                .map(a -> a.priceMax)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .max(BigDecimal::compareTo)
                .orElse(null);
        int fullMerchantCount = aggList.stream()
                .mapToInt(a -> a.merchantCount != null ? a.merchantCount : 0)
                .sum();
        dto.setPriceMin(fullPriceMin);
        dto.setPriceMax(fullPriceMax);
        dto.setMerchantCount(fullMerchantCount);

// 按排序
        if ("price_asc".equals(sortBy)) {
            summaries.sort((a, b) -> {
                boolean aHas = a.getPriceMin() != null && a.getPriceMin().compareTo(BigDecimal.ZERO) > 0;
                boolean bHas = b.getPriceMin() != null && b.getPriceMin().compareTo(BigDecimal.ZERO) > 0;
                if (!aHas && !bHas) return 0;
                if (!aHas) return 1;
                if (!bHas) return -1;
                return a.getPriceMin().compareTo(b.getPriceMin());
            });
        } else if ("price_desc".equals(sortBy)) {
            summaries.sort((a, b) -> {
                boolean aHas = a.getPriceMax() != null && a.getPriceMax().compareTo(BigDecimal.ZERO) > 0;
                boolean bHas = b.getPriceMax() != null && b.getPriceMax().compareTo(BigDecimal.ZERO) > 0;
                if (!aHas && !bHas) return 0;
                if (!aHas) return 1;
                if (!bHas) return -1;
                return b.getPriceMax().compareTo(a.getPriceMax());
            });
        } else {
            summaries.sort((a, b) -> Integer.compare(
                    b.getOfferCount() != null ? b.getOfferCount() : 0,
                    a.getOfferCount() != null ? a.getOfferCount() : 0
            ));
        }

        int totalCount = summaries.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalCount);

        if (fromIndex >= totalCount) {
            dto.setSummaries(new ArrayList<>());
        } else {
            dto.setSummaries(new ArrayList<>(summaries.subList(fromIndex, toIndex)));
        }

        dto.setTotalCount(totalCount);
        dto.setTotalPages(totalPages);

        log.info("getBrandProductDetail result: brandName={}, productName={}, factoryCount={}, productCount={}, todayOffer={}, yesterdayOffer={}, totalOffer={}, summaries.size={}, totalCount={}, totalPages={}",
                brandName, productName, factoryCount, dto.getProductCount(), todayOfferCount, yesterdayOfferCount, dto.getTotalOfferCount(),
                dto.getSummaries().size(), dto.getTotalCount(), dto.getTotalPages());

        return dto;
    }
}