package com.mooket.social.scheduler;

import com.mooket.social.service.PriceTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 价格趋势计算调度器
 * 每2分钟计算一次当天实时价格趋势
 */
@Component
public class PriceTrendScheduler {

    @Autowired
    private PriceTrendService priceTrendService;

    /**
     * 每2分钟执行一次价格趋势计算
     * 初始延迟30秒执行（等待系统启动完成）
     */
    @Scheduled(fixedRate = 120000, initialDelay = 30000) // 每2分钟 = 120000ms
    public void calculatePriceTrends() {
        try {
            System.out.println("[PriceTrendScheduler] Starting scheduled calculation at " + System.currentTimeMillis());
            priceTrendService.calculateAndSaveTodayTrends();
            System.out.println("[PriceTrendScheduler] Scheduled calculation completed");
        } catch (Exception e) {
            System.err.println("[PriceTrendScheduler] Error during calculation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 每天00:05执行，固化昨天的最终数据
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void backfillYesterdayTrends() {
        try {
            System.out.println("[PriceTrendScheduler] Starting yesterday backfill at " + System.currentTimeMillis());
            priceTrendService.backfillYesterday();
            System.out.println("[PriceTrendScheduler] Yesterday backfill completed");
        } catch (Exception e) {
            System.err.println("[PriceTrendScheduler] Error during yesterday backfill: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
