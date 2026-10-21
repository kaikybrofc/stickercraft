package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 1-to-many relation model between StickerPackEntity and its StickerEntity list.
 * Handled natively by Room using @Embedded and @Relation.
 */
data class StickerPackWithStickers(
    @Embedded
    val pack: StickerPackEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "packId"
    )
    val stickers: List<StickerEntity>
)
