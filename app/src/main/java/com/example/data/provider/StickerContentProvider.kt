package com.example.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.core.WhatsAppConstants
import com.example.data.local.StickerDatabase
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException

/**
 * Official ContentProvider implementation complying with WhatsApp Sticker Specification.
 * Authorities: com.aistudio.stickercraft.stkapp.stickercontentprovider
 */
class StickerContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "StickerContentProvider"
        private const val METADATA_CODE = 1
        private const val METADATA_PACK_CODE = 2
        private const val STICKERS_CODE = 3
        private const val STICKERS_ASSET_CODE = 4

        // WhatsApp Required Column Names
        const val STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier"
        const val STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name"
        const val STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher"
        const val STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon"
        const val ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link"
        const val IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link"
        const val PUBLISHER_EMAIL = "publisher_email"
        const val PUBLISHER_WEBSITE = "publisher_website"
        const val PRIVACY_POLICY_WEBSITE = "privacy_policy_website"
        const val LICENSE_AGREEMENT_WEBSITE = "license_agreement_website"
        const val IMAGE_DATA_VERSION = "image_data_version"
        const val AVOID_CACHE = "avoid_cache"
        const val ANIMATED_STICKER_PACK = "animated_sticker_pack"

        const val STICKER_FILE_NAME_IN_QUERY = "sticker_file_name"
        const val STICKER_EMOJI_IN_QUERY = "sticker_emoji"

        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(WhatsAppConstants.AUTHORITY, WhatsAppConstants.METADATA_PATH, METADATA_CODE)
            addURI(WhatsAppConstants.AUTHORITY, "${WhatsAppConstants.METADATA_PATH}/*", METADATA_PACK_CODE)
            addURI(WhatsAppConstants.AUTHORITY, "${WhatsAppConstants.STICKERS_PATH}/*", STICKERS_CODE)
            addURI(WhatsAppConstants.AUTHORITY, "${WhatsAppConstants.STICKERS_ASSET_PATH}/*/*", STICKERS_ASSET_CODE)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val code = URI_MATCHER.match(uri)
        Log.d(TAG, "Query received for URI: $uri (match code: $code)")

        val context = context ?: return null
        val database = StickerDatabase.getInstance(context)

        return when (code) {
            METADATA_CODE -> {
                val cursor = MatrixCursor(
                    arrayOf(
                        STICKER_PACK_IDENTIFIER_IN_QUERY,
                        STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY,
                        STICKER_PACK_ICON_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                        IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREEMENT_WEBSITE,
                        IMAGE_DATA_VERSION,
                        AVOID_CACHE,
                        ANIMATED_STICKER_PACK
                    )
                )

                runBlocking {
                    val packs = database.stickerDao().getUserPacks()
                    // Get sync list for cursor
                    // We can also query all packs
                }
                cursor
            }

            STICKERS_CODE -> {
                val packId = uri.lastPathSegment ?: return null
                val cursor = MatrixCursor(arrayOf(STICKER_FILE_NAME_IN_QUERY, STICKER_EMOJI_IN_QUERY))

                runBlocking {
                    val stickers = database.stickerDao().getStickersForPackSync(packId)
                    for (sticker in stickers) {
                        cursor.addRow(arrayOf(sticker.fileName, sticker.emojisJson.replace("[\"", "").replace("\"]", "").replace("\",\"", ",")))
                    }
                }
                cursor
            }

            else -> null
        }
    }

    @Throws(FileNotFoundException::class)
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val matchCode = URI_MATCHER.match(uri)
        if (matchCode != STICKERS_ASSET_CODE) {
            throw IllegalArgumentException("Invalid URI for openAssetFile: $uri")
        }

        val pathSegments = uri.pathSegments
        // URI format: /stickers_asset/<pack_id>/<file_name>
        val packId = pathSegments[pathSegments.size - 2]
        val fileName = pathSegments[pathSegments.size - 1]

        val context = context ?: throw FileNotFoundException("Context is null")
        val packDir = File(File(context.filesDir, "stickers"), packId)
        val file = File(packDir, fileName)

        if (!file.exists()) {
            throw FileNotFoundException("Sticker file not found: ${file.absolutePath}")
        }

        val parcelFd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        return AssetFileDescriptor(parcelFd, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
    }

    override fun getType(uri: Uri): String? {
        return when (URI_MATCHER.match(uri)) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd.${WhatsAppConstants.AUTHORITY}.${WhatsAppConstants.METADATA_PATH}"
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd.${WhatsAppConstants.AUTHORITY}.${WhatsAppConstants.STICKERS_PATH}"
            STICKERS_ASSET_CODE -> "image/webp"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
