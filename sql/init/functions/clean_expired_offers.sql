-- ===== Function: clean_expired_offers =====

--
-- Name: clean_expired_offers(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.clean_expired_offers() RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM biz_offer WHERE data_date < CURRENT_DATE - INTERVAL '2 days';
END;
$$;
--
-- Name: FUNCTION clean_expired_offers(); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON FUNCTION public.clean_expired_offers() IS '清理过期报盘数据（保留最近2天）';
