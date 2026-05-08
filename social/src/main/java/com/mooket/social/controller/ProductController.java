package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.ProductDetailDTO;
import com.mooket.social.service.ProductService;
import org.springframework.web.bind.annotation.*;

/**
 * 产品 Controller
 */
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取产品详情（按产品聚合所有商家的报盘/求购）
     *
     * @param id 产品ID
     * @param category 品类（牛/猪）
     * @param type 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price(价格)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailDTO> getProductDetail(
            @PathVariable("id") Integer id,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", defaultValue = "offer") String type,
            @RequestParam(value = "sortBy", defaultValue = "comprehensive") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            ProductDetailDTO result = productService.getProductDetail(id, category, type, sortBy, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
