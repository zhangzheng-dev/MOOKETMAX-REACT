-- PostgreSQL 数据库建表语句
-- 数据库: mooket_db

-- 1. 品牌字典表
CREATE TABLE dict_brand (
    brand_id SERIAL PRIMARY KEY,
    brand_name VARCHAR(255) NOT NULL,
    alias_list VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 产品字典表
CREATE TABLE dict_product (
    product_id SERIAL PRIMARY KEY,
    category VARCHAR(50),
    product_name VARCHAR(255) NOT NULL,
    alias_list VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 商家字典表
CREATE TABLE dict_merchant (
    merchant_id BIGSERIAL PRIMARY KEY,
    merchant_name VARCHAR(255),
    merchant_short_name VARCHAR(255),
    merchant_tags VARCHAR(500),
    contact_phone VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 厂号字典表
CREATE TABLE dict_factory (
    factory_id SERIAL PRIMARY KEY,
    category VARCHAR(50),
    country VARCHAR(100),
    country_alias VARCHAR(200),
    factory_no VARCHAR(100),
    brand_id INTEGER REFERENCES dict_brand(brand_id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 报盘/求购表
CREATE TABLE biz_offer (
    offer_id BIGSERIAL PRIMARY KEY,
    offer_original_text TEXT,
    category VARCHAR(50),
    product_id INTEGER,
    product_name VARCHAR(255),
    country VARCHAR(100),
    factory_no VARCHAR(100),
    factory_id INTEGER REFERENCES dict_factory(factory_id),
    brand_id INTEGER REFERENCES dict_brand(brand_id),
    merchant_id BIGINT,
    contact_phone VARCHAR(50),
    user_id BIGINT,
    user_nickname VARCHAR(100),
    price DECIMAL(12,2),
    price_max DECIMAL(12,2),
    weight VARCHAR(100),
    offer_type VARCHAR(20),
    goods_type VARCHAR(50),
    goods_location VARCHAR(255),
    tags VARCHAR(500),
    fat_ratio VARCHAR(50),
    feeding_type VARCHAR(50),
    cattle_breed VARCHAR(100),
    remark VARCHAR(1000),
    publish_time TIMESTAMP,
    data_date DATE,
    status VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 商家统计表
CREATE TABLE stat_merchant (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,
    merchant_id BIGINT,
    today_offer_count INTEGER DEFAULT 0,
    today_inquiry_count INTEGER DEFAULT 0,
    today_product_count INTEGER DEFAULT 0,
    today_factory_count INTEGER DEFAULT 0,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 用户商家关联表
CREATE TABLE rel_user_merchant (
    user_id BIGSERIAL PRIMARY KEY,
    mobile VARCHAR(50),
    nickname VARCHAR(100),
    identity VARCHAR(100),
    merchant_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_biz_offer_merchant_id ON biz_offer(merchant_id);
CREATE INDEX idx_biz_offer_product_id ON biz_offer(product_id);
CREATE INDEX idx_biz_offer_factory_id ON biz_offer(factory_id);
CREATE INDEX idx_biz_offer_data_date ON biz_offer(data_date);
CREATE INDEX idx_biz_offer_status ON biz_offer(status);
CREATE INDEX idx_stat_merchant_merchant_id ON stat_merchant(merchant_id);
CREATE INDEX idx_stat_merchant_stat_date ON stat_merchant(stat_date);
CREATE INDEX idx_dict_factory_country ON dict_factory(country);
CREATE INDEX idx_dict_factory_factory_no ON dict_factory(factory_no);
