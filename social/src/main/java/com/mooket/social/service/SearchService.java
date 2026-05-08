package com.mooket.social.service;

import com.mooket.social.dto.SearchSuggestDTO;
import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 获取搜索联想词
     * @param category 大类（牛/猪）
     * @param keyword 搜索关键词
     * @return 联想词列表
     */
    List<SearchSuggestDTO> getSearchSuggestions(String category, String keyword);

    /**
     * 保存搜索历史
     * @param userId 用户ID（默认1）
     * @param searchWord 搜索词
     * @param searchType 搜索类型
     * @param isSelfSelect 是否自选（0-否，1-是）
     * @param productId 产品ID
     * @param productName 产品名称
     * @param country 国家
     * @param factoryNo 厂号
     * @param brandId 品牌ID
     * @param merchantId 商家ID
     */
    void saveSearchHistory(Long userId, String searchWord, String searchType, Integer isSelfSelect,
                           Long productId, String productName, String country, String factoryNo,
                           Long brandId, Long merchantId);
}
