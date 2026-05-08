-- =============================================
-- 商家维度统计表
-- =============================================

DROP TABLE IF EXISTS stat_merchant CASCADE;

CREATE TABLE stat_merchant (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,
    merchant_id INT NOT NULL,
    today_offer_count INT DEFAULT 0,
    today_inquiry_count INT DEFAULT 0,
    today_product_count INT DEFAULT 0,
    today_factory_count INT DEFAULT 0,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_stat_date_merchant UNIQUE (stat_date, merchant_id)
);

-- 创建索引
CREATE INDEX idx_stat_date ON stat_merchant(stat_date);
CREATE INDEX idx_stat_merchant ON stat_merchant(merchant_id);
CREATE INDEX idx_stat_date_merchant ON stat_merchant(stat_date, merchant_id);

-- =============================================
-- 初始化商家统计数据
-- =============================================
INSERT INTO stat_merchant (stat_date, merchant_id, today_offer_count, today_inquiry_count, today_product_count, today_factory_count)
SELECT
    CURRENT_DATE,
    merchant_id,
    COUNT(*) FILTER (WHERE offer_type = '报盘'),
    COUNT(*) FILTER (WHERE offer_type = '求购'),
    COUNT(DISTINCT product_id) FILTER (WHERE offer_type = '报盘'),
    COUNT(DISTINCT factory_id) FILTER (WHERE offer_type = '报盘' AND factory_id IS NOT NULL)
FROM biz_offer
WHERE data_date >= CURRENT_DATE - INTERVAL '2 days'
  AND status = 'ACTIVE'
GROUP BY merchant_id;
