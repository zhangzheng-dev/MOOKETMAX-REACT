package com.mooket.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

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
    public DataSource primaryDataSource(
            @Value("${mooket.datasource.primary.jdbc-url}") String jdbcUrl,
            @Value("${mooket.datasource.primary.username}") String username,
            @Value("${mooket.datasource.primary.password}") String password) {
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        return new com.zaxxer.hikari.HikariDataSource(config);
    }

    /**
     * 数据库初始化：stat_brand/stat_merchant/stat_factory_product 表添加 category 字段
     * 支持按牛/猪分类过滤（一次性迁移，幂等执行）
     */
    @Bean
    public org.springframework.boot.ApplicationRunner categoryMigrationRunner(DataSource dataSource) {
        return args -> {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            migrateStatBrand(jdbc);
            migrateStatMerchant(jdbc);
            migrateStatFactoryProduct(jdbc);
            migrateDictProductSourceGoodsId(jdbc);
            migrateDictProductSourceMap(jdbc);
            migrateBizOfferSourceBusinessId(jdbc);
        };
    }

    private void migrateDictProductSourceGoodsId(JdbcTemplate jdbc) {
        try {
            boolean colExists = jdbc.queryForList(
                "SELECT 1 FROM information_schema.columns WHERE table_name='dict_product' AND column_name='source_goods_id'").size() > 0;
            if (!colExists) {
                jdbc.execute("ALTER TABLE dict_product ADD COLUMN source_goods_id BIGINT");
                System.out.println("[Migration] dict_product.source_goods_id added");
            } else {
                System.out.println("[Migration] dict_product.source_goods_id already exists, skipping");
            }
            try {
                jdbc.execute("DROP INDEX IF EXISTS uk_dict_product_source_goods_id");
                jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_dict_product_source_goods_id ON dict_product(source_goods_id) WHERE source_goods_id IS NOT NULL");
                jdbc.execute("CREATE INDEX IF NOT EXISTS idx_dict_product_source_goods_id ON dict_product(source_goods_id)");
            } catch (Exception e) {
                System.out.println("[Migration] dict_product source_goods_id indexes: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[Migration] dict_product source_goods_id FAILED: " + e.getMessage());
        }
    }

    private void migrateDictProductSourceMap(JdbcTemplate jdbc) {
        try {
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS dict_product_source_map (
                    source_goods_id BIGINT PRIMARY KEY,
                    product_id INT NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
            jdbc.execute("CREATE INDEX IF NOT EXISTS idx_dict_product_source_map_product_id ON dict_product_source_map(product_id)");
            jdbc.update("""
                INSERT INTO dict_product_source_map (source_goods_id, product_id, create_time, update_time)
                SELECT source_goods_id, product_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                FROM dict_product
                WHERE source_goods_id IS NOT NULL
                ON CONFLICT (source_goods_id) DO UPDATE
                SET product_id = EXCLUDED.product_id, update_time = EXCLUDED.update_time
                """);
            System.out.println("[Migration] dict_product_source_map ready");
        } catch (Exception e) {
            System.err.println("[Migration] dict_product_source_map FAILED: " + e.getMessage());
        }
    }

    private void migrateBizOfferSourceBusinessId(JdbcTemplate jdbc) {
        try {
            boolean colExists = jdbc.queryForList(
                "SELECT 1 FROM information_schema.columns WHERE table_name='biz_offer' AND column_name='source_business_id'").size() > 0;
            if (!colExists) {
                jdbc.execute("ALTER TABLE biz_offer ADD COLUMN source_business_id BIGINT");
                System.out.println("[Migration] biz_offer.source_business_id added");
            } else {
                System.out.println("[Migration] biz_offer.source_business_id already exists, skipping");
            }
            int deletedLegacyRows = jdbc.update("DELETE FROM biz_offer WHERE source_business_id IS NULL");
            if (deletedLegacyRows > 0) {
                System.out.println("[Migration] biz_offer legacy rows without source_business_id deleted: " + deletedLegacyRows);
            }
            try {
                jdbc.execute("ALTER TABLE biz_offer ALTER COLUMN product_id DROP NOT NULL");
                System.out.println("[Migration] biz_offer.product_id nullable");
            } catch (Exception e) {
                System.out.println("[Migration] biz_offer product_id nullable: " + e.getMessage());
            }
            try {
                jdbc.execute("DROP INDEX IF EXISTS biz_offer_unique_key");
                jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_biz_offer_source_business_id ON biz_offer(source_business_id)");
                jdbc.execute("CREATE INDEX IF NOT EXISTS idx_biz_offer_source_business_id ON biz_offer(source_business_id)");
            } catch (Exception e) {
                System.out.println("[Migration] biz_offer source index: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[Migration] biz_offer source_business_id FAILED: " + e.getMessage());
        }
    }

    private void migrateStatBrand(JdbcTemplate jdbc) {
        try {
            // 检查列是否存在
            boolean colExists = jdbc.queryForList(
                "SELECT 1 FROM information_schema.columns WHERE table_name='stat_brand' AND column_name='category'").size() > 0;
            if (!colExists) {
                jdbc.execute("ALTER TABLE stat_brand ADD COLUMN category VARCHAR(20)");
                jdbc.execute("UPDATE stat_brand SET category = '牛' WHERE category IS NULL");
                jdbc.execute("ALTER TABLE stat_brand ALTER COLUMN category SET NOT NULL");
                jdbc.execute("ALTER TABLE stat_brand ALTER COLUMN category SET DEFAULT '牛'");
                System.out.println("[Migration] stat_brand.category added (nullable -> NOT NULL)");
            } else {
                System.out.println("[Migration] stat_brand.category already exists, skipping");
            }
            // 更新主键和索引
            try {
                jdbc.execute("ALTER TABLE stat_brand DROP CONSTRAINT IF EXISTS pk_stat_brand");
                jdbc.execute("ALTER TABLE stat_brand ADD CONSTRAINT pk_stat_brand PRIMARY KEY (stat_date, brand_id, category)");
            } catch (Exception e) {
                System.out.println("[Migration] stat_brand PK: " + e.getMessage());
            }
            try {
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_brand_date");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_brand_offer_count");
                jdbc.execute("CREATE INDEX idx_stat_brand_date ON stat_brand(stat_date)");
                jdbc.execute("CREATE INDEX idx_stat_brand_offer_count ON stat_brand(stat_date, category, today_offer_count DESC)");
            } catch (Exception e) {
                System.out.println("[Migration] stat_brand indexes: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[Migration] stat_brand FAILED: " + e.getMessage());
        }
    }

    private void migrateStatMerchant(JdbcTemplate jdbc) {
        try {
            boolean colExists = jdbc.queryForList(
                "SELECT 1 FROM information_schema.columns WHERE table_name='stat_merchant' AND column_name='category'").size() > 0;
            if (!colExists) {
                jdbc.execute("ALTER TABLE stat_merchant ADD COLUMN category VARCHAR(20)");
                jdbc.execute("UPDATE stat_merchant SET category = '牛' WHERE category IS NULL");
                jdbc.execute("ALTER TABLE stat_merchant ALTER COLUMN category SET NOT NULL");
                jdbc.execute("ALTER TABLE stat_merchant ALTER COLUMN category SET DEFAULT '牛'");
                System.out.println("[Migration] stat_merchant.category added (nullable -> NOT NULL)");
            } else {
                System.out.println("[Migration] stat_merchant.category already exists, skipping");
            }
            try {
                jdbc.execute("ALTER TABLE stat_merchant DROP CONSTRAINT IF EXISTS uk_stat_date_merchant");
                jdbc.execute("ALTER TABLE stat_merchant ADD CONSTRAINT uk_stat_date_merchant UNIQUE (stat_date, merchant_id, category)");
            } catch (Exception e) {
                System.out.println("[Migration] stat_merchant UK: " + e.getMessage());
            }
            try {
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_merchant_date");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_merchant");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_date_merchant");
                jdbc.execute("CREATE INDEX idx_stat_merchant_date ON stat_merchant(stat_date)");
                jdbc.execute("CREATE INDEX idx_stat_merchant_merchant ON stat_merchant(merchant_id)");
                jdbc.execute("CREATE INDEX idx_stat_merchant_date_merchant ON stat_merchant(stat_date, merchant_id, category)");
            } catch (Exception e) {
                System.out.println("[Migration] stat_merchant indexes: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[Migration] stat_merchant FAILED: " + e.getMessage());
        }
    }

    private void migrateStatFactoryProduct(JdbcTemplate jdbc) {
        try {
            boolean colExists = jdbc.queryForList(
                "SELECT 1 FROM information_schema.columns WHERE table_name='stat_factory_product' AND column_name='category'").size() > 0;
            if (!colExists) {
                jdbc.execute("ALTER TABLE stat_factory_product ADD COLUMN category VARCHAR(20)");
                jdbc.execute("UPDATE stat_factory_product SET category = '牛' WHERE category IS NULL");
                jdbc.execute("ALTER TABLE stat_factory_product ALTER COLUMN category SET NOT NULL");
                jdbc.execute("ALTER TABLE stat_factory_product ALTER COLUMN category SET DEFAULT '牛'");
                System.out.println("[Migration] stat_factory_product.category added (nullable -> NOT NULL)");
            } else {
                System.out.println("[Migration] stat_factory_product.category already exists, skipping");
            }
            try {
                jdbc.execute("ALTER TABLE stat_factory_product DROP CONSTRAINT IF EXISTS pk_stat_factory_product");
                jdbc.execute("ALTER TABLE stat_factory_product ADD CONSTRAINT pk_stat_factory_product PRIMARY KEY (stat_date, factory_id, product_id, category)");
            } catch (Exception e) {
                System.out.println("[Migration] stat_factory_product PK: " + e.getMessage());
            }
            try {
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_factory_product_date");
                jdbc.execute("DROP INDEX IF EXISTS idx_stat_factory_product_offer_count");
                jdbc.execute("CREATE INDEX idx_stat_factory_product_date ON stat_factory_product(stat_date)");
                jdbc.execute("CREATE INDEX idx_stat_factory_product_offer_count ON stat_factory_product(stat_date, category, today_offer_count DESC)");
            } catch (Exception e) {
                System.out.println("[Migration] stat_factory_product indexes: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[Migration] stat_factory_product FAILED: " + e.getMessage());
        }
    }
}
