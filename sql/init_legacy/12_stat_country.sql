-- =============================================
-- 国家维度统计表
-- =============================================

CREATE TABLE IF NOT EXISTS stat_country (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,                     -- 统计日期
    category VARCHAR(20) NOT NULL,               -- 大类：牛/猪
    country VARCHAR(50) NOT NULL,                -- 国家
    today_offer_count INT DEFAULT 0,             -- 今日报盘数
    today_inquiry_count INT DEFAULT 0,           -- 今日求购数
    today_factory_count INT DEFAULT 0,           -- 今日活跃厂号数
    today_merchant_count INT DEFAULT 0,         -- 今日报盘商家数
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 主键：日期+国家
ALTER TABLE stat_country ADD CONSTRAINT pk_stat_country PRIMARY KEY (stat_date, country);

-- 索引
CREATE INDEX idx_stat_country_date ON stat_country(stat_date);
CREATE INDEX idx_stat_country_offer_count ON stat_country(stat_date, today_offer_count DESC);

COMMENT ON TABLE stat_country IS '国家维度统计表';
COMMENT ON COLUMN stat_country.stat_date IS '统计日期';
COMMENT ON COLUMN stat_country.category IS '大类：牛/猪';
COMMENT ON COLUMN stat_country.country IS '国家';
COMMENT ON COLUMN stat_country.today_offer_count IS '今日报盘数';
COMMENT ON COLUMN stat_country.today_inquiry_count IS '今日求购数';
COMMENT ON COLUMN stat_country.today_factory_count IS '今日活跃厂号数';
COMMENT ON COLUMN stat_country.today_merchant_count IS '今日报盘商家数';
