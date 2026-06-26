-- 近30天价格趋势表
CREATE TABLE IF NOT EXISTS stat_price_trend (
    id BIGSERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,              -- 统计日期（历史日期或当天）
    dimension_type VARCHAR(50) NOT NULL,  -- 维度类型: country_product / country_factory_product
    country VARCHAR(100),                  -- 国家
    product_id INTEGER,                    -- 产品ID
    product_name VARCHAR(200),             -- 产品名称（冗余存储便于查询）
    factory_no VARCHAR(100),               -- 厂号（为空表示国家+产品维度）
    offer_type VARCHAR(20),                 -- 报盘/求购类型: 报盘 / 求购
    avg_price DECIMAL(10,2),              -- 当日均价
    record_date DATE NOT NULL,             -- 记录日期（系统当前日期）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(stat_date, dimension_type, country, product_id, factory_no, offer_type)
);

-- 查询索引（图表查询用，按维度+日期倒序）
CREATE INDEX IF NOT EXISTS idx_trend_query ON stat_price_trend(dimension_type, country, product_id, factory_no, offer_type, stat_date DESC);

-- 记录索引（UPSERT去重）
CREATE INDEX IF NOT EXISTS idx_trend_record ON stat_price_trend(dimension_type, country, product_id, factory_no, offer_type, record_date);

-- 维度类型枚举
COMMENT ON TABLE stat_price_trend IS '近30天价格趋势表';
COMMENT ON COLUMN stat_price_trend.stat_date IS '统计日期';
COMMENT ON COLUMN stat_price_trend.dimension_type IS '维度类型: country_product(国家+产品) / country_factory_product(国家+厂号+产品)';
COMMENT ON COLUMN stat_price_trend.country IS '国家';
COMMENT ON COLUMN stat_price_trend.product_id IS '产品ID';
COMMENT ON COLUMN stat_price_trend.product_name IS '产品名称';
COMMENT ON COLUMN stat_price_trend.factory_no IS '厂号';
COMMENT ON COLUMN stat_price_trend.offer_type IS '报盘/求购: 报盘 / 求购';
COMMENT ON COLUMN stat_price_trend.avg_price IS '当日均价';
COMMENT ON COLUMN stat_price_trend.record_date IS '记录日期';
