# mooket_db 数据库初始化

本目录的 SQL 文件由 **`pg_dump --schema-only`** 从生产库 `mooket_db` 导出后整理生成，
和生产库当前 schema **100% 对齐**（19 表 / 77 索引 / 2 函数 / 17 序列）。

## 文件结构

```
sql/init/
├── README.md                   ← 本文件
├── schema.sql                  ← 单文件全量初始化（推荐用于迁移 / 新部署）
├── functions/                  ← 自定义函数（如 clean_expired_offers / update_merchant_stat）
└── tables/                     ← 19 张表的独立文件，按依赖顺序编号
    ├── 01_dict_user.sql
    ├── 02_dict_product.sql
    ├── 03_dict_product_source_map.sql
    ├── 04_dict_factory.sql
    ├── 05_dict_brand.sql
    ├── 06_dict_merchant.sql
    ├── 07_factory_tier.sql
    ├── 08_rel_user_merchant.sql
    ├── 09_biz_offer.sql
    ├── 10_biz_search_history.sql
    ├── 11_stat_country.sql
    ├── 12_stat_factory.sql
    ├── 13_stat_product.sql
    ├── 14_stat_merchant.sql
    ├── 15_stat_brand.sql
    ├── 16_stat_country_product.sql
    ├── 17_stat_factory_product.sql
    ├── 18_stat_brand_product.sql
    └── 19_stat_price_trend.sql
```

每张表的文件按以下顺序输出：
1. `CREATE SEQUENCE`（如有）
2. `CREATE TABLE`
3. `ALTER SEQUENCE ... OWNED BY`（把序列绑定到主键列）
4. `ALTER TABLE ... SET DEFAULT nextval(...)`
5. `ALTER TABLE ... ADD CONSTRAINT`（主键、唯一键等）
6. `CREATE INDEX`（按字母序）
7. `COMMENT ON TABLE/COLUMN`

## 全新数据库初始化

```bash
# 1) 创建数据库
psql -U postgres -c "CREATE DATABASE mooket_db;"

# 2) 一次性应用 schema
psql -U <dba_user> -d mooket_db -v ON_ERROR_STOP=1 -f schema.sql
```

容器内（生产部署常用）：

```bash
docker exec -i -e PGPASSWORD=<your_password> postgres32 \
  psql -U mooketmax_dba -d mooket_db -v ON_ERROR_STOP=1 < schema.sql
```

## 验证 schema 完整性

应用 `schema.sql` 后比对 object 数应该完全相等：

```sql
SELECT
  (SELECT count(*) FROM pg_tables    WHERE schemaname='public') AS tables,
  (SELECT count(*) FROM pg_indexes   WHERE schemaname='public') AS indexes,
  (SELECT count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace
     WHERE n.nspname='public') AS functions,
  (SELECT count(*) FROM pg_sequences WHERE schemaname='public') AS sequences;
```

预期结果：

| tables | indexes | functions | sequences |
|--------|---------|-----------|-----------|
| 19     | 77      | 2         | 17        |

## 重新生成

当生产库 schema 变化（加表、加索引、改列等），用以下两步同步本目录：

```bash
# 1. 在服务器上重新导出
docker exec -e PGPASSWORD=<pwd> postgres32 \
  pg_dump -U mooketmax_dba -d mooket_db \
  --schema-only --no-owner --no-privileges \
  > /tmp/mooket_schema.sql

# 2. 下载到 .tmp/ 后跑解析脚本
scp <server>:/tmp/mooket_schema.sql .tmp/
python .tmp/parse_schema.py
```

## 历史 init 文件

旧版手写的 init 文件被移到了 `sql/init_legacy/` 作为历史参考。新代码应使用本目录的文件，
特别注意旧版**漏了 `dict_product_source_map` 表** 和 `clean_expired_offers / update_merchant_stat`
两个函数，且很多索引（如 biz_offer 的 7 个复合索引）也没有定义。
