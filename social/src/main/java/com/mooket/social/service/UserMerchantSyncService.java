package com.mooket.social.service;

import com.mooket.social.entity.mysql.SocialExternalBusinessUser;
import com.mooket.social.mysql.mapper.SocialExternalBusinessUserMapper;
import com.mooket.social.mysql.mapper.UacIndustryGroupIdentityMapper;
import com.mooket.social.mysql.mapper.UacDictMapper;
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
 * 用户商家关联同步服务（优化版）
 * 从 MySQL social_external_business_user 同步到 PostgreSQL rel_user_merchant
 */
@Service
public class UserMerchantSyncService {

    private final SocialExternalBusinessUserMapper sourceMapper;
    private final UacIndustryGroupIdentityMapper uacIndustryGroupIdentityMapper;
    private final UacDictMapper uacDictMapper;
    private final JdbcTemplate pgJdbcTemplate;

    public UserMerchantSyncService(SocialExternalBusinessUserMapper sourceMapper,
                                  UacIndustryGroupIdentityMapper uacIndustryGroupIdentityMapper,
                                  UacDictMapper uacDictMapper,
                                  @Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.sourceMapper = sourceMapper;
        this.uacIndustryGroupIdentityMapper = uacIndustryGroupIdentityMapper;
        this.uacDictMapper = uacDictMapper;
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    /**
     * 执行同步（优化版：批量Upsert + 批量预加载identity）
     * @param lastSyncTime 上次同步时间，null则全量同步
     * @return 同步数量
     */
    @Transactional
    public int sync(LocalDateTime lastSyncTime) {
        System.out.println("[UserMerchantSyncService] 开始同步 rel_user_merchant，上次同步时间: " + lastSyncTime);

        // 1. 获取源数据
        List<SocialExternalBusinessUser> sourceData;
        if (lastSyncTime == null) {
            sourceData = sourceMapper.selectAll();
            System.out.println("[UserMerchantSyncService] 全量同步，获取 " + sourceData.size() + " 条数据");
        } else {
            sourceData = sourceMapper.selectActiveAfter(lastSyncTime);
            System.out.println("[UserMerchantSyncService] 增量同步，获取 " + sourceData.size() + " 条数据");
        }

        if (sourceData.isEmpty()) {
            return 0;
        }

        // 2. 批量预加载 identity 映射
        Map<Integer, String> identityMap = new HashMap<>();
        for (SocialExternalBusinessUser src : sourceData) {
            if (src.getIndustryGroupId() != null) {
                try {
                    String industryIdentity = uacIndustryGroupIdentityMapper.selectIndustryIdentityByGroupId(src.getIndustryGroupId().intValue());
                    if (industryIdentity != null) {
                        String identity = uacDictMapper.selectDictValueByNameEnAndKey("industry_group_industry_identity", industryIdentity);
                        identityMap.put(src.getIndustryGroupId().intValue(), identity);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        // 3. 批量 Upsert（使用 PostgreSQL ON CONFLICT）
        String sql = """
            INSERT INTO rel_user_merchant (user_id, mobile, nickname, identity, merchant_id, create_time, update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (user_id) DO UPDATE SET
                mobile = EXCLUDED.mobile,
                nickname = EXCLUDED.nickname,
                identity = EXCLUDED.identity,
                merchant_id = EXCLUDED.merchant_id,
                update_time = EXCLUDED.update_time
            """;

        int[] results = pgJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SocialExternalBusinessUser src = sourceData.get(i);
                Timestamp createTime = src.getCreatedTime() != null ? Timestamp.valueOf(src.getCreatedTime()) : Timestamp.valueOf(LocalDateTime.now());
                Timestamp updateTime = src.getUpdateTime() != null ? Timestamp.valueOf(src.getUpdateTime()) : Timestamp.valueOf(LocalDateTime.now());
                String identity = identityMap.get(src.getIndustryGroupId());

                ps.setLong(1, src.getId());
                ps.setString(2, src.getMobileNo());
                ps.setString(3, src.getNickName());
                ps.setString(4, identity);
                Long industryGroupId = src.getIndustryGroupId();
                if (industryGroupId != null) {
                    ps.setLong(5, industryGroupId);
                } else {
                    ps.setNull(5, java.sql.Types.BIGINT);
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
        System.out.println("[UserMerchantSyncService] 同步完成: 成功=" + successCount);
        return successCount;
    }
}
