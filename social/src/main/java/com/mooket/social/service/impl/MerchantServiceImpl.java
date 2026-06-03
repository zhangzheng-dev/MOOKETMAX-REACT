package com.mooket.social.service.impl;

import com.mooket.social.dto.EmployeeOfferDTO;
import com.mooket.social.dto.MerchantDetailDTO;
import com.mooket.social.dto.MerchantProductPageDTO;
import com.mooket.social.dto.OfferSummaryDTO;
import com.mooket.social.entity.BizOffer;
import com.mooket.social.entity.DictMerchant;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictMerchantMapper;
import com.mooket.social.service.MerchantService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MerchantServiceImpl implements MerchantService {

    private static final int DETAIL_INITIAL_PAGE_SIZE = 20;
    private static final DateTimeFormatter PUBLISH_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DictMerchantMapper merchantMapper;
    private final BizOfferMapper offerMapper;

    public MerchantServiceImpl(DictMerchantMapper merchantMapper, BizOfferMapper offerMapper) {
        this.merchantMapper = merchantMapper;
        this.offerMapper = offerMapper;
    }

    @Override
    @Cacheable(value = "merchantDetail", key = "#merchantId + '_' + #category")
    public MerchantDetailDTO getMerchantDetail(Long merchantId, String category) {
        DictMerchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }

        MerchantDetailDTO dto = new MerchantDetailDTO();
        dto.setMerchantId(merchant.getMerchantId());
        dto.setMerchantName(merchant.getMerchantName());
        dto.setMerchantShortName(merchant.getMerchantShortName());
        dto.setMerchantTags(merchant.getMerchantTags());
        dto.setContactPhone(merchant.getContactPhone());

        BizOfferMapper.MerchantDashboardStats stats = offerMapper.selectMerchantDashboardStats(merchantId, category);
        dto.setTodayOfferCount(stats.recentOfferCount != null ? stats.recentOfferCount.intValue() : 0);
        dto.setTodayInquiryCount(stats.recentInquiryCount != null ? stats.recentInquiryCount.intValue() : 0);
        dto.setTodayProductCount(stats.recentProductCount != null ? stats.recentProductCount.intValue() : 0);
        dto.setTodayFactoryCount(stats.recentFactoryCount != null ? stats.recentFactoryCount.intValue() : 0);

        List<BizOfferMapper.MerchantOfferAgg> offerAggs = offerMapper.selectMerchantOfferAgg(
                merchantId, category, "报盘", DETAIL_INITIAL_PAGE_SIZE, 0);
        List<BizOfferMapper.MerchantOfferAgg> inquiryAggs = offerMapper.selectMerchantOfferAgg(
                merchantId, category, "求购", DETAIL_INITIAL_PAGE_SIZE, 0);

        dto.setOffers(buildOfferSummaries(merchantId, category, "报盘", offerAggs));
        dto.setInquiries(buildOfferSummaries(merchantId, category, "求购", inquiryAggs));

        dto.setTotalOffers(offerMapper.countMerchantOfferAgg(merchantId, category, "报盘"));
        dto.setTotalInquiries(offerMapper.countMerchantOfferAgg(merchantId, category, "求购"));
        return dto;
    }

    @Override
    @Cacheable(value = "merchantProducts", key = "#merchantId + '_' + #category + '_' + #offerType + '_' + #page + '_' + #pageSize + '_' + #sortBy")
    public MerchantProductPageDTO getMerchantProducts(Long merchantId, String category, String offerType, int page, int pageSize, String sortBy) {
        DictMerchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }

        String dbOfferType = "offer".equalsIgnoreCase(offerType) ? "报盘" : "求购";

        // 价格排序需要全量获取再排序分页
        boolean isPriceSort = "price_asc".equals(sortBy) || "price_desc".equals(sortBy);
        
        if (isPriceSort) {
            // 全量获取，内存排序后分页
            int totalCount = offerMapper.countMerchantOfferAgg(merchantId, category, dbOfferType);
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            
            List<BizOfferMapper.MerchantOfferAgg> allAgg = offerMapper.selectMerchantOfferAgg(
                    merchantId, category, dbOfferType, totalCount, 0);
            
            List<OfferSummaryDTO> allSummaries = buildOfferSummaries(merchantId, category, dbOfferType, allAgg);
            
            // 排序：协商报价（无价格）放最后
            if ("price_asc".equals(sortBy)) {
                allSummaries.sort((a, b) -> {
                    boolean aHas = a.getPrice() != null && a.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0;
                    boolean bHas = b.getPrice() != null && b.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0;
                    if (!aHas && !bHas) return 0;
                    if (!aHas) return 1;
                    if (!bHas) return -1;
                    return a.getPrice().compareTo(b.getPrice());
                });
            } else {
                allSummaries.sort((a, b) -> {
                    boolean aHas = a.getPriceMax() != null && a.getPriceMax().compareTo(java.math.BigDecimal.ZERO) > 0;
                    boolean bHas = b.getPriceMax() != null && b.getPriceMax().compareTo(java.math.BigDecimal.ZERO) > 0;
                    if (!aHas && !bHas) return 0;
                    if (!aHas) return 1;
                    if (!bHas) return -1;
                    return b.getPriceMax().compareTo(a.getPriceMax());
                });
            }
            
            // 内存分页
            int offset = (page - 1) * pageSize;
            int endIdx = Math.min(offset + pageSize, allSummaries.size());
            List<OfferSummaryDTO> pagedSummaries = offset < allSummaries.size()
                    ? allSummaries.subList(offset, endIdx)
                    : Collections.emptyList();
            
            MerchantProductPageDTO result = new MerchantProductPageDTO();
            result.setProducts(pagedSummaries);
            result.setTotalCount(totalCount);
            result.setPage(page);
            result.setPageSize(pageSize);
            result.setTotalPages(totalPages);
            result.setOfferType(offerType);
            return result;
        }

        // 非价格排序：正常分页查询
        int totalCount = offerMapper.countMerchantOfferAgg(merchantId, category, dbOfferType);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int offset = (page - 1) * pageSize;

        List<BizOfferMapper.MerchantOfferAgg> aggList = offerMapper.selectMerchantOfferAgg(
                merchantId, category, dbOfferType, pageSize, offset);

        MerchantProductPageDTO result = new MerchantProductPageDTO();
        result.setProducts(buildOfferSummaries(merchantId, category, dbOfferType, aggList));
        result.setTotalCount(totalCount);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotalPages(totalPages);
        result.setOfferType(offerType);
        return result;
    }

    private List<OfferSummaryDTO> buildOfferSummaries(Long merchantId,
                                                      String category,
                                                      String offerType,
                                                      List<BizOfferMapper.MerchantOfferAgg> aggList) {
        if (aggList == null || aggList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<EmployeeOfferDTO>> employeeOfferMap = buildEmployeeOfferMap(
                offerMapper.selectByMerchantIdAndType(merchantId, offerType, category));

        return aggList.stream()
                .map(agg -> {
                    OfferSummaryDTO summary = new OfferSummaryDTO();
                    summary.setProductName(agg.productName);
                    summary.setCountry(agg.country);
                    summary.setFactoryNo(agg.factoryNo);
                    summary.setPrice(agg.priceMin);
                    summary.setPriceMax(agg.priceMax);
                    summary.setTags(agg.tags);
                    summary.setGoodsLocation(agg.goodsLocations);
                    summary.setGoodsType(agg.goodsTypes);
                    summary.setFeedingType(agg.feedingTypes);
                    summary.setPublishTime(agg.latestPublishTime);
                    summary.setEmployeeOffers(employeeOfferMap.getOrDefault(
                            groupKey(agg.productName, agg.country, agg.factoryNo),
                            Collections.emptyList()));
                    return summary;
                })
                .collect(Collectors.toList());
    }

    private Map<String, List<EmployeeOfferDTO>> buildEmployeeOfferMap(List<BizOffer> offers) {
        if (offers == null || offers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<EmployeeOfferDTO>> grouped = new LinkedHashMap<>();
        for (BizOffer offer : offers) {
            grouped.computeIfAbsent(groupKey(offer.getProductName(), offer.getCountry(), offer.getFactoryNo()),
                            key -> new ArrayList<>())
                    .add(convertToEmployeeOfferDTO(offer));
        }

        grouped.values().forEach(items ->
                items.sort(Comparator.comparing(EmployeeOfferDTO::getPublishTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))));
        return grouped;
    }

    private EmployeeOfferDTO convertToEmployeeOfferDTO(BizOffer offer) {
        EmployeeOfferDTO dto = new EmployeeOfferDTO();
        dto.setOfferId(offer.getOfferId());
        dto.setUserNickname(offer.getUserNickname());
        dto.setContactPhone(offer.getContactPhone());
        dto.setPrice(offer.getPrice());
        dto.setPriceMax(offer.getPriceMax());
        dto.setWeight(offer.getWeight());
        dto.setGoodsLocation(offer.getGoodsLocation());
        dto.setTags(offer.getTags());
        dto.setGoodsType(offer.getGoodsType());
        dto.setFeedingMethod(offer.getFeedingType());
        dto.setOfferOriginalText(offer.getOfferOriginalText());
        if (offer.getPublishTime() != null) {
            dto.setPublishTime(offer.getPublishTime().format(PUBLISH_TIME_FORMATTER));
        }
        return dto;
    }

    private String groupKey(String productName, String country, String factoryNo) {
        return String.join("|", nullToEmpty(productName), nullToEmpty(country), nullToEmpty(factoryNo));
    }

    private String nullToEmpty(String value) {
        return Objects.toString(value, "");
    }
}
