package com.pawar47.issuerank.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Hand-drawn SF-Symbol-style glyphs, so the app needs no icon library. */
@Composable
fun TabGlyph(tab: AppTab, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(26.dp)) {
        val s = size.minDimension
        val sw = s * 0.09f
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (tab) {
            AppTab.Rank -> {
                val bw = s * 0.24f
                val r = CornerRadius(s * 0.06f)
                drawRoundRect(color, Offset(s * 0.08f, s * 0.45f), Size(bw, s * 0.45f), r)
                drawRoundRect(color, Offset(s * 0.38f, s * 0.12f), Size(bw, s * 0.78f), r)
                drawRoundRect(color, Offset(s * 0.68f, s * 0.6f), Size(bw, s * 0.3f), r)
            }
            AppTab.Name -> {
                drawCircle(color, radius = s * 0.2f, center = Offset(s * 0.5f, s * 0.3f))
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(s * 0.14f, s * 0.56f),
                    size = Size(s * 0.72f, s * 0.64f)
                )
            }
            AppTab.Issues -> {
                drawRoundRect(
                    color,
                    topLeft = Offset(s * 0.14f, s * 0.12f),
                    size = Size(s * 0.72f, s * 0.76f),
                    cornerRadius = CornerRadius(s * 0.16f),
                    style = stroke
                )
                for (y in listOf(0.35f, 0.5f, 0.65f)) {
                    drawLine(
                        color,
                        Offset(s * 0.3f, s * y),
                        Offset(s * 0.7f, s * y),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                }
            }
            AppTab.Status -> {
                drawCircle(color, radius = s * 0.38f, center = Offset(s * 0.5f, s * 0.5f), style = stroke)
                drawLine(color, Offset(s * 0.5f, s * 0.5f), Offset(s * 0.5f, s * 0.27f), strokeWidth = sw, cap = StrokeCap.Round)
                drawLine(color, Offset(s * 0.5f, s * 0.5f), Offset(s * 0.66f, s * 0.6f), strokeWidth = sw, cap = StrokeCap.Round)
            }
            AppTab.Resolved -> {
                drawCircle(color, radius = s * 0.38f, center = Offset(s * 0.5f, s * 0.5f), style = stroke)
                val check = Path().apply {
                    moveTo(s * 0.32f, s * 0.52f)
                    lineTo(s * 0.45f, s * 0.65f)
                    lineTo(s * 0.7f, s * 0.37f)
                }
                drawPath(check, color, style = stroke)
            }
        }
    }
}

@Composable
fun PlusGlyph(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.13f
        drawLine(color, Offset(s * 0.5f, s * 0.12f), Offset(s * 0.5f, s * 0.88f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.12f, s * 0.5f), Offset(s * 0.88f, s * 0.5f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

/** A chevron that points down at rotation 0; -90 points right (disclosure), 180 points up. */
@Composable
fun ChevronGlyph(
    color: Color,
    modifier: Modifier = Modifier,
    rotation: Float = -90f,
    size: Dp = 14.dp
) {
    Canvas(modifier.size(size).rotate(rotation)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.2f, s * 0.35f)
            lineTo(s * 0.5f, s * 0.65f)
            lineTo(s * 0.8f, s * 0.35f)
        }
        drawPath(path, color, style = Stroke(width = s * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun SearchGlyph(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.12f
        drawCircle(color, radius = s * 0.3f, center = Offset(s * 0.42f, s * 0.42f), style = Stroke(width = sw))
        drawLine(color, Offset(s * 0.64f, s * 0.64f), Offset(s * 0.9f, s * 0.9f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}
