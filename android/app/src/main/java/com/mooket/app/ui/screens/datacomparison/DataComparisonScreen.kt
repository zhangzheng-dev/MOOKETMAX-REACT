package com.mooket.app.ui.screens.datacomparison

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mooket.app.data.model.FactoryTrendData
import com.mooket.app.ui.theme.*

/**
 * 数据对比页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataComparisonScreen(
    country: String,
    factoryNos: List<String>,
    productName: String,
    category: String,
    excludeFactoryNo: String? = null,
    onBackClick: () -> Unit,
    viewModel: DataComparisonViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(country, factoryNos, productName, category) {
        viewModel.initialize(country, factoryNos, productName, category, excludeFactoryNo)
    }

    Scaffold(
        topBar = {
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
                            text = "数据对比",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
                .background(Background)
        ) {
            Divider(color = Border, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
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
                    Text(text = uiState.error ?: "加载失败", color = Error)
                }
            } else {
                // 顶部：产品信息 + 厂号选择器（不刷新）
                FactorySelectorSection(
                    productInfo = "$country${factoryNos.firstOrNull() ?: ""} $productName-平替产品",
                    selectedCount = uiState.selectedFactories.count { it != excludeFactoryNo },
                    allFactories = uiState.allFactories.filter { it != excludeFactoryNo },
                    selectedFactories = uiState.selectedFactories.filterNot { it == excludeFactoryNo }.toSet(),
                    excludeFactoryNo = excludeFactoryNo,
                    onToggleFactory = { viewModel.toggleFactory(it, excludeFactoryNo) }
                )

                // 分割线
                Divider(color = Border, thickness = 8.dp, modifier = Modifier.fillMaxWidth())

                // 内容区：加载中覆盖层
                val activeSelectedFactories = uiState.selectedFactories.filterNot { it == excludeFactoryNo }.toSet()
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 中间：价格卡片（只有选中厂号时才显示）
                        if (activeSelectedFactories.isNotEmpty()) {
                            uiState.comparisonData?.let { data ->
                                val filteredFactories = data.factories.filter { activeSelectedFactories.contains(it.factoryNo) }
                                if (filteredFactories.isNotEmpty()) {
                                    PriceCardsSection(
                                        country = country,
                                        factories = filteredFactories,
                                        selectedDateIndex = uiState.selectedDateIndex,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }

                        // 底部：趋势图（只有选中厂号时才显示）
                        if (activeSelectedFactories.isNotEmpty()) {
                            uiState.comparisonData?.let { data ->
                                val filteredFactories = data.factories.filter { activeSelectedFactories.contains(it.factoryNo) }
                                if (filteredFactories.isNotEmpty()) {
                                    TrendChartSection(
                                        factories = filteredFactories,
                                        selectedDateIndex = uiState.selectedDateIndex,
                                        onDateSelected = { viewModel.selectDateIndex(it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 内容区加载中覆盖层
                    if (uiState.isContentLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Background.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 顶部厂号选择器区域
 */
@Composable
private fun FactorySelectorSection(
    productInfo: String,
    selectedCount: Int,
    allFactories: List<String>,
    selectedFactories: Set<String>,
    excludeFactoryNo: String?,
    onToggleFactory: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        // 标题行：产品信息 + 数量
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = productInfo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
            Text(
                text = "$selectedCount/6",
                fontSize = 14.sp,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 厂号选择chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            allFactories.forEach { factoryNo ->
                val isSelected = selectedFactories.contains(factoryNo)
                val chipColor = if (isSelected) Primary else TextSecondary
                val bgColor = if (isSelected) PrimaryLight else Color(0xFFF5F5F5)
                val borderColor = if (isSelected) Primary else Border

                Box(
                    modifier = Modifier
                        .background(bgColor, RoundedCornerShape(2.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(2.dp))
                        .clickable {
                            val effectiveSize = selectedFactories.count { it != excludeFactoryNo }
                            if (!isSelected && effectiveSize >= 6) return@clickable
                            onToggleFactory(factoryNo)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = factoryNo,
                        fontSize = 14.sp,
                        color = chipColor
                    )
                }
            }
        }
    }
}

/**
 * 价格卡片区域
 */
@Composable
private fun PriceCardsSection(
    country: String,
    factories: List<FactoryTrendData>,
    selectedDateIndex: Int,
    modifier: Modifier = Modifier
) {
    val chartColors = listOf(
        Primary,
        Color(0xFFFC9E39),
        Color(0xFFE438AE),
        Color(0xFF0C40DD),
        Color(0xFF7B61FF),
        Color(0xFF00C53F)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // 日期标题
        val today = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault()).format(java.util.Date())
        Text(
            text = "$today 报盘价格走势",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 卡片按每行2个分组
        var globalIndex = 0
        factories.chunked(2).forEach { rowFactories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFactories.forEach { factory ->
                    val cardColor = chartColors[globalIndex % chartColors.size]
                    val point = factory.trend.getOrNull(selectedDateIndex)

                    PriceCard(
                        factoryNo = "$country${factory.factoryNo}",
                        priceRange = formatPriceRange(factory),
                        dailyAvg = point?.avgPrice,
                        offerCount = point?.offerCount,
                        borderColor = cardColor,
                        modifier = Modifier.weight(1f)
                    )
                    globalIndex++
                }
                // 如果不足2个，填满剩余空间
                if (rowFactories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * 价格卡片
 */
@Composable
private fun PriceCard(
    factoryNo: String,
    priceRange: String,
    dailyAvg: Double?,
    offerCount: Int?,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        // 厂号
        Text(
            text = factoryNo,
            fontSize = 12.sp,
            color = TextPrimary
        )

        // 价格区间
        Text(
            text = priceRange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Divider(color = Border, modifier = Modifier.padding(vertical = 4.dp))

        // 日均价值和报盘数
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "日均价",
                    fontSize = 10.sp,
                    color = TextHint
                )
                Text(
                    text = dailyAvg?.let { "¥${String.format("%.1f", it)}" } ?: "--",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "报盘数",
                    fontSize = 10.sp,
                    color = TextHint
                )
                Text(
                    text = offerCount?.toString() ?: "--",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

/**
 * 趋势图区域
 */
@Composable
private fun TrendChartSection(
    factories: List<FactoryTrendData>,
    selectedDateIndex: Int,
    onDateSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chartColors = listOf(
        Primary,
        Color(0xFFFC9E39),
        Color(0xFFE438AE),
        Color(0xFF0C40DD),
        Color(0xFF7B61FF),
        Color(0xFF00C53F)
    )

    Column(modifier = modifier.fillMaxWidth().padding(bottom = 60.dp)) {
        val firstFactory = factories.firstOrNull()

        // 图表（可点击）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val trendSize = firstFactory?.trend?.size ?: 0
                        if (trendSize > 0) {
                            val stepX = if (trendSize > 1) size.width.toFloat() / (trendSize - 1) else size.width.toFloat()
                            val clickedIndex = (offset.x / stepX).toInt().coerceIn(0, trendSize - 1)
                            onDateSelected(clickedIndex)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (factories.isEmpty()) return@Canvas

                val allPrices = factories.flatMap { it.trend.mapNotNull { p -> p.avgPrice } }
                if (allPrices.isEmpty()) return@Canvas

                val minPrice = allPrices.minOrNull() ?: return@Canvas
                val maxPrice = allPrices.maxOrNull() ?: return@Canvas
                val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice

                val width = size.width
                val height = size.height
                val trendSize = factories.firstOrNull()?.trend?.size ?: 0
                val stepX = if (trendSize > 1) width / (trendSize - 1) else width

                // 绘制网格线
                val gridColor = Color(0xFFDEE4E1)
                for (i in 0..8) {
                    val y = height * i / 8
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }

                // 绘制每个厂号的线条和节点
                factories.forEachIndexed { index, factory ->
                    val color = chartColors[index % chartColors.size]
                    val path = Path()
                    var firstPoint = true

                    factory.trend.forEachIndexed { pointIndex, point ->
                        if (point.avgPrice != null) {
                            val x = pointIndex * stepX
                            val y = height - ((point.avgPrice - minPrice) / priceRange * height).toFloat()

                            if (firstPoint) {
                                path.moveTo(x, y)
                                firstPoint = false
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 绘制所有数据点圆点
                    factory.trend.forEachIndexed { pointIndex, point ->
                        if (point.avgPrice != null) {
                            val x = pointIndex * stepX
                            val y = height - ((point.avgPrice - minPrice) / priceRange * height).toFloat()
                            val isSelected = pointIndex == selectedDateIndex

                            // 外圈白色
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                            // 内圈颜色
                            drawCircle(
                                color = color,
                                radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }

        // X轴日期标签
        val trendSize = firstFactory?.trend?.size ?: 0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val datesToShow = if (trendSize > 7) {
                listOf(0, trendSize / 4, trendSize / 2, trendSize * 3 / 4, trendSize - 1)
            } else {
                trendSize.coerceAtMost(5).let { n -> (0 until n).toList() }
            }

            datesToShow.forEach { index ->
                if (index < trendSize) {
                    val date = firstFactory?.trend?.getOrNull(index)?.date ?: ""
                    val isSelected = index == selectedDateIndex
                    Text(
                        text = date,
                        fontSize = 10.sp,
                        color = if (isSelected) Primary else TextHint,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.clickable { onDateSelected(index) }
                    )
                }
            }
        }

        // 悬浮提示（显示在图表下方）
        if (selectedDateIndex in 0 until trendSize) {
            val selectedDate = firstFactory?.trend?.getOrNull(selectedDateIndex)?.date ?: ""
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = selectedDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    factories.forEachIndexed { idx, factory ->
                        val color = chartColors[idx % chartColors.size]
                        val price = factory.trend.getOrNull(selectedDateIndex)?.avgPrice
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = factory.factoryNo,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = price?.let { "¥${String.format("%.1f", it)}" } ?: "--",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPriceRange(factory: FactoryTrendData): String {
    val prices = factory.trend.mapNotNull { it.avgPrice }
    if (prices.isEmpty()) return "--"
    val min = prices.minOrNull() ?: return "--"
    val max = prices.maxOrNull() ?: return "--"
    if (min == max) return "¥${String.format("%.1f", min)}/kg"
    return "¥${String.format("%.1f", min)}-${String.format("%.1f", max)}/kg"
}