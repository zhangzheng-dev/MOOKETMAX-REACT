package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.HomeCardsResponseDTO;
import com.mooket.social.service.HomeStatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 首页 Controller
 */
@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    private final HomeStatService homeStatService;

    public HomeController(HomeStatService homeStatService) {
        this.homeStatService = homeStatService;
    }

    /**
     * 获取热门搜索推荐
     */
    @GetMapping("/hot-search")
    public ApiResponse<List<HomeStatService.HotSearchItem>> getHotSearchRecommendations(
            @RequestParam(required = false, defaultValue = "牛") String category) {
        List<HomeStatService.HotSearchItem> recommendations = homeStatService.getHotSearchRecommendations(category);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取首页统计数据（报盘总量、求购总量）
     */
    @GetMapping("/stat")
    public ApiResponse<HomeStatService.HomeStatData> getHomeStatData(
            @RequestParam(required = false, defaultValue = "牛") String category) {
        HomeStatService.HomeStatData statData = homeStatService.getHomeStatData(category);
        return ApiResponse.success(statData);
    }

    /**
     * 获取首页卡片数据（瀑布流8种卡片）
     */
    @GetMapping("/cards")
    public ApiResponse<HomeCardsResponseDTO> getHomeCards(
            @RequestParam(required = false, defaultValue = "牛") String category) {
        HomeCardsResponseDTO cards = homeStatService.getHomeCards(category);
        return ApiResponse.success(cards);
    }

    /**
     * 手动触发首页统计计算（用于测试或数据修复）
     */
    @PostMapping("/compute-stats")
    public ApiResponse<Map<String, String>> computeStats() {
        try {
            homeStatService.computeAllStats();
            return ApiResponse.success(Map.of("message", "统计计算完成"));
        } catch (Exception e) {
            return ApiResponse.error("统计计算失败: " + e.getMessage());
        }
    }
}
