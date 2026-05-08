package com.mooket.social.service;

import com.mooket.social.entity.DictBrand;
import com.mooket.social.entity.DictFactory;
import com.mooket.social.entity.mysql.SocialGroupFactoryNo;
import com.mooket.social.mapper.DictFactoryMapper;
import com.mooket.social.mysql.mapper.SocialGroupFactoryNoMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 品牌字典同步服务
 * 从 MySQL social_group_factory_no 同步到 PostgreSQL dict_brand
 * 数据来源：
 *   - brand_name  ← group_name
 *   - alias_list  ← group_alias
 *   - factory_no  ← factory_no
 *   - factory_id  ← dict_factory.factory_no 查询
 *   - country     ← dict_factory.factory_no 查询
 */
@Service
public class BrandSyncService {

    private final SocialGroupFactoryNoMapper sourceMapper;
    private final DictFactoryMapper factoryMapper;
    private final JdbcTemplate pgJdbcTemplate;

    public BrandSyncService(SocialGroupFactoryNoMapper sourceMapper,
                           DictFactoryMapper factoryMapper,
                           @Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.sourceMapper = sourceMapper;
        this.factoryMapper = factoryMapper;
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    /**
     * 执行全量同步
     */
    public int sync() {
        System.out.println("[BrandSyncService] 开始同步 dict_brand（来源: social_group_factory_no）...");

        // 1. 获取所有源数据
        List<SocialGroupFactoryNo> sourceData = sourceMapper.selectAllActive();
        if (sourceData == null || sourceData.isEmpty()) {
            System.out.println("[BrandSyncService] 没有需要同步的品牌数据");
            return 0;
        }

        System.out.println("[BrandSyncService] 待同步品牌: " + sourceData.size() + " 条");

        // 2. 批量获取厂号信息（factory_no → factoryId + country）
        Map<String, DictFactory> factoryInfoMap = batchGetFactoryInfo(sourceData);

        // 3. 转换并插入/更新
        int successCount = 0;
        int failCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (SocialGroupFactoryNo src : sourceData) {
            try {
                upsert(src, factoryInfoMap, now);
                successCount++;
            } catch (Exception e) {
                System.err.println("[BrandSyncService] 同步失败, id=" + src.getId() + ", error=" + e.getMessage());
                e.printStackTrace();
                failCount++;
            }
        }

        System.out.println("[BrandSyncService] 同步完成: 成功=" + successCount + ", 失败=" + failCount);
        return successCount;
    }

    /**
     * 修复 dict_brand 中 factory_id 为 NULL 的记录
     * 通过 factory_no 重新关联正确的 factory_id
     */
    public int repairNullFactoryId() {
        System.out.println("[BrandSyncService] 开始修复 dict_brand 中 factory_id 为 NULL 的记录...");

        // 1. 查出所有 factory_id 为 NULL 的记录
        List<DictBrand> nullFactoryBrands = pgJdbcTemplate.query(
                "SELECT brand_id, brand_name, factory_no FROM dict_brand WHERE factory_id IS NULL",
                (rs, rowNum) -> {
                    DictBrand b = new DictBrand();
                    b.setBrandId(rs.getInt("brand_id"));
                    b.setBrandName(rs.getString("brand_name"));
                    b.setFactoryNo(rs.getString("factory_no"));
                    return b;
                });

        if (nullFactoryBrands.isEmpty()) {
            System.out.println("[BrandSyncService] 没有需要修复的记录");
            return 0;
        }

        System.out.println("[BrandSyncService] 发现 " + nullFactoryBrands.size() + " 条 factory_id 为 NULL 的记录");

        int fixedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (DictBrand brand : nullFactoryBrands) {
            String factoryNo = brand.getFactoryNo();
            if (factoryNo == null || factoryNo.isEmpty()) {
                continue;
            }

            try {
                // 通过 factory_no 查找 dict_factory 中的 factory_id
                List<DictFactory> factories = factoryMapper.selectByFactoryNo(factoryNo);
                if (factories != null && !factories.isEmpty()) {
                    Integer factoryId = factories.get(0).getFactoryId();
                    pgJdbcTemplate.update(
                            "UPDATE dict_brand SET factory_id = ?, update_time = ? WHERE brand_id = ?",
                            factoryId, now, brand.getBrandId());
                    System.out.println("[BrandSyncService] 修复: brand_id=" + brand.getBrandId()
                            + ", brand_name=" + brand.getBrandName() + ", factory_no=" + factoryNo
                            + " → factory_id=" + factoryId);
                    fixedCount++;
                } else {
                    System.out.println("[BrandSyncService] 无法修复: factory_no=" + factoryNo + " 在 dict_factory 中不存在");
                }
            } catch (Exception e) {
                System.err.println("[BrandSyncService] 修复失败: brand_id=" + brand.getBrandId() + ", error=" + e.getMessage());
            }
        }

        System.out.println("[BrandSyncService] 修复完成: 成功修复 " + fixedCount + " 条");
        return fixedCount;
    }

    /**
     * 修复 dict_brand 中 factory_id 与 dict_factory 中实际 factory_id 不一致的记录
     * 场景：dict_factory 中同一 factory_no 因 country 不同产生多条记录，BrandSyncService 查到了旧记录
     */
    public int repairInconsistentFactoryId() {
        System.out.println("[BrandSyncService] 开始修复 dict_brand 中 factory_id 不一致的记录...");

        // 1. 查出所有有 factory_id 且有 factory_no 的 dict_brand 记录
        List<DictBrand> allBrands = pgJdbcTemplate.query(
                "SELECT brand_id, brand_name, factory_no, factory_id FROM dict_brand WHERE factory_id IS NOT NULL AND factory_no IS NOT NULL AND factory_no != ''",
                (rs, rowNum) -> {
                    DictBrand b = new DictBrand();
                    b.setBrandId(rs.getInt("brand_id"));
                    b.setBrandName(rs.getString("brand_name"));
                    b.setFactoryNo(rs.getString("factory_no"));
                    b.setFactoryId(rs.getInt("factory_id"));
                    return b;
                });

        if (allBrands.isEmpty()) {
            System.out.println("[BrandSyncService] 没有需要检查的记录");
            return 0;
        }

        int fixedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (DictBrand brand : allBrands) {
            String factoryNo = brand.getFactoryNo();
            try {
                // 查 dict_factory 中该 factory_no 对应的 factory_id（取第一条，忽略 country 差异）
                List<DictFactory> factories = factoryMapper.selectByFactoryNo(factoryNo);
                if (factories != null && !factories.isEmpty()) {
                    Integer correctFactoryId = factories.get(0).getFactoryId();
                    if (!correctFactoryId.equals(brand.getFactoryId())) {
                        System.out.println("[BrandSyncService] 修复不一致: brand_id=" + brand.getBrandId()
                                + ", brand_name=" + brand.getBrandName() + ", factory_no=" + factoryNo
                                + ", 旧factory_id=" + brand.getFactoryId() + " → 新factory_id=" + correctFactoryId);
                        pgJdbcTemplate.update(
                                "UPDATE dict_brand SET factory_id = ?, update_time = ? WHERE brand_id = ?",
                                correctFactoryId, now, brand.getBrandId());
                        fixedCount++;
                    }
                }
            } catch (Exception e) {
                System.err.println("[BrandSyncService] 修复失败: brand_id=" + brand.getBrandId() + ", error=" + e.getMessage());
            }
        }

        System.out.println("[BrandSyncService] 修复完成: 成功修复 " + fixedCount + " 条");
        return fixedCount;
    }

    /**
     * 批量获取厂号信息
     */
    private Map<String, DictFactory> batchGetFactoryInfo(List<SocialGroupFactoryNo> sourceData) {
        // 收集所有不重复的 factory_no
        Map<String, DictFactory> result = new HashMap<>();
        for (SocialGroupFactoryNo src : sourceData) {
            String factoryNo = src.getFactoryNo();
            if (factoryNo == null || factoryNo.isEmpty()) continue;
            if (result.containsKey(factoryNo)) continue;

            try {
                List<DictFactory> factories = factoryMapper.selectByFactoryNo(factoryNo);
                if (factories != null && !factories.isEmpty()) {
                    result.put(factoryNo, factories.get(0));
                }
            } catch (Exception e) {
                // ignore
            }
        }

        System.out.println("[BrandSyncService] 厂号信息获取完成: " + result.size() + " 个");
        return result;
    }

    /**
     * 转换品类
     */
    private String convertCategory(Integer category) {
        if (category == null) return null;
        switch (category) {
            case 1: return "牛";
            case 2: return "羊";
            case 3: return "猪";
            case 4: return "禽";
            case 5: return "水产";
            default: return null;
        }
    }

    /**
     * 存在则更新，不存在则插入
     */
    private void upsert(SocialGroupFactoryNo src, Map<String, DictFactory> factoryInfoMap, LocalDateTime now) {
        String brandName = src.getGroupName();
        String factoryNo = src.getFactoryNo();
        String aliasList = src.getGroupAlias();
        DictFactory factoryInfo = factoryInfoMap.get(factoryNo);

        Integer factoryId = factoryInfo != null ? factoryInfo.getFactoryId() : null;
        String country = factoryInfo != null ? factoryInfo.getCountry() : null;
        String category = convertCategory(src.getGoodsCategory());

        // 先查询该品牌+厂号组合是否已存在
        DictBrand existing = pgJdbcTemplate.queryForObject(
                "SELECT brand_id, factory_id FROM dict_brand WHERE brand_name = ? AND factory_no = ?",
                new Object[]{brandName, factoryNo},
                (rs, rowNum) -> {
                    DictBrand b = new DictBrand();
                    b.setBrandId(rs.getInt("brand_id"));
                    Integer fid = rs.getInt("factory_id");
                    b.setFactoryId(rs.wasNull() ? null : fid);
                    return b;
                });

        if (existing != null) {
            // 记录已存在，按 brand_id 更新
            pgJdbcTemplate.update(
                    "UPDATE dict_brand SET category = ?, alias_list = ?, factory_id = ?, country = ?, update_time = ? WHERE brand_id = ?",
                    category, aliasList != null ? aliasList : "", factoryId, country, now, existing.getBrandId());
        } else {
            // 品牌+厂号组合不存在，直接插入（允许多厂号同品牌）
            pgJdbcTemplate.update(
                    "INSERT INTO dict_brand (brand_name, category, alias_list, factory_id, factory_no, country, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    brandName, category, aliasList != null ? aliasList : "", factoryId, factoryNo, country, now, now);
        }
    }
}
