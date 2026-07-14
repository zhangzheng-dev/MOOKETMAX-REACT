-- ===== rel_user_merchant =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Table
--
-- Name: rel_user_merchant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rel_user_merchant (
    user_id bigint NOT NULL,
    mobile character varying(20) DEFAULT ''::character varying,
    nickname character varying(100) DEFAULT ''::character varying,
    identity character varying(200) DEFAULT ''::character varying,
    merchant_id bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Constraints (PK / UK / FK)
--
-- Name: rel_user_merchant rel_user_merchant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rel_user_merchant
    ADD CONSTRAINT rel_user_merchant_pkey PRIMARY KEY (user_id);

-- Indexes
--
-- Name: idx_user_merchant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_merchant ON public.rel_user_merchant USING btree (merchant_id);
--
-- Name: idx_user_mobile; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_mobile ON public.rel_user_merchant USING btree (mobile);
