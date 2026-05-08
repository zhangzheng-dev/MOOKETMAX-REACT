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
import androidx.compose.ui.graphics.Brush
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
 * 商家卡片
 * 显示内容：商家名称、标签、最新2个报盘、今日报盘数
 */
@Composable
fun MerchantCard(
    card: HomeCardItem,
    onClick: () -> Unit
) {
    // 判断是否为知名商家
    val isTrusted = card.merchantTags?.contains("知名商家") == true

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp), spotColor = Color(0x05000000))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFEFD), Color(0xFFF9D088))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFFFFE7C5), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 顶部：商家名称（第一行）
            Text(
                text = card.merchantName ?: "商家-${card.merchantId}",
                fontSize = 16.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 知名商家标签 + 皇冠 + 商家简称（第二行）
            if (isTrusted) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(20.dp)
                ) {
                    // 知名商家标签
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF244C56), RoundedCornerShape(2.2.dp))
                            .padding(horizontal = 5.dp, vertical = 2.2.dp)
                    ) {
                        Text(
                            text = "知名商家",
                            fontSize = 11.sp,
                            color = Color(0xFFF2FFFD),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // 皇冠图标 - 覆盖在标签右边
                    Image(
                        painter = painterResource(id = R.drawable.ic_merchant_crown),
                        contentDescription = "知名商家",
                        modifier = Modifier
                            .offset(x = (-12).dp)
                            .size(width = 30.dp, height = 20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 分隔线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 最新报盘标题
            Text(
                text = "最新报盘",
                fontSize = 10.sp,
                color = Color(0x803C4947)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 最新报盘列表
            card.latestOffers?.take(2)?.forEach { offer ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // 产品名称 + 国家 + 厂号
                    Text(
                        text = "${offer["productName"] ?: "--"} ${offer["country"] ?: ""} ${offer["factoryNo"] ?: ""}",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 价格 + 重量
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 价格
                        val price = offer["price"]
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (price != null) "¥$price" else "--",
                                fontSize = 12.sp,
                                color = Primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "/kg",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // 重量（去掉尾部的 .000/.00/.0，如 "2.000 柜" -> "2 柜"，"360.000 件" -> "360 件"）
                        val weightAny = offer["weight"]
                        val weight = when {
                            weightAny == null || weightAny.toString().isEmpty() || weightAny.toString() == "--" -> "--"
                            else -> {
                                val w = weightAny.toString()
                                // 格式：数字 + 空格 + 单位，如 "2.000 柜"
                                val parts = w.split(" ")
                                if (parts.size == 2) {
                                    val numPart = parts[0]
                                    val unitPart = parts[1]
                                    val formattedNum = if (numPart.contains(".")) {
                                        numPart.trimEnd('0').trimEnd('.')
                                    } else {
                                        numPart
                                    }
                                    "$formattedNum $unitPart"
                                } else {
                                    w
                                }
                            }
                        }
                        Text(
                            text = weight,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } ?: Text(
                text = "暂无报盘",
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

            // 今日报盘数
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
