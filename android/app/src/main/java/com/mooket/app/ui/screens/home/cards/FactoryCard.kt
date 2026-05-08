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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mooket.app.data.model.HomeCardItem
import com.mooket.app.ui.theme.*
import com.mooket.app.ui.util.CountryFlagUtil

/**
 * 国家厂号卡片
 * 显示内容：国旗、厂号、热门产品x3、今日报盘数
 */
@Composable
fun FactoryCard(
    card: HomeCardItem,
    onClick: (() -> Unit)? = null
) {
    // 排名文字颜色
    val rankBrown1 = Color(0xFF906134)
    val rankGray = Color(0xFF4B5462)
    val rankBrown2 = Color(0xFF80521E)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp), spotColor = Color(0x05000000))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFE2FFEE))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFFE3EAE7), RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 顶部：国旗 + 国家名 + 厂号
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 热门产品标题
            Text(
                text = "热门产品",
                fontSize = 10.sp,
                color = Color(0x803C4947)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 热门产品列表
            card.hotProducts?.take(3)?.forEachIndexed { index, product ->
                val rank = (product["rank"] as? Number)?.toInt() ?: (index + 1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(2.dp))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 排名数字（带棕色/灰色背景）
                    Box(
                        modifier = Modifier.size(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 背景色块
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    color = when (rank) {
                                        1 -> rankBrown1.copy(alpha = 0.15f)
                                        2 -> rankGray.copy(alpha = 0.15f)
                                        else -> rankBrown2.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        // 排名数字
                        Text(
                            text = rank.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (rank) {
                                1 -> rankBrown1
                                2 -> rankGray
                                else -> rankBrown2
                            }
                        )
                    }

                    // 产品名称
                    Text(
                        text = product["productName"] as? String ?: "--",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 报盘数
                    Text(
                        text = formatOfferCount(product["offerCount"]),
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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

            // 今日报盘数总计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日报盘数",
                    fontSize = 10.sp,
                    color = Color(0x803C4947)
                )
                Text(
                    text = card.todayOfferCount?.let {
                        when {
                            it >= 1000 -> "${it / 1000}.${(it % 1000) / 100}k"
                            else -> it.toString()
                        }
                    } ?: "--",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun formatOfferCount(count: Any?): String {
    return when (count) {
        is Number -> {
            val it = count.toInt()
            when {
                it >= 1000 -> "${it / 1000}.${(it % 1000) / 100}k"
                else -> it.toString()
            }
        }
        else -> "--"
    }
}
