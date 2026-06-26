-- =============================================
-- 为商家1（阿西食品）添加更多报盘数据
-- 用于测试国家筛选的水平滚动功能
-- =============================================

-- 巴西的报盘
INSERT INTO biz_offer (
    category, product_id, product_name, country, factory_no, factory_id, brand_id,
    merchant_id, contact_phone, user_id, user_nickname, price, price_max, weight,
    offer_type, goods_type, goods_location, tags, feeding_type, publish_time, data_date
) VALUES
('牛', 1, '牛腩', '巴西', 'SIF504', 2, 1, 1, '13800138001', 1001, '张三', 46.00, 50.00, '3柜',
 '报盘', '现货', '上海', '草饲,新日期', '草饲', NOW(), CURRENT_DATE),
('牛', 1, '牛腩', '巴西', 'SIF2058', 3, 1, 1, '13800138001', 1002, '李四', 47.00, NULL, '2柜',
 '报盘', '期货', '郑州', '谷饲', '谷饲', NOW(), CURRENT_DATE),
('牛', 1, '牛腩', '巴西', 'SIF4333', 4, 1, 1, '13800138001', 1001, '张三', 45.00, 48.00, '4柜',
 '报盘', '半期货', '上海', '草饲', '草饲', NOW(), CURRENT_DATE);

-- 阿根廷的报盘
INSERT INTO biz_offer (
    category, product_id, product_name, country, factory_no, factory_id, brand_id,
    merchant_id, contact_phone, user_id, user_nickname, price, price_max, weight,
    offer_type, goods_type, goods_location, tags, feeding_type, publish_time, data_date
) VALUES
('牛', 2, '牛霖', '阿根廷', 'SIF89', 6, 2, 1, '13800138001', 1003, '王五', 44.00, NULL, '2柜',
 '报盘', '现货', '广州', '新日期', '草饲', NOW(), CURRENT_DATE),
('牛', 2, '牛霖', '阿根廷', 'SIF200', 7, 2, 1, '13800138001', 1003, '王五', 43.00, 46.00, '3柜',
 '报盘', '期货', '天津', '谷饲', '谷饲', NOW(), CURRENT_DATE);

-- 乌拉圭的报盘
INSERT INTO biz_offer (
    category, product_id, product_name, country, factory_no, factory_id, brand_id,
    merchant_id, contact_phone, user_id, user_nickname, price, price_max, weight,
    offer_type, goods_type, goods_location, tags, feeding_type, publish_time, data_date
) VALUES
('牛', 3, '牛腱子', '乌拉圭', 'SIF2', 8, 3, 1, '13800138001', 1004, '赵六', 55.00, NULL, '1柜',
 '报盘', '现货', '深圳', '草饲', '草饲', NOW(), CURRENT_DATE),
('牛', 4, '牛肩肉', '乌拉圭', 'SIF3', 9, 3, 1, '13800138001', 1004, '赵六', 52.00, 55.00, '2柜',
 '报盘', '半期货', '青岛', '新日期', '谷饲', NOW(), CURRENT_DATE);

-- 澳大利亚的报盘
INSERT INTO biz_offer (
    category, product_id, product_name, country, factory_no, factory_id, brand_id,
    merchant_id, contact_phone, user_id, user_nickname, price, price_max, weight,
    offer_type, goods_type, goods_location, tags, feeding_type, publish_time, data_date
) VALUES
('牛', 5, '牛前肉', '澳大利亚', '1804', 10, 4, 1, '13800138001', 1001, '张三', 65.00, NULL, '2柜',
 '报盘', '现货', '上海', '新日期', '草饲', NOW(), CURRENT_DATE),
('牛', 5, '牛前肉', '澳大利亚', '262', 11, 4, 1, '13800138001', 1005, '钱七', 63.00, 68.00, '3柜',
 '报盘', '期货', '大连', '谷饲', '谷饲', NOW(), CURRENT_DATE);

-- 新西兰的报盘
INSERT INTO biz_offer (
    category, product_id, product_name, country, factory_no, factory_id, brand_id,
    merchant_id, contact_phone, user_id, user_nickname, price, price_max, weight,
    offer_type, goods_type, goods_location, tags, feeding_type, publish_time, data_date
) VALUES
('牛', 6, '牛后肉', '新西兰', 'S12', 12, 5, 1, '13800138001', 1006, '孙八', 58.00, NULL, '1柜',
 '报盘', '现货', '厦门', '新日期', '草饲', NOW(), CURRENT_DATE),
('牛', 6, '牛后肉', '新西兰', 'S18', 13, 5, 1, '13800138001', 1006, '孙八', 56.00, 60.00, '2柜',
 '报盘', '半期货', '福州', '谷饲', '谷饲', NOW(), CURRENT_DATE);
