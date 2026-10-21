package com.example.data.whatsapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.core.WhatsAppConstants
import com.example.domain.model.StickerPack

sealed class ExportResult {
    data class Success(val targetApp: String) : ExportResult()
    data class ValidationError(val message: String) : ExportResult()
    data class AppNotInstalled(val message: String) : ExportResult()
    data class Error(val throwable: Throwable) : ExportResult()
}

class WhatsAppExporter(private val context: Context) {

    /**
     * Checks if standard WhatsApp is installed.
     */
    fun isWhatsAppInstalled(): Boolean {
        return isPackageInstalled(WhatsAppConstants.PACKAGE_WHATSAPP)
    }

    /**
     * Checks if WhatsApp Business is installed.
     */
    fun isWhatsAppBusinessInstalled(): Boolean {
        return isPackageInstalled(WhatsAppConstants.PACKAGE_WHATSAPP_BUSINESS)
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Creates the official WhatsApp Sticker Pack Intent.
     */
    fun createAddStickerPackIntent(pack: StickerPack, targetBusiness: Boolean = false): Intent {
        val targetPackage = if (targetBusiness) {
            WhatsAppConstants.PACKAGE_WHATSAPP_BUSINESS
        } else {
            WhatsAppConstants.PACKAGE_WHATSAPP
        }

        return Intent().apply {
            action = WhatsAppConstants.ACTION_ENABLE_STICKER_PACK
            putExtra(WhatsAppConstants.EXTRA_STICKER_PACK_ID, pack.id)
            putExtra(WhatsAppConstants.EXTRA_STICKER_PACK_AUTHORITY, WhatsAppConstants.AUTHORITY)
            putExtra(WhatsAppConstants.EXTRA_STICKER_PACK_NAME, pack.name)
            setPackage(targetPackage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Validates pack compliance before attempting WhatsApp export.
     */
    fun validatePack(pack: StickerPack): String? {
        if (pack.stickers.size < WhatsAppConstants.MIN_STICKERS_PER_PACK) {
            return "O pacote possui apenas ${pack.stickers.size} figurinhas. O WhatsApp exige no mínimo 3."
        }
        if (pack.stickers.size > WhatsAppConstants.MAX_STICKERS_PER_PACK) {
            return "O pacote possui ${pack.stickers.size} figurinhas. O WhatsApp permite no máximo 30."
        }
        if (pack.name.isBlank()) {
            return "O título do pacote não pode estar vazio."
        }
        if (pack.publisher.isBlank()) {
            return "O nome do criador/autor não pode estar vazio."
        }
        return null
    }
}
