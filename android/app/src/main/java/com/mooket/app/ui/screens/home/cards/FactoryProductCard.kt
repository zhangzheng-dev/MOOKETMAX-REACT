package com.mooket.app.ui.screens.home.cards

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mooket.app.data.model.HomeCardItem
import com.mooket.app.ui.components.MiniTrendChart
import com.mooket.app.ui.theme.*
import com.mooket.app.ui.util.CountryFlagUtil

/**
 * 国家厂号产品卡片
 * 显示内容：国旗、厂号、产品名、报价区间、涨跌、趋势图、热门商家x3、今日报盘数、今日求购数
 */
@Composable
fun FactoryProductCard(
    card: HomeCardItem,
    onClick: () -> Unit
) {
    // 涨跌颜色
    val changeBgColor = Color(0xFFE67F5A).copy(alpha = 0.1f)
    val changeTextColor = Color(0xFFA53321)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp), spotColor = Color(0x05000000))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE3EAE7), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 顶部：国旗 + 国家 + 厂号
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 国旗
                val flagEmoji = card.country?.let { CountryFlagUtil.getFlagEmoji(it) } ?: ""
                if (flagEmoji.isNotEmpty()) {
                    Text(
                        text = flagEmoji,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = "${card.countryAlias ?: card.country ?: "--"} ${card.factoryNo ?: ""}",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // 产品名单独一行
            Text(
                text = card.productName ?: "",
                fontSize = 16.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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
            val priceChange = card.priceChange
            val priceChangeRate = card.priceChangeRate
            if (priceChange != null || priceChangeRate != null) {
                Row(
                    modifier = Modifier
                        .background(changeBgColor, RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 涨跌图标/符号
                    val isUp = priceChange != null && priceChange > 0
                    val isDown = priceChange != null && priceChange < 0

                    Text(
                        text = when {
                            isUp -> "+${String.format("%.1f", priceChange)}"
                            isDown -> String.format("%.1f", priceChange)
                            else -> "0"
                        },
                        fontSize = 12.sp,
                        color = changeTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            priceChangeRate != null -> "${if (priceChangeRate > 0) "+" else ""}${String.format("%.1f", priceChangeRate)}%"
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

            // 热门商家表头
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "热门商家",
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

            // 热门商家列表
            card.hotMerchants?.take(3)?.forEach { merchant ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = merchant["merchantName"] as? String ?: "商家-${merchant["merchantId"]}",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    val priceMin = merchant["priceMin"]
                    val priceMax = merchant["priceMax"]
                    Text(
                        text = if (priceMin != null && priceMax != null) {
                            if (priceMin == priceMax) "¥$priceMin" else "¥$priceMin-$priceMax"
                        } else {
                            "--"
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

            // 今日报盘数 和 今日求购数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日报盘数",
                        fontSize = 10.sp,
                        color = Color(0x803C4947)
                    )
                    Text(
                        text = card.todayOfferCount?.toString() ?: "--",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日求购数",
                        fontSize = 10.sp,
                        color = Color(0x803C4947)
                    )
                    Text(
                        text = card.inquiryCount?.toString() ?: "--",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
