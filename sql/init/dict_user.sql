-- 用户表
CREATE TABLE IF NOT EXISTS dict_user (
    user_id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    nickname VARCHAR(20),
    identity_tags VARCHAR(200),  -- 逗号分隔：海外服务商,贸易商,加工厂/商超,其它
    wechat VARCHAR(50),
    wechat_nickname VARCHAR(50),
    real_name VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 手机号索引
CREATE INDEX IF NOT EXISTS idx_dict_user_phone ON dict_user(phone);
