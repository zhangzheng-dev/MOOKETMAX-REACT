-- =============================================
-- 牧集数据同步脚本：从 MySQL (social_online_business) 同步到 PostgreSQL (biz_offer)
-- 执行前请确保：
-- 1. 已创建 MySQL 到 PostgreSQL 的外部数据源连接
-- 2. 已配置好 dict_factory、dict_merchant、sys_dict 等字典表的 MySQL fdw
-- 3. 已创建 social_online_business 和相关表的 fdw
-- =============================================

-- 创建外部服务器 (MySQL FDW) - 示例配置
-- CREATE EXTENSION IF NOT EXISTS mysql_fdw;
-- CREATE SERVER mysql_server FOREIGN DATA WRAPPER mysql_fdw
--   OPTIONS (host 'xxx', port '3306', database 'your_db');
-- CREATE USER MAPPING FOR CURRENT_USER SERVER mysql_server
--   OPTIONS (username 'your_user', password 'your_password');

-- 同步 SQL
INSERT INTO biz_offer (
    offer_original_text,
    category,
    product_id,
    product_name,
    country,
    factory_no,
    factory_id,
    merchant_id,
    contact_phone,
    user_id,
    user_nickname,
    price,
    weight,
    offer_type,
    goods_type,
    goods_location,
    fat_ratio,
    feeding_type,
    cattle_breed,
    remark,
    publish_time,
    data_date,
    status,
    create_time
)
SELECT
    -- offer_original_text: 通过 online_business_content_id 关联 social_online_business_content
    (SELECT content FROM mysql_social_online_business_content WHERE id = src.online_business_content_id),

    -- category: goods_category 转换 1=牛, 2=羊, 3=猪, 4=禽, 5=水产
    CASE src.goods_category
        WHEN 1 THEN '牛'
        WHEN 2 THEN '羊'
        WHEN 3 THEN '猪'
        WHEN 4 THEN '禽'
        WHEN 5 THEN '水产'
        ELSE NULL
    END,

    -- product_id: 直接映射 standard_goods_name_id
    src.standard_goods_name_id,

    -- product_name: 通过 standard_goods_name_id 关联 social_standard_goods_name
    (SELECT standard_goods_name FROM mysql_social_standard_goods_name WHERE id = src.standard_goods_name_id),

    -- country: 通过 country 关联 sys_dict (dict_name_en='ggoods_country')
    (SELECT dict_value FROM mysql_sys_dict
     WHERE dict_name_en = 'ggoods_country' AND dict_key = src.country::text),

    -- factory_no: 直接映射
    src.plant_no,

    -- factory_id: 通过 plant_no + country 关联 dict_factory
    (SELECT f.factory_id FROM dict_factory f
     WHERE f.factory_no = src.plant_no
       AND f.country = (SELECT dict_value FROM mysql_sys_dict
                       WHERE dict_name_en = 'ggoods_country' AND dict_key = src.country::text)
     LIMIT 1),

    -- merchant_id: 通过 phone_no 或 user_id 关联 dict_merchant
    (SELECT m.merchant_id FROM dict_merchant m
     WHERE m.phone = src.phone_no OR m.user_id = src.user_id
     LIMIT 1),

    -- contact_phone: 直接映射
    src.phone_no,

    -- user_id: 直接映射
    src.user_id,

    -- user_nickname: 直接映射
    src.user_name,

    -- price: 直接映射
    src.amount,

    -- weight: weight + weight_unit (通过 sys_dict 转换, dict_name_en='weight_unit')
    src.weight || ' ' || (SELECT dict_value FROM mysql_sys_dict
                          WHERE dict_name_en = 'weight_unit' AND dict_key = src.weight_unit::text),

    -- offer_type: is_offer 转换 0=求购, 1=报盘
    CASE src.is_offer WHEN 0 THEN '求购' WHEN 1 THEN '报盘' ELSE NULL END,

    -- goods_type: business_category (通过 sys_dict 转换, dict_name_en='business_category')
    (SELECT dict_value FROM mysql_sys_dict
     WHERE dict_name_en = 'business_category' AND dict_key = src.business_category::text),

    -- goods_location: address_province + address_city 组合
    CONCAT(src.address_province, src.address_city),

    -- fat_ratio: lean_ratio (通过 sys_dict 转换, dict_name_en='lean_ratio')
    (SELECT dict_value FROM mysql_sys_dict
     WHERE dict_name_en = 'lean_ratio' AND dict_key = src.lean_ratio::text),

    -- feeding_type: 直接映射
    src.standard,

    -- cattle_breed: 直接映射
    src.standard_two,

    -- remark: 直接映射
    src.memo,

    -- publish_time: 直接映射
    src.offer_date,

    -- data_date: 从 offer_date 提取日期部分
    DATE(src.offer_date),

    -- status: 1=已过期, 3=ACTIVE
    CASE src.status WHEN 1 THEN '已过期' WHEN 3 THEN 'ACTIVE' ELSE NULL END,

    -- create_time: 直接映射
    src.created_time

FROM mysql_social_online_business src

-- 只同步 status=3 的数据，过滤 status=1 和 status=2
WHERE src.status = 3
  AND src.is_deleted = 0
  AND src.display_flag = 1;
