package com.mooket.social.service.impl;

import com.mooket.social.dto.EmployeeOfferDTO;
import com.mooket.social.dto.MerchantDetailDTO;
import com.mooket.social.dto.MerchantProductPageDTO;
import com.mooket.social.dto.OfferSummaryDTO;
import com.mooket.social.entity.DictMerchant;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.DictMerchantMapper;
import com.mooket.social.service.MerchantService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家服务实现
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private final DictMerchantMapper merchantMapper;
    private final BizOfferMapper offerMapper;

    public MerchantServiceImpl(DictMerchantMapper merchantMapper,
                              BizOfferMapper offerMapper) {
        this.merchantMapper = merchantMapper;
        this.offerMapper = offerMapper;
    }

    @Override
    @Cacheable(value = "merchantDetail", key = "#merchantId + '_' + #category")
    public MerchantDetailDTO getMerchantDetail(Long merchantId, String category) {
        MerchantDetailDTO dto = new MerchantDetailDTO();

        // 获取商家基本信息
        DictMerchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }
        dto.setMerchantId(merchant.getMerchantId());
        dto.setMerchantName(merchant.getMerchantName());
        dto.setMerchantShortName(merchant.getMerchantShortName());
        dto.setMerchantTags(merchant.getMerchantTags());
        dto.setContactPhone(merchant.getContactPhone());

        // 使用SQL聚合查询近2日统计数据（昨天+今天），与首页商家卡片的"今日报盘数"区分开
        BizOfferMapper.MerchantDashboardStats stats = offerMapper.selectMerchantDashboardStats(merchantId, category);
        dto.setTodayOfferCount(stats.recentOfferCount != null ? stats.recentOfferCount.intValue() : 0);
        dto.setTodayInquiryCount(stats.recentInquiryCount != null ? stats.recentInquiryCount.intValue() : 0);
        dto.setTodayProductCount(stats.recentProductCount != null ? stats.recentProductCount.intValue() : 0);
        dto.setTodayFactoryCount(stats.recentFactoryCount != null ? stats.recentFactoryCount.intValue() : 0);

        // 获取报盘列表（只取前10条用于显示）
        List<BizOfferMapper.MerchantOfferAgg> offerAggs = offerMapper.selectMerchantOfferAgg(merchantId, category, "报盘", 10, 0);
        dto.setOffers(convertToOfferSummaries(offerAggs, "报盘"));

        // 获取求购列表（只取前10条用于显示）
        List<BizOfferMapper.MerchantOfferAgg> inquiryAggs = offerMapper.selectMerchantOfferAgg(merchantId, category, "求购", 10, 0);
        dto.setInquiries(convertToOfferSummaries(inquiryAggs, "求购"));

        // 设置产品总数
        int totalOffers = offerMapper.countMerchantOfferAgg(merchantId, category, "报盘");
        int totalInquiries = offerMapper.countMerchantOfferAgg(merchantId, category, "求购");
        dto.setTotalOffers(totalOffers);
        dto.setTotalInquiries(totalInquiries);

        return dto;
    }

    @Override
    @Cacheable(value = "merchantProducts", key = "#merchantId + '_' + #category + '_' + #offerType + '_' + #page + '_' + #pageSize")
    public MerchantProductPageDTO getMerchantProducts(Long merchantId, String category, String offerType, int page, int pageSize) {
        // 获取商家基本信息（验证商家存在）
        DictMerchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }

        // 确定查询类型
        String dbOfferType = "offer".equalsIgnoreCase(offerType) ? "报盘" : "求购";

        // 获取总数
        int totalCount = offerMapper.countMerchantOfferAgg(merchantId, category, dbOfferType);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 分页查询
        int offset = (page - 1) * pageSize;
        List<BizOfferMapper.MerchantOfferAgg> aggList = offerMapper.selectMerchantOfferAgg(
                merchantId, category, dbOfferType, pageSize, offset);

        // 转换结果
        List<OfferSummaryDTO> products = convertToOfferSummaries(aggList, dbOfferType);

        // 构建返回结果
        MerchantProductPageDTO result = new MerchantProductPageDTO();
        result.setProducts(products);
        result.setTotalCount(totalCount);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotalPages(totalPages);
        result.setOfferType(offerType);

        return result;
    }

    private List<OfferSummaryDTO> convertToOfferSummaries(List<BizOfferMapper.MerchantOfferAgg> aggList, String offerType) {
        if (aggList == null || aggList.isEmpty()) {
            return Collections.emptyList();
        }

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

                    // 构建员工报价明细
                    EmployeeOfferDTO empOffer = new EmployeeOfferDTO();
                    empOffer.setUserNickname(agg.userNickname);
                    empOffer.setPrice(agg.empPrice);
                    empOffer.setPriceMax(agg.empPriceMax);
                    empOffer.setWeight(agg.empWeight);
                    empOffer.setGoodsLocation(agg.empGoodsLocation);
                    empOffer.setOfferOriginalText(agg.offerOriginalText);
                    // 格式化发布时间为字符串，供 Android 端解析
                    if (agg.latestPublishTime != null) {
                        empOffer.setPublishTime(agg.latestPublishTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    summary.setEmployeeOffers(Collections.singletonList(empOffer));

                    return summary;
                })
                .collect(Collectors.toList());
    }
}
