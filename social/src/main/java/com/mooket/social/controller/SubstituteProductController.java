package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.SubstituteProductDTO;
import com.mooket.social.service.SubstituteProductService;
import org.springframework.web.bind.annotation.*;

/**
 * 平替产品 Controller
 */
@RestController
@RequestMapping("/api/v1/substitute")
public class SubstituteProductController {

    private final SubstituteProductService substituteProductService;

    public SubstituteProductController(SubstituteProductService substituteProductService) {
        this.substituteProductService = substituteProductService;
    }

    /**
     * 获取平替产品列表（同产品同等级的所有厂号）
     *
     * @param country 国家
     * @param factoryNo 当前厂号
     * @param productName 产品名称
     * @param category 品类
     */
    @GetMapping("/products")
    public ApiResponse<SubstituteProductDTO> getSubstituteProducts(
            @RequestParam("country") String country,
            @RequestParam("factoryNo") String factoryNo,
            @RequestParam("productName") String productName,
            @RequestParam(value = "category", defaultValue = "牛") String category) {
        try {
            SubstituteProductDTO result = substituteProductService.getSubstituteProducts(
                    country, factoryNo, productName, category);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取平替产品详情（带报盘数据）
     *
     * @param country 国家
     * @param factoryNo 厂号
     * @param productName 产品名称
     * @param category 品类
     * @param type offer/inquiry
     * @param sortBy 排序
     * @param page 页码
     * @param pageSize 每页大小
     */
    @GetMapping("/product/detail")
    public ApiResponse<SubstituteProductDTO.SubstituteProductDetailDTO> getSubstituteProductDetail(
            @RequestParam("country") String country,
            @RequestParam("factoryNo") String factoryNo,
            @RequestParam("productName") String productName,
            @RequestParam(value = "category", defaultValue = "牛") String category,
            @RequestParam(value = "type", defaultValue = "offer") String type,
            @RequestParam(value = "sortBy", defaultValue = "comprehensive") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            SubstituteProductDTO.SubstituteProductDetailDTO result = substituteProductService.getSubstituteProductDetail(
                    country, factoryNo, productName, category, type, sortBy, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}