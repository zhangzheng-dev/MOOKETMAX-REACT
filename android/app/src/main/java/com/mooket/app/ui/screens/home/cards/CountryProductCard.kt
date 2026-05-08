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
import com.mooket.app.ui.theme.*

/**
 * 国家产品卡片
 * 显示内容：国旗、产品名、热门工厂x3、报盘工厂数、报盘数
 */
@Composable
fun CountryProductCard(
    card: HomeCardItem,
    onClick: () -> Unit
) {
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
            // 顶部：产品图标(左) + 国家 + 产品名(右，可换行)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 产品图标在左边
                Image(
                    painter = painterResource(id = R.drawable.ic_product),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(22.dp)
                )

                // 文字在icon右边，可换行到第二行，第二行从最左边开始
                Text(
                    text = "${card.countryAlias ?: card.country ?: "--"} ${card.productName ?: ""}",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    modifier = Modifier
                        .padding(start = 28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
            card.topFactories?.take(3)?.forEach { factory ->
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
                    val minPrice = factory["priceMin"]
                    val maxPrice = factory["priceMax"]
                    Text(
                        text = if (minPrice != null && maxPrice != null) {
                            if (minPrice == maxPrice) "¥$minPrice" else "¥$minPrice-$maxPrice"
                        } else "--",
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日报盘工厂",
                        fontSize = 9.sp,
                        color = Color(0x803C4947),
                        maxLines = 1
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
            }
        }
    }
}
