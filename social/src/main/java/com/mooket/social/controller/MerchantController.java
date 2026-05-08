package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.MerchantDetailDTO;
import com.mooket.social.dto.MerchantProductPageDTO;
import com.mooket.social.scheduler.MerchantStatScheduler;
import com.mooket.social.service.MerchantService;
import org.springframework.web.bind.annotation.*;

/**
 * 商家 Controller
 */
@RestController
@RequestMapping("/api/v1/merchant")
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantStatScheduler merchantStatScheduler;

    public MerchantController(MerchantService merchantService,
                              MerchantStatScheduler merchantStatScheduler) {
        this.merchantService = merchantService;
        this.merchantStatScheduler = merchantStatScheduler;
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{id}")
    public ApiResponse<MerchantDetailDTO> getMerchantDetail(
            @PathVariable("id") Long merchantId,
            @RequestParam(value = "category", required = false) String category) {
        try {
            MerchantDetailDTO detail = merchantService.getMerchantDetail(merchantId, category);
            return ApiResponse.success(detail);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 分页获取商家产品列表
     * @param type offer(报盘) 或 inquiry(求购)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    @GetMapping("/{id}/products")
    public ApiResponse<MerchantProductPageDTO> getMerchantProducts(
            @PathVariable("id") Long merchantId,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            MerchantProductPageDTO result = merchantService.getMerchantProducts(merchantId, category, type, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 手动触发商家统计更新（用于测试）
     */
    @PostMapping("/trigger-stat-update")
    public ApiResponse<String> triggerStatUpdate() {
        try {
            merchantStatScheduler.updateMerchantStats();
            return ApiResponse.success("商家统计更新已触发");
        } catch (Exception e) {
            return ApiResponse.error("触发失败: " + e.getMessage());
        }
    }
}
