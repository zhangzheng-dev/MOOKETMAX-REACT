-- ===== stat_merchant =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: stat_merchant_stat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.stat_merchant_stat_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: stat_merchant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stat_merchant (
    stat_id integer NOT NULL,
    stat_date date NOT NULL,
    merchant_id bigint NOT NULL,
    today_offer_count integer DEFAULT 0,
    today_inquiry_count integer DEFAULT 0,
    today_product_count integer DEFAULT 0,
    today_factory_count integer DEFAULT 0,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    category character varying(20) DEFAULT '牛'::character varying NOT NULL
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: stat_merchant_stat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.stat_merchant_stat_id_seq OWNED BY public.stat_merchant.stat_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: stat_merchant stat_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_merchant ALTER COLUMN stat_id SET DEFAULT nextval('public.stat_merchant_stat_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: stat_merchant stat_merchant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_merchant
    ADD CONSTRAINT stat_merchant_pkey PRIMARY KEY (stat_date, merchant_id, category);
--
-- Name: stat_merchant uk_stat_date_merchant; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_merchant
    ADD CONSTRAINT uk_stat_date_merchant UNIQUE (stat_date, merchant_id, category);
--
-- Name: stat_merchant uk_stat_date_merchant_category; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stat_merchant
    ADD CONSTRAINT uk_stat_date_merchant_category UNIQUE (stat_date, merchant_id, category);

-- Indexes
--
-- Name: idx_stat_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_date ON public.stat_merchant USING btree (stat_date);
--
-- Name: idx_stat_merchant_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_merchant_date ON public.stat_merchant USING btree (stat_date);
--
-- Name: idx_stat_merchant_date_merchant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_merchant_date_merchant ON public.stat_merchant USING btree (stat_date, merchant_id, category);
--
-- Name: idx_stat_merchant_merchant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stat_merchant_merchant ON public.stat_merchant USING btree (merchant_id);
