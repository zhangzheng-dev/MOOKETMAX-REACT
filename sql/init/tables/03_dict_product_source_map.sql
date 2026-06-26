-- ===== dict_product_source_map =====
-- Generated from pg_dump --schema-only of mooket_db (production)

-- Table
--
-- Name: dict_product_source_map; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dict_product_source_map (
    source_goods_id bigint NOT NULL,
    product_id integer NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- Constraints (PK / UK / FK)
--
-- Name: dict_product_source_map dict_product_source_map_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dict_product_source_map
    ADD CONSTRAINT dict_product_source_map_pkey PRIMARY KEY (source_goods_id);

-- Indexes
--
-- Name: idx_dict_product_source_map_product_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dict_product_source_map_product_id ON public.dict_product_source_map USING btree (product_id);
