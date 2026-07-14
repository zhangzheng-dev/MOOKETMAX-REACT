-- ===== stat_price_trend =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: stat_price_trend_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stat_price_trend_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: stat_price_trend; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stat_price_trend (
    id bigint NOT NULL,
    stat_date date NOT NULL,
    dimension_type character varying(50) NOT NULL,
    country character varying(100),
    product_id integer,
    product_name character varying(200),
    factory_no character varying(100),
    offer_type character varying(20),
    avg_price numeric(10,2),
    record_date date NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: stat_price_trend_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stat_price_trend_id_seq OWNED BY public.stat_price_trend.id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: stat_price_trend id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_price_trend ALTER COLUMN id SET DEFAULT nextval('public.stat_price_trend_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: stat_price_trend stat_price_trend_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_price_trend
    ADD CONSTRAINT stat_price_trend_pkey PRIMARY KEY (id);
--
-- Name: stat_price_trend stat_price_trend_stat_date_dimension_type_country_product_i_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_price_trend
    ADD CONSTRAINT stat_price_trend_stat_date_dimension_type_country_product_i_key UNIQUE (stat_date, dimension_type, country, product_id, factory_no, offer_type);

-- Indexes
--
-- Name: idx_trend_query; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_trend_query ON public.stat_price_trend USING btree (dimension_type, country, product_id, factory_no, offer_type, stat_date DESC);
--
-- Name: idx_trend_record; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_trend_record ON public.stat_price_trend USING btree (dimension_type, country, product_id, factory_no, offer_type, record_date);
