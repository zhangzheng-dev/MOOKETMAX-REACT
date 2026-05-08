package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.SearchSuggestDTO;
import com.mooket.social.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索 Controller
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 获取搜索联想词
     */
    @GetMapping("/suggest")
    public ApiResponse<List<SearchSuggestDTO>> getSearchSuggestions(
            @RequestParam(required = false, defaultValue = "牛") String category,
            @RequestParam String keyword) {
        List<SearchSuggestDTO> suggestions = searchService.getSearchSuggestions(category, keyword);
        return ApiResponse.success(suggestions);
    }

    /**
     * 保存搜索历史
     * @param userId 用户ID（可选，默认1）
     * @param searchWord 搜索词
     * @param searchType 搜索类型（产品/国家/品牌/商家/国家厂号/国家产品/品牌产品/国家厂号产品）
     * @param isSelfSelect 是否自选（0-否，1-是）
     * @param productId 产品ID
     * @param productName 产品名称
     * @param country 国家
     * @param factoryNo 厂号
     * @param brandId 品牌ID
     * @param merchantId 商家ID
     */
    @PostMapping("/history")
    public ApiResponse<Void> saveSearchHistory(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam String searchWord,
            @RequestParam String searchType,
            @RequestParam(required = false, defaultValue = "0") Integer isSelfSelect,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String factoryNo,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long merchantId) {
        searchService.saveSearchHistory(userId, searchWord, searchType, isSelfSelect,
                productId, productName, country, factoryNo, brandId, merchantId);
        return ApiResponse.success(null);
    }
}
