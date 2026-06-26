-- ===== biz_offer =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: biz_offer_offer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.biz_offer_offer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: biz_offer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.biz_offer (
    offer_id bigint NOT NULL,
    offer_original_text text DEFAULT ''::text,
    category character varying(10) NOT NULL,
    product_id integer,
    product_name character varying(100) NOT NULL,
    country character varying(50) DEFAULT ''::character varying,
    factory_no character varying(50) DEFAULT ''::character varying,
    factory_id integer,
    brand_id integer,
    merchant_id bigint,
    contact_phone character varying(20) DEFAULT ''::character varying,
    user_id bigint NOT NULL,
    user_nickname character varying(100) DEFAULT ''::character varying,
    price numeric(10,2) DEFAULT NULL::numeric,
    price_max numeric(10,2) DEFAULT NULL::numeric,
    weight character varying(50) DEFAULT ''::character varying,
    offer_type character varying(10) NOT NULL,
    goods_type character varying(20) DEFAULT ''::character varying,
    goods_location character varying(50) DEFAULT ''::character varying,
    tags character varying(200) DEFAULT ''::character varying,
    fat_ratio character varying(20) DEFAULT ''::character varying,
    feeding_type character varying(20) DEFAULT ''::character varying,
    cattle_breed character varying(50) DEFAULT ''::character varying,
    remark text DEFAULT ''::text,
    publish_time timestamp without time zone NOT NULL,
    data_date date NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    package_id character varying,
    package_price numeric(12,2),
    package_total_quantity character varying,
    package_sell_type character varying,
    source_business_id bigint
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: biz_offer_offer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.biz_offer_offer_id_seq OWNED BY public.biz_offer.offer_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: biz_offer offer_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.biz_offer ALTER COLUMN offer_id SET DEFAULT nextval('public.biz_offer_offer_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: biz_offer biz_offer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.biz_offer
    ADD CONSTRAINT biz_offer_pkey PRIMARY KEY (offer_id);

-- Indexes
--
-- Name: idx_biz_offer_group_agg; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_biz_offer_group_agg ON public.biz_offer USING btree (product_id, category, offer_type, status, data_date, country, factory_no);
--
-- Name: idx_biz_offer_product_query; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_biz_offer_product_query ON public.biz_offer USING btree (product_id, category, offer_type, status, data_date);
--
-- Name: idx_biz_offer_source_business_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_biz_offer_source_business_id ON public.biz_offer USING btree (source_business_id);
--
-- Name: idx_offer_brand; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_brand ON public.biz_offer USING btree (brand_id);
--
-- Name: idx_offer_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_category ON public.biz_offer USING btree (category);
--
-- Name: idx_offer_data_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_data_date ON public.biz_offer USING btree (data_date);
--
-- Name: idx_offer_factory; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_factory ON public.biz_offer USING btree (factory_id);
--
-- Name: idx_offer_feeding_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_feeding_type ON public.biz_offer USING btree (feeding_type);
--
-- Name: idx_offer_goods_location; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_goods_location ON public.biz_offer USING btree (goods_location);
--
-- Name: idx_offer_goods_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_goods_type ON public.biz_offer USING btree (goods_type);
--
-- Name: idx_offer_merchant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_merchant ON public.biz_offer USING btree (merchant_id);
--
-- Name: idx_offer_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_product ON public.biz_offer USING btree (product_id);
--
-- Name: idx_offer_publish_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_publish_time ON public.biz_offer USING btree (publish_time);
--
-- Name: idx_offer_search; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_search ON public.biz_offer USING btree (category, offer_type, status, data_date);
--
-- Name: idx_offer_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_status ON public.biz_offer USING btree (status);
--
-- Name: idx_offer_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_offer_type ON public.biz_offer USING btree (offer_type);
--
-- Name: uk_biz_offer_source_business_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_biz_offer_source_business_id ON public.biz_offer USING btree (source_business_id);
