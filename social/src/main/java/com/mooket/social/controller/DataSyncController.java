package com.mooket.social.controller;

import com.mooket.social.common.ApiResponse;
import com.mooket.social.scheduler.DataSyncScheduler;
import com.mooket.social.service.BrandSyncService;
import com.mooket.social.service.BrandUpdateFromExcelService;
import com.mooket.social.service.DataSyncService;
import com.mooket.social.service.UserMerchantSyncService;
import com.mooket.social.service.MerchantSyncService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据同步测试接口
 * 仅用于测试，生产环境可删除
 */
@RestController
@RequestMapping("/api/v1/sync")
public class DataSyncController {

    private final DataSyncScheduler dataSyncScheduler;
    private final DataSyncService dataSyncService;
    private final UserMerchantSyncService userMerchantSyncService;
    private final MerchantSyncService merchantSyncService;
    private final BrandSyncService brandSyncService;
    private final BrandUpdateFromExcelService brandUpdateFromExcelService;

    public DataSyncController(DataSyncScheduler dataSyncScheduler,
                             DataSyncService dataSyncService,
                             UserMerchantSyncService userMerchantSyncService,
                             MerchantSyncService merchantSyncService,
                             BrandSyncService brandSyncService,
                             BrandUpdateFromExcelService brandUpdateFromExcelService) {
        this.dataSyncScheduler = dataSyncScheduler;
        this.dataSyncService = dataSyncService;
        this.userMerchantSyncService = userMerchantSyncService;
        this.merchantSyncService = merchantSyncService;
        this.brandSyncService = brandSyncService;
        this.brandUpdateFromExcelService = brandUpdateFromExcelService;
    }

    /**
     * 手动触发 dict_product 同步（一次性）
     */
    @PostMapping("/dict-product")
    public ApiResponse<String> syncDictProduct() {
        try {
            dataSyncScheduler.syncDictProductManually();
            return ApiResponse.success("dict_product 同步完成");
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发 biz_offer 首次同步（最近2天）
     */
    @PostMapping("/biz-offer/initial")
    public ApiResponse<String> syncBizOfferInitial() {
        try {
            int count = dataSyncService.sync();
            return ApiResponse.success("biz_offer 同步完成，共 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发 biz_offer 全量同步（最近2天，强制重新同步）
     */
    @PostMapping("/biz-offer/full")
    public ApiResponse<String> syncBizOfferFull() {
        try {
            int count = dataSyncService.syncFull();
            return ApiResponse.success("biz_offer 全量同步完成，共 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发 dict_factory 同步（一次性）
     */
    @PostMapping("/dict-factory")
    public ApiResponse<String> syncDictFactory() {
        try {
            dataSyncScheduler.syncDictFactoryManually();
            return ApiResponse.success("dict_factory 同步完成");
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发 rel_user_merchant 同步（一次性）
     */
    @PostMapping("/rel-user-merchant")
    public ApiResponse<String> syncRelUserMerchant() {
        try {
            int count = userMerchantSyncService.sync(null);
            return ApiResponse.success("rel_user_merchant 同步完成，共 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发 dict_merchant 同步（一次性）
     */
    @PostMapping("/dict-merchant")
    public ApiResponse<String> syncDictMerchant() {
        try {
            int count = merchantSyncService.sync();
            return ApiResponse.success("dict_merchant 同步完成，共 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发 dict_brand 同步（一次性）
     */
    @PostMapping("/dict-brand")
    public ApiResponse<String> syncDictBrand() {
        try {
            int count = brandSyncService.sync();
            return ApiResponse.success("dict_brand 同步完成，共 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 修复 dict_brand 中 factory_id 为 NULL 的记录
     * 用于修复历史上因 dict_factory 未同步而导致的关联缺失
     */
    @PostMapping("/dict-brand/repair")
    public ApiResponse<String> repairDictBrandFactoryId() {
        try {
            int count = brandSyncService.repairNullFactoryId();
            return ApiResponse.success("dict_brand factory_id 修复完成，共修复 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("修复失败: " + e.getMessage());
        }
    }

    /**
     * 修复 dict_brand 中 factory_id 与 dict_factory 中实际 factory_id 不一致的记录
     * 场景：同一 factory_no 因 country 不同产生多条 dict_factory 记录，brand 关联了错误的 factory_id
     */
    @PostMapping("/dict-brand/repair-inconsistent")
    public ApiResponse<String> repairDictBrandInconsistentFactoryId() {
        try {
            int count = brandSyncService.repairInconsistentFactoryId();
            return ApiResponse.success("dict_brand factory_id 不一致修复完成，共修复 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("修复失败: " + e.getMessage());
        }
    }

    /**
     * 修复 biz_offer 中 brand_id 为 NULL 的记录
     * 通过 factory_no + category 直查 dict_brand 补全 brand_id
     */
    @PostMapping("/biz-offer/repair-brand-id")
    public ApiResponse<String> repairBizOfferBrandId() {
        try {
            int count = dataSyncService.repairNullBrandId();
            return ApiResponse.success("biz_offer brand_id 修复完成，共修复 " + count + " 条");
        } catch (Exception e) {
            return ApiResponse.error("修复失败: " + e.getMessage());
        }
    }

    /**
     * 从Excel更新 dict_brand（使用集团vs厂号.xlsx）
     */
    @PostMapping("/dict-brand/from-excel")
    public ApiResponse<BrandUpdateResultVO> updateDictBrandFromExcel() {
        try {
            // 从JSON文件读取Excel数据
            String jsonPath = "/app/brand_data.json";
            java.nio.file.Path path = java.nio.file.Paths.get(jsonPath);
            if (!java.nio.file.Files.exists(path)) {
                return ApiResponse.error("品牌数据文件不存在: " + jsonPath);
            }

            String content = java.nio.file.Files.readString(path);
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> rawList = objectMapper.readValue(content, java.util.List.class);

            // 转换为BrandData列表
            List<BrandUpdateFromExcelService.BrandData> brandDataList = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> item : rawList) {
                String factoryNo = (String) item.get("factory_no");
                String brandName = (String) item.get("brand_name");
                String aliasList = (String) item.get("alias_list");
                brandDataList.add(new BrandUpdateFromExcelService.BrandData(factoryNo, brandName, aliasList));
            }

            // 执行全量导入
            BrandUpdateFromExcelService.UpdateBrandResult result = brandUpdateFromExcelService.fullImportFromExcel(brandDataList);

            return ApiResponse.success(new BrandUpdateResultVO(result.updateCount, result.insertCount, result.deleteCount, result.skipCount));
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 补充插入缺失的品牌数据（只插入Excel中有但dict_brand中没有的厂号）
     */
    @PostMapping("/dict-brand/fill-missing")
    public ApiResponse<BrandUpdateResultVO> fillMissingBrands() {
        try {
            String jsonPath = "/app/brand_data.json";
            java.nio.file.Path path = java.nio.file.Paths.get(jsonPath);
            if (!java.nio.file.Files.exists(path)) {
                return ApiResponse.error("品牌数据文件不存在: " + jsonPath);
            }

            String content = java.nio.file.Files.readString(path);
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> rawList = objectMapper.readValue(content, java.util.List.class);

            List<BrandUpdateFromExcelService.BrandData> brandDataList = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> item : rawList) {
                String factoryNo = (String) item.get("factory_no");
                String brandName = (String) item.get("brand_name");
                String aliasList = (String) item.get("alias_list");
                brandDataList.add(new BrandUpdateFromExcelService.BrandData(factoryNo, brandName, aliasList));
            }

            BrandUpdateFromExcelService.UpdateBrandResult result = brandUpdateFromExcelService.fillMissingBrands(brandDataList);

            return ApiResponse.success(new BrandUpdateResultVO(result.updateCount, result.insertCount, result.deleteCount, result.skipCount));
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 手动更新 stat_brand 和 stat_brand_product 表中的 brand_name
     */
    @PostMapping("/update-brand-names")
    public ApiResponse<String> updateBrandNames() {
        try {
            // 更新 stat_brand 表
            String updateStatBrand = """
                UPDATE stat_brand sb
                SET brand_name = db.brand_name
                FROM dict_brand db
                WHERE sb.brand_id = db.brand_id
                AND sb.brand_name != db.brand_name
                """;
            int brandCount = jdbcTemplate.update(updateStatBrand);

            // 更新 stat_brand_product 表
            String updateStatBrandProduct = """
                UPDATE stat_brand_product sbp
                SET brand_name = db.brand_name
                FROM dict_brand db
                WHERE sbp.brand_id = db.brand_id
                AND sbp.brand_name != db.brand_name
                """;
            int brandProductCount = jdbcTemplate.update(updateStatBrandProduct);

            return ApiResponse.success("更新完成：stat_brand " + brandCount + " 条，stat_brand_product " + brandProductCount + " 条");
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 添加 stat_country 表唯一约束（解决 HomeStatScheduler 执行失败问题）
     */
    @PostMapping("/add-stat-country-constraint")
    public ApiResponse<String> addStatCountryConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE stat_country ADD CONSTRAINT uk_stat_country_date_country_category UNIQUE (stat_date, country, category)");
            return ApiResponse.success("约束添加成功");
        } catch (Exception e) {
            return ApiResponse.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 清空 dict_factory 表（删除错误同步的数字国家数据）
     */
    @PostMapping("/dict-factory/truncate")
    public ApiResponse<String> truncateDictFactory() {
        try {
            dataSyncService.truncateDictFactory();
            return ApiResponse.success("dict_factory 已清空");
        } catch (Exception e) {
            return ApiResponse.error("清空失败: " + e.getMessage());
        }
    }

    /**
     * 修复 biz_offer 表中 country 为 null 的哥伦比亚厂号数据
     * 直接用工厂号关联 dict_factory 获取国家名称
     */
    @PostMapping("/fix-colombia-country")
    public ApiResponse<String> fixColombiaCountry() {
        try {
            int updated = dataSyncService.fixColombiaCountry();
            return ApiResponse.success("修复了 " + updated + " 条哥伦比亚国家数据");
        } catch (Exception e) {
            return ApiResponse.error("修复失败: " + e.getMessage());
        }
    }

    /**
     * 品牌更新结果VO
     */
    public static class BrandUpdateResultVO {
        public int updateCount;
        public int insertCount;
        public int deleteCount;
        public int skipCount;

        public BrandUpdateResultVO(int updateCount, int insertCount, int deleteCount, int skipCount) {
            this.updateCount = updateCount;
            this.insertCount = insertCount;
            this.deleteCount = deleteCount;
            this.skipCount = skipCount;
        }
    }
}
