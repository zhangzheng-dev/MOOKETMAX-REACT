package com.mooket.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 平替产品 DTO
 */
data class SubstituteProduct(
    val category: String,
    val productName: String,
    val currentFactoryNo: String,
    val tier: String,
    val priceMin: Double?,
    val priceMax: Double?,
    val offerCount: Long,
    val merchantCount: Int,
    val factories: List<SubstituteFactory>
)

data class SubstituteFactory(
    val factoryNo: String,
    val priceMin: Double?,
    val priceMax: Double?,
    val offerCount: Long,
    val merchantCount: Int,
    val isSelected: Boolean
)

/**
 * 平替产品详情
 */
data class SubstituteProductDetail(
    val country: String,
    val factoryNo: String,
    val productName: String,
    val tier: String?,
    val productId: Int?,
    val priceMin: Double?,
    val priceMax: Double?,
    val priceChange: Double?,
    val priceChangeRate: Double?,
    val offerCount: Long,
    val inquiryCount: Long,
    val merchantCount: Int,
    val priceHistory7Days: List<DailyPrice>,
    val priceHistory30Days: List<DailyPrice>,
    val merchantOffers: List<MerchantOfferGroup>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)