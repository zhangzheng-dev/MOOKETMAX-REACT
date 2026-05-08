package com.mooket.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mooket.app.R
import com.mooket.app.data.model.HotSearchItem
import com.mooket.app.data.model.SearchHistory
import com.mooket.app.ui.theme.*

/**
 * 首页
 * 设计来源：首页.md 3.2节
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onSearchClick: (String) -> Unit,
    onProductClick: (Int, String, String) -> Unit,
    onCountryClick: (String, String) -> Unit,
    onBrandClick: (String, String) -> Unit,
    onMerchantClick: (Long, String) -> Unit,
    onFactoryClick: (String, String, String) -> Unit,
    onCountryProductClick: (String, String, String) -> Unit,
    onCountryFactoryProductClick: (String, String, String, String) -> Unit,
    onHomeCardsClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("牛", "猪")
    var expanded by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<SearchHistory?>(null) }
    var refreshCountdown by remember { mutableIntStateOf(10) } // 倒计时秒数

    // 滚动状态 - 用于固定头部 + FAB显示/隐藏
    val lazyListState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0 } }
    val fabScale by animateFloatAsState(if (fabVisible) 1f else 0f, label = "fabScale")
    val coroutineScope = rememberCoroutineScope()

    // 热门搜索项点击路由
    fun navigateToDetail(hotItem: HotSearchItem) {
        val category = uiState.selectedCategory
        when (hotItem.dimension) {
            "国家厂号产品" -> {
                hotItem.country?.let { c ->
                    hotItem.factoryNo?.let { fn ->
                        hotItem.keyword?.let { kw ->
                            // keyword 格式: 巴西SIF941牛腩，需要解析出产品名
                            val productName = kw.removePrefix(c).removePrefix(fn)
                                .removePrefix(" ").trim().ifEmpty { kw }
                            onCountryFactoryProductClick(c, fn, productName, category)
                        }
                    }
                }
            }
            "国家产品" -> {
                hotItem.country?.let { c ->
                    hotItem.keyword?.let { kw ->
                        val productName = kw.removePrefix(c).removePrefix(" ").trim()
                        onCountryProductClick(c, productName, category)
                    }
                }
            }
            "国家" -> {
                hotItem.country?.let { c -> onCountryClick(c, category) }
            }
            "产品" -> {
                hotItem.productId?.let { id ->
                    hotItem.keyword?.let { kw -> onProductClick(id, category, kw) }
                }
            }
            "品牌" -> {
                hotItem.keyword?.let { kw -> onBrandClick(kw, category) }
            }
            "商家" -> {
                hotItem.merchantId?.let { mid -> onMerchantClick(mid, category) }
            }
            "国家厂号" -> {
                hotItem.country?.let { c ->
                    hotItem.factoryNo?.let { fn -> onFactoryClick(c, fn, category) }
                }
            }
            "品牌产品" -> {
                hotItem.productId?.let { id ->
                    hotItem.keyword?.let { kw -> onProductClick(id, category, kw) }
                }
            }
        }
    }

    // 每10秒自动刷新首页统计数据
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            viewModel.refreshHomeStat()
            refreshCountdown = 10
        }
    }

    // 倒计时更新（每秒更新一次）
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            if (refreshCountdown > 0) {
                refreshCountdown--
            }
        }
    }

    // 删除确认弹窗
    showDeleteDialog?.let { history ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除搜索记录") },
            text = { Text("确定要删除「${history.searchWord}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    // 暂时禁用删除功能
                    showDeleteDialog = null
                }) {
                    Text("删除", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        // 固定头部：Logo + 搜索框 + 热门搜索 + 统计数据 + Tab栏
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp), spotColor = Color(0x14000000))
                .background(Color.White)
                .statusBarsPadding()
        ) {
            // Logo + 搜索框
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mooket_max_logo),
                    contentDescription = "MooketMax Logo",
                    modifier = Modifier.height(14.645.dp).width(90.dp)
                )
                Icon(imageVector = Icons.Outlined.Person, contentDescription = "数据卡片", tint = TextPrimary, modifier = Modifier.size(24.dp).clickable { onHomeCardsClick() })
            }

            // 搜索框
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(50.dp)
                        .shadow(elevation = 5.dp, shape = RoundedCornerShape(4.dp), spotColor = Color(0x26006A61))
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEFF5F3))
                        .border(1.dp, Primary, RoundedCornerShape(4.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight().padding(start = 16.dp).clickable { expanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = uiState.selectedCategory, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(painter = painterResource(id = R.drawable.ic_arrow_down), contentDescription = "下拉", modifier = Modifier.size(16.dp))
                    }
                    Box(modifier = Modifier.width(0.8f.dp).height(24.dp).background(Color(0xFFDEE4E1)).align(Alignment.CenterStart).offset(x = 61.dp))
                    Box(modifier = Modifier.size(42.dp).align(Alignment.CenterEnd).offset(x = (-3).dp, y = 1.dp), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "搜索", tint = Primary, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        text = "搜索国家、厂号、产品、商家，品牌",
                        fontSize = 14.sp, color = TextHint, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.Center).padding(start = 68.dp, end = 50.dp).clickable { onSearchClick(uiState.selectedCategory) }
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(80.dp).background(Color.White).border(1.dp, Color(0xFFDEE4E1), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = category, fontSize = 14.sp, color = if (category == uiState.selectedCategory) Primary else TextPrimary, fontWeight = if (category == uiState.selectedCategory) FontWeight.SemiBold else FontWeight.Normal)
                                    if (category == uiState.selectedCategory) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "选中", tint = Primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            onClick = { viewModel.selectCategory(category); expanded = false }
                        )
                    }
                }
            }

            // 热门搜索
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "热门搜索", fontSize = 11.sp, color = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(20.dp))
                } else if (uiState.hotSearchItems.isEmpty()) {
                    Text(text = "暂无热门搜索", fontSize = 11.sp, color = TextHint)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.hotSearchItems) { item ->
                            HotSearchChip(item = item, onNavigate = { navigateToDetail(item) })
                        }
                    }
                }
            }

            // 统计数据
            val statData = uiState.homeStatData
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3B5C59))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF3F706B), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "两日数据",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatItemDark(label = "报盘", value = statData?.totalOfferCount ?: "--")
                        Spacer(modifier = Modifier.width(12.dp))
                        StatItemDark(label = "求购", value = statData?.totalInquiryCount ?: "--")
                        Spacer(modifier = Modifier.width(12.dp))
                        StatItemDark(label = "商家", value = statData?.merchantCount ?: "--")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statData?.statTime ?: "--:--",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = refreshCountdown / 10f,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 1.5.dp,
                            color = Primary,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "$refreshCountdown",
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Tab栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TabItem(
                        selected = uiState.selectedTab == 0,
                        icon = Icons.Default.Star,
                        text = "自选数据",
                        onClick = {
                            viewModel.selectTab(0)
                            viewModel.refreshSelfSelectCards()
                        },
                        iconTint = if (uiState.selectedTab == 0) Primary else Color(0xFF9DA4A3),
                        textColor = if (uiState.selectedTab == 0) Primary else Color(0xFF9DA4A3),
                        customIcon = com.mooket.app.ui.theme.CandleChartIcon
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    TabItem(
                        selected = uiState.selectedTab == 1,
                        icon = Icons.Default.Schedule,
                        text = "历史搜索数据",
                        onClick = {
                            viewModel.selectTab(1)
                            viewModel.refreshRecentSearchCards()
                        },
                        iconTint = if (uiState.selectedTab == 1) Primary else Color(0xFF3C4947),
                        textColor = if (uiState.selectedTab == 1) Primary else Color(0xFF3C4947)
                    )
                }
                Text(
                    text = if (isEditMode) "完成" else "编辑",
                    fontSize = 14.sp,
                    color = if (isEditMode) Primary else Color(0xFF9DA4A3),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clickable { isEditMode = !isEditMode }
                )
            }
        }

        // 可滚动区域：Tab内容
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 280.dp),
            state = lazyListState
        ) {
            item {
                when (uiState.selectedTab) {
                    0 -> {
                        val selfSelectCards = uiState.selfSelectCards
                        if (selfSelectCards.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("暂无自选数据", fontSize = 12.sp, color = TextHint)
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalItemSpacing = 12.dp,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.heightIn(max = 600.dp)
                                ) {
                                    items(selfSelectCards) { card ->
                                        HomeCardItemView(
                                            card = card,
                                            onProductClick = onProductClick,
                                            onCountryClick = onCountryClick,
                                            onBrandClick = onBrandClick,
                                            onMerchantClick = onMerchantClick,
                                            onFactoryClick = onFactoryClick,
                                            onCountryProductClick = onCountryProductClick,
                                            onCountryFactoryProductClick = onCountryFactoryProductClick,
                                            isEditMode = isEditMode,
                                            onAddToSelfSelect = null,
                                            onDelete = { card.historyId?.let { viewModel.cancelSelfSelect(it) } }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (uiState.recentSearchCards.isEmpty()) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(text = "最近搜索", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.padding(vertical = 12.dp))
                                val examples = listOf(
                                    RecentSearchExample("产品", "牛前八件套", "产品"),
                                    RecentSearchExample("国家", "巴西", "国家"),
                                    RecentSearchExample("品牌", "JBS S.A.", "品牌"),
                                    RecentSearchExample("国家厂号", "巴西SIF504", "国家厂号"),
                                    RecentSearchExample("国家产品", "巴西牛前八件套", "国家产品"),
                                    RecentSearchExample("国家厂号产品", "巴西SIF1440牛前八件套", "国家厂号产品")
                                )
                                examples.forEach { example ->
                                    RecentSearchExampleCard(example = example, onCardClick = {
                                        navigateToExample(example, uiState.selectedCategory, onProductClick, onCountryClick, onFactoryClick, onCountryProductClick, onCountryFactoryProductClick)
                                    })
                                }
                            }
                        } else {
                            val historyCards = uiState.recentSearchCards

                            if (historyCards.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text("暂无历史搜索数据", fontSize = 12.sp, color = TextHint)
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    LazyVerticalStaggeredGrid(
                                        columns = StaggeredGridCells.Fixed(2),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalItemSpacing = 12.dp,
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        modifier = Modifier.heightIn(max = 600.dp)
                                    ) {
                                        items(historyCards) { card ->
                                            HomeCardItemView(
                                                card = card,
                                                onProductClick = onProductClick,
                                                onCountryClick = onCountryClick,
                                                onBrandClick = onBrandClick,
                                                onMerchantClick = onMerchantClick,
                                                onFactoryClick = onFactoryClick,
                                                onCountryProductClick = onCountryProductClick,
                                                onCountryFactoryProductClick = onCountryFactoryProductClick,
                                                isEditMode = isEditMode,
                                                onAddToSelfSelect = { card.historyId?.let { viewModel.moveToSelfSelect(it) } },
                                                onDelete = { card.historyId?.let { viewModel.deleteRecentSearch(it) } }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // 返回顶部 FAB
        AnimatedVisibility(
            visible = fabVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(scaleX = fabScale, scaleY = fabScale)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x40006A61))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Primary)
                    .clickable {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "返回顶部",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HotSearchChip(item: HotSearchItem, onNavigate: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(2.dp))
            .border(1.dp, Border, RoundedCornerShape(2.dp))
            .clickable { onNavigate() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = item.keyword, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TabItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    iconTint: Color = if (selected) Primary else TextHint,
    textColor: Color = if (selected) Primary else TextHint,
    customIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = customIcon ?: icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                color = textColor,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        // 选中指示条
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (selected) 16.dp else 0.dp)
                .background(
                    color = if (selected) Primary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HomeCardsContent(
    selectedCategory: String,
    onProductClick: (Int, String, String) -> Unit,
    onCountryClick: (String, String) -> Unit,
    onBrandClick: (String, String) -> Unit,
    onMerchantClick: (Long, String) -> Unit,
    onFactoryClick: (String, String, String) -> Unit,
    onCountryProductClick: (String, String, String) -> Unit,
    onCountryFactoryProductClick: (String, String, String, String) -> Unit
) {
    val viewModel: HomeCardsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // 当选中品类变化时，重新加载数据
    LaunchedEffect(selectedCategory) {
        viewModel.selectCategory(selectedCategory)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 卡片列表 - 使用最近搜索卡片
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.recentSearchCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Border, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text("暂无卡片数据", fontSize = 12.sp, color = TextHint)
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                items(uiState.recentSearchCards, key = { "${it.cardType}_${it.rank}_${it.historyId}" }) { card ->
                    HomeCardItemView(
                        card = card,
                        onProductClick = onProductClick,
                        onCountryClick = onCountryClick,
                        onBrandClick = onBrandClick,
                        onMerchantClick = onMerchantClick,
                        onFactoryClick = onFactoryClick,
                        onCountryProductClick = onCountryProductClick,
                        onCountryFactoryProductClick = onCountryFactoryProductClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeCardItemView(
    card: com.mooket.app.data.model.HomeCardItem,
    onProductClick: (Int, String, String) -> Unit,
    onCountryClick: (String, String) -> Unit,
    onBrandClick: (String, String) -> Unit,
    onMerchantClick: (Long, String) -> Unit,
    onFactoryClick: (String, String, String) -> Unit,
    onCountryProductClick: (String, String, String) -> Unit,
    onCountryFactoryProductClick: (String, String, String, String) -> Unit,
    isEditMode: Boolean = false,
    onAddToSelfSelect: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val category = "牛"

    Box {
        when (card.cardType) {
            "product" -> com.mooket.app.ui.screens.home.cards.ProductCard(
                card = card,
                onClick = {
                    card.productId?.let { id ->
                        onProductClick(id, category, card.productName ?: "")
                    }
                }
            )
            "country" -> com.mooket.app.ui.screens.home.cards.CountryCard(
                card = card,
                onClick = {
                    card.country?.let { country ->
                        onCountryClick(country, category)
                    }
                }
            )
            "brand" -> com.mooket.app.ui.screens.home.cards.BrandCard(
                card = card,
                onClick = {
                    card.brandName?.let { name ->
                        onBrandClick(name, category)
                    }
                }
            )
            "merchant" -> com.mooket.app.ui.screens.home.cards.MerchantCard(
                card = card,
                onClick = {
                    card.merchantId?.let { id ->
                        onMerchantClick(id, category)
                    }
                }
            )
            "factory" -> com.mooket.app.ui.screens.home.cards.FactoryCard(
                card = card,
                onClick = {
                    card.country?.let { country ->
                        card.factoryNo?.let { factoryNo ->
                            onFactoryClick(country, factoryNo, category)
                        }
                    }
                }
            )
            "brandProduct" -> com.mooket.app.ui.screens.home.cards.BrandProductCard(
                card = card,
                onClick = {
                    card.brandId?.let { brandId ->
                        card.productId?.let { productId ->
                            onProductClick(productId, category, card.productName ?: "")
                        }
                    }
                }
            )
            "factoryProduct" -> com.mooket.app.ui.screens.home.cards.FactoryProductCard(
                card = card,
                onClick = {
                    card.country?.let { country ->
                        card.factoryNo?.let { factoryNo ->
                            card.productName?.let { productName ->
                                onCountryFactoryProductClick(country, factoryNo, productName, category)
                            }
                        }
                    }
                }
            )
            "countryProduct" -> com.mooket.app.ui.screens.home.cards.CountryProductCard(
                card = card,
                onClick = {
                    card.country?.let { country ->
                        card.productName?.let { productName ->
                            onCountryProductClick(country, productName, category)
                        }
                    }
                }
            )
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                )
            }
        }

        // 编辑模式下的右上角操作按钮
        if (isEditMode) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 添加到自选按钮
                if (onAddToSelfSelect != null) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color(0xFFEFF5F3), RoundedCornerShape(4.dp))
                            .clickable { onAddToSelfSelect() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_to_self_select),
                            contentDescription = "添加到自选",
                            tint = Primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                // 删除按钮
                if (onDelete != null) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color(0xFFE7F5F3), RoundedCornerShape(4.dp))
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_history),
                            contentDescription = "删除",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (value.isNotEmpty()) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
        Text(text = label, fontSize = 10.sp, color = TextHint, maxLines = 1)
    }
}

@Composable
private fun StatItemDark(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 11.sp, color = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchHistoryCard(
    history: SearchHistory,
    isEditMode: Boolean,
    onCardClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onAddSelfSelect: (() -> Unit)? = null
) {
    // 根据搜索类型渲染不同卡片
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Border, RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = if (isEditMode && onDelete != null) onDelete else onCardClick,
                onLongClick = if (!isEditMode && onLongClick != null) onLongClick else null
            )
            .padding(12.dp)
    ) {
        when (history.searchType) {
            "产品" -> ProductCard(history)
            "国家" -> CountryCard(history)
            "品牌" -> BrandCard(history)
            "商家" -> MerchantCard(history)
            "国家厂号" -> FactoryCard(history)
            "国家产品" -> CountryProductCard(history)
            "国家厂号产品" -> FactoryProductCard(history)
            else -> DefaultCard(history)
        }

        // 右上角操作按钮
        if (!isEditMode) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (history.isSelfSelect == 0 && onAddSelfSelect != null) {
                    Box(modifier = Modifier.clickable { onAddSelfSelect() }.padding(4.dp)) {
                        Text(text = "添加自选", fontSize = 10.sp, color = Primary)
                    }
                } else if (history.isSelfSelect == 1) {
                    Text(text = "已自选", fontSize = 10.sp, color = Primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "进入",
                    tint = TextHint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductCard(history: SearchHistory) {
    // 产品卡片
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "产品", fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "今日报盘量: --", fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun CountryCard(history: SearchHistory) {
    // 国家卡片
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "国家", fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "热门厂号: --", fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun BrandCard(history: SearchHistory) {
    // 品牌卡片
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "品牌", fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "今日报盘: --", fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun MerchantCard(history: SearchHistory) {
    // 商家卡片
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "商家", fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "今日最新报盘: --", fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun FactoryCard(history: SearchHistory) {
    // 国家厂号卡片
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "国家厂号", fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "热门产品: --", fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun CountryProductCard(history: SearchHistory) {
    // 国家产品卡片
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "国家产品", fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "报价区间: --", fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun FactoryProductCard(history: SearchHistory) {
    // 国家厂号产品卡片
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "国家厂号产品", fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(text = "报价: --", fontSize = 10.sp, color = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "涨幅: --", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun DefaultCard(history: SearchHistory) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = history.searchType, fontSize = 10.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = history.searchWord, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentSearchExampleCard(example: RecentSearchExample, onCardClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Border, RoundedCornerShape(4.dp))
            .combinedClickable(onClick = onCardClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = example.type, fontSize = 10.sp, color = TextHint)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = example.keyword, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "进入", tint = TextHint, modifier = Modifier.size(20.dp))
        }
    }
}

private data class RecentSearchExample(val type: String, val keyword: String, val searchType: String)

/**
 * 将 SearchHistory 转换为 HomeCardItem 格式
 */
private fun searchHistoryToCardItem(history: SearchHistory): com.mooket.app.data.model.HomeCardItem? {
    val cardType = when (history.searchType) {
        "产品" -> "product"
        "国家" -> "country"
        "品牌" -> "brand"
        "商家" -> "merchant"
        "国家厂号", "国家+厂号" -> "factory"
        "国家产品", "国家+产品" -> "countryProduct"
        "国家厂号产品", "国家+厂号+产品" -> "factoryProduct"
        "品牌产品", "品牌+产品" -> "brandProduct"
        else -> return null
    }

    // 对于国家+产品类型，从 searchWord 中解析 country 和 productName
    val (country, productName, factoryNo) = when {
        history.searchType.contains("国家产品") && !history.searchType.contains("厂号") -> {
            // 国家+产品：可能是 "巴西 牛腩" 或 "巴西牛腩"
            val parts = history.searchWord.split(" ")
            if (parts.size >= 2) {
                Triple(parts[0], parts[1], null)
            } else {
                // 尝试从连续文字中提取（国家名通常在前面）
                val country = history.country ?: parts.getOrNull(0)?.takeWhile { !it.isDigit() }
                val product = history.productName ?: parts.getOrNull(0)?.dropWhile { !it.isDigit() }
                Triple(country, product, null)
            }
        }
        history.searchType.contains("国家厂号") && !history.searchType.contains("产品") -> {
            // 国家+厂号
            val parts = history.searchWord.split(" ")
            if (parts.size >= 2) {
                Triple(parts[0], null, parts.getOrNull(1))
            } else {
                Triple(history.country, null, history.factoryNo)
            }
        }
        history.searchType.contains("国家厂号产品") || history.searchType.contains("国家+厂号+产品") -> {
            // 国家+厂号+产品
            val parts = history.searchWord.split(" ")
            if (parts.size >= 3) {
                Triple(parts[0], parts[2], parts[1])
            } else {
                Triple(history.country, history.productName, history.factoryNo)
            }
        }
        else -> Triple(history.country, history.productName, history.factoryNo)
    }

    return com.mooket.app.data.model.HomeCardItem(
        cardType = cardType,
        rank = 0,
        productId = history.productId,
        productName = productName,
        country = country,
        factoryNo = factoryNo,
        brandId = history.brandId,
        merchantId = history.merchantId,
        todayOfferCount = null,
        merchantCount = null,
        factoryCount = null,
        priceMin = null,
        priceMax = null
    )
}

private fun navigateToDetail(
    history: SearchHistory,
    category: String,
    onProductClick: (Int, String, String) -> Unit,
    onCountryClick: (String, String) -> Unit,
    onMerchantClick: (Long, String) -> Unit,
    onFactoryClick: (String, String, String) -> Unit,
    onCountryProductClick: (String, String, String) -> Unit,
    onCountryFactoryProductClick: (String, String, String, String) -> Unit
) {
    when (history.searchType) {
        "产品" -> history.productId?.let { onProductClick(it, category, history.searchWord) }
        "国家" -> onCountryClick(history.searchWord, category)
        "商家" -> history.merchantId?.let { onMerchantClick(it, category) }
        "国家厂号" -> {
            val parts = parseCountryAndFactory(history.searchWord)
            if (parts != null) onFactoryClick(parts.first, parts.second, category)
        }
        "国家产品" -> {
            val parts = parseCountryAndProduct(history.searchWord)
            if (parts != null) onCountryProductClick(parts.first, parts.second, category)
        }
        "国家厂号产品" -> {
            val parts = parseCountryFactoryProduct(history.searchWord)
            if (parts != null && parts.size >= 3) onCountryFactoryProductClick(parts[0], parts[1], parts[2], category)
        }
    }
}

private fun navigateToExample(
    example: RecentSearchExample,
    category: String,
    onProductClick: (Int, String, String) -> Unit,
    onCountryClick: (String, String) -> Unit,
    onFactoryClick: (String, String, String) -> Unit,
    onCountryProductClick: (String, String, String) -> Unit,
    onCountryFactoryProductClick: (String, String, String, String) -> Unit
) {
    when (example.searchType) {
        "产品" -> onProductClick(0, category, example.keyword)
        "国家" -> onCountryClick(example.keyword, category)
        "国家厂号" -> {
            val parts = parseCountryAndFactory(example.keyword)
            if (parts != null) onFactoryClick(parts.first, parts.second, category)
        }
        "国家产品" -> {
            val parts = parseCountryAndProduct(example.keyword)
            if (parts != null) onCountryProductClick(parts.first, parts.second, category)
        }
        "国家厂号产品" -> {
            val parts = parseCountryFactoryProduct(example.keyword)
            if (parts != null && parts.size >= 3) onCountryFactoryProductClick(parts[0], parts[1], parts[2], category)
        }
    }
}

private fun parseCountryAndFactory(keyword: String): Pair<String, String>? {
    val countries = listOf("巴西", "阿根廷", "乌拉圭", "澳大利亚", "新西兰", "美国", "加拿大", "中国")
    for (country in countries) {
        if (keyword.startsWith(country)) {
            val remaining = keyword.substring(country.length)
            for (i in remaining.indices) {
                if (remaining[i].isLetter() && i + 1 < remaining.length && remaining[i + 1].isDigit()) {
                    return Pair(country, remaining.substring(i))
                }
            }
        }
    }
    return null
}

private fun parseCountryAndProduct(keyword: String): Pair<String, String>? {
    val countries = listOf("巴西", "阿根廷", "乌拉圭", "澳大利亚", "新西兰", "美国", "加拿大", "中国")
    for (country in countries) {
        if (keyword.startsWith(country)) {
            val product = keyword.substring(country.length).trim()
            if (product.isNotEmpty()) return Pair(country, product)
        }
    }
    return null
}

private fun parseCountryFactoryProduct(keyword: String): List<String>? {
    val countries = listOf("巴西", "阿根廷", "乌拉圭", "澳大利亚", "新西兰", "美国", "加拿大", "中国")
    for (country in countries) {
        if (keyword.startsWith(country)) {
            val remaining = keyword.substring(country.length)
            for (i in remaining.indices) {
                if (remaining[i].isLetter() && i + 1 < remaining.length && remaining[i + 1].isDigit()) {
                    val factoryPart = remaining.substring(i)
                    val product = remaining.substring(0, i).trim()
                    if (factoryPart.isNotEmpty() && product.isNotEmpty()) {
                        return listOf(country, factoryPart, product)
                    }
                }
            }
            val product = remaining.trim()
            if (product.isNotEmpty()) return listOf(country, "", product)
        }
    }
    return null
}
