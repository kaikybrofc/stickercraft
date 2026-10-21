package com.example.core

/**
 * Constants defined according to the official WhatsApp Sticker Specification.
 * Reference: https://github.com/WhatsApp/stickers
 */
object WhatsAppConstants {
    const val AUTHORITY = "com.aistudio.stickercraft.stkapp.stickercontentprovider"

    // WhatsApp packages
    const val PACKAGE_WHATSAPP = "com.whatsapp"
    const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    // Intent actions and extras for exporting sticker packs
    const val ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
    const val EXTRA_STICKER_PACK_ID = "extra_sticker_pack_id"
    const val EXTRA_STICKER_PACK_AUTHORITY = "extra_sticker_pack_authority"
    const val EXTRA_STICKER_PACK_NAME = "extra_sticker_pack_name"

    // Provider URIs and path segments
    const val METADATA_PATH = "metadata"
    const val STICKERS_PATH = "stickers"
    const val STICKERS_ASSET_PATH = "stickers_asset"

    // Technical WhatsApp constraints
    const val STICKER_DIMENSION_PX = 512
    const val STICKER_SAFETY_MARGIN_PX = 16
    const val STICKER_MAX_CONTENT_PX = 480 // 512 - 2 * 16

    const val TRAY_IMAGE_DIMENSION_PX = 96
    const val MAX_STICKER_FILE_SIZE_BYTES = 100 * 1024 // 100 KB max per sticker
    const val MAX_TRAY_FILE_SIZE_BYTES = 50 * 1024    // 50 KB max for tray icon

    const val MIN_STICKERS_PER_PACK = 3
    const val MAX_STICKERS_PER_PACK = 30
}
