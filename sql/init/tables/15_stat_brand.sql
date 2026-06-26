-- ===== stat_brand =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: stat_brand_stat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stat_brand_stat_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: stat_brand; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stat_brand (
    stat_id integer NOT NULL,
    stat_date date NOT NULL,
    brand_id integer NOT NULL,
    brand_name character varying(100) NOT NULL,
    today_offer_count integer DEFAULT 0,
    today_factory_count integer DEFAULT 0,
    today_product_count integer DEFAULT 0,
    price_min numeric(10,2),
    price_max numeric(10,2),
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    category character varying(20) DEFAULT '牛'::character varying NOT NULL
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: stat_brand_stat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stat_brand_stat_id_seq OWNED BY public.stat_brand.stat_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: stat_brand stat_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_brand ALTER COLUMN stat_id SET DEFAULT nextval('public.stat_brand_stat_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: stat_brand pk_stat_brand; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_brand
    ADD CONSTRAINT pk_stat_brand PRIMARY KEY (stat_date, brand_id, category);

-- Comments
--
-- Name: TABLE stat_brand; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.stat_brand IS '品牌维度统计表';
--
-- Name: COLUMN stat_brand.stat_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.stat_date IS '统计日期';
--
-- Name: COLUMN stat_brand.brand_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.brand_id IS '品牌ID';
--
-- Name: COLUMN stat_brand.brand_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.brand_name IS '品牌名称';
--
-- Name: COLUMN stat_brand.today_offer_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.today_offer_count IS '今日报盘数';
--
-- Name: COLUMN stat_brand.today_factory_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.today_factory_count IS '今日报盘工厂数';
--
-- Name: COLUMN stat_brand.today_product_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.today_product_count IS '今日报盘产品种类数';
--
-- Name: COLUMN stat_brand.price_min; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.price_min IS '今日最低价';
--
-- Name: COLUMN stat_brand.price_max; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand.price_max IS '今日最高价';
