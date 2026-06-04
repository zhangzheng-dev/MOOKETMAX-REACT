package com.mooket.social.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * PostgreSQL table initializer for fresh environments.
 */
@Component
public class PostgreSqlInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public PostgreSqlInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[PostgreSqlInitializer] Initializing core tables...");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dict_product (
                    product_id SERIAL PRIMARY KEY,
                    source_goods_id BIGINT,
                    category VARCHAR(10) NOT NULL,
                    product_name VARCHAR(100) NOT NULL,
                    alias_list VARCHAR(500) DEFAULT '',
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_category_product UNIQUE (category, product_name)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dict_product_source_map (
                    source_goods_id BIGINT PRIMARY KEY,
                    product_id INT NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_dict_product_source_map_product_id ON dict_product_source_map(product_id)");

            System.out.println("[PostgreSqlInitializer] dict_product and dict_product_source_map ready");
        }
    }
}
