package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.CountryDetailDTO;
import com.mooket.social.service.CountryService;
import org.springframework.web.bind.annotation.*;

/**
 * 国家 Controller
 */
@RestController
@RequestMapping("/api/v1/country")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    /**
     * 获取国家详情
     *
     * @param country 国家名称
     * @param category 品类（牛/猪）
     * @param type 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price(价格)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    @GetMapping("/{country}")
    public ApiResponse<CountryDetailDTO> getCountryDetail(
            @PathVariable("country") String country,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", defaultValue = "offer") String type,
            @RequestParam(value = "sortBy", defaultValue = "comprehensive") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            CountryDetailDTO result = countryService.getCountryDetail(country, category, type, sortBy, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
