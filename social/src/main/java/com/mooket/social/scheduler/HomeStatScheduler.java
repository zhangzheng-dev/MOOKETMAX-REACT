package com.mooket.social.scheduler;

import com.mooket.social.service.HomeStatService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 首页统计数据定时任务
 * 每5分钟执行一次，更新所有首页统计数据
 */
@Component
public class HomeStatScheduler {

    private final HomeStatService homeStatService;

    public HomeStatScheduler(HomeStatService homeStatService) {
        this.homeStatService = homeStatService;
    }

    /**
     * 每5分钟执行一次，定时更新首页统计数据
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void updateHomeStats() {
        System.out.println("[HomeStatScheduler] 开始执行首页统计定时任务...");
        try {
            homeStatService.computeAllStats();
            System.out.println("[HomeStatScheduler] 首页统计定时任务执行完成");
        } catch (Exception e) {
            System.err.println("[HomeStatScheduler] 首页统计定时任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
