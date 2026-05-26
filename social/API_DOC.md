# 牧集 (Mooket) API 文档

> 全球肉类供应链 B2B 商机与行情搜索平台 API

**Base URL**: `https://twms.malleeglobal.com/social/api/v1`

**统一响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**错误响应**:
```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

---

## 1. 搜索服务 (Search)

### 1.1 获取搜索联想词

获取搜索关键词的联想建议，支持语义级实体识别（国家、厂号、品牌、产品、商家）。

**Endpoint**: `GET /api/v1/search/suggest`

**Query Parameters**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| category | string | 否 | 牛 | 品类：`牛` 或 `猪` |
| keyword | string | 是 | - | 搜索关键词 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "text": "巴西牛肉",
      "keyword": "巴西",
      "type": "country",
      "priority": 1,
      "targetId": 1,
      "matchType": "factory",
      "inputKeyword": "巴西",
      "standardName": null,
      "aliasName": null
    },
    {
      "text": "牛腱子",
      "keyword": "腱",
      "type": "产品",
      "priority": 2,
      "targetId": 15,
      "matchType": "product",
      "inputKeyword": "腱",
      "standardName": "牛腱子",
      "aliasName": null
    }
  ]
}
```

**matchType 枚举值**:
- `product` - 产品匹配
- `factory` - 厂号匹配
- `brand` - 品牌匹配
- `merchant` - 商家匹配
- `combined` - 组合匹配

---

## 2. 厂号服务 (Factory)

### 2.1 获取厂号筛选数据

获取用于筛选的厂号列表，按国家分组。

**Endpoint**: `GET /api/v1/factory/filter`

**Query Parameters**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| category | string | 否 | 牛 | 品类：`牛` 或 `猪` |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "countries": ["巴西", "阿根廷", "乌拉圭"],
    "factories": [
      { "country": "巴西", "factoryNo": "SIF50" },
      { "country": "巴西", "factoryNo": "SIF100" },
      { "country": "阿根廷", "factoryNo": "EP9" }
    ]
  }
}
```

---

## 3. 商家服务 (Merchant)

### 3.1 获取商家详情

获取商家详细信息，包括统计数据、报盘列表和求购列表。

**Endpoint**: `GET /api/v1/merchant/{id}`

**Path Parameters**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 商家 ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "merchantId": 1,
    "merchantName": "某某进出口公司",
    "merchantShortName": "某某",
    "merchantTags": "源头货源,一手报价",
    "contactPhone": "13800138000",
    "todayOfferCount": 25,
    "todayInquiryCount": 8,
    "todayProductCount": 15,
    "todayFactoryCount": 6,
    "offers": [
      {
        "offerId": 1001,
        "productName": "牛腱子",
        "country": "巴西",
        "factoryNo": "SIF50",
        "price": 42.00,
        "priceMax": 45.00,
        "goodsLocation": "上海冷库",
        "tags": "期货,现货",
        "goodsType": "冷冻",
        "feedingType": "谷饲",
        "publishTime": "2026-04-13T10:30:00",
        "employeeOffers": [
          {
            "employeeId": 1,
            "employeeName": "张三",
            "price": 42.00,
            "priceMax": 45.00,
            "minQuantity": 1,
            "unit": "吨",
            "publishTime": "2026-04-13T10:30:00"
          }
        ]
      }
    ],
    "inquiries": []
  }
}
```

### 3.2 手动触发商家统计更新

手动触发商家统计数据更新（用于测试或管理员操作）。

**Endpoint**: `POST /api/v1/merchant/trigger-stat-update`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "商家统计更新已触发"
}
```

---

## 附录

### DTO 数据结构

#### SearchSuggestDTO (搜索联想词)
| 字段 | 类型 | 说明 |
|------|------|------|
| text | string | 联想显示文本 |
| keyword | string | 原始输入关键词 |
| type | string | 类型 |
| priority | integer | 优先级 (1-7，越小越高) |
| targetId | integer | 目标 ID |
| matchType | string | 匹配类型 |
| inputKeyword | string | 用户原始输入 |
| standardName | string | 标准名称 (别名匹配时) |
| aliasName | string | 别名 (别名匹配时) |

#### MerchantDetailDTO (商家详情)
| 字段 | 类型 | 说明 |
|------|------|------|
| merchantId | integer | 商家 ID |
| merchantName | string | 商家全称 |
| merchantShortName | string | 商家简称 |
| merchantTags | string | 商家标签 |
| contactPhone | string | 联系电话 |
| todayOfferCount | integer | 今日报盘数 |
| todayInquiryCount | integer | 今日求购数 |
| todayProductCount | integer | 今日产品数 |
| todayFactoryCount | integer | 今日厂号数 |
| offers | List\<OfferSummaryDTO\> | 报盘列表 |
| inquiries | List\<OfferSummaryDTO\> | 求购列表 |

#### OfferSummaryDTO (报盘摘要)
| 字段 | 类型 | 说明 |
|------|------|------|
| offerId | long | 报盘 ID |
| productName | string | 产品名称 |
| country | string | 国家 |
| factoryNo | string | 厂号 |
| price | BigDecimal | 单价 |
| priceMax | BigDecimal | 最高价 |
| goodsLocation | string | 货物位置 |
| tags | string | 标签 |
| goodsType | string | 货物类型 (冷冻/冷藏) |
| feedingType | string | 饲养方式 (谷饲/草饲) |
| publishTime | LocalDateTime | 发布时间 |
| employeeOffers | List\<EmployeeOfferDTO\> | 员工报价明细 |

#### EmployeeOfferDTO (员工报价明细)
| 字段 | 类型 | 说明 |
|------|------|------|
| employeeId | integer | 员工 ID |
| employeeName | string | 员工姓名 |
| price | BigDecimal | 单价 |
| priceMax | BigDecimal | 最高价 |
| minQuantity | integer | 最小数量 |
| unit | string | 单位 |
| publishTime | LocalDateTime | 发布时间 |

#### FactoryFilterDTO (厂号筛选)
| 字段 | 类型 | 说明 |
|------|------|------|
| countries | List\<string\> | 国家列表 |
| factories | List\<FactoryItem\> | 厂号列表 |

#### FactoryFilterDTO.FactoryItem
| 字段 | 类型 | 说明 |
|------|------|------|
| country | string | 国家 |
| factoryNo | string | 厂号 |

---

## 错误码说明

| code | 说明 |
|------|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |
