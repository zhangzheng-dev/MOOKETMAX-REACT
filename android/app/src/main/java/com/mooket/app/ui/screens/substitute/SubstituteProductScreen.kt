package com.mooket.app.ui.screens.substitute

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mooket.app.R
import com.mooket.app.data.model.DailyPrice
import com.mooket.app.data.model.EmployeeOfferItem
import com.mooket.app.data.model.MerchantOfferGroup
import com.mooket.app.data.model.MerchantOption
import com.mooket.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 平替产品页
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubstituteProductScreen(
    country: String,
    factoryNo: String,
    productName: String,
    category: String,
    onBackClick: () -> Unit,
    onFactoryClick: (String, String) -> Unit,
    onDataComparisonClick: (String, List<String>, String, String, String) -> Unit,
    viewModel: SubstituteProductViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var originalText by remember { mutableStateOf("") }

    LaunchedEffect(country, factoryNo, productName) {
        viewModel.loadData(country, factoryNo, productName, category)
    }

    // 加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .debounce(100)
            .collect { lastIndex ->
                if (lastIndex != null) {
                    val detail = uiState.detail
                    if (!uiState.isLoading && !uiState.isListLoading && detail != null && detail.totalPages > detail.page) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIos,
                                contentDescription = "返回",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onBackClick() }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "平替产品",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(22.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Divider(color = Border, thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(Background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.error ?: "加载失败", color = TextHint)
                }
            } else {
                uiState.substituteProduct?.let { substituteProduct ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .animateContentSize()
                    ) {
                        // 平替产品信息卡片
                        SubstituteProductInfoCard(
                            country = country,
                            factoryNo = factoryNo,
                            productName = productName,
                            priceMin = uiState.detail?.priceMin,
                            priceMax = uiState.detail?.priceMax,
                            factories = substituteProduct.factories,
                            selectedFactoryNo = uiState.selectedFactoryNo,
                            onFactorySelect = { viewModel.selectFactory(it) },
                            onDataCompareClick = {
                            val allFactoryNos = listOf(factoryNo) + substituteProduct.factories.map { it.factoryNo }
                            onDataComparisonClick(country, allFactoryNos, productName, category, factoryNo)
                        }
                        )

                        // 平替产品选项与筛选项之间的绿色分隔线
                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 2.dp,
                            color = Primary
                        )

                        // 价格详情卡片（当前选中厂号的数据）
                        uiState.detail?.let { detail ->
                            SortBar(
                                currentSort = uiState.sortBy,
                                currentType = uiState.offerType,
                                onSortChange = { viewModel.switchSortBy(it) },
                                onTypeChange = { viewModel.switchOfferType(it) }
                            )

                            // 计算商家选项
                            val merchantOptions = remember(detail.merchantOffers) {
                                detail.merchantOffers.mapNotNull { group ->
                                    group.merchantId?.let { id -> MerchantOption(id, group.merchantName ?: "未知商家") }
                                }.distinctBy { it.id }
                            }

                            // 计算地区选项
                            val regionOptions = remember(detail.merchantOffers) {
                                detail.merchantOffers.mapNotNull { group ->
                                    group.employeeOffers.mapNotNull { it.goodsLocation }
                                }.flatten().distinct()
                            }

                            FilterBar(
                                activeFilters = uiState.activeFilters,
                                priceMin = uiState.priceMin,
                                priceMax = uiState.priceMax,
                                goodsTypes = uiState.goodsTypes,
                                feedingTypes = uiState.feedingTypes,
                                tags = uiState.tags,
                                offerType = uiState.offerType,
                                isFamousMerchant = uiState.isFamousMerchant,
                                selectedMerchants = uiState.selectedMerchants,
                                regions = uiState.regions,
                                merchantOptions = merchantOptions,
                                regionOptions = regionOptions,
                                onFilterClick = { viewModel.toggleFilter(it) },
                                onPriceRangeChange = { min, max -> viewModel.setPriceRange(min, max) },
                                onGoodsTypeToggle = { viewModel.toggleGoodsType(it) },
                                onFeedingTypeToggle = { viewModel.toggleFeedingType(it) },
                                onTagToggle = { viewModel.toggleTag(it) },
                                onFamousMerchantToggle = { viewModel.toggleFamousMerchant() },
                                onMerchantToggle = { viewModel.toggleMerchant(it) },
                                onRegionToggle = { viewModel.toggleRegion(it) },
                                onClearFilters = { viewModel.clearFilters() }
                            )

                            // 应用筛选逻辑
                            val filteredMerchantOffers = remember(detail.merchantOffers, uiState.priceMin, uiState.priceMax, uiState.goodsTypes, uiState.feedingTypes, uiState.tags, uiState.isFamousMerchant, uiState.selectedMerchants, uiState.regions) {
                                detail.merchantOffers.mapNotNull { group ->
                                    if (uiState.isFamousMerchant && !group.isFamousMerchant) {
                                        return@mapNotNull null
                                    }
                                    if (uiState.selectedMerchants.isNotEmpty()) {
                                        if (group.merchantId == null || !uiState.selectedMerchants.contains(group.merchantId)) {
                                            return@mapNotNull null
                                        }
                                    }
                                    if (uiState.regions.isNotEmpty()) {
                                        val hasRegion = group.employeeOffers.any { offer ->
                                            offer.goodsLocation != null && uiState.regions.any { region -> offer.goodsLocation!!.contains(region) }
                                        }
                                        if (!hasRegion) return@mapNotNull null
                                    }
                                    val filteredOffers = group.employeeOffers.filter { offer ->
                                        var passes = true
                                        if (uiState.priceMin != null || uiState.priceMax != null) {
                                            val price = offer.price?.replace(Regex("[^\\d.]"), "")?.toDoubleOrNull()
                                            if (price != null) {
                                                if (uiState.priceMin != null && price < uiState.priceMin!!.toDouble()) passes = false
                                                if (uiState.priceMax != null && price > uiState.priceMax!!.toDouble()) passes = false
                                            } else {
                                                passes = false
                                            }
                                        }
                                        if (uiState.goodsTypes.isNotEmpty()) {
                                            if (offer.goodsType == null || !uiState.goodsTypes.contains(offer.goodsType)) {
                                                passes = false
                                            }
                                        }
                                        if (uiState.feedingTypes.isNotEmpty()) {
                                            val hasFeedingType = uiState.feedingTypes.any { ft ->
                                                offer.tags?.contains(ft) == true
                                            }
                                            if (!hasFeedingType) passes = false
                                        }
                                        if (uiState.tags.isNotEmpty()) {
                                            if (offer.tags == null || !uiState.tags.any { tag -> offer.tags!!.contains(tag) }) {
                                                passes = false
                                            }
                                        }
                                        passes
                                    }
                                    if (filteredOffers.isNotEmpty()) {
                                        group.copy(employeeOffers = filteredOffers, offerCount = filteredOffers.size)
                                    } else null
                                }
                            }

                            // 筛选项与列表之间的分隔线
                            Divider(color = Border, thickness = 0.5.dp)

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState
                            ) {
                                itemsIndexed(filteredMerchantOffers) { index, merchantGroup ->
                                    Column {
                                        MerchantOfferItemSubstitute(
                                            merchantGroup = merchantGroup,
                                            offerType = uiState.offerType,
                                            onClick = { },
                                            onCopyPhone = { phone ->
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("phone", phone)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "已复制手机号", Toast.LENGTH_SHORT).show()
                                            },
                                            onCallClick = { phone ->
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:$phone")
                                                }
                                                context.startActivity(intent)
                                            },
                                            onViewOriginalText = { text ->
                                                originalText = text
                                                showBottomSheet = true
                                            }
                                        )
                                    }
                                    if (index < filteredMerchantOffers.lastIndex) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Divider(color = Border, thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }

                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (detail.page < detail.totalPages) {
                                            if (uiState.isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Primary,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(
                                                    text = "加载更多",
                                                    fontSize = 11.sp,
                                                    color = Primary
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "没有更多了～",
                                                fontSize = 11.sp,
                                                color = TextHint
                                            )
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 底部抽屉 - 原文详情
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "原文内容",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (originalText.isBlank()) "抱歉，暂无原文！" else originalText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = if (originalText.isBlank()) Color(0xFF9DA4A3) else Color(0xFF3C4947)
                )
            }
        }
    }
}

/**
 * 平替产品信息卡片（顶部厂号选择栏）
 */
@Composable
private fun SubstituteProductInfoCard(
    country: String,
    factoryNo: String,
    productName: String,
    priceMin: Double?,
    priceMax: Double?,
    factories: List<com.mooket.app.data.model.SubstituteFactory>,
    selectedFactoryNo: String,
    onFactorySelect: (String) -> Unit,
    onDataCompareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 第一行：产品信息 + 数据对比按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：国家+厂号+产品名
            Column {
                Text(
                    text = "${country}${factoryNo} · ${productName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 近2日报盘价格区间
                val priceText = if (priceMin != null && priceMax != null) {
                    "¥${formatPrice(priceMin)}-${formatPrice(priceMax)}/kg"
                } else if (priceMin != null) {
                    "¥${formatPrice(priceMin)}/kg"
                } else if (priceMax != null) {
                    "¥${formatPrice(priceMax)}/kg"
                } else {
                    "暂无报价"
                }
                Text(
                    text = "近2日报盘价格区间：$priceText",
                    fontSize = 11.sp,
                    color = TextHint
                )
            }

            // 右侧：数据对比按钮
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 48.dp)
                    .clickable { onDataCompareClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.frame_42732),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_data_compare_icon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "数据对比",
                        fontSize = 10.sp,
                        color = Primary
                    )
                }
            }
        }

        // 分隔区域（延伸至两侧）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Divider(color = Border)
                Spacer(modifier = Modifier.weight(1f))
                Divider(color = Border)
            }
        }

        // 第二行：平替产品选择
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "平替产品",
                fontSize = 12.sp,
                color = Primary
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_platform_product),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Primary
            )
        }

        // 第三行：可选厂号（横向滚动选择）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            factories.forEach { factory ->
                val isSelected = factory.factoryNo == selectedFactoryNo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Primary else Color(0xFFF3F6F5))
                        .clickable { onFactorySelect(factory.factoryNo) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = country,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = factory.factoryNo,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (factory.priceMin != null && factory.priceMax != null) {
                                "¥${formatPrice(factory.priceMin)}-${formatPrice(factory.priceMax)}"
                            } else if (factory.priceMin != null) {
                                "¥${formatPrice(factory.priceMin)}"
                            } else if (factory.priceMax != null) {
                                "¥${formatPrice(factory.priceMax)}"
                            } else {
                                "暂无报价"
                            },
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextHint
                        )
                    }
                }
            }
        }
    }
}

/**
 * 价格信息卡片（平替产品详情页用）
 */
@Composable
private fun PriceInfoCardSubstitute(
    detail: com.mooket.app.data.model.SubstituteProductDetail,
    offerType: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 标题行：产品名
        Text(
            text = detail.productName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 价格信息行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (offerType == "offer") "近2日报盘价格区间（RMB）" else "近2日求购价格区间（RMB）",
                    fontSize = 10.sp,
                    color = TextHint
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    val hasPrice = detail.priceMin != null || detail.priceMax != null
                    val priceText = if (detail.priceMin != null && detail.priceMax != null) {
                        "¥${formatPrice(detail.priceMin)}-${formatPrice(detail.priceMax)}"
                    } else if (detail.priceMin != null) {
                        "¥${formatPrice(detail.priceMin)}"
                    } else if (detail.priceMax != null) {
                        "¥${formatPrice(detail.priceMax)}"
                    } else {
                        "暂无报价"
                    }

                    Text(
                        text = priceText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )

                    if (hasPrice) {
                        Text(
                            text = "/kg",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 涨跌指示
                    if (detail.priceChange != null && detail.priceChangeRate != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (detail.priceChange >= 0) R.drawable.ic_price_trend_up else R.drawable.ic_price_trend_down
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp, 6.dp),
                                tint = if (detail.priceChange >= 0) Color(0xFFA53321) else Primary
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            Text(
                                text = "${if (detail.priceChange >= 0) "+" else ""}${formatPrice(detail.priceChange)}  ${formatPrice(detail.priceChangeRate)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (detail.priceChange >= 0) Color(0xFFA53321) else Primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 排序栏
 */
@Composable
private fun SortBar(
    currentSort: String,
    currentType: String,
    onSortChange: (String) -> Unit,
    onTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Background)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                TypeTab("报盘", currentType == "offer", onClick = { onTypeChange("offer") })
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                SortTab("综合推荐", currentSort == "comprehensive", onClick = { onSortChange("comprehensive") })
                SortTab("发布时间", currentSort == "publish_time", onClick = { onSortChange("publish_time") })
                PriceSortButton(
                    isPriceAscActive = currentSort == "price_asc",
                    isPriceDescActive = currentSort == "price_desc",
                    onPriceAscClick = { onSortChange("price_asc") },
                    onPriceDescClick = { onSortChange("price_desc") }
                )
            }
        }
    }
}

/**
 * 排序标签
 */
@Composable
private fun SortTab(text: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) TextPrimary else TextHint
        )
        if (isActive) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(3.dp)
                    .background(Primary, RoundedCornerShape(1.dp))
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

/**
 * 价格排序按钮
 */
@Composable
private fun PriceSortButton(
    isPriceAscActive: Boolean,
    isPriceDescActive: Boolean,
    onPriceAscClick: () -> Unit,
    onPriceDescClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "价格",
                fontSize = 14.sp,
                fontWeight = if (isPriceAscActive || isPriceDescActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isPriceAscActive || isPriceDescActive) TextPrimary else TextHint
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp, 8.dp)
                        .background(
                            if (isPriceAscActive) Primary else Color.Transparent,
                            RoundedCornerShape(1.dp)
                        )
                        .clickable { onPriceAscClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "升序",
                        tint = if (isPriceAscActive) Color.White else Color(0xFF8B8B8B),
                        modifier = Modifier.size(12.dp, 8.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp, 8.dp)
                        .background(
                            if (isPriceDescActive) Primary else Color.Transparent,
                            RoundedCornerShape(1.dp)
                        )
                        .clickable { onPriceDescClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "降序",
                        tint = if (isPriceDescActive) Color.White else Color(0xFF8B8B8B),
                        modifier = Modifier.size(12.dp, 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(3.dp)
                .background(if (isPriceAscActive || isPriceDescActive) Primary else Color.Transparent, RoundedCornerShape(1.dp))
        )
    }
}

/**
 * 类型标签
 */
@Composable
private fun TypeTab(text: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) TextPrimary else TextHint
        )
        if (isActive) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(3.dp)
                    .background(Primary, RoundedCornerShape(1.dp))
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

/**
 * 筛选栏
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBar(
    activeFilters: Set<String>,
    priceMin: String?,
    priceMax: String?,
    goodsTypes: Set<String>,
    feedingTypes: Set<String>,
    tags: Set<String>,
    offerType: String,
    isFamousMerchant: Boolean,
    selectedMerchants: Set<Long>,
    regions: Set<String>,
    merchantOptions: List<MerchantOption>,
    regionOptions: List<String>,
    onFilterClick: (String) -> Unit,
    onPriceRangeChange: (String?, String?) -> Unit,
    onGoodsTypeToggle: (String) -> Unit,
    onFeedingTypeToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onFamousMerchantToggle: () -> Unit,
    onMerchantToggle: (Long) -> Unit,
    onRegionToggle: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var currentFilterType by remember { mutableStateOf("") }

    val hasActiveFilters = activeFilters.isNotEmpty() || priceMin != null || priceMax != null ||
            goodsTypes.isNotEmpty() || feedingTypes.isNotEmpty() || tags.isNotEmpty() ||
            isFamousMerchant || selectedMerchants.isNotEmpty() || regions.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                text = "知名商家",
                isActive = isFamousMerchant,
                onClick = { onFamousMerchantToggle() }
            )
            FilterChip(
                text = "商家筛选",
                isActive = selectedMerchants.isNotEmpty(),
                onClick = {
                    currentFilterType = "商家筛选"
                    showFilterSheet = true
                }
            )
            FilterChip(
                text = "地区",
                isActive = regions.isNotEmpty(),
                onClick = {
                    currentFilterType = "地区"
                    showFilterSheet = true
                }
            )
            FilterChip(
                text = "价格区间",
                isActive = priceMin != null || priceMax != null,
                onClick = {
                    currentFilterType = "价格区间"
                    showFilterSheet = true
                }
            )
            FilterChip(
                text = "货物类型",
                isActive = goodsTypes.isNotEmpty(),
                onClick = {
                    currentFilterType = "货物类型"
                    showFilterSheet = true
                }
            )
            FilterChip(
                text = "饲养方式",
                isActive = feedingTypes.isNotEmpty(),
                onClick = {
                    currentFilterType = "饲养方式"
                    showFilterSheet = true
                }
            )
            FilterChip(
                text = "标签",
                isActive = tags.isNotEmpty(),
                onClick = {
                    currentFilterType = "标签"
                    showFilterSheet = true
                }
            )
            if (hasActiveFilters) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFFFE4E4))
                        .clickable { onClearFilters() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "清除",
                        fontSize = 12.sp,
                        color = Color(0xFFFF4444)
                    )
                }
            }
        }

        if (hasActiveFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (priceMin != null || priceMax != null) {
                    val priceText = when {
                        priceMin != null && priceMax != null -> "¥$priceMin-$priceMax"
                        priceMin != null -> "≥¥$priceMin"
                        priceMax != null -> "≤¥$priceMax"
                        else -> ""
                    }
                    SelectedFilterTag(text = priceText, onRemove = { onPriceRangeChange(null, null) })
                }
                goodsTypes.forEach { type ->
                    SelectedFilterTag(text = type, onRemove = { onGoodsTypeToggle(type) })
                }
                feedingTypes.forEach { type ->
                    SelectedFilterTag(text = type, onRemove = { onFeedingTypeToggle(type) })
                }
                tags.forEach { tag ->
                    SelectedFilterTag(text = tag, onRemove = { onTagToggle(tag) })
                }
                selectedMerchants.forEach { merchantId ->
                    val merchantName = merchantOptions.find { it.id == merchantId }?.name ?: "商家$merchantId"
                    SelectedFilterTag(text = merchantName, onRemove = { onMerchantToggle(merchantId) })
                }
                regions.forEach { region ->
                    SelectedFilterTag(text = region, onRemove = { onRegionToggle(region) })
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            when (currentFilterType) {
                "商家筛选" -> MerchantFilterSheet(
                    selectedMerchants = selectedMerchants,
                    merchantOptions = merchantOptions,
                    onToggle = onMerchantToggle,
                    onDismiss = { showFilterSheet = false }
                )
                "地区" -> RegionFilterSheet(
                    selectedRegions = regions,
                    regionOptions = regionOptions,
                    onToggle = onRegionToggle,
                    onDismiss = { showFilterSheet = false }
                )
                "价格区间" -> PriceFilterSheet(
                    priceMin = priceMin,
                    priceMax = priceMax,
                    onPriceChange = onPriceRangeChange,
                    onDismiss = { showFilterSheet = false }
                )
                "货物类型" -> GoodsTypeFilterSheet(
                    selectedTypes = goodsTypes,
                    onToggle = onGoodsTypeToggle,
                    onDismiss = { showFilterSheet = false }
                )
                "饲养方式" -> FeedingTypeFilterSheet(
                    selectedTypes = feedingTypes,
                    onToggle = onFeedingTypeToggle,
                    onDismiss = { showFilterSheet = false }
                )
                "标签" -> TagsFilterSheet(
                    selectedTags = tags,
                    offerType = offerType,
                    onToggle = onTagToggle,
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}

/**
 * 已选筛选标签
 */
@Composable
private fun SelectedFilterTag(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(PrimaryLight)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = Primary)
        Box(modifier = Modifier.clickable { onRemove() }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "移除",
                modifier = Modifier.size(12.dp),
                tint = Primary
            )
        }
    }
}

/**
 * 筛选芯片
 */
@Composable
private fun FilterChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(if (isActive) Primary else Color(0xFFF3F6F5))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isActive) Color.White else TextPrimary
        )
    }
}

/**
 * 价格区间筛选 Sheet
 */
@Composable
private fun PriceFilterSheet(
    priceMin: String?,
    priceMax: String?,
    onPriceChange: (String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var minText by remember { mutableStateOf(priceMin ?: "") }
    var maxText by remember { mutableStateOf(priceMax ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "价格区间", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = minText,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                    val parts = filtered.split(".")
                    val formatted = when {
                        parts.size > 2 -> parts[0] + "." + parts[1]
                        parts.size == 2 && parts[1].length > 2 -> parts[0] + "." + parts[1].take(2)
                        else -> filtered
                    }
                    minText = formatted
                    errorText = null
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("最低价", color = TextHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                )
            )

            Text(text = "至", color = TextHint)

            OutlinedTextField(
                value = maxText,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                    val parts = filtered.split(".")
                    val formatted = when {
                        parts.size > 2 -> parts[0] + "." + parts[1]
                        parts.size == 2 && parts[1].length > 2 -> parts[0] + "." + parts[1].take(2)
                        else -> filtered
                    }
                    maxText = formatted
                    errorText = null
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("最高价", color = TextHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                )
            )
        }

        if (errorText != null) {
            Text(
                text = errorText!!,
                color = Color(0xFFFF4444),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "不填最低价则不过滤低价，不填最高价则不过滤高价",
            fontSize = 12.sp,
            color = TextHint
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    minText = ""
                    maxText = ""
                    errorText = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("重置")
            }
            Button(
                onClick = {
                    val min = minText.toDoubleOrNull()
                    val max = maxText.toDoubleOrNull()
                    if (min != null && max != null && min > max) {
                        errorText = "最低价不能大于最高价"
                    } else {
                        onPriceChange(minText.takeIf { it.isNotEmpty() }, maxText.takeIf { it.isNotEmpty() })
                        onDismiss()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("确定")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 货物类型筛选 Sheet
 */
@Composable
private fun GoodsTypeFilterSheet(
    selectedTypes: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val goodsTypes = listOf("现货", "半期货", "期货")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "货物类型", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(12.dp))

        goodsTypes.forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(type) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = type, fontSize = 14.sp)
                Checkbox(
                    checked = selectedTypes.contains(type),
                    onCheckedChange = { onToggle(type) },
                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                )
            }
            Divider(color = Border, thickness = 0.5.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("确定")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 饲养方式筛选 Sheet
 */
@Composable
private fun FeedingTypeFilterSheet(
    selectedTypes: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val feedingTypes = listOf("草饲", "谷饲")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "饲养方式", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(16.dp))

        feedingTypes.forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(type) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = type, fontSize = 14.sp)
                Checkbox(
                    checked = selectedTypes.contains(type),
                    onCheckedChange = { onToggle(type) },
                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                )
            }
            Divider(color = Border, thickness = 0.5.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("确定")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 标签筛选 Sheet
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsFilterSheet(
    selectedTags: Set<String>,
    offerType: String,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val offerTags = listOf("今日报价", "急售", "正品", "低价", "批发")
    val inquiryTags = listOf("长期采购", "急需", "大量采购", "同行转售")

    val tags = if (offerType == "offer") offerTags else inquiryTags

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "标签筛选", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Primary else Color(0xFFF3F6F5))
                        .clickable { onToggle(tag) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("确定")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 商家筛选 Sheet
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MerchantFilterSheet(
    selectedMerchants: Set<Long>,
    merchantOptions: List<MerchantOption>,
    onToggle: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val filteredMerchants = remember(searchText, merchantOptions) {
        if (searchText.isEmpty()) merchantOptions
        else merchantOptions.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "商家筛选", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索商家", color = TextHint, fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedMerchants.isNotEmpty()) {
            Text(text = "已选商家", fontSize = 12.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                selectedMerchants.forEach { merchantId: Long ->
                    val merchant: MerchantOption? = merchantOptions.find { it.id == merchantId }
                    if (merchant != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = merchant.name, fontSize = 11.sp, color = Primary)
                                Box(modifier = Modifier.padding(start = 2.dp).clickable { onToggle(merchantId) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "移除",
                                        modifier = Modifier.size(12.dp),
                                        tint = Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(max = 400.dp)
        ) {
            items(filteredMerchants.size) { index ->
                val merchant = filteredMerchants[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(merchant.id) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = merchant.name, fontSize = 13.sp)
                    Checkbox(
                        checked = selectedMerchants.contains(merchant.id),
                        onCheckedChange = { onToggle(merchant.id) },
                        colors = CheckboxDefaults.colors(checkedColor = Primary),
                        modifier = Modifier.height(32.dp)
                    )
                }
                if (index < filteredMerchants.size - 1) {
                    Divider(color = Border, thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("确定")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 地区筛选 Sheet
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RegionFilterSheet(
    selectedRegions: Set<String>,
    regionOptions: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "地区筛选", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            regionOptions.forEach { region ->
                val isSelected = selectedRegions.contains(region)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Primary else Color(0xFFF3F6F5))
                        .clickable { onToggle(region) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = region,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("确定")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 商家报盘分组项
 */
@Composable
private fun MerchantOfferItemSubstitute(
    merchantGroup: MerchantOfferGroup,
    offerType: String,
    onClick: () -> Unit,
    onCopyPhone: (String) -> Unit,
    onCallClick: (String) -> Unit,
    onViewOriginalText: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val copyPhoneCallback: () -> Unit = {
        merchantGroup.merchantPhone?.let { phone ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("phone", phone)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "已复制手机号", Toast.LENGTH_SHORT).show()
        }
    }
    val callPhoneCallback: () -> Unit = {
        merchantGroup.merchantPhone?.let { phone ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            context.startActivity(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBFFFE))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val merchantName = merchantGroup.merchantName ?: "商家"
                val isNameLong = merchantName.length > 10
                val isFamous = merchantGroup.isFamousMerchant

                if (isNameLong) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isFamous) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF254d5a), RoundedCornerShape(2.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "知名商家",
                                                fontSize = 9.sp,
                                                color = Color.White
                                            )
                                        }
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_merchant_crown),
                                            contentDescription = "知名商家",
                                            modifier = Modifier
                                                .width(26.dp)
                                                .height(18.dp)
                                                .offset(x = (-10).dp)
                                        )
                                    }
                                    Text(
                                        text = merchantName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .widthIn(max = 170.dp)
                                            .offset(x = (-8).dp)
                                    )
                                } else {
                                    Text(
                                        text = merchantName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (isFamous) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF254d5a), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "知名商家",
                                        fontSize = 9.sp,
                                        color = Color.White
                                    )
                                }
                                Image(
                                    painter = painterResource(id = R.drawable.ic_merchant_crown),
                                    contentDescription = "知名商家",
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height(18.dp)
                                        .offset(x = (-10).dp)
                                )
                            }
                            Text(
                                text = merchantName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .widthIn(max = 170.dp)
                                    .offset(x = (-8).dp)
                            )
                        } else {
                            Text(
                                text = merchantName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val minPrice = merchantGroup.employeeOffers.mapNotNull { it.price?.toDoubleOrNull() }.minOrNull()
                    val maxPrice = merchantGroup.employeeOffers.mapNotNull { it.price?.toDoubleOrNull() }.maxOrNull()

                    if (minPrice != null && maxPrice != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (minPrice == maxPrice) "¥$minPrice" else "¥$minPrice-$maxPrice",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                            Text(
                                text = "/kg",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary
                            )
                        }
                    } else {
                        Text(
                            text = "协商报价",
                            fontSize = 12.sp,
                            color = TextHint
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isExpanded = !isExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_right),
                            contentDescription = null,
                            colorFilter = if (!isExpanded) ColorFilter.tint(Color(0xFFBFCAC8)) else null,
                            modifier = Modifier
                                .size(12.dp, 16.dp)
                                .graphicsLayer { rotationZ = if (isExpanded) 270f else 90f }
                        )
                    }
                }
            }

            val allLocations = merchantGroup.employeeOffers.mapNotNull { extractCity(it.goodsLocation) }.filter { it.isNotEmpty() }.distinct()
            val allTypes = merchantGroup.employeeOffers.mapNotNull { it.goodsType }.filter { it.isNotEmpty() }.distinct()
            val allTags = merchantGroup.employeeOffers.mapNotNull { it.tags?.split(",")?.filter { t -> t.isNotBlank() }?.take(4) }.flatten().distinct().take(4)
            if (allLocations.isNotEmpty() || allTypes.isNotEmpty() || allTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (allLocations.isNotEmpty()) {
                        OfferTag(text = allLocations.joinToString("/"), bgColor = Color(0xFFF2F8F7), textColor = Primary, hasIcon = true)
                    }
                    if (allTypes.isNotEmpty()) {
                        OfferTag(text = allTypes.joinToString("/"), bgColor = Color(0xFFF2F3FF), textColor = Color(0xFF485B88), hasIcon = false)
                    }
                    allTags.forEach { tag ->
                        val (bgColor, txtColor) = when {
                            tag.contains("大日期") || tag.contains("日期") -> Color(0xFFF2F3FF) to Color(0xFF3163DC)
                            tag.contains("可开票") || tag.contains("票") -> Color(0xFFFFF5E4) to Color(0xFFA07D17)
                            tag.contains("品牌") -> Color(0xFFFFF0F0) to Color(0xFFDC3545)
                            else -> Color(0xFFF2F8F7) to Color(0xFF3C4947)
                        }
                        OfferTag(text = tag.trim(), bgColor = bgColor, textColor = txtColor, hasIcon = false)
                    }
                }
            }
        }

        if (isExpanded) {
            merchantGroup.employeeOffers.forEach { employeeOffer ->
                EmployeeOfferCardSubstitute(
                    employeeOffer = employeeOffer,
                    merchantPhone = merchantGroup.merchantPhone,
                    onCopyPhone = copyPhoneCallback,
                    onCallClick = callPhoneCallback,
                    onViewOriginalText = onViewOriginalText
                )
            }
        }
    }
}

/**
 * 员工报价卡片
 */
@Composable
private fun EmployeeOfferCardSubstitute(
    employeeOffer: EmployeeOfferItem,
    merchantPhone: String?,
    onViewOriginalText: (String) -> Unit = {},
    onCopyPhone: () -> Unit = {},
    onCallClick: () -> Unit = {}
) {
    val (weightValue, weightUnit) = remember(employeeOffer.weight) { parseWeight(employeeOffer.weight) }
    val hasWeight = weightValue.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFFFBFFFE),
                    RoundedCornerShape(4.dp)
                )
                .border(0.5.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (hasWeight) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_avatar),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = employeeOffer.userNickname ?: "员工",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            merchantPhone?.let { phone ->
                                Text(
                                    text = phone,
                                    fontSize = 12.sp,
                                    color = TextHint,
                                    modifier = Modifier.padding(start = 26.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_avatar),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = employeeOffer.userNickname ?: "员工",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            merchantPhone?.let { phone ->
                                Text(
                                    text = phone,
                                    fontSize = 12.sp,
                                    color = TextHint,
                                    modifier = Modifier.padding(start = 26.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (weightValue.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = weightValue,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (weightUnit.isNotEmpty()) {
                                    Text(
                                        text = weightUnit,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                        if (employeeOffer.price != null && employeeOffer.price != "协商报价") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "¥${employeeOffer.price}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                                Text(
                                    text = "/kg",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = TextPrimary
                                )
                            }
                        } else {
                            Text(
                                text = "协商报价",
                                fontSize = 12.sp,
                                color = TextHint
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(employeeOffer.publishTime),
                        fontSize = 11.sp,
                        color = Color(0xFF3C4947)
                    )

                    if (employeeOffer.goodsLocation?.isNotEmpty() == true) {
                        OfferTag(
                            text = extractCity(employeeOffer.goodsLocation),
                            bgColor = Color(0xFFF2F8F7),
                            textColor = Primary,
                            hasIcon = true
                        )
                    }
                    if (employeeOffer.goodsType?.isNotEmpty() == true) {
                        OfferTag(
                            text = employeeOffer.goodsType,
                            bgColor = Color(0xFFF3F6F5),
                            textColor = Color(0xFF3C4947),
                            hasIcon = false
                        )
                    }
                    employeeOffer.tags?.split(",")?.filter { it.isNotBlank() }?.take(4)?.forEach { tag ->
                        val (bgColor, txtColor) = when {
                            tag.contains("大日期") || tag.contains("日期") -> Color(0xFFF2F3FF) to Color(0xFF3163DC)
                            tag.contains("可开票") || tag.contains("票") -> Color(0xFFFFF5E4) to Color(0xFFA07D17)
                            tag.contains("整柜") || tag.contains("柜") -> Color(0xFFFFF0ED) to Color(0xFFD54941)
                            tag.contains("一口价") || tag.contains("价") -> Color(0xFFF3F6F5) to Color(0xFF3C4947)
                            else -> Color(0xFFF3F6F5) to Color(0xFF3C4947)
                        }
                        OfferTag(text = tag.trim(), bgColor = bgColor, textColor = txtColor, hasIcon = false)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmployeeActionButtonSubstitute(
                        icon = Icons.Default.Description,
                        text = "查看原文",
                        textColor = Color(0xFF3C4947),
                        onClick = { onViewOriginalText(employeeOffer.offerOriginalText ?: employeeOffer.offerType ?: "") },
                        iconPainter = painterResource(id = R.drawable.ic_book)
                    )

                    VerticalDividerSubstitute()

                    EmployeeActionButtonSubstitute(
                        icon = Icons.Default.ContentCopy,
                        text = "添加微信",
                        textColor = Color(0xFF3C4947),
                        onClick = onCopyPhone,
                        iconPainter = painterResource(id = R.drawable.ic_add_square)
                    )

                    VerticalDividerSubstitute()

                    EmployeeActionButtonSubstitute(
                        icon = Icons.Default.Call,
                        text = "拨打电话",
                        textColor = Primary,
                        onClick = onCallClick
                    )
                }
            }
        }
    }
}

private fun parseWeight(weight: String?): Pair<String, String> {
    if (weight.isNullOrBlank()) {
        return "" to ""
    }
    val regex = Regex("^([\\d.]+)(.*)$")
    val match = regex.find(weight.trim())
    return if (match != null) {
        val (value, unit) = match.destructured
        val numValue = value.toDoubleOrNull()
        val roundedValue = if (numValue != null) {
            val rounded = Math.round(numValue * 10.0) / 10.0
            if (rounded == rounded.toLong().toDouble()) {
                rounded.toLong().toString()
            } else {
                String.format("%.1f", rounded)
            }
        } else {
            value
        }
        roundedValue to unit.trim()
    } else {
        weight to ""
    }
}

private fun extractCity(location: String?): String {
    if (location.isNullOrBlank()) return ""
    return try {
        when {
            location.contains("/") -> location.substringAfter("/").trim()
            location.contains("\\") -> location.substringAfter("\\").trim()
            location.contains("省") -> location.substringAfter("省").trim()
            else -> location.trim()
        }
    } catch (e: Exception) {
        location
    }
}

private fun formatTime(timeString: String?): String {
    if (timeString.isNullOrEmpty()) return ""
    return try {
        val date = try {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(timeString)
        } catch (e: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(timeString)
            } catch (e: Exception) {
                null
            }
        } ?: return timeString.takeLast(5)

        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val dateCalendar = Calendar.getInstance().apply { time = date }

        val prefix = when {
            dateCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            dateCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "今天"
            dateCalendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            dateCalendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "昨天"
            else -> ""
        }
        if (prefix.isNotEmpty()) {
            "$prefix ${outputFormat.format(date)}"
        } else {
            outputFormat.format(date)
        }
    } catch (e: Exception) {
        timeString.takeLast(5)
    }
}

@Composable
private fun EmployeeActionButtonSubstitute(
    icon: ImageVector,
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null
) {
    Row(
        modifier = Modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (iconPainter != null) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor
        )
    }
}

@Composable
private fun VerticalDividerSubstitute() {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(13.dp)
            .background(Color(0xFF3C4947))
    )
}

@Composable
private fun OfferTag(
    text: String,
    bgColor: Color,
    textColor: Color,
    hasIcon: Boolean
) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(1.dp))
            .padding(horizontal = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (hasIcon) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(10.dp)
                )
            }
            Text(
                text = text,
                fontSize = 10.sp,
                color = textColor
            )
        }
    }
}

private fun formatPrice(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}
