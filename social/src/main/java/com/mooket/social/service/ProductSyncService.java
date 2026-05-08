package com.mooket.social.service;

import com.mooket.social.entity.mysql.SocialStandardGoodsName;
import com.mooket.social.entity.mysql.SocialStandardGoodsNameDetail;
import com.mooket.social.entity.mysql.SysDict;
import com.mooket.social.mysql.mapper.ProductAliasDTO;
import com.mooket.social.mysql.mapper.SocialStandardGoodsNameDetailMapper;
import com.mooket.social.mysql.mapper.SocialStandardGoodsNameMapper;
import com.mooket.social.mysql.mapper.SysDictMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 产品字典同步服务
 * 从 MySQL social_standard_goods_name 同步到 PostgreSQL dict_product
 */
@Service
public class ProductSyncService {

    private final SocialStandardGoodsNameMapper sourceMapper;
    private final SocialStandardGoodsNameDetailMapper detailMapper;
    private final SysDictMapper sysDictMapper;
    private final JdbcTemplate pgJdbcTemplate; // PostgreSQL JdbcTemplate

    public ProductSyncService(SocialStandardGoodsNameMapper sourceMapper,
                            SocialStandardGoodsNameDetailMapper detailMapper,
                            SysDictMapper sysDictMapper,
                            @Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.sourceMapper = sourceMapper;
        this.detailMapper = detailMapper;
        this.sysDictMapper = sysDictMapper;
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    /**
     * 执行全量同步
     */
    public int sync() {
        System.out.println("[ProductSyncService] 开始同步 dict_product...");

        // 1. 获取所有源数据
        List<SocialStandardGoodsName> sourceData = sourceMapper.selectAll();
        if (sourceData == null || sourceData.isEmpty()) {
            System.out.println("[ProductSyncService] 没有需要同步的产品数据");
            return 0;
        }

        System.out.println("[ProductSyncService] 待同步产品: " + sourceData.size() + " 条");

        // 2. 批量获取字典值（goods_category 转换）
        Map<String, String> dictValueMap = batchGetDictValues();

        // 3. 批量获取别名
        Map<String, String> aliasByName = batchGetAliases(sourceData);

        // 4. 转换并插入/更新
        int successCount = 0;
        int failCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (SocialStandardGoodsName src : sourceData) {
            try {
                upsert(src, dictValueMap, aliasByName, now);
                successCount++;
            } catch (Exception e) {
                System.err.println("[ProductSyncService] 同步失败, id=" + src.getId() + ", error=" + e.getMessage());
                failCount++;
            }
        }

        System.out.println("[ProductSyncService] 同步完成: 成功=" + successCount + ", 失败=" + failCount);
        return successCount;
    }

    /**
     * 批量获取字典值 (goods_category 转换)
     */
    private Map<String, String> batchGetDictValues() {
        List<SysDict> dicts = sysDictMapper.selectByDictNameEn("goods_category");
        return dicts.stream()
                .collect(Collectors.toMap(SysDict::getDictKey, SysDict::getDictValue));
    }

    /**
     * 批量获取别名（通过 SQL JOIN 直接按产品名聚合，避免 ID 截断问题）
     */
    private Map<String, String> batchGetAliases(List<SocialStandardGoodsName> sourceData) {
        // 直接用 SQL JOIN 查询，按产品名聚合别名
        List<ProductAliasDTO> aliasList = detailMapper.selectAliasJoinByProductName();

        System.out.println("[ProductSyncService] 别名JOIN记录数: " + aliasList.size());

        // 按产品名分组，用 "、" 拼接多个别名
        Map<String, String> aliasByName = aliasList.stream()
                .filter(dto -> dto.getProductName() != null && dto.getAliasName() != null)
                .collect(Collectors.groupingBy(
                        ProductAliasDTO::getProductName,
                        Collectors.mapping(ProductAliasDTO::getAliasName, Collectors.joining("、"))
                ));

        System.out.println("[ProductSyncService] 别名数据(按产品名): " + aliasByName.size() + " 个产品有别名");
        // 打印前3个示例
        aliasByName.entrySet().stream().limit(3)
                .forEach(e -> System.out.println("  [别名示例] " + e.getKey() + " → " + e.getValue()));
        return aliasByName;
    }

    /**
     * 存在则更新，不存在则插入
     */
    private void upsert(SocialStandardGoodsName src,
                        Map<String, String> dictValueMap,
                        Map<String, String> aliasMap,
                        LocalDateTime now) {
        String productName = src.getStandardGoodsName();
        String category = src.getGoodsCategory() != null ?
                dictValueMap.get(src.getGoodsCategory().toString()) : null;
        String aliasList = aliasMap.get(productName);

        // 先查询是否存在
        Integer exists = pgJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dict_product WHERE product_name = ?",
                Integer.class,
                productName);

        if (exists != null && exists > 0) {
            // 更新
            pgJdbcTemplate.update(
                    "UPDATE dict_product SET category = ?, alias_list = ?, update_time = ? WHERE product_name = ?",
                    category, aliasList, now, productName);
            if (aliasList != null) {
                System.out.println("  [更新别名] " + productName + " → " + aliasList);
            }
        } else {
            // 插入
            pgJdbcTemplate.update(
                    "INSERT INTO dict_product (category, product_name, alias_list, create_time, update_time) VALUES (?, ?, ?, ?, ?)",
                    category, productName, aliasList, now, now);
            if (aliasList != null) {
                System.out.println("  [插入别名] " + productName + " → " + aliasList);
            }
        }
    }
}
