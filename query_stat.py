import psycopg2
conn = psycopg2.connect('postgresql://mooketmax_dba:MooketMax%402024%21@43.139.56.124:30032/mooket_db')
cur = conn.cursor()

# Get ALL rows for JBS 前腱 today
cur.execute("""
SELECT stat_date, brand_id, brand_name, product_id, product_name, today_offer_count, today_factory_count
FROM stat_brand_product
WHERE REPLACE(brand_name, ' ', '') = 'JBS'
  AND REPLACE(product_name, ' ', '') = '前腱'
  AND stat_date = CURRENT_DATE
ORDER BY today_offer_count DESC
""")
rows = cur.fetchall()
print(f"=== stat_brand_product rows for JBS 前腱 today: {len(rows)} rows ===")
total = 0
for r in rows:
    print(r)
    total += r[5]
print(f"SUM today_offer_count = {total}")

# What does the SQL query return?
cur.execute("""
SELECT
    MAX(brand_name) AS brand_name,
    MAX(product_id) AS product_id,
    MAX(product_name) AS product_name,
    SUM(today_factory_count) AS today_factory_count,
    SUM(today_offer_count) AS today_offer_count
FROM stat_brand_product
WHERE stat_date = CURRENT_DATE
  AND REPLACE(brand_name, ' ', '') = 'JBS'
  AND REPLACE(product_name, ' ', '') = '前腱'
  AND today_offer_count >= 0
GROUP BY REPLACE(brand_name, ' ', ''), REPLACE(product_name, ' ', '')
""")
print("\n=== Result of selectAggregatedByBrandNameAndProductName SQL ===")
for r in cur.fetchall():
    print(r)

# Check how many distinct product_ids for JBS 前腱
cur.execute("""
SELECT product_id, product_name, COUNT(*) as cnt, SUM(today_offer_count) as total
FROM stat_brand_product
WHERE stat_date = CURRENT_DATE
  AND REPLACE(brand_name, ' ', '') = 'JBS'
  AND REPLACE(product_name, ' ', '') = '前腱'
GROUP BY product_id, product_name
ORDER BY total DESC
""")
print("\n=== product_id breakdown for JBS 前腱 ===")
for r in cur.fetchall():
    print(r)

# biz_offer direct count
cur.execute("""
SELECT COUNT(*) FROM biz_offer
WHERE REPLACE(brand_name, ' ', '') = 'JBS'
  AND REPLACE(product_name, ' ', '') = '前腱'
  AND data_date = CURRENT_DATE
  AND status = 'ACTIVE'
""")
print(f"\n=== biz_offer direct count for JBS 前腱 today: {cur.fetchone()[0]} ===")

conn.close()
