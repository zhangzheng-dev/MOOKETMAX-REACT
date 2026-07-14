-- ===== stat_product =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: stat_product_stat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stat_product_stat_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: stat_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stat_product (
    stat_id integer NOT NULL,
    stat_date date NOT NULL,
    category character varying(20) NOT NULL,
    product_id integer NOT NULL,
    product_name character varying(100) NOT NULL,
    today_offer_count integer DEFAULT 0,
    today_inquiry_count integer DEFAULT 0,
    today_merchant_count integer DEFAULT 0,
    today_factory_count integer DEFAULT 0,
    price_min numeric(10,2),
    price_max numeric(10,2),
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: stat_product_stat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stat_product_stat_id_seq OWNED BY public.stat_product.stat_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: stat_product stat_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_product ALTER COLUMN stat_id SET DEFAULT nextval('public.stat_product_stat_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: stat_product pk_stat_product; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_product
    ADD CONSTRAINT pk_stat_product PRIMARY KEY (stat_date, product_id);

-- Indexes
--
-- Name: idx_stat_product_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_product_date ON public.stat_product USING btree (stat_date);
--
-- Name: idx_stat_product_offer_count; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_product_offer_count ON public.stat_product USING btree (stat_date, today_offer_count DESC);

-- Comments
--
-- Name: TABLE stat_product; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.stat_product IS '产品维度统计表';
--
-- Name: COLUMN stat_product.stat_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.stat_date IS '统计日期';
--
-- Name: COLUMN stat_product.category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.category IS '大类：牛/猪';
--
-- Name: COLUMN stat_product.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.product_id IS '产品ID';
--
-- Name: COLUMN stat_product.product_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.product_name IS '产品名称';
--
-- Name: COLUMN stat_product.today_offer_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.today_offer_count IS '今日报盘数';
--
-- Name: COLUMN stat_product.today_inquiry_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.today_inquiry_count IS '今日求购数';
--
-- Name: COLUMN stat_product.today_merchant_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.today_merchant_count IS '今日报盘商家数';
--
-- Name: COLUMN stat_product.today_factory_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.today_factory_count IS '今日报盘工厂数';
--
-- Name: COLUMN stat_product.price_min; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.price_min IS '今日最低价';
--
-- Name: COLUMN stat_product.price_max; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_product.price_max IS '今日最高价';
