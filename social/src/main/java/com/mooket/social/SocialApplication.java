package com.mooket.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * 牧集 APP 后端服务启动类
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
public class SocialApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialApplication.class, args);
    }

    /**
     * PostgreSQL 主数据源 - 作为默认数据源
     */
    @Bean(name = "primaryDataSource")
    @Primary
    public DataSource primaryDataSource() {
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://43.139.56.124:30032/mooket_db?characterEncoding=UTF-8&useUnicode=true&connectTimeout=5000");
        config.setUsername("mooketmax_dba");
        config.setPassword("MooketMax@2024!");
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        return new com.zaxxer.hikari.HikariDataSource(config);
    }

    /**
     * 数据库初始化：stat_brand/stat_merchant/stat_factory_product 表添加 category 字段
     * 支持按牛/猪分类过滤（一次性迁移）
     */
    @Bean
    public org.springframework.boot.ApplicationRunner categoryMigrationRunner(DataSource dataSource) {
        return args -> {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            // 1. stat_brand 添加 category 字段
            try {
                jdbc.execute("ALTER TABLE stat_brand ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT '牛'");
                jdbc.execute("ALTER TABLE stat_brand DROP CONSTRAINT IF EXISTS pk_stat_brand");
                jdbc.execute("ALTER TABLE stat_brand ADD CONSTRAINT pk_stat_brand PRIMARY KEY (stat_date, brand_id, category)");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_brand_date");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_brand_offer_count");
                jdbc.execute("CREATE INDEX idx_stat_brand_date ON stat_brand(stat_date)");
                jdbc.execute("CREATE INDEX idx_stat_brand_offer_count ON stat_brand(stat_date, category, today_offer_count DESC)");
                System.out.println("[Migration] stat_brand.category added");
            } catch (Exception e) {
                System.out.println("[Migration] stat_brand.category: " + e.getMessage());
            }
            // 2. stat_merchant 添加 category 字段
            try {
                jdbc.execute("ALTER TABLE stat_merchant ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT '牛'");
                jdbc.execute("ALTER TABLE stat_merchant DROP CONSTRAINT IF EXISTS uk_stat_date_merchant");
                jdbc.execute("ALTER TABLE stat_merchant ADD CONSTRAINT uk_stat_date_merchant UNIQUE (stat_date, merchant_id, category)");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_merchant_date");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_merchant");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_date_merchant");
                jdbc.execute("CREATE INDEX idx_stat_merchant_date ON stat_merchant(stat_date)");
                jdbc.execute("CREATE INDEX idx_stat_merchant_merchant ON stat_merchant(merchant_id)");
                jdbc.execute("CREATE INDEX idx_stat_merchant_date_merchant ON stat_merchant(stat_date, merchant_id, category)");
                System.out.println("[Migration] stat_merchant.category added");
            } catch (Exception e) {
                System.out.println("[Migration] stat_merchant.category: " + e.getMessage());
            }
            // 3. stat_factory_product 添加 category 字段
            try {
                jdbc.execute("ALTER TABLE stat_factory_product ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT '牛'");
                jdbc.execute("ALTER TABLE stat_factory_product DROP CONSTRAINT IF EXISTS pk_stat_factory_product");
                jdbc.execute("ALTER TABLE stat_factory_product ADD CONSTRAINT pk_stat_factory_product PRIMARY KEY (stat_date, factory_id, product_id, category)");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_factory_product_date");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_factory_product_offer_count");
                jdbc.execute("CREATE INDEX idx_stat_factory_product_date ON stat_factory_product(stat_date)");
                jdbc.execute("CREATE INDEX idx_stat_factory_product_offer_count ON stat_factory_product(stat_date, category, today_offer_count DESC)");
                System.out.println("[Migration] stat_factory_product.category added");
            } catch (Exception e) {
                System.out.println("[Migration] stat_factory_product.category: " + e.getMessage());
            }
        };
    }
}
