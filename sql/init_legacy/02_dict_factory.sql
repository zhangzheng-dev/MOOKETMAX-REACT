-- =============================================
-- 国家厂号字典表
-- =============================================

DROP TABLE IF EXISTS dict_factory CASCADE;

CREATE TABLE dict_factory (
    factory_id SERIAL PRIMARY KEY,
    category VARCHAR(10) NOT NULL,
    country VARCHAR(50) NOT NULL,
    country_alias VARCHAR(200) DEFAULT '',
    factory_no VARCHAR(50) NOT NULL,
    brand_id INT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_category_country_factory UNIQUE (category, country, factory_no)
);

-- 创建索引
CREATE INDEX idx_factory_category ON dict_factory(category);
CREATE INDEX idx_factory_country ON dict_factory(country);
CREATE INDEX idx_factory_brand ON dict_factory(brand_id);
CREATE INDEX idx_factory_no ON dict_factory(factory_no);

-- =============================================
-- 示例数据（牛肉厂号）
-- =============================================
INSERT INTO dict_factory (category, country, country_alias, factory_no, brand_id) VALUES
('牛', '巴西', '巴西', 'SIF1440', 1),
('牛', '巴西', '巴西', 'SIF504', 1),
('牛', '巴西', '巴西', 'SIF2058', 1),
('牛', '巴西', '巴西', 'SIF4333', 1),
('牛', '阿根廷', '阿根廷', 'SIF89', 2),
('牛', '阿根廷', '阿根廷', 'SIF200', 2),
('牛', '乌拉圭', '乌拉圭', 'SIF2', 3),
('牛', '澳大利亚', '澳洲', '1804', 4),
('牛', '澳大利亚', '澳洲', '262', 4),
('牛', '新西兰', '新西兰', 'S12', 5);

-- =============================================
-- 示例数据（猪肉厂号）
-- =============================================
INSERT INTO dict_factory (category, country, country_alias, factory_no, brand_id) VALUES
('猪', '中国', '中国', 'CN001', 6),
('猪', '美国', '美国', 'US401', 7),
('猪', '西班牙', '西班牙', 'ES10', 8),
('猪', '德国', '德国', 'DE100', 9);
