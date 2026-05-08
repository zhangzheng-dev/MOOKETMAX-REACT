package com.mooket.app.ui.screens.login

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mooket.app.R
import com.mooket.app.ui.theme.*

/**
 * 验证码确认页面
 */
@Composable
fun SmsVerifyScreen(
    phone: String,
    countdown: Int,
    isLoading: Boolean,
    error: String?,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 29.dp)
        ) {
            // 顶部导航栏
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

            Spacer(modifier = Modifier.height(44.dp))

            // 标题
            Text(
                text = "输入验证码",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 副标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "短信验证码已发送至",
                    fontSize = 14.sp,
                    color = Color(0xFF3C4947)
                )
                Text(
                    text = if (countdown > 0) "${countdown}s后重新发送" else "重新发送",
                    fontSize = 14.sp,
                    color = Primary,
                    modifier = Modifier.clickable(enabled = countdown == 0) { onResend() }
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 手机号
            Text(
                text = "+86 ${phone}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 验证码输入框（4格）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(0.dp))
                            .border(
                                width = if (index == code.length) 2.dp else 1.dp,
                                color = if (index == code.length) Primary else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(0.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = if (index < code.length) code[index].toString() else "",
                            onValueChange = { newChar ->
                                if (newChar.length == 1 && newChar.all { it.isDigit() }) {
                                    val newCode = code + newChar
                                    code = newCode
                                    onClearError()
                                    if (newCode.length == 4) {
                                        onVerify(newCode)
                                    }
                                } else if (newChar.isEmpty() && code.isNotEmpty()) {
                                    code = code.dropLast(1)
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(Primary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxSize(),
                            decorationBox = {}
                        )
                    }
                }
            }

            // 错误提示
            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0x0FF77234), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color(0xFFF77234)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 确认按钮
            Button(
                onClick = { onVerify(code) },
                enabled = code.length == 4 && !isLoading,
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

            Spacer(modifier = Modifier.weight(1f))

            // 第三方登录
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "第三方登录",
                    fontSize = 14.sp,
                    color = Color(0xFF3C4947)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_ctcc),
                    contentDescription = "天翼一键登录",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}
