package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.HomeCardsResponseDTO;
import com.mooket.social.service.HomeStatService;
import com.mooket.social.service.SearchHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 搜索历史 Controller
 */
@RestController
@RequestMapping("/api/v1/search-history")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;
    private final HomeStatService homeStatService;

    // 默认用户ID（实际项目中从登录态获取）
    private static final Long DEFAULT_USER_ID = 1L;

    public SearchHistoryController(SearchHistoryService searchHistoryService, HomeStatService homeStatService) {
        this.searchHistoryService = searchHistoryService;
        this.homeStatService = homeStatService;
    }

    /**
     * 获取最近搜索记录
     */
    @GetMapping("/recent")
    public ApiResponse<List<SearchHistoryService.SearchHistoryDTO>> getRecentSearches(
            @RequestParam(defaultValue = "200") int limit) {
        List<SearchHistoryService.SearchHistoryDTO> histories = searchHistoryService.getRecentSearches(DEFAULT_USER_ID, limit);
        return ApiResponse.success(histories);
    }

    /**
     * 获取最近搜索的卡片数据（带完整统计信息，和自选数据一样）
     */
    @GetMapping("/cards/recent")
    public ApiResponse<HomeCardsResponseDTO> getRecentSearchCards(
            @RequestParam(defaultValue = "牛") String category) {
        HomeCardsResponseDTO cards = searchHistoryService.getRecentSearchCards(DEFAULT_USER_ID, category);
        return ApiResponse.success(cards);
    }

    /**
     * 获取自选搜索记录
     */
    @GetMapping("/self-select")
    public ApiResponse<List<SearchHistoryService.SearchHistoryDTO>> getSelfSelectSearches(
            @RequestParam(defaultValue = "200") int limit) {
        List<SearchHistoryService.SearchHistoryDTO> histories = searchHistoryService.getSelfSelectSearches(DEFAULT_USER_ID, limit);
        return ApiResponse.success(histories);
    }

    /**
     * 获取自选搜索的卡片数据（带完整统计信息）
     */
    @GetMapping("/cards/self-select")
    public ApiResponse<HomeCardsResponseDTO> getSelfSelectCards(
            @RequestParam(defaultValue = "牛") String category) {
        HomeCardsResponseDTO cards = searchHistoryService.getSelfSelectCards(DEFAULT_USER_ID, category);
        return ApiResponse.success(cards);
    }

    /**
     * 添加搜索记录
     */
    @PostMapping("/add")
    public ApiResponse<Map<String, String>> addSearchHistory(
            @RequestParam String searchWord,
            @RequestParam String searchType) {
        searchHistoryService.addSearchHistory(DEFAULT_USER_ID, searchWord, searchType);
        return ApiResponse.success(Map.of("message", "添加成功"));
    }

    /**
     * 删除搜索记录
     */
    @DeleteMapping("/{historyId}")
    public ApiResponse<Map<String, String>> deleteSearchHistory(
            @PathVariable Long historyId) {
        searchHistoryService.deleteSearchHistory(historyId);
        return ApiResponse.success(Map.of("message", "删除成功"));
    }

    /**
     * 批量删除搜索记录
     */
    @DeleteMapping("/batch")
    public ApiResponse<Map<String, String>> batchDeleteSearchHistory(
            @RequestBody List<Long> historyIds) {
        searchHistoryService.batchDeleteSearchHistory(historyIds);
        return ApiResponse.success(Map.of("message", "批量删除成功"));
    }

    /**
     * 添加自选
     */
    @PostMapping("/self-select/add")
    public ApiResponse<Map<String, String>> addSelfSelect(
            @RequestParam String searchWord,
            @RequestParam String searchType) {
        searchHistoryService.addSelfSelect(DEFAULT_USER_ID, searchWord, searchType);
        return ApiResponse.success(Map.of("message", "添加自选成功"));
    }

    /**
     * 取消自选
     */
    @PostMapping("/self-select/cancel/{historyId}")
    public ApiResponse<Map<String, String>> cancelSelfSelect(
            @PathVariable Long historyId) {
        searchHistoryService.cancelSelfSelect(historyId);
        return ApiResponse.success(Map.of("message", "取消自选成功"));
    }

    /**
     * 将历史记录移动到自选
     */
    @PostMapping("/self-select/move/{historyId}")
    public ApiResponse<Map<String, String>> moveToSelfSelect(
            @PathVariable Long historyId) {
        searchHistoryService.moveToSelfSelect(historyId);
        return ApiResponse.success(Map.of("message", "移动到自选成功"));
    }
}
