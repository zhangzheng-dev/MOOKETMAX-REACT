package com.mooket.social.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * PostgreSQL 表初始化器 - 简化版
 */
@Component
public class PostgreSqlInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public PostgreSqlInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[PostgreSqlInitializer] 开始创建表...");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 创建 dict_product 表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dict_product (
                    product_id SERIAL PRIMARY KEY,
                    category VARCHAR(10) NOT NULL,
                    product_name VARCHAR(100) NOT NULL,
                    alias_list VARCHAR(500) DEFAULT '',
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_category_product UNIQUE (category, product_name)
                )
                """);

            System.out.println("[PostgreSqlInitializer] dict_product 表创建成功！");
        }
    }
}
