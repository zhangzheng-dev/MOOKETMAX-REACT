-- =============================================
-- 品牌维度统计表
-- =============================================

CREATE TABLE IF NOT EXISTS stat_brand (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,                     -- 统计日期
    brand_id INT NOT NULL,                        -- 品牌ID
    brand_name VARCHAR(100) NOT NULL,            -- 品牌名称
    today_offer_count INT DEFAULT 0,             -- 今日报盘数
    today_factory_count INT DEFAULT 0,           -- 今日报盘工厂数
    today_product_count INT DEFAULT 0,           -- 今日报盘产品种类数
    price_min DECIMAL(10,2),                     -- 今日最低价
    price_max DECIMAL(10,2),                     -- 今日最高价
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 主键：日期+品牌ID
ALTER TABLE stat_brand ADD CONSTRAINT pk_stat_brand PRIMARY KEY (stat_date, brand_id);

-- 索引
CREATE INDEX idx_stat_brand_date ON stat_brand(stat_date);
CREATE INDEX idx_stat_brand_offer_count ON stat_brand(stat_date, today_offer_count DESC);

COMMENT ON TABLE stat_brand IS '品牌维度统计表';
COMMENT ON COLUMN stat_brand.stat_date IS '统计日期';
COMMENT ON COLUMN stat_brand.brand_id IS '品牌ID';
COMMENT ON COLUMN stat_brand.brand_name IS '品牌名称';
COMMENT ON COLUMN stat_brand.today_offer_count IS '今日报盘数';
COMMENT ON COLUMN stat_brand.today_factory_count IS '今日报盘工厂数';
COMMENT ON COLUMN stat_brand.today_product_count IS '今日报盘产品种类数';
COMMENT ON COLUMN stat_brand.price_min IS '今日最低价';
COMMENT ON COLUMN stat_brand.price_max IS '今日最高价';
