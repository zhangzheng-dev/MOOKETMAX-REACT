-- =============================================
-- 品牌产品维度统计表
-- =============================================

CREATE TABLE IF NOT EXISTS stat_brand_product (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,                     -- 统计日期
    brand_id INT NOT NULL,                        -- 品牌ID
    brand_name VARCHAR(100) NOT NULL,            -- 品牌名称
    product_id INT NOT NULL,                      -- 产品ID
    product_name VARCHAR(100) NOT NULL,          -- 产品名称
    today_factory_count INT DEFAULT 0,           -- 今日报盘工厂数
    today_offer_count INT DEFAULT 0,             -- 今日报盘数
    price_min DECIMAL(10,2),                     -- 今日最低价
    price_max DECIMAL(10,2),                     -- 今日最高价
    avg_price DECIMAL(10,2),                     -- 今日均价
    avg_price_yesterday DECIMAL(10,2),           -- 昨日均价
    price_change DECIMAL(10,2),                  -- 涨跌额
    price_change_rate DECIMAL(5,2),              -- 涨跌幅（%）
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 主键：日期+品牌ID+产品ID
ALTER TABLE stat_brand_product ADD CONSTRAINT pk_stat_brand_product PRIMARY KEY (stat_date, brand_id, product_id);

-- 索引
CREATE INDEX idx_stat_brand_product_date ON stat_brand_product(stat_date);
CREATE INDEX idx_stat_brand_product_offer_count ON stat_brand_product(stat_date, today_offer_count DESC);

COMMENT ON TABLE stat_brand_product IS '品牌产品维度统计表';
COMMENT ON COLUMN stat_brand_product.stat_date IS '统计日期';
COMMENT ON COLUMN stat_brand_product.brand_id IS '品牌ID';
COMMENT ON COLUMN stat_brand_product.brand_name IS '品牌名称';
COMMENT ON COLUMN stat_brand_product.product_id IS '产品ID';
COMMENT ON COLUMN stat_brand_product.product_name IS '产品名称';
COMMENT ON COLUMN stat_brand_product.today_factory_count IS '今日报盘工厂数';
COMMENT ON COLUMN stat_brand_product.today_offer_count IS '今日报盘数';
COMMENT ON COLUMN stat_brand_product.price_min IS '今日最低价';
COMMENT ON COLUMN stat_brand_product.price_max IS '今日最高价';
COMMENT ON COLUMN stat_brand_product.avg_price IS '今日均价';
COMMENT ON COLUMN stat_brand_product.avg_price_yesterday IS '昨日均价';
COMMENT ON COLUMN stat_brand_product.price_change IS '涨跌额';
COMMENT ON COLUMN stat_brand_product.price_change_rate IS '涨跌幅（%）';
