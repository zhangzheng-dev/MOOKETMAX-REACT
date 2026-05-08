-- 厂号等级配置表（factory_tier）
-- 存储产品的厂号等级信息，用于平替产品功能。同一产品下，相同等级的厂号互为平替。

CREATE TABLE IF NOT EXISTS factory_tier (
    id SERIAL PRIMARY KEY,
    category VARCHAR(20) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    factory_no VARCHAR(50) NOT NULL,
    tier VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (category, product_name, factory_no)
);

CREATE INDEX IF NOT EXISTS idx_factory_tier_tier ON factory_tier(category, product_name, tier);
