package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.FactoryDetailDTO;
import com.mooket.social.service.FactoryService;
import org.springframework.web.bind.annotation.*;

/**
 * 厂号 Controller
 */
@RestController
@RequestMapping("/api/v1/factory")
public class FactoryController {

    private final FactoryService factoryService;

    public FactoryController(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    /**
     * 获取厂号详情
     *
     * @param country 国家名称
     * @param factoryNo 厂号
     * @param category 品类（牛/猪）
     * @param type 报盘类型：offer(报盘) 或 inquiry(求购)
     * @param sortBy 排序方式：comprehensive(综合) 或 price_asc/price_desc(价格)
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    @GetMapping("/detail")
    public ApiResponse<FactoryDetailDTO> getFactoryDetail(
            @RequestParam(value = "country") String country,
            @RequestParam(value = "factoryNo") String factoryNo,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "type", defaultValue = "offer") String type,
            @RequestParam(value = "sortBy", defaultValue = "comprehensive") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            FactoryDetailDTO result = factoryService.getFactoryDetail(country, factoryNo, category, type, sortBy, page, pageSize);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
