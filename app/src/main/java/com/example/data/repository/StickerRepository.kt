package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.local.StickerDao
import com.example.data.local.StickerEntity
import com.example.data.local.StickerPackEntity
import com.example.data.storage.StickerFileManager
import com.example.domain.model.Sticker
import com.example.domain.model.StickerPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class StickerRepository(
    private val context: Context,
    private val stickerDao: StickerDao,
    private val fileManager: StickerFileManager
) {

    val userPacks: Flow<List<StickerPack>> = stickerDao.getUserPacks().map { entities ->
        entities.map { entity -> mapPackEntityToDomain(entity) }
    }.flowOn(Dispatchers.IO)

    val communityPacks: Flow<List<StickerPack>> = stickerDao.getCommunityPacks().map { entities ->
        entities.map { entity -> mapPackEntityToDomain(entity) }
    }.flowOn(Dispatchers.IO)

    private suspend fun mapPackEntityToDomain(entity: StickerPackEntity): StickerPack {
        val stickers = stickerDao.getStickersForPackSync(entity.id).map { stickerEntity ->
            Sticker(
                id = stickerEntity.id,
                packId = stickerEntity.packId,
                fileName = stickerEntity.fileName,
                filePath = stickerEntity.filePath,
                emojis = stickerEntity.emojisJson.removeSurrounding("[", "]").replace("\"", "").split(",").filter { it.isNotBlank() },
                fileSizeBytes = stickerEntity.fileSizeBytes,
                isAnimated = stickerEntity.isAnimated
            )
        }
        return StickerPack(
            id = entity.id,
            name = entity.name,
            publisher = entity.publisher,
            trayImageFileName = entity.trayImageFileName,
            trayImagePath = entity.trayImagePath,
            isUserCreated = entity.isUserCreated,
            category = entity.category,
            downloadsCount = entity.downloadsCount,
            likesCount = entity.likesCount,
            stickers = stickers
        )
    }

    suspend fun getPackById(packId: String): StickerPack? = withContext(Dispatchers.IO) {
        val entity = stickerDao.getPackById(packId) ?: return@withContext null
        mapPackEntityToDomain(entity)
    }

    suspend fun createPack(
        name: String,
        publisher: String,
        category: String = "Geral",
        trayBitmap: Bitmap? = null
    ): StickerPack = withContext(Dispatchers.IO) {
        val packId = "pack_${UUID.randomUUID().toString().take(8)}"
        val trayName = "tray_$packId.webp"

        val effectiveTrayBitmap = trayBitmap ?: fileManager.createSampleStickerBitmap(
            label = name.take(3),
            emoji = "🎨",
            bgColor = 0xFF4F46E5.toInt()
        )
        val trayFile = fileManager.saveTrayIconWebp(packId, effectiveTrayBitmap)

        val entity = StickerPackEntity(
            id = packId,
            name = name,
            publisher = publisher,
            trayImageFileName = trayName,
            trayImagePath = trayFile.absolutePath,
            isUserCreated = true,
            category = category,
            downloadsCount = 0,
            likesCount = 0
        )
        stickerDao.insertPack(entity)

        return@withContext StickerPack(
            id = packId,
            name = name,
            publisher = publisher,
            trayImageFileName = trayName,
            trayImagePath = trayFile.absolutePath,
            isUserCreated = true,
            category = category,
            stickers = emptyList()
        )
    }

    suspend fun addStickerToPack(
        packId: String,
        bitmap: Bitmap,
        emojis: List<String> = listOf("✨")
    ): Sticker = withContext(Dispatchers.IO) {
        val count = stickerDao.getStickerCount(packId)
        val stickerId = "stk_${UUID.randomUUID().toString().take(8)}"
        val fileName = "sticker_${count + 1}.webp"

        val file = fileManager.saveStickerWebp(packId, fileName, bitmap)

        val entity = StickerEntity(
            id = stickerId,
            packId = packId,
            fileName = fileName,
            filePath = file.absolutePath,
            emojisJson = "[\"${emojis.joinToString("\",\"")}\"]",
            fileSizeBytes = file.length(),
            isAnimated = false,
            orderIndex = count
        )
        stickerDao.insertSticker(entity)

        return@withContext Sticker(
            id = stickerId,
            packId = packId,
            fileName = fileName,
            filePath = file.absolutePath,
            emojis = emojis,
            fileSizeBytes = file.length(),
            isAnimated = false
        )
    }

    suspend fun deletePack(packId: String) = withContext(Dispatchers.IO) {
        stickerDao.deletePack(packId)
    }

    suspend fun deleteSticker(stickerId: String) = withContext(Dispatchers.IO) {
        stickerDao.deleteSticker(stickerId)
    }

    /**
     * Seeds initial community and template sticker packs if empty.
     */
    suspend fun seedSampleDataIfNeeded() = withContext(Dispatchers.IO) {
        val existing = stickerDao.getPackById("pack_memes_br")
        if (existing != null) return@withContext

        // Pack 1: Memes Brasil 🔥
        val pack1Id = "pack_memes_br"
        val tray1 = fileManager.createSampleStickerBitmap("MEMES", "😂", 0xFFE11D48.toInt())
        val tray1File = fileManager.saveTrayIconWebp(pack1Id, tray1)
        stickerDao.insertPack(
            StickerPackEntity(
                id = pack1Id,
                name = "Memes Brasileiros 2026",
                publisher = "StickerGod",
                trayImageFileName = "tray_$pack1Id.webp",
                trayImagePath = tray1File.absolutePath,
                isUserCreated = false,
                category = "Memes",
                downloadsCount = 14200,
                likesCount = 3890
            )
        )

        val memesStickers = listOf(
            Triple("EITA!", "😱", 0xFFF59E0B.toInt()),
            Triple("RIR PRA NÃO CHORAR", "😭", 0xFF3B82F6.toInt()),
            Triple("CALMA CALABRESO", "🍗", 0xFFEF4444.toInt()),
            Triple("MANDA PIX", "💸", 0xFF10B981.toInt()),
            Triple("FOCO NO GOLPE", "🕵️", 0xFF8B5CF6.toInt())
        )
        memesStickers.forEachIndexed { index, (label, emoji, color) ->
            val b = fileManager.createSampleStickerBitmap(label, emoji, color)
            val f = fileManager.saveStickerWebp(pack1Id, "meme_${index + 1}.webp", b)
            stickerDao.insertSticker(
                StickerEntity(
                    id = "stk_m_$index",
                    packId = pack1Id,
                    fileName = "meme_${index + 1}.webp",
                    filePath = f.absolutePath,
                    emojisJson = "[\"$emoji\"]",
                    fileSizeBytes = f.length(),
                    orderIndex = index
                )
            )
        }

        // Pack 2: Fofuras & Gatinhos 🐱
        val pack2Id = "pack_cats_cute"
        val tray2 = fileManager.createSampleStickerBitmap("CATS", "🐱", 0xFFEC4899.toInt())
        val tray2File = fileManager.saveTrayIconWebp(pack2Id, tray2)
        stickerDao.insertPack(
            StickerPackEntity(
                id = pack2Id,
                name = "Gatinhos Reativos",
                publisher = "MiawClub",
                trayImageFileName = "tray_$pack2Id.webp",
                trayImagePath = tray2File.absolutePath,
                isUserCreated = false,
                category = "Reações",
                downloadsCount = 8900,
                likesCount = 2100
            )
        )

        val catsStickers = listOf(
            Triple("Julgando você", "😼", 0xFF6366F1.toInt()),
            Triple("Com soninho", "🥱", 0xFF14B8A6.toInt()),
            Triple("Gato pistola", "😾", 0xFFEF4444.toInt()),
            Triple("Amorzin", "😻", 0xFFF43F5E.toInt())
        )
        catsStickers.forEachIndexed { index, (label, emoji, color) ->
            val b = fileManager.createSampleStickerBitmap(label, emoji, color)
            val f = fileManager.saveStickerWebp(pack2Id, "cat_${index + 1}.webp", b)
            stickerDao.insertSticker(
                StickerEntity(
                    id = "stk_c_$index",
                    packId = pack2Id,
                    fileName = "cat_${index + 1}.webp",
                    filePath = f.absolutePath,
                    emojisJson = "[\"$emoji\"]",
                    fileSizeBytes = f.length(),
                    orderIndex = index
                )
            )
        }

        // Pack 3: User default pack template with 3 initial stickers
        val packUserId = "pack_user_my_first"
        val trayUser = fileManager.createSampleStickerBitmap("MEU", "⭐", 0xFF10B981.toInt())
        val trayUserFile = fileManager.saveTrayIconWebp(packUserId, trayUser)
        stickerDao.insertPack(
            StickerPackEntity(
                id = packUserId,
                name = "Minhas Figurinhas VIP",
                publisher = "Eu",
                trayImageFileName = "tray_$packUserId.webp",
                trayImagePath = trayUserFile.absolutePath,
                isUserCreated = true,
                category = "Geral",
                downloadsCount = 1,
                likesCount = 1
            )
        )

        val userStickers = listOf(
            Triple("BOM DIA GRUPO", "☕", 0xFFD97706.toInt()),
            Triple("CHEGUEI!", "🚀", 0xFF2563EB.toInt()),
            Triple("VALEU DEMAIS", "🤝", 0xFF059669.toInt())
        )
        userStickers.forEachIndexed { index, (label, emoji, color) ->
            val b = fileManager.createSampleStickerBitmap(label, emoji, color)
            val f = fileManager.saveStickerWebp(packUserId, "user_stk_${index + 1}.webp", b)
            stickerDao.insertSticker(
                StickerEntity(
                    id = "stk_u_$index",
                    packId = packUserId,
                    fileName = "user_stk_${index + 1}.webp",
                    filePath = f.absolutePath,
                    emojisJson = "[\"$emoji\"]",
                    fileSizeBytes = f.length(),
                    orderIndex = index
                )
            )
        }
    }
}
