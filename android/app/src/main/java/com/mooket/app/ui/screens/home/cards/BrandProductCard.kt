package com.mooket.app.ui.screens.home.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mooket.app.R
import com.mooket.app.data.model.HomeCardItem
import com.mooket.app.ui.components.MiniTrendChart
import com.mooket.app.ui.theme.*

/**
 * 品牌产品卡片
 * 显示内容：品牌+产品名、报价区间、涨跌、近30天趋势图、热门工厂x3、今日报盘工厂、今日报盘数
 */
@Composable
fun BrandProductCard(
    card: HomeCardItem,
    onClick: (() -> Unit)? = null,
    isExample: Boolean = false
) {
    // 涨跌颜色（正涨红，负跌绿）
    val priceChange = card.priceChange
    val isUp = priceChange != null && priceChange > 0
    val isDown = priceChange != null && priceChange < 0
    val changeBgColor = when {
        isUp -> Color(0xFFE67F5A).copy(alpha = 0.12f)  // 涨：红底
        isDown -> Color(0xFF47BB58).copy(alpha = 0.1f) // 跌：绿底
        else -> Color(0xFFE3EAE7).copy(alpha = 0.1f)   // 平：无色
    }
    val changeTextColor = when {
        isUp -> Color(0xFFA53321)   // 涨：红字
        isDown -> Color(0xFF0E8D41)  // 跌：绿字
        else -> TextSecondary       // 平：灰字
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp), spotColor = Color(0x05000000))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE3EAE7), RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 顶部：品牌名+产品名在同一行，超长才换行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_product),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${card.brandName ?: "--"} ${card.productName ?: ""}",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 价格区间
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (card.priceMin != null && card.priceMax != null) {
                    Text(
                        text = "¥${card.priceMin}-${card.priceMax}",
                        fontSize = 20.sp,
                        color = Primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "/kg",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 涨跌标签
            val priceChangeRate = card.priceChangeRate
            if (priceChange != null || priceChangeRate != null) {
                Row(
                    modifier = Modifier
                        .background(changeBgColor, RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 示例卡片：显示红色向上箭头
                    if (isExample && isUp) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_price_up),
                            contentDescription = "涨",
                            modifier = Modifier.size(10.dp, 10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    Text(
                        text = when {
                            isUp -> "+${String.format("%.2f", priceChange)}"
                            isDown -> String.format("%.2f", priceChange)
                            else -> "0"
                        },
                        fontSize = 12.sp,
                        color = changeTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            priceChangeRate != null -> "${if (priceChangeRate > 0) "+" else ""}${String.format("%.2f", priceChangeRate)}%"
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = changeTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 迷你趋势图
            card.trendPoints?.let { points ->
                if (points.isNotEmpty()) {
                    val trendData = points.mapNotNull {
                        it["avgPrice"] as? Double ?: (it["avgPrice"] as? Number)?.toDouble()
                    }
                    if (trendData.isNotEmpty()) {
                        MiniTrendChart(
                            data = trendData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // 热门工厂表头
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "热门工厂",
                    fontSize = 10.sp,
                    color = Color(0x803C4947)
                )
                Text(
                    text = "(元/千克)",
                    fontSize = 10.sp,
                    color = Color(0x803C4947)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 热门工厂列表
            card.hotFactories?.take(3)?.forEach { factory ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = factory["factoryNo"] as? String ?: "--",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    val priceMin = factory["priceMin"]
                    val priceMax = factory["priceMax"]
                    Text(
                        text = if (priceMin != null && priceMax != null) {
                            if (priceMin == priceMax) "¥$priceMin" else "¥$priceMin-$priceMax"
                        } else {
                            factory["offerCount"]?.let { "${it}" } ?: "--"
                        },
                        fontSize = 12.sp,
                        color = Primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            } ?: Text(
                text = "暂无数据",
                fontSize = 11.sp,
                color = TextHint
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 底部分隔线
            Divider(
                color = Color(0x0A000000),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 今日报盘工厂 和 今日报盘数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日报盘工厂",
                        fontSize = 8.sp,
                        color = Color(0x803C4947),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = card.factoryCount?.toString() ?: "--",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日报盘数",
                        fontSize = 8.sp,
                        color = Color(0x803C4947),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = card.todayOfferCount?.toString() ?: "--",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
