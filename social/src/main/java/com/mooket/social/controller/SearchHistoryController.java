package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.common.JwtUtil;
import com.mooket.social.dto.HomeCardsResponseDTO;
import com.mooket.social.service.SearchHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Search history endpoints.
 */
@RestController
@RequestMapping("/api/v1/search-history")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    public SearchHistoryController(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping("/recent")
    public ApiResponse<List<SearchHistoryService.SearchHistoryDTO>> getRecentSearches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "200") int limit) {
        AuthState authState = requireUser(authHeader);
        if (!authState.valid()) {
            return ApiResponse.error(401, authState.message());
        }
        return ApiResponse.success(searchHistoryService.getRecentSearches(authState.userId(), limit));
    }

    @GetMapping("/cards/recent")
    public ApiResponse<HomeCardsResponseDTO> getRecentSearchCards(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "牛") String category) {
        AuthState authState = requireUser(authHeader);
        if (!authState.valid()) {
            return ApiResponse.error(401, authState.message());
        }
        return ApiResponse.success(searchHistoryService.getRecentSearchCards(authState.userId(), category));
    }

    @GetMapping("/self-select")
    public ApiResponse<List<SearchHistoryService.SearchHistoryDTO>> getSelfSelectSearches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "200") int limit) {
        AuthState authState = requireUser(authHeader);
        if (!authState.valid()) {
            return ApiResponse.error(401, authState.message());
        }
        return ApiResponse.success(searchHistoryService.getSelfSelectSearches(authState.userId(), limit));
    }

    @GetMapping("/cards/self-select")
    public ApiResponse<HomeCardsResponseDTO> getSelfSelectCards(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "牛") String category) {
        AuthState authState = requireUser(authHeader);
        if (!authState.valid()) {
            return ApiResponse.error(401, authState.message());
        }
        return ApiResponse.success(searchHistoryService.getSelfSelectCards(authState.userId(), category));
    }

    @PostMapping("/add")
    public ApiResponse<Map<String, String>> addSearchHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String searchWord,
            @RequestParam String searchType) {
        AuthState authState = requireUser(authHeader);
        if (!authState.valid()) {
            return ApiResponse.error(401, authState.message());
        }
        searchHistoryService.addSearchHistory(authState.userId(), searchWord, searchType);
        return ApiResponse.success(Map.of("message", "添加成功"));
    }

    @DeleteMapping("/{historyId}")
    public ApiResponse<Map<String, String>> deleteSearchHistory(@PathVariable Long historyId) {
        searchHistoryService.deleteSearchHistory(historyId);
        return ApiResponse.success(Map.of("message", "删除成功"));
    }

    @DeleteMapping("/batch")
    public ApiResponse<Map<String, String>> batchDeleteSearchHistory(@RequestBody List<Long> historyIds) {
        searchHistoryService.batchDeleteSearchHistory(historyIds);
        return ApiResponse.success(Map.of("message", "批量删除成功"));
    }

    @PostMapping("/self-select/add")
    public ApiResponse<Map<String, String>> addSelfSelect(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String searchWord,
            @RequestParam String searchType) {
        AuthState authState = requireUser(authHeader);
        if (!authState.valid()) {
            return ApiResponse.error(401, authState.message());
        }
        searchHistoryService.addSelfSelect(authState.userId(), searchWord, searchType);
        return ApiResponse.success(Map.of("message", "添加自选成功"));
    }

    @PostMapping("/self-select/cancel/{historyId}")
    public ApiResponse<Map<String, String>> cancelSelfSelect(@PathVariable Long historyId) {
        searchHistoryService.cancelSelfSelect(historyId);
        return ApiResponse.success(Map.of("message", "取消自选成功"));
    }

    @PostMapping("/self-select/move/{historyId}")
    public ApiResponse<Map<String, String>> moveToSelfSelect(@PathVariable Long historyId) {
        searchHistoryService.moveToSelfSelect(historyId);
        return ApiResponse.success(Map.of("message", "移动到自选成功"));
    }

    private AuthState requireUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new AuthState(null, "请先登录", false);
        }

        String token = authHeader.substring(7);
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return new AuthState(null, validation.getMessage(), false);
        }

        return new AuthState(JwtUtil.getUserId(token), null, true);
    }

    private record AuthState(Long userId, String message, boolean valid) {
    }
}
