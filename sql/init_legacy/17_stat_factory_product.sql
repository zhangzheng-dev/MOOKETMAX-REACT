-- =============================================
-- 国家厂号产品维度统计表
-- =============================================

CREATE TABLE IF NOT EXISTS stat_factory_product (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,                     -- 统计日期
    factory_id INT NOT NULL,                     -- 厂号ID
    factory_no VARCHAR(50) NOT NULL,             -- 厂号
    country VARCHAR(50) NOT NULL,                 -- 国家
    product_id INT NOT NULL,                      -- 产品ID
    product_name VARCHAR(100) NOT NULL,          -- 产品名称
    today_offer_count INT DEFAULT 0,             -- 今日报盘数
    today_inquiry_count INT DEFAULT 0,           -- 今日求购数
    price_min DECIMAL(10,2),                     -- 今日最低价
    price_max DECIMAL(10,2),                     -- 今日最高价
    avg_price DECIMAL(10,2),                     -- 今日均价
    avg_price_yesterday DECIMAL(10,2),           -- 昨日均价
    price_change DECIMAL(10,2),                  -- 涨跌额
    price_change_rate DECIMAL(5,2),              -- 涨跌幅（%）
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 主键：日期+厂号ID+产品ID
ALTER TABLE stat_factory_product ADD CONSTRAINT pk_stat_factory_product PRIMARY KEY (stat_date, factory_id, product_id);

-- 索引
CREATE INDEX idx_stat_factory_product_date ON stat_factory_product(stat_date);
CREATE INDEX idx_stat_factory_product_offer_count ON stat_factory_product(stat_date, today_offer_count DESC);

COMMENT ON TABLE stat_factory_product IS '国家厂号产品维度统计表';
COMMENT ON COLUMN stat_factory_product.stat_date IS '统计日期';
COMMENT ON COLUMN stat_factory_product.factory_id IS '厂号ID';
COMMENT ON COLUMN stat_factory_product.factory_no IS '厂号';
COMMENT ON COLUMN stat_factory_product.country IS '国家';
COMMENT ON COLUMN stat_factory_product.product_id IS '产品ID';
COMMENT ON COLUMN stat_factory_product.product_name IS '产品名称';
COMMENT ON COLUMN stat_factory_product.today_offer_count IS '今日报盘数';
COMMENT ON COLUMN stat_factory_product.today_inquiry_count IS '今日求购数';
COMMENT ON COLUMN stat_factory_product.price_min IS '今日最低价';
COMMENT ON COLUMN stat_factory_product.price_max IS '今日最高价';
COMMENT ON COLUMN stat_factory_product.avg_price IS '今日均价';
COMMENT ON COLUMN stat_factory_product.avg_price_yesterday IS '昨日均价';
COMMENT ON COLUMN stat_factory_product.price_change IS '涨跌额';
COMMENT ON COLUMN stat_factory_product.price_change_rate IS '涨跌幅（%）';
