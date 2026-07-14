-- ===== stat_brand_product =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: stat_brand_product_stat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stat_brand_product_stat_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: stat_brand_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stat_brand_product (
    stat_id integer NOT NULL,
    stat_date date NOT NULL,
    brand_id integer NOT NULL,
    brand_name character varying(100) NOT NULL,
    product_id integer NOT NULL,
    product_name character varying(100) NOT NULL,
    today_factory_count integer DEFAULT 0,
    today_offer_count integer DEFAULT 0,
    price_min numeric(10,2),
    price_max numeric(10,2),
    avg_price numeric(10,2),
    avg_price_yesterday numeric(10,2),
    price_change numeric(10,2),
    price_change_rate numeric(10,2),
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    category character varying(10) DEFAULT '牛'::character varying NOT NULL
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: stat_brand_product_stat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stat_brand_product_stat_id_seq OWNED BY public.stat_brand_product.stat_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: stat_brand_product stat_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_brand_product ALTER COLUMN stat_id SET DEFAULT nextval('public.stat_brand_product_stat_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: stat_brand_product pk_stat_brand_product; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_brand_product
    ADD CONSTRAINT pk_stat_brand_product PRIMARY KEY (stat_date, brand_id, product_id, category);

-- Indexes
--
-- Name: idx_stat_brand_product_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_brand_product_date ON public.stat_brand_product USING btree (stat_date);
--
-- Name: idx_stat_brand_product_offer_count; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_brand_product_offer_count ON public.stat_brand_product USING btree (stat_date, today_offer_count DESC);
--
-- Name: idx_stat_brand_product_pk; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_stat_brand_product_pk ON public.stat_brand_product USING btree (stat_date, brand_id, product_id, category);

-- Comments
--
-- Name: TABLE stat_brand_product; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.stat_brand_product IS '品牌产品维度统计表';
--
-- Name: COLUMN stat_brand_product.stat_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.stat_date IS '统计日期';
--
-- Name: COLUMN stat_brand_product.brand_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.brand_id IS '品牌ID';
--
-- Name: COLUMN stat_brand_product.brand_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.brand_name IS '品牌名称';
--
-- Name: COLUMN stat_brand_product.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.product_id IS '产品ID';
--
-- Name: COLUMN stat_brand_product.product_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.product_name IS '产品名称';
--
-- Name: COLUMN stat_brand_product.today_factory_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.today_factory_count IS '今日报盘工厂数';
--
-- Name: COLUMN stat_brand_product.today_offer_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.today_offer_count IS '今日报盘数';
--
-- Name: COLUMN stat_brand_product.price_min; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.price_min IS '今日最低价';
--
-- Name: COLUMN stat_brand_product.price_max; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.price_max IS '今日最高价';
--
-- Name: COLUMN stat_brand_product.avg_price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.avg_price IS '今日均价';
--
-- Name: COLUMN stat_brand_product.avg_price_yesterday; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.avg_price_yesterday IS '昨日均价';
--
-- Name: COLUMN stat_brand_product.price_change; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.price_change IS '涨跌额';
--
-- Name: COLUMN stat_brand_product.price_change_rate; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.stat_brand_product.price_change_rate IS '涨跌幅（%）';
