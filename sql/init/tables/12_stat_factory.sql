-- ===== stat_factory =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: stat_factory_stat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stat_factory_stat_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: stat_factory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stat_factory (
    stat_id integer NOT NULL,
    stat_date date NOT NULL,
    category character varying(20) NOT NULL,
    country character varying(50) NOT NULL,
    factory_no character varying(50) NOT NULL,
    factory_id integer NOT NULL,
    today_offer_count integer DEFAULT 0,
    today_inquiry_count integer DEFAULT 0,
    today_merchant_count integer DEFAULT 0,
    price_min numeric(10,2),
    price_max numeric(10,2),
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: stat_factory_stat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stat_factory_stat_id_seq OWNED BY public.stat_factory.stat_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: stat_factory stat_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_factory ALTER COLUMN stat_id SET DEFAULT nextval('public.stat_factory_stat_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: stat_factory pk_stat_factory; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_factory
    ADD CONSTRAINT pk_stat_factory PRIMARY KEY (stat_date, factory_id, category);

-- Indexes
--
-- Name: idx_stat_factory_offer_count; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_factory_offer_count ON public.stat_factory USING btree (stat_date, today_offer_count DESC);

-- Comments
--
-- Name: TABLE stat_factory; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.stat_factory IS '国家厂号维度统计表';
--
-- Name: COLUMN stat_factory.stat_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.stat_date IS '统计日期';
--
-- Name: COLUMN stat_factory.category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.category IS '大类：牛/猪';
--
-- Name: COLUMN stat_factory.country; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.country IS '国家';
--
-- Name: COLUMN stat_factory.factory_no; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.factory_no IS '厂号';
--
-- Name: COLUMN stat_factory.factory_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.factory_id IS '厂号ID';
--
-- Name: COLUMN stat_factory.today_offer_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.today_offer_count IS '今日报盘数';
--
-- Name: COLUMN stat_factory.today_inquiry_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.today_inquiry_count IS '今日求购数';
--
-- Name: COLUMN stat_factory.today_merchant_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.today_merchant_count IS '今日报盘商家数';
--
-- Name: COLUMN stat_factory.price_min; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.price_min IS '今日最低价';
--
-- Name: COLUMN stat_factory.price_max; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_factory.price_max IS '今日最高价';
