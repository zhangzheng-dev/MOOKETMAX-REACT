-- =============================================
-- 品牌字典表
-- =============================================

DROP TABLE IF EXISTS dict_brand CASCADE;

CREATE TABLE dict_brand (
    brand_id SERIAL PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL,
    alias_list VARCHAR(500) DEFAULT '',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_brand_name UNIQUE (brand_name)
);

-- 创建索引
CREATE INDEX idx_brand_name ON dict_brand(brand_name);

-- =============================================
-- 示例数据
-- =============================================
INSERT INTO dict_brand (brand_name, alias_list) VALUES
('JBS S.A.', 'JBS、JBS SA'),
('Minerva', 'MFS'),
('Marfrig', 'MARFRIG'),
('Australian Lamb', '澳洲羊肉'),
('Alliance', '新西兰联合'),
('双汇', 'SH'),
('Smithfield', '史密斯菲尔德'),
('ElPozo', '埃尔波索'),
('Tonnies', '通尼斯');
