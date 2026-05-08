package com.mooket.social.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mooket.social.dto.*;
import com.mooket.social.entity.*;
import com.mooket.social.mapper.*;
import com.mooket.social.service.BrandService;
import com.mooket.social.service.SearchHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索历史服务实现
 */
@Slf4j
@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private BizSearchHistoryMapper searchHistoryMapper;

    @Autowired
    private DictProductMapper productMapper;

    @Autowired
    private DictFactoryMapper factoryMapper;

    @Autowired
    private DictBrandMapper brandMapper;

    @Autowired
    private DictMerchantMapper merchantMapper;

    @Autowired
    private StatProductMapper statProductMapper;

    @Autowired
    private StatCountryMapper statCountryMapper;

    @Autowired
    private StatFactoryMapper statFactoryMapper;

    @Autowired
    private StatBrandMapper statBrandMapper;

    @Autowired
    private StatCountryProductMapper statCountryProductMapper;

    @Autowired
    private StatBrandProductMapper statBrandProductMapper;

    @Autowired
    private StatFactoryProductMapper statFactoryProductMapper;

    @Autowired
    private StatMerchantMapper statMerchantMapper;

    @Autowired
    private StatPriceTrendMapper statPriceTrendMapper;

    @Autowired
    private BizOfferMapper bizOfferMapper;

    @Autowired
    private BrandService brandService;

    @Override
    @Transactional
    public void addSearchHistory(Long userId, String searchWord, String searchType) {
        Long existingId = searchHistoryMapper.findExistingHistory(userId, searchWord, searchType);
        if (existingId != null) {
            searchHistoryMapper.updateCreateTime(existingId);
        } else {
            searchHistoryMapper.insertOrIgnore(userId, searchWord, searchType, 0);
        }
    }

    @Override
    public List<SearchHistoryDTO> getRecentSearches(Long userId, int limit) {
        List<BizSearchHistory> histories = searchHistoryMapper.findRecentSearches(userId, limit);
        return convertToDTO(histories);
    }

    @Override
    public List<SearchHistoryDTO> getSelfSelectSearches(Long userId, int limit) {
        List<BizSearchHistory> histories = searchHistoryMapper.findSelfSelectSearches(userId, limit);
        return convertToDTO(histories);
    }

    @Override
    public HomeCardsResponseDTO getRecentSearchCards(Long userId, String category) {
        List<BizSearchHistory> histories = searchHistoryMapper.findRecentSearches(userId, 50);
        return buildCardsFromHistory(histories, category);
    }

    @Override
    public HomeCardsResponseDTO getSelfSelectCards(Long userId, String category) {
        List<BizSearchHistory> histories = searchHistoryMapper.findSelfSelectSearches(userId, 50);
        return buildCardsFromHistory(histories, category);
    }

    private HomeCardsResponseDTO buildCardsFromHistory(List<BizSearchHistory> histories, String category) {
        HomeCardsResponseDTO response = new HomeCardsResponseDTO();
        List<HomeCardItemDTO> cards = new ArrayList<>();
        int rank = 1;
        LocalDate today = LocalDate.now();

        for (BizSearchHistory history : histories) {
            HomeCardItemDTO card = buildCardFromHistory(history, category, today, rank);
            if (card != null) {
                cards.add(card);
                rank++;
            }
            if (cards.size() >= 20) break; // 最多20张卡片
        }

        response.setCards(cards);
        response.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        return response;
    }

    private HomeCardItemDTO buildCardFromHistory(BizSearchHistory history, String category, LocalDate today, int rank) {
        String type = history.getSearchType();
        if (type == null) return null;

        try {
            // 规范化类型名称（兼容"国家+厂号"和"国家厂号"两种格式）
            String normalizedType = type.replace("+", "");

            switch (normalizedType) {
                case "产品":
                    return buildProductCard(history, category, today, rank);
                case "国家":
                    return buildCountryCard(history, category, today, rank);
                case "品牌":
                    return buildBrandCard(history, category, today, rank);
                case "商家":
                    return buildMerchantCard(history, today, rank);
                case "国家厂号":
                    return buildFactoryCard(history, category, today, rank);
                case "国家产品":
                    return buildCountryProductCard(history, category, today, rank);
                case "品牌产品":
                    return buildBrandProductCard(history, category, today, rank);
                case "国家厂号产品":
                    return buildFactoryProductCard(history, category, today, rank);
                default:
                    // 兼容：检查 searchWord 是否包含特定模式
                    String keyword = history.getSearchWord();
                    if (keyword != null) {
                        if (type.contains("厂号") && type.contains("产品")) {
                            return buildFactoryProductCard(history, category, today, rank);
                        } else if (type.contains("国家") && type.contains("产品")) {
                            return buildCountryProductCard(history, category, today, rank);
                        } else if (type.contains("品牌") && type.contains("产品")) {
                            return buildBrandProductCard(history, category, today, rank);
                        } else if (type.contains("厂号")) {
                            return buildFactoryCard(history, category, today, rank);
                        }
                    }
                    return null;
            }
        } catch (Exception e) {
            log.warn("构建卡片失败: historyId={}, type={}, error={}", history.getHistoryId(), type, e.getMessage());
            return null;
        }
    }

    private ProductCardDTO buildProductCard(BizSearchHistory history, String category, LocalDate today, int rank) {
        Integer productId = history.getProductId() != null ? history.getProductId().intValue() : null;
        if (productId == null) {
            String productName = history.getSearchWord();
            // 如果搜索词包含"别名："格式，提取标准产品名（括号前的部分）
            if (productName != null && productName.contains("(别名：")) {
                int idx = productName.indexOf("(别名：");
                productName = productName.substring(0, idx);
            }
            DictProduct product = productMapper.selectByProductName(productName);
            if (product != null) {
                productId = product.getProductId();
            }
        }
        if (productId == null) return null;

        StatProduct stat = statProductMapper.selectByProductIdAndCategory(productId, category);
        if (stat == null) return null;

        ProductCardDTO card = new ProductCardDTO();
        card.setCardType("product");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setProductId(productId);
        card.setProductName(stat.getProductName() != null ? stat.getProductName() : history.getSearchWord());
        card.setTodayOfferCount(stat.getTodayOfferCount());
        card.setMerchantCount(stat.getTodayMerchantCount());
        card.setFactoryCount(stat.getTodayFactoryCount());
        card.setPriceMin(stat.getPriceMin());
        card.setPriceMax(stat.getPriceMax());
        return card;
    }

    private CountryCardDTO buildCountryCard(BizSearchHistory history, String category, LocalDate today, int rank) {
        String country = history.getCountry();
        if (country == null) {
            country = history.getSearchWord();
        }

        StatCountry stat = statCountryMapper.selectByCountryAndCategory(country, category);
        // 如果直接查询为空，尝试模糊匹配（处理搜索词和标准名不完全匹配的情况）
        if (stat == null && country != null) {
            stat = statCountryMapper.selectByCountryKeyword(country, category);
        }
        if (stat == null) return null;

        CountryCardDTO card = new CountryCardDTO();
        card.setCardType("country");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setCountry(stat.getCountry());
        card.setCountryAlias(stat.getCountry());
        card.setTodayOfferCount(stat.getTodayOfferCount());

        // 如果 stat_country 表中的 hotFactories 为空，直接从 biz_offer 查询
        List<CountryCardDTO.HotFactoryDTO> hotFactories = parseHotFactoriesFromJson(stat.getHotFactories());
        if (hotFactories.isEmpty()) {
            hotFactories = queryHotFactoriesFromOffers(country, category, 3);
        }
        card.setHotFactories(hotFactories);

        // 如果 stat_country 表中的 hotProducts 为空，直接从 biz_offer 查询
        List<CountryCardDTO.HotProductDTO> hotProducts = parseHotProductsFromJson(stat.getHotProducts());
        if (hotProducts.isEmpty()) {
            hotProducts = queryHotProductsFromOffers(country, category, 3);
        }
        card.setHotProducts(hotProducts);
        return card;
    }

    /**
     * 从 biz_offer 表查询热门厂号
     */
    private List<CountryCardDTO.HotFactoryDTO> queryHotFactoriesFromOffers(String country, String category, int limit) {
        List<CountryCardDTO.HotFactoryDTO> result = new ArrayList<>();
        try {
            List<BizOfferMapper.HotFactoryAgg> hotFactories = bizOfferMapper.selectHotFactories(country, category, "报盘");
            for (BizOfferMapper.HotFactoryAgg f : hotFactories) {
                if (result.size() >= limit) break;
                CountryCardDTO.HotFactoryDTO dto = new CountryCardDTO.HotFactoryDTO();
                dto.setFactoryNo(f.factoryNo);
                dto.setOfferCount(f.offerCount);
                result.add(dto);
            }
        } catch (Exception e) {
            log.warn("查询热门厂号失败: country={}, error={}", country, e.getMessage());
        }
        return result;
    }

    /**
     * 从 biz_offer 表查询热门产品
     */
    private List<CountryCardDTO.HotProductDTO> queryHotProductsFromOffers(String country, String category, int limit) {
        List<CountryCardDTO.HotProductDTO> result = new ArrayList<>();
        try {
            List<BizOfferMapper.HotProductAgg> hotProducts = bizOfferMapper.selectHotProducts(country, category, "报盘");
            for (BizOfferMapper.HotProductAgg p : hotProducts) {
                if (result.size() >= limit) break;
                CountryCardDTO.HotProductDTO dto = new CountryCardDTO.HotProductDTO();
                dto.setProductName(p.productName);
                dto.setOfferCount(p.offerCount);
                result.add(dto);
            }
        } catch (Exception e) {
            log.warn("查询热门产品失败: country={}, error={}", country, e.getMessage());
        }
        return result;
    }

    private BrandCardDTO buildBrandCard(BizSearchHistory history, String category, LocalDate today, int rank) {
        Long brandId = history.getBrandId();
        if (brandId == null) {
            brandMapper.findByName(history.getSearchWord()).ifPresent(b -> {
                if (b.getBrandId() != null) {
                    history.setBrandId(b.getBrandId().longValue());
                }
            });
            brandId = history.getBrandId();
        }
        if (brandId == null) return null;

        BrandCardDTO card = new BrandCardDTO();
        card.setCardType("brand");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setBrandId(brandId.intValue());

        // 优先从 stat_brand 查今日统计（满足 today_offer_count >= 10 才在 hotBrands 里）
        List<StatBrandMapper.HotBrand> hotBrands = statBrandMapper.findHotBrands(today, 100);
        for (StatBrandMapper.HotBrand hb : hotBrands) {
            if (hb.brandId != null && hb.brandId.equals(brandId.intValue())) {
                card.setBrandName(hb.brandName);
                card.setTodayOfferCount(hb.todayOfferCount);
                card.setProductCount(hb.productCount);
                card.setFactoryCount(hb.factoryCount);
                return card;
            }
        }

        // stat_brand 没有则查 dict_brand 本身（即使统计数 < 10 也展示）
        DictBrand brand = brandMapper.selectById(brandId.intValue());
        if (brand == null) return null;
        card.setBrandName(brand.getBrandName());

        // 调用 BrandService 获取实时统计（与品牌详情页一致的数据）
        try {
            BrandDetailDTO brandDetail = brandService.getBrandDetail(brand.getBrandName(), category, "offer", "comprehensive", 1, 1);
            card.setTodayOfferCount(brandDetail.getTodayOfferCount() != null ? brandDetail.getTodayOfferCount().intValue() : 0);
            card.setProductCount(brandDetail.getProductCount());
            card.setFactoryCount(brandDetail.getFactoryCount());
        } catch (Exception e) {
            log.warn("获取品牌统计失败: brandId={}, error={}", brandId, e.getMessage());
            card.setTodayOfferCount(0);
            card.setProductCount(null);
            card.setFactoryCount(null);
        }
        return card;
    }

    private MerchantCardDTO buildMerchantCard(BizSearchHistory history, LocalDate today, int rank) {
        Long merchantId = history.getMerchantId();
        if (merchantId == null) {
            String searchWord = history.getSearchWord();
            // 如果搜索词包含"别名："格式，提取标准商家名（括号前的部分）
            if (searchWord != null && searchWord.contains("(别名：")) {
                int idx = searchWord.indexOf("(别名：");
                searchWord = searchWord.substring(0, idx);
            }
            merchantMapper.findByName(searchWord).ifPresent(m -> {
                history.setMerchantId(m.getMerchantId());
            });
            merchantId = history.getMerchantId(); // Refresh after potential update
        }
        if (merchantId == null) return null;

        StatMerchant stat = statMerchantMapper.selectByMerchantIdAndDate(merchantId, today);
        if (stat == null) return null;

        DictMerchant merchant = merchantMapper.selectById(merchantId);
        MerchantCardDTO card = new MerchantCardDTO();
        card.setCardType("merchant");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setMerchantId(merchantId);
        if (merchant != null) {
            card.setMerchantName(merchant.getMerchantName() != null ? merchant.getMerchantName() : "商家-" + merchantId);
            card.setMerchantShortName(merchant.getMerchantShortName());
            card.setMerchantTags(merchant.getMerchantTags());
        } else {
            card.setMerchantName("商家-" + merchantId);
        }
        card.setTodayOfferCount(stat.getTodayOfferCount());

        // 最新报盘
        List<BizOffer> latestOffers = bizOfferMapper.findLatestByMerchant(merchantId, 2);
        List<MerchantCardDTO.LatestOfferDTO> latestOfferDTOs = new ArrayList<>();
        for (BizOffer offer : latestOffers) {
            MerchantCardDTO.LatestOfferDTO dto = new MerchantCardDTO.LatestOfferDTO();
            dto.setProductName(offer.getProductName());
            dto.setCountry(offer.getCountry());
            dto.setFactoryNo(offer.getFactoryNo());
            dto.setPrice(offer.getPrice() != null ? offer.getPrice().doubleValue() : null);
            dto.setWeight(offer.getWeight());
            dto.setPublishTime(offer.getPublishTime() != null ? offer.getPublishTime().toString() : null);
            latestOfferDTOs.add(dto);
        }
        card.setLatestOffers(latestOfferDTOs);
        return card;
    }

    private FactoryCardDTO buildFactoryCard(BizSearchHistory history, String category, LocalDate today, int rank) {
        String factoryNo = history.getFactoryNo();
        String country = history.getCountry();
        // 如果字段为 NULL，尝试从 searchWord 解析
        if (factoryNo == null || country == null) {
            String keyword = history.getSearchWord();
            int idx = parseCountryAndFactory(keyword);
            if (idx > 0) {
                country = keyword.substring(0, idx);
                factoryNo = keyword.substring(idx);
            }
        }
        if (factoryNo == null) return null;
        if (country == null) country = "";

        StatFactory stat = statFactoryMapper.selectByFactoryNoAndCategory(factoryNo, category);
        if (stat == null) return null;

        FactoryCardDTO card = new FactoryCardDTO();
        card.setCardType("factory");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setCountry(stat.getCountry() != null ? stat.getCountry() : country);
        card.setCountryAlias(stat.getCountry() != null ? stat.getCountry() : country);
        card.setFactoryNo(stat.getFactoryNo() != null ? stat.getFactoryNo() : factoryNo);
        card.setTodayOfferCount(stat.getTodayOfferCount());

        // 热门产品
        List<FactoryProductStatDTO> factoryProducts = bizOfferMapper.aggregateByFactoryProduct(today);
        List<FactoryCardDTO.HotProductDTO> hotProductsList = new ArrayList<>();
        for (FactoryProductStatDTO fp : factoryProducts) {
            if (factoryNo.equals(fp.getFactoryNo())) {
                FactoryCardDTO.HotProductDTO dto = new FactoryCardDTO.HotProductDTO();
                dto.setProductName(fp.getProductName());
                dto.setOfferCount(fp.getTodayOfferCount());
                hotProductsList.add(dto);
            }
        }
        hotProductsList.sort((a, b) -> Integer.compare(
                b.getOfferCount() != null ? b.getOfferCount() : 0,
                a.getOfferCount() != null ? a.getOfferCount() : 0));
        if (hotProductsList.size() > 3) {
            hotProductsList = hotProductsList.subList(0, 3);
        }
        card.setHotProducts(hotProductsList);
        return card;
    }

    private CountryProductCardDTO buildCountryProductCard(BizSearchHistory history, String category, LocalDate today, int rank) {
        String country = history.getCountry();
        Integer productId = history.getProductId() != null ? history.getProductId().intValue() : null;

        if (country == null) {
            country = extractCountry(history.getSearchWord());
        }
        if (productId == null) {
            // 尝试从 productName 获取
            if (history.getProductName() != null) {
                String productName = history.getProductName();
                if (productName.contains("(别名：")) {
                    int idx = productName.indexOf("(别名：");
                    productName = productName.substring(0, idx);
                }
                DictProduct product = productMapper.selectByProductName(productName);
                if (product != null) {
                    productId = product.getProductId();
                }
            }
            // 如果还是 null，尝试从 searchWord 解析产品名
            if (productId == null) {
                String productName = history.getProductName();
                if (productName == null) {
                    // 从 searchWord 提取产品名（去除国家前缀和厂号）
                    String keyword = history.getSearchWord();
                    String extractedCountry = extractCountry(keyword);
                    String remaining = keyword;
                    if (!extractedCountry.isEmpty()) {
                        remaining = keyword.substring(extractedCountry.length()).trim();
                    }
                    // 再去掉厂号部分
                    int factoryIdx = parseCountryAndFactory(remaining);
                    if (factoryIdx > 0) {
                        remaining = remaining.substring(factoryIdx).trim();
                    }
                    if (!remaining.isEmpty()) {
                        productName = remaining;
                    }
                }
                // 如果产品名包含"别名："格式，提取标准产品名（括号前的部分）
                if (productName != null && productName.contains("(别名：")) {
                    int idx = productName.indexOf("(别名：");
                    productName = productName.substring(0, idx);
                }
                if (productName != null && !productName.isEmpty()) {
                    DictProduct product = productMapper.selectByProductName(productName);
                    if (product != null) {
                        productId = product.getProductId();
                    }
                }
            }
        }
        if (country == null || country.isEmpty() || productId == null) return null;

        StatCountryProduct stat = statCountryProductMapper.selectByCountryAndProductId(country, productId, category);
        if (stat == null) return null;

        CountryProductCardDTO card = new CountryProductCardDTO();
        card.setCardType("countryProduct");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setCountry(stat.getCountry());
        card.setCountryAlias(stat.getCountry()); // 设置国家别名
        card.setProductId(stat.getProductId());
        card.setProductName(stat.getProductName());
        card.setTodayOfferCount(stat.getTodayOfferCount());
        card.setFactoryCount(stat.getTodayFactoryCount());
        card.setPriceMin(stat.getPriceMin());
        card.setPriceMax(stat.getPriceMax());

        // 查询热门工厂
        try {
            List<FactoryStatWithPriceDTO> factoryStats = bizOfferMapper.aggregateByFactoryForCountryProduct(today, country, productId);
            if (factoryStats != null && !factoryStats.isEmpty()) {
                List<CountryProductCardDTO.FactoryPriceDTO> topFactories = new ArrayList<>();
                for (FactoryStatWithPriceDTO fs : factoryStats) {
                    CountryProductCardDTO.FactoryPriceDTO dto = new CountryProductCardDTO.FactoryPriceDTO();
                    dto.setFactoryNo(fs.getFactoryNo());
                    dto.setPriceMin(fs.getPriceMin());
                    dto.setPriceMax(fs.getPriceMax());
                    topFactories.add(dto);
                }
                card.setTopFactories(topFactories);
            }
        } catch (Exception e) {
            log.warn("获取热门工厂失败: country={}, productId={}, error={}", country, productId, e.getMessage());
        }

        return card;
    }

    private BrandProductCardDTO buildBrandProductCard(BizSearchHistory history, String category, LocalDate today, int rank) {
        Long brandId = history.getBrandId();
        Integer productId = history.getProductId() != null ? history.getProductId().intValue() : null;

        if (brandId == null) {
            brandMapper.findByName(history.getSearchWord()).ifPresent(b -> {
                if (b.getBrandId() != null) {
                    history.setBrandId(b.getBrandId().longValue());
                }
            });
            brandId = history.getBrandId();
        }
        if (productId == null && history.getProductName() != null) {
            String productName = history.getProductName();
            // 如果产品名包含"别名："格式，提取标准产品名（括号前的部分）
            if (productName.contains("(别名：")) {
                int idx = productName.indexOf("(别名：");
                productName = productName.substring(0, idx);
            }
            DictProduct product = productMapper.selectByProductName(productName);
            if (product != null) {
                productId = product.getProductId();
            }
        }
        if (brandId == null || productId == null) return null;

        // 直接从 biz_offer 聚合查询，不依赖 stat_brand_product 表（2天窗口，和首页口径一致）
        BrandProductStatDTO statData = bizOfferMapper.aggregateByBrandProductById(today, brandId.intValue(), productId);
        if (statData == null) return null;

        BrandProductCardDTO card = new BrandProductCardDTO();
        card.setCardType("brandProduct");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setBrandId(statData.getBrandId());
        card.setBrandName(statData.getBrandName());
        card.setProductId(statData.getProductId());
        card.setProductName(statData.getProductName());
        card.setPriceMin(statData.getPriceMin());
        card.setPriceMax(statData.getPriceMax());
        // 计算涨跌
        if (statData.getAvgPrice() != null && statData.getAvgPriceYesterday() != null
                && statData.getAvgPriceYesterday().compareTo(java.math.BigDecimal.ZERO) > 0) {
            java.math.BigDecimal priceChange = statData.getAvgPrice().subtract(statData.getAvgPriceYesterday());
            java.math.BigDecimal rate = priceChange
                    .divide(statData.getAvgPriceYesterday(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            card.setPriceChange(priceChange);
            card.setPriceChangeRate(rate);
        }
        card.setTodayOfferCount(statData.getTodayOfferCount());

        // 热门工厂
        List<FactoryStatWithPriceDTO> factoryStats = bizOfferMapper.aggregateByFactoryForBrandProduct(today, brandId.intValue(), productId);
        List<BrandProductCardDTO.HotFactoryDTO> hotFactories = new ArrayList<>();
        for (FactoryStatWithPriceDTO fs : factoryStats) {
            BrandProductCardDTO.HotFactoryDTO dto = new BrandProductCardDTO.HotFactoryDTO();
            dto.setFactoryNo(fs.getFactoryNo());
            dto.setOfferCount(fs.getTodayOfferCount());
            dto.setPriceMin(fs.getPriceMin());
            dto.setPriceMax(fs.getPriceMax());
            hotFactories.add(dto);
        }
        card.setHotFactories(hotFactories);
        return card;
    }

    private FactoryProductCardDTO buildFactoryProductCard(BizSearchHistory history, String category, LocalDate today, int rank) {
        String factoryNo = history.getFactoryNo();
        String country = history.getCountry();
        Integer productId = history.getProductId() != null ? history.getProductId().intValue() : null;

        if (factoryNo == null || country == null) {
            String keyword = history.getSearchWord();
            String extractedCountry = extractCountry(keyword);
            if (!extractedCountry.isEmpty()) {
                country = extractedCountry;
            }
            int idx = parseCountryAndFactory(keyword);
            if (idx >= 0 && factoryNo == null) {
                factoryNo = keyword.substring(idx);
                // 如果 factoryNo 包含空格（如 "SIF112 上脑"），只取第一部分
                if (factoryNo != null && factoryNo.contains(" ")) {
                    factoryNo = factoryNo.split(" ")[0];
                }
            }
        }
        // 产品名解析：当 productId 为 null 时从 keyword 解析
        if (productId == null) {
            String keyword = history.getSearchWord();
            // 去掉国家前缀
            String extractedCountry = extractCountry(keyword);
            String remaining = keyword;
            if (!extractedCountry.isEmpty()) {
                remaining = keyword.substring(extractedCountry.length()).trim();
            }
            // 如果剩余部分包含空格，按空格分割处理
            if (remaining.contains(" ")) {
                String[] parts = remaining.split(" ");
                // 跳过第一个可能是厂号的部分，取后续部分作为产品名
                // 例如 "SIF112 上脑" -> 跳过 "SIF112"，取 "上脑"
                // 例如 "SIF112 上脑 牛腩" -> 取 "上脑"
                StringBuilder productPart = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i];
                    // 跳过包含连续数字的部分（认为是厂号的一部分）
                    if (part.length() >= 3 && part.length() <= 10 &&
                        Character.isLetter(part.charAt(0)) &&
                        part.matches(".*\\d.*")) {
                        continue;
                    }
                    if (productPart.length() > 0) {
                        productPart.append(" ");
                    }
                    productPart.append(part);
                }
                if (productPart.length() > 0) {
                    remaining = productPart.toString();
                }
            }
            if (!remaining.isEmpty()) {
                // 如果产品名包含"别名："格式，提取标准产品名（括号前的部分）
                if (remaining.contains("(别名：")) {
                    int idx = remaining.indexOf("(别名：");
                    remaining = remaining.substring(0, idx);
                }
                DictProduct product = productMapper.selectByProductName(remaining);
                if (product != null) {
                    productId = product.getProductId();
                }
            }
        }
        if (factoryNo == null || productId == null) return null;
        if (country == null) country = "";

        // 获取 factoryId 用于查询热门商家
        Integer factoryId = null;
        List<DictFactory> factories = factoryMapper.findByCountryAndFactoryNo(category, country, factoryNo);
        if (factories != null && !factories.isEmpty()) {
            factoryId = factories.get(0).getFactoryId();
        }

        StatFactoryProduct stat = statFactoryProductMapper.selectByFactoryNoAndProductId(factoryNo, productId);
        if (stat == null) return null;

        // 获取产品名用于 IQR 过滤查询
        String productName = stat.getProductName();
        if (productName == null) {
            DictProduct product = productMapper.selectById(productId);
            if (product != null) {
                productName = product.getProductName();
            }
        }

        FactoryProductCardDTO card = new FactoryProductCardDTO();
        card.setCardType("factoryProduct");
        card.setRank(rank);
        card.setHistoryId(history.getHistoryId());
        card.setCountry(stat.getCountry() != null ? stat.getCountry() : country);
        card.setCountryAlias(stat.getCountry() != null ? stat.getCountry() : country);
        card.setFactoryNo(stat.getFactoryNo() != null ? stat.getFactoryNo() : factoryNo);
        card.setProductId(stat.getProductId());
        card.setProductName(productName);
        // 价格区间：从实时 IQR 过滤 SQL 获取（口径与详情页一致）
        try {
            BizOfferMapper.PriceRange priceRange = bizOfferMapper.selectFilteredPriceRangeByCountryFactoryProduct(
                    country, factoryNo, productName, category, "报盘");
            card.setPriceMin(priceRange != null ? priceRange.priceMin : null);
            card.setPriceMax(priceRange != null ? priceRange.priceMax : null);
        } catch (Exception e) {
            log.warn("获取价格区间失败: country={}, factoryNo={}, product={}, error={}",
                    country, factoryNo, productName, e.getMessage());
            card.setPriceMin(stat.getPriceMin());
            card.setPriceMax(stat.getPriceMax());
        }
        // 价格涨跌：从 stat_price_trend 读取今日/昨日数据计算（口径与详情页一致）
        try {
            List<StatPriceTrendMapper.PriceTrendPoint> trendPoints = statPriceTrendMapper.selectTrendPointsByCountryFactoryProduct(
                    StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT,
                    country,
                    productId,
                    factoryNo,
                    "报盘"
            );
            if (trendPoints != null && trendPoints.size() >= 2) {
                StatPriceTrendMapper.PriceTrendPoint todayPoint = null;
                StatPriceTrendMapper.PriceTrendPoint yesterdayPoint = null;
                for (StatPriceTrendMapper.PriceTrendPoint p : trendPoints) {
                    if (p.date != null && p.date.equals(today)) {
                        todayPoint = p;
                    } else if (p.date != null && p.date.equals(today.minusDays(1))) {
                        yesterdayPoint = p;
                    }
                }
                if (todayPoint != null && yesterdayPoint != null
                        && todayPoint.avgPrice != null && yesterdayPoint.avgPrice != null) {
                    BigDecimal todayPrice = todayPoint.avgPrice;
                    BigDecimal yesterdayPrice = yesterdayPoint.avgPrice;
                    BigDecimal change = todayPrice.subtract(yesterdayPrice).setScale(2, RoundingMode.HALF_UP);
                    card.setPriceChange(change);
                    // price_change_rate: ±999.99 上限约束（DECIMAL(5,2)）
                    if (yesterdayPrice.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal rate = change.divide(yesterdayPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                        if (rate.abs().compareTo(new BigDecimal("999.99")) > 0) {
                            rate = rate.signum() == 1 ? new BigDecimal("999.99") : new BigDecimal("-999.99");
                        }
                        card.setPriceChangeRate(rate);
                    } else {
                        card.setPriceChangeRate(null);
                    }
                } else {
                    card.setPriceChange(stat.getPriceChange());
                    card.setPriceChangeRate(stat.getPriceChangeRate());
                }
            } else {
                card.setPriceChange(stat.getPriceChange());
                card.setPriceChangeRate(stat.getPriceChangeRate());
            }
        } catch (Exception e) {
            log.warn("获取价格趋势失败: country={}, factoryNo={}, productId={}, error={}",
                    country, factoryNo, productId, e.getMessage());
            card.setPriceChange(stat.getPriceChange());
            card.setPriceChangeRate(stat.getPriceChangeRate());
        }
        card.setTodayOfferCount(stat.getTodayOfferCount());
        card.setInquiryCount(stat.getTodayInquiryCount());

        // 价格趋势
        try {
            List<StatPriceTrendMapper.PriceTrendPoint> trendPoints = statPriceTrendMapper.selectTrendPointsByCountryFactoryProduct(
                    StatPriceTrendMapper.DIMENSION_COUNTRY_FACTORY_PRODUCT,
                    country,
                    productId,
                    factoryNo,
                    "报盘"
            );
            if (trendPoints != null && !trendPoints.isEmpty()) {
                List<FactoryProductCardDTO.TrendPointDTO> trendPointDTOs = new ArrayList<>();
                for (StatPriceTrendMapper.PriceTrendPoint p : trendPoints) {
                    FactoryProductCardDTO.TrendPointDTO dto = new FactoryProductCardDTO.TrendPointDTO();
                    dto.setDate(p.date != null ? p.date.toString() : "");
                    dto.setAvgPrice(p.avgPrice != null ? p.avgPrice.doubleValue() : null);
                    trendPointDTOs.add(dto);
                }
                card.setTrendPoints(trendPointDTOs);
            }
        } catch (Exception e) {
            log.warn("获取价格趋势失败: {}", e.getMessage());
        }

        // 热门商家（SQL 已 JOIN dict_merchant 返回 merchantName）
        if (factoryId != null) {
            List<MerchantStatWithPriceDTO> merchantStats = bizOfferMapper.aggregateByMerchantForFactoryProduct(today, factoryId, productId);
            List<FactoryProductCardDTO.HotMerchantDTO> hotMerchants = new ArrayList<>();
            for (MerchantStatWithPriceDTO ms : merchantStats) {
                FactoryProductCardDTO.HotMerchantDTO dto = new FactoryProductCardDTO.HotMerchantDTO();
                dto.setMerchantId(ms.getMerchantId());
                // SQL 已 LEFT JOIN dict_merchant.short_name，fallback 到 ID
                if (ms.getMerchantName() != null && !ms.getMerchantName().isEmpty() && !"NULL".equalsIgnoreCase(ms.getMerchantName())) {
                    dto.setMerchantName(ms.getMerchantName());
                } else {
                    dto.setMerchantName("商家-" + ms.getMerchantId());
                }
                dto.setOfferCount(ms.getTodayOfferCount());
                dto.setPriceMin(ms.getPriceMin());
                dto.setPriceMax(ms.getPriceMax());
                hotMerchants.add(dto);
            }
            card.setHotMerchants(hotMerchants);
        }

        return card;
    }

    private List<CountryCardDTO.HotFactoryDTO> parseHotFactoriesFromJson(String json) {
        List<CountryCardDTO.HotFactoryDTO> result = new ArrayList<>();
        if (json == null || json.isEmpty()) return result;
        try {
            List<?> list = objectMapper.readValue(json, new TypeReference<List<?>>() {});
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                if (list.get(i) instanceof String) {
                    CountryCardDTO.HotFactoryDTO dto = new CountryCardDTO.HotFactoryDTO();
                    dto.setFactoryNo((String) list.get(i));
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            log.warn("解析hotFactories失败: {}", e.getMessage());
        }
        return result;
    }

    private List<CountryCardDTO.HotProductDTO> parseHotProductsFromJson(String json) {
        List<CountryCardDTO.HotProductDTO> result = new ArrayList<>();
        if (json == null || json.isEmpty()) return result;
        try {
            List<?> list = objectMapper.readValue(json, new TypeReference<List<?>>() {});
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                if (list.get(i) instanceof String) {
                    CountryCardDTO.HotProductDTO dto = new CountryCardDTO.HotProductDTO();
                    dto.setProductName((String) list.get(i));
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            log.warn("解析hotProducts失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteSearchHistory(Long historyId) {
        searchHistoryMapper.deleteById(historyId);
    }

    @Override
    @Transactional
    public void batchDeleteSearchHistory(List<Long> historyIds) {
        searchHistoryMapper.batchDelete(historyIds);
    }

    @Override
    @Transactional
    public void addSelfSelect(Long userId, String searchWord, String searchType) {
        Long existingId = searchHistoryMapper.findExistingHistory(userId, searchWord, searchType);
        if (existingId != null) {
            BizSearchHistory history = new BizSearchHistory();
            history.setHistoryId(existingId);
            history.setIsSelfSelect(1);
            searchHistoryMapper.updateById(history);
        } else {
            searchHistoryMapper.insertOrIgnore(userId, searchWord, searchType, 1);
        }
    }

    @Override
    @Transactional
    public void cancelSelfSelect(Long historyId) {
        BizSearchHistory history = new BizSearchHistory();
        history.setHistoryId(historyId);
        history.setIsSelfSelect(0);
        searchHistoryMapper.updateById(history);
    }

    @Override
    @Transactional
    public void moveToSelfSelect(Long historyId) {
        BizSearchHistory history = new BizSearchHistory();
        history.setHistoryId(historyId);
        history.setIsSelfSelect(1);
        searchHistoryMapper.updateById(history);
    }

    private List<SearchHistoryDTO> convertToDTO(List<BizSearchHistory> histories) {
        List<SearchHistoryDTO> result = new ArrayList<>();
        for (BizSearchHistory h : histories) {
            SearchHistoryDTO dto = new SearchHistoryDTO();
            dto.historyId = h.getHistoryId();
            dto.searchWord = h.getSearchWord();
            dto.searchType = h.getSearchType();
            dto.isSelfSelect = h.getIsSelfSelect();
            dto.createTime = h.getCreateTime() != null ? h.getCreateTime().format(DATE_FORMATTER) : "";

            fillDetailInfo(dto);

            result.add(dto);
        }
        return result;
    }

    private void fillDetailInfo(SearchHistoryDTO dto) {
        String keyword = dto.searchWord;
        String type = dto.searchType;

        try {
            switch (type) {
                case "产品":
                    DictProduct product = productMapper.selectByProductName(keyword);
                    if (product != null) {
                        dto.productId = product.getProductId();
                        dto.productName = product.getProductName();
                    }
                    break;
                case "国家":
                    dto.country = keyword;
                    break;
                case "品牌":
                    brandMapper.findByName(keyword).ifPresent(b -> {
                        dto.brandId = b.getBrandId();
                    });
                    break;
                case "商家":
                    merchantMapper.findByName(keyword).ifPresent(m -> {
                        dto.merchantId = m.getMerchantId();
                    });
                    break;
                case "国家厂号":
                    int idx = parseCountryAndFactory(keyword);
                    if (idx > 0) {
                        dto.country = keyword.substring(0, idx);
                        dto.factoryNo = keyword.substring(idx);
                    }
                    break;
                case "国家产品":
                    dto.country = extractCountry(keyword);
                    dto.productName = keyword.substring(dto.country.length()).trim();
                    break;
                case "品牌产品":
                    int spaceIdx = keyword.lastIndexOf(" ");
                    if (spaceIdx > 0) {
                        dto.productName = keyword.substring(spaceIdx + 1);
                    }
                    break;
                case "国家厂号产品":
                    parseCountryFactoryProduct(keyword, dto);
                    break;
            }
        } catch (Exception e) {
            log.warn("解析搜索历史详情失败: keyword={}, type={}, error={}", keyword, type, e.getMessage());
        }
    }

    private int parseCountryAndFactory(String keyword) {
        // 如果有空格，尝试按空格分割
        if (keyword.contains(" ")) {
            String[] parts = keyword.split(" ");
            if (parts.length >= 2) {
                // 找第一个包含字母+数字的部分（厂号）
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i];
                    if (part.length() >= 3 && Character.isLetter(part.charAt(0))) {
                        // 检查是否包含数字（厂号特征）
                        boolean hasDigit = false;
                        for (char c : part.toCharArray()) {
                            if (Character.isDigit(c)) {
                                hasDigit = true;
                                break;
                            }
                        }
                        if (hasDigit) {
                            // 返回这部分在原字符串中的起始位置
                            return keyword.indexOf(part);
                        }
                    }
                }
            }
        }
        // 没有空格的话，在去掉空格的字符串中查找字母+数字模式
        String noSpace = keyword.replace(" ", "");
        for (int i = 0; i < noSpace.length(); i++) {
            char c = noSpace.charAt(i);
            if (Character.isLetter(c) && i + 2 < noSpace.length() && Character.isDigit(noSpace.charAt(i + 1))) {
                return keyword.indexOf(noSpace.charAt(i));
            }
        }
        return -1;
    }

    private String extractCountry(String keyword) {
        String[] countries = {"巴西", "阿根廷", "乌拉圭", "澳大利亚", "新西兰", "美国", "加拿大", "中国"};
        for (String country : countries) {
            if (keyword.startsWith(country)) {
                return country;
            }
        }
        return "";
    }

    private void parseCountryFactoryProduct(String keyword, SearchHistoryDTO dto) {
        String country = extractCountry(keyword);
        dto.country = country;

        String remaining = keyword.substring(country.length());
        int factoryStart = -1;
        for (int i = 0; i < remaining.length(); i++) {
            char c = remaining.charAt(i);
            if (Character.isLetter(c) && i + 1 < remaining.length() && Character.isDigit(remaining.charAt(i + 1))) {
                factoryStart = i;
                break;
            }
        }

        if (factoryStart >= 0) {
            String factoryPart = remaining.substring(factoryStart);
            dto.factoryNo = factoryPart;

            dto.productName = remaining.substring(0, factoryStart).trim();
            if (dto.productName.isEmpty()) {
                dto.productName = factoryPart;
                dto.factoryNo = "";
            }
        } else {
            dto.productName = remaining;
        }
    }
}
