package com.mooket.social.service;

import com.mooket.social.entity.mysql.SocialStandardGoodsName;
import com.mooket.social.entity.mysql.SysDict;
import com.mooket.social.mysql.mapper.ProductAliasDTO;
import com.mooket.social.mysql.mapper.SocialStandardGoodsNameDetailMapper;
import com.mooket.social.mysql.mapper.SocialStandardGoodsNameMapper;
import com.mooket.social.mysql.mapper.SysDictMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final JdbcTemplate pgJdbcTemplate;

    public ProductSyncService(SocialStandardGoodsNameMapper sourceMapper,
                              SocialStandardGoodsNameDetailMapper detailMapper,
                              SysDictMapper sysDictMapper,
                              @Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.sourceMapper = sourceMapper;
        this.detailMapper = detailMapper;
        this.sysDictMapper = sysDictMapper;
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    public int sync() {
        System.out.println("[ProductSyncService] 开始同步 dict_product...");

        List<SocialStandardGoodsName> sourceData = sourceMapper.selectAll();
        if (sourceData == null || sourceData.isEmpty()) {
            System.out.println("[ProductSyncService] 没有需要同步的产品数据");
            return 0;
        }

        System.out.println("[ProductSyncService] 待同步产品: " + sourceData.size() + " 条");

        Map<String, String> dictValueMap = batchGetDictValues();
        Map<String, String> aliasByName = batchGetAliases();

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

    private Map<String, String> batchGetDictValues() {
        List<SysDict> dicts = sysDictMapper.selectByDictNameEn("goods_category");
        return dicts.stream().collect(Collectors.toMap(SysDict::getDictKey, SysDict::getDictValue));
    }

    /**
     * 直接按产品名 JOIN 聚合别名，避免明细表 ID 对齐问题。
     */
    private Map<String, String> batchGetAliases() {
        List<ProductAliasDTO> aliasList = detailMapper.selectAliasJoinByProductName();

        System.out.println("[ProductSyncService] 别名 JOIN 记录数: " + aliasList.size());

        Map<String, String> aliasByName = aliasList.stream()
                .filter(dto -> dto.getProductName() != null && dto.getAliasName() != null)
                .collect(Collectors.groupingBy(
                        ProductAliasDTO::getProductName,
                        Collectors.mapping(ProductAliasDTO::getAliasName, Collectors.joining("、"))
                ));

        System.out.println("[ProductSyncService] 别名数据(按产品名): " + aliasByName.size() + " 个产品有别名");
        aliasByName.entrySet().stream().limit(3)
                .forEach(e -> System.out.println("  [别名示例] " + e.getKey() + " -> " + e.getValue()));
        return aliasByName;
    }

    /**
     * `dict_product` 维持去重后的产品字典，一品名一条；
     * `dict_product_source_map` 负责保留每个源 standard_goods_name_id 到 product_id 的映射。
     */
    private void upsert(SocialStandardGoodsName src,
                        Map<String, String> dictValueMap,
                        Map<String, String> aliasMap,
                        LocalDateTime now) {
        String productName = src.getStandardGoodsName();
        Long sourceGoodsId = src.getId();
        String category = src.getGoodsCategory() != null
                ? dictValueMap.get(src.getGoodsCategory().toString())
                : null;
        String aliasList = aliasMap.get(productName);

        Integer productId = pgJdbcTemplate.query(
                "SELECT product_id FROM dict_product WHERE category = ? AND product_name = ? LIMIT 1",
                ps -> {
                    ps.setString(1, category);
                    ps.setString(2, productName);
                },
                rs -> rs.next() ? rs.getInt("product_id") : null
        );

        if (productId == null) {
            pgJdbcTemplate.update(
                    "INSERT INTO dict_product (source_goods_id, category, product_name, alias_list, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)",
                    sourceGoodsId, category, productName, aliasList, now, now
            );
            productId = pgJdbcTemplate.queryForObject(
                    "SELECT product_id FROM dict_product WHERE category = ? AND product_name = ?",
                    Integer.class,
                    category, productName
            );
        } else {
            pgJdbcTemplate.update(
                    "UPDATE dict_product SET alias_list = ?, update_time = ?, source_goods_id = COALESCE(source_goods_id, ?) WHERE product_id = ?",
                    aliasList, now, sourceGoodsId, productId
            );
        }

        Timestamp nowTs = Timestamp.valueOf(now);
        pgJdbcTemplate.update(
                "INSERT INTO dict_product_source_map (source_goods_id, product_id, create_time, update_time) VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT (source_goods_id) DO UPDATE SET product_id = EXCLUDED.product_id, update_time = EXCLUDED.update_time",
                sourceGoodsId, productId, nowTs, nowTs
        );
    }
}
