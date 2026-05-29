package com.mooket.social.service.impl;

import com.mooket.social.dto.ProductDetailDTO;
import com.mooket.social.dto.ProductSummaryDTO;
import com.mooket.social.entity.DictProduct;
import com.mooket.social.entity.StatProduct;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictProductMapper;
import com.mooket.social.mapper.StatProductMapper;
import com.mooket.social.service.ProductService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品服务实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final DictProductMapper productMapper;
    private final BizOfferMapper offerMapper;
    private final StatProductMapper statProductMapper;

    public ProductServiceImpl(DictProductMapper productMapper,
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

    /**
     * 构建产品详情（内部方法，不使用缓存）
     */
    private ProductDetailDTO buildProductDetail(Integer productId, String category, String offerType,
                                                String sortBy, int page, int pageSize) {
        // 1. 获取产品信息
        DictProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("产品不存在");
        }

        // 2. 确定查询类型
        String dbOfferType = "offer".equalsIgnoreCase(offerType) ? "报盘" : "求购";
        boolean isOffer = "报盘".equals(dbOfferType);

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setCategory(product.getCategory());

        // 3. 报盘和求购都实时查询（统一数据源，不用stat_product，避免口径不一致）
        BizOfferMapper.ProductDashboardStats dashboardStats =
                offerMapper.selectProductDashboardStats(productId, category, dbOfferType);
        dto.setOfferCount(dashboardStats.totalOfferCount != null ? dashboardStats.totalOfferCount : 0L);
        dto.setMerchantCount(dashboardStats.merchantCount != null ? dashboardStats.merchantCount : 0);
        dto.setFactoryCount(dashboardStats.factoryCount != null ? dashboardStats.factoryCount : 0);
        // 价格区间直接用dashboardStats（无IQR，只过滤price<=0）
        dto.setPriceMin(dashboardStats.priceMin);
        dto.setPriceMax(dashboardStats.priceMax);

        // 5. 使用SQL聚合查询获取分页数据
        int offset = (page - 1) * pageSize;

        // 报盘和求购都用无IQR的SQL（统一口径，避免看板数量和列表不一致）
        List<BizOfferMapper.ProductStatAgg> aggList = offerMapper.selectProductStatsAgg(
                productId, category, dbOfferType, pageSize, offset);

        // 获取总数
        int totalCount = offerMapper.countProductStatsAgg(productId, category, dbOfferType);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 6. 转换聚合数据为DTO
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
                        summary.setMerchantNames(names);
                    } else {
                        summary.setMerchantNames(java.util.Collections.emptyList());
                    }
                    return summary;
                })
                .collect(Collectors.toList());

        // 7. 排序（内存中排序，因为数据量已经很小了）
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            summaries.sort((a, b) -> {
                BigDecimal aPrice = a.getPriceMin() != null ? a.getPriceMin() : BigDecimal.ZERO;
                BigDecimal bPrice = b.getPriceMin() != null ? b.getPriceMin() : BigDecimal.ZERO;
                return aPrice.compareTo(bPrice);
            });
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            summaries.sort((a, b) -> {
                BigDecimal aPrice = a.getPriceMax() != null ? a.getPriceMax() : BigDecimal.ZERO;
                BigDecimal bPrice = b.getPriceMax() != null ? b.getPriceMax() : BigDecimal.ZERO;
                return bPrice.compareTo(aPrice);
            });
        }
        // else: 默认按报盘数降序已在SQL中处理

        // 8. 设置返回结果
        dto.setSummaries(summaries);
        dto.setTotalCount(totalCount);
        dto.setPage(page);
        dto.setPageSize(pageSize);
        dto.setTotalPages(totalPages);

        return dto;
    }

    /**
     * 清除产品缓存（数据同步后调用）
     */
    @CacheEvict(value = "productDetail", allEntries = true)
    public void clearProductCache() {
        // 缓存将在数据同步后自动清除
    }
}
