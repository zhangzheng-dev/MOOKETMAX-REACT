# 牧集 (Mooket) Android App 开发记录

## 项目概述

**牧集** - 全球肉类供应链 B2B 商机与行情搜索平台

- **Android App**: Kotlin + Jetpack Compose (minSdk 24, targetSdk 34)
- **Backend**: Java 17 + Spring Boot 3.2.0 + MyBatis Plus
- **服务器**: `ai-pg-43.139.56.124`

---

## 已完成页面

| 页面 | 路由 | 说明 |
|------|------|------|
| HomeScreen | `home` | 首页 |
| SearchScreen | `search/{category}` | 搜索激活页 |
| MerchantScreen | `merchant/{merchantId}/{category}` | 商家详情页 |
| ProductDetailScreen | `product/{productId}/{category}/{productName}` | 产品详情页 |
| CountryDetailScreen | `country/{country}/{category}` | 国家详情页 |
| FactoryDetailScreen | `factory/{country}/{factoryNo}/{category}` | 厂号详情页 |
| CountryProductScreen | `country-product/{country}/{productName}/{category}` | 国家+产品详情页 |
| CountryFactoryProductScreen | `country-factory-product/{country}/{factoryNo}/{productName}/{category}` | 国家+厂号+产品详情页 |

---

## 2026-04-22 今日完成工作

### 1. 图表数据源分离（7日 vs 30日）

**问题**: 之前 7 日和 30 日图表共用同一数据源，且都只显示近 7 天数据。

**解决方案**:
- 后端 `CountryProductServiceImpl.java` 新增两个独立方法：
  - `getPriceHistory7Days()` - 只取最近 7 天数据
  - `getPriceHistory30Days()` - 取完整 30 天数据
- Android 端 `CountryProductDetail` 数据类新增 `priceHistory7Days` 和 `priceHistory30Days` 两个独立字段

### 2. 修复 30 日趋势图显示异常

**问题**: 30 日趋势图只显示为一条直线，尽管数据库中存在不同的价格数据（54-59 区间）。

**根因**: Canvas 所在 Box 使用 `weight(1f)` 但父 Column 没有固定高度，导致图表高度为 0。

**解决方案**: 将图表容器高度从 `weight(1f)` 改为固定 `120.dp`

### 3. 图表 Tooltip 格式

**要求**: 点击图表节点显示 tooltip，内容格式：
- 日期：月-日（无年份）
- 价格：数值 + 单位 "元/kg"

**实现**: 在 `CountryProductScreen.kt` 中为 `PriceTrendChart` 组件添加 tooltip 支持。

### 4. 空状态提示

**要求**: 7 日报价走势区域无数据时显示 "暂无走势数据" 文字。

**实现**:
```kotlin
if (detail.priceHistory7Days.isNotEmpty()) {
    SparklineChart(...)
} else {
    Text(
        text = "暂无走势数据",
        fontSize = 10.sp,
        color = TextHint
    )
}
```

### 5. 删除功能（搜索标签）

**需求**: 所有详情页（除商家外）添加可删除的搜索标签，点击删除后跳转回搜索激活页。

**各页面实现**:

| 页面 | 删除操作 | 跳转目标 |
|------|----------|----------|
| ProductDetailScreen | 删除产品标签 | SearchScreen |
| CountryDetailScreen | 删除国家标签 | SearchScreen |
| FactoryDetailScreen | 删除厂号标签 | SearchScreen |
| CountryProductScreen | 删除国家标签 | ProductDetailScreen |
| CountryProductScreen | 删除产品标签 | CountryDetailScreen |

**SearchTag 组件定义**:
```kotlin
@Composable
fun SearchTag(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Primary)
            .clickable { onClick() }
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = text, fontSize = 12.sp, color = Color.White)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "删除",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
```

### 6. CountryProductScreen 导航逻辑修复

**问题**: 国家+产品详情页的删除国家/删除产品功能导航目标错误。

**正确逻辑**:
- 删除国家标签 → 进入该产品的详情页（需要 productId）
- 删除产品标签 → 进入该国家的详情页

**修复内容**:
1. 后端 `CountryProductDetailDTO` 新增 `productId` 字段
2. `CountryProductServiceImpl` 通过 `dict_product` 表查询填充 productId
3. Android 端 `CountryProductScreen` 修改回调签名：
   ```kotlin
   onCountryDelete: (Int, String, String) -> Unit, // productId, productName, category
   onProductDelete: (String, String) -> Unit,      // country, category
   ```
4. Navigation.kt 更新导航逻辑

---

### 7. 新增国家+厂号+产品详情页

**需求**: 点击搜索联想词为国家+厂号+产品类型时，跳转到国家+厂号+产品详情页。

**实现内容**:

1. **后端新增**:
   - `CountryFactoryProductDetailDTO.java` - 新 DTO 包含 factoryNo 字段
   - `CountryFactoryProductService.java` - Service 接口
   - `CountryFactoryProductServiceImpl.java` - Service 实现
   - `CountryFactoryProductController.java` - REST 控制器
   - `BizOfferMapper.java` 新增方法：
     - `selectCountryFactoryProductStats` - 看板统计
     - `selectFilteredPriceRangeByCountryFactoryProduct` - 价格区间（带 IQR 过滤）
     - `selectOfferListByCountryFactoryProduct` - 报盘列表（分页、排序）
     - `countOfferListByCountryFactoryProduct` - 报盘总数
   - `StatPriceTrendMapper.java` - 修改 `selectTrendPoints` 支持 factoryNo 过滤

2. **Android 新增**:
   - `CountryFactoryProductDetail` 数据类
   - `Offer` 数据类（报盘）
   - `CountryFactoryProductViewModel.kt`
   - `CountryFactoryProductScreen.kt`
   - API 接口 `getCountryFactoryProductDetail`

3. **导航集成**:
   - 新增 `CountryFactoryProduct` 路由
   - SearchScreen 新增 `onCountryFactoryProductClick` 回调
   - 当搜索联想词类型为"国家+厂号+产品"时导航到新页面

**API 接口**:
```
GET /api/v1/country-factory-product
参数: country, factoryNo, productName, type, category, sortBy, page, pageSize
```

---

## 技术要点

### 后端
- **数据源分离**: 7 日和 30 日价格趋势使用独立查询方法
- **productId 查询**: 通过 `DictProductMapper.findByName(category, productName)` 获取
- **缓存**: `@Cacheable(value = "countryProductDetail", key = ...)` 缓存国家+产品详情

### Android
- **图表高度**: 使用固定 `120.dp` 而非 `weight(1f)` 避免布局问题
- **导航回调**: 通过高阶函数实现页面间解耦
- **状态管理**: ViewModel + remember/mutableStateOf 管理 UI 状态

---

## 文件变更清单

### 后端 (E:\project6\social)
| 文件 | 变更 |
|------|------|
| `dto/CountryProductDetailDTO.java` | 新增 productId 字段 |
| `dto/CountryFactoryProductDetailDTO.java` | **新增** - 国家+厂号+产品详情 DTO |
| `service/CountryFactoryProductService.java` | **新增** - Service 接口 |
| `service/impl/CountryFactoryProductServiceImpl.java` | **新增** - Service 实现 |
| `controller/CountryFactoryProductController.java` | **新增** - REST 控制器 |
| `service/impl/CountryProductServiceImpl.java` | 拆分 7 日/30 日方法，查询 productId |
| `mapper/BizOfferMapper.java` | 新增国家+厂号+产品相关查询方法 |
| `mapper/StatPriceTrendMapper.java` | 修改 selectTrendPoints 支持 factoryNo 过滤 |

### Android (E:\project6\android)
| 文件 | 变更 |
|------|------|
| `data/model/Models.kt` | CountryProductDetail 新增 productId，DailyPrice 新增 priceUnit，**新增** CountryFactoryProductDetail、Offer |
| `data/api/ApiService.kt` | **新增** getCountryFactoryProductDetail 接口 |
| `ui/screens/countryproduct/CountryProductScreen.kt` | 图表固定高度、空状态提示、SearchTag 组件、修复删除导航 |
| `ui/screens/countryfactoryproduct/CountryFactoryProductScreen.kt` | **新增** - 国家+厂号+产品详情页 |
| `ui/screens/countryfactoryproduct/CountryFactoryProductViewModel.kt` | **新增** - ViewModel |
| `ui/screens/product/ProductDetailScreen.kt` | 添加 SearchTag 和 onSearchDelete 回调 |
| `ui/screens/country/CountryDetailScreen.kt` | 添加 SearchTag 和 onSearchDelete 回调 |
| `ui/screens/factory/FactoryDetailScreen.kt` | 添加 SearchTag 和 onSearchDelete 回调 |
| `ui/screens/search/SearchScreen.kt` | **新增** onCountryFactoryProductClick 回调 |
| `navigation/Navigation.kt` | 更新所有详情页路由和回调逻辑，**新增** CountryFactoryProduct 路由 |

---

## 待办事项

- [ ] 完善图表 tooltip 交互体验
- [ ] 添加加载状态动画
- [ ] 错误处理和重试机制
- [ ] 单元测试覆盖

---

## 构建和部署

### 后端
```bash
cd E:\project6\social
mvn package -DskipTests
# 上传 target/social-1.0.0-SNAPSHOT.jar 到服务器 /tmp/social.jar
# 启动: cd /tmp && nohup java -jar social.jar --server.port=8080 > /tmp/social.log 2>&1 &
```

### Android
```bash
cd E:\project6\android
./gradlew assembleDebug
# APK 输出: app/build/outputs/apk/debug/app-debug.apk
```
