-- ===== dict_merchant =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: dict_merchant_merchant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dict_merchant_merchant_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: dict_merchant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dict_merchant (
    merchant_id bigint NOT NULL,
    merchant_name character varying(200) NOT NULL,
    merchant_short_name character varying(100) DEFAULT ''::character varying,
    merchant_tags character varying(200) DEFAULT ''::character varying,
    contact_phone character varying(20) DEFAULT ''::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: dict_merchant_merchant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dict_merchant_merchant_id_seq OWNED BY public.dict_merchant.merchant_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: dict_merchant merchant_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_merchant ALTER COLUMN merchant_id SET DEFAULT nextval('public.dict_merchant_merchant_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: dict_merchant dict_merchant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_merchant
    ADD CONSTRAINT dict_merchant_pkey PRIMARY KEY (merchant_id);

-- Indexes
--
-- Name: idx_merchant_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_name ON public.dict_merchant USING btree (merchant_name);
