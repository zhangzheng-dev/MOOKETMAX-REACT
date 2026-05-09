package com.mooket.social.service;

import com.mooket.social.entity.uac.UacIndustryGroup;
import com.mooket.social.uac.mapper.UacIndustryGroupMapper;
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
 * 商家字典同步服务（优化版）
 * 从 MySQL uac_industry_group 同步到 PostgreSQL dict_merchant
 */
@Service
public class MerchantSyncService {

    private final UacIndustryGroupMapper sourceMapper;
    private final JdbcTemplate pgJdbcTemplate;

    public MerchantSyncService(UacIndustryGroupMapper sourceMapper,
                             @Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.sourceMapper = sourceMapper;
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    /**
     * 执行同步（优化版：批量Upsert）
     * @return 同步数量
     */
    @Transactional
    public int sync() {
        System.out.println("[MerchantSyncService] 开始同步 dict_merchant...");

        // 1. 获取所有源数据
        List<UacIndustryGroup> sourceData = sourceMapper.selectAll();
        if (sourceData == null || sourceData.isEmpty()) {
            System.out.println("[MerchantSyncService] 没有需要同步的数据");
            return 0;
        }

        System.out.println("[MerchantSyncService] 待同步数据: " + sourceData.size() + " 条");

        // 2. 预加载所有 rel_user_merchant 的 merchant_id → mobile 映射
        List<MerchantPhone> phoneList = pgJdbcTemplate.query(
                "SELECT merchant_id, mobile FROM rel_user_merchant WHERE merchant_id IS NOT NULL",
                (rs, rowNum) -> new MerchantPhone(rs.getLong("merchant_id"), rs.getString("mobile"))
        );
        Map<Long, String> merchantIdToMobile = new HashMap<>();
        for (MerchantPhone mp : phoneList) {
            merchantIdToMobile.put(mp.merchantId, mp.mobile);
        }

        // 3. 批量 Upsert（使用 PostgreSQL ON CONFLICT）
        String sql = """
            INSERT INTO dict_merchant (merchant_id, merchant_name, merchant_short_name, merchant_tags, contact_phone, create_time, update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (merchant_id) DO UPDATE SET
                merchant_name = EXCLUDED.merchant_name,
                merchant_short_name = EXCLUDED.merchant_short_name,
                contact_phone = EXCLUDED.contact_phone,
                update_time = EXCLUDED.update_time
            """;

        int[] results = pgJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UacIndustryGroup src = sourceData.get(i);
                String contactPhone = merchantIdToMobile.get(src.getId());
                Timestamp createTime = src.getCreatedTime() != null ? Timestamp.valueOf(src.getCreatedTime()) : null;
                Timestamp updateTime = src.getUpdateTime() != null ? Timestamp.valueOf(src.getUpdateTime()) : Timestamp.valueOf(LocalDateTime.now());
                ps.setLong(1, src.getId());
                ps.setString(2, src.getIndustryGroupName());
                ps.setString(3, src.getIndustryGroupNameAbbreviation());
                ps.setNull(4, java.sql.Types.VARCHAR);
                if (contactPhone != null) {
                    ps.setString(5, contactPhone);
                } else {
                    ps.setNull(5, java.sql.Types.VARCHAR);
                }
                ps.setTimestamp(6, createTime);
                ps.setTimestamp(7, updateTime);
            }

            @Override
            public int getBatchSize() {
                return sourceData.size();
            }
        });

        int successCount = results.length;
        System.out.println("[MerchantSyncService] 同步完成: 成功=" + successCount);
        return successCount;
    }

    private static class MerchantPhone {
        Long merchantId;
        String mobile;
        MerchantPhone(Long merchantId, String mobile) {
            this.merchantId = merchantId;
            this.mobile = mobile;
        }
    }
}
