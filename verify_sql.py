import psycopg2
conn = psycopg2.connect('postgresql://mooketmax_dba:MooketMax%402024%21@43.139.56.124:30032/mooket_db')
cur = conn.cursor()

cur.execute("""
SELECT
    MAX(t.brand_id) AS brand_id,
    MAX(t.brand_name) AS brand_name,
    MAX(t.product_id) AS product_id,
    MAX(t.product_name) AS product_name,
    SUM(t.today_factory_count) AS today_factory_count,
    SUM(t.today_offer_count) AS today_offer_count,
    MIN(t.price_min) AS price_min,
    MAX(t.price_max) AS price_max,
    CASE WHEN SUM(t.today_offer_count) > 0 THEN ROUND(SUM(t.avg_price * t.today_offer_count) / SUM(t.today_offer_count), 4) ELSE 0 END AS avg_price,
    CASE WHEN MAX(y.yesterday_total_count) > 0 THEN ROUND(SUM(y.yesterday_weighted_price) / SUM(y.yesterday_total_count), 4) ELSE 0 END AS avg_price_yesterday,
    CASE WHEN SUM(t.today_offer_count) > 0 AND MAX(y.yesterday_total_count) > 0 THEN ROUND((SUM(t.avg_price * t.today_offer_count) / SUM(t.today_offer_count)) - (SUM(y.yesterday_weighted_price) / SUM(y.yesterday_total_count)), 4) ELSE 0 END AS price_change,
    CASE WHEN SUM(t.today_offer_count) > 0 AND MAX(y.yesterday_total_count) > 0 AND (SUM(y.yesterday_weighted_price) / SUM(y.yesterday_total_count)) != 0 THEN ROUND(((SUM(t.avg_price * t.today_offer_count) / SUM(t.today_offer_count)) - (SUM(y.yesterday_weighted_price) / SUM(y.yesterday_total_count))) / (SUM(y.yesterday_weighted_price) / SUM(y.yesterday_total_count)) * 100, 2) ELSE 0 END AS price_change_rate
FROM stat_brand_product t
LEFT JOIN (
    SELECT brand_id, product_id,
        SUM(avg_price_yesterday * today_offer_count) AS yesterday_weighted_price,
        SUM(today_offer_count) AS yesterday_total_count
    FROM stat_brand_product
    WHERE stat_date = CURRENT_DATE - 1
      AND REPLACE(brand_name, ' ', '') = 'JBS'
      AND REPLACE(product_name, ' ', '') = '前腱'
      AND today_offer_count >= 0
    GROUP BY brand_id, product_id
) y ON t.brand_id = y.brand_id AND t.product_id = y.product_id
WHERE t.stat_date = CURRENT_DATE
  AND REPLACE(t.brand_name, ' ', '') = 'JBS'
  AND REPLACE(t.product_name, ' ', '') = '前腱'
  AND t.today_offer_count >= 0
""")
row = cur.fetchone()
print(f"brand_name={row[1]}, product_name={row[3]}")
print(f"today_factory_count={row[4]}, today_offer_count={row[5]}")
print(f"price_min={row[6]}, price_max={row[7]}")
print(f"avg_price={row[8]}, avg_price_yesterday={row[9]}")
print(f"price_change={row[10]}, price_change_rate={row[11]}%")
conn.close()
