package com.example.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun CheckerboardBackground(
    modifier: Modifier = Modifier,
    squareSizeDp: Float = 16f,
    lightColor: Color = Color(0xFFF1F5F9),
    darkColor: Color = Color(0xFFCBD5E1),
    content: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = squareSizeDp * density
            val cols = (size.width / step).toInt() + 1
            val rows = (size.height / step).toInt() + 1

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val isEven = (r + c) % 2 == 0
                    drawRect(
                        color = if (isEven) lightColor else darkColor,
                        topLeft = Offset(c * step, r * step),
                        size = Size(step, step)
                    )
                }
            }
        }
        content()
    }
}
