package com.mooket.app.data.repository

import com.mooket.app.data.api.RetrofitClient
import com.mooket.app.data.model.*

/**
 * 牧集数据仓库
 */
class MooketRepository {

    private val apiService = RetrofitClient.apiService

    /**
     * 获取商家详情
     */
    suspend fun getMerchantDetail(merchantId: Long, category: String): Result<MerchantDetail> {
        return try {
            val response = apiService.getMerchantDetail(merchantId, category)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 分页获取商家产品列表
     * @param type offer 或 inquiry
     */
    suspend fun getMerchantProducts(
        merchantId: Long,
        type: String,
        category: String,
        page: Int,
        pageSize: Int
    ): Result<MerchantProductPage> {
        return try {
            val response = apiService.getMerchantProducts(merchantId, type, category, page, pageSize)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取搜索联想词
     */
    suspend fun getSearchSuggestions(category: String, keyword: String): Result<List<SearchSuggest>> {
        return try {
            val response = apiService.getSearchSuggestions(category, keyword)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取厂号筛选数据
     */
    suspend fun getFactoryFilter(category: String = "牛"): Result<FactoryFilter> {
        return try {
            val response = apiService.getFactoryFilter(category)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取产品详情
     * @param type offer 或 inquiry
     * @param sortBy comprehensive 或 price
     */
    suspend fun getProductDetail(
        productId: Int,
        category: String?,
        type: String = "offer",
        sortBy: String = "comprehensive",
        page: Int = 1,
        pageSize: Int = 10
    ): Result<ProductDetail> {
        return try {
            val response = apiService.getProductDetail(productId, category, type, sortBy, page, pageSize)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取国家详情
     * @param type offer 或 inquiry
     * @param sortBy comprehensive 或 price
     */
    suspend fun getCountryDetail(
        country: String,
        category: String?,
        type: String = "offer",
        sortBy: String = "comprehensive",
        page: Int = 1,
        pageSize: Int = 10
    ): Result<CountryDetail> {
        return try {
            val response = apiService.getCountryDetail(country, category, type, sortBy, page, pageSize)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取品牌详情
     * @param brandName 品牌名称
     * @param category 品类（牛/猪）
     * @param type offer 或 inquiry
     * @param sortBy comprehensive 或 price
     */
    suspend fun getBrandDetail(
        brandName: String,
        category: String?,
        type: String = "offer",
        sortBy: String = "comprehensive",
        page: Int = 1,
        pageSize: Int = 10
    ): Result<BrandDetail> {
        return try {
            val response = apiService.getBrandDetail(brandName, category, type, sortBy, page, pageSize)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取品牌+产品详情
     * @param brandName 品牌名称
     * @param productName 产品名称
     * @param category 品类
     * @param type offer 或 inquiry
     * @param sortBy comprehensive 或 price
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     */
    suspend fun getBrandProductDetail(
        brandName: String,
        productName: String,
        category: String?,
        type: String = "offer",
        sortBy: String = "comprehensive",
        page: Int = 1,
        pageSize: Int = 10
    ): Result<BrandProductDetailResult> {
        return try {
            val response = apiService.getBrandProductDetail(brandName, productName, category, type, sortBy, page, pageSize)
            if (response.code == 200 && response.data != null) {
                val data = response.data
                // 看板数据直接使用 API 返回的聚合值（后端从全量 aggList 计算，不受排序/分页影响）
                val result = BrandProductDetailResult(
                    brandName = data.brandName,
                    factoryCount = data.factoryCount,
                    productCount = data.productCount,
                    merchantCount = data.merchantCount ?: 0,
                    priceMin = data.priceMin,
                    priceMax = data.priceMax,
                    todayOfferCount = data.todayOfferCount,
                    yesterdayOfferCount = data.yesterdayOfferCount,
                    todayInquiryCount = data.todayInquiryCount,
                    yesterdayInquiryCount = data.yesterdayInquiryCount,
                    summaries = data.summaries,
                    totalCount = data.totalCount,
                    page = data.page,
                    pageSize = data.pageSize,
                    totalPages = data.totalPages
                )
                Result.success(result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取厂号详情
     * @param country 国家
     * @param factoryNo 厂号
     * @param category 品类（牛/猪）
     * @param type offer 或 inquiry
     * @param sortBy comprehensive 或 price_asc/price_desc
     */
    suspend fun getFactoryDetail(
        country: String,
        factoryNo: String,
        category: String?,
        type: String = "offer",
        sortBy: String = "comprehensive",
        page: Int = 1,
        pageSize: Int = 10
    ): Result<FactoryDetail> {
        return try {
            val response = apiService.getFactoryDetail(country, factoryNo, category, type, sortBy, page, pageSize)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取价格趋势（近30天历史 + 当天实时）
     * @param type 维度类型: country_product / country_factory_product
     * @param country 国家
     * @param productId 产品ID
     * @param factoryNo 厂号（可为空）
     * @param offerType 报盘/求购: 报盘 / 求购
     */
    suspend fun getPriceTrend(
        type: String,
        country: String,
        productId: Int,
        factoryNo: String?,
        offerType: String
    ): Result<PriceTrend> {
        return try {
            val response = apiService.getPriceTrend(type, country, productId, factoryNo, offerType)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取多厂号价格对比数据
     */
    suspend fun getFactoryPriceComparison(
        country: String,
        factoryNos: List<String>,
        productName: String,
        category: String = "牛",
        offerType: String = "报盘",
        days: Int = 30
    ): Result<FactoryPriceComparison> {
        return try {
            val response = apiService.getFactoryPriceComparison(
                country,
                factoryNos.joinToString(","),
                productName,
                category,
                offerType,
                days
            )
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取热门搜索推荐
     */
    suspend fun getHotSearchRecommendations(category: String = "牛"): Result<List<HotSearchItem>> {
        return try {
            val response = apiService.getHotSearchRecommendations(category)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取首页统计数据
     */
    suspend fun getHomeStatData(category: String = "牛"): Result<HomeStatData> {
        return try {
            val response = apiService.getHomeStatData(category)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取首页卡片数据
     */
    suspend fun getHomeCards(category: String = "牛"): Result<HomeCardsResponse> {
        return try {
            val response = apiService.getHomeCards(category)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取最近搜索记录
     */
    suspend fun getRecentSearches(limit: Int = 200): Result<List<SearchHistory>> {
        return try {
            val response = apiService.getRecentSearches(limit)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取自选搜索记录
     */
    suspend fun getSelfSelectSearches(limit: Int = 200): Result<List<SearchHistory>> {
        return try {
            val response = apiService.getSelfSelectSearches(limit)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 添加搜索记录
     */
    suspend fun addSearchHistory(searchWord: String, searchType: String): Result<Unit> {
        return try {
            val response = apiService.addSearchHistory(searchWord, searchType)
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取最近搜索的卡片数据（带完整统计信息）
     */
    suspend fun getRecentSearchCards(category: String = "牛"): Result<HomeCardsResponse> {
        return try {
            val response = apiService.getRecentSearchCards(category)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取自选搜索的卡片数据（带完整统计信息）
     */
    suspend fun getSelfSelectCards(category: String = "牛"): Result<HomeCardsResponse> {
        return try {
            val response = apiService.getSelfSelectCards(category)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 保存搜索历史到服务器（支持自选标记）
     */
    suspend fun saveSearchHistory(
        searchWord: String,
        searchType: String,
        isSelfSelect: Int = 0,
        productId: Long? = null,
        productName: String? = null,
        country: String? = null,
        factoryNo: String? = null,
        brandId: Long? = null,
        merchantId: Long? = null
    ): Result<Unit> {
        return try {
            val response = apiService.saveSearchHistory(
                searchWord = searchWord,
                searchType = searchType,
                isSelfSelect = isSelfSelect,
                productId = productId,
                productName = productName,
                country = country,
                factoryNo = factoryNo,
                brandId = brandId,
                merchantId = merchantId
            )
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除搜索记录
     */
    suspend fun deleteSearchHistory(historyId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteSearchHistory(historyId)
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 将历史记录移动到自选
     */
    suspend fun moveToSelfSelect(historyId: Long): Result<Unit> {
        return try {
            val response = apiService.moveToSelfSelect(historyId)
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 添加自选
     */
    suspend fun addSelfSelect(searchWord: String, searchType: String): Result<Unit> {
        return try {
            val response = apiService.addSelfSelect(searchWord, searchType)
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 取消自选
     */
    suspend fun cancelSelfSelect(historyId: Long): Result<Unit> {
        return try {
            val response = apiService.cancelSelfSelect(historyId)
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
