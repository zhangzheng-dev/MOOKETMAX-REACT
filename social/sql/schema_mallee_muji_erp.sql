-- MySQL 建表语句
-- 数据库: mallee_muji_erp

-- 1. ERP基础审批表（厂号）
CREATE TABLE erp_base_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category INT COMMENT '类别(1=牛 2=羊 3=猪 4=禽 5=水产)',
    country VARCHAR(100) COMMENT '国家',
    plant_no VARCHAR(100) COMMENT '厂号',
    plant_status INT COMMENT '厂号状态',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. ERP字典表
CREATE TABLE erp_sys_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_key VARCHAR(100) COMMENT '字典键',
    dict_value VARCHAR(255) COMMENT '字典值',
    dict_name_en VARCHAR(100) COMMENT '字典英文名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 索引
CREATE INDEX idx_erp_base_approval_country ON erp_base_approval(country);
CREATE INDEX idx_erp_base_approval_plant_no ON erp_base_approval(plant_no);
CREATE INDEX idx_erp_base_approval_category ON erp_base_approval(category);
