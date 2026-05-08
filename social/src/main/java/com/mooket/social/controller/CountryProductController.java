package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.CountryProductDetailDTO;
import com.mooket.social.service.CountryProductService;
import org.springframework.web.bind.annotation.*;

/**
 * 国家+产品 Controller
 */
@RestController
@RequestMapping("/api/v1/country-product")
public class CountryProductController {

    private final CountryProductService countryProductService;

    public CountryProductController(CountryProductService countryProductService) {
        this.countryProductService = countryProductService;
    }

    /**
     * 获取国家+产品详情
     *
     * @param country 国家名称
     * @param productName 产品名称
     * @param type 类型：offer(报盘) 或 inquiry(求购)
     * @param category 品类（牛/猪）
     * @param sortBy 排序方式：comprehensive(综合) 或 price_asc(价格升序) 或 price_desc(价格降序)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    @GetMapping
    public ApiResponse<CountryProductDetailDTO> getCountryProductDetail(
            @RequestParam("country") String country,
            @RequestParam("productName") String productName,
            @RequestParam(value = "type", defaultValue = "offer") String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sortBy", defaultValue = "comprehensive") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            CountryProductDetailDTO result = countryProductService.getCountryProductDetail(
                    country, productName, type, category, sortBy, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
