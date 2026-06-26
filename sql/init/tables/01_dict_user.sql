-- ===== dict_user =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: dict_user_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dict_user_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: dict_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dict_user (
    user_id bigint NOT NULL,
    phone character varying(20) NOT NULL,
    nickname character varying(20),
    identity_tags character varying(200),
    wechat character varying(50),
    wechat_nickname character varying(50),
    real_name character varying(50),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    avatar_url character varying(500),
    real_name_status character varying(20) DEFAULT 'pending'::character varying,
    cancellation_status character varying(20) DEFAULT 'active'::character varying,
    mooket_id character varying(50),
    mooket_no character varying(100)
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: dict_user_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dict_user_user_id_seq OWNED BY public.dict_user.user_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: dict_user user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_user ALTER COLUMN user_id SET DEFAULT nextval('public.dict_user_user_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: dict_user dict_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_user
    ADD CONSTRAINT dict_user_pkey PRIMARY KEY (user_id);

-- Indexes
--
-- Name: idx_dict_user_phone; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dict_user_phone ON public.dict_user USING btree (phone);
