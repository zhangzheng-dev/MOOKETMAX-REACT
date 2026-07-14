-- ===== dict_factory =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: dict_factory_factory_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dict_factory_factory_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: dict_factory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dict_factory (
    factory_id integer NOT NULL,
    category character varying(10) NOT NULL,
    country character varying(50) NOT NULL,
    country_alias character varying(200) DEFAULT ''::character varying,
    factory_no character varying(50) NOT NULL,
    brand_id integer,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: dict_factory_factory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dict_factory_factory_id_seq OWNED BY public.dict_factory.factory_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: dict_factory factory_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_factory ALTER COLUMN factory_id SET DEFAULT nextval('public.dict_factory_factory_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: dict_factory dict_factory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_factory
    ADD CONSTRAINT dict_factory_pkey PRIMARY KEY (factory_id);
--
-- Name: dict_factory uk_category_country_factory; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_factory
    ADD CONSTRAINT uk_category_country_factory UNIQUE (category, country, factory_no);

-- Indexes
--
-- Name: idx_factory_brand; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_factory_brand ON public.dict_factory USING btree (brand_id);
--
-- Name: idx_factory_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_factory_category ON public.dict_factory USING btree (category);
--
-- Name: idx_factory_country; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_factory_country ON public.dict_factory USING btree (country);
--
-- Name: idx_factory_no; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_factory_no ON public.dict_factory USING btree (factory_no);
