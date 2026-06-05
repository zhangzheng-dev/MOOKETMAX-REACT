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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service
public class CountryServiceImpl implements CountryService {
    private static final Logger log = LoggerFactory.getLogger(CountryServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final BizOfferMapper offerMapper;
    private final StatCountryMapper statCountryMapper;

    public CountryServiceImpl(BizOfferMapper offerMapper, StatCountryMapper statCountryMapper) {
        this.offerMapper = offerMapper;
        this.statCountryMapper = statCountryMapper;
    }

    @Override
    @Cacheable(value = {"countryDetail"}, key = "#country + '_' + #category + '_' + #offerType + '_' + #sortBy + '_' + #page + '_' + #pageSize")
    public CountryDetailDTO getCountryDetail(String country, String category, String offerType, String sortBy, int page, int pageSize) {
        return buildCountryDetail(country, category, offerType, sortBy, page, pageSize);
    }

    private CountryDetailDTO buildCountryDetail(String country, String category, String offerType, String sortBy, int page, int pageSize) {
        String dbOfferType = "offer".equalsIgnoreCase(offerType) ? "报盘" : "求购";
        boolean isOffer = "报盘".equals(dbOfferType);
        CountryDetailDTO dto = new CountryDetailDTO();
        dto.setCountry(country);

        if (isOffer) {
            StatCountry statCountry = statCountryMapper.selectByCountryAndCategory(country, category);
            log.info("buildCountryDetail statCountry lookup: country={}, category={}, found={}", country, category, statCountry != null);
            if (statCountry != null) {
                dto.setOfferCount(statCountry.getTodayOfferCount() != null ? statCountry.getTodayOfferCount().longValue() : 0L);
                dto.setMerchantCount(statCountry.getTodayMerchantCount() != null ? statCountry.getTodayMerchantCount() : 0);
                dto.setFactoryCount(statCountry.getTodayFactoryCount() != null ? statCountry.getTodayFactoryCount() : 0);
                dto.setHotFactories(parseHotFactoriesFromJson(statCountry.getHotFactories()));
                dto.setHotProducts(parseHotProductsFromJson(statCountry.getHotProducts()));
            } else {
                dto.setOfferCount(0L);
                dto.setMerchantCount(0);
                dto.setFactoryCount(0);
                dto.setHotFactories(Collections.emptyList());
                dto.setHotProducts(Collections.emptyList());
            }
        } else {
            BizOfferMapper.CountryDashboardStats dashboardStats = offerMapper.selectCountryDashboardStats(country, category, dbOfferType);
            dto.setOfferCount(dashboardStats.totalOfferCount != null ? dashboardStats.totalOfferCount.longValue() : 0L);
            dto.setMerchantCount(dashboardStats.merchantCount != null ? dashboardStats.merchantCount : 0);
            dto.setFactoryCount(dashboardStats.factoryCount != null ? dashboardStats.factoryCount : 0);

            List<BizOfferMapper.HotFactoryAgg> hotFactoryAggList = offerMapper.selectHotFactories(country, category, dbOfferType);
            List<HotFactoryDTO> hotFactories = new ArrayList<>();
            for (int i = 0; i < hotFactoryAggList.size(); i++) {
                BizOfferMapper.HotFactoryAgg agg = hotFactoryAggList.get(i);
                HotFactoryDTO hotFactory = new HotFactoryDTO();
                hotFactory.setFactoryNo(agg.factoryNo);
                hotFactory.setOfferCount(agg.offerCount);
                hotFactory.setRank(i + 1);
                hotFactories.add(hotFactory);
            }
            dto.setHotFactories(hotFactories);

            List<BizOfferMapper.HotProductAgg> hotProductAggList = offerMapper.selectHotProducts(country, category, dbOfferType);
            List<HotProductDTO> hotProducts = new ArrayList<>();
            for (int i = 0; i < hotProductAggList.size(); i++) {
                BizOfferMapper.HotProductAgg agg = hotProductAggList.get(i);
                HotProductDTO hotProduct = new HotProductDTO();
                hotProduct.setProductName(agg.productName);
                hotProduct.setOfferCount(agg.offerCount);
                hotProduct.setRank(i + 1);
                hotProducts.add(hotProduct);
            }
            dto.setHotProducts(hotProducts);
        }

        BizOfferMapper.PriceRange priceRange = offerMapper.selectFilteredPriceRangeByCountry(country, category, dbOfferType);
        if (priceRange != null) {
            dto.setPriceMin(priceRange.priceMin);
            dto.setPriceMax(priceRange.priceMax);
        }

        int totalCount = offerMapper.countCountryProductAgg(country, category, dbOfferType);
        int offset = Math.max(0, (page - 1) * pageSize);
        List<BizOfferMapper.CountryProductAgg> pageData =
                offerMapper.selectCountryProductAggPaged(country, category, dbOfferType, pageSize, offset, sortBy);

        List<CountryProductSummaryDTO> summaries = pageData.stream().map(agg -> {
            CountryProductSummaryDTO summary = new CountryProductSummaryDTO();
            summary.setProductId(agg.productId);
            summary.setProductName(agg.productName);
            summary.setPriceMin(agg.priceMin);
            summary.setPriceMax(agg.priceMax);
            summary.setFactoryCount(agg.factoryCount);
            summary.setOfferCount(agg.offerCount);
            if (agg.factoryNos != null && !agg.factoryNos.isEmpty()) {
                List<String> factoryNoList = Arrays.stream(agg.factoryNos.split(","))
                        .filter(n -> n != null && !n.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                summary.setFactoryNos(factoryNoList);
            } else {
                summary.setFactoryNos(Collections.emptyList());
            }
            return summary;
        }).collect(Collectors.toList());

        dto.setSummaries(summaries);
        dto.setTotalCount(totalCount);
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setTotalPages((int) Math.ceil(totalCount / (double) pageSize));
        return dto;
    }

    private List<HotFactoryDTO> parseHotFactoriesFromJson(String json) {
        List<HotFactoryDTO> result = new ArrayList<>();
        if (json == null || json.isEmpty() || ClassUtils.ARRAY_SUFFIX.equals(json)) {
            return result;
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> item = list.get(i);
                HotFactoryDTO dto = new HotFactoryDTO();
                dto.setFactoryNo(item.get("factoryNo") != null ? item.get("factoryNo").toString() : null);
                Object offerCountObj = item.get("offerCount");
                dto.setOfferCount(offerCountObj != null ? ((Number) offerCountObj).intValue() : 0);
                dto.setRank(i + 1);
                result.add(dto);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private List<HotProductDTO> parseHotProductsFromJson(String json) {
        List<HotProductDTO> result = new ArrayList<>();
        if (json == null || json.isEmpty() || ClassUtils.ARRAY_SUFFIX.equals(json)) {
            return result;
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> item = list.get(i);
                HotProductDTO dto = new HotProductDTO();
                dto.setProductName(item.get("productName") != null ? item.get("productName").toString() : null);
                Object offerCountObj = item.get("offerCount");
                dto.setOfferCount(offerCountObj != null ? ((Number) offerCountObj).intValue() : 0);
                dto.setRank(i + 1);
                result.add(dto);
            }
        } catch (Exception ignored) {
        }
        return result;
    }
}
