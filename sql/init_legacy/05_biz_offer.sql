-- =============================================
-- 报盘/求购表（核心业务表）
-- =============================================

DROP TABLE IF EXISTS biz_offer CASCADE;

CREATE TABLE biz_offer (
    offer_id BIGSERIAL PRIMARY KEY,
    source_business_id BIGINT,
    offer_original_text TEXT DEFAULT '',
    category VARCHAR(10) NOT NULL,
    product_id INT DEFAULT NULL,
    product_name VARCHAR(100) NOT NULL,
    country VARCHAR(50) DEFAULT '',
    factory_no VARCHAR(50) DEFAULT '',
    factory_id INT DEFAULT NULL,
    brand_id INT DEFAULT NULL,
    merchant_id INT NOT NULL,
    contact_phone VARCHAR(20) DEFAULT '',
    user_id BIGINT NOT NULL,
    user_nickname VARCHAR(100) DEFAULT '',
    price DECIMAL(10,2) DEFAULT NULL,
    price_max DECIMAL(10,2) DEFAULT NULL,
    weight VARCHAR(50) DEFAULT '',
    offer_type VARCHAR(10) NOT NULL,
    goods_type VARCHAR(20) DEFAULT '',
    goods_location VARCHAR(50) DEFAULT '',
    tags VARCHAR(200) DEFAULT '',
    fat_ratio VARCHAR(20) DEFAULT '',
    feeding_type VARCHAR(20) DEFAULT '',
    cattle_breed VARCHAR(50) DEFAULT '',
    remark TEXT DEFAULT '',
    publish_time TIMESTAMP NOT NULL,
    data_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_offer_category ON biz_offer(category);
CREATE INDEX idx_offer_type ON biz_offer(offer_type);
CREATE INDEX idx_offer_product ON biz_offer(product_id);
CREATE INDEX idx_offer_merchant ON biz_offer(merchant_id);
CREATE INDEX idx_offer_factory ON biz_offer(factory_id);
CREATE INDEX idx_offer_brand ON biz_offer(brand_id);
CREATE INDEX idx_offer_publish_time ON biz_offer(publish_time);
CREATE INDEX idx_offer_data_date ON biz_offer(data_date);
CREATE INDEX idx_offer_status ON biz_offer(status);
CREATE INDEX idx_offer_goods_location ON biz_offer(goods_location);
CREATE INDEX idx_offer_goods_type ON biz_offer(goods_type);
CREATE INDEX idx_offer_feeding_type ON biz_offer(feeding_type);
CREATE UNIQUE INDEX uk_biz_offer_source_business_id ON biz_offer(source_business_id);

-- 创建联合索引用于搜索
CREATE INDEX idx_offer_search ON biz_offer(category, offer_type, status, data_date);

-- =============================================
-- 示例数据
-- =============================================
INSERT INTO biz_offer (
    category, product_id, product_name, country, factory_no, factory_id, brand_id,
    merchant_id, contact_phone, user_id, user_nickname, price, price_max, weight,
    offer_type, goods_type, goods_location, tags, publish_time, data_date
) VALUES
('牛', 1, '牛腩', '巴西', 'SIF1440', 1, 1, 1, '13800138001', 1001, '张三', 48.00, 52.00, '2柜',
 '报盘', '现货', '上海', '急售,新日期', NOW(), CURRENT_DATE),
('牛', 1, '牛腩', '巴西', 'SIF1440', 1, 1, 1, '13800138001', 1002, '李四', 50.00, NULL, '27吨',
 '报盘', '现货', '郑州', '一口价', NOW(), CURRENT_DATE),
('牛', 2, '牛霖', '阿根廷', 'SIF89', 6, 2, 2, '13800138002', 1003, '王五', 45.00, NULL, '1柜',
 '报盘', '半期货', '天津', '可开票', NOW(), CURRENT_DATE),
('牛', 3, '牛腱子', '澳大利亚', '1804', 8, 4, 1, '13800138001', 1001, '张三', 68.00, 72.00, '500件',
 '报盘', '现货', '上海', '新日期', NOW(), CURRENT_DATE),
('猪', 11, '猪五花', '中国', 'CN001', 11, 6, 3, '13800138003', 1004, '赵六', 28.00, NULL, '3柜',
 '求购', '期货', '广州', '长期求购', NOW(), CURRENT_DATE);
