package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.BrandDetailDTO;
import com.mooket.social.service.BrandService;
import org.springframework.web.bind.annotation.*;

/**
 * 品牌 Controller
 */
@RestController
@RequestMapping("/api/v1/brand")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    /**
     * 获取品牌详情
     *
     * @param brandName 品牌名称
     * @param category 品类（牛/猪）
     * @param type 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price(价格)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    @GetMapping("/{brandName}")
    public ApiResponse<BrandDetailDTO> getBrandDetail(
            @PathVariable("brandName") String brandName,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", defaultValue = "offer") String type,
            @RequestParam(value = "sortBy", defaultValue = "comprehensive") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            BrandDetailDTO result = brandService.getBrandDetail(brandName, category, type, sortBy, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取品牌+产品详情（品牌+产品搜索结果页）
     *
     * @param brandName 品牌名称
     * @param productName 产品名称
     * @param category 品类
     * @param type 报盘类型：offer 或 inquiry
     * @param sortBy 排序方式
     * @param page 页码
     * @param pageSize 每页大小
     */
    @GetMapping("/{brandName}/product/{productName}")
    public ApiResponse<BrandDetailDTO> getBrandProductDetail(
            @PathVariable("brandName") String brandName,
            @PathVariable("productName") String productName,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", defaultValue = "offer") String type,
            @RequestParam(value = "sortBy", defaultValue = "comprehensive") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            BrandDetailDTO result = brandService.getBrandProductDetail(brandName, productName, category, type, sortBy, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}