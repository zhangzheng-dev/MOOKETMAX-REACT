package com.mooket.app.ui.screens.login

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mooket.app.R
import com.mooket.app.ui.theme.*

/**
 * 注册信息页面（昵称 + 身份选择）
 */
@Composable
fun RegisterScreen(
    nickname: String,
    selectedTags: Set<String>,
    isLoading: Boolean,
    error: String?,
    onNicknameChange: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    val identityOptions = listOf(
        "海外供应商" to "海外服务商 - 提供海外物流、供应链等服务",
        "贸易商" to "从事肉类进出口贸易的企业",
        "服务商" to "提供各类服务的中间商",
        "加工厂/商场" to "肉类加工企业或商超采购商",
        "其它" to "不属于以上类型的其他用户"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6FFFB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
        ) {
            // 顶部装饰圆
            Box(
                modifier = Modifier
                    .offset(x = 201.dp, y = (-111).dp)
                    .size(332.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ellipse426),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 返回按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "返回",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.height(58.dp))

            // 标题
            Column {
                Column {
                    Text(
                        text = "为了更好地向您提供",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Primary)) { append("数据") }
                            append("与")
                            withStyle(SpanStyle(color = Primary)) { append("服务") }
                        },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 请填写 badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF00AEA0), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 0.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "请填写",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    // 小三角箭头
                    Box(
                        modifier = Modifier
                            .offset(x = (-4).dp)
                            .size(10.dp, 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_polygon22),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 昵称输入
            Column {
                Text(
                    text = "您的昵称",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(2.dp))
                        .border(1.dp, Color(0x26006A61), RoundedCornerShape(2.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = nickname,
                        onValueChange = {
                            if (it.length <= 20) {
                                onNicknameChange(it)
                                onClearError()
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 15.sp,
                            color = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box {
                                if (nickname.isEmpty()) {
                                    Text(
                                        text = "输入您的昵称",
                                        fontSize = 15.sp,
                                        color = TextHint
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 行业身份
            Column {
                Text(
                    text = "您的行业身份",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 第一行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IdentityChip(
                        text = "海外供应商",
                        selected = selectedTags.contains("海外供应商"),
                        onClick = { onTagToggle("海外供应商") },
                        modifier = Modifier.weight(1f)
                    )
                    IdentityChip(
                        text = "贸易商",
                        selected = selectedTags.contains("贸易商"),
                        onClick = { onTagToggle("贸易商") },
                        modifier = Modifier.weight(1f)
                    )
                    IdentityChip(
                        text = "服务商",
                        selected = selectedTags.contains("服务商"),
                        onClick = { onTagToggle("服务商") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 第二行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IdentityChip(
                        text = "加工厂/商场",
                        selected = selectedTags.contains("加工厂/商场"),
                        onClick = { onTagToggle("加工厂/商场") },
                        modifier = Modifier.weight(1f)
                    )
                    IdentityChip(
                        text = "其它",
                        selected = selectedTags.contains("其它"),
                        onClick = { onTagToggle("其它") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // 错误提示
            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Error
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 确认按钮
            Button(
                onClick = onConfirm,
                enabled = nickname.length >= 2 && selectedTags.isNotEmpty() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Color(0x66006A61)
                ),
                shape = RoundedCornerShape(4.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "确认",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun IdentityChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (selected) Color(0xFF006A61).copy(alpha = 0.05f)
                else Color.White
            )
            .border(
                width = 1.dp,
                color = if (selected) Primary else Color(0x26006A61),
                shape = RoundedCornerShape(2.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = if (selected) Primary else Color(0xFF3C4947)
        )
    }
}
