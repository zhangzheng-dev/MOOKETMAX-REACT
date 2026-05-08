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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import java.util.Locale
import com.mooket.app.R
import com.mooket.app.data.model.HomeCardItem
import com.mooket.app.ui.theme.*

/**
 * 产品卡片
 * 显示内容：近2日报盘量、商家数、工厂数
 */
@Composable
fun ProductCard(
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
            // 顶部：产品名称 + 排名badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 产品名称 + 图标
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.size(22.dp, 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_product),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp, 18.dp),
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = card.productName ?: "--",
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 近2日报盘
            Column {
                Text(
                    text = "近2日报盘",
                    fontSize = 10.sp,
                    color = Color(0x803C4947)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.todayOfferCount?.let {
                        if (it >= 1000) {
                            String.format(Locale.US, "%.1fk", it / 1000.0)
                        } else {
                            it.toString()
                        }
                    } ?: "--",
                    fontSize = 20.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 底部分隔线
            Divider(
                color = Color(0x0A000000),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 商家数和工厂数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 商家数
                Column {
                    Text(
                        text = "商家数",
                        fontSize = 10.sp,
                        color = Color(0x803C4947)
                    )
                    Text(
                        text = (card.merchantCount ?: "--").toString(),
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // 工厂数
                Column {
                    Text(
                        text = "工厂数",
                        fontSize = 10.sp,
                        color = Color(0x803C4947)
                    )
                    Text(
                        text = (card.factoryCount ?: "--").toString(),
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
