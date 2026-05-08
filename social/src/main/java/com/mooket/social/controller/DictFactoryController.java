package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.dto.FactoryFilterDTO;
import com.mooket.social.service.DictFactoryService;
import org.springframework.web.bind.annotation.*;

/**
 * 厂号字典 Controller
 */
@RestController
@RequestMapping("/api/v1/factory")
public class DictFactoryController {

    private final DictFactoryService dictFactoryService;

    public DictFactoryController(DictFactoryService dictFactoryService) {
        this.dictFactoryService = dictFactoryService;
    }

    /**
     * 获取厂号筛选数据
     * @param category 类别（牛/猪），默认牛
     */
    @GetMapping("/filter")
    public ApiResponse<FactoryFilterDTO> getFactoryFilter(
            @RequestParam(value = "category", defaultValue = "牛") String category) {
        try {
            FactoryFilterDTO filter = dictFactoryService.getFactoryFilter(category);
            return ApiResponse.success(filter);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
