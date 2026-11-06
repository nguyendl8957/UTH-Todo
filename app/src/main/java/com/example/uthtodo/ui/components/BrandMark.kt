package com.example.uthtodo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

/** Compose version of the app mark so its primary strokes follow the selected accent. */
@Composable
fun BrandMark(
    primary: Color,
    modifier: Modifier = Modifier,
    onPrimaryTile: Boolean = false
) {
    Canvas(modifier) {
        val unit = size.width / 100f
        scale(unit, pivot = Offset.Zero) {
            val circleColor = if (onPrimaryTile) Color.White else primary
            val lineColor = if (onPrimaryTile) Color.White else primary
            val checkColor = if (onPrimaryTile) primary else Color.White

            drawCircle(circleColor, radius = 9f, center = Offset(31f, 37f))
            drawLine(
                checkColor, Offset(26.6f, 37.3f), Offset(29.7f, 40.4f),
                strokeWidth = 3.2f, cap = StrokeCap.Round
            )
            drawLine(
                checkColor, Offset(29.7f, 40.4f), Offset(35.6f, 34.2f),
                strokeWidth = 3.2f, cap = StrokeCap.Round
            )
            drawLine(
                lineColor, Offset(48f, 37f), Offset(73f, 37f),
                strokeWidth = 8f, cap = StrokeCap.Round
            )
            drawCircle(
                lineColor.copy(alpha = if (onPrimaryTile) 0.7f else 0.55f),
                radius = 7.2f, center = Offset(31f, 63f),
                style = Stroke(width = 3.6f)
            )
            drawLine(
                lineColor.copy(alpha = if (onPrimaryTile) 0.55f else 0.45f),
                Offset(48f, 63f), Offset(65f, 63f),
                strokeWidth = 8f, cap = StrokeCap.Round
            )
        }
    }
}
