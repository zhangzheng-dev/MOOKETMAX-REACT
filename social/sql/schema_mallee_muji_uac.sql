-- MySQL 建表语句
-- 数据库: mallee_muji_uac

-- 1. 行业协会组表
CREATE TABLE uac_industry_group (
    id BIGINT PRIMARY KEY COMMENT '行业协会ID',
    industry_group_name VARCHAR(255) COMMENT '行业协会名称',
    industry_group_name_abbreviation VARCHAR(100) COMMENT '行业协会简称',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 行业协会身份表（跨库关联表）
CREATE TABLE uac_industry_group_identity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    industry_group_id BIGINT COMMENT '行业协会ID',
    industry_identity VARCHAR(100) COMMENT '行业身份标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
