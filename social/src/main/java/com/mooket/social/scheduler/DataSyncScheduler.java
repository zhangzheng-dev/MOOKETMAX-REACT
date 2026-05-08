package com.mooket.social.scheduler;

import com.mooket.social.mapper.BizOfferMapper;
import com.mooket.social.service.BrandSyncService;
import com.mooket.social.service.DataSyncService;
import com.mooket.social.service.FactorySyncService;
import com.mooket.social.service.MerchantSyncService;
import com.mooket.social.service.ProductSyncService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MySQL 数据同步调度器
 * - dict_product: 手动触发，一次性同步
 * - dict_factory: 手动触发，一次性同步
 * - dict_merchant: 每天 8:00 / 14:00 / 20:00 同步
 * - biz_offer: 每5分钟增量同步
 */
@Component
public class DataSyncScheduler {

    private final DataSyncService dataSyncService;
    private final ProductSyncService productSyncService;
    private final FactorySyncService factorySyncService;
    private final BrandSyncService brandSyncService;
    private final MerchantSyncService merchantSyncService;
    private final BizOfferMapper bizOfferMapper;

    // 标记是否已执行过首次同步（避免重复执行）
    private volatile boolean initialSyncExecuted = false;

    // dict_factory 上次同步时间（用于增量同步）
    private volatile LocalDateTime lastFactorySyncTime = null;

    public DataSyncScheduler(DataSyncService dataSyncService,
                            ProductSyncService productSyncService,
                            FactorySyncService factorySyncService,
                            BrandSyncService brandSyncService,
                            MerchantSyncService merchantSyncService,
                            BizOfferMapper bizOfferMapper) {
        this.dataSyncService = dataSyncService;
        this.productSyncService = productSyncService;
        this.factorySyncService = factorySyncService;
        this.brandSyncService = brandSyncService;
        this.merchantSyncService = merchantSyncService;
        this.bizOfferMapper = bizOfferMapper;
    }

    /**
     * 应用启动后执行 biz_offer 首次同步（最近2天数据）
     * 异步执行，避免阻塞 HTTP 请求
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!initialSyncExecuted) {
            initialSyncExecuted = true;
            System.out.println("[DataSyncScheduler] 应用启动，异步执行 biz_offer 首次同步...");
            // 异步执行，不阻塞主线程
            new Thread(() -> {
                try {
                    dataSyncService.sync();
                } catch (Exception e) {
                    System.err.println("[DataSyncScheduler] biz_offer 首次同步失败: " + e.getMessage());
                }
            }).start();
        }
    }

    /**
     * 每5分钟执行一次增量同步（仅 biz_offer）
     * 异步执行，避免阻塞 HTTP 请求
     */
    @Scheduled(fixedRate = 300000)
    public void incrementalSync() {
        System.out.println("[DataSyncScheduler] 开始执行增量同步（异步）...");
        // 异步执行，不阻塞
        new Thread(() -> {
            try {
                int count = dataSyncService.sync();
                System.out.println("[DataSyncScheduler] 增量同步完成，同步了 " + count + " 条数据");
            } catch (Exception e) {
                System.err.println("[DataSyncScheduler] 增量同步失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 每天 8:00 / 14:00 / 20:00 执行 dict_factory → dict_brand → dict_merchant 串行同步
     * 依赖顺序：dict_factory（无依赖）→ dict_brand（依赖factory）→ dict_merchant（无依赖）
     */
    @Scheduled(cron = "0 0 8,14,20 * * ?")
    public void syncDictFactoryAndBrandAndMerchant() {
        // 1. dict_factory 全量同步
        System.out.println("[DataSyncScheduler] 开始执行 dict_factory 定时同步...");
        try {
            int factoryCount = factorySyncService.sync(null);
            System.out.println("[DataSyncScheduler] dict_factory 定时同步完成，共 " + factoryCount + " 条");
        } catch (Exception e) {
            System.err.println("[DataSyncScheduler] dict_factory 定时同步失败: " + e.getMessage());
            return; // factory 失败则不继续
        }

        // 2. dict_brand 同步（依赖 dict_factory 完成）
        System.out.println("[DataSyncScheduler] 开始执行 dict_brand 定时同步...");
        try {
            int brandCount = brandSyncService.sync();
            System.out.println("[DataSyncScheduler] dict_brand 定时同步完成，共 " + brandCount + " 条");
        } catch (Exception e) {
            System.err.println("[DataSyncScheduler] dict_brand 定时同步失败: " + e.getMessage());
        }

        // 3. dict_merchant 同步
        System.out.println("[DataSyncScheduler] 开始执行 dict_merchant 定时同步...");
        try {
            int merchantCount = merchantSyncService.sync();
            System.out.println("[DataSyncScheduler] dict_merchant 定时同步完成，共 " + merchantCount + " 条");
        } catch (Exception e) {
            System.err.println("[DataSyncScheduler] dict_merchant 定时同步失败: " + e.getMessage());
        }
    }

    /**
     * 每天 0:00 执行一次 biz_offer 旧数据清理
     * 删除 data_date < CURRENT_DATE - INTERVAL '1 day' 的数据（即前天0点之前的数据）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldBizOfferData() {
        System.out.println("[DataSyncScheduler] 开始执行 biz_offer 旧数据清理...");
        try {
            int deleted = bizOfferMapper.deleteOldData();
            System.out.println("[DataSyncScheduler] biz_offer 旧数据清理完成，删除了 " + deleted + " 条");
        } catch (Exception e) {
            System.err.println("[DataSyncScheduler] biz_offer 旧数据清理失败: " + e.getMessage());
        }
    }

    // ==================== 手动触发方法（用于测试） ====================

    /**
     * 手动触发 dict_product 同步（一次性）
     * 成功后取消下面的注释，下次启动会自动执行
     */
    public void syncDictProductManually() {
        System.out.println("[DataSyncScheduler] 手动触发 dict_product 同步...");
        try {
            int count = productSyncService.sync();
            System.out.println("[DataSyncScheduler] dict_product 同步完成，共 " + count + " 条");
        } catch (Exception e) {
            System.err.println("[DataSyncScheduler] dict_product 同步失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 手动触发 dict_factory 同步（一次性）
     * null表示全量同步
     */
    public void syncDictFactoryManually() {
        System.out.println("[DataSyncScheduler] 手动触发 dict_factory 同步...");
        try {
            int count = factorySyncService.sync(null); // 全量同步
            lastFactorySyncTime = LocalDateTime.now();
            System.out.println("[DataSyncScheduler] dict_factory 同步完成，共 " + count + " 条");
        } catch (Exception e) {
            System.err.println("[DataSyncScheduler] dict_factory 同步失败: " + e.getMessage());
            throw e;
        }
    }
}
