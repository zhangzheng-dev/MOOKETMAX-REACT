package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 管理接口
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final JdbcTemplate jdbcTemplate;

    public AdminController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 创建索引（用于优化查询性能）
     */
    @PostMapping("/create-indexes")
    public ApiResponse<String> createIndexes() {
        try {
            // 产品详情查询优化索引 - 覆盖主要查询条件
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_biz_offer_product_query " +
                    "ON biz_offer(product_id, category, offer_type, status, data_date)");

            // 聚合查询索引 - 包含分组字段
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_biz_offer_group_agg " +
                    "ON biz_offer(product_id, category, offer_type, status, data_date, country, factory_no)");

            return ApiResponse.success("索引创建成功");
        } catch (Exception e) {
            return ApiResponse.error("创建索引失败: " + e.getMessage());
        }
    }
}
