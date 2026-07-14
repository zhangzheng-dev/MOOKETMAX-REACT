-- ===== dict_brand =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: dict_brand_brand_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dict_brand_brand_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: dict_brand; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dict_brand (
    brand_id integer NOT NULL,
    brand_name character varying(100) NOT NULL,
    alias_list character varying(500) DEFAULT ''::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    factory_id integer,
    factory_no character varying(100),
    country character varying(50),
    category character varying(20)
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: dict_brand_brand_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dict_brand_brand_id_seq OWNED BY public.dict_brand.brand_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: dict_brand brand_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_brand ALTER COLUMN brand_id SET DEFAULT nextval('public.dict_brand_brand_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: dict_brand dict_brand_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_brand
    ADD CONSTRAINT dict_brand_pkey PRIMARY KEY (brand_id);

-- Indexes
--
-- Name: idx_brand_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_brand_name ON public.dict_brand USING btree (brand_name);
