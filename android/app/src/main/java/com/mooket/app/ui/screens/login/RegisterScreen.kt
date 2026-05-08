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
import androidx.compose.ui.layout.ContentScale
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6FFFB))
    ) {
        // 顶部装饰圆 - 绝对定位，不遮挡内容
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-80).dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ellipse426),
                contentDescription = null,
                modifier = Modifier.size(280.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
        ) {
            // 返回按钮 - 放在Column最顶部，不受verticalScroll影响
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 标题
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // 请填写 badge + 三角尾巴朝下（在badge左下方）
                    Box {
                        // 先放badge
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
                        // 三角尾巴在badge下方（朝下 = 不翻转）
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = (-4).dp, y = 20.dp)
                                .size(width = 10.dp, height = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_polygon22),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 昵称输入
                Column {
                    Text(
                        text = "您的昵称",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0x26006A61), RoundedCornerShape(2.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                // 行业身份
                Column {
                    Text(
                        text = "您的行业身份",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 第一行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IdentityChip(
                            text = "海外供应商",
                            selected = selectedTags.contains("海外供应商"),
                            onClick = { onTagToggle("海外供应商") }
                        )
                        IdentityChip(
                            text = "贸易商",
                            selected = selectedTags.contains("贸易商"),
                            onClick = { onTagToggle("贸易商") }
                        )
                        IdentityChip(
                            text = "服务商",
                            selected = selectedTags.contains("服务商"),
                            onClick = { onTagToggle("服务商") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 第二行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IdentityChip(
                            text = "加工厂/商场",
                            selected = selectedTags.contains("加工厂/商场"),
                            onClick = { onTagToggle("加工厂/商场") }
                        )
                        IdentityChip(
                            text = "其它",
                            selected = selectedTags.contains("其它"),
                            onClick = { onTagToggle("其它") }
                        )
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

                Spacer(modifier = Modifier.height(32.dp))

                // 确认按钮
                Button(
                    onClick = onConfirm,
                    enabled = nickname.length >= 2 && selectedTags.isNotEmpty() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
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

                Spacer(modifier = Modifier.height(32.dp))
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
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = if (selected) Primary else Color(0xFF3C4947)
        )
    }
}
