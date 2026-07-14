-- ===== dict_product =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: dict_product_product_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dict_product_product_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: dict_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dict_product (
    product_id integer NOT NULL,
    category character varying(10) NOT NULL,
    product_name character varying(100) NOT NULL,
    alias_list character varying(500) DEFAULT ''::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    source_goods_id bigint
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: dict_product_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dict_product_product_id_seq OWNED BY public.dict_product.product_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: dict_product product_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_product ALTER COLUMN product_id SET DEFAULT nextval('public.dict_product_product_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: dict_product dict_product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_product
    ADD CONSTRAINT dict_product_pkey PRIMARY KEY (product_id);
--
-- Name: dict_product uk_category_product; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_product
    ADD CONSTRAINT uk_category_product UNIQUE (category, product_name);

-- Indexes
--
-- Name: idx_dict_product_source_goods_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dict_product_source_goods_id ON public.dict_product USING btree (source_goods_id);
--
-- Name: idx_product_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_product_category ON public.dict_product USING btree (category);
--
-- Name: idx_product_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_product_name ON public.dict_product USING btree (product_name);
--
-- Name: uk_dict_product_source_goods_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_dict_product_source_goods_id ON public.dict_product USING btree (source_goods_id) WHERE (source_goods_id IS NOT NULL);
