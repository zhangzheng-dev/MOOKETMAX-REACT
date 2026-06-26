-- =============================================
-- 产品维度统计表
-- =============================================

CREATE TABLE IF NOT EXISTS stat_product (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,                    -- 统计日期
    category VARCHAR(20) NOT NULL,              -- 大类：牛/猪
    product_id INT NOT NULL,                     -- 产品ID
    product_name VARCHAR(100) NOT NULL,         -- 产品名称
    today_offer_count INT DEFAULT 0,             -- 今日报盘数
    today_inquiry_count INT DEFAULT 0,           -- 今日求购数
    today_merchant_count INT DEFAULT 0,          -- 今日报盘商家数
    today_factory_count INT DEFAULT 0,           -- 今日报盘工厂数
    price_min DECIMAL(10,2),                     -- 今日最低价
    price_max DECIMAL(10,2),                     -- 今日最高价
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 主键：日期+产品ID
ALTER TABLE stat_product ADD CONSTRAINT pk_stat_product PRIMARY KEY (stat_date, product_id);

-- 索引
CREATE INDEX idx_stat_product_date ON stat_product(stat_date);
CREATE INDEX idx_stat_product_offer_count ON stat_product(stat_date, today_offer_count DESC);

COMMENT ON TABLE stat_product IS '产品维度统计表';
COMMENT ON COLUMN stat_product.stat_date IS '统计日期';
COMMENT ON COLUMN stat_product.category IS '大类：牛/猪';
COMMENT ON COLUMN stat_product.product_id IS '产品ID';
COMMENT ON COLUMN stat_product.product_name IS '产品名称';
COMMENT ON COLUMN stat_product.today_offer_count IS '今日报盘数';
COMMENT ON COLUMN stat_product.today_inquiry_count IS '今日求购数';
COMMENT ON COLUMN stat_product.today_merchant_count IS '今日报盘商家数';
COMMENT ON COLUMN stat_product.today_factory_count IS '今日报盘工厂数';
COMMENT ON COLUMN stat_product.price_min IS '今日最低价';
COMMENT ON COLUMN stat_product.price_max IS '今日最高价';
