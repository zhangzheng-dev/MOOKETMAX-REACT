package com.mooket.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mooket.social.entity.StatBrandProduct;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
/* loaded from: temp_jar_download.jar:BOOT-INF/classes/com/mooket/social/mapper/StatBrandProductMapper.class */
public interface StatBrandProductMapper extends BaseMapper<StatBrandProduct> {
    @Insert({"<script>INSERT INTO stat_brand_product (stat_date, brand_id, brand_name, product_id, product_name, today_factory_count, today_offer_count, price_min, price_max, avg_price, avg_price_yesterday, price_change, price_change_rate, update_time) VALUES <foreach collection='list' item='item' separator=','>(#{item.statDate}, #{item.brandId}, #{item.brandName}, #{item.productId}, #{item.productName}, #{item.todayFactoryCount}, #{item.todayOfferCount}, #{item.priceMin}, #{item.priceMax}, #{item.avgPrice}, #{item.avgPriceYesterday}, #{item.priceChange}, #{item.priceChangeRate}, #{item.updateTime})</foreach> ON CONFLICT (stat_date, brand_id, product_id) DO UPDATE SET today_factory_count = EXCLUDED.today_factory_count, today_offer_count = EXCLUDED.today_offer_count, price_min = EXCLUDED.price_min, price_max = EXCLUDED.price_max, avg_price = EXCLUDED.avg_price, avg_price_yesterday = EXCLUDED.avg_price_yesterday, price_change = EXCLUDED.price_change, price_change_rate = EXCLUDED.price_change_rate, update_time = EXCLUDED.update_time</script>"})
    void batchUpsert(@Param("list") List<StatBrandProduct> stats);

    @Delete({"DELETE FROM stat_brand_product WHERE stat_date = #{statDate}"})
    void deleteByDate(@Param("statDate") LocalDate statDate);

    @Select({"SELECT brand_id, brand_name, product_id, product_name, today_factory_count, today_offer_count, price_min, price_max, avg_price, avg_price_yesterday, price_change, price_change_rate FROM stat_brand_product WHERE stat_date = #{statDate} AND today_offer_count >= 10 ORDER BY today_offer_count DESC LIMIT #{limit}"})
    @Results({@Result(property = "brandId", column = "brand_id"), @Result(property = "brandName", column = "brand_name"), @Result(property = "productId", column = "product_id"), @Result(property = "productName", column = "product_name"), @Result(property = "todayFactoryCount", column = "today_factory_count"), @Result(property = "todayOfferCount", column = "today_offer_count"), @Result(property = "priceMin", column = "price_min"), @Result(property = "priceMax", column = "price_max"), @Result(property = "avgPrice", column = "avg_price"), @Result(property = "avgPriceYesterday", column = "avg_price_yesterday"), @Result(property = "priceChange", column = "price_change"), @Result(property = "priceChangeRate", column = "price_change_rate")})
    List<StatBrandProduct> findHotBrandProducts(@Param("statDate") LocalDate statDate, @Param("limit") int limit);

    @Select({"SELECT brand_id, brand_name, product_id, product_name, today_factory_count, today_offer_count, price_min, price_max, avg_price, avg_price_yesterday, price_change, price_change_rate FROM stat_brand_product WHERE brand_id = #{brandId} AND product_id = #{productId} ORDER BY stat_date DESC LIMIT 1"})
    @Results({@Result(property = "brandId", column = "brand_id"), @Result(property = "brandName", column = "brand_name"), @Result(property = "productId", column = "product_id"), @Result(property = "productName", column = "product_name"), @Result(property = "todayFactoryCount", column = "today_factory_count"), @Result(property = "todayOfferCount", column = "today_offer_count"), @Result(property = "priceMin", column = "price_min"), @Result(property = "priceMax", column = "price_max"), @Result(property = "avgPrice", column = "avg_price"), @Result(property = "avgPriceYesterday", column = "avg_price_yesterday"), @Result(property = "priceChange", column = "price_change"), @Result(property = "priceChangeRate", column = "price_change_rate")})
    StatBrandProduct selectByBrandIdAndProductId(@Param("brandId") Integer brandId, @Param("productId") Integer productId);
}