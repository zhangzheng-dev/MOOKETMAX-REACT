-- ===== biz_search_history =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Sequences (must exist before CREATE TABLE references them)
--
-- Name: biz_search_history_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.biz_search_history_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table
--
-- Name: biz_search_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.biz_search_history (
    history_id bigint NOT NULL,
    user_id bigint NOT NULL,
    search_word character varying(200) NOT NULL,
    search_type character varying(50) NOT NULL,
    is_self_select smallint DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    product_id bigint,
    product_name character varying(255),
    country character varying(100),
    factory_no character varying(100),
    brand_id bigint,
    merchant_id bigint
);

-- Link sequences to columns (must run after CREATE TABLE)
--
-- Name: biz_search_history_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.biz_search_history_history_id_seq OWNED BY public.biz_search_history.history_id;

-- ALTER TABLE (column defaults, etc.)
--
-- Name: biz_search_history history_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.biz_search_history ALTER COLUMN history_id SET DEFAULT nextval('public.biz_search_history_history_id_seq'::regclass);

-- Constraints (PK / UK / FK)
--
-- Name: biz_search_history biz_search_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.biz_search_history
    ADD CONSTRAINT biz_search_history_pkey PRIMARY KEY (history_id);

-- Indexes
--
-- Name: idx_search_history_create_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_search_history_create_time ON public.biz_search_history USING btree (user_id, create_time DESC);
--
-- Name: idx_search_history_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_search_history_user ON public.biz_search_history USING btree (user_id);
--
-- Name: idx_search_history_user_select; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_search_history_user_select ON public.biz_search_history USING btree (user_id, is_self_select);

-- Comments
--
-- Name: TABLE biz_search_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.biz_search_history IS '搜索历史表';
--
-- Name: COLUMN biz_search_history.history_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.biz_search_history.history_id IS '历史ID，主键';
--
-- Name: COLUMN biz_search_history.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.biz_search_history.user_id IS '用户ID';
--
-- Name: COLUMN biz_search_history.search_word; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.biz_search_history.search_word IS '搜索词';
--
-- Name: COLUMN biz_search_history.search_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.biz_search_history.search_type IS '搜索类型';
--
-- Name: COLUMN biz_search_history.is_self_select; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.biz_search_history.is_self_select IS '是否自选（0-否，1-是）';
--
-- Name: COLUMN biz_search_history.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.biz_search_history.create_time IS '创建时间';
