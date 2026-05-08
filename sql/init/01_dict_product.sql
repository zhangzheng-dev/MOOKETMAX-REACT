-- =============================================
-- 牧集 APP 数据库初始化脚本
-- 数据库: PostgreSQL 15+
-- 描述: 产品字典表（牛/猪部位）
-- =============================================

DROP TABLE IF EXISTS dict_product CASCADE;

CREATE TABLE dict_product (
    product_id SERIAL PRIMARY KEY,
    category VARCHAR(10) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    alias_list VARCHAR(500) DEFAULT '',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_category_product UNIQUE (category, product_name)
);

-- 创建索引
CREATE INDEX idx_product_category ON dict_product(category);
CREATE INDEX idx_product_name ON dict_product(product_name);

-- =============================================
-- 示例数据（牛肉产品）
-- =============================================
INSERT INTO dict_product (category, product_name, alias_list) VALUES
('牛', '牛腩', '牛腹腩、腹腩'),
('牛', '牛霖', '和尚头'),
('牛', '牛腱子', '腱子肉'),
('牛', '牛里脊', '西冷、肉眼'),
('牛', '牛眼肉', '眼肉'),
('牛', '牛上脑', '上脑'),
('牛', '牛腿肉', '腿肉'),
('牛', '牛碎肉', '碎肉'),
('牛', '牛排骨', '排骨'),
('牛', '牛舌', '牛口条');

-- =============================================
-- 示例数据（猪肉产品）
-- =============================================
INSERT INTO dict_product (category, product_name, alias_list) VALUES
('猪', '猪五花', '五花肉'),
('猪', '猪前腿', '前腿肉'),
('猪', '猪后腿', '后腿肉'),
('猪', '猪里脊', '里脊肉'),
('猪', '猪排骨', '排骨'),
('猪', '猪筒骨', '筒骨'),
('猪', '猪碎肉', '碎肉'),
('猪', '猪脚', '猪蹄');
