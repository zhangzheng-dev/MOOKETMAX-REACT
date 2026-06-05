package com.mooket.social.service.impl;

import com.mooket.social.dto.FactoryDetailDTO;
import com.mooket.social.entity.DictFactory;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictFactoryMapper;
import com.mooket.social.service.FactoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FactoryServiceImpl implements FactoryService {

    private final BizOfferMapper offerMapper;
    private final DictFactoryMapper factoryMapper;

    public FactoryServiceImpl(BizOfferMapper offerMapper, DictFactoryMapper factoryMapper) {
        this.offerMapper = offerMapper;
        this.factoryMapper = factoryMapper;
    }

    @Override
    @Cacheable(value = "factoryDetail", key = "#country + '_' + #factoryNo + '_' + #category + '_' + #offerType + '_' + #sortBy + '_' + #page + '_' + #pageSize")
    public FactoryDetailDTO getFactoryDetail(String country, String factoryNo, String category,
                                             String offerType, String sortBy, int page, int pageSize) {
        List<DictFactory> factories = factoryMapper.selectByFactoryNo(factoryNo);
        String countryAlias = null;
        for (DictFactory factory : factories) {
            if (factory.getCountry().equals(country)) {
                countryAlias = factory.getCountryAlias();
                break;
            }
        }

        String dbOfferType = "offer".equalsIgnoreCase(offerType) || "报盘".equals(offerType) ? "报盘" : "求购";
        BizOfferMapper.FactoryDashboardAgg dashboardStats =
                offerMapper.selectFactoryDashboardStats(country, factoryNo, category, dbOfferType);

        FactoryDetailDTO dto = new FactoryDetailDTO();
        dto.setCountry(country);
        dto.setFactoryNo(factoryNo);
        dto.setCountryAlias(countryAlias);
        dto.setProductCount(dashboardStats.productCount != null ? dashboardStats.productCount : 0);
        dto.setInquiryCount(dashboardStats.inquiryCount != null ? dashboardStats.inquiryCount : 0);
        dto.setRecentOfferCount(dashboardStats.recentOfferCount != null ? dashboardStats.recentOfferCount : 0);

        int offset = (page - 1) * pageSize;
        int totalCount = offerMapper.countFactoryProductAggFast(country, factoryNo, category, dbOfferType);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        List<BizOfferMapper.FactoryProductAgg> aggList = offerMapper.selectFactoryProductAggFast(
                country, factoryNo, category, dbOfferType, pageSize, offset, normalizeSortBy(sortBy));

        List<FactoryDetailDTO.FactoryProductDTO> products = aggList.stream()
                .map(agg -> {
                    FactoryDetailDTO.FactoryProductDTO product = new FactoryDetailDTO.FactoryProductDTO();
                    product.setProductId(agg.productId);
                    product.setProductName(agg.productName);
                    product.setPriceMin(agg.priceMin != null ? agg.priceMin.doubleValue() : null);
                    product.setPriceMax(agg.priceMax != null ? agg.priceMax.doubleValue() : null);
                    product.setMerchantCount(agg.merchantCount);
                    product.setOfferCount(agg.offerCount);
                    if (agg.merchantNames != null && !agg.merchantNames.isEmpty()) {
                        List<String> merchantList = Arrays.stream(agg.merchantNames.split(","))
                                .filter(name -> name != null && !name.isEmpty())
                                .distinct()
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
                        product.setMerchantNames(merchantList);
                    } else {
                        product.setMerchantNames(Collections.emptyList());
                    }
                    return product;
                })
                .collect(Collectors.toList());

        dto.setProducts(products);
        dto.setTotalCount(totalCount);
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setTotalPages(totalPages);
        return dto;
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "comprehensive";
        }
        return sortBy;
    }
}
