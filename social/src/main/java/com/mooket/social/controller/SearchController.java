package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.common.JwtUtil;
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
     */
    @PostMapping("/history")
    public ApiResponse<Void> saveSearchHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String searchWord,
            @RequestParam String searchType,
            @RequestParam(required = false, defaultValue = "0") Integer isSelfSelect,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String factoryNo,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long merchantId) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        searchService.saveSearchHistory(userId, searchWord, searchType, isSelfSelect,
                productId, productName, country, factoryNo, brandId, merchantId);
        return ApiResponse.success(null);
    }
}
