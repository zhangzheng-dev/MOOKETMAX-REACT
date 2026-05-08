package com.mooket.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Figma 设计主题色
val Primary = Color(0xFF006A61)      // 深绿色 - 主色
val PrimaryDark = Color(0xFF004D47)  // 深绿色
val PrimaryLight = Color(0xFFE8F5F3)  // 浅绿色背景

// 背景色
val Background = Color(0xFFF4FBF8)   // 浅绿色背景
val Surface = Color(0xFFFFFFFF)      // 白色
val CardBackground = Color(0xFFF5FBF9) // 卡片浅绿

// 边框色
val Border = Color(0xFFDEE4E1)        // 边框灰色
val BorderActive = Color(0xFF006A61)  // 激活边框

// 文字颜色
val TextPrimary = Color(0xFF171D1C)    // 主文字黑色
val TextSecondary = Color(0xFF6C757D)  // 次要文字
val TextHint = Color(0xFF6C7A77)       // 提示文字 (rgba(108,122,119,0.5))

// 状态颜色
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFF44336)

// 辅助色
val Secondary = Color(0xFFFF9800)       // 橙色 - 辅助色

// 分割线
val Divider = Color(0xFFDEE4E1)

// 卡片渐变色
val CardGradientGreenStart = Color(0xFF006A61)
val CardGradientGreenEnd = Color(0xFF00A896)
val CardGradientBlueStart = Color(0xFF1976D2)
val CardGradientBlueEnd = Color(0xFF42A5F5)
val CardGradientPinkStart = Color(0xFFE91E63)
val CardGradientPinkEnd = Color(0xFFF48FB1)
val CardGradientYellowStart = Color(0xFFFF9800)
val CardGradientYellowEnd = Color(0xFFFFCA28)
val CardGradientPurpleStart = Color(0xFF7B1FA2)
val CardGradientPurpleEnd = Color(0xFFBA68C8)

// 蜡烛图图标 (自选数据Tab用)
val CandleChartIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CandleChart",
        defaultWidth = 16.dp,
        defaultHeight = 16.dp,
        viewportWidth = 16f,
        viewportHeight = 16f
    ).apply {
        // 左边蜡烛 - 上影线
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(4.333f, 14.667f)
            lineTo(4.333f, 10f)
        }
        // 左边蜡烛 - 下影线
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(4.333f, 3.333f)
            lineTo(4.333f, 1.333f)
        }
        // 左边蜡烛 - 矩形body (4个边)
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(4.999f, 3.333f)
            lineTo(6.333f, 4.667f)
            lineTo(6.333f, 8.667f)
            lineTo(4.999f, 10f)
            lineTo(3.667f, 8.667f)
            lineTo(3.667f, 4.667f)
            close()
        }
        // 右边蜡烛 - 上影线
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(11.667f, 14.667f)
            lineTo(11.667f, 12.667f)
        }
        // 右边蜡烛 - 下影线
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(11.667f, 6f)
            lineTo(11.667f, 1.333f)
        }
        // 右边蜡烛 - 矩形body (4个边)
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(11.001f, 6f)
            lineTo(12.333f, 7.333f)
            lineTo(12.333f, 11.333f)
            lineTo(11.001f, 12.667f)
            lineTo(9.667f, 11.333f)
            lineTo(9.667f, 7.333f)
            close()
        }
    }.build()

// 时钟图标 (历史搜索数据Tab用)
val ClockIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Clock",
        defaultWidth = 16.dp,
        defaultHeight = 16.dp,
        viewportWidth = 16f,
        viewportHeight = 16f
    ).apply {
        // 外圈圆环
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(14.667f, 8f)
            arcTo(5.333f, 5.333f, 0f, false, true, 8f, 1.333f)
            arcTo(5.333f, 5.333f, 0f, false, true, 1.333f, 8f)
            arcTo(5.333f, 5.333f, 0f, false, true, 8f, 14.667f)
            arcTo(5.333f, 5.333f, 0f, false, true, 14.667f, 8f)
        }
        // 时针 (指向右上)
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(10.473f, 10.12f)
            lineTo(8.407f, 8.887f)
        }
        // 分针 (指向左上)
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(8.407f, 8.887f)
            lineTo(7.753f, 5.007f)
        }
    }.build()
