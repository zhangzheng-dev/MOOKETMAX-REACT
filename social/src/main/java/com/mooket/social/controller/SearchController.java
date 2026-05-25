package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.common.JwtUtil;
import com.mooket.social.dto.SearchSuggestDTO;
import com.mooket.social.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Search endpoints.
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/suggest")
    public ApiResponse<List<SearchSuggestDTO>> getSearchSuggestions(
            @RequestParam(required = false, defaultValue = "牛") String category,
            @RequestParam String keyword) {
        return ApiResponse.success(searchService.getSearchSuggestions(category, keyword));
    }

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

        String token = authHeader.replace("Bearer ", "");
        JwtUtil.ValidationResult validation = JwtUtil.validateTokenDetailed(token);
        if (!validation.isValid()) {
            return ApiResponse.error(401, validation.getMessage());
        }

        Long userId = JwtUtil.getUserId(token);
        searchService.saveSearchHistory(userId, searchWord, searchType, isSelfSelect,
                productId, productName, country, factoryNo, brandId, merchantId);
        return ApiResponse.success(null);
    }
}
