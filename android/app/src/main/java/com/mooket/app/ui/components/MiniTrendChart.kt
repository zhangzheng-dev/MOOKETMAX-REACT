package com.mooket.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mooket.app.ui.theme.Primary
import com.mooket.app.ui.theme.PrimaryLight

/**
 * 迷你趋势图组件
 * 用于显示近30天价格趋势
 */
@Composable
fun MiniTrendChart(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val lineColor = Primary
    val fillColor = PrimaryLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(fillColor.copy(alpha = 0.3f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            if (data.size < 2) return@Canvas

            val minValue = data.minOrNull() ?: 0.0
            val maxValue = data.maxOrNull() ?: 0.0
            val range = if (maxValue - minValue > 0) maxValue - minValue else 1.0

            val stepX = size.width / (data.size - 1)
            val paddingY = size.height * 0.1f
            val chartHeight = size.height - paddingY * 2

            // 绘制填充区域
            val fillPath = Path().apply {
                moveTo(0f, size.height - paddingY)
                data.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - paddingY - ((value - minValue) / range * chartHeight).toFloat()
                    lineTo(x, y)
                }
                lineTo(size.width, size.height - paddingY)
                close()
            }
            drawPath(
                path = fillPath,
                color = fillColor.copy(alpha = 0.5f)
            )

            // 绘制线条
            val linePath = Path().apply {
                data.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - paddingY - ((value - minValue) / range * chartHeight).toFloat()
                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 2f)
            )

            // 绘制最后一个点
            val lastX = (data.size - 1) * stepX
            val lastY = size.height - paddingY - ((data.last() - minValue) / range * chartHeight).toFloat()
            drawCircle(
                color = lineColor,
                radius = 3f,
                center = Offset(lastX, lastY)
            )
        }
    }
}
