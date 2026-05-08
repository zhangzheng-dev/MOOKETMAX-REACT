package com.mooket.social.service;

import com.mooket.social.entity.erp.ErpBaseApproval;
import com.mooket.social.erp.mapper.ErpBaseApprovalMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 厂号字典同步服务（优化版）
 * 从 MySQL erp_base_approval 同步到 PostgreSQL dict_factory
 */
@Service
public class FactorySyncService {

    private final ErpBaseApprovalMapper sourceMapper;
    private final JdbcTemplate pgJdbcTemplate; // PostgreSQL JdbcTemplate

    // 国家编码→名称映射（静态表，支持所有国家包括哥伦比亚等）
    private static final Map<String, String> COUNTRY_NAME_MAP = new HashMap<>();
    static {
        COUNTRY_NAME_MAP.put("1", "阿根廷");     COUNTRY_NAME_MAP.put("2", "澳大利亚");
        COUNTRY_NAME_MAP.put("3", "白罗斯");     COUNTRY_NAME_MAP.put("4", "巴西");
        COUNTRY_NAME_MAP.put("5", "加拿大");     COUNTRY_NAME_MAP.put("6", "中国");
        COUNTRY_NAME_MAP.put("7", "智利");       COUNTRY_NAME_MAP.put("8", "哥斯达黎加");
        COUNTRY_NAME_MAP.put("9", "法国");       COUNTRY_NAME_MAP.put("10", "匈牙利");
        COUNTRY_NAME_MAP.put("11", "爱尔兰");    COUNTRY_NAME_MAP.put("12", "墨西哥");
        COUNTRY_NAME_MAP.put("13", "蒙古");      COUNTRY_NAME_MAP.put("14", "纳米比亚");
        COUNTRY_NAME_MAP.put("15", "新西兰");    COUNTRY_NAME_MAP.put("16", "南非");
        COUNTRY_NAME_MAP.put("17", "塞尔维亚");  COUNTRY_NAME_MAP.put("18", "乌拉圭");
        COUNTRY_NAME_MAP.put("19", "美国");     COUNTRY_NAME_MAP.put("20", "哈萨克斯坦");
        COUNTRY_NAME_MAP.put("21", "奥地利");    COUNTRY_NAME_MAP.put("22", "比利时");
        COUNTRY_NAME_MAP.put("23", "丹麦");     COUNTRY_NAME_MAP.put("24", "英国");
        COUNTRY_NAME_MAP.put("25", "芬兰");      COUNTRY_NAME_MAP.put("26", "德国");
        COUNTRY_NAME_MAP.put("27", "意大利");    COUNTRY_NAME_MAP.put("28", "荷兰");
        COUNTRY_NAME_MAP.put("29", "波兰");      COUNTRY_NAME_MAP.put("30", "罗马尼亚");
        COUNTRY_NAME_MAP.put("31", "西班牙");    COUNTRY_NAME_MAP.put("32", "韩国");
        COUNTRY_NAME_MAP.put("33", "泰国");      COUNTRY_NAME_MAP.put("34", "俄罗斯");
        COUNTRY_NAME_MAP.put("35", "玻利维亚");  COUNTRY_NAME_MAP.put("36", "立陶宛");
        COUNTRY_NAME_MAP.put("37", "乌克兰");    COUNTRY_NAME_MAP.put("38", "巴拿马");
        COUNTRY_NAME_MAP.put("39", "葡萄牙");    COUNTRY_NAME_MAP.put("40", "拉脱维亚");
        COUNTRY_NAME_MAP.put("41", "冰岛");     COUNTRY_NAME_MAP.put("42", "瑞士");
        COUNTRY_NAME_MAP.put("43", "新加坡");    COUNTRY_NAME_MAP.put("44", "日本");
        COUNTRY_NAME_MAP.put("45", "土耳其");    COUNTRY_NAME_MAP.put("46", "秘鲁");
        COUNTRY_NAME_MAP.put("47", "挪威");     COUNTRY_NAME_MAP.put("48", "格陵兰岛");
        COUNTRY_NAME_MAP.put("49", "哥伦比亚");  COUNTRY_NAME_MAP.put("50", "巴拉圭");
        COUNTRY_NAME_MAP.put("51", "哥伦比亚");  COUNTRY_NAME_MAP.put("52", "危地马拉");
    }

    public FactorySyncService(ErpBaseApprovalMapper sourceMapper,
                             @Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.sourceMapper = sourceMapper;
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    private Map<String, String> getCountryNameMap() {
        return COUNTRY_NAME_MAP;
    }

    /**
     * 执行增量同步（优化版：批量Upsert）
     * @param lastSyncTime 上次同步时间，null则全量同步
     * @return 同步数量
     */
    @Transactional
    public int sync(LocalDateTime lastSyncTime) {
        System.out.println("[FactorySyncService] 开始同步 dict_factory，上次同步时间: " + lastSyncTime);

        // 1. 获取源数据
        List<ErpBaseApproval> sourceData;
        if (lastSyncTime == null) {
            sourceData = sourceMapper.selectAllActive();
            System.out.println("[FactorySyncService] 全量同步，获取 " + sourceData.size() + " 条数据");
        } else {
            sourceData = sourceMapper.selectActiveAfter(lastSyncTime);
            System.out.println("[FactorySyncService] 增量同步，获取 " + sourceData.size() + " 条数据");
        }

        if (sourceData.isEmpty()) {
            return 0;
        }

        // 预加载国家映射（从 ggoods_country 字典，支持哥伦比亚等所有国家）
        Map<String, String> countryMap = getCountryNameMap();

        // 2. 批量 Upsert（使用 PostgreSQL ON CONFLICT）
        // 注意：唯一约束为 (category, country, factory_no)，与 INSERT 列顺序一致
        String sql = """
            INSERT INTO dict_factory (category, country, country_alias, factory_no, brand_id, create_time, update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (category, country, factory_no) DO UPDATE SET
                category = EXCLUDED.category,
                country_alias = EXCLUDED.country_alias,
                brand_id = EXCLUDED.brand_id,
                update_time = EXCLUDED.update_time
            """;

        int[] results = pgJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ErpBaseApproval src = sourceData.get(i);
                String category = convertCategory(src.getCategory());
                String country = getCountry(String.valueOf(src.getCountry()), countryMap);
                String countryAlias = getCountryAlias(country);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                ps.setString(1, category);
                ps.setString(2, country);
                ps.setString(3, countryAlias);
                ps.setString(4, src.getPlantNo());
                ps.setNull(5, java.sql.Types.INTEGER);
                ps.setTimestamp(6, now);
                ps.setTimestamp(7, now);
            }

            @Override
            public int getBatchSize() {
                return sourceData.size();
            }
        });

        int successCount = results.length;
        System.out.println("[FactorySyncService] 同步完成: 成功=" + successCount);
        return successCount;
    }

    private String getCountry(String countryCode, Map<String, String> countryMap) {
        if (countryCode == null) return null;
        // 优先从字典映射（ggoods_country 支持所有国家，包括哥伦比亚等）
        String name = countryMap.get(countryCode);
        if (name != null) {
            return name;
        }
        // 兜底：尝试直接返回（某些数据可能直接存国家名称）
        return countryCode;
    }

    private String convertCategory(Integer category) {
        if (category == null) return null;
        return switch (category) {
            case 1 -> "牛";
            case 2 -> "羊";
            case 3 -> "猪";
            case 4 -> "禽";
            case 5 -> "水产";
            default -> null;
        };
    }

    private String getCountryAlias(String country) {
        if (country == null) return null;
        return switch (country) {
            case "白罗斯" -> "白俄罗斯，白俄";
            case "哥斯达黎加" -> "哥";
            case "玻利维亚" -> "玻";
            case "澳大利亚" -> "澳洲";
            default -> null;
        };
    }
}
