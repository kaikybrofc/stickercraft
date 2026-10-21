package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stickers",
    foreignKeys = [
        ForeignKey(
            entity = StickerPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["packId"])]
)
data class StickerEntity(
    @PrimaryKey
    val id: String,
    val packId: String,
    val fileName: String,
    val filePath: String,
    val emojisJson: String = "[]",
    val fileSizeBytes: Long = 0L,
    val isAnimated: Boolean = false,
    val orderIndex: Int = 0
)
