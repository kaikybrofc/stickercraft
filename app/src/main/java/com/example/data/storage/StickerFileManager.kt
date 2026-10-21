package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.core.ImageUtils
import com.example.core.WhatsAppConstants
import java.io.File
import java.io.FileOutputStream

class StickerFileManager(private val context: Context) {

    private val stickersDir: File
        get() = File(context.filesDir, "stickers").apply { if (!exists()) mkdirs() }

    fun getPackDirectory(packId: String): File {
        val dir = File(stickersDir, packId)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Saves a 512x512 sticker bitmap as WebP with guaranteed <= 100KB file size.
     */
    fun saveStickerWebp(packId: String, fileName: String, bitmap: Bitmap): File {
        val packDir = getPackDirectory(packId)
        val file = File(packDir, if (fileName.endsWith(".webp")) fileName else "$fileName.webp")

        // Format to exact 512x512 with safe margin
        val formatted = ImageUtils.formatForWhatsAppSticker(bitmap)
        val bytes = ImageUtils.compressToWebpWithinLimit(formatted, WhatsAppConstants.MAX_STICKER_FILE_SIZE_BYTES)

        FileOutputStream(file).use { it.write(bytes) }
        return file
    }

    /**
     * Saves a 96x96 tray icon bitmap as WebP with guaranteed <= 50KB file size.
     */
    fun saveTrayIconWebp(packId: String, bitmap: Bitmap): File {
        val packDir = getPackDirectory(packId)
        val file = File(packDir, "tray_$packId.webp")

        val formatted = ImageUtils.formatForWhatsAppTray(bitmap)
        val bytes = ImageUtils.compressToWebpWithinLimit(formatted, WhatsAppConstants.MAX_TRAY_FILE_SIZE_BYTES)

        FileOutputStream(file).use { it.write(bytes) }
        return file
    }

    fun getStickerFile(packId: String, fileName: String): File? {
        val file = File(getPackDirectory(packId), fileName)
        return if (file.exists()) file else null
    }

    /**
     * Creates a demo sticker bitmap with emojis and stylized background for sample packs.
     */
    fun createSampleStickerBitmap(label: String, emoji: String, bgColor: Int = Color.TRANSPARENT): Bitmap {
        val size = WhatsAppConstants.STICKER_DIMENSION_PX
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (bgColor != Color.TRANSPARENT) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - 30f, bgPaint)
        }

        // Draw big emoji
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 180f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(emoji, size / 2f, size / 2f + 40f, emojiPaint)

        // Draw sticker label at bottom
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 54f
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 2f, 2f, Color.BLACK)
            isFakeBoldText = true
        }
        canvas.drawText(label, size / 2f, size - 45f, textPaint)

        return bitmap
    }
}
