import psycopg2
conn = psycopg2.connect('postgresql://mooketmax_dba:MooketMax%402024%21@43.139.56.124:30032/mooket_db')
cur = conn.cursor()

# Check JBS 前腱 stat rows for both dates
cur.execute("""
SELECT stat_date, brand_id, brand_name, product_name, today_offer_count, today_factory_count,
       price_min, price_max, avg_price, avg_price_yesterday, price_change, price_change_rate
FROM stat_brand_product
WHERE REPLACE(brand_name, ' ', '') = 'JBS'
  AND REPLACE(product_name, ' ', '') = '前腱'
  AND stat_date IN (CURRENT_DATE, CURRENT_DATE - 1)
ORDER BY stat_date DESC, brand_id
""")
print("=== stat_brand_product JBS 前腱 (today + yesterday) ===")
for r in cur.fetchall():
    print(r)

# Check the SQL result for aggregated query
cur.execute("""
SELECT
    MAX(brand_name) AS brand_name,
    MAX(product_id) AS product_id,
    MAX(product_name) AS product_name,
    SUM(today_factory_count) AS today_factory_count,
    SUM(today_offer_count) AS today_offer_count,
    MIN(price_min) AS price_min,
    MAX(price_max) AS price_max,
    CASE WHEN SUM(today_offer_count) > 0 THEN ROUND(SUM(avg_price * today_offer_count) / SUM(today_offer_count), 4) ELSE 0 END AS avg_price,
    CASE WHEN SUM(today_offer_count) > 0 THEN ROUND(SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count), 4) ELSE 0 END AS avg_price_yesterday,
    CASE WHEN SUM(today_offer_count) > 0 THEN ROUND((SUM(avg_price * today_offer_count) / SUM(today_offer_count)) - (SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count)), 4) ELSE 0 END AS price_change,
    CASE WHEN SUM(today_offer_count) > 0 AND SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count) != 0 THEN ROUND(((SUM(avg_price * today_offer_count) / SUM(today_offer_count)) - (SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count))) / (SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count)) * 100, 2) ELSE 0 END AS price_change_rate
FROM stat_brand_product
WHERE stat_date = CURRENT_DATE
  AND REPLACE(brand_name, ' ', '') = 'JBS'
  AND REPLACE(product_name, ' ', '') = '前腱'
  AND today_offer_count >= 0
GROUP BY REPLACE(brand_name, ' ', ''), REPLACE(product_name, ' ', '')
""")
print("\n=== Aggregated SQL result (stat_date = CURRENT_DATE) ===")
for r in cur.fetchall():
    print(r)

# Without date filter
cur.execute("""
SELECT
    MAX(brand_name) AS brand_name,
    MAX(product_id) AS product_id,
    MAX(product_name) AS product_name,
    SUM(today_factory_count) AS today_factory_count,
    SUM(today_offer_count) AS today_offer_count,
    MIN(price_min) AS price_min,
    MAX(price_max) AS price_max,
    CASE WHEN SUM(today_offer_count) > 0 THEN ROUND(SUM(avg_price * today_offer_count) / SUM(today_offer_count), 4) ELSE 0 END AS avg_price,
    CASE WHEN SUM(today_offer_count) > 0 THEN ROUND(SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count), 4) ELSE 0 END AS avg_price_yesterday,
    CASE WHEN SUM(today_offer_count) > 0 THEN ROUND((SUM(avg_price * today_offer_count) / SUM(today_offer_count)) - (SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count)), 4) ELSE 0 END AS price_change,
    CASE WHEN SUM(today_offer_count) > 0 AND SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count) != 0 THEN ROUND(((SUM(avg_price * today_offer_count) / SUM(today_offer_count)) - (SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count))) / (SUM(avg_price_yesterday * today_offer_count) / SUM(today_offer_count)) * 100, 2) ELSE 0 END AS price_change_rate
FROM stat_brand_product
WHERE REPLACE(brand_name, ' ', '') = 'JBS'
  AND REPLACE(product_name, ' ', '') = '前腱'
  AND today_offer_count >= 0
GROUP BY REPLACE(brand_name, ' ', ''), REPLACE(product_name, ' ', '')
""")
print("\n=== Aggregated SQL result (ALL dates) ===")
for r in cur.fetchall():
    print(r)

conn.close()
