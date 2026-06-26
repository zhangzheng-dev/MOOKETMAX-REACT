-- =============================================
-- 商家字典表
-- =============================================

DROP TABLE IF EXISTS dict_merchant CASCADE;

CREATE TABLE dict_merchant (
    merchant_id SERIAL PRIMARY KEY,
    merchant_name VARCHAR(200) NOT NULL,
    merchant_short_name VARCHAR(100) DEFAULT '',
    merchant_tags VARCHAR(200) DEFAULT '',
    contact_phone VARCHAR(20) DEFAULT '',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_merchant_name ON dict_merchant(merchant_name);

-- =============================================
-- 示例数据
-- =============================================
INSERT INTO dict_merchant (merchant_name, merchant_short_name, merchant_tags, contact_phone) VALUES
('上海阿西食品有限公司', '阿西食品', '知名商家', '13800138001'),
('北京中恒国际贸易有限公司', '中恒国际', '知名商家', '13800138002'),
('广州恒达进出口贸易公司', '恒达贸易', '', '13800138003'),
('深圳华肉类食品有限公司', '华肉类', '', '13800138004'),
('成都老邻居食品有限公司', '老邻居', '', '13800138005');
