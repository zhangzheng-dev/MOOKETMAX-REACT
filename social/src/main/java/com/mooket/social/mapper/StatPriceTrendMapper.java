package com.mooket.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mooket.social.entity.StatPriceTrend;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/mapper/StatPriceTrendMapper.class */
public interface StatPriceTrendMapper extends BaseMapper<StatPriceTrend> {
    public static final String DIMENSION_COUNTRY_PRODUCT = "country_product";
    public static final String DIMENSION_COUNTRY_FACTORY_PRODUCT = "country_factory_product";
    public static final String DIMENSION_BRAND_PRODUCT = "brand_product";

    /* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/mapper/StatPriceTrendMapper$PriceTrendPoint.class */
    public static class PriceTrendPoint {
        public LocalDate date;
        public BigDecimal avgPrice;
        public Integer offerCount;
    }

    @Insert({"INSERT INTO stat_price_trend (stat_date, dimension_type, country, product_id, product_name, factory_no, offer_type, avg_price, record_date, created_at, updated_at) VALUES (#{statDate}, #{dimensionType}, #{country}, #{productId}, #{productName}, #{factoryNo}, #{offerType}, #{avgPrice}, #{recordDate}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (stat_date, dimension_type, country, product_id, factory_no, offer_type) DO UPDATE SET avg_price = EXCLUDED.avg_price, updated_at = CURRENT_TIMESTAMP"})
    int upsertPriceTrend(@Param("statDate") LocalDate statDate, @Param("dimensionType") String dimensionType, @Param("country") String country, @Param("productId") Integer productId, @Param("productName") String productName, @Param("factoryNo") String factoryNo, @Param("offerType") String offerType, @Param("avgPrice") BigDecimal avgPrice, @Param("recordDate") LocalDate recordDate);

    @Select({"SELECT stat_date, avg_price FROM stat_price_trend WHERE dimension_type = #{dimensionType} AND country = #{country} AND product_id = #{productId} AND factory_no = '' AND offer_type = #{offerType} AND stat_date >= CURRENT_DATE - INTERVAL '29 day' ORDER BY stat_date ASC"})
    @Results({@Result(property = "date", column = "stat_date"), @Result(property = "avgPrice", column = "avg_price")})
    List<PriceTrendPoint> selectTrendPointsByCountryProduct(@Param("dimensionType") String dimensionType, @Param("country") String country, @Param("productId") Integer productId, @Param("offerType") String offerType);

    @Select({"SELECT stat_date, avg_price FROM stat_price_trend WHERE dimension_type = #{dimensionType} AND country = #{country} AND product_id = #{productId} AND factory_no = #{factoryNo} AND offer_type = #{offerType} AND stat_date >= CURRENT_DATE - INTERVAL '29 day' ORDER BY stat_date ASC"})
    @Results({@Result(property = "date", column = "stat_date"), @Result(property = "avgPrice", column = "avg_price")})
    List<PriceTrendPoint> selectTrendPointsByCountryFactoryProduct(@Param("dimensionType") String dimensionType, @Param("country") String country, @Param("productId") Integer productId, @Param("factoryNo") String factoryNo, @Param("offerType") String offerType);

    @Delete({"DELETE FROM stat_price_trend WHERE stat_date < CURRENT_DATE - INTERVAL '30 day'"})
    int deleteOldRecords();

    @Select({"SELECT stat_date, avg_price FROM stat_price_trend WHERE dimension_type = #{dimensionType} AND country = #{country} AND product_id = #{productId} AND factory_no = #{factoryNo} AND offer_type = #{offerType} AND stat_date = #{targetDate} - INTERVAL '1 day'"})
    @Results({@Result(property = "date", column = "stat_date"), @Result(property = "avgPrice", column = "avg_price")})
    PriceTrendPoint selectPreviousDayPrice(@Param("dimensionType") String dimensionType, @Param("country") String country, @Param("productId") Integer productId, @Param("factoryNo") String factoryNo, @Param("offerType") String offerType, @Param("targetDate") LocalDate targetDate);

    @Select({"SELECT stat_date, avg_price FROM stat_price_trend WHERE dimension_type = #{dimensionType} AND country = #{country} AND product_id = #{productId} AND factory_no = #{factoryNo} AND offer_type = #{offerType} AND stat_date = #{targetDate}"})
    @Results({@Result(property = "date", column = "stat_date"), @Result(property = "avgPrice", column = "avg_price")})
    PriceTrendPoint selectPricePoint(@Param("dimensionType") String dimensionType, @Param("country") String country, @Param("productId") Integer productId, @Param("factoryNo") String factoryNo, @Param("offerType") String offerType, @Param("targetDate") LocalDate targetDate);

    @Select({"SELECT t.stat_date, t.avg_price, COALESCE(cnt.offer_count, 0) as offer_count FROM stat_price_trend t LEFT JOIN (  SELECT data_date as offer_date, COUNT(*) as offer_count   FROM biz_offer   WHERE country = #{country} AND factory_no = #{factoryNo} AND product_name = #{productName}   AND offer_type = #{offerType} AND data_date >= CURRENT_DATE - INTERVAL '29 day'   GROUP BY data_date) cnt ON t.stat_date = cnt.offer_date WHERE t.dimension_type = #{dimensionType} AND t.country = #{country} AND t.product_id = #{productId} AND t.factory_no = #{factoryNo} AND t.offer_type = #{offerType} AND t.stat_date >= CURRENT_DATE - INTERVAL '29 day' ORDER BY t.stat_date ASC"})
    @Results({@Result(property = "date", column = "stat_date"), @Result(property = "avgPrice", column = "avg_price"), @Result(property = "offerCount", column = "offer_count")})
    List<PriceTrendPoint> selectTrendPointsWithOfferCount(@Param("dimensionType") String dimensionType, @Param("country") String country, @Param("productId") Integer productId, @Param("factoryNo") String factoryNo, @Param("productName") String productName, @Param("offerType") String offerType);

    @Select({"SELECT stat_date, avg_price FROM stat_price_trend WHERE dimension_type = #{dimensionType} AND country = #{brandName} AND product_id = #{productId} AND factory_no = '' AND offer_type = #{offerType} AND stat_date >= CURRENT_DATE - INTERVAL '29 day' ORDER BY stat_date ASC"})
    @Results({@Result(property = "date", column = "stat_date"), @Result(property = "avgPrice", column = "avg_price")})
    List<PriceTrendPoint> selectTrendPointsByBrandProduct(@Param("dimensionType") String dimensionType, @Param("brandName") String brandName, @Param("productId") Integer productId, @Param("offerType") String offerType);
}