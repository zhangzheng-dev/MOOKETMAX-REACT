package com.mooket.social.controller;

import com.mooket.social.dto.PriceTrendDTO;
import com.mooket.social.mapper.StatPriceTrendMapper;
import com.mooket.social.service.PriceTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 价格趋势 Controller
 */
@RestController
@RequestMapping("/api/v1/trend")
public class PriceTrendController {

    @Autowired
    private PriceTrendService priceTrendService;

    /**
     * 获取价格趋势（近30天历史 + 当天实时）
     *
     * @param type 维度类型: country_product / country_factory_product
     * @param country 国家
     * @param productId 产品ID
     * @param factoryNo 厂号（可为空）
     * @param offerType 报盘/求购: 报盘 / 求购
     * @return 价格趋势数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPriceTrend(
            @RequestParam("type") String type,
            @RequestParam("country") String country,
            @RequestParam("productId") Integer productId,
            @RequestParam(value = "factoryNo", required = false) String factoryNo,
            @RequestParam("offerType") String offerType) {

        try {
            PriceTrendDTO trend = priceTrendService.getPriceTrend(
                    type, country, productId, factoryNo, offerType);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", trend);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            error.put("data", null);
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 手动触发价格趋势计算（用于测试或管理员操作）
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> triggerCalculation() {
        try {
            priceTrendService.calculateAndSaveTodayTrends();

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "计算完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "计算失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 回填历史数据（用于初始化）
     *
     * @param days 回填天数（默认29天）
     */
    @PostMapping("/backfill")
    public ResponseEntity<Map<String, Object>> backfillData(
            @RequestParam(value = "days", defaultValue = "29") int days) {
        try {
            priceTrendService.backfillHistoricalData(days);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "回填完成，共回填" + days + "天数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "回填失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 回填单个产品的历史数据
     *
     * @param type 维度类型: country_product / country_factory_product
     * @param country 国家
     * @param productId 产品ID
     * @param factoryNo 厂号（可为空）
     * @param offerType 报盘/求购
     * @param days 回填天数
     */
    @PostMapping("/backfill/single")
    public ResponseEntity<Map<String, Object>> backfillSingleProduct(
            @RequestParam("type") String type,
            @RequestParam("country") String country,
            @RequestParam("productId") Integer productId,
            @RequestParam(value = "factoryNo", required = false) String factoryNo,
            @RequestParam("offerType") String offerType,
            @RequestParam(value = "days", defaultValue = "29") int days) {
        try {
            priceTrendService.backfillSingleProduct(type, country, productId, factoryNo, offerType, days);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "回填完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "回填失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 回填昨天的数据（每天00:05执行，固化昨天的最终数据）
     */
    @PostMapping("/backfill/yesterday")
    public ResponseEntity<Map<String, Object>> backfillYesterday() {
        try {
            priceTrendService.backfillYesterday();

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "昨日数据回填完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "回填失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
