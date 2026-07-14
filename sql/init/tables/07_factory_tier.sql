-- ===== factory_tier =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: factory_tier_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.factory_tier_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: factory_tier; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.factory_tier (
    id integer NOT NULL,
    category character varying(20) DEFAULT '牛'::character varying,
    product_name character varying(100) NOT NULL,
    factory_no character varying(50) NOT NULL,
    tier character varying(10) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    country character varying(50) DEFAULT '巴西'::character varying
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: factory_tier_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.factory_tier_id_seq OWNED BY public.factory_tier.id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: factory_tier id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.factory_tier ALTER COLUMN id SET DEFAULT nextval('public.factory_tier_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: factory_tier factory_tier_country_product_name_factory_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.factory_tier
    ADD CONSTRAINT factory_tier_country_product_name_factory_no_key UNIQUE (country, product_name, factory_no, tier);
--
-- Name: factory_tier factory_tier_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.factory_tier
    ADD CONSTRAINT factory_tier_pkey PRIMARY KEY (id);

-- Indexes
--
-- Name: idx_factory_tier_tier; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_factory_tier_tier ON public.factory_tier USING btree (category, product_name, tier);
