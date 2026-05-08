package com.mooket.social.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mooket.social.dto.CountryDetailDTO;
import com.mooket.social.dto.CountryProductSummaryDTO;
import com.mooket.social.dto.HotFactoryDTO;
import com.mooket.social.dto.HotProductDTO;
import com.mooket.social.entity.StatCountry;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.StatCountryMapper;
import com.mooket.social.service.CountryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service
/* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/service/impl/CountryServiceImpl.class */
public class CountryServiceImpl implements CountryService {
    private final BizOfferMapper offerMapper;
    private final StatCountryMapper statCountryMapper;
    private static final Logger log = LoggerFactory.getLogger((Class<?>) CountryServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public CountryServiceImpl(BizOfferMapper offerMapper, StatCountryMapper statCountryMapper) {
        this.offerMapper = offerMapper;
        this.statCountryMapper = statCountryMapper;
    }

    @Override // com.mooket.social.service.CountryService
    @Cacheable(value = {"countryDetail"}, key = "#country + '_' + #category + '_' + #offerType + '_' + #sortBy + '_' + #page + '_' + #pageSize")
    public CountryDetailDTO getCountryDetail(String country, String category, String offerType, String sortBy, int page, int pageSize) {
        return buildCountryDetail(country, category, offerType, sortBy, page, pageSize);
    }

    private CountryDetailDTO buildCountryDetail(String country, String category, String offerType, String sortBy, int page, int pageSize) {
        String dbOfferType = "offer".equalsIgnoreCase(offerType) ? "报盘" : "求购";
        boolean isOffer = "报盘".equals(dbOfferType);
        log.info("buildCountryDetail: country={}, category={}, offerType={}, dbOfferType={}, isOffer={}", country, category, offerType, dbOfferType, Boolean.valueOf(isOffer));
        CountryDetailDTO dto = new CountryDetailDTO();
        dto.setCountry(country);
        if (isOffer) {
            LocalDate today = LocalDate.now();
            List<StatCountry> statCountries = this.statCountryMapper.selectByDateAndCategory(today, category);
            log.info("buildCountryDetail statCountries: today={}, category={}, count={}", today, category, Integer.valueOf(statCountries.size()));
            StatCountry statCountry = null;
            Iterator<StatCountry> it = statCountries.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                StatCountry sc = it.next();
                log.info("  statCountry: country={}, offerCount={}, hotFactories={}", sc.getCountry(), sc.getTodayOfferCount(), sc.getHotFactories());
                if (sc.getCountry().equals(country)) {
                    statCountry = sc;
                    break;
                }
            }
            if (statCountry != null) {
                log.info("Found statCountry for {}: offerCount={}, hotFactories={}", country, statCountry.getTodayOfferCount(), statCountry.getHotFactories());
                dto.setOfferCount(Long.valueOf(statCountry.getTodayOfferCount() != null ? statCountry.getTodayOfferCount().longValue() : 0L));
                dto.setMerchantCount(Integer.valueOf(statCountry.getTodayMerchantCount() != null ? statCountry.getTodayMerchantCount().intValue() : 0));
                dto.setFactoryCount(Integer.valueOf(statCountry.getTodayFactoryCount() != null ? statCountry.getTodayFactoryCount().intValue() : 0));
                dto.setHotFactories(parseHotFactoriesFromJson(statCountry.getHotFactories()));
                dto.setHotProducts(parseHotProductsFromJson(statCountry.getHotProducts()));
            } else {
                log.warn("statCountry NOT found for country={}, category={}", country, category);
                dto.setOfferCount(0L);
                dto.setMerchantCount(0);
                dto.setFactoryCount(0);
                dto.setHotFactories(Collections.emptyList());
                dto.setHotProducts(Collections.emptyList());
            }
        } else {
            BizOfferMapper.CountryDashboardStats dashboardStats = this.offerMapper.selectCountryDashboardStats(country, category, dbOfferType);
            dto.setOfferCount(Long.valueOf(dashboardStats.totalOfferCount != null ? dashboardStats.totalOfferCount.longValue() : 0L));
            dto.setMerchantCount(Integer.valueOf(dashboardStats.merchantCount != null ? dashboardStats.merchantCount.intValue() : 0));
            dto.setFactoryCount(Integer.valueOf(dashboardStats.factoryCount != null ? dashboardStats.factoryCount.intValue() : 0));
            List<BizOfferMapper.HotFactoryAgg> hotFactoryAggList = this.offerMapper.selectHotFactories(country, category, dbOfferType);
            List<HotFactoryDTO> hotFactories = new ArrayList<>();
            for (int i = 0; i < hotFactoryAggList.size(); i++) {
                BizOfferMapper.HotFactoryAgg agg = hotFactoryAggList.get(i);
                HotFactoryDTO hotFactory = new HotFactoryDTO();
                hotFactory.setFactoryNo(agg.factoryNo);
                hotFactory.setOfferCount(agg.offerCount);
                hotFactory.setRank(Integer.valueOf(i + 1));
                hotFactories.add(hotFactory);
            }
            dto.setHotFactories(hotFactories);
            List<BizOfferMapper.HotProductAgg> hotProductAggList = this.offerMapper.selectHotProducts(country, category, dbOfferType);
            List<HotProductDTO> hotProducts = new ArrayList<>();
            for (int i2 = 0; i2 < hotProductAggList.size(); i2++) {
                BizOfferMapper.HotProductAgg agg2 = hotProductAggList.get(i2);
                HotProductDTO hotProduct = new HotProductDTO();
                hotProduct.setProductName(agg2.productName);
                hotProduct.setOfferCount(agg2.offerCount);
                hotProduct.setRank(Integer.valueOf(i2 + 1));
                hotProducts.add(hotProduct);
            }
            dto.setHotProducts(hotProducts);
        }
        List<BizOfferMapper.CountryProductAgg> aggList = this.offerMapper.selectCountryProductAgg(country, category, dbOfferType, 1000, 0);
        if (!aggList.isEmpty()) {
            BigDecimal priceMin = null;
            BigDecimal priceMax = null;
            for (BizOfferMapper.CountryProductAgg agg3 : aggList) {
                if (agg3.priceMin != null && (priceMin == null || agg3.priceMin.compareTo(priceMin) < 0)) {
                    priceMin = agg3.priceMin;
                }
                if (agg3.priceMax != null && (priceMax == null || agg3.priceMax.compareTo(priceMax) > 0)) {
                    priceMax = agg3.priceMax;
                }
            }
            dto.setPriceMin(priceMin);
            dto.setPriceMax(priceMax);
        }
        // Sort full dataset BEFORE pagination so page 1 and page 2 are consistently ordered
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            aggList.sort((a, b) -> {
                BigDecimal aPrice = a.priceMin != null ? a.priceMin : BigDecimal.ZERO;
                BigDecimal bPrice = b.priceMin != null ? b.priceMin : BigDecimal.ZERO;
                return aPrice.compareTo(bPrice);
            });
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            aggList.sort((a, b) -> {
                BigDecimal aPrice = a.priceMax != null ? a.priceMax : BigDecimal.ZERO;
                BigDecimal bPrice = b.priceMax != null ? b.priceMax : BigDecimal.ZERO;
                return bPrice.compareTo(aPrice);
            });
        }
        int totalCount = aggList.size();
        int offset = (page - 1) * pageSize;
        int endIndex = Math.min(offset + pageSize, totalCount);
        List<BizOfferMapper.CountryProductAgg> pageData = offset < totalCount ? aggList.subList(offset, endIndex) : Collections.emptyList();
        List<CountryProductSummaryDTO> summaries = (List) pageData.stream().map(agg4 -> {
            CountryProductSummaryDTO summary = new CountryProductSummaryDTO();
            summary.setProductId(agg4.productId);
            summary.setProductName(agg4.productName);
            summary.setPriceMin(agg4.priceMin);
            summary.setPriceMax(agg4.priceMax);
            summary.setFactoryCount(agg4.factoryCount);
            summary.setOfferCount(agg4.offerCount);
            if (agg4.factoryNos != null && !agg4.factoryNos.isEmpty()) {
                List<String> factoryNoList = (List) Arrays.stream(agg4.factoryNos.split(",")).filter(n -> {
                    return (n == null || n.isEmpty()) ? false : true;
                }).distinct().collect(Collectors.toList());
                summary.setFactoryNos(factoryNoList);
            } else {
                summary.setFactoryNos(Collections.emptyList());
            }
            return summary;
        }).collect(Collectors.toList());
        dto.setSummaries(summaries);
        dto.setTotalCount(Integer.valueOf(totalCount));
        dto.setPage(Integer.valueOf(page));
        dto.setPageSize(Integer.valueOf(pageSize));
        dto.setTotalPages(Integer.valueOf((int) Math.ceil(totalCount / pageSize)));
        return dto;
    }

    private List<HotFactoryDTO> parseHotFactoriesFromJson(String json) {
        List<HotFactoryDTO> result = new ArrayList<>();
        if (json == null || json.isEmpty() || ClassUtils.ARRAY_SUFFIX.equals(json)) {
            return result;
        }
        try {
            List<Map<String, Object>> list = (List) objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() { // from class: com.mooket.social.service.impl.CountryServiceImpl.1
            });
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> item = list.get(i);
                HotFactoryDTO dto = new HotFactoryDTO();
                dto.setFactoryNo(item.get("factoryNo") != null ? item.get("factoryNo").toString() : null);
                Object offerCountObj = item.get("offerCount");
                dto.setOfferCount(Integer.valueOf(offerCountObj != null ? ((Number) offerCountObj).intValue() : 0));
                dto.setRank(Integer.valueOf(i + 1));
                result.add(dto);
            }
        } catch (Exception e) {
        }
        return result;
    }

    private List<HotProductDTO> parseHotProductsFromJson(String json) {
        List<HotProductDTO> result = new ArrayList<>();
        if (json == null || json.isEmpty() || ClassUtils.ARRAY_SUFFIX.equals(json)) {
            return result;
        }
        try {
            List<Map<String, Object>> list = (List) objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() { // from class: com.mooket.social.service.impl.CountryServiceImpl.2
            });
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> item = list.get(i);
                HotProductDTO dto = new HotProductDTO();
                dto.setProductName(item.get("productName") != null ? item.get("productName").toString() : null);
                Object offerCountObj = item.get("offerCount");
                dto.setOfferCount(Integer.valueOf(offerCountObj != null ? ((Number) offerCountObj).intValue() : 0));
                dto.setRank(Integer.valueOf(i + 1));
                result.add(dto);
            }
        } catch (Exception e) {
        }
        return result;
    }
}