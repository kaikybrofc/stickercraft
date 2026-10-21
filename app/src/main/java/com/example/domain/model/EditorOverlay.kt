package com.example.domain.model

enum class CutoutMode(val label: String, val icon: String) {
    ORIGINAL("Original", "🖼️"),
    AUTO_CUT("Auto Cut (IA)", "✨"),
    CIRCLE_CROP("Círculo", "⭕"),
    SQUARE_CROP("Quadrado", "⬛"),
    LASSO_FREE("Livre", "✂️")
}

data class TextOverlay(
    val id: String,
    val text: String,
    val colorHex: Long = 0xFFFFFFFF,
    val strokeColorHex: Long = 0xFF000000,
    val fontSizeSp: Float = 28f,
    val isBold: Boolean = true,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f
)

data class AccessoryOverlay(
    val id: String,
    val emojiOrRes: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
)
