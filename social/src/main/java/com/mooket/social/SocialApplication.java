package com.mooket.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
}
