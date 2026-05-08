package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.common.JwtUtil;
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

    public SearchHistoryController(SearchHistoryService searchHistoryService, HomeStatService homeStatService) {
        this.searchHistoryService = searchHistoryService;
        this.homeStatService = homeStatService;
    }

    /**
     * 从 Authorization header 提取用户ID
     */
    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            if (!JwtUtil.validateToken(token)) {
                return null;
            }
            return JwtUtil.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取最近搜索记录
     */
    @GetMapping("/recent")
    public ApiResponse<List<SearchHistoryService.SearchHistoryDTO>> getRecentSearches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "200") int limit) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ApiResponse.success(List.of());
        }
        List<SearchHistoryService.SearchHistoryDTO> histories = searchHistoryService.getRecentSearches(userId, limit);
        return ApiResponse.success(histories);
    }

    /**
     * 获取最近搜索的卡片数据（带完整统计信息，和自选数据一样）
     */
    @GetMapping("/cards/recent")
    public ApiResponse<HomeCardsResponseDTO> getRecentSearchCards(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "牛") String category) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            HomeCardsResponseDTO empty = new HomeCardsResponseDTO();
            empty.setCards(List.of());
            empty.setUpdateTime(null);
            return ApiResponse.success(empty);
        }
        HomeCardsResponseDTO cards = searchHistoryService.getRecentSearchCards(userId, category);
        return ApiResponse.success(cards);
    }

    /**
     * 获取自选搜索记录
     */
    @GetMapping("/self-select")
    public ApiResponse<List<SearchHistoryService.SearchHistoryDTO>> getSelfSelectSearches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "200") int limit) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ApiResponse.success(List.of());
        }
        List<SearchHistoryService.SearchHistoryDTO> histories = searchHistoryService.getSelfSelectSearches(userId, limit);
        return ApiResponse.success(histories);
    }

    /**
     * 获取自选搜索的卡片数据（带完整统计信息）
     */
    @GetMapping("/cards/self-select")
    public ApiResponse<HomeCardsResponseDTO> getSelfSelectCards(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "牛") String category) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            HomeCardsResponseDTO empty = new HomeCardsResponseDTO();
            empty.setCards(List.of());
            empty.setUpdateTime(null);
            return ApiResponse.success(empty);
        }
        HomeCardsResponseDTO cards = searchHistoryService.getSelfSelectCards(userId, category);
        return ApiResponse.success(cards);
    }

    /**
     * 添加搜索记录
     */
    @PostMapping("/add")
    public ApiResponse<Map<String, String>> addSearchHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String searchWord,
            @RequestParam String searchType) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        searchHistoryService.addSearchHistory(userId, searchWord, searchType);
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
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String searchWord,
            @RequestParam String searchType) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        searchHistoryService.addSelfSelect(userId, searchWord, searchType);
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
