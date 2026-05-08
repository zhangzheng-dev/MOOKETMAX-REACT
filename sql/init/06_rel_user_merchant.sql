-- =============================================
-- 用户商家关联表
-- =============================================

DROP TABLE IF EXISTS rel_user_merchant CASCADE;

CREATE TABLE rel_user_merchant (
    user_id BIGINT PRIMARY KEY,
    mobile VARCHAR(20) DEFAULT '',
    nickname VARCHAR(100) DEFAULT '',
    identity VARCHAR(200) DEFAULT '',
    merchant_id INT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_user_merchant ON rel_user_merchant(merchant_id);
CREATE INDEX idx_user_mobile ON rel_user_merchant(mobile);

-- 示例数据
INSERT INTO rel_user_merchant (user_id, mobile, nickname, identity, merchant_id) VALUES
(1001, '13800138001', '张三', '贸易商', 1),
(1002, '13800138002', '李四', '加工厂', 1),
(1003, '13800138003', '王五', '海外服务商', 2),
(1004, '13800138004', '赵六', '商超', 3);
