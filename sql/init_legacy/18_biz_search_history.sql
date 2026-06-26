-- =============================================
-- 搜索历史表
-- =============================================

CREATE TABLE IF NOT EXISTS biz_search_history (
    history_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,                          -- 用户ID
    search_word VARCHAR(200) NOT NULL,                -- 搜索词
    search_type VARCHAR(50) NOT NULL,                 -- 搜索类型（产品/国家/品牌/商家/国家厂号/国家产品/品牌产品/国家厂号产品）
    is_self_select SMALLINT DEFAULT 0,                -- 是否自选（0-否，1-是）
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP   -- 创建时间
);

-- 索引
CREATE INDEX idx_search_history_user ON biz_search_history(user_id);
CREATE INDEX idx_search_history_user_select ON biz_search_history(user_id, is_self_select);
CREATE INDEX idx_search_history_create_time ON biz_search_history(user_id, create_time DESC);

COMMENT ON TABLE biz_search_history IS '搜索历史表';
COMMENT ON COLUMN biz_search_history.history_id IS '历史ID，主键';
COMMENT ON COLUMN biz_search_history.user_id IS '用户ID';
COMMENT ON COLUMN biz_search_history.search_word IS '搜索词';
COMMENT ON COLUMN biz_search_history.search_type IS '搜索类型';
COMMENT ON COLUMN biz_search_history.is_self_select IS '是否自选（0-否，1-是）';
COMMENT ON COLUMN biz_search_history.create_time IS '创建时间';
