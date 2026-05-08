-- MySQL 建表语句
-- 数据库: mallee_muji_social

-- 1. 线上业务数据表（主表）
CREATE TABLE social_online_business (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    is_offer TINYINT COMMENT '0=求购 1=报盘',
    goods_category INT COMMENT '产品分类(1=牛 2=羊 3=猪 4=禽 5=水产)',
    goods_name VARCHAR(255),
    standard_goods_name_id BIGINT COMMENT '对应标准品名id',
    country INT COMMENT '原产国(关联sys_dict)',
    plant_no VARCHAR(100) COMMENT '厂号',
    business_category INT COMMENT '报盘类型/货物类型(关联sys_dict)',
    address_province VARCHAR(100) COMMENT '货物地-省份',
    address_province_id BIGINT COMMENT '货物地-省份id',
    address_city VARCHAR(100) COMMENT '货物地-城市',
    address_city_id BIGINT COMMENT '货物地-城市id',
    standard VARCHAR(100) COMMENT '规格/饲养方式',
    standard_two VARCHAR(100) COMMENT '牛肉规格/牛种',
    lean_ratio INT COMMENT '瘦肉率(关联sys_dict)',
    amount DECIMAL(12,2) COMMENT '报价',
    amount_unit INT COMMENT '报价单位(关联sys_dict)',
    weight DECIMAL(12,2) COMMENT '重量',
    weight_unit INT COMMENT '重量单位(关联sys_dict)',
    offer_date DATETIME COMMENT '报盘时间',
    user_name VARCHAR(100) COMMENT '用户名称',
    phone_no VARCHAR(50) COMMENT '电话号码',
    status INT COMMENT '状态(1=待上架 2=未上架 3=已上架)',
    memo VARCHAR(1000) COMMENT '备注',
    online_time DATETIME COMMENT '上架时间',
    display_flag TINYINT COMMENT '按需快搜是否展示',
    hash_code_a VARCHAR(255) COMMENT '大类+产品+国家厂号+饲养方式+商家hash',
    online_business_content_id BIGINT COMMENT '全网询报盘文本表id',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除(0=否 1=是)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 业务内容文本表
CREATE TABLE social_online_business_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT COMMENT '报盘原文内容'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 标准产品名表
CREATE TABLE social_standard_goods_name (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    standard_goods_name VARCHAR(255) COMMENT '标准品名',
    goods_category INT COMMENT '产品分类(关联sys_dict)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 标准产品名详情表（别名）
CREATE TABLE social_standard_goods_name_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    standard_goods_name_id BIGINT COMMENT '关联social_standard_goods_name.id',
    associated_goods_name VARCHAR(255) COMMENT '产品别名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 外部商家用户表
CREATE TABLE social_external_business_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mobile_no VARCHAR(50) COMMENT '手机号',
    nick_name VARCHAR(100) COMMENT '昵称',
    industry_group_id BIGINT COMMENT '行业协会ID(关联mallee_muji_uac.uac_industry_group.id)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 字典表
CREATE TABLE sys_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_key VARCHAR(100) COMMENT '字典键',
    dict_value VARCHAR(255) COMMENT '字典值',
    dict_name_en VARCHAR(100) COMMENT '字典英文名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 索引
CREATE INDEX idx_social_online_business_phone ON social_online_business(phone_no);
CREATE INDEX idx_social_online_business_status ON social_online_business(status);
CREATE INDEX idx_social_online_business_offer_date ON social_online_business(offer_date);
CREATE INDEX idx_social_online_business_goods_category ON social_online_business(goods_category);
CREATE INDEX idx_social_standard_goods_name_category ON social_standard_goods_name(goods_category);
CREATE INDEX idx_social_external_business_user_industry_group ON social_external_business_user(industry_group_id);
CREATE INDEX idx_sys_dict_name_en ON sys_dict(dict_name_en);
