package com.example.domain.model

data class Sticker(
    val id: String,
    val packId: String,
    val fileName: String,          // e.g. "sticker_01.webp"
    val filePath: String,          // Internal storage path or drawable URI
    val emojis: List<String> = emptyList(), // Up to 3 emojis per WhatsApp rules
    val fileSizeBytes: Long = 0L,
    val isAnimated: Boolean = false
)
