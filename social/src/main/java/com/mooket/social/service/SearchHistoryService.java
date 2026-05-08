package com.mooket.social.service;

import com.mooket.social.dto.HomeCardsResponseDTO;
import com.mooket.social.entity.BizSearchHistory;

import java.util.List;

/**
 * 搜索历史服务接口
 */
public interface SearchHistoryService {

    /**
     * 添加搜索记录
     */
    void addSearchHistory(Long userId, String searchWord, String searchType);

    /**
     * 获取最近搜索记录
     */
    List<SearchHistoryDTO> getRecentSearches(Long userId, int limit);

    /**
     * 获取最近搜索的卡片数据（带完整统计信息）
     */
    HomeCardsResponseDTO getRecentSearchCards(Long userId, String category);

    /**
     * 获取自选搜索记录
     */
    List<SearchHistoryDTO> getSelfSelectSearches(Long userId, int limit);

    /**
     * 获取自选搜索的卡片数据（带完整统计信息）
     */
    HomeCardsResponseDTO getSelfSelectCards(Long userId, String category);

    /**
     * 删除搜索记录
     */
    void deleteSearchHistory(Long historyId);

    /**
     * 批量删除搜索记录
     */
    void batchDeleteSearchHistory(List<Long> historyIds);

    /**
     * 添加自选
     */
    void addSelfSelect(Long userId, String searchWord, String searchType);

    /**
     * 取消自选
     */
    void cancelSelfSelect(Long historyId);

    /**
     * 将历史记录移动到自选
     */
    void moveToSelfSelect(Long historyId);

    /**
     * 搜索历史 DTO
     */
    class SearchHistoryDTO {
        public Long historyId;
        public String searchWord;
        public String searchType;
        public Integer isSelfSelect;
        public String createTime;

        // 搜索类型对应的详情ID
        public Integer productId;      // 产品ID
        public Integer brandId;       // 品牌ID
        public Long merchantId;        // 商家ID
        public String country;         // 国家
        public String factoryNo;      // 厂号
        public String productName;     // 产品名称
    }
}
