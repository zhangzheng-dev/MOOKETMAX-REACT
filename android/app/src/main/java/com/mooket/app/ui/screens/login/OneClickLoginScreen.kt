package com.mooket.app.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * 一键登录页面（天翼）
 */
@Composable
fun OneClickLoginScreen(
    phone: String,
    isLoading: Boolean,
    onOneClickLogin: () -> Unit,
    onOtherLogin: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 29.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(157.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_mooket_max_logo),
                contentDescription = "MooketMax Logo",
                modifier = Modifier
                    .height(29.29.dp)
                    .width(180.dp)
            )

            Spacer(modifier = Modifier.height(157.dp))

            // 手机号显示
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = phone,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "天翼账号提供认证服务",
                    fontSize = 12.sp,
                    color = Color(0xFF3C4947)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 一键登录按钮
            Button(
                onClick = onOneClickLogin,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
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
                        text = "本机号码一键登录",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 其他手机号登录
            Text(
                text = "其他手机号登录",
                fontSize = 16.sp,
                color = Color(0xFF3C4947),
                modifier = Modifier.clickable { onOtherLogin() }
            )

            Spacer(modifier = Modifier.height(61.dp))

            // 协议
            Row(
                modifier = Modifier.clickable { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "勾选",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append("阅读并同意")
                        pushStyle(SpanStyle(color = Primary))
                        append("天翼账号服务与隐私协议")
                        pushStyle(SpanStyle(color = TextPrimary))
                        append("、")
                        pushStyle(SpanStyle(color = Primary))
                        append("用户协议")
                        pushStyle(SpanStyle(color = TextPrimary))
                        append("和")
                        pushStyle(SpanStyle(color = Primary))
                        append("隐私政策")
                    },
                    fontSize = 11.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(52.dp))

            // 第三方登录
            Column(
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
                    contentDescription = "天翼",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
