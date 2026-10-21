package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sticker_packs")
data class StickerPackEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val publisher: String,
    val trayImageFileName: String,
    val trayImagePath: String,
    val isUserCreated: Boolean,
    val category: String,
    val downloadsCount: Int,
    val likesCount: Int,
    val tagsJson: String = "[]",
    val createdAtMillis: Long = System.currentTimeMillis()
)
