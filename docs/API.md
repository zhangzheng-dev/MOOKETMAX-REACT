# 牧集 (Mooket) API 接口文档

> **版本**: v1.0.0
> **基础URL**: `http://43.139.56.124:8080`
> **更新时间**: 2026-04-21

---

## 目录

1. [概述](#概述)
2. [通用说明](#通用说明)
3. [搜索接口](#搜索接口)
4. [商家接口](#商家接口)
5. [产品接口](#产品接口)
6. [国家接口](#国家接口)
7. [厂号接口](#厂号接口)
8. [数据同步接口](#数据同步接口)
9. [数据模型](#数据模型)

---

## 概述

牧集是一个全球肉类供应链 B2B 商机与行情搜索平台，提供以下核心功能：

- **搜索**：支持按国家、厂号、产品、商家、品牌进行搜索联想
- **商家详情**：查看商家的报盘/求购信息
- **产品详情**：按产品聚合所有商家的报盘/求购
- **国家详情**：按国家查看产品、厂号、商家统计
- **厂号详情**：按厂号查看产品、报盘统计

### 技术栈

- **后端**：Java 17 + Spring Boot 3.2.0 + MyBatis Plus
- **数据库**：PostgreSQL (主数据源) + MySQL (数据源)
- **缓存**：Caffeine (本地缓存)

---

## 通用说明

### 请求格式

所有接口均使用 **GET** 请求，参数通过 URL Query String 传递。

### 响应格式

所有接口返回统一 JSON 格式：

```json
{
  "code": 200,           // 状态码，200表示成功
  "message": "success",  // 状态消息
  "data": { ... }        // 响应数据，失败时为null
}
```

### 通用参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| category | String | 否 | 牛 | 品类筛选，支持：牛、猪、羊、禽、水产 |
| page | Integer | 否 | 1 | 页码（从1开始） |
| pageSize | Integer | 否 | 10 | 每页大小 |

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 其他 | 请求失败，message 包含错误信息 |

---

## 搜索接口

### 1.1 获取搜索联想词

获取搜索关键词的联想建议列表。

**请求**

```
GET /api/v1/search/suggest
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category | String | 否 | 品类（默认"牛"） |
| keyword | String | **是** | 搜索关键词 |

**示例**

```bash
# 搜索包含"巴西"的联想词
curl "http://43.139.56.124:8080/api/v1/search/suggest?category=牛&keyword=巴西"
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "text": "巴西",
      "keyword": "巴西",
      "type": "国家",
      "priority": 1,
      "targetId": 0,
      "matchType": "country",
      "inputKeyword": "巴西",
      "standardName": null,
      "aliasName": null
    },
    {
      "text": "巴西 JBS",
      "keyword": "巴西 JBS",
      "type": "品牌",
      "priority": 2,
      "targetId": 123,
      "matchType": "brand",
      "inputKeyword": "巴西 JBS",
      "standardName": "JBS",
      "aliasName": null
    }
  ]
}
```

**联想词类型 (type)**

| type 值 | 说明 | 示例 |
|---------|------|------|
| 国家 | 国家名称 | "巴西"、"阿根廷" |
| 厂号 | 工厂编号 | "SIF1440"、"JBS001" |
| 产品 | 产品名称 | "牛腩"、"牛霖" |
| 商家 | 商家名称 | "上海一牛贸易" |
| 品牌 | 品牌名称 | "JBS"、"MINERVA" |
| 综合 | 组合搜索 | "巴西 牛腩" |

**匹配类型 (matchType)**

| matchType 值 | 说明 |
|--------------|------|
| country | 国家匹配 |
| factory | 厂号匹配 |
| product | 产品匹配 |
| merchant | 商家匹配 |
| brand | 品牌匹配 |
| combined | 组合匹配 |

**优先级 (priority)**

数字越小优先级越高，搜索结果按优先级排序。

---

## 商家接口

### 2.1 获取商家详情

获取指定商家的详细信息和统计数据。

**请求**

```
GET /api/v1/merchant/{id}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | **是** | 商家ID（路径参数） |
| category | String | 否 | 品类筛选 |

**示例**

```bash
curl "http://43.139.56.124:8080/api/v1/merchant/123?category=牛"
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "merchantId": 123,
    "merchantName": "上海一牛国际贸易有限公司",
    "merchantShortName": "上海一牛",
    "merchantTags": "源头直采|冷链物流",
    "contactPhone": "400-888-8888",
    "todayOfferCount": 56,
    "todayInquiryCount": 12,
    "todayProductCount": 32,
    "todayFactoryCount": 24,
    "offers": [
      {
        "offerId": 1001,
        "productName": "牛霖",
        "country": "巴西",
        "factoryNo": "SIF1440",
        "price": 59.5,
        "priceMax": 60.5,
        "goodsLocation": "上海",
        "tags": "原包|品质保障",
        "goodsType": "期货",
        "feedingType": "草饲",
        "publishTime": "2026-04-21T10:30:00",
        "employeeOffers": [...]
      }
    ],
    "inquiries": [...],
    "totalOffers": 56,
    "totalInquiries": 12
  }
}
```

### 2.2 分页获取商家产品列表

分页获取指定商家的报盘/求购产品列表。

**请求**

```
GET /api/v1/merchant/{id}/products
```

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | Long | **是** | - | 商家ID（路径参数） |
| type | String | **是** | - | 类型：offer(报盘) 或 inquiry(求购) |
| category | String | 否 | - | 品类筛选 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**示例**

```bash
curl "http://43.139.56.124:8080/api/v1/merchant/123/products?type=offer&category=牛&page=1&pageSize=10"
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "products": [
      {
        "offerId": 1001,
        "productName": "牛霖",
        "country": "巴西",
        "factoryNo": "SIF1440",
        "price": 59.5,
        "priceMax": 60.5,
        "goodsLocation": "上海",
        "tags": "原包|品质保障",
        "goodsType": "期货",
        "feedingType": "草饲",
        "publishTime": "2026-04-21T10:30:00",
        "employeeOffers": [...]
      }
    ],
    "totalCount": 56,
    "page": 1,
    "pageSize": 10,
    "totalPages": 6,
    "offerType": "offer"
  }
}
```

---

## 产品接口

### 3.1 获取产品详情

按产品聚合所有商家的报盘/求购信息。

**请求**

```
GET /api/v1/product/{id}
```

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | Integer | **是** | - | 产品ID（路径参数） |
| category | String | 否 | - | 品类筛选 |
| type | String | 否 | offer | 类型：offer(报盘) 或 inquiry(求购) |
| sortBy | String | 否 | comprehensive | 排序：comprehensive(综合) 或 price(价格) |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**示例**

```bash
curl "http://43.139.56.124:8080/api/v1/product/1?category=牛&type=offer&sortBy=price&page=1"
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "productId": 1,
    "productName": "牛霖",
    "category": "牛",
    "offerCount": 128,
    "priceMin": 55.0,
    "priceMax": 68.5,
    "merchantCount": 24,
    "factoryCount": 16,
    "summaries": [
      {
        "country": "巴西",
        "factoryNo": "SIF1440",
        "countryFactory": "巴西 SIF1440",
        "priceMin": 59.0,
        "priceMax": 60.5,
        "merchantNames": ["上海一牛", "天津大洋", "JBS中国"],
        "merchantCount": 8,
        "offerCount": 24
      }
    ],
    "totalCount": 16,
    "page": 1,
    "pageSize": 10,
    "totalPages": 2
  }
}
```

**排序说明**

| sortBy 值 | 说明 |
|-----------|------|
| comprehensive | 综合推荐（默认，按报盘数降序） |
| price | 价格排序（可切换升序/降序） |

---

## 国家接口

### 4.1 获取国家详情

获取指定国家的详细统计和产品列表。

**请求**

```
GET /api/v1/country/{country}
```

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| country | String | **是** | - | 国家名称（路径参数，如"巴西"） |
| category | String | 否 | - | 品类筛选 |
| type | String | 否 | offer | 类型：offer(报盘) 或 inquiry(求购) |
| sortBy | String | 否 | comprehensive | 排序：comprehensive 或 price |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**示例**

```bash
curl "http://43.139.56.124:8080/api/v1/country/巴西?category=牛&type=offer&page=1"
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "country": "巴西",
    "offerCount": 1256,
    "merchantCount": 48,
    "factoryCount": 32,
    "priceMin": 48.0,
    "priceMax": 85.5,
    "hotFactories": [
      { "factoryNo": "SIF1440", "offerCount": 128, "rank": 1 },
      { "factoryNo": "JBS001", "offerCount": 96, "rank": 2 },
      { "factoryNo": "MINERVA", "offerCount": 72, "rank": 3 }
    ],
    "hotProducts": [
      { "productName": "牛霖", "offerCount": 256, "rank": 1 },
      { "productName": "牛腩", "offerCount": 198, "rank": 2 },
      { "productName": "牛腱", "offerCount": 156, "rank": 3 }
    ],
    "summaries": [
      {
        "productId": 1,
        "productName": "牛霖",
        "priceMin": 55.0,
        "priceMax": 62.5,
        "factoryNos": ["SIF1440", "JBS001", "MINERVA"],
        "factoryCount": 12,
        "offerCount": 86
      }
    ],
    "totalCount": 24,
    "page": 1,
    "pageSize": 10,
    "totalPages": 3
  }
}
```

---

## 厂号接口

### 5.1 获取厂号筛选数据

获取用于筛选的厂号列表（国家+厂号组合）。

**请求**

```
GET /api/v1/factory/filter
```

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| category | String | 否 | 牛 | 品类筛选 |

**示例**

```bash
curl "http://43.139.56.124:8080/api/v1/factory/filter?category=牛"
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "countries": ["巴西", "阿根廷", "乌拉圭", "澳大利亚"],
    "factories": [
      { "country": "巴西", "factoryNo": "SIF1440" },
      { "country": "巴西", "factoryNo": "JBS001" },
      { "country": "阿根廷", "factoryNo": "227" }
    ]
  }
}
```

### 5.2 获取厂号详情

获取指定厂号的详细统计和产品列表。

**请求**

```
GET /api/v1/factory/detail
```

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| country | String | **是** | - | 国家名称 |
| factoryNo | String | **是** | - | 厂号 |
| category | String | 否 | - | 品类筛选 |
| type | String | 否 | offer | 类型：offer(报盘) 或 inquiry(求购) |
| sortBy | String | 否 | comprehensive | 排序：comprehensive、price_asc、price_desc |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**示例**

```bash
curl "http://43.139.56.124:8080/api/v1/factory/detail?country=巴西&factoryNo=SIF1440&category=牛"
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "factoryId": 1,
    "country": "巴西",
    "countryAlias": null,
    "factoryNo": "SIF1440",
    "productCount": 32,
    "inquiryCount": 8,
    "recentOfferCount": 56,
    "products": [
      {
        "productId": 1,
        "productName": "牛霖",
        "priceMin": 59.0,
        "priceMax": 60.5,
        "merchantNames": ["上海一牛", "天津大洋", "JBS中国"],
        "merchantCount": 8,
        "offerCount": 24
      }
    ],
    "totalCount": 32,
    "page": 1,
    "pageSize": 10,
    "totalPages": 4
  }
}
```

**sortBy 排序说明**

| sortBy 值 | 说明 |
|-----------|------|
| comprehensive | 综合推荐（默认，按报盘数降序） |
| price_asc | 价格升序 |
| price_desc | 价格降序 |

---

## 数据同步接口

> ⚠️ **注意**: 以下接口仅用于数据管理和测试，生产环境需谨慎使用。

### 6.1 同步产品字典

手动触发 `dict_product` 字典同步（一次性）。

**请求**

```
POST /api/v1/sync/dict-product
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "dict_product 同步完成"
}
```

### 6.2 同步厂号字典

手动触发 `dict_factory` 字典同步（一次性）。

**请求**

```
POST /api/v1/sync/dict-factory
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "dict_factory 同步完成"
}
```

### 6.3 同步品牌字典

手动触发 `dict_brand` 字典同步（一次性）。

**请求**

```
POST /api/v1/sync/dict-brand
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "dict_brand 同步完成，共 128 条"
}
```

### 6.4 同步商家字典

手动触发 `dict_merchant` 字典同步（一次性）。

**请求**

```
POST /api/v1/sync/dict-merchant
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "dict_merchant 同步完成，共 256 条"
}
```

### 6.5 同步用户商家关联

手动触发 `rel_user_merchant` 关联同步（一次性）。

**请求**

```
POST /api/v1/sync/rel-user-merchant
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "rel_user_merchant 同步完成，共 512 条"
}
```

### 6.6 同步报盘数据（增量）

手动触发 `biz_offer` 增量同步（最近2天数据）。

**请求**

```
POST /api/v1/sync/biz-offer/initial
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "biz_offer 同步完成，共 2048 条"
}
```

### 6.7 同步报盘数据（全量）

手动触发 `biz_offer` 全量同步（最近2天数据，强制重新同步）。

**请求**

```
POST /api/v1/sync/biz-offer/full
```

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": "biz_offer 全量同步完成，共 4096 条"
}
```

---

## 数据模型

### 7.1 统一响应格式

```json
{
  "code": 200,           // 状态码
  "message": "success",    // 消息
  "data": { ... }         // 数据体
}
```

### 7.2 商家详情 (MerchantDetailDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| merchantId | Long | 商家ID |
| merchantName | String | 商家全称 |
| merchantShortName | String | 商家简称 |
| merchantTags | String | 商家标签（分隔符：\|） |
| contactPhone | String | 联系电话 |
| todayOfferCount | Integer | 今日报盘数 |
| todayInquiryCount | Integer | 今日求购数 |
| todayProductCount | Integer | 今日产品数 |
| todayFactoryCount | Integer | 今日涉及的工厂数 |
| offers | List\<OfferSummaryDTO\> | 报盘列表 |
| inquiries | List\<OfferSummaryDTO\> | 求购列表 |

### 7.3 产品详情 (ProductDetailDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| productId | Integer | 产品ID |
| productName | String | 产品名称 |
| category | String | 品类（牛/猪） |
| offerCount | Long | 报盘总数 |
| priceMin | BigDecimal | 价格区间最低 |
| priceMax | BigDecimal | 价格区间最高 |
| merchantCount | Integer | 商家数 |
| factoryCount | Integer | 工厂数 |
| summaries | List\<ProductSummaryDTO\> | 按国家厂号聚合的列表 |
| totalCount | Integer | 聚合条目总数 |
| page | Integer | 当前页 |
| pageSize | Integer | 每页大小 |
| totalPages | Integer | 总页数 |

### 7.4 国家详情 (CountryDetailDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| country | String | 国家名称 |
| offerCount | Long | 报盘数 |
| merchantCount | Integer | 商家数 |
| factoryCount | Integer | 工厂数 |
| priceMin | BigDecimal | 价格区间最低 |
| priceMax | BigDecimal | 价格区间最高 |
| hotFactories | List\<HotFactoryDTO\> | 热门厂号（Top 3） |
| hotProducts | List\<HotProductDTO\> | 热门产品（Top 3） |
| summaries | List\<CountryProductSummaryDTO\> | 按产品聚合的列表 |
| totalCount | Integer | 聚合条目总数 |
| page | Integer | 当前页 |
| pageSize | Integer | 每页大小 |
| totalPages | Integer | 总页数 |

### 7.5 厂号详情 (FactoryDetailDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| factoryId | Integer | 厂号ID |
| country | String | 国家名称 |
| countryAlias | String | 国家别名（用于显示） |
| factoryNo | String | 厂号 |
| productCount | Integer | 产品数 |
| inquiryCount | Integer | 求购数 |
| recentOfferCount | Integer | 近2日报盘数 |
| products | List\<FactoryProductDTO\> | 产品列表 |
| totalCount | Integer | 产品条目总数 |
| page | Integer | 当前页 |
| pageSize | Integer | 每页大小 |
| totalPages | Integer | 总页数 |

### 7.6 搜索联想词 (SearchSuggestDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| text | String | 联想显示文本 |
| keyword | String | 原始输入关键词 |
| type | String | 类型：国家/厂号/产品/商家/品牌/综合 |
| priority | Integer | 优先级（1-7，数字越小优先级越高） |
| targetId | Long | 目标ID |
| matchType | String | 匹配类型 |
| inputKeyword | String | 用户输入的原始关键词 |
| standardName | String | 标准名称（如输入匹配别名） |
| aliasName | String | 别名（如输入匹配别名） |

### 7.7 热门厂号 (HotFactoryDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| factoryNo | String | 厂号 |
| offerCount | Integer | 报盘数 |
| rank | Integer | 排名（1/2/3） |

### 7.8 热门产品 (HotProductDTO)

| 字段 | 类型 | 说明 |
|------|------|------|
| productName | String | 产品名称 |
| offerCount | Integer | 报盘数 |
| rank | Integer | 排名（1/2/3） |

---

## 错误码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 其他 | 失败，message 包含具体错误信息 |

---

## 分页说明

所有列表接口支持分页，参数说明：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| page | 1 | 页码，从1开始 |
| pageSize | 10 | 每页条目数 |

响应中包含分页信息：

| 字段 | 说明 |
|------|------|
| totalCount | 总条目数 |
| page | 当前页 |
| pageSize | 每页大小 |
| totalPages | 总页数 |

---

## 价格异常值过滤

产品详情和国家详情接口的价格区间会自动过滤偏离中位数20%以上的异常价格，确保显示的价格区间更具参考价值。

统计类数据（报盘数、商家数、工厂数）不受价格过滤影响。

---

## 数据时效性

- 报盘/求购数据仅保留**近2天**
- 每天凌晨自动清理超过2天的历史数据
- 看板统计数据实时计算

---

## 更新日志

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-04-21 | v1.0.0 | 初始版本，包含搜索、商家、产品、国家、厂号五大模块 |
