-- =============================================
-- 牧集 APP 数据库初始化主脚本
-- 执行顺序: 00_main.sql -> 按编号顺序执行其他脚本
-- =============================================

-- 创建数据库（如果不存在）
-- CREATE DATABASE mooket_db;

-- 设置默认字符集
SET client_encoding = 'UTF8';

-- 执行各表初始化脚本
\i 01_dict_product.sql
\i 02_dict_factory.sql
\i 03_dict_brand.sql
\i 04_dict_merchant.sql
\i 05_biz_offer.sql
\i 06_rel_user_merchant.sql
\i 07_stat_merchant.sql
\i 11_stat_product.sql
\i 12_stat_country.sql
\i 13_stat_factory.sql
\i 14_stat_brand.sql
\i 15_stat_country_product.sql
\i 16_stat_brand_product.sql
\i 17_stat_factory_product.sql
\i 18_biz_search_history.sql

-- 更新序列起始值（如果需要）
SELECT setval('dict_product_product_id_seq', (SELECT COALESCE(MAX(product_id), 0) FROM dict_product));
SELECT setval('dict_factory_factory_id_seq', (SELECT COALESCE(MAX(factory_id), 0) FROM dict_factory));
SELECT setval('dict_brand_brand_id_seq', (SELECT COALESCE(MAX(brand_id), 0) FROM dict_brand));
SELECT setval('dict_merchant_merchant_id_seq', (SELECT COALESCE(MAX(merchant_id), 0) FROM dict_merchant));
SELECT setval('biz_offer_offer_id_seq', (SELECT COALESCE(MAX(offer_id), 0) FROM biz_offer));

-- 创建自动清理过期数据的函数（保留最近2天数据）
CREATE OR REPLACE FUNCTION clean_expired_offers()
RETURNS void AS $$
BEGIN
    DELETE FROM biz_offer WHERE data_date < CURRENT_DATE - INTERVAL '2 days';
END;
$$ LANGUAGE plpgsql;

-- 创建更新商家统计的函数
CREATE OR REPLACE FUNCTION update_merchant_stat(merchant_id_param INT, stat_date_param DATE)
RETURNS void AS $$
BEGIN
    INSERT INTO stat_merchant (stat_date, merchant_id, today_offer_count, today_inquiry_count, today_product_count, today_factory_count, update_time)
    SELECT
        stat_date_param,
        merchant_id_param,
        COUNT(*) FILTER (WHERE offer_type = '报盘') as offer_count,
        COUNT(*) FILTER (WHERE offer_type = '求购') as inquiry_count,
        COUNT(DISTINCT product_id) FILTER (WHERE offer_type = '报盘') as product_count,
        COUNT(DISTINCT factory_id) FILTER (WHERE offer_type = '报盘' AND factory_id IS NOT NULL) as factory_count,
        CURRENT_TIMESTAMP
    FROM biz_offer
    WHERE merchant_id = merchant_id_param
      AND data_date = stat_date_param
      AND status = 'ACTIVE'
    GROUP BY merchant_id
    ON CONFLICT (stat_date, merchant_id) DO UPDATE SET
        today_offer_count = EXCLUDED.today_offer_count,
        today_inquiry_count = EXCLUDED.today_inquiry_count,
        today_product_count = EXCLUDED.today_product_count,
        today_factory_count = EXCLUDED.today_factory_count,
        update_time = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- 初始化所有商家的统计数据
DO $$
DECLARE
    m_id INT;
BEGIN
    FOR m_id IN SELECT DISTINCT merchant_id FROM biz_offer LOOP
        PERFORM update_merchant_stat(m_id, CURRENT_DATE);
    END LOOP;
END;
$$;

COMMENT ON FUNCTION clean_expired_offers() IS '清理过期报盘数据（保留最近2天）';
COMMENT ON FUNCTION update_merchant_stat(INT, DATE) IS '更新商家统计数据';
