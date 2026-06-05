package com.mooket.social.service.impl;

import com.mooket.social.dto.BrandDetailDTO;
import com.mooket.social.dto.BrandProductSummaryDTO;
import com.mooket.social.entity.DictBrand;
import com.mooket.social.entity.DictProduct;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictBrandMapper;
import com.mooket.social.mapper.DictProductMapper;
import com.mooket.social.mapper.StatBrandMapper;
import com.mooket.social.service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    private static final Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

    private final BizOfferMapper offerMapper;
    private final StatBrandMapper statBrandMapper;
    private final DictBrandMapper dictBrandMapper;
    private final DictProductMapper dictProductMapper;

    public BrandServiceImpl(BizOfferMapper offerMapper,
                            StatBrandMapper statBrandMapper,
                            DictBrandMapper dictBrandMapper,
                            DictProductMapper dictProductMapper) {
        this.offerMapper = offerMapper;
        this.statBrandMapper = statBrandMapper;
        this.dictBrandMapper = dictBrandMapper;
        this.dictProductMapper = dictProductMapper;
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

        String dbOfferType = "offer".equalsIgnoreCase(type) ? "报盘" : "求购";
        String normalizedSortBy = normalizeSortBy(sortBy);
        int offset = (page - 1) * pageSize;

        List<DictBrand> brandList = dictBrandMapper.selectByName(brandName);
        boolean isRealBrand = !brandList.isEmpty();

        long todayOfferCount = 0L;
        long yesterdayOfferCount = 0L;
        long todayInquiryCount = 0L;
        long yesterdayInquiryCount = 0L;
        int totalCount;
        List<BizOfferMapper.BrandProductAgg> aggList;

        if (isRealBrand) {
            List<Integer> brandIds = brandList.stream()
                    .map(DictBrand::getBrandId)
                    .collect(Collectors.toList());

            BizOfferMapper.BrandStatByType offerStat = offerMapper.countByBrandIdsAndType(brandIds, category, "报盘");
            if (offerStat != null) {
                todayOfferCount = valueOrZero(offerStat.todayCount);
                yesterdayOfferCount = valueOrZero(offerStat.yesterdayCount);
            }
            BizOfferMapper.BrandStatByType inquiryStat = offerMapper.countByBrandIdsAndType(brandIds, category, "求购");
            if (inquiryStat != null) {
                todayInquiryCount = valueOrZero(inquiryStat.todayCount);
                yesterdayInquiryCount = valueOrZero(inquiryStat.yesterdayCount);
            }

            totalCount = offerMapper.countBrandProductAggByBrandIds(brandIds, category, dbOfferType);
            aggList = offerMapper.selectBrandProductAggByBrandIds(
                    brandIds, category, dbOfferType, normalizedSortBy, pageSize, offset);
            dto.setFactoryCount((int) brandList.stream()
                    .map(DictBrand::getFactoryNo)
                    .filter(factoryNo -> factoryNo != null && !factoryNo.isBlank())
                    .distinct()
                    .count());
        } else {
            BizOfferMapper.BrandStatByType offerStat = offerMapper.countByProductNameAndType(brandName, category, "报盘");
            if (offerStat != null) {
                todayOfferCount = valueOrZero(offerStat.todayCount);
                yesterdayOfferCount = valueOrZero(offerStat.yesterdayCount);
            }
            BizOfferMapper.BrandStatByType inquiryStat = offerMapper.countByProductNameAndType(brandName, category, "求购");
            if (inquiryStat != null) {
                todayInquiryCount = valueOrZero(inquiryStat.todayCount);
                yesterdayInquiryCount = valueOrZero(inquiryStat.yesterdayCount);
            }

            totalCount = offerMapper.countBrandProductAggByProductName(brandName, category, dbOfferType);
            aggList = offerMapper.selectBrandProductAggByProductName(
                    brandName, category, dbOfferType, normalizedSortBy, pageSize, offset);
        }

        List<BrandProductSummaryDTO> summaries = aggList.stream()
                .map(this::toBrandProductSummary)
                .collect(Collectors.toList());

        dto.setSummaries(summaries);
        dto.setProductCount(totalCount);
        dto.setTodayOfferCount(todayOfferCount);
        dto.setYesterdayOfferCount(yesterdayOfferCount);
        dto.setTotalOfferCount(todayOfferCount + yesterdayOfferCount);
        dto.setTodayInquiryCount(todayInquiryCount);
        dto.setYesterdayInquiryCount(yesterdayInquiryCount);
        dto.setTotalInquiryCount(todayInquiryCount + yesterdayInquiryCount);
        dto.setPriceMin(summaries.stream()
                .map(BrandProductSummaryDTO::getPriceMin)
                .filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(null));
        dto.setPriceMax(summaries.stream()
                .map(BrandProductSummaryDTO::getPriceMax)
                .filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
                .max(BigDecimal::compareTo)
                .orElse(null));
        dto.setMerchantCount(null);
        dto.setTotalCount(totalCount);
        dto.setTotalPages((int) Math.ceil((double) totalCount / pageSize));

        if (!isRealBrand) {
            dto.setFactoryCount(countFactoriesFromSummaries(summaries));
        }
        return dto;
    }

    @Override
    @Cacheable(value = "brandProductDetail", key = "#brandName + '_' + #productName + '_' + #category + '_' + #type + '_' + #sortBy + '_' + #page + '_' + #pageSize")
    public BrandDetailDTO getBrandProductDetail(String brandName, String productName, String category, String type, String sortBy, int page, int pageSize) {
        log.info("getBrandProductDetail: brandName={}, productName={}, category={}, type={}, sortBy={}, page={}, pageSize={}",
                brandName, productName, category, type, sortBy, page, pageSize);

        BrandDetailDTO dto = new BrandDetailDTO();
        dto.setBrandName(brandName + " " + productName);
        dto.setPage(page);
        dto.setPageSize(pageSize);

        List<DictBrand> brandList = dictBrandMapper.selectByName(brandName);
        if (brandList.isEmpty()) {
            dto.setSummaries(Collections.emptyList());
            dto.setTotalCount(0);
            dto.setTotalPages(0);
            return dto;
        }

        List<Integer> brandIds = brandList.stream()
                .map(DictBrand::getBrandId)
                .collect(Collectors.toList());
        DictProduct product = dictProductMapper.findByName(category, productName);
        Integer productId = product != null ? product.getProductId() : null;
        String dbOfferType = "offer".equalsIgnoreCase(type) ? "报盘" : "求购";
        String normalizedSortBy = normalizeSortBy(sortBy);
        int offset = (page - 1) * pageSize;

        long todayOfferCount = 0L;
        long yesterdayOfferCount = 0L;
        long todayInquiryCount = 0L;
        long yesterdayInquiryCount = 0L;
        int totalCount;
        List<BizOfferMapper.BrandProductDetailAgg> aggList;

        if (productId != null) {
            BizOfferMapper.BrandStatByType offerStat = offerMapper.countByBrandIdsAndProductIdAndType(brandIds, productId, category, "报盘");
            if (offerStat != null) {
                todayOfferCount = valueOrZero(offerStat.todayCount);
                yesterdayOfferCount = valueOrZero(offerStat.yesterdayCount);
            }
            BizOfferMapper.BrandStatByType inquiryStat = offerMapper.countByBrandIdsAndProductIdAndType(brandIds, productId, category, "求购");
            if (inquiryStat != null) {
                todayInquiryCount = valueOrZero(inquiryStat.todayCount);
                yesterdayInquiryCount = valueOrZero(inquiryStat.yesterdayCount);
            }

            totalCount = offerMapper.countBrandProductDetailByBrandIdsAndProductId(brandIds, productId, category, dbOfferType);
            aggList = offerMapper.selectBrandProductDetailByBrandIdsAndProductId(
                    brandIds, productId, productName, category, dbOfferType, normalizedSortBy, pageSize, offset);
        } else {
            BizOfferMapper.BrandStatByType offerStat = offerMapper.countByBrandIdsAndProductNameAndType(brandIds, productName, category, "报盘");
            if (offerStat != null) {
                todayOfferCount = valueOrZero(offerStat.todayCount);
                yesterdayOfferCount = valueOrZero(offerStat.yesterdayCount);
            }
            BizOfferMapper.BrandStatByType inquiryStat = offerMapper.countByBrandIdsAndProductNameAndType(brandIds, productName, category, "求购");
            if (inquiryStat != null) {
                todayInquiryCount = valueOrZero(inquiryStat.todayCount);
                yesterdayInquiryCount = valueOrZero(inquiryStat.yesterdayCount);
            }

            totalCount = offerMapper.countBrandProductDetailByBrandIdsAndProductName(brandIds, productName, category, dbOfferType);
            aggList = offerMapper.selectBrandProductDetailByBrandIdsAndProductName(
                    brandIds, productName, category, dbOfferType, normalizedSortBy, pageSize, offset);
        }

        List<BrandProductSummaryDTO> summaries = aggList.stream()
                .map(agg -> {
                    BrandProductSummaryDTO summary = new BrandProductSummaryDTO();
                    summary.setCountry(agg.country);
                    summary.setFactoryNo(agg.factoryNo);
                    summary.setCountryFactory(buildCountryFactory(agg.country, agg.factoryNo));
                    summary.setProductId(agg.productId);
                    summary.setProductName(productName);
                    summary.setPriceMin(agg.priceMin);
                    summary.setPriceMax(agg.priceMax);
                    summary.setOfferCount(agg.offerCount);
                    summary.setMerchantCount(agg.merchantCount);
                    summary.setMerchantNames(parseMerchantNames(agg.merchantNames));
                    return summary;
                })
                .collect(Collectors.toList());

        dto.setSummaries(summaries);
        dto.setFactoryCount(totalCount);
        dto.setProductCount(totalCount);
        dto.setTodayOfferCount(todayOfferCount);
        dto.setYesterdayOfferCount(yesterdayOfferCount);
        dto.setTotalOfferCount(todayOfferCount + yesterdayOfferCount);
        dto.setTodayInquiryCount(todayInquiryCount);
        dto.setYesterdayInquiryCount(yesterdayInquiryCount);
        dto.setTotalInquiryCount(todayInquiryCount + yesterdayInquiryCount);
        dto.setPriceMin(summaries.stream()
                .map(BrandProductSummaryDTO::getPriceMin)
                .filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(null));
        dto.setPriceMax(summaries.stream()
                .map(BrandProductSummaryDTO::getPriceMax)
                .filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
                .max(BigDecimal::compareTo)
                .orElse(null));
        dto.setMerchantCount(summaries.stream()
                .map(BrandProductSummaryDTO::getMerchantCount)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum());
        dto.setTotalCount(totalCount);
        dto.setTotalPages((int) Math.ceil((double) totalCount / pageSize));
        return dto;
    }

    private BrandProductSummaryDTO toBrandProductSummary(BizOfferMapper.BrandProductAgg agg) {
        BrandProductSummaryDTO summary = new BrandProductSummaryDTO();
        summary.setProductId(agg.productId);
        summary.setProductName(agg.productName);
        summary.setPriceMin(agg.priceMin);
        summary.setPriceMax(agg.priceMax);
        summary.setFactoryNos(agg.factoryNos);
        summary.setFactoryCount(agg.factoryCount);
        summary.setOfferCount(agg.offerCount);
        return summary;
    }

    private String buildCountryFactory(String country, String factoryNo) {
        if (country != null && factoryNo != null) {
            return country + " " + factoryNo;
        }
        if (country != null) {
            return country;
        }
        return factoryNo;
    }

    private int countFactoriesFromSummaries(List<BrandProductSummaryDTO> summaries) {
        Set<String> factories = new LinkedHashSet<>();
        for (BrandProductSummaryDTO summary : summaries) {
            if (summary.getFactoryNos() == null || summary.getFactoryNos().isBlank()) {
                continue;
            }
            factories.addAll(Arrays.stream(summary.getFactoryNos().split(","))
                    .filter(factoryNo -> factoryNo != null && !factoryNo.trim().isEmpty())
                    .collect(Collectors.toSet()));
        }
        return factories.size();
    }

    private long valueOrZero(Long value) {
        return value != null ? value : 0L;
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "comprehensive";
        }
        return sortBy;
    }

    private List<String> parseMerchantNames(String merchantNames) {
        if (merchantNames == null || merchantNames.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(merchantNames.split(","))
                .filter(raw -> raw != null && !raw.trim().isEmpty())
                .map(raw -> {
                    String trimmed = raw.trim();
                    int sep = trimmed.indexOf('|');
                    if (sep > 0) {
                        String shortName = trimmed.substring(0, sep).trim();
                        String fullName = trimmed.substring(sep + 1).trim();
                        return (!shortName.isEmpty() && !"NULL".equalsIgnoreCase(shortName)) ? shortName : fullName;
                    }
                    return trimmed;
                })
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
