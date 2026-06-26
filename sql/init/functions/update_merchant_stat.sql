-- ===== Function: update_merchant_stat =====

--
-- Name: update_merchant_stat(integer, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_merchant_stat(merchant_id_param integer, stat_date_param date) RETURNS void
    LANGUAGE plpgsql
    AS $$
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
$$;
--
-- Name: FUNCTION update_merchant_stat(merchant_id_param integer, stat_date_param date); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON FUNCTION public.update_merchant_stat(merchant_id_param integer, stat_date_param date) IS '更新商家统计数据';
