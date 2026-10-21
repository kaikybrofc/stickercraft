package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StickerDao {

    @Query("SELECT * FROM sticker_packs ORDER BY createdAtMillis DESC")
    fun getAllPacks(): Flow<List<StickerPackEntity>>

    @Query("SELECT * FROM sticker_packs WHERE isUserCreated = 1 ORDER BY createdAtMillis DESC")
    fun getUserPacks(): Flow<List<StickerPackEntity>>

    @Query("SELECT * FROM sticker_packs WHERE isUserCreated = 0 ORDER BY downloadsCount DESC")
    fun getCommunityPacks(): Flow<List<StickerPackEntity>>

    @Query("SELECT * FROM sticker_packs WHERE id = :packId LIMIT 1")
    suspend fun getPackById(packId: String): StickerPackEntity?

    @Query("SELECT * FROM stickers WHERE packId = :packId ORDER BY orderIndex ASC")
    fun getStickersForPack(packId: String): Flow<List<StickerEntity>>

    @Query("SELECT * FROM stickers WHERE packId = :packId ORDER BY orderIndex ASC")
    suspend fun getStickersForPackSync(packId: String): List<StickerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: StickerPackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickers(stickers: List<StickerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSticker(sticker: StickerEntity)

    @Query("DELETE FROM sticker_packs WHERE id = :packId")
    suspend fun deletePack(packId: String)

    @Query("DELETE FROM stickers WHERE id = :stickerId")
    suspend fun deleteSticker(stickerId: String)

    @Query("SELECT COUNT(*) FROM stickers WHERE packId = :packId")
    suspend fun getStickerCount(packId: String): Int
}
