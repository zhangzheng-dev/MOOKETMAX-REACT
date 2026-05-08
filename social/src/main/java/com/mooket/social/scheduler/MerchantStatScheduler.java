package com.mooket.social.scheduler;

import com.mooket.social.entity.StatMerchant;
import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.mapper.StatMerchantMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商家统计定时任务（优化版）
 * 每5分钟执行一次，使用批量SQL聚合计算所有商家统计
 */
@Component
public class MerchantStatScheduler {

    private final BizOfferMapper offerMapper;
    private final StatMerchantMapper statMapper;

    public MerchantStatScheduler(BizOfferMapper offerMapper,
                                StatMerchantMapper statMapper) {
        this.offerMapper = offerMapper;
        this.statMapper = statMapper;
    }

    /**
     * 每5分钟执行一次，定时更新商家统计数据（优化版）
     * 优化：使用单次SQL聚合 + 批量Upsert，将16000+次查询减少到2次
     */
    @Scheduled(fixedRate = 300000)
    public void updateMerchantStats() {
        System.out.println("[MerchantStatScheduler] 开始执行商家统计定时任务...");

        LocalDate today = LocalDate.now();

        // 1. 单次SQL查询，获取所有商家近2日的聚合统计
        List<BizOfferMapper.MerchantStatAgg> aggList = offerMapper.selectMerchantStatsAgg();
        System.out.println("[MerchantStatScheduler] 查询到 " + aggList.size() + " 个有报盘的商家");

        if (!aggList.isEmpty()) {
            // 2. 批量构建StatMerchant实体
            LocalDateTime now = LocalDateTime.now();
            List<StatMerchant> stats = new ArrayList<>(aggList.size());

            for (BizOfferMapper.MerchantStatAgg agg : aggList) {
                StatMerchant stat = new StatMerchant();
                stat.setStatDate(today);
                stat.setMerchantId(agg.merchantId);
                stat.setTodayOfferCount(agg.offerCount != null ? agg.offerCount : 0);
                stat.setTodayInquiryCount(agg.inquiryCount != null ? agg.inquiryCount : 0);
                stat.setTodayProductCount(agg.productCount != null ? agg.productCount : 0);
                stat.setTodayFactoryCount(agg.factoryCount != null ? agg.factoryCount : 0);
                stat.setUpdateTime(now);
                stats.add(stat);
            }

            // 3. 批量Upsert（单次SQL完成所有插入/更新）
            statMapper.batchUpsert(stats);
            System.out.println("[MerchantStatScheduler] 批量更新 " + stats.size() + " 个商家统计完成");
        }

        System.out.println("[MerchantStatScheduler] 商家统计定时任务执行完成，共处理 " + aggList.size() + " 个商家");
    }
}
