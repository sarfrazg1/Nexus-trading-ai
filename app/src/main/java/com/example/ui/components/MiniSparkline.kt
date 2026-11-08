package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen

@Composable
fun MiniSparkline(
    points: List<Float>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    lineColor: Color = if (isPositive) BullishGreen else BearishRed,
    strokeWidth: Float = 3f
) {
    if (points.size < 2) return

    val minVal = points.minOrNull() ?: 0f
    val maxVal = points.maxOrNull() ?: 1f
    val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val stepX = width / (points.size - 1)
        val strokePath = Path()
        val fillPath = Path()

        val firstY = height - ((points[0] - minVal) / range) * (height - 8f) - 4f
        strokePath.moveTo(0f, firstY)
        fillPath.moveTo(0f, height)
        fillPath.lineTo(0f, firstY)

        for (i in 1 until points.size) {
            val x = i * stepX
            val y = height - ((points[i] - minVal) / range) * (height - 8f) - 4f
            val prevX = (i - 1) * stepX
            val prevY = height - ((points[i - 1] - minVal) / range) * (height - 8f) - 4f

            // Smooth cubic bezier
            val controlX1 = prevX + (x - prevX) / 2
            val controlY1 = prevY
            val controlX2 = prevX + (x - prevX) / 2
            val controlY2 = y

            strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.28f),
                    Color.Transparent
                )
            )
        )

        // Draw stroke line
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
