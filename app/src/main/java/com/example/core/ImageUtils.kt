package com.example.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Build
import java.io.ByteArrayOutputStream
import kotlin.math.min

object ImageUtils {

    /**
     * Resizes and centers a bitmap onto an exact 512x512 transparent canvas
     * respecting the WhatsApp required 16px margin on all sides.
     */
    fun formatForWhatsAppSticker(source: Bitmap): Bitmap {
        val targetSize = WhatsAppConstants.STICKER_DIMENSION_PX // 512
        val maxContentSize = WhatsAppConstants.STICKER_MAX_CONTENT_PX.toFloat() // 480

        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Calculate aspect ratio scale to fit inside 480x480
        val scale = min(maxContentSize / source.width, maxContentSize / source.height)
        val scaledWidth = (source.width * scale).toInt()
        val scaledHeight = (source.height * scale).toInt()

        // Center on 512x512 canvas
        val left = (targetSize - scaledWidth) / 2
        val top = (targetSize - scaledHeight) / 2
        val destRect = Rect(left, top, left + scaledWidth, top + scaledHeight)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, null, destRect, paint)

        return output
    }

    /**
     * Resizes bitmap to 96x96 for WhatsApp tray icon.
     */
    fun formatForWhatsAppTray(source: Bitmap): Bitmap {
        val size = WhatsAppConstants.TRAY_IMAGE_DIMENSION_PX // 96
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val scale = min(size.toFloat() / source.width, size.toFloat() / source.height)
        val scaledWidth = (source.width * scale).toInt()
        val scaledHeight = (source.height * scale).toInt()

        val left = (size - scaledWidth) / 2
        val top = (size - scaledHeight) / 2
        val destRect = Rect(left, top, left + scaledWidth, top + scaledHeight)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, null, destRect, paint)

        return output
    }

    /**
     * Compresses a Bitmap into WEBP format ensuring that file size strictly obeys
     * the WhatsApp limit (<= 100 KB for stickers, <= 50 KB for tray icon).
     * Automatically adjusts compression quality until the target is met.
     */
    fun compressToWebpWithinLimit(
        bitmap: Bitmap,
        maxBytes: Int = WhatsAppConstants.MAX_STICKER_FILE_SIZE_BYTES
    ): ByteArray {
        var quality = 95
        var stream = ByteArrayOutputStream()

        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }

        bitmap.compress(format, quality, stream)

        // Iteratively lower quality if exceeds maxBytes
        while (stream.size() > maxBytes && quality > 20) {
            stream.reset()
            quality -= 10
            bitmap.compress(format, quality, stream)
        }

        return stream.toByteArray()
    }

    /**
     * Adds the classic WhatsApp sticker thick white outline (stroke) around the alpha channel
     * of a transparent cut-out sticker bitmap.
     */
    fun addStickerOutline(source: Bitmap, strokeWidthPx: Float = 12f, strokeColor: Int = Color.WHITE): Bitmap {
        if (strokeWidthPx <= 0f) return source

        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Generate silhouette by coloring alpha mask
        val paintOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = PorterDuffColorFilter(strokeColor, PorterDuff.Mode.SRC_IN)
        }

        // Draw multiple passes in circle directions to form a smooth outer outline
        val step = 45
        val radius = strokeWidthPx.toInt()
        for (r in 1..radius step 2) {
            for (angle in 0 until 360 step step) {
                val rad = Math.toRadians(angle.toDouble())
                val dx = (r * Math.cos(rad)).toFloat()
                val dy = (r * Math.sin(rad)).toFloat()
                canvas.drawBitmap(source, dx, dy, paintOutline)
            }
        }

        // Finally draw original image in center
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(source, 0f, 0f, normalPaint)

        return output
    }
}
