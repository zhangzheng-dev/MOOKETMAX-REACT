package com.mooket.social.service.impl;

import com.mooket.social.dto.ProductDetailDTO;
import com.mooket.social.dto.ProductSummaryDTO;
import com.mooket.social.entity.DictProduct;
import com.mooket.social.entity.StatProduct;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictProductMapper;
import com.mooket.social.mapper.StatProductMapper;
import com.mooket.social.service.ProductService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final DictProductMapper productMapper;
    private final BizOfferMapper offerMapper;
    private final StatProductMapper statProductMapper;

    public ProductServiceImpl(
            DictProductMapper productMapper,
            BizOfferMapper offerMapper,
            StatProductMapper statProductMapper) {
        this.productMapper = productMapper;
        this.offerMapper = offerMapper;
        this.statProductMapper = statProductMapper;
    }

    @Override
    @Cacheable(value = "productDetail", key = "#productId + '_' + #category + '_' + #offerType + '_' + #sortBy + '_' + #page + '_' + #pageSize")
    public ProductDetailDTO getProductDetail(Integer productId, String category, String offerType,
                                             String sortBy, int page, int pageSize) {
        return buildProductDetail(productId, category, offerType, sortBy, page, pageSize);
    }

    private ProductDetailDTO buildProductDetail(Integer productId, String category, String offerType,
                                                String sortBy, int page, int pageSize) {
        DictProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }

        String dbOfferType = "offer".equalsIgnoreCase(offerType) ? "报盘" : "求购";
        boolean isOffer = "报盘".equals(dbOfferType);

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setCategory(product.getCategory());

        if (isOffer) {
            StatProduct stat = statProductMapper.selectByProductIdAndCategory(productId, category);
            if (stat != null) {
                dto.setOfferCount(stat.getTodayOfferCount() != null ? stat.getTodayOfferCount().longValue() : 0L);
                dto.setMerchantCount(stat.getTodayMerchantCount() != null ? stat.getTodayMerchantCount() : 0);
                dto.setFactoryCount(stat.getTodayFactoryCount() != null ? stat.getTodayFactoryCount() : 0);
                dto.setPriceMin(stat.getPriceMin());
                dto.setPriceMax(stat.getPriceMax());
            } else {
                BizOfferMapper.ProductDashboardStats dashboardStats =
                        offerMapper.selectProductDashboardStats(productId, category, dbOfferType);
                dto.setOfferCount(dashboardStats.totalOfferCount != null ? dashboardStats.totalOfferCount : 0L);
                dto.setMerchantCount(dashboardStats.merchantCount != null ? dashboardStats.merchantCount : 0);
                dto.setFactoryCount(dashboardStats.factoryCount != null ? dashboardStats.factoryCount : 0);
                dto.setPriceMin(dashboardStats.priceMin);
                dto.setPriceMax(dashboardStats.priceMax);
            }
        } else {
            BizOfferMapper.ProductDashboardStats dashboardStats =
                    offerMapper.selectProductDashboardStats(productId, category, dbOfferType);
            dto.setOfferCount(dashboardStats.totalOfferCount != null ? dashboardStats.totalOfferCount : 0L);
            dto.setMerchantCount(dashboardStats.merchantCount != null ? dashboardStats.merchantCount : 0);
            dto.setFactoryCount(dashboardStats.factoryCount != null ? dashboardStats.factoryCount : 0);
            dto.setPriceMin(dashboardStats.priceMin);
            dto.setPriceMax(dashboardStats.priceMax);
        }

        int offset = Math.max(0, (page - 1) * pageSize);
        List<BizOfferMapper.ProductStatAgg> aggList = offerMapper.selectProductStatsAgg(
                productId, category, dbOfferType, pageSize, offset, sortBy);
        int totalCount = offerMapper.countProductStatsAgg(productId, category, dbOfferType);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        List<ProductSummaryDTO> summaries = aggList.stream()
                .map(agg -> {
                    ProductSummaryDTO summary = new ProductSummaryDTO();
                    summary.setCountry(agg.country);
                    summary.setFactoryNo(agg.factoryNo);
                    summary.setCountryFactory((agg.country != null ? agg.country : "") + " " +
                            (agg.factoryNo != null ? agg.factoryNo : ""));
                    summary.setPriceMin(agg.priceMin);
                    summary.setPriceMax(agg.priceMax);
                    summary.setOfferCount(agg.offerCount);
                    summary.setMerchantCount(agg.merchantCount);
                    if (agg.merchantNames != null && !agg.merchantNames.isEmpty()) {
                        List<String> names = Arrays.stream(agg.merchantNames.split(","))
                                .filter(n -> n != null && !n.isEmpty())
                                .map(raw -> {
                                    int sep = raw.indexOf('|');
                                    if (sep > 0) {
                                        String shortName = raw.substring(0, sep);
                                        String fullName = raw.substring(sep + 1);
                                        return (!shortName.isEmpty() && !"NULL".equalsIgnoreCase(shortName))
                                                ? shortName
                                                : fullName;
                                    }
                                    return raw;
                                })
                                .collect(Collectors.toList());
                        summary.setMerchantNames(names);
                    } else {
                        summary.setMerchantNames(Collections.emptyList());
                    }
                    return summary;
                })
                .collect(Collectors.toList());

        dto.setSummaries(summaries);
        dto.setTotalCount(totalCount);
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setTotalPages(totalPages);
        return dto;
    }

    @CacheEvict(value = "productDetail", allEntries = true)
    public void clearProductCache() {
    }
}
