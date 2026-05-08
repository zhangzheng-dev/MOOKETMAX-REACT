package com.mooket.social.service;

import com.mooket.social.mapper.DictBrandMapper;
import com.mooket.social.mapper.DictFactoryMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 从Excel导入品牌字典服务
 */
@Service
public class BrandUpdateFromExcelService {

    private final DictFactoryMapper factoryMapper;
    private final DictBrandMapper brandMapper;
    private final JdbcTemplate pgJdbcTemplate;

    public BrandUpdateFromExcelService(DictFactoryMapper factoryMapper,
                                       DictBrandMapper brandMapper,
                                       @Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.factoryMapper = factoryMapper;
        this.brandMapper = brandMapper;
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    /**
     * 全量导入品牌数据
     * 先清空dict_brand表，再插入Excel中所有有效的厂号记录
     */
    public UpdateBrandResult fullImportFromExcel(List<BrandData> brandDataList) {
        System.out.println("[BrandUpdateFromExcelService] 开始全量导入品牌数据，共 " + brandDataList.size() + " 条...");

        LocalDateTime now = LocalDateTime.now();
        int insertCount = 0;
        int skipCount = 0;

        // 清空dict_brand表
        pgJdbcTemplate.update("DELETE FROM dict_brand");
        System.out.println("[BrandUpdateFromExcelService] 已清空dict_brand表");

        for (BrandData data : brandDataList) {
            if (data.factoryNo == null || data.factoryNo.isEmpty()) {
                skipCount++;
                continue;
            }
            if (data.factoryNo.contains("暂停输华")) {
                skipCount++;
                continue;
            }

            try {
                List<com.mooket.social.entity.DictFactory> factories = factoryMapper.selectByFactoryNo(data.factoryNo);
                if (factories == null || factories.isEmpty()) {
                    System.out.println("[BrandUpdateFromExcelService] 跳过: 厂号 " + data.factoryNo + " 在dict_factory中不存在");
                    skipCount++;
                    continue;
                }

                com.mooket.social.entity.DictFactory factory = factories.get(0);
                Integer factoryId = factory.getFactoryId();
                String country = factory.getCountry();

                pgJdbcTemplate.update(
                        "INSERT INTO dict_brand (brand_name, category, alias_list, factory_id, factory_no, country, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        data.brandName, "牛", data.aliasList, factoryId, data.factoryNo, country, now, now);
                insertCount++;

            } catch (Exception e) {
                System.err.println("[BrandUpdateFromExcelService] 插入失败, factoryNo=" + data.factoryNo + ", error=" + e.getMessage());
                e.printStackTrace();
                skipCount++;
            }
        }

        System.out.println("[BrandUpdateFromExcelService] 全量导入完成: 插入=" + insertCount + ", 跳过=" + skipCount);
        return new UpdateBrandResult(0, insertCount, 0, skipCount);
    }

    /**
     * 补充插入缺失的品牌数据
     * 只插入Excel中有但dict_brand中没有的厂号记录
     */
    public UpdateBrandResult fillMissingBrands(List<BrandData> brandDataList) {
        System.out.println("[BrandUpdateFromExcelService] 开始补充插入缺失品牌数据，共 " + brandDataList.size() + " 条...");

        LocalDateTime now = LocalDateTime.now();
        int insertCount = 0;
        int skipCount = 0;

        // 获取dict_brand中已存在的factory_no
        Set<String> existingFactoryNos = new HashSet<>();
        List<Map<String, Object>> existing = pgJdbcTemplate.queryForList(
                "SELECT DISTINCT factory_no FROM dict_brand WHERE factory_no IS NOT NULL AND factory_no != ''");
        for (Map<String, Object> row : existing) {
            existingFactoryNos.add((String) row.get("factory_no"));
        }
        System.out.println("[BrandUpdateFromExcelService] dict_brand已有厂号数: " + existingFactoryNos.size());

        for (BrandData data : brandDataList) {
            if (data.factoryNo == null || data.factoryNo.isEmpty()) {
                skipCount++;
                continue;
            }
            if (data.factoryNo.contains("暂停输华")) {
                skipCount++;
                continue;
            }
            // 跳过已存在的厂号
            if (existingFactoryNos.contains(data.factoryNo)) {
                skipCount++;
                continue;
            }

            try {
                List<com.mooket.social.entity.DictFactory> factories = factoryMapper.selectByFactoryNo(data.factoryNo);
                if (factories == null || factories.isEmpty()) {
                    System.out.println("[BrandUpdateFromExcelService] 跳过: 厂号 " + data.factoryNo + " 在dict_factory中不存在");
                    skipCount++;
                    continue;
                }

                com.mooket.social.entity.DictFactory factory = factories.get(0);
                Integer factoryId = factory.getFactoryId();
                String country = factory.getCountry();

                pgJdbcTemplate.update(
                        "INSERT INTO dict_brand (brand_name, category, alias_list, factory_id, factory_no, country, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        data.brandName, "牛", data.aliasList, factoryId, data.factoryNo, country, now, now);
                insertCount++;

            } catch (Exception e) {
                System.err.println("[BrandUpdateFromExcelService] 插入失败, factoryNo=" + data.factoryNo + ", error=" + e.getMessage());
                e.printStackTrace();
                skipCount++;
            }
        }

        System.out.println("[BrandUpdateFromExcelService] 补充插入完成: 插入=" + insertCount + ", 跳过=" + skipCount);
        return new UpdateBrandResult(0, insertCount, 0, skipCount);
    }

    /**
     * 品牌数据
     */
    public static class BrandData {
        public String factoryNo;
        public String brandName;
        public String aliasList;

        public BrandData(String factoryNo, String brandName, String aliasList) {
            this.factoryNo = factoryNo;
            this.brandName = brandName;
            this.aliasList = aliasList;
        }
    }

    /**
     * 导入结果
     */
    public static class UpdateBrandResult {
        public int updateCount;
        public int insertCount;
        public int deleteCount;
        public int skipCount;

        public UpdateBrandResult(int updateCount, int insertCount, int deleteCount, int skipCount) {
            this.updateCount = updateCount;
            this.insertCount = insertCount;
            this.deleteCount = deleteCount;
            this.skipCount = skipCount;
        }
    }
}
