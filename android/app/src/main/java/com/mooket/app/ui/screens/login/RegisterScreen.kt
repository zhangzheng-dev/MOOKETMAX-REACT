package com.mooket.app.ui.screens.login

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.unit.plus
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6FFFB))
            .statusBarsPadding()  // 不占用系统状态栏区域
            .padding(horizontal = 30.dp)
    ) {
        // 返回按钮 - y=0 in Figma (below status bar), h=48
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
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

        // Figma: content starts at y=98, back button ends at y=48 → gap=50dp
        Spacer(modifier = Modifier.height(50.dp - 48.dp))

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

            // Figma: badge group y=80 from title top, badge h=25, triangle at badge内y=29
            // badge text is 17px at badge内y=4 → text bottom ≈ badge内y=21
            // triangle tip at badge内y=29 → triangle is 9dp below badge text bottom
            Spacer(modifier = Modifier.height(9.dp))

            // badge + triangle tail (badge has bottom-left sharp corner for tail)
            Box {
                // badge: topStart/topEnd rounded, bottom-left/bottom-right NOT rounded
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                        .background(Color(0xFF00AEA0))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "请填写",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                // triangle at badge内 y=29 → badge h=25 → 4dp below badge bottom
                // badge bottom = 25dp, triangle height = 4dp, offset from badge top = 29dp
                // But we want it below badge: offset y = badge height + (29-25) = 25 + 4 = 29dp from badge top
                // Actually: triangle tip is at badge内y=29, badge bottom is at y=25 → 4dp below badge
                // In our layout: badge is at y=0, triangle offset y = 25dp (badge h) + (29-25)=29dp from badge top
                // = just 4dp below badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = 25.dp)
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

        Spacer(modifier = Modifier.height(35.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        // 行业身份
        Column {
            Text(
                text = "您的行业身份",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Figma row: 海外供应商 w=115, 贸易商 w=85, 服务商 w=85, gap=16, total=315
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IdentityChip(
                    text = "海外供应商",
                    selected = selectedTags.contains("海外供应商"),
                    onClick = { onTagToggle("海外供应商") },
                    modifier = Modifier.widthIn(min = 80.dp)
                )
                IdentityChip(
                    text = "贸易商",
                    selected = selectedTags.contains("贸易商"),
                    onClick = { onTagToggle("贸易商") },
                    modifier = Modifier.widthIn(min = 65.dp)
                )
                IdentityChip(
                    text = "服务商",
                    selected = selectedTags.contains("服务商"),
                    onClick = { onTagToggle("服务商") },
                    modifier = Modifier.widthIn(min = 65.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Figma: 加工厂/商场 w=123, 其它 w=70, gap=16
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IdentityChip(
                    text = "加工厂/商场",
                    selected = selectedTags.contains("加工厂/商场"),
                    onClick = { onTagToggle("加工厂/商场") },
                    modifier = Modifier.widthIn(min = 90.dp)
                )
                IdentityChip(
                    text = "其它",
                    selected = selectedTags.contains("其它"),
                    onClick = { onTagToggle("其它") },
                    modifier = Modifier.widthIn(min = 55.dp)
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

        Spacer(modifier = Modifier.weight(1f))

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
