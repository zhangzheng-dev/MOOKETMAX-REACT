package com.mooket.social.controller;

import com.mooket.social.dto.FactoryPriceComparisonDTO;
import com.mooket.social.service.PriceComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 价格对比 Controller
 */
@RestController
@RequestMapping("/api/v1/price-trend")
public class PriceComparisonController {

    @Autowired
    private PriceComparisonService priceComparisonService;

    /**
     * 获取多厂号价格对比数据
     *
     * @param country 国家
     * @param factoryNos 多个厂号（逗号分隔）
     * @param productName 产品名称
     * @param category 品类（默认牛）
     * @param offerType 报盘/求购（默认报盘）
     * @param days 天数（默认30）
     * @return 价格对比数据
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> getFactoryPriceComparison(
            @RequestParam("country") String country,
            @RequestParam("factoryNos") String factoryNos,
            @RequestParam("productName") String productName,
            @RequestParam(value = "category", defaultValue = "牛") String category,
            @RequestParam(value = "offerType", defaultValue = "报盘") String offerType,
            @RequestParam(value = "days", defaultValue = "30") int days) {

        try {
            // 解析厂号列表
            List<String> factoryNoList = Arrays.stream(factoryNos.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (factoryNoList.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "厂号不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            if (factoryNoList.size() > 6) {
                // 超过6个则只取前6个
                factoryNoList = factoryNoList.subList(0, 6);
            }

            FactoryPriceComparisonDTO result = priceComparisonService.getFactoryPriceComparison(
                    country, factoryNoList, productName, category, offerType, days);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            error.put("data", null);
            return ResponseEntity.status(500).body(error);
        }
    }
}
