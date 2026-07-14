-- ===== stat_country =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: stat_country_stat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stat_country_stat_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: stat_country; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stat_country (
    stat_id integer NOT NULL,
    stat_date date NOT NULL,
    category character varying(20) NOT NULL,
    country character varying(50) NOT NULL,
    today_offer_count integer DEFAULT 0,
    today_inquiry_count integer DEFAULT 0,
    today_factory_count integer DEFAULT 0,
    today_merchant_count integer DEFAULT 0,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    hot_factories text,
    hot_products text
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: stat_country_stat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stat_country_stat_id_seq OWNED BY public.stat_country.stat_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: stat_country stat_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_country ALTER COLUMN stat_id SET DEFAULT nextval('public.stat_country_stat_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: stat_country stat_country_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_country
    ADD CONSTRAINT stat_country_pkey PRIMARY KEY (stat_date, country, category);
--
-- Name: stat_country uk_stat_country_date_country_category; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_country
    ADD CONSTRAINT uk_stat_country_date_country_category UNIQUE (stat_date, country, category);

-- Indexes
--
-- Name: idx_stat_country_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_country_date ON public.stat_country USING btree (stat_date);
--
-- Name: idx_stat_country_offer_count; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_country_offer_count ON public.stat_country USING btree (stat_date, today_offer_count DESC);

-- Comments
--
-- Name: TABLE stat_country; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.stat_country IS '国家维度统计表';
--
-- Name: COLUMN stat_country.stat_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_country.stat_date IS '统计日期';
--
-- Name: COLUMN stat_country.category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_country.category IS '大类：牛/猪';
--
-- Name: COLUMN stat_country.country; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_country.country IS '国家';
--
-- Name: COLUMN stat_country.today_offer_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_country.today_offer_count IS '今日报盘数';
--
-- Name: COLUMN stat_country.today_inquiry_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_country.today_inquiry_count IS '今日求购数';
--
-- Name: COLUMN stat_country.today_factory_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_country.today_factory_count IS '今日活跃厂号数';
--
-- Name: COLUMN stat_country.today_merchant_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_country.today_merchant_count IS '今日报盘商家数';
